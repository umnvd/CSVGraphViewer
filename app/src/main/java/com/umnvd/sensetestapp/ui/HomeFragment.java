package com.umnvd.sensetestapp.ui;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.umnvd.sensetestapp.Navigator;
import com.umnvd.sensetestapp.R;
import com.umnvd.sensetestapp.data.ChooseFileContract;

public class HomeFragment extends Fragment {

    private Navigator navigator;

    private final ActivityResultLauncher<Uri> chooseFileLauncher = registerForActivityResult(
            new ChooseFileContract(),
            uri -> {
                if (uri == null) toast(R.string.uri_error);
                else if (navigator != null) navigator.navigateTo(PlotFragment.newInstance(uri));
            }
    );

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navigator = (Navigator) requireActivity();

        Button chooseFileButton = view.findViewById(R.id.chooseButton);
        chooseFileButton.setOnClickListener(v -> chooseFileLauncher.launch(null));
    }

    private void toast(@StringRes int messageId) {
        Toast.makeText(requireContext(), messageId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        navigator = null;
    }
}
