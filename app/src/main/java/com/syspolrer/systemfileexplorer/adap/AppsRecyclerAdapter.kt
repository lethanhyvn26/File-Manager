package com.syspolrer.systemfileexplorer.adap

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.IntDef
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.syspolrer.systemfileexplorer.R
import com.syspolrer.systemfileexplorer.utils.AppDataParcelable
import com.syspolrer.systemfileexplorer.utils.AppHolder
import com.syspolrer.systemfileexplorer.utils.EmptyViewHolder
import com.syspolrer.systemfileexplorer.utils.FileUtils
import com.syspolrer.systemfileexplorer.utils.SpecialViewHolder
import java.io.File
import java.util.Locale
import kotlin.math.roundToInt

class AppsRecyclerAdapter(
    private val fragment: Fragment,
    private val modelProvider: AppsAdapterPreloadModel,
    private val isBottomSheet: Boolean,
    private val appDataParcelableList: MutableList<AppDataParcelable>,
    private val callback: UninstallCallback
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    private var appDataListItem: MutableList<ListItem> = mutableListOf()
        set(value) {
            value.clear()
            val headerFlags = BooleanArray(2)
            appDataParcelableList.forEach {

                if (!isBottomSheet && it.isSystemApp && !headerFlags[0]) {
//                    value.add(ListItem(TYPE_HEADER_SYSTEM))
//                    modelProvider.addItem("")
//                    headerFlags[0] = true
                } else if (!isBottomSheet && !it.isSystemApp && !headerFlags[1]) {
//                    value.add(ListItem(TYPE_HEADER_THIRD_PARTY))
//                    modelProvider.addItem("")
//                    headerFlags[1] = true
                }
                modelProvider.addItem(it.path)
                value.add(ListItem(it))
            }
            if (!isBottomSheet) {
                modelProvider.addItem("")
                value.add(ListItem(EMPTY_LAST_ITEM))
            }
            field = value
        }
    private var appDataParcelableListFull: MutableList<AppDataParcelable> =
        ArrayList(appDataParcelableList)

    init {
        appDataListItem = mutableListOf()
    }

    private val mInflater: LayoutInflater
        get() =
            fragment.requireActivity()
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    companion object {
        const val TYPE_ITEM = 0
        const val TYPE_HEADER_SYSTEM = 1
        const val TYPE_HEADER_THIRD_PARTY = 2
        const val EMPTY_LAST_ITEM = 3
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        var view = View(fragment.requireContext())
        when (viewType) {
            TYPE_ITEM -> {
                view = mInflater.inflate(R.layout.rowlayout, parent, false)
                return AppHolder(view)
            }

            TYPE_HEADER_SYSTEM, TYPE_HEADER_THIRD_PARTY -> {
                view = mInflater.inflate(R.layout.list_header, parent, false)
                return SpecialViewHolder(
                    view,
                    if (viewType == TYPE_HEADER_SYSTEM) {
                        SpecialViewHolder.HEADER_SYSTEM_APP
                    } else {
                        SpecialViewHolder.HEADER_USER_APP
                    },
                )
            }

            EMPTY_LAST_ITEM -> {
                view.minimumHeight =
                    (
                            fragment.requireActivity().resources.getDimension(R.dimen.fab_height) +
                                    fragment.requireContext().resources
                                        .getDimension(R.dimen.fab_margin)
                            ).roundToInt()
                return EmptyViewHolder(view)
            }

            else -> {
                throw IllegalStateException("Illegal $viewType in apps adapter")
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return appDataListItem[position].listItemType
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        if (holder is AppHolder) {
            appDataListItem[position].appDataParcelable?.let { rowItem ->
//                modelProvider.loadApkImage(rowItem.path, holder.apkIcon)

//                Glide.with(fragment)
//                    .load(rowItem.icon)
//                    .fitCenter()
//                    .error(R.drawable.ic_android)
//                    .into(holder.apkIcon)
                modelProvider.loadApkImage(rowItem.packageName, holder.apkIcon)

                showPopup(holder.about, rowItem)
//                holder.rl.setOnFocusChangeListener { _, _ ->
//                    adjustListViewCallback.adjustListViewForTv(
//                        holder,
//                        fragment.requireActivity() as MainActivity,
//                    )
//                }
                holder.txtTitle.text = rowItem.label
                holder.packageName.text = rowItem.packageName
                holder.packageName.isSelected = true // for marquee
                holder.txtDesc.text =
                    fragment.getString(R.string.format_file_size_pipe, rowItem.fileSize)

                holder.rl.isClickable = true
                holder.rl.nextFocusRightId = holder.about.id
                holder.rl.setOnClickListener {
                    startActivityForRowItem(rowItem)
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return appDataListItem.size
    }

    /**
     * Set list elements
     * @param showSystemApps whether to filter system apps or not
     */
    fun setData(
        data: List<AppDataParcelable>,
        showSystemApps: Boolean,
    ) {
        appDataParcelableList.run {
            clear()
            val list = if (!showSystemApps) data.filter { !it.isSystemApp } else data
            addAll(list)
            appDataListItem = mutableListOf()
            notifyDataSetChanged()
        }
    }
//    fun removeUninstalledApp(packageName: String) {
//        Log.i("HAOHAO", "removeUninstalledApp $packageName")
//        appDataParcelableListFull.removeIf { it.packageName == packageName }
//        appDataParcelableList.removeIf { it.packageName == packageName }
//        appDataListItem = mutableListOf()
//        notifyDataSetChanged()
//    }

    private fun startActivityForRowItem(rowItem: AppDataParcelable?) {
        rowItem?.run {
            if (isBottomSheet) {
                val openFileParcelable = rowItem.openFileParcelable
                openFileParcelable?.let {
                    safeLet(
                        openFileParcelable.uri,
                        openFileParcelable.mimeType,
                        openFileParcelable.useNewStack,
                    ) { uri, mimeType, useNewStack ->
                        val intent =
                            buildIntent(
                                fragment.requireContext(),
                                uri,
                                mimeType,
                                useNewStack,
                                openFileParcelable.className,
                                openFileParcelable.packageName,
                            )
//                        setLastOpenedApp(
//                            rowItem,
//                            fragment.requireActivity() as PreferenceActivity,
//                        )
                        fragment.requireContext().startActivityCatchingSecurityException(intent)
                    }
                }
            } else {
                val i1 =
                    fragment.requireContext().packageManager.getLaunchIntentForPackage(
                        rowItem.packageName,
                    )
                if (i1 != null) {
                    fragment.startActivity(i1)
                } else {
                    Toast.makeText(
                        fragment.context,
                        fragment.getString(R.string.not_allowed),
                        Toast.LENGTH_LONG,
                    )
                        .show()
                    // TODO: Implement this method
                }
            }
        }
    }

    private fun showPopup(
        v: View,
        rowItem: AppDataParcelable?,
    ) {
        v.setOnClickListener { view: View? ->
//            var context = fragment.context
//            if ((
//                    fragment.requireActivity()
//                        as MainActivity
//                ).appTheme == AppTheme.BLACK
//            ) {
//                context = ContextThemeWrapper(context, R.style.overflow_black)
//            }
            val popupMenu = PopupMenu(view!!.context, view)
            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                val colorAccent = R.color.primary
                when (item.itemId) {
                    R.id.open -> {
                        rowItem?.let {
                            popupOpen(it)
                        }
                        return@setOnMenuItemClickListener true
                    }

                    R.id.share -> {
                        rowItem?.let {
                            popupShare(it, colorAccent)
                        }
                        return@setOnMenuItemClickListener true
                    }

                    R.id.unins -> {
                        rowItem?.let {
                            callback.onUninstall(it.packageName)
                        }
                        return@setOnMenuItemClickListener true
                    }

                    R.id.play -> {
                        rowItem?.let {
                            popupPlay(it)
                        }
                        return@setOnMenuItemClickListener true
                    }

                    R.id.properties -> {
                        fragment.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                "package:${rowItem!!.packageName}".toUri(),
                            ),
                        )
                        return@setOnMenuItemClickListener true
                    }

                }

                false
            }
            popupMenu.inflate(R.menu.app_options)
            popupMenu.show()
        }
    }

    private fun popupOpen(appDataParcelable: AppDataParcelable) {
        val i1 =
            fragment
                .context
                ?.packageManager
                ?.getLaunchIntentForPackage(appDataParcelable.packageName)
        if (i1 != null) {
            fragment.startActivity(i1)
        } else {
            Toast.makeText(
                fragment.context,
                fragment.getString(R.string.not_allowed),
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    private fun popupShare(
        appDataParcelable: AppDataParcelable,
        colorAccent: Int,
    ) {
        val arrayList2 =
            ArrayList<File>()
        arrayList2.add(File(appDataParcelable.path))
        FileUtils.shareFiles(
            arrayList2,
            fragment.activity,
            colorAccent,
        )
    }


    private fun popupPlay(appDataParcelable: AppDataParcelable) {
        val intent1 =
            Intent(Intent.ACTION_VIEW)
        try {
            intent1.data = "market://details?id=${appDataParcelable.packageName}".toUri()
            fragment.startActivity(intent1)
        } catch (_: ActivityNotFoundException) {
            intent1.data =
                "https://play.google.com/store/apps/details?id=${appDataParcelable.packageName}".toUri()
            fragment.startActivity(intent1)
        }
    }

//
//    private fun showDeleteSystemAppDialog(
//        themedActivity: ThemedActivity,
//        colorAccent: Int,
//        f1: HybridFileParcelable,
//    ) {
//        val builder1 =
//            MaterialDialog.Builder(fragment.requireContext())
//        builder1
//            .theme(
//                themedActivity.appTheme.getMaterialDialogTheme(),
//            )
//            .content(fragment.getString(R.string.unin_system_apk))
//            .title(fragment.getString(R.string.warning))
//            .negativeColor(colorAccent)
//            .positiveColor(colorAccent)
//            .negativeText(fragment.getString(R.string.no))
//            .positiveText(fragment.getString(R.string.yes))
//            .onNegative { dialog: MaterialDialog, _: DialogAction? -> dialog.cancel() }
//            .onPositive { _: MaterialDialog?, _: DialogAction? ->
//                val files =
//                    ArrayList<HybridFileParcelable>()
//                val parent =
//                    f1.getParent(fragment.context)
//                if (parent != "app" && parent != "priv-app") {
//                    val baseFile =
//                        HybridFileParcelable(
//                            f1.getParent(fragment.context),
//                        )
//                    baseFile.mode =
//                        OpenMode.ROOT
//                    files.add(baseFile)
//                } else {
//                    files.add(f1)
//                }
//                DeleteTask(fragment.requireContext(), false).execute(files)
//            }
//            .build()
//            .show()
//    }

    @Target(AnnotationTarget.TYPE)
    @IntDef(
        TYPE_ITEM,
        TYPE_HEADER_SYSTEM,
        TYPE_HEADER_THIRD_PARTY,
        EMPTY_LAST_ITEM,
    )
    annotation class ListItemType

    data class ListItem(
        var appDataParcelable: AppDataParcelable?,
        var listItemType: @ListItemType Int = TYPE_ITEM,
    ) {
        constructor(listItemType: @ListItemType Int) : this(null, listItemType)
    }

    inline fun <T1 : Any, T2 : Any, T3 : Any, R : Any> safeLet(
        p1: T1?,
        p2: T2?,
        p3: T3?,
        block: (T1, T2, T3) -> R?,
    ): R? {
        return if (p1 != null && p2 != null && p3 != null) {
            block(
                p1,
                p2,
                p3,
            )
        } else {
            null
        }
    }


    /**
     * Builds an intent which necessary permission flags for external apps to open uri file
     */
    private fun buildIntent(
        context: Context,
        uri: Uri,
        mimeType: String,
        useNewStack: Boolean,
        className: String?,
        packageName: String?,
    ): Intent {
        val chooserIntent = Intent()
        chooserIntent.action = Intent.ACTION_VIEW
        chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        chooserIntent.setDataAndType(uri, mimeType)

        for (
        resolveInfo in context.packageManager
            .queryIntentActivities(
                chooserIntent,
                PackageManager.MATCH_DEFAULT_ONLY,
            )
        ) context.grantUriPermission(
            resolveInfo.activityInfo.packageName,
            uri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION,
        )

        if (useNewStack) {
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
        }
        className?.run {
            packageName?.run {
                chooserIntent.setClassName(packageName, className)
            }
        }
        return chooserIntent
    }

    /**
     * Makes the [Activity] starting not crash in case the app is
     * not meant to deal with this kind of intent
     */
    fun Context.startActivityCatchingSecurityException(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: SecurityException) {
            Log.e("Error when starting activity: ", e.message.toString())
            Toast.makeText(this, R.string.security_error, Toast.LENGTH_SHORT).show()
        }
    }


    override fun getFilter(): Filter {
        return appFilter
    }

    private val appFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList = mutableListOf<AppDataParcelable>()

            if (constraint.isNullOrEmpty()) {
                filteredList.addAll(appDataParcelableListFull)
            } else {
                val filterPattern = constraint.toString().lowercase(Locale.getDefault()).trim()
                for (item in appDataParcelableListFull) {
                    if (item.label.lowercase(Locale.getDefault()).contains(filterPattern)) {
                        filteredList.add(item)
                    }
                }
            }

            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            appDataParcelableList.clear()
            val values = results?.values
            if (values is List<*>) {
                appDataParcelableList.addAll(values.filterIsInstance<AppDataParcelable>())
            }
            appDataListItem = mutableListOf() // Recreate the app data list with headers and items
            notifyDataSetChanged()
        }

    }

    interface UninstallCallback {
        fun onUninstall(packageName: String)
    }
}

