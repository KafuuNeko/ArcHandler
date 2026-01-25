#pragma once


#include <functional>
#include <memory>
#include <string>

class ArchiveExtractor {
public:
    using ProgressListener = std::function<void(const std::string &current_file,
                                                size_t current_index, size_t total_files)>;

    struct ArchiveEntry {
        std::string pathname;
        mode_t mode;
        int64_t modify_time_ms;
        int64_t entry_size;
    };

    explicit ArchiveExtractor(std::string archive_path);

    [[nodiscard]] std::vector<ArchiveEntry> ListEntry() const;

    void Extract(
            const std::string& output_dir,
            const ProgressListener& listener = nullptr,
            bool overwrite = true
    ) const;

    struct TestResult {
        bool success;
        std::string error_message;
        size_t tested_files;
        size_t total_files;
    };

    [[nodiscard]] TestResult Test(const ProgressListener& listener = nullptr) const;

private:
    std::string archive_path_;

    [[nodiscard]] size_t CountFilesInArchive() const;
};

