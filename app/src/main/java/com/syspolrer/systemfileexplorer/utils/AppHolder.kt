package com.syspolrer.systemfileexplorer.utils

import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.syspolrer.systemfileexplorer.R

class AppHolder(view: View) : RecyclerView.ViewHolder(view) {
    @JvmField
    val apkIcon: AppCompatImageView = view.findViewById(R.id.apk_icon)

    @JvmField
    val txtTitle: AppCompatTextView = view.findViewById(R.id.firstline)

    @JvmField
    val rl: RelativeLayout = view.findViewById(R.id.second)

    @JvmField
    val txtDesc: AppCompatTextView = view.findViewById(R.id.date)

    @JvmField
    val about: AppCompatImageButton = view.findViewById(R.id.properties)

    @JvmField
    val summary: RelativeLayout = view.findViewById(R.id.summary)

    @JvmField
    val packageName: AppCompatTextView = view.findViewById(R.id.appManagerPackageName)

    init {
        apkIcon.visibility = View.VISIBLE
        packageName.visibility = View.VISIBLE

//        val layoutParams = txtDesc.layoutParams as ViewGroup.MarginLayoutParams
//        layoutParams.setMargins(
//            txtDesc.marginLeft,
//            txtDesc.marginTop,
//            Utils.dpToPx(view.context, 4),
//            txtDesc.marginBottom,
//        )
//        txtDesc.layoutParams = layoutParams

    }
}
