package com.syspolrer.systemfileexplorer.frag

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.syspolrer.systemfileexplorer.R
import com.syspolrer.systemfileexplorer.databinding.FragmentSettingsBinding


class FragmentSettings : Fragment() {

    private lateinit var binding: FragmentSettingsBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentSettingsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    private fun initView() {

        binding.apply {

            tvShare.setOnClickListener { requireActivity().shareApp() }
            tvFeedback.setOnClickListener { requireActivity().feedback() }
            tvRateUs.setOnClickListener { requireActivity().showRateApp() }
            tvVersion.text = getString(R.string.format_version, requireActivity().getVersionApp())

            ivBack.setOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()

            }
            tvPrivacyAdmob.visibility = View.GONE

        }

    }

    fun Activity.getVersionApp(): String {
        try {
            val manager: PackageManager = this
                .packageManager
            var info: PackageInfo? = null
            try {
                info = manager.getPackageInfo(
                    this
                        .packageName,
                    0
                )
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            assert(info != null)
            return info!!.versionName.toString()

        } catch (_: Exception) {
        }
        return ""
    }

    fun Activity.shareApp() {
        val sendIntent = Intent(Intent.ACTION_SEND)
        sendIntent.type = "text/plain"
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            """ Hey, look this app ! 
 https://play.google.com/store/apps/details?id=${this.packageName}"""
        )
        val shareIntent =
            Intent.createChooser(sendIntent, "Share this app with your friends !")
        startActivity(shareIntent)
    }
    fun Activity.showRateApp() {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)
            )
        )
    }

    fun Activity.feedback() {


        val deviceInfo = buildString {
            append("\n")
            append("\n")
            append("\n")
            append("\n")
            append("\n")
            append("\n")
            append("\n")
            append("\n")
            append("\n")
            append("\n")
            append("\n")
            append("\n")
            append("\n")
            append("\nSystem Info:")
            append("\nAndroid Device: " + Build.MODEL + " (" + Build.MANUFACTURER + ")")
            append("\nAndroid version: " + Build.VERSION.RELEASE)
            append("\n Device: " + Build.DEVICE)
            append("\n OS API Level: " + Build.VERSION.SDK_INT)
            append("\n App version: " + getVersionApp())

        }

        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:") // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email_support)))
        intent.putExtra(
            Intent.EXTRA_SUBJECT,
            getString(R.string.email_subject_format, getString(R.string.app_name))
        )
        intent.putExtra(Intent.EXTRA_TEXT, deviceInfo)
        startActivity(Intent.createChooser(intent, "Send email..."))

    }
}