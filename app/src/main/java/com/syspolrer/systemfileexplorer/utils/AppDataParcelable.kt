package com.syspolrer.systemfileexplorer.utils

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
class AppDataParcelable(
    var label: String,
    var path: String,
//    var icon:Drawable,
//    var  splitPathList: List<String>?,
    var packageName: String,
//    var data: String,
    var fileSize: String,
    var size: Long,
    var lastModification: Long,
    var isSystemApp: Boolean,
    var openFileParcelable: OpenFileParcelable?,
) : Parcelable
