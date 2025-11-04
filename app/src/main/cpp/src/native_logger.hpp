#pragma once

#include <android/log.h>
namespace logger{
    constexpr auto k_log_tag = "native_lib";

    inline void debug(const char *fmt, ...) {
        va_list args;
        va_start(args, fmt);
        __android_log_vprint(ANDROID_LOG_DEBUG, k_log_tag, fmt, args);
        va_end(args);
    }

    inline void error(const char *fmt, ...) {
        va_list args;
        va_start(args, fmt);
        __android_log_vprint(ANDROID_LOG_ERROR, k_log_tag, fmt, args);
        va_end(args);
    }

    inline void info(const char *fmt, ...) {
        va_list args;
        va_start(args, fmt);
        __android_log_vprint(ANDROID_LOG_INFO, k_log_tag, fmt, args);
        va_end(args);
    }
}