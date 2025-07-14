package cc.kafuu.archandler.libs.model

import androidx.annotation.DrawableRes
import cc.kafuu.archandler.R

enum class FileType(
    @DrawableRes val icon: Int
) {
    Folder(R.drawable.ic_folder),

    Archive(R.drawable.ic_archive),

    Image(R.drawable.ic_file_image),

    Pdf(R.drawable.ic_file_pdf),

    Docs(R.drawable.ic_file_docs),

    Movie(R.drawable.ic_file_movie),

    Music(R.drawable.ic_file_music),

    Database(R.drawable.ic_file_database),

    Unknow(R.drawable.ic_file)
}