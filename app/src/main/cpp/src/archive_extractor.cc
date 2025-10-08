// archive_extractor.cc
#include "archive_extractor.hpp"
#include "archive_common.hpp"

#include <archive.h>
#include <archive_entry.h>

#include <stdexcept>
#include <filesystem>
#include <string>
#include <memory>
#include <utility>
#include <vector>
#include <system_error>

constexpr size_t WRITE_BUFFER_SIZE = 8192;
constexpr size_t READ_BLOCK_SIZE = 10240;

ArchiveExtractor::ArchiveExtractor(
        std::string archive_path,
        std::string output_dir,
        ProgressListener listener,
        bool overwrite
) : archive_path_(std::move(archive_path)), output_dir_(std::move(output_dir)),
    listener_(std::move(listener)), overwrite_(overwrite) {};

size_t ArchiveExtractor::CountFilesInArchive() {
    auto reader = CreateArchiveReader(archive_path_, READ_BLOCK_SIZE);
    size_t count = 0;
    struct archive_entry *entry = nullptr;
    while (true) {
        auto rc = archive_read_next_header(reader.get(), &entry);
        if (rc == ARCHIVE_EOF) break;
        if (rc < ARCHIVE_OK) {
            auto err = archive_error_string(reader.get());
            throw std::runtime_error(std::string("Error while counting archive entries: ") +
                                     (err ? err : "unknown"));
        }
        // 只计算常规文件数量
        if (archive_entry_filetype(entry) == AE_IFREG) ++count;
    }
    return count;
}

// Extract Helpers
namespace {
    /**
     * 将归档内路径（archive_entry）映射到目标文件系统路径
     */
    std::filesystem::path
    ResolveDestinationPath(const std::string &output_dir, struct archive_entry *entry) {
        auto orig_path = archive_entry_pathname(entry);
        if (!orig_path) return {};
        return std::filesystem::path(output_dir) / std::string(orig_path);
    }

    /**
     * 确保目标文件的父目录存在；失败不会抛出错误
     */
    void EnsureParentDirectories(const std::filesystem::path &dest) noexcept {
        if (!dest.has_parent_path()) return;
        std::error_code ec;
        std::filesystem::create_directories(dest.parent_path(), ec);
    }

    /**
     * 写 header
     * @throw std::runtime_error 包含 archive 的错误
     */
    void WriteHeaderOrThrow(
            archive *disk,
            struct archive_entry *entry,
            const std::filesystem::path &dest
    ) {
        if (archive_write_header(disk, entry) == ARCHIVE_OK) return;
        auto err = archive_error_string(disk);
        throw std::runtime_error(
                std::string("Failed to write header for ") + dest.string() + ": " +
                (err ? err : "unknown"));
    }

    /**
     * 从reader读取数据并写入disk
     * @throw std::runtime_error 读取或者写入失败的时候抛出此错误
     */
    void CopyEntryDataOrThrow(
            archive *reader,
            archive *disk,
            const std::filesystem::path &dest,
            std::vector<char> &buffer
    ) {
        while (true) {
            auto len = archive_read_data(reader, buffer.data(), buffer.size());
            if (len == 0) break;
            if (len < 0) {
                auto err = archive_error_string(reader);
                throw std::runtime_error(
                        std::string("Error reading data from archive for ") + dest.string() + ": " +
                        (err ? err : "unknown"));
            }
            auto wrote = archive_write_data(disk, buffer.data(), len);
            if (wrote < 0) {
                auto err = archive_error_string(disk);
                throw std::runtime_error(
                        std::string("Error writing data to disk for ") + dest.string() + ": " +
                        (err ? err : "unknown"));
            }
        }
    }

    /**
     * 完成条目（finish entry）
     * @throw std::runtime_error 调用archive_write_finish_entry失败时将抛出错误信息
     */
    void FinishEntryOrThrow(archive *disk, const std::filesystem::path &dest) {
        if (archive_write_finish_entry(disk) == ARCHIVE_OK) return;
        auto err = archive_error_string(disk);
        throw std::runtime_error(std::string("Failed to finish entry ") + dest.string() + ": " +
                                 (err ? err : "unknown"));
    }
}

void ArchiveExtractor::Extract() {
    // 统计total_files（可能抛出）
    size_t total_files = CountFilesInArchive();

    // 打开reader与disk
    auto reader = CreateArchiveReader(archive_path_, READ_BLOCK_SIZE);
    long disk_options = ARCHIVE_EXTRACT_TIME | ARCHIVE_EXTRACT_PERM | ARCHIVE_EXTRACT_ACL |
                        ARCHIVE_EXTRACT_FFLAGS;
    if (!overwrite_) disk_options |= ARCHIVE_EXTRACT_NO_OVERWRITE;
    auto disk = CreateArchiveWriteDisk(disk_options);

    struct archive_entry *entry = nullptr;
    std::vector<char> buffer(WRITE_BUFFER_SIZE);

    size_t current_index = 0;
    while (true) {
        int rc = archive_read_next_header(reader.get(), &entry);
        if (rc == ARCHIVE_EOF) break;
        if (rc < ARCHIVE_OK) {
            auto err = archive_error_string(reader.get());
            throw std::runtime_error(
                    std::string("Failed to read next header: ") + (err ? err : "unknown"));
        }

        // 解析目标路径并准备目录
        std::filesystem::path dest = ResolveDestinationPath(output_dir_, entry);
        if (dest.empty()) continue;
        EnsureParentDirectories(dest);

        // 将entry pathname替换为目标路径（写到output_dir）
        archive_entry_set_pathname(entry, dest.string().c_str());

        // 写header（根据entry type创建目录、链接或准备写入文件）
        WriteHeaderOrThrow(disk.get(), entry, dest);

        // 如果是常规文件则复制数据并报告进度
        if (archive_entry_filetype(entry) == AE_IFREG) {
            if (listener_) listener_(dest.string(), ++current_index, total_files);
            CopyEntryDataOrThrow(reader.get(), disk.get(), dest, buffer);
        }

        FinishEntryOrThrow(disk.get(), dest);
    }
}