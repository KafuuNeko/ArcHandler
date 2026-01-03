# ArcHandler

一个 Android 归档文件处理应用，支持多种压缩格式的创建、解压和管理。

## 📋 功能特性

### 核心功能

- **压缩文件创建**
  - 支持多种归档格式：ZIP、7Z、TAR、CPIO
  - 支持多种压缩算法：Gzip、Bzip2、XZ、LZ4、Zstd、LZMA、Deflate
  - 可自定义压缩级别（1-9，Zstd 支持 1-19）
  - 支持密码保护的压缩包（ZIP、7Z）
  - 支持分卷压缩（Volume Split）

- **压缩文件解压**
  - 支持广泛的归档格式：
    - **常见格式**：ZIP、7Z、RAR、RAR5、TAR、CPIO
    - **压缩格式**：GZ、BZ2、XZ、LZ4、ZST、LZMA
    - **组合格式**：TAR.GZ、TAR.BZ2、TAR.XZ、TAR.LZ4、TAR.ZST
    - **其他格式**：ISO、HFS、ARJ、CAB、LZH、CHM、NSIS、AR、RPM、UDF、WIM、XAR、FAT、NTFS
  - 支持分卷压缩包（.7z.001、.zip.001 等）
  - 支持密码保护的压缩包

- **文件管理**
  - 文件浏览和导航
  - 文件复制、移动、删除
  - 多文件选择
  - 存储卷管理（内部存储、外部存储）

## 📦 支持的格式

### 归档格式
- ZIP（支持密码保护）
- 7Z（支持密码保护）
- TAR
- CPIO
- RAR / RAR5
- ISO
- 以及其他多种格式

### 压缩算法
- **无压缩**：None、Store
- **通用压缩**：Deflate、LZMA
- **Unix 压缩**：Gzip、Bzip2、XZ
- **现代压缩**：LZ4、Zstd

### 组合格式
- TAR.GZ、TAR.BZ2、TAR.XZ、TAR.LZ4、TAR.ZST
- CPIO.GZ、CPIO.BZ2、CPIO.XZ、CPIO.LZ4、CPIO.ZST

## 🔧 构建要求

- **Android Studio** - 最新版本
- **JDK 11** 或更高版本
- **Android SDK**
  - `compileSdk`: 35
  - `minSdk`: 24
  - `targetSdk`: 35
- **NDK** - 版本 28.1.13356709
- **CMake** - 版本 3.22.1 或更高
- **Gradle** - 通过 Gradle Wrapper 管理

## 🚀 构建步骤

1. 克隆仓库
```bash
git clone https://github.com/yourusername/ArcHandler.git
cd ArcHandler
```

2. 使用 Android Studio 打开项目

3. 同步 Gradle 依赖

4. 构建项目
```bash
./gradlew assembleDebug
```

5. 安装到设备
```bash
./gradlew installDebug
```

## 📱 使用说明

### 创建压缩包

1. 在主界面选择要压缩的文件或文件夹
2. 点击"归档"按钮
3. 选择归档格式（ZIP、7Z、TAR、CPIO）
4. 选择压缩类型和级别
5. （可选）设置密码保护
6. （可选）设置分卷大小
7. 选择保存位置
8. 开始创建压缩包

### 解压压缩包

1. 在主界面找到压缩包文件
2. 点击压缩包文件
3. 选择解压目标位置
4. 如果压缩包有密码，输入密码
5. 处理文件冲突（如需要）
6. 开始解压

### 文件管理

- **复制/移动**：选择文件后，使用底部菜单进行操作
- **删除**：选择文件后，点击删除按钮
- **多选**：长按文件进入多选模式

## 🔐 权限说明

应用需要以下权限：

- `MANAGE_EXTERNAL_STORAGE` - 管理外部存储（Android 11+）
- `READ_EXTERNAL_STORAGE` - 读取外部存储
- `WRITE_EXTERNAL_STORAGE` - 写入外部存储

这些权限用于访问和管理设备上的文件。

## 📄 许可证

本项目采用开源许可证（具体许可证请查看 LICENSE 文件）。

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## ⚠️ 注意事项

- 这是一个 FOSS（自由开源软件）版本
- 当前版本为 Alpha 阶段，可能存在一些不稳定因素
- 建议在生产环境使用前进行充分测试
