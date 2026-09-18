package com.syspolrer.systemfileexplorer.frag;

import java.util.Locale;

import static com.syspolrer.systemfileexplorer.utils.ViewExtenstionKt.checkShowAdsFull;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.syspolrer.systemfileexplorer.activity.MainActivity;
import com.syspolrer.systemfileexplorer.R;
import com.syspolrer.systemfileexplorer.adap.DocumentAdapter;
import com.syspolrer.systemfileexplorer.databinding.FilterBottomSheetBinding;
import com.syspolrer.systemfileexplorer.databinding.FragmentDocumentBinding;
import com.syspolrer.systemfileexplorer.interfaces.ActionModeListener;
import com.syspolrer.systemfileexplorer.interfaces.FileSelectedListener;
import com.syspolrer.systemfileexplorer.model.MediaModel;
import com.syspolrer.systemfileexplorer.utils.OpenFile;
import com.syspolrer.systemfileexplorer.utils.Utils;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.Executors;

public class DocumentFragment extends Fragment implements FileSelectedListener, ActionModeListener {

    private FragmentDocumentBinding binding;
    private final ArrayList<File> doc = new ArrayList<>();
    private DocumentAdapter docAdapter;
    private boolean cbPdf = false;
    private boolean cbDoc = false;
    private boolean cbXls = false;
    private boolean cbPpt = false;
    private boolean cbTxt = false;
    private boolean cbOthers = false;
    public static ActionModeListener actionModeListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDocumentBinding.inflate(inflater);

        binding.rv.setLayoutManager(new LinearLayoutManager(requireActivity()));
        docAdapter = new DocumentAdapter(requireActivity(), this);
        binding.rv.setAdapter(docAdapter);

        binding.loader.setVisibility(View.VISIBLE);
        setData();

        binding.ivFilter.setOnClickListener(v -> {
            showBottomSheet();
            showAdsFull();
        });

        actionModeListener = this;

        binding.ivBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        binding.rootDocument.setOnClickListener(v -> Utils.hideKeyboard(requireActivity()));
        binding.et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                docAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return binding.getRoot();
    }

    @Override
    public void onStop() {
        super.onStop();
        Utils.hideKeyboard(requireActivity());
    }


    private void setData() {
        doc.clear();

        Executors.newSingleThreadExecutor().execute(() -> {
            if (Utils.EXTERNAL_FILE != null) {
                doc.addAll(doc(Utils.EXTERNAL_FILE));
            }
            if (Utils.SD_CARD_FILE != null) {
                doc.addAll(doc(Utils.SD_CARD_FILE));
            }

            docAdapter.addAll(doc,this,binding.loader,binding.noData);

        });
    }

    ArrayList<File> doc(File file) {

        ArrayList<File> fileArrayList = new ArrayList<>();
        File[] files = file.listFiles();
        if (files != null) {
            for (File singleFile : files) {
                if (singleFile.isDirectory() && !singleFile.isHidden()) {
                    fileArrayList.addAll(doc(singleFile));
                } else {
                    if (!singleFile.isHidden() && singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".doc") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".docx") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".xls") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".xlsx") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".txt") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".ppt") || singleFile.getName().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
                        fileArrayList.add(singleFile);
                    }
                }
            }
        }

        return fileArrayList;
    }

    private ArrayList<File> getFilter(ArrayList<File> files) {
        ArrayList<File> filterList = new ArrayList<>();
        for (File file : files) {
            if (cbPdf && cbDoc && cbXls && cbPpt && cbTxt && cbOthers) {
                filterList.add(file);
            } else if (!cbPdf && !cbDoc && !cbXls && !cbPpt && !cbTxt && !cbOthers) {
                filterList.add(file);
            } else {
                if (cbPdf) {
                    if (file.getName().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
                        filterList.add(file);
                    }
                }
                if (cbDoc) {
                    if (file.getName().toLowerCase(Locale.ROOT).endsWith(".doc") || file.getName().toLowerCase(Locale.ROOT).endsWith(".docx")) {
                        filterList.add(file);
                    }
                }
                if (cbXls) {
                    if (file.getName().toLowerCase(Locale.ROOT).endsWith(".xls")) {
                        filterList.add(file);
                    }
                }
                if (cbPpt) {
                    if (file.getName().toLowerCase(Locale.ROOT).endsWith("ppt") || file.getName().toLowerCase(Locale.ROOT).endsWith("pptx")) {
                        filterList.add(file);
                    }
                }
                if (cbTxt) {
                    if (file.getName().toLowerCase(Locale.ROOT).endsWith(".txt")) {
                        filterList.add(file);
                    }
                }
                if (cbOthers) {
                    if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".pdf") || !file.getName().toLowerCase(Locale.ROOT).endsWith(".doc") || !file.getName().toLowerCase(Locale.ROOT).endsWith(".docx") || !file.getName().toLowerCase(Locale.ROOT).endsWith(".xls") || !file.getName().toLowerCase(Locale.ROOT).endsWith("ppt") || !file.getName().toLowerCase(Locale.ROOT).endsWith("pptx") || !file.getName().toLowerCase(Locale.ROOT).endsWith(".txt")) {
                        filterList.add(file);
                    }
                }
            }
        }
        return filterList;
    }

    @Override
    public void onFileSelect(ImageView imageView, File file) {
        OpenFile.open(file);
//        showAdsFull();

    }

    @Override
    public void onBind(ImageView imageView, File file) {

    }

    @Override
    public void onIvSelectClick(ArrayList<File> files, int position, ImageView imageView) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("List", files);
        bundle.putInt("Position", position);
        bundle.putString("From", "Doc");
        SelectAppsFragment selectAppsFragment = new SelectAppsFragment();
        selectAppsFragment.setArguments(bundle);
        requireActivity().getSupportFragmentManager().beginTransaction().add(MainActivity.MAIN_CONTAINER, selectAppsFragment).addToBackStack(null).commit();

        showAdsFull();
    }

    @Override
    public void onIvMediaSelectClick(ArrayList<MediaModel> files, int position, ImageView imageView) {

    }

    private void showBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext(),R.style.BottomSheetStyle);
        FilterBottomSheetBinding bottomSheetBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.filter_bottom_sheet, null, false);
        bottomSheetDialog.setContentView(bottomSheetBinding.getRoot());
        bottomSheetDialog.show();

        if (cbPdf) {
            bottomSheetBinding.ivPdf.setImageResource(R.drawable.ic_checkbox);
        } else {
            bottomSheetBinding.ivPdf.setImageResource(R.drawable.ic_uncheck_checkbox);
        }
        if (cbDoc) {
            bottomSheetBinding.ivDoc.setImageResource(R.drawable.ic_checkbox);
        } else {
            bottomSheetBinding.ivDoc.setImageResource(R.drawable.ic_uncheck_checkbox);
        }

        if (cbXls) {
            bottomSheetBinding.ivXls.setImageResource(R.drawable.ic_checkbox);
        } else {
            bottomSheetBinding.ivXls.setImageResource(R.drawable.ic_uncheck_checkbox);
        }

        if (cbPpt) {
            bottomSheetBinding.ivPpt.setImageResource(R.drawable.ic_checkbox);
        } else {
            bottomSheetBinding.ivPpt.setImageResource(R.drawable.ic_uncheck_checkbox);
        }

        if (cbTxt) {
            bottomSheetBinding.ivTxt.setImageResource(R.drawable.ic_checkbox);
        } else {
            bottomSheetBinding.ivTxt.setImageResource(R.drawable.ic_uncheck_checkbox);
        }

        if (cbOthers) {
            bottomSheetBinding.ivOthers.setImageResource(R.drawable.ic_checkbox);
        } else {
            bottomSheetBinding.ivOthers.setImageResource(R.drawable.ic_uncheck_checkbox);
        }

        bottomSheetBinding.lyPdf.setOnClickListener(v -> {
            if (!cbPdf) {
                bottomSheetBinding.ivPdf.setImageResource(R.drawable.ic_checkbox);
                cbPdf = true;
            } else {
                bottomSheetBinding.ivPdf.setImageResource(R.drawable.ic_uncheck_checkbox);
                cbPdf = false;
            }
            docAdapter.addAll(getFilter(doc),this,null,null);
        });

        bottomSheetBinding.lyDoc.setOnClickListener(v -> {
            if (!cbDoc) {
                bottomSheetBinding.ivDoc.setImageResource(R.drawable.ic_checkbox);
                cbDoc = true;
            } else {
                bottomSheetBinding.ivDoc.setImageResource(R.drawable.ic_uncheck_checkbox);
                cbDoc = false;
            }
            docAdapter.addAll(getFilter(doc),this,null,null);
        });

        bottomSheetBinding.lyXls.setOnClickListener(v -> {
            if (!cbXls) {
                bottomSheetBinding.ivXls.setImageResource(R.drawable.ic_checkbox);
                cbXls = true;
            } else {
                bottomSheetBinding.ivXls.setImageResource(R.drawable.ic_uncheck_checkbox);
                cbXls = false;
            }
            docAdapter.addAll(getFilter(doc),this,null,null);
        });

        bottomSheetBinding.lyPpt.setOnClickListener(v -> {
            if (!cbPpt) {
                bottomSheetBinding.ivPpt.setImageResource(R.drawable.ic_checkbox);
                cbPpt = true;
            } else {
                bottomSheetBinding.ivPpt.setImageResource(R.drawable.ic_uncheck_checkbox);
                cbPpt = false;
            }
            docAdapter.addAll(getFilter(doc),this,null,null);
        });

        bottomSheetBinding.lyTxt.setOnClickListener(v -> {
            if (!cbTxt) {
                bottomSheetBinding.ivTxt.setImageResource(R.drawable.ic_checkbox);
                cbTxt = true;
            } else {
                bottomSheetBinding.ivTxt.setImageResource(R.drawable.ic_uncheck_checkbox);
                cbTxt = false;
            }
            docAdapter.addAll(getFilter(doc),this,null,null);
        });

        bottomSheetBinding.lyOthers.setOnClickListener(v -> {
            if (!cbOthers) {
                bottomSheetBinding.ivOthers.setImageResource(R.drawable.ic_checkbox);
                cbOthers = true;
            } else {
                bottomSheetBinding.ivOthers.setImageResource(R.drawable.ic_uncheck_checkbox);
                cbOthers = false;
            }
            docAdapter.addAll(getFilter(doc),this,null,null);
        });

    }

    @Override
    public void onEventListener(int event) {
        switch (event) {
            case Utils.EVENT_ADD_TO_FAV:

            case Utils.EVENT_CLOSE:
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
                break;

            case Utils.EVENT_DELETE:
                setData();
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
                break;

            case Utils.EVENT_COPY:
            case Utils.EVENT_MOVE:
                setData();
                new Handler(Looper.getMainLooper()).post(() -> {
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                });
                break;
        }
        showAdsFull();
    }

    private void showAdsFull() {
        if (checkShowAdsFull()) {
            ((MainActivity) requireActivity()).showAdsFull();
        }
    }
}