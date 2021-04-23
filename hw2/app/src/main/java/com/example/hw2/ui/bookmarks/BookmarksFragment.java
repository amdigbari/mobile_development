package com.example.hw2.ui.bookmarks;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hw2.MainActivity;
import com.example.hw2.R;
import com.example.hw2.model.Place;
import com.example.hw2.repository.db.AppDatabase;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.ArrayList;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class BookmarksFragment extends Fragment implements MaterialSearchBar.OnSearchActionListener {
    public RecyclerView mRecyclerView;
    public BookmarksAdapter mBookmarkAdapter;
    private Disposable disposable;
    private final ArrayList<Place> databasePlaces = new ArrayList<>();
    private final ArrayList<Place> places = new ArrayList<>();

    @SuppressLint("ResourceType")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bookmarks, container, false);

        this.mRecyclerView = root.findViewById(R.id.bookmarks_list);
        this.mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(root.getContext());
        this.mRecyclerView.setLayoutManager(mLayoutManager);
        this.mBookmarkAdapter = new BookmarksAdapter(places, position -> {
            System.out.println("Show Map");
            // TODO: navigate to map tab
        }, position -> {
            removeFromDb(mBookmarkAdapter.getItem(position));
        });
        this.mRecyclerView.setAdapter(this.mBookmarkAdapter);

        initializeSearchBar(root);

        readPlacesFromDb();

        return root;
    }

    private void initializeSearchBar(View view) {
        MaterialSearchBar searchBar = view.findViewById(R.id.search_bookmarks);
        searchBar.setHint("Search Address");
        searchBar.setSpeechMode(true);
        searchBar.setOnSearchActionListener(this);
    }

    private void readPlacesFromDb() {
        MainActivity.threadPoolExecutor.execute(() -> {
            disposable = Single.fromCallable(() -> AppDatabase.getInstance(getContext()).getAppDao().getAllPlaces())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((dbPlaces, throwable) -> {
                        databasePlaces.addAll(dbPlaces);
                        resetPlaces();
                    });
        });
    }

    private void resetPlaces() {
        this.places.clear();
        this.places.addAll(this.databasePlaces);
        this.mBookmarkAdapter.notifyDataSetChanged();
    }

    private void removeFromDb(Place place) {
        MainActivity.threadPoolExecutor.execute(() -> {
            AppDatabase database = AppDatabase.getInstance(getContext());
            disposable = Completable.fromAction(() -> {
                database.getAppDao().delete(place);
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                        databasePlaces.remove(place);
                        places.remove(place);
                        mBookmarkAdapter.notifyDataSetChanged();
                        Toast.makeText(getContext(), R.string.place_removed, Toast.LENGTH_LONG).show();
                    });
        });
    }

    private void searchPlaces(String text) {
        this.places.clear();
        for (Place place : databasePlaces) {
            if (place.getName().toLowerCase().contains(text.toLowerCase())) {
                this.places.add(place);
            }
        }
        this.mBookmarkAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        if (disposable != null)
            disposable.dispose();
        super.onDestroy();
    }

    @Override
    public void onSearchStateChanged(boolean enabled) {

    }

    @Override
    public void onSearchConfirmed(CharSequence text) {
        if (text.length() > 0 && text.toString().trim().length() > 0) {
            this.searchPlaces(text.toString());
        }
    }

    @Override
    public void onButtonClicked(int buttonCode) {

    }
}