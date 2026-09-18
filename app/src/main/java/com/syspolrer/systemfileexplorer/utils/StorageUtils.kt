package com.syspolrer.systemfileexplorer.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object StorageUtils {
    private const val PREFS_NAME = "app_data_prefs"
    private const val KEY_APP_DATA_LIST = "app_data_list"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveAppDataList(context: Context, appDataList: List<AppDataParcelable>) {
        val gson = Gson()
        val json = gson.toJson(appDataList)
        getSharedPreferences(context).edit {
            putString(KEY_APP_DATA_LIST, json)
        }
    }

    fun loadAppDataList(context: Context): List<AppDataParcelable> {
        val gson = Gson()
        val json = getSharedPreferences(context).getString(KEY_APP_DATA_LIST, null)
        return if (json != null) {
            val type = object : TypeToken<List<AppDataParcelable>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }
}
