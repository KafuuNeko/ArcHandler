#include <archive.h>
#include <archive_entry.h>
#include <algorithm>
#include <cerrno>
#include <cstdint>
#include <filesystem>
#include <functional>
#include <memory>
#include <stdexcept>
#include <string>
#include <vector>
#include <fstream>

#include "archive_builder.hpp"
#include "utils/file_utils.hpp"

ArchiveBuilder::ArchiveBuilder(
        std::string output_path,
        std::string base_dir,
        std::vector<std::string> input_files,
        ProgressListener listener,
        ArchiveFormat format,
        CompressionType compression,
        int32_t compression_level
) : archive_(std::move(CreateArchive())),
    output_path_(std::move(output_path)),
    base_dir_(std::move(base_dir)),
    input_files_(std::move(input_files)),
    listener_(std::move(listener)),
    format_(format),
    compression_(compression),
    compression_level_(compression_level) {}

int32_t
ArchiveBuilder::ConfigureZipOptions(CompressionType compression, int32_t compression_level) {
    auto lvl = std::clamp(compression_level, 0, 9);
    std::string opt = "zip:hdrcharset=UTF-8";
    if (lvl == 0 || compression == CompressionType::None) {
        opt += ",zip:compression=store";
    } else {
        opt += ",zip:compression=deflate";
        opt += "zip:compression-level=" + std::to_string(lvl);
    }
    return archive_write_set_options(archive_.get(), opt.c_str());
}

int32_t
ArchiveBuilder::AddFilterAndSetLevel(CompressionType compression, int32_t compression_level) {
    int32_t rc = ARCHIVE_OK;
    switch (compression) {
        case CompressionType::None:
            rc = archive_write_add_filter_none(archive_.get());
            break;
        case CompressionType::Gzip: {
            rc = archive_write_add_filter_gzip(archive_.get());
            if (rc != ARCHIVE_OK) return rc;
            int lvl = std::clamp(compression_level, 1, 9);
            std::string opt = "gzip:compression-level=" + std::to_string(lvl);
            rc = archive_write_set_options(archive_.get(), opt.c_str());
            break;
        }
        case CompressionType::Bzip2: {
            rc = archive_write_add_filter_bzip2(archive_.get());
            if (rc != ARCHIVE_OK) return rc;
            int lvl = std::clamp(compression_level, 1, 9);
            std::string opt = "bzip2:compression-level=" + std::to_string(lvl);
            rc = archive_write_set_options(archive_.get(), opt.c_str());
            break;
        }
        case CompressionType::Xz: {
            rc = archive_write_add_filter_xz(archive_.get());
            if (rc != ARCHIVE_OK) return rc;
            int lvl = std::clamp(compression_level, 0, 9);
            std::string opt = "xz:compression-level=" + std::to_string(lvl);
            rc = archive_write_set_options(archive_.get(), opt.c_str());
            break;
        }
        case CompressionType::Lz4: {
            rc = archive_write_add_filter_lz4(archive_.get());
            if (rc != ARCHIVE_OK) return rc;
            rc = archive_write_set_options(archive_.get(), nullptr);
            break;
        }
        case CompressionType::Zstd: {
            rc = archive_write_add_filter_zstd(archive_.get());
            if (rc != ARCHIVE_OK) return rc;
            int lvl = std::clamp(compression_level, 1, 19);
            std::string opt = "compression-level=" + std::to_string(lvl);
            rc = archive_write_set_options(archive_.get(), opt.c_str());
            break;
        }
    }
    return rc;
}

void ArchiveBuilder::SetArchiveFormat(ArchiveFormat format) {
    switch (format) {
        case ArchiveFormat::Tar:
            archive_write_set_format_pax(archive_.get());
            break;
        case ArchiveFormat::Cpio:
            archive_write_set_format_cpio_newc(archive_.get());
            break;
        case ArchiveFormat::Zip:
            archive_write_set_format_zip(archive_.get());
            break;
    }
}


/**
 * 写入 header 并处理错误
 */
void ArchiveBuilder::WriteHeaderOrThrow(archive_entry *entry, const std::filesystem::path &path) {
    if (archive_write_header(archive_.get(), entry) != ARCHIVE_OK) {
        throw std::runtime_error(
                "Failed to write header for " + path.string() + ": " +
                archive_error_string(archive_.get())
        );
    }
}

/**
 * 写文件内容到 archive
 */
void ArchiveBuilder::WriteFileToArchive(const std::filesystem::path &path) {
    std::ifstream in(path, std::ios::binary);
    if (!in) throw std::runtime_error("Cannot open file: " + path.string());

    std::vector<char> buffer(8192);
    while (in) {
        in.read(buffer.data(), buffer.size());
        std::streamsize bytesRead = in.gcount();
        if (bytesRead == 0) break;

        if (archive_write_data(archive_.get(), buffer.data(), bytesRead) < 0) {
            throw std::runtime_error(
                    "Write data error for " + path.string() + ": " +
                    archive_error_string(archive_.get())
            );
        }
    }
}

/**
 * 递归低将给定的路径添加到压缩包
 */
void ArchiveBuilder::AddToArchive(
        const std::filesystem::path &path,
        const std::function<void(const std::string &path)> &on_progress
) {
    struct stat st{};
    if (stat(path.c_str(), &st) != 0) {
        throw std::runtime_error("Cannot stat file: " + path.string());
    }

    auto entry_name = std::filesystem::relative(path, base_dir_).u8string();
    if (S_ISDIR(st.st_mode) && !entry_name.empty() && entry_name.back() != '/') entry_name += '/';
    if (entry_name.empty()) return;

    auto entry = CreateArchiveEntry(entry_name);
    if (S_ISDIR(st.st_mode)) {
        archive_entry_set_filetype(entry.get(), AE_IFDIR);
        archive_entry_set_size(entry.get(), 0);
        WriteHeaderOrThrow(entry.get(), path);

        for (const auto &p: std::filesystem::directory_iterator(path)) {
            AddToArchive(p.path(), on_progress);
        }
    } else if (S_ISREG(st.st_mode)) {
        if (listener_) on_progress(path.string());
        archive_entry_set_filetype(entry.get(), AE_IFREG);
        archive_entry_set_size(entry.get(), st.st_size);
        WriteHeaderOrThrow(entry.get(), path);
        WriteFileToArchive(path);
    }
}


void ArchiveBuilder::Create() {
    if (!archive_) throw std::runtime_error("Failed to create archive object.");

    int rc = ARCHIVE_OK;
    if (format_ == ArchiveFormat::Zip) {
        SetArchiveFormat(format_);
        rc = ConfigureZipOptions(compression_, compression_level_);
        if (rc != ARCHIVE_OK) {
            throw std::runtime_error(
                    "Failed to set zip options: " +
                    std::string(archive_error_string(archive_.get())));
        }
    } else {
        rc = AddFilterAndSetLevel(compression_, compression_level_);
        if (rc != ARCHIVE_OK) {
            throw std::runtime_error("Failed to set compression filter/options: " +
                                     std::string(archive_error_string(archive_.get())));
        }
        SetArchiveFormat(format_);
    }

    if (archive_write_open_filename(archive_.get(), output_path_.c_str()) != ARCHIVE_OK) {
        throw std::runtime_error(
                "Failed to open output archive: " +
                std::string(archive_error_string(archive_.get())));
    }

    size_t total_files = CountFilesRecursively(input_files_);
    size_t current_index = 0;
    for (const auto &file: input_files_) {
        AddToArchive(file, [&](const std::string &path) {
            if (listener_) listener_(path, ++current_index, total_files);
        });
    }
}
