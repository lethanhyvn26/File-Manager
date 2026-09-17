package com.syspolrer.systemfileexplorer.utils

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class OpenFileParcelable(
    var uri: Uri?,
    var mimeType: String?,
    var useNewStack: Boolean?,
    var className: String?,
    var packageName: String?,
) : Parcelable
