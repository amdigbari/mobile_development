package com.example.hw2.ui.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.hw2.MainActivity;
import com.example.hw2.R;
import com.example.hw2.repository.db.AppDatabase;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


@SuppressLint("UseSwitchCompatOrMaterialCode")
public class SettingsFragment extends Fragment {

    Disposable disposable;
    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        Switch switchDark = (Switch) root.findViewById(R.id.switch1);
        switchDark.setText(R.string.dark_mode);
        // Saving state of our app using SharedPreferences
        SharedPreferences sharedPreferences = this.requireActivity().getSharedPreferences("shrredPrefs", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        final boolean isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn", false);

        switchDark.setChecked(isDarkModeOn);

        Button btnDeleteDataButton = root.findViewById(R.id.data_button);
        btnDeleteDataButton.setOnClickListener(v -> {
            disposable = Completable.fromAction(() -> AppDatabase.getInstance(getContext()).getAppDao().deleteAllPlaces())
                    .subscribeOn(Schedulers.from(MainActivity.threadPoolExecutor))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> Toast.makeText(getContext(), "Cleared", Toast.LENGTH_SHORT).show());
        });

        switchDark.setOnClickListener(
                view -> {
                    // When user taps the enable/disable dark mode button
                    if (isDarkModeOn) {
                        // if dark mode is on it will turn it off
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        // it will set isDarkModeOn boolean to false
                        editor.putBoolean("isDarkModeOn", false);
                    } else {
                        // if dark mode is off it will turn it on
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        // it will set isDarkModeOn boolean to true
                        editor.putBoolean("isDarkModeOn", true);
                    }
                    editor.apply();
                });
        return root;
    }

    @Override
    public void onDestroy() {
        if (disposable != null) {
            disposable.dispose();
        }
        super.onDestroy();
    }
}