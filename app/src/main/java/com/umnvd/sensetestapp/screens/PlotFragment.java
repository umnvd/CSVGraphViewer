package com.umnvd.sensetestapp.screens;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.umnvd.sensetestapp.R;
import com.umnvd.sensetestapp.data.CSVFileReader;
import com.umnvd.sensetestapp.views.PlotView;

public class PlotFragment extends Fragment {

    private static final String URI_KEY = "uri";

    private CSVFileReader reader;

    public PlotFragment() {
        super(R.layout.fragment_plot);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        reader = CSVFileReader.getInstance(requireContext().getApplicationContext());
        PlotView plotView = view.findViewById(R.id.plotView);
        Uri uri = requireArguments().getParcelable(URI_KEY);
        reader.read(uri, plotView::setPoints, this::toast);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        reader = null;
    }

    private void toast(@StringRes int messageId) {
        Toast.makeText(requireContext(), messageId, Toast.LENGTH_SHORT).show();
    }

    public static PlotFragment newInstance(Uri uri) {
        Bundle args = new Bundle();
        args.putParcelable(URI_KEY, uri);
        PlotFragment fragment = new PlotFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
