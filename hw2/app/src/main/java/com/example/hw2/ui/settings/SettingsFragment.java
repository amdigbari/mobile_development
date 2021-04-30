package com.example.hw2.ui.settings;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.hw2.MainActivity;
import com.example.hw2.R;
import com.example.hw2.model.Place;
import com.example.hw2.repository.db.AppDatabase;


@SuppressLint("UseSwitchCompatOrMaterialCode")
public class SettingsFragment extends Fragment {
    private Disposable disposable;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        Switch switchDark = (Switch) root.findViewById(R.id.switch1);
        switchDark.setText(R.string.dark_mode);
        Button data = root.findViewById(R.id.data_button);
        // Saving state of our app using SharedPreferences
        SharedPreferences sharedPreferences = this.requireActivity().getSharedPreferences("shrredPrefs", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        final boolean isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn", false);

        data.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

            builder.setTitle(R.string.clear_data);
            builder.setMessage(R.string.sure_about_clear_data);

            builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
                clearDb();
                dialog.dismiss();
            });
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

            AlertDialog alert = builder.create();
            alert.show();
        });

        switchDark.setChecked(isDarkModeOn);

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

    private void clearDb() {
        MainActivity.threadPoolExecutor.execute(() -> {
            AppDatabase database = AppDatabase.getInstance(getContext());
            disposable = Completable.fromAction(() -> database.getAppDao().deleteAllPlaces())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> Toast.makeText(getContext(), R.string.data_deleted, Toast.LENGTH_LONG).show());
        });
    }
}