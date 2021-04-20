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
import com.example.hw2.model.Place;
import com.example.hw2.repository.db.AppDatabase;

import io.reactivex.Completable;
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
        View root = inflater.inflate(R.layout.fragment_bookmarks, container, false);
        final TextView textView = root.findViewById(R.id.text_bookmarks);
        bookmarksViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        InsertToDb();

        return root;
    }

    private void InsertToDb() {
        Place place1 = new Place();
        place1.setLatitude("lat1");
        place1.setLongitude("long1");
        place1.setName("Qom");
        place1.setPriority(1);

        Place place2 = new Place();
        place2.setLatitude("lat2");
        place2.setLongitude("long2");
        place2.setName("isfahan");
        place2.setPriority(2);

        Place place3 = new Place();
        place3.setLatitude("lat3");
        place3.setLongitude("long3");
        place3.setName("tehran");
        place3.setPriority(3);

        AppDatabase database = AppDatabase.getInstance(getContext());

        disposable = Completable.fromAction(() -> {
            database.getAppDao().deleteAllPlaces();
            database.getAppDao().insert(place1);
            database.getAppDao().insert(place2);
            database.getAppDao().insert(place3);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> Log.d("DataBase", "inserted successfully"));
    }

    @Override
    public void onDestroy() {
        if (disposable != null)
            disposable.dispose();
        super.onDestroy();
    }
}