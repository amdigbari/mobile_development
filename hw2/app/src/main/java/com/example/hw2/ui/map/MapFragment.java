package com.example.hw2.ui.map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.hw2.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFragment extends Fragment implements OnMapReadyCallback, PermissionsListener, MaterialSearchBar.OnSearchActionListener, SuggestionsAdapter.OnItemViewClickListener {

    private MapView mapView;
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private Marker marker;
    private MaterialSearchBar searchBar;
    private CustomSuggestionsAdapter customSuggestionsAdapter;
    private final List<CarmenFeature> searchedAddressesList = new ArrayList<>();
    public ExecutorService threadPoolExecutor =
            new ThreadPoolExecutor(5, 10, 5000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Mapbox.getInstance(this.requireContext(), getString(R.string.mapbox_access_token));

        View root = inflater.inflate(R.layout.fragment_map, container, false);

        initializeMap(root, savedInstanceState);
        initializeSearchBar(root, inflater);

        return root;
    }

    private void initializeSearchBar(View view, LayoutInflater inflater) {
        this.searchBar = view.findViewById(R.id.searchBar);
        this.searchBar.setHint("Search Address");
        this.searchBar.setSpeechMode(true);
        //enable searchbar callbacks
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

        FloatingActionButton currentLocationButton = view.findViewById(R.id.get_location);
        currentLocationButton.setOnClickListener(view1 -> getUserLocation());
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        this.mapboxMap.addOnMapLongClickListener(point -> {
            showNewMarker(new LatLng(point.getLatitude(), point.getLongitude()), false);
            // TODO: show modal and store data to database
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

    private void showNewMarker(LatLng latLng, boolean isUser) {
        if (this.marker != null) {
            this.mapboxMap.removeMarker(this.marker);
        }
        this.mapboxMap.animateCamera(mapboxMap -> new CameraPosition.Builder().target(latLng).zoom(16).build());
        this.marker = this.mapboxMap.addMarker(new MarkerOptions().position(latLng).icon(IconFactory.getInstance(requireActivity()).fromResource(isUser ? R.drawable.black_marker : R.drawable.red_marker)));
    }

    private void searchAddress(String address) {
        this.threadPoolExecutor.execute(() -> {
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
                public void onFailure(@NotNull Call<GeocodingResponse> call, Throwable t) {
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
    }

    @Override
    public void OnItemClickListener(int position, View v) {
        if (v.getTag() instanceof CarmenFeature) {
            CarmenFeature feature = (CarmenFeature) v.getTag();
            this.searchBar.hideSuggestionsList();
            this.searchBar.setText("");
            this.searchBar.clearFocus();

            LatLng latLng = new LatLng(feature.center().latitude(), feature.center().longitude());
            showNewMarker(latLng, false);

            // TODO: Show save modal
        }
    }

    @Override
    public void OnItemDeleteListener(int position, View v) {

    }
}