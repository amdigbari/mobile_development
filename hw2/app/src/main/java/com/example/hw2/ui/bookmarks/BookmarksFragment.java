package com.example.hw2.ui.bookmarks;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hw2.MainActivity;
import com.example.hw2.R;
import com.example.hw2.model.Place;
import com.example.hw2.repository.db.AppDatabase;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.ArrayList;
import java.util.Locale;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class BookmarksFragment extends Fragment implements MaterialSearchBar.OnSearchActionListener {
    public static final Integer RecordAudioRequestCode = 1;
    public RecyclerView mRecyclerView;
    public BookmarksAdapter mBookmarkAdapter;
    private Disposable disposable;
    private final ArrayList<Place> databasePlaces = new ArrayList<>();
    private final ArrayList<Place> places = new ArrayList<>();
    private MaterialSearchBar searchBar;

    @SuppressLint("ResourceType")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bookmarks, container, false);

        this.mRecyclerView = root.findViewById(R.id.bookmarks_list);
        this.mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(root.getContext());
        this.mRecyclerView.setLayoutManager(mLayoutManager);
        this.mBookmarkAdapter = new BookmarksAdapter(places, position -> navigate(mBookmarkAdapter.getItem(position)), position -> removeFromDb(mBookmarkAdapter.getItem(position)));

        this.mRecyclerView.setAdapter(this.mBookmarkAdapter);



        initializeSearchBar(root);

        initializeSpeechRecognizer();

        readPlacesFromDb();

        return root;
    }

    private void initializeSpeechRecognizer() {
        if (ContextCompat.checkSelfPermission(this.requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this.requireActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, RecordAudioRequestCode);
            }
        }
    }

    private void initializeSearchBar(View view) {
        this.searchBar = view.findViewById(R.id.search_bookmarks);
        this.searchBar.setHint("Search Address");
        this.searchBar.setSpeechMode(true);
        this.searchBar.setOnSearchActionListener(this);
    }

    private void initializeSpeechRecognizer() {
        if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, RecordAudioRequestCode);
            }
        }
    }

    private void initializeSearchBar(View view) {
        this.searchBar = view.findViewById(R.id.search_bookmarks);
        this.searchBar.setHint("Search Address");
        this.searchBar.setSpeechMode(true);
        this.searchBar.setOnSearchActionListener(this);
    }

    private void readPlacesFromDb() {
        MainActivity.threadPoolExecutor.execute(() -> disposable = Single.fromCallable(() -> AppDatabase.getInstance(getContext()).getAppDao().getAllPlaces())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((dbPlaces, throwable) -> {
                    databasePlaces.addAll(dbPlaces);
                    resetPlaces();
                }));
    }

    private void resetPlaces() {
        this.places.clear();
        this.places.addAll(this.databasePlaces);
        this.mBookmarkAdapter.notifyDataSetChanged();
    }

    private void removeFromDb(Place place) {
        MainActivity.threadPoolExecutor.execute(() -> {
            AppDatabase database = AppDatabase.getInstance(getContext());
            disposable = Completable.fromAction(() -> database.getAppDao().delete(place))
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
            this.searchBar.setPlaceHolder(text.toString());
        } else {
            this.resetPlaces();
            this.searchBar.setPlaceHolder("Search Address");
        }
    }

    @Override
    public void onButtonClicked(int buttonCode) {
        if (buttonCode == 1) {
            Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");
            try {
                startActivityForResult(speechRecognizerIntent, BookmarksFragment.RecordAudioRequestCode);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BookmarksFragment.RecordAudioRequestCode) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                assert result != null;
                String text = result.get(0);
                this.searchBar.setText(text);
                this.searchBar.setPlaceHolder(text);
                searchPlaces(text);
            }
        }
    }

    private void navigate(Place place) {
        Bundle args = new Bundle();
        args.putParcelable("place", place);
        args.putBoolean("isFromBookmark", true);
        Navigation.findNavController(this.requireView()).navigate(R.id.action_bookmarkFragment_to_mapFragment, args);
    }

}