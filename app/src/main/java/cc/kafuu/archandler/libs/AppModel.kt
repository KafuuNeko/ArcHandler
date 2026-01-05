package cc.kafuu.archandler.libs

import com.chibatching.kotpref.KotprefModel

object AppModel : KotprefModel() {
    const val EMAIL = "kafuuneko@gmail.com"

    const val CODE_REPOSITORY_URL = "https://github.com/KafuuNeko/ArcHandler"

    const val FEEDBACK_URL = "https://github.com/KafuuNeko/ArcHandler/issues"

    const val GOOGLE_PLAY_URL = "https://play.google.com"

    const val KEY_DATA = "data"

    const val KEY_USER_REDIRECT_PATH = "user_redirect_path"

    var isShowHiddenFiles by booleanPref(default = false)

    var isShowUnreadableDirectories by booleanPref(default = false)

    var isShowUnreadableFiles by booleanPref(default = true)

    var listSortType by intPref(default = 0)

}