package com.example.hw2.ui.map;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.hw2.MainActivity;
import com.example.hw2.R;
import com.example.hw2.model.Place;
import com.example.hw2.repository.db.AppDatabase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFragment extends Fragment implements OnMapReadyCallback, PermissionsListener, MaterialSearchBar.OnSearchActionListener, SuggestionsAdapter.OnItemViewClickListener {
    public static final Integer RecordAudioRequestCode = 1;
    private MapView mapView;
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private Marker marker;
    private MaterialSearchBar searchBar;
    private CustomSuggestionsAdapter customSuggestionsAdapter;
    private final List<CarmenFeature> searchedAddressesList = new ArrayList<>();
    private TextView locationGeocode;
    private TextInputEditText locationName;
    private RelativeLayout saveModal;
    private LatLng savingLocation;
    private FloatingActionButton userLocation;
    private Disposable disposable;
    private boolean isFromBookmark = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        isFromBookmark = getArguments().getBoolean("isFromBookmark");

        Mapbox.getInstance(this.requireContext(), getString(R.string.mapbox_access_token));

        View root = inflater.inflate(R.layout.fragment_map, container, false);

        this.userLocation = root.findViewById(R.id.get_location);

        initializeSpeechRecognizer();

        initializeModal(root);
        initializeMap(root, savedInstanceState);
        initializeSearchBar(root, inflater);

        return root;
    }

    private void initializeSpeechRecognizer() {
        if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, RecordAudioRequestCode);
            }
        }
    }

    private void initializeModal(View view) {
        this.saveModal = view.findViewById(R.id.save_modal);
        this.locationGeocode = view.findViewById(R.id.location_geocode);
        this.locationName = view.findViewById(R.id.location_name);
        Button saveLocation = view.findViewById(R.id.save_location);
        Button cancelSave = view.findViewById(R.id.cancel_save);

        this.saveModal.setVisibility(View.INVISIBLE);

        saveLocation.setOnClickListener(v -> {
            if (Objects.requireNonNull(this.locationName.getText()).toString().trim().length() == 0) {
                this.saveToDB("Location " + Math.round(Math.random() * 5000), this.savingLocation);
            } else {
                this.saveToDB(this.locationName.getText().toString(), this.savingLocation);
            }
            this.hideModal();
        });

        cancelSave.setOnClickListener(v -> this.hideModal());
    }

    private void saveToDB(String name, LatLng latLng) {
        Place place = new Place();
        place.setLatitude(String.valueOf(latLng.getLatitude()));
        place.setLongitude(String.valueOf(latLng.getLongitude()));
        place.setName(name);
        place.setPriority(1);

        MainActivity.threadPoolExecutor.execute(() -> {
            AppDatabase database = AppDatabase.getInstance(getContext());

            disposable = Completable.fromAction(() -> database.getAppDao().insert(place))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> Toast.makeText(getContext(), "Location saved with name: " + place.getName(), Toast.LENGTH_LONG).show());
        });
    }

    private void hideModal() {
        this.deleteMarker();
        this.saveModal.setVisibility(View.INVISIBLE);
        this.userLocation.setVisibility(View.VISIBLE);
    }

    private void initializeSearchBar(View view, LayoutInflater inflater) {
        this.searchBar = view.findViewById(R.id.searchBar);
        this.searchBar.setHint("Search Address");
        this.searchBar.setSpeechMode(true);

        this.searchBar.setOnSearchActionListener(this);

        this.customSuggestionsAdapter = new CustomSuggestionsAdapter(inflater);
        this.customSuggestionsAdapter.setListener(this);
        this.customSuggestionsAdapter.setSuggestions(this.searchedAddressesList);
        this.searchBar.setCustomSuggestionAdapter(this.customSuggestionsAdapter);
    }

    private void initializeMap(View view, Bundle savedInstanceState) {
        this.mapView = view.findViewById(R.id.mapView);
        this.mapView.onCreate(savedInstanceState);
        this.mapView.getMapAsync(this);

        this.userLocation.setOnClickListener(view1 -> getUserLocation());
    }

    private void showSaveModal() {
        this.locationName.setText("");
        this.locationGeocode.setText(new StringBuilder("Save Location (\"").append(this.savingLocation.getLatitude()).append("\", \"").append(this.savingLocation.getLatitude()).append("\")"));
        this.saveModal.setVisibility(View.VISIBLE);
        this.locationName.requestFocus();
        this.userLocation.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        this.mapboxMap.addOnMapLongClickListener(point -> {
            this.savingLocation = new LatLng(point.getLatitude(), point.getLongitude());
            showNewMarker(this.savingLocation, false);
            this.showSaveModal();
            return false;
        });

        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/streets-v11"),
                style -> getUserLocation());
    }

    @SuppressWarnings({"MissingPermission"})
    private void getUserLocation() {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this.requireContext())) {
            LocationManager lm = (LocationManager) this.requireActivity().getSystemService(Context.LOCATION_SERVICE);
            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    showNewMarker(new LatLng(location.getLatitude(), location.getLongitude()), true);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            };
            assert lm != null;
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 100, locationListener);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this.getActivity());
        }
    }

    private void deleteMarker() {
        if (this.marker != null) {
            this.mapboxMap.removeMarker(this.marker);
        }
    }

    private void showNewMarker(LatLng latLng, boolean isUser) {
        this.deleteMarker();
        this.mapboxMap.animateCamera(mapboxMap -> new CameraPosition.Builder().target(latLng).zoom(16).build());
        this.marker = this.mapboxMap.addMarker(new MarkerOptions().position(latLng).icon(IconFactory.getInstance(requireActivity()).fromResource(isUser ? R.drawable.black_marker : R.drawable.red_marker)));
    }

    private void searchAddress(String address) {
        MainActivity.threadPoolExecutor.execute(() -> {
            MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
                    .accessToken(getResources().getString(R.string.mapbox_access_token))
                    .query(address)
                    .build();

            mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
                @Override
                public void onResponse(@NotNull Call<GeocodingResponse> call, @NotNull Response<GeocodingResponse> response) {
                    assert response.body() != null;
                    List<CarmenFeature> results = response.body().features();

                    searchedAddressesList.clear();
                    if (results.size() > 0) {
                        searchedAddressesList.addAll(results);
                        searchBar.showSuggestionsList();
                    } else {
                        searchBar.hideSuggestionsList();
                    }
                    customSuggestionsAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(@NotNull Call<GeocodingResponse> call, @NotNull Throwable t) {
                    t.printStackTrace();
                }
            });
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this.getContext(), R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            getUserLocation();
        } else {
            Toast.makeText(this.getContext(), R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        if (disposable != null) {
            disposable.dispose();
        }
        super.onDestroyView();
        mapView.onDestroy();
    }

    @Override
    public void onSearchStateChanged(boolean enabled) {
    }

    @Override
    public void onSearchConfirmed(CharSequence text) {
        searchAddress(text.toString());
    }

    @Override
    public void onButtonClicked(int buttonCode) {
        if (buttonCode == 1) {
            Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");
            try {
                startActivityForResult(speechRecognizerIntent, RecordAudioRequestCode);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void OnItemClickListener(int position, View v) {
        if (v.getTag() instanceof CarmenFeature) {
            CarmenFeature feature = (CarmenFeature) v.getTag();
            this.searchBar.hideSuggestionsList();
            this.searchBar.setText("");
            this.searchBar.clearFocus();

            this.savingLocation = new LatLng(Objects.requireNonNull(feature.center()).latitude(), feature.center().longitude());
            showNewMarker(this.savingLocation, false);
            showSaveModal();
        }
    }

    @Override
    public void OnItemDeleteListener(int position, View v) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RecordAudioRequestCode) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String text = result.get(0);
                this.searchBar.setText(text);
                this.searchBar.setPlaceHolder(text);
                searchAddress(text);
            }
        }
    }
}