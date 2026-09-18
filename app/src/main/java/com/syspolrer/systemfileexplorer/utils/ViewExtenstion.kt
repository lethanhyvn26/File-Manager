package com.syspolrer.systemfileexplorer.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.net.InetAddress
import java.net.UnknownHostException


var countClick: Int = 2




fun checkShowAdsFull(): Boolean {
    return if (countClick >= 2) {
        true
    } else {
        countClick++
        false

    }
}

fun isInternetAvailable(): Boolean {
    try {
        val address = InetAddress.getByName("www.google.com")
        return !address.equals("")
    } catch (e: UnknownHostException) {
        e.printStackTrace()
    }
    return false
}



fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val nw = connectivityManager.activeNetwork ?: return false
    val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
    return when {
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        //for other device how are able to connect with Ethernet
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        //for check internet over Bluetooth
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
        else -> false
    }
}




