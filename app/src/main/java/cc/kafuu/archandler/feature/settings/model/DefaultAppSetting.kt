package cc.kafuu.archandler.feature.settings.model

import cc.kafuu.archandler.R
import cc.kafuu.archandler.libs.model.DefaultAppType

/**
 * 默认应用设置
 */
enum class DefaultAppSetting(
    val type: DefaultAppType,
    val titleResId: Int
) {
    DEFAULT_APP_IMAGES(DefaultAppType.IMAGES, R.string.default_app_images),
    DEFAULT_APP_VIDEOS(DefaultAppType.VIDEOS, R.string.default_app_videos),
    DEFAULT_APP_DOCUMENTS(DefaultAppType.DOCUMENTS, R.string.default_app_documents);
}