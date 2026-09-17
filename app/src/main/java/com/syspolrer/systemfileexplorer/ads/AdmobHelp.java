package com.syspolrer.systemfileexplorer.ads;


import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.syspolrer.systemfileexplorer.MyApplication;
import com.syspolrer.systemfileexplorer.R;


public class AdmobHelp {
    private static AdmobHelp instance;
    private InterstitialAd mInterstitialAd;
    private AdCloseListener adCloseListener;
    private boolean isShowing = false;


    private boolean isReload = false;

    public static AdmobHelp getInstance() {
        if (instance == null) {
            instance = new AdmobHelp();
        }
        return instance;
    }

    private AdmobHelp() {

    }

    public void init(Context context) {
        MobileAds.initialize(context, initializationStatus -> {
        });
        loadInterstitialAd(context);
    }

    private void loadInterstitialAd(final Context context) {
        if (mInterstitialAd != null) return;

        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(context, context.getString(R.string.str_ads_interstitial), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                mInterstitialAd = interstitialAd;
                isReload = false;
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when fullscreen content is dismissed.
                        adCloseListener.onAdClosed();
                        isShowing = false;

                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // Called when fullscreen content failed to show.
                        adCloseListener.onAdClosed();
                        isShowing = false;

                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        mInterstitialAd = null;
                        loadInterstitialAd(context);
                        isShowing = true;


                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                mInterstitialAd = null;
                if (!isReload) {
                    isReload = true;
                    loadInterstitialAd(context);
                }
                isShowing = false;

            }
        });
    }

    public void showInterstitialAd(Activity activity, final AdCloseListener adCloseListener) {
        try {
            if (canShowInterstitialAd(activity) && !MyApplication.Companion.getInstance().getOpenAdsManager().isShowingAd()) {
                this.adCloseListener = adCloseListener;
                mInterstitialAd.show(activity);

            } else {
                if(mInterstitialAd==null){

                    loadInterstitialAd(activity);
                }
                adCloseListener.onAdClosed();
            }
        }catch (Exception ex){
            adCloseListener.onAdClosed();

        }

    }

    private boolean canShowInterstitialAd(Context context) {
        return mInterstitialAd != null && context instanceof Activity;
    }

    public interface AdCloseListener {
        void onAdClosed();
    }

    public Boolean isShowingInterstitialAd() {

        return (mInterstitialAd != null && isShowing);

    }

}
