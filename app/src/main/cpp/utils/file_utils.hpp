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

/**
 * 路径标准化
 */
static std::string NormalizePath(const std::string &path) {
    if (path.empty()) return path;
    std::string normalized = path;
    // 统一使用正斜杠
    std::replace(normalized.begin(), normalized.end(), '\\', '/');
    // 移除末尾斜杠（除非是根路径"/"）
    if (normalized.length() > 1 && normalized.back() == '/') {
        normalized.pop_back();
    }
    return normalized;
}

/**
 * 从路径提取文件名
 */
static std::string ExtractNameFromPath(const std::string &normalized_path, bool is_directory) {
    if (normalized_path.empty() || normalized_path == "/") {
        return "/";
    }

    std::string name = normalized_path;
    // 如果是目录且有尾部斜杠
    if (is_directory && name.length() > 1 && name.back() == '/') {
        name.pop_back();
    }

    auto last_slash = name.find_last_of('/');
    if (last_slash != std::string::npos && last_slash < name.length() - 1) {
        return name.substr(last_slash + 1);
    }
    return name;
}
