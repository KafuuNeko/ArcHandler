package cc.kafuu.archandler.libs.model

enum class DefaultAppType(val mimeTypes: List<String>) {
    IMAGES(
        listOf(
            "image/*",
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "image/bmp", "image/tiff", "image/svg+xml", "image/heic", "image/heif"
        )
    ),
    VIDEOS(
        listOf(
            "video/*",
            "video/mp4", "video/3gp", "video/avi", "video/mkv",
            "video/webm", "video/flv", "video/mov", "video/wmv", "video/mpeg"
        )
    ),
    DOCUMENTS(
        listOf(
            // PDF
            "application/pdf",
            // Word
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            // Excel
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            // PowerPoint
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            // Text
            "text/plain", "text/csv", "text/rtf", "text/html", "application/json",
            // Markdown
            "text/markdown", "application/x-markdown"
        )
    )
}