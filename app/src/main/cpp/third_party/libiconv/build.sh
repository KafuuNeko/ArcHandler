#!/bin/bash
set -e

# ========================================
# Android NDK
# ========================================
NDK="/home/kafuuneko/Android/Ndk/android-ndk-r28c"
API=24

# ========================================
# 构建和安装目录
# ========================================
BUILD_DIR="$(pwd)/build-android"
INSTALL_DIR="$(pwd)/install-android"

# ABI 列表
ABIS=("armeabi-v7a" "arm64-v8a" "x86" "x86_64")

# 清理旧目录
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"
mkdir -p "$INSTALL_DIR"

# ========================================
# Building
# ========================================
for ABI in "${ABIS[@]}"; do
    echo "=== 编译 $ABI ==="

    case "$ABI" in
        armeabi-v7a)
            TARGET_HOST="armv7a-linux-androideabi"
            TOOLCHAIN="$NDK/toolchains/llvm/prebuilt/linux-x86_64/bin"
            CC="$TOOLCHAIN/armv7a-linux-androideabi$API-clang"
            ;;
        arm64-v8a)
            TARGET_HOST="aarch64-linux-android"
            TOOLCHAIN="$NDK/toolchains/llvm/prebuilt/linux-x86_64/bin"
            CC="$TOOLCHAIN/aarch64-linux-android$API-clang"
            ;;
        x86)
            TARGET_HOST="i686-linux-android"
            TOOLCHAIN="$NDK/toolchains/llvm/prebuilt/linux-x86_64/bin"
            CC="$TOOLCHAIN/i686-linux-android$API-clang"
            ;;
        x86_64)
            TARGET_HOST="x86_64-linux-android"
            TOOLCHAIN="$NDK/toolchains/llvm/prebuilt/linux-x86_64/bin"
            CC="$TOOLCHAIN/x86_64-linux-android$API-clang"
            ;;
        *)
            echo "未知 ABI: $ABI"
            exit 1
            ;;
    esac

    # 构建和安装目录
    ABI_BUILD_DIR="$BUILD_DIR/$ABI"
    ABI_INSTALL_DIR="$INSTALL_DIR/$ABI"
    mkdir -p "$ABI_BUILD_DIR"
    mkdir -p "$ABI_INSTALL_DIR"

    pushd "$ABI_BUILD_DIR"

    # 使用libiconv根目录下的 configure，路径为绝对路径
    CONFIGURE_SCRIPT="$(pwd)/../../configure"

    "$CONFIGURE_SCRIPT" \
        --host="$TARGET_HOST" \
        --prefix="$ABI_INSTALL_DIR" \
        --disable-shared \
        --enable-static \
        --enable-extra-encodings \
        CC="$CC" \
        CFLAGS="-fPIC"

    make -j$(nproc)
    make install

    popd
done

echo "=== 完成, 静态库已安装到 $INSTALL_DIR ==="

