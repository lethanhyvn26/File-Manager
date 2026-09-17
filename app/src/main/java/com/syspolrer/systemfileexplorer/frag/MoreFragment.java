package com.syspolrer.systemfileexplorer.frag;

import static com.syspolrer.systemfileexplorer.utils.ViewExtenstionKt.checkShowAdsFull;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.syspolrer.systemfileexplorer.activity.MainActivity;
import com.syspolrer.systemfileexplorer.adap.HomeAppAdapter;
import com.syspolrer.systemfileexplorer.databinding.FragmentMoreBinding;
import com.syspolrer.systemfileexplorer.interfaces.HomeAppClickListener;


public class MoreFragment extends Fragment implements HomeAppClickListener {

    private FragmentMoreBinding binding;
    private final String[] apps = {"Images", "Audio", "Videos", "Zips", "Apps", "Document", "Download"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMoreBinding.inflate(inflater);

        binding.rv.setLayoutManager(new GridLayoutManager(getContext(), 4));
        HomeAppAdapter homeAppAdapter = new HomeAppAdapter(requireActivity(), apps, this);
        binding.rv.setAdapter(homeAppAdapter);
        binding.ivBack.setOnClickListener(v -> requireActivity().onBackPressed());

        binding.icFavourite.setOnClickListener(v ->
                {
                    requireActivity().getSupportFragmentManager().beginTransaction().replace(MainActivity.MAIN_CONTAINER, new FavouriteFragment()).addToBackStack(null).commit();
                    showAdsFull();
                }
        );

        binding.icLargeFiles.setOnClickListener(v -> {

            requireActivity().getSupportFragmentManager().beginTransaction().replace(MainActivity.MAIN_CONTAINER, new LargeFileFragment()).addToBackStack(null).commit();
            showAdsFull();
        });

        return binding.getRoot();
    }

    @Override
    public void onHomeAppClick(String name) {
        if (apps[0].equals(name) || apps[1].equals(name) || apps[2].equals(name)) {
            Bundle bundle = new Bundle();
            bundle.putString("FILE", name);
            ImageFragment imageFragment = new ImageFragment();
            imageFragment.setArguments(bundle);
            requireActivity().getSupportFragmentManager().beginTransaction().replace(MainActivity.MAIN_CONTAINER, imageFragment).addToBackStack(null).commit();
        } else if (apps[3].equals(name)) {
            requireActivity().getSupportFragmentManager().beginTransaction().replace(MainActivity.MAIN_CONTAINER, new ZipFragment()).addToBackStack(null).commit();
        } else if (apps[4].equals(name)) {

            requireActivity().getSupportFragmentManager().beginTransaction().replace(MainActivity.MAIN_CONTAINER, new AppsFragment()).addToBackStack(null).commit();
        } else if (apps[5].equals(name)) {
            requireActivity().getSupportFragmentManager().beginTransaction().replace(MainActivity.MAIN_CONTAINER, new DocumentFragment()).addToBackStack(null).commit();
        } else if (apps[6].equals(name)) {
            requireActivity().getSupportFragmentManager().beginTransaction().replace(MainActivity.MAIN_CONTAINER, new DownloadFragment()).addToBackStack(null).commit();
        }
        showAdsFull();
    }

    private void showAdsFull() {
        if (checkShowAdsFull()) {
            ((MainActivity) requireActivity()).showAdsFull();
        }
    }
}