package com.syspolrer.systemfileexplorer

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.syspolrer.systemfileexplorer.activity.SplashActivity
import com.syspolrer.systemfileexplorer.ads.AdmobHelp
import com.syspolrer.systemfileexplorer.utils.isInternetAvailable
import java.util.Date

class MyApplication : Application(), Application.ActivityLifecycleCallbacks, LifecycleObserver {


    private lateinit var appOpenAdManager: AppOpenAdManager
    private var currentActivity: Activity? = null

    companion object {
        fun getInstance(): MyApplication {
            return instance!!
        }

        private var instance: MyApplication? = null

    }

    fun getOpenAdsManager(): AppOpenAdManager {
        return appOpenAdManager
    }

    @Synchronized
    open fun setInstance(mInstance: MyApplication) {
        instance = mInstance
    }


    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        if (instance == null) {
            setInstance(this)
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        appOpenAdManager = AppOpenAdManager()
        AdmobHelp.getInstance()?.init(this) //init ads
        appOpenAdManager.loadAd(this)

    }

    /** LifecycleObserver method that shows the app open ad when the app moves to foreground. */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        // Show the ad (if available) when the app moves to foreground.
        currentActivity?.let {
            appOpenAdManager.showAdIfAvailable(it)
        }
    }


    /** ActivityLifecycleCallback methods. */
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        // An ad activity is started when an ad is showing, which could be AdActivity class from Google
        // SDK or another activity class implemented by a third party mediation partner. Updating the
        // currentActivity only when an ad is not showing will ensure it is not an ad activity, but the
        // one that shows the ad.
        if (!appOpenAdManager.isShowingAd) {
            currentActivity = activity
        }
    }

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}

    /**
     * Shows an app open ad.
     *
     * @param activity the activity that shows the app open ad
     * @param onShowAdCompleteListener the listener to be notified when an app open ad is complete
     */
    fun showAdIfAvailable(
        activity: Activity,
        onShowAdCompleteListener: OnShowAdCompleteListener
    ) {
        // We wrap the showAdIfAvailable to enforce that other classes only interact with MyApplication
        // class.
        appOpenAdManager.showAdIfAvailable(activity, onShowAdCompleteListener)
    }
    fun showAdIfAvailableSplash(
        activity: Activity,
        onShowAdCompleteListener: OnShowAdCompleteListener
    ) {
        // We wrap the showAdIfAvailable to enforce that other classes only interact with MyApplication
        // class.
        appOpenAdManager.showAdIfAvailableSplash(activity, onShowAdCompleteListener)
    }



    /**
     * Interface definition for a callback to be invoked when an app open ad is complete
     * (i.e. dismissed or fails to show).
     */
    interface OnShowAdCompleteListener {
        fun onShowAdComplete()

    }

    /** Inner class that loads and shows app open ads. */
    inner class AppOpenAdManager {

        private var appOpenAd: AppOpenAd? = null
        var isLoadingAd = false
        var isShowingAd = false

        /** Keep track of the time an app open ad is loaded to ensure you don't show an expired ad. */
        private var loadTime: Long = 0

        /**
         * Load an ad.
         *
         * @param context the context of the activity that loads the ad
         */
        fun loadAd(context: Context) {
            // Do not load ad if there is an unused ad or one is already loading.
            if (isLoadingAd || isAdAvailable()) {
                return
            }

            Log.i("HAOHAO", "Loading new interstitial ad.")
            isLoadingAd = true



            if(!isInternetAvailable(context)){
                isLoadingAd = false
                return
            }
            val request = AdRequest.Builder().build()
            // GMA 24+ removed orientation overloads; orientation is taken from the request.
            // https://developers.google.com/admob/android/migration
            AppOpenAd.load(
                context,
                getString(R.string.str_ads_open),
                request,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    /**
                     * Called when an app open ad has loaded.
                     *
                     * @param ad the loaded app open ad.
                     */
                    override fun onAdLoaded(ad: AppOpenAd) {
                        appOpenAd = ad
                        isLoadingAd = false
                        loadTime = Date().time

                    }

                    /**
                     * Called when an app open ad has failed to load.
                     *
                     * @param loadAdError the error.
                     */
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        isLoadingAd = false

                    }
                })
        }

        /** Check if ad was loaded more than n hours ago. */
        private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
            val dateDifference: Long = Date().time - loadTime
            val numMilliSecondsPerHour: Long = 3600000
            return dateDifference < numMilliSecondsPerHour * numHours
        }

        /** Check if ad exists and can be shown. */
        private fun isAdAvailable(): Boolean {
            // Ad references in the app open beta will time out after four hours, but this time limit
            // may change in future beta versions. For details, see:
            // https://support.google.com/admob/answer/9341964?hl=en
            return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
        }

        /**
         * Show the ad if one isn't already showing.
         *
         * @param activity the activity that shows the app open ad
         */
        fun showAdIfAvailable(activity: Activity) {
            showAdIfAvailable(
                activity,
                object : OnShowAdCompleteListener {
                    override fun onShowAdComplete() {
                        // Empty because the user will go back to the activity that shows the ad.
                    }
                })
        }

        /**
         * Show the ad if one isn't already showing.
         *
         * @param activity the activity that shows the app open ad
         * @param onShowAdCompleteListener the listener to be notified when an app open ad is complete
         */
        fun showAdIfAvailable(
            activity: Activity,
            onShowAdCompleteListener: OnShowAdCompleteListener,
        ) {

            if (activity is SplashActivity ) {
                onShowAdCompleteListener.onShowAdComplete()
                return
            }

            if (AdmobHelp.getInstance().isShowingInterstitialAd) {  // if ad Interstitial is showing then not show ad open
                onShowAdCompleteListener.onShowAdComplete()

                return
            }

            // If the app open ad is already showing, do not show the ad again.
            if (isShowingAd) {
                onShowAdCompleteListener.onShowAdComplete()

                return
            }


            // If the app open ad is not available yet, invoke the callback then load the ad.
            if (!isAdAvailable()) {
                onShowAdCompleteListener.onShowAdComplete()
                loadAd(activity)

                return
            }


            appOpenAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {
                /** Called when full screen content is dismissed. */
                override fun onAdDismissedFullScreenContent() {
                    // Set the reference to null so isAdAvailable() returns false.
                    appOpenAd = null
                    isShowingAd = false


                    onShowAdCompleteListener.onShowAdComplete()
                    loadAd(activity)

                }

                /** Called when fullscreen content failed to show. */
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    appOpenAd = null
                    isShowingAd = false


                    onShowAdCompleteListener.onShowAdComplete()
                    loadAd(activity)

                }

                /** Called when fullscreen content is shown. */
                override fun onAdShowedFullScreenContent() {

                }
            }
            isShowingAd = true
            appOpenAd!!.show(activity)
        }

        fun showAdIfAvailableSplash(
            activity: Activity,
            onShowAdCompleteListener: OnShowAdCompleteListener,
        ) {


            if (AdmobHelp.getInstance().isShowingInterstitialAd) {  // if ad Interstitial is showing then not show ad open
                Log.i("HAOHAO", " showAdIfAvailableSplash AdmobHelp.getInstance().isShowingInterstitialAd")
                onShowAdCompleteListener.onShowAdComplete()

                return
            }

            // If the app open ad is already showing, do not show the ad again.
            if (isShowingAd) {
                onShowAdCompleteListener.onShowAdComplete()

                Log.i("HAOHAO", " showAdIfAvailableSplash  isShowingAd")
                return
            }


            // If the app open ad is not available yet, invoke the callback then load the ad.
            if (!isAdAvailable()) {
                Log.i("HAOHAO", " showAdIfAvailableSplash  !isAdAvailable()")
                onShowAdCompleteListener.onShowAdComplete()
                loadAd(activity)

                return
            }


            appOpenAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {
                /** Called when full screen content is dismissed. */
                override fun onAdDismissedFullScreenContent() {
                    // Set the reference to null so isAdAvailable() returns false.
                    appOpenAd = null
                    isShowingAd = false


                    onShowAdCompleteListener.onShowAdComplete()
                    loadAd(activity)

                }

                /** Called when fullscreen content failed to show. */
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    appOpenAd = null
                    isShowingAd = false


                    onShowAdCompleteListener.onShowAdComplete()
                    loadAd(activity)

                }

                /** Called when fullscreen content is shown. */
                override fun onAdShowedFullScreenContent() {

                }
            }
            isShowingAd = true
            appOpenAd!!.show(activity)
        }
    }
}