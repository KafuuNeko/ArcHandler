
#include <jni.h>
#include <vector>
#include <string>
#include <map>
#include <algorithm>
#include <filesystem>
#include <archive_entry.h>

#include "native_logger.hpp"
#include "utils/jni_utils.hpp"
#include "src/archive_builder.hpp"
#include "src/archive_extractor.hpp"

#define JNI_METHOD(cls, name) Java_cc_kafuu_archandler_libs_jni_##cls##_##name

namespace internal {
    constexpr auto k_log_tag = "native_lib";
    static auto s_latest_error_message = std::string("none");

    jboolean CreateArchive(
            JNIEnv *env,
            jstring output_path,
            jstring base_dir,
            jobject input_files,
            jobject listener,
            ArchiveFormat format = ArchiveFormat::TarPax,
            CompressionType compression = CompressionType::None,
            jint compression_level = -1
    ) {
        auto builder = ArchiveBuilder(
                JStringToCString(env, output_path),
                JStringToCString(env, base_dir),
                JStringListToCVector(env, input_files)
        );
        builder.SetListener([=](const std::string &path, size_t index, size_t total) -> auto {
            auto j_path = CreateJavaString(env, path);
            auto j_index = CreateJavaInteger(env, static_cast<jint>(index));
            auto j_total = CreateJavaInteger(env, static_cast<jint>(total));
            auto params = std::vector<jobject>{j_path.get(), j_index.get(), j_total.get()};
            // 如果检测到取消异常，会抛出 OperationCancelledException
            CallNativeCallback(env, listener, params);
        });
        builder.SetFormat(format);
        builder.SetCompression(compression);
        if (compression_level >= 0) {
            builder.SetCompressionLevel(compression_level);
        }
        try {
            builder.Create();
            return JNI_TRUE;
        } catch (const OperationCancelledException &) {
            // 操作被取消，这是正常情况，不需要记录错误
            s_latest_error_message = "Operation cancelled";
            return JNI_FALSE;
        } catch (const std::exception &exception) {
            s_latest_error_message = exception.what();
            logger::error("CreateArchive failed: %s", exception.what());
            return JNI_FALSE;
        }
    }

    jboolean ExtractArchive(
            JNIEnv *env,
            jstring archive_path,
            jstring output_dir,
            jobject listener,
            bool overwrite = true
    ) {
        try {
            ArchiveExtractor extractor(JStringToCString(env, archive_path));
            extractor.Extract(
                    JStringToCString(env, output_dir),
                    [=](const std::string &path, size_t index, size_t total) {
                        if (!listener) return;
                        auto j_path = CreateJavaString(env, path);
                        auto j_index = CreateJavaInteger(env, static_cast<jint>(index));
                        auto j_total = CreateJavaInteger(env, static_cast<jint>(total));
                        std::vector<jobject> params{j_path.get(), j_index.get(), j_total.get()};
                        // 如果检测到取消异常，会抛出 OperationCancelledException
                        CallNativeCallback(env, listener, params);
                    },
                    overwrite
            );
            return JNI_TRUE;
        } catch (const OperationCancelledException &) {
            // 操作被取消，这是正常情况，不需要记录错误
            s_latest_error_message = "Operation cancelled";
            return JNI_FALSE;
        } catch (const std::exception &exception) {
            s_latest_error_message = exception.what();
            logger::error("ExtractArchive failed: %s", exception.what());
            return JNI_FALSE;
        }
    }

    jobjectArray ListArchiveFiles(
            JNIEnv *env,
            jobject thiz,
            jstring archive_path
    ) {
        auto c_archive_path = JStringToCString(env, archive_path);
        try {
            ArchiveExtractor extractor(JStringToCString(env, archive_path));
            auto list_entity = extractor.ListEntry();

            // 获取 ArchiveEntry 类
            auto entry_class_ptr = FindClass(env,
                                             "cc/kafuu/archandler/libs/archive/model/ArchiveEntry");
            if (!entry_class_ptr) {
                s_latest_error_message = "Failed to find ArchiveEntry class";
                logger::error("ListArchiveFiles failed: %s", s_latest_error_message.c_str());
                return nullptr;
            }

            // 标准化路径（统一使用正斜杠，移除末尾斜杠）
            auto normalize_path = [](const std::string &path) -> std::string {
                if (path.empty()) return path;
                std::string normalized = path;
                // 统一使用正斜杠
                std::replace(normalized.begin(), normalized.end(), '\\', '/');
                // 移除末尾斜杠（除非是根路径"/"）
                if (normalized.length() > 1 && normalized.back() == '/') {
                    normalized.pop_back();
                }
                return normalized;
            };
            
            // 路径 -> 条目信息
            std::map<std::string, ArchiveExtractor::ArchiveEntry> entry_map;
            
            // 添加所有显式存在的条目
            for (const auto &entity : list_entity) {
                std::string normalized_path = normalize_path(entity.pathname);
                entry_map[normalized_path] = entity;
                
                // 对于文件路径，提取所有父目录并添加到map中
                if (entity.mode == AE_IFREG && !normalized_path.empty()) {
                    // 使用字符串操作提取父目录（避免filesystem路径在不同平台上的差异）
                    std::string current_path = normalized_path;
                    while (true) {
                        // 查找最后一个斜杠
                        auto last_slash = current_path.find_last_of('/');
                        // 没有父目录了，或者已经是根目录
                        if (last_slash == std::string::npos || last_slash == 0) break;
                        // 提取父目录路径
                        std::string parent_path = current_path.substr(0, last_slash);
                        // 如果目录还不存在，添加它
                        if (entry_map.find(parent_path) == entry_map.end()) {
                            ArchiveExtractor::ArchiveEntry dir_entry;
                            dir_entry.pathname = parent_path;
                            dir_entry.mode = AE_IFDIR;
                            dir_entry.entry_size = 0;  // 目录大小始终为0
                            dir_entry.modify_time_ms = entity.modify_time_ms;  // 使用文件的修改时间
                            entry_map[parent_path] = dir_entry;
                        }
                        
                        current_path = parent_path;
                    }
                }
            }

            // 创建对象数组
            auto size = static_cast<jsize>(entry_map.size());
            auto array_ptr = WrapLocalRef(
                    env,
                    env->NewObjectArray(size, entry_class_ptr.get(), nullptr)
            );
            if (!array_ptr) {
                s_latest_error_message = "Failed to create ArchiveEntry array";
                logger::error("ListArchiveFiles failed: %s", s_latest_error_message.c_str());
                return nullptr;
            }

            // 填充数组
            jsize i = 0;
            for (const auto &[pathname, entity] : entry_map) {
                // 判断是否是目录
                bool is_directory = (entity.mode == AE_IFDIR);

                // 从路径中提取文件名
                std::string name;
                if (!pathname.empty()) {
                    // 使用字符串操作提取文件名（避免filesystem路径在不同平台上的差异）
                    std::string normalized = normalize_path(pathname);
                    auto last_slash = normalized.find_last_of('/');
                    if (last_slash != std::string::npos) {
                        name = normalized.substr(last_slash + 1);
                    } else {
                        name = normalized;
                    }
                    // 如果名称仍然为空（例如根目录"/"），使用路径本身
                    if (name.empty()) {
                        if (normalized == "/" || normalized.empty()) {
                            name = "/";
                        } else if (is_directory) {
                            name = normalized;
                            if (!name.empty() && name.back() == '/') {
                                name.pop_back();
                            }
                            auto last_slash2 = name.find_last_of('/');
                            if (last_slash2 != std::string::npos && last_slash2 < name.length() - 1) {
                                name = name.substr(last_slash2 + 1);
                            }
                        }
                    }
                }

                // 对于目录，大小应该始终为0
                int64_t entry_size = is_directory ? 0 : entity.entry_size;
                // 确保大小不为负数（libarchive可能返回-1）
                if (entry_size < 0) entry_size = 0;

                // 创建ArchiveEntry对象
                // 注意：compressedSize在libarchive中不可用
                auto entry_obj = CreateArchiveEntry(
                        env, pathname, name, is_directory,
                        entry_size, entry_size, entity.modify_time_ms
                );

                if (!entry_obj) {
                    s_latest_error_message = "Failed to create ArchiveEntry object";
                    logger::error("ListArchiveFiles failed: %s", s_latest_error_message.c_str());
                    return nullptr;
                }

                env->SetObjectArrayElement(array_ptr.get(), i++, entry_obj.get());
            }

            return array_ptr.release();
        } catch (const std::exception &exception) {
            s_latest_error_message = exception.what();
            logger::error("ListArchiveFiles failed: %s", exception.what());
            return nullptr;
        }
    }
}

extern "C"
JNIEXPORT jstring JNICALL
JNI_METHOD(NativeLib, getLatestErrorMessage)(JNIEnv *env, jobject thiz) {
    return CreateJavaString(env, internal::s_latest_error_message).release();
}

extern "C"
JNIEXPORT jboolean JNICALL
JNI_METHOD(NativeLib, createArchive)(
        JNIEnv *env,
        jobject thiz,
        jstring output_path,
        jstring base_dir,
        jobject input_files,
        jint format,
        jint compression,
        jint compression_level,
        jobject listener
) {
    return internal::CreateArchive(
            env, output_path, base_dir, input_files, listener,
            static_cast<ArchiveFormat>(format),
            static_cast<CompressionType>(compression),
            compression_level
    );
}

extern "C"
JNIEXPORT jboolean JNICALL
JNI_METHOD(NativeLib, extractArchive)(
        JNIEnv *env,
        jobject thiz,
        jstring archive_path,
        jstring output_dir,
        jobject listener,
        jboolean overwrite
) {
    return internal::ExtractArchive(env, archive_path, output_dir, listener, overwrite);
}

extern "C"
JNIEXPORT jobjectArray JNICALL
JNI_METHOD(NativeLib, fetchArchiveFiles)(
        JNIEnv *env,
        jobject thiz,
        jstring archive_path
) {
    return internal::ListArchiveFiles(env, thiz, archive_path);
}