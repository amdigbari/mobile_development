package com.example.hw2.ui.bookmarks;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.hw2.R;
import com.example.hw2.repository.db.AppDatabase;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class BookmarksFragment extends Fragment {

    private BookmarksViewModel bookmarksViewModel;
    private Disposable disposable;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        bookmarksViewModel =
                new ViewModelProvider(this).get(BookmarksViewModel.class);
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        final TextView textView = root.findViewById(R.id.text_bookmarks);
        bookmarksViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        readPlacesFromDb();

        return root;
    }

    private void readPlacesFromDb() {
        disposable = Single.fromCallable(() -> AppDatabase.getInstance(getContext()).getAppDao().getAllPlaces())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((places, throwable) -> {
                    for (int i = 0; i < places.size(); i++) {
                        Log.d("DataBase", places.get(i).toString());
                    }
                });
    }

    @Override
    public void onDestroy() {
        if (disposable != null)
            disposable.dispose();
        super.onDestroy();
    }
}