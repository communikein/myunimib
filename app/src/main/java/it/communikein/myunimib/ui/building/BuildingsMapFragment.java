package it.communikein.myunimib.ui.building;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import it.communikein.myunimib.R;
import it.communikein.myunimib.data.model.Building;
import it.communikein.myunimib.databinding.FragmentBuildingsMapBinding;
import it.communikein.myunimib.viewmodel.BuildingsViewModel;


public class BuildingsMapFragment extends Fragment implements OnMapReadyCallback {

    public static final String LOG_TAG = BuildingsMapFragment.class.getSimpleName();

    private FragmentBuildingsMapBinding mBinding;

    /* Might be null if Google Play services APK is not available. */
    private GoogleMap mMap = null;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final LatLng milan = new LatLng(45.6983, 9.6773);


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /* Inflate the layout for this fragment */
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_buildings_map, container, false);

        initMap(savedInstanceState);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getParentViewModel() != null) {
            updateMap(getParentViewModel().getBuildings());

            getParentViewModel().getSelectedBuilding().observe(this, building -> {
                if (building != null) {
                    moveCamera(building.getLatitude(), building.getLongitude(), 6f);
                }
            });
        }
    }

    private BuildingsViewModel getParentViewModel() {
        if (getParentFragment() != null)
            return ((BuildingsFragment) getParentFragment()).getViewModel();
        else
            return null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;

        if (getParentViewModel() != null) {
            updateMap(getParentViewModel().getBuildings());

            if (getParentViewModel().getSelectedBuilding().getValue() != null) {
                Building b = getParentViewModel().getSelectedBuilding().getValue();
                moveCamera(b.getLatitude(), b.getLongitude(), 6f);
            }
        }
    }

    private void initMap(Bundle savedInstanceState) {
        /*
         * *** IMPORTANT ***
         * MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
         * objects or sub-Bundles.
         */
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mBinding.map.onCreate(mapViewBundle);
        mBinding.map.getMapAsync(this);
    }

    private void updateMap(List<Building> buildings) {
        if (mMap != null) {
            for (Building building : buildings) {
                LatLng coords = new LatLng(building.getLatitude(), building.getLongitude());

                mMap.addMarker(new MarkerOptions().position(coords));
            }

            moveCamera(milan.latitude, milan.longitude, 5f);
        }
    }

    private void moveCamera(double lat, double lng, float zoom) {
        if (mMap != null) {
            CameraPosition.Builder builder = new CameraPosition.Builder();
            builder.target(new LatLng(lat, lng));
            builder.zoom(zoom);

            CameraUpdate update = CameraUpdateFactory.newCameraPosition(builder.build());
            mMap.moveCamera(update);
        }
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mBinding.map.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        mBinding.map.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        mBinding.map.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBinding.map.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mBinding.map.onLowMemory();
    }

}
