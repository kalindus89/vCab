package com.vcab.driver.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.vcab.driver.MessagesClass;
import com.vcab.driver.R;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    MapFragment mapFragment;
    GoogleMap googleMap;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_home, container, false);

        mapFragment = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.google_map);


        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showMap();
            }
        });
        return v;

    }

    private void showMap() {

        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        try{

            boolean success= googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(),R.raw.vcab_maps_style));
            if(!success){
                MessagesClass.showToastMsg("style not applied",getActivity());
            }

        }catch (Exception e){
            MessagesClass.showToastMsg(e.getMessage(),getActivity());
        }

        googleMap.getUiSettings().setZoomControlsEnabled(true);

        markOnMap(6.8649,79.8997,16,"Nugegoda","Nugegoda");
    }

    public void markOnMap(double latitude, double longitude, float zoomLevel, String locationName, String snippet) {

        googleMap.clear(); // clear map and remove markers

        LatLng latLng = new LatLng(latitude, longitude);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title(locationName);
        markerOptions.position(latLng);
        markerOptions.snippet(snippet); //place info's
        // googleMap.addMarker(markerOptions).remove();
        googleMap.addMarker(markerOptions);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel);  //max zoom 21. 1world, 5Continents, 10Cities, 15Streets, 20Buildings
        //googleMap.moveCamera(cameraUpdate); //directly show
        // googleMap.animateCamera(cameraUpdate); // moving to position without time

        googleMap.animateCamera(CameraUpdateFactory.zoomTo(2.0f));
        googleMap.animateCamera(cameraUpdate, 3000, new GoogleMap.CancelableCallback() { // moving to position with time
            @Override
            public void onFinish() {

            }

            @Override
            public void onCancel() {

            }
        });
    }
}