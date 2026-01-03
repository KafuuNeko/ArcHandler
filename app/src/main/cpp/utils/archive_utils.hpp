//
// Created by kafuu on 2026/1/4.
//

#pragma once

#include <map>
#include <archive_entry.h>

#include "src/archive_extractor.hpp"
#include "file_utils.hpp"

/**
 * 基于ArchiveEntry列表构建完整的文件列表（包含缺失的目录）
 */
static std::map<std::string, ArchiveExtractor::ArchiveEntry> BuildCompleteEntryMap(
        const std::vector<ArchiveExtractor::ArchiveEntry> &raw_entries
) {

    std::map<std::string, ArchiveExtractor::ArchiveEntry> entry_map;

    for (const auto &entity: raw_entries) {
        std::string normalized_path = NormalizePath(entity.pathname);
        if (normalized_path.empty()) continue;

        // 存入当前条目
        entry_map[normalized_path] = entity;

        // 循环向上补全父目录
        std::string current_path = normalized_path;
        while (true) {
            auto last_slash = current_path.find_last_of('/');
            if (last_slash == std::string::npos || last_slash == 0) break;

            std::string parent_path = current_path.substr(0, last_slash);

            // 如果父目录不存在于map中，创建一个虚拟目录条目
            if (entry_map.find(parent_path) == entry_map.end()) {
                ArchiveExtractor::ArchiveEntry dir_entry;
                dir_entry.pathname = parent_path;
                dir_entry.mode = AE_IFDIR;
                dir_entry.entry_size = 0;
                dir_entry.modify_time_ms = entity.modify_time_ms;
                entry_map[parent_path] = dir_entry;
            }
            current_path = parent_path;
        }
    }
    return entry_map;
}