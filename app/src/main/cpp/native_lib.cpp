// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("archandler");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("archandler")
//      }
//    }

#include <archive.h>
#include <archive_entry.h>
#include <sys/stat.h>
#include <iostream>
#include <memory>
#include <vector>
#include <string>
#include <stdexcept>
#include <fstream>

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

void create_tar_gz(
        const std::string &outputPath,
        const std::vector <std::string> &inputFiles
) {
    std::unique_ptr <archive, ArchiveDeleter> archivePtr(archive_write_new());
    if (!archivePtr) {
        throw std::runtime_error("Failed to create archive object.");
    }

    archive_write_add_filter_gzip(archivePtr.get());
    archive_write_set_format_pax_restricted(archivePtr.get());

    if (archive_write_open_filename(archivePtr.get(), outputPath.c_str()) != ARCHIVE_OK) {
        throw std::runtime_error(
                std::string("Failed to open output archive: ") +
                archive_error_string(archivePtr.get())
        );
    }

    for (const auto &filepath: inputFiles) {
        struct stat st{};
        if (stat(filepath.c_str(), &st) != 0) {
            throw std::runtime_error("Cannot stat file: " + filepath);
        }

        std::unique_ptr <archive_entry, ArchiveEntryDeleter> entry(archive_entry_new());
        archive_entry_set_pathname(entry.get(), filepath.c_str());
        archive_entry_set_size(entry.get(), st.st_size);
        archive_entry_set_filetype(entry.get(), AE_IFREG);
        archive_entry_set_perm(entry.get(), 0644);

        if (archive_write_header(archivePtr.get(), entry.get()) != ARCHIVE_OK) {
            throw std::runtime_error(
                    std::string("Failed to write header for ") + filepath + ": " +
                    archive_error_string(archivePtr.get())
            );
        }

        std::ifstream in(filepath, std::ios::binary);
        if (!in) {
            throw std::runtime_error("Cannot open file: " + filepath);
        }

        std::vector<char> buffer(8192);
        while (in) {
            in.read(buffer.data(), buffer.size());
            std::streamsize bytesRead = in.gcount();
            if (bytesRead == 0) continue;
            if (archive_write_data(archivePtr.get(), buffer.data(), bytesRead) < 0) {
                throw std::runtime_error(
                        std::string("Write data error for ") + filepath + ": " +
                        archive_error_string(archivePtr.get())
                );
            }
        }
    }
}