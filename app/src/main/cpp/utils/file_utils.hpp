#pragma once

#include <cinttypes>
#include <vector>
#include <string>
#include <filesystem>

/**
 * 计算所有文件数量
 */
static size_t CountFilesRecursively(const std::vector<std::string> &input_files) {
    size_t count = 0;
    for (const auto &f : input_files) {
        if (!std::filesystem::exists(f)) continue;
        if (std::filesystem::is_directory(f)) {
            for (auto &p : std::filesystem::recursive_directory_iterator(f)) {
                if (std::filesystem::is_regular_file(p)) ++count;
            }
        } else if (std::filesystem::is_regular_file(f)) {
            ++count;
        }
    }
    return count;
}
