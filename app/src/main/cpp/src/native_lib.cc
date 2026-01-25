
#include <jni.h>
#include <vector>
#include <string>
#include <map>
#include <algorithm>
#include <filesystem>
#include <archive_entry.h>

#include "native_logger.hpp"
#include "utils/jni_utils.hpp"
#include "utils/archive_utils.hpp"
#include "src/archive_builder.hpp"
#include "src/archive_extractor.hpp"

#define JNI_METHOD(cls, name) Java_cc_kafuu_archandler_libs_jni_##cls##_##name

namespace internal {
    thread_local auto s_latest_error_message = std::string("none");

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

    jobject TestArchive(
            JNIEnv *env,
            jobject thiz,
            jstring archive_path,
            jobject listener
    ) {
        auto c_archive_path = JStringToCString(env, archive_path);
        try {
            ArchiveExtractor extractor(c_archive_path);

            auto result = extractor.Test([=](const std::string &path, size_t index, size_t total) {
                if (!listener) return;
                auto j_path = CreateJavaString(env, path);
                auto j_index = CreateJavaInteger(env, static_cast<jint>(index));
                auto j_total = CreateJavaInteger(env, static_cast<jint>(total));
                std::vector<jobject> params{j_path.get(), j_index.get(), j_total.get()};
                // 如果检测到取消异常，会抛出 OperationCancelledException
                CallNativeCallback(env, listener, params);
            });

            return CreateArchiveTestResult(
                    env,
                    result.success,
                    result.error_message,
                    static_cast<jint>(result.tested_files),
                    static_cast<jint>(result.total_files)
            ).release();
        } catch (const OperationCancelledException &) {
            // 操作被取消，这是正常情况，不需要记录错误
            s_latest_error_message = "Operation cancelled";
            return CreateArchiveTestResult(
                    env, false, "Operation cancelled", 0, 0
            ).release();
        } catch (const std::exception &exception) {
            s_latest_error_message = exception.what();
            logger::error("TestArchive exception: %s", exception.what());
            return CreateArchiveTestResult(
                    env, false, exception.what(), 0, 0
            ).release();
        }
    }

    jobjectArray ListArchiveFiles(JNIEnv *env, jobject thiz, jstring archive_path) {
        auto c_archive_path = JStringToCString(env, archive_path);
        try {
            ArchiveExtractor extractor(c_archive_path);
            auto list_entity = extractor.ListEntry();
            auto entry_map = BuildCompleteEntryMap(list_entity);
            auto mapper = [&](const auto &pair) {
                const auto &[pathname, entity] = pair;
                bool is_directory = (entity.mode == AE_IFDIR);
                std::string name = ExtractNameFromPath(pathname, is_directory);
                int64_t entry_size = is_directory ? 0 : std::max<int64_t>(0, entity.entry_size);
                return CreateArchiveEntry(
                        env, pathname, name, is_directory, entry_size, entry_size,
                        entity.modify_time_ms
                );
            };
            auto result_array = CreateJObjectArray(
                    env, "cc/kafuu/archandler/libs/archive/model/ArchiveEntry",
                    entry_map.cbegin(), entry_map.cend(), mapper
            );
            if (!result_array) {
                if (s_latest_error_message.empty()) {
                    s_latest_error_message = "Failed to create array or convert entries";
                }
                logger::error("ListArchiveFiles error: %s", s_latest_error_message.c_str());
                return nullptr;
            }
            return result_array.release();
        } catch (const std::exception &exception) {
            s_latest_error_message = exception.what();
            logger::error("ListArchiveFiles exception: %s", exception.what());
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

extern "C"
JNIEXPORT jobject JNICALL
JNI_METHOD(NativeLib, testArchive)(
        JNIEnv *env,
        jobject thiz,
        jstring archive_path,
        jobject listener
) {
    return internal::TestArchive(env, thiz, archive_path, listener);
}