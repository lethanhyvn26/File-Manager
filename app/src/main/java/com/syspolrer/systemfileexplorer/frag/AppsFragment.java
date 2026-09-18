package com.syspolrer.systemfileexplorer.frag;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.syspolrer.systemfileexplorer.databinding.FragmentAppsBinding;
import com.syspolrer.systemfileexplorer.utils.Utils;

import java.util.ArrayList;

public class AppsFragment extends Fragment {

    private FragmentAppsBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAppsBinding.inflate(inflater);
        AppsPagerAdapter pagerAdapter = new AppsPagerAdapter(getChildFragmentManager());

        pagerAdapter.addFragment(new AppsInstalledFragment(), "Installed Apps");
        pagerAdapter.addFragment(new APKFragment(), "Apks");

        binding.viewPager.setAdapter(pagerAdapter);
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        binding.ivBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        binding.rootApps.setOnClickListener(v -> Utils.hideKeyboard(requireActivity()));

        return binding.getRoot();
    }

    @Override
    public void onStop() {
        super.onStop();
        Utils.hideKeyboard(requireActivity());
    }

    private class AppsPagerAdapter extends FragmentPagerAdapter {

        ArrayList<Fragment> fragmentList = new ArrayList<>();
        ArrayList<String> stringList = new ArrayList<>();

        public AppsPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            stringList.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return stringList.get(position);
        }
    }
}