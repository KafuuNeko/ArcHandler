# ArcHandler

[中文](README_zh.md) | English

An Android archive file processing application that supports creating, extracting, and managing various compression formats.

## 📋 Features

### Core Functions

- **Archive Creation**
  - Supports multiple archive formats: ZIP, 7Z, TAR, CPIO
  - Supports multiple compression algorithms: Gzip, Bzip2, XZ, LZ4, Zstd, LZMA, Deflate
  - Customizable compression levels (1-9, Zstd supports 1-19)
  - Password-protected archives (ZIP, 7Z)
  - Volume split support

- **Archive Extraction**
  - Supports a wide range of archive formats:
    - **Common formats**: ZIP, 7Z, RAR, RAR5, TAR, CPIO
    - **Compressed formats**: GZ, BZ2, XZ, LZ4, ZST, LZMA
    - **Combined formats**: TAR.GZ, TAR.BZ2, TAR.XZ, TAR.LZ4, TAR.ZST
    - **Other formats**: ISO, HFS, ARJ, CAB, LZH, CHM, NSIS, AR, RPM, UDF, WIM, XAR, FAT, NTFS
  - Supports split archive files (.7z.001, .zip.001, etc.)
  - Supports password-protected archives

- **File Management**
  - File browsing and navigation
  - File copy, move, delete
  - Multi-file selection
  - Storage volume management (internal storage, external storage)

## 📦 Supported Formats

### Archive Formats
- ZIP (password protected)
- 7Z (password protected)
- TAR
- CPIO
- RAR / RAR5
- ISO
- And many other formats

### Compression Algorithms
- **No compression**: None, Store
- **General compression**: Deflate, LZMA
- **Unix compression**: Gzip, Bzip2, XZ
- **Modern compression**: LZ4, Zstd

### Combined Formats
- TAR.GZ, TAR.BZ2, TAR.XZ, TAR.LZ4, TAR.ZST
- CPIO.GZ, CPIO.BZ2, CPIO.XZ, CPIO.LZ4, CPIO.ZST

## 🔧 Build Requirements

- **Android Studio** - Latest version
- **JDK 11** or higher
- **Android SDK**
  - `compileSdk`: 35
  - `minSdk`: 24
  - `targetSdk`: 35
- **NDK** - Version 28.1.13356709
- **CMake** - Version 3.22.1 or higher
- **Gradle** - Managed via Gradle Wrapper

## 📱 Usage Guide

### Creating an Archive

1. Select files or folders to compress on the main screen
2. Tap the "Archive" button
3. Choose archive format (ZIP, 7Z, TAR, CPIO)
4. Select compression type and level
5. (Optional) Set password protection
6. (Optional) Set volume split size
7. Choose save location
8. Start creating the archive

### Extracting an Archive

1. Find the archive file on the main screen
2. Tap the archive file
3. Select extraction destination
4. Enter password if the archive is protected
5. Handle file conflicts (if any)
6. Start extraction

### File Management

- **Copy/Move**: After selecting files, use the bottom menu to perform operations
- **Delete**: After selecting files, tap the delete button
- **Multi-select**: Long press a file to enter multi-select mode

## 🔐 Permissions

The app requires the following permissions:

- `MANAGE_EXTERNAL_STORAGE` - Manage external storage (Android 11+)
- `READ_EXTERNAL_STORAGE` - Read external storage
- `WRITE_EXTERNAL_STORAGE` - Write external storage

These permissions are used to access and manage files on your device.

## 📄 License

This project uses an open source license (see LICENSE file for details).

## 🤝 Contributing

Feel free to submit Issues and Pull Requests!

## ⚠️ Notes

- This is a FOSS (Free and Open Source Software) version
- Current version is in Alpha stage, there may be some instabilities
- It is recommended to perform thorough testing before using in production environments
