package com.umnvd.sensetestapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.umnvd.sensetestapp.screens.HomeFragment;

public class MainActivity extends AppCompatActivity implements Navigator {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            launchFragment(new HomeFragment(), false);
        }
    }

    @Override
    public void navigateTo(Fragment fragment) {
        launchFragment(fragment, true);
    }

    private void launchFragment(Fragment fragment, boolean addToBackstack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.mainContainer, fragment);
        if (addToBackstack) transaction.addToBackStack(null);
        transaction.commit();
    }

}