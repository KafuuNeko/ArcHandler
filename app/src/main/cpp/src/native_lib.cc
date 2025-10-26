
#include <jni.h>
#include <vector>
#include <android/log.h>

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
            ArchiveFormat format = ArchiveFormat::Tar,
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
        } catch (const std::exception &exception) {
            s_latest_error_message = exception.what();
            __android_log_print(ANDROID_LOG_ERROR, k_log_tag, "CreateArchive failed: %s",
                                exception.what());
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
                        CallNativeCallback(env, listener, params);
                    },
                    overwrite
            );
            return JNI_TRUE;
        } catch (const std::exception &exception) {
            s_latest_error_message = exception.what();
            __android_log_print(ANDROID_LOG_ERROR, k_log_tag, "ExtractArchive failed: %s",
                                exception.what());
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
            auto list_files = std::vector<ArchiveExtractor::ArchiveEntry>();
            std::copy_if(
                    list_entity.cbegin(), list_entity.cend(),
                    std::back_inserter(list_files),
                    [](const ArchiveExtractor::ArchiveEntry &entity) {
                        return entity.mode == AE_IFREG;
                    }
            );
            auto list_pathname = std::vector<std::string>(list_files.size());
            std::transform(
                    list_files.cbegin(), list_files.cend(),
                    list_pathname.begin(),
                    [](const ArchiveExtractor::ArchiveEntry &entity) {
                        return std::string(entity.pathname);
                    }
            );
            return CreateJStringArray(env, list_pathname.cbegin(), list_pathname.cend()).release();
        } catch (const std::exception &exception) {
            s_latest_error_message = exception.what();
            __android_log_print(ANDROID_LOG_ERROR, k_log_tag, "ExtractArchive failed: %s",
                                exception.what());
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