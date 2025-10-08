#pragma once


#include <functional>
#include <memory>
#include <string>

class ArchiveExtractor {
public:
    using ProgressListener = std::function<void(const std::string &current_file,
                                                size_t current_index, size_t total_files)>;

    ArchiveExtractor(
            std::string archive_path,
            std::string output_dir,
            ProgressListener listener = nullptr,
            bool overwrite = true
    );

    void Extract();

    ArchiveExtractor &SetListener(ProgressListener l) {
        listener_ = std::move(l);
        return *this;
    }

    ArchiveExtractor &SetOverwrite(bool o) {
        overwrite_ = o;
        return *this;
    }

private:
    std::string archive_path_;
    std::string output_dir_;
    ProgressListener listener_;
    bool overwrite_;

    size_t CountFilesInArchive();
};
