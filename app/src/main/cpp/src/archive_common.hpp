#pragma once

#include <archive.h>
#include <archive_entry.h>

enum class ArchiveFormat {
    Tar = 0, Cpio = 1, Zip = 2
};

enum class CompressionType {
    None = 0, Gzip = 1, Bzip2 = 2, Xz = 3, Lz4 = 4, Zstd = 5
};

struct ArchiveDeleter {
    void operator()(archive *a) const {
        if (a == nullptr) return;
        archive_write_close(a);
        archive_write_free(a);
    }
};

struct ArchiveEntryDeleter {
    void operator()(archive_entry *e) const {
        if (e) archive_entry_free(e);
    }
};

struct ArchiveReadDeleter {
    void operator()(archive *a) const {
        if (a == nullptr) return;
        archive_read_close(a);
        archive_read_free(a);
    }
};

struct ArchiveWriteDiskDeleter {
    void operator()(archive *a) const {
        if (a == nullptr) return;
        archive_write_close(a);
        archive_write_free(a);
    }
};


/**
 * 创建Archive对象
 */
inline std::unique_ptr<archive, ArchiveDeleter> CreateArchive() {
    return std::unique_ptr<archive, ArchiveDeleter>(archive_write_new());
}

/**
 * 创建 archive_entry
 */
inline std::unique_ptr<archive_entry, ArchiveEntryDeleter>
CreateArchiveEntry(const std::string &entry_name, mode_t perm = 0755) {
    auto entry = std::unique_ptr<archive_entry, ArchiveEntryDeleter>(archive_entry_new());
    archive_entry_set_pathname_utf8(entry.get(), entry_name.c_str());
    archive_entry_set_perm(entry.get(), perm);
    return entry;
}

/**
 * 创建 Archive Reader 对象
 * execute: archive_read_support_format_all / archive_read_support_filter_all
 * @throw std::runtime_error 创建失败将抛出此异常
 */
inline auto CreateArchiveReader() {
    auto ptr = std::unique_ptr<archive, ArchiveReadDeleter>(archive_read_new());
    if (!ptr) throw std::runtime_error("Failed to create archive reader");
    archive_read_support_format_all(ptr.get());
    archive_read_support_filter_all(ptr.get());
    return ptr;
}

/**
 * 打开并返回已打开的 reader
 * execute: archive_read_open_filename
 * @throw std::runtime_error 创建失败或打开失败将抛出此异常
 */
inline auto CreateArchiveReader(const std::string &archive_path, size_t block_size) {
    auto reader = CreateArchiveReader();
    if (archive_read_open_filename(reader.get(), archive_path.c_str(), block_size) != ARCHIVE_OK) {
        auto err = archive_error_string(reader.get());
        throw std::runtime_error(std::string("Failed to open archive: ") + (err ? err : "unknown"));
    }
    return reader;
}

/**
 * 创建 Archive Write Disk 对象
 * 用于解压时将内容写入磁盘，并自动设置常用提取选项
 * @throw std::runtime_error 创建失败将抛出此异常
 */
inline auto CreateArchiveWriteDisk(long options) {
    auto ptr = std::unique_ptr<archive, ArchiveWriteDiskDeleter>(archive_write_disk_new());
    if (!ptr) throw std::runtime_error("Failed to create archive_write_disk");
    archive_write_disk_set_options(ptr.get(), options);
    return ptr;
}
