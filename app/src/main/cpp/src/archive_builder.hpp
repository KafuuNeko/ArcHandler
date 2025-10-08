#pragma once

#include <functional>
#include <string>
#include <filesystem>

#include "archive_common.hpp"

class ArchiveBuilder {
public:
    using ProgressListener = std::function<
            void(const std::string &current_file, size_t current_index, size_t total_files)>;

    ArchiveBuilder(
            std::string output_path,
            std::string base_dir,
            std::vector<std::string> input_files,
            ProgressListener listener = nullptr,
            ArchiveFormat format = ArchiveFormat::Tar,
            CompressionType compression = CompressionType::None,
            int32_t compression_level = 0
    );

    void Create();

    ArchiveBuilder &SetFormat(ArchiveFormat fmt) {
        format_ = fmt;
        return *this;
    }

    ArchiveBuilder &SetCompression(CompressionType c) {
        compression_ = c;
        return *this;
    }

    ArchiveBuilder &SetCompressionLevel(int32_t lvl) {
        compression_level_ = lvl;
        return *this;
    }

    ArchiveBuilder &SetListener(ProgressListener l) {
        listener_ = std::move(l);
        return *this;
    }

private:
    std::unique_ptr<struct archive, ArchiveDeleter> archive_;
    std::string output_path_;
    std::string base_dir_;
    std::vector<std::string> input_files_;
    ProgressListener listener_;
    ArchiveFormat format_;
    CompressionType compression_;
    int32_t compression_level_;

    int32_t ConfigureZipOptions(CompressionType compression, int32_t compression_level);

    int32_t AddFilterAndSetLevel(CompressionType compression, int32_t compression_level);

    void SetArchiveFormat(ArchiveFormat format);

    void WriteHeaderOrThrow(struct archive_entry *entry, const std::filesystem::path &path);

    void WriteFileToArchive(const std::filesystem::path &path);

    void AddToArchive(const std::filesystem::path &path,
                      const std::function<void(const std::string &path)> &on_progress);
};

