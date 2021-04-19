package com.example.hw2.ui.map;

import android.content.Context;
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
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
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

import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback, PermissionsListener {

    private MapView mapView;
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private Marker userMarker;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Mapbox.getInstance(this.requireContext(), getString(R.string.mapbox_access_token));

        View root = inflater.inflate(R.layout.fragment_map, container, false);

        initializeMap(root, savedInstanceState);

        return root;
    }

    private void initializeMap(View view, Bundle savedInstanceState) {
        this.mapView = (MapView) view.findViewById(R.id.mapView);
        this.mapView.onCreate(savedInstanceState);
        this.mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        this.mapboxMap.addOnMapLongClickListener(point -> {
            System.out.println("Long Tap");
            // TODO: show modal and store data to database
            return false;
        });

        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/streets-v11"),
                this::getUserLocation);
    }

    @SuppressWarnings({"MissingPermission"})
    private void getUserLocation(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this.requireContext())) {
            LocationManager lm = (LocationManager) this.requireActivity().getSystemService(Context.LOCATION_SERVICE);
            LocationListener locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    showUserNewMarker(new LatLng(location.getLatitude(), location.getLongitude()));
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

    private void showUserNewMarker(LatLng latLng) {
        if (this.userMarker != null) {
            this.mapboxMap.removeMarker(this.userMarker);
        }
        this.mapboxMap.setCameraPosition(new CameraPosition.Builder().target(latLng).zoom(16).build());
        this.userMarker = this.mapboxMap.addMarker(new MarkerOptions().position(latLng).icon(IconFactory.getInstance(requireActivity()).fromResource(R.drawable.black_marker)));
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
            mapboxMap.getStyle(this::getUserLocation);
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
}