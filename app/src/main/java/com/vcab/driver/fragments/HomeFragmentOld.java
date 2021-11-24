package com.vcab.driver.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.vcab.driver.MessagesClass;
import com.vcab.driver.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragmentOld extends Fragment implements OnMapReadyCallback {

    SupportMapFragment mapFragment;
    GoogleMap googleMap;


    //update current locations
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallBack;

    DatabaseReference onlineRef, currentUserRef, driversLocationRef;
    GeoFire geoFire;


    ValueEventListener onlineValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {

            if (snapshot.exists() && currentUserRef!=null) {
                System.out.println("aaaaaaa ");
             //   currentUserRef.onDisconnect().removeValue(); // delete data when app close . only data added initially
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };


    public void stopLocationUpdates() {
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
        }
    }

    public HomeFragmentOld() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home_old, container, false);


        onlineRef = FirebaseDatabase.getInstance().getReference(".info/connected"); //it is useful for your app to know when it is online or offline. which is updated every time the Firebase Realtime Database client's connection state changes

        registerOnlineSystem();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mapFragment == null) {
                    mapFragment = SupportMapFragment.newInstance();
                    showMap();
                }
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                // R.id.map is a layout
                transaction.replace(R.id.google_map, mapFragment).commit();

                showMap();
                updateLastKnowLocations();
            }
        }, 1500);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        registerOnlineSystem();
    }

    private void registerOnlineSystem() {

        onlineRef.addValueEventListener(onlineValueEventListener);

    }

    @Override
    public void onDestroy() {
        stopLocationUpdates();
        geoFire.removeLocation(FirebaseAuth.getInstance().getCurrentUser().getUid());
        onlineRef.removeEventListener(onlineValueEventListener);
        super.onDestroy();

    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopLocationUpdates();
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void showMap() {

        mapFragment.getMapAsync(this);

    }

    private void updateLastKnowLocations() {

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        fusedLocationProviderClient.getLastLocation().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                MessagesClass.showToastMsg(e.getMessage(),getContext());

            }
        }).addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                if (googleMap != null) {
                    //   markOnMap(locationResult.getLastLocation(),16,);
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));

                    Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

                    List<Address> addressList;

                    try{
                        addressList = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                        String cityName=addressList.get(0).getLocality();

                        //Query
                        driversLocationRef = FirebaseDatabase.getInstance().getReference("DriversLocation").child(cityName); //DriversLocation path
                        currentUserRef =driversLocationRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid());//path inside DriversLocation

                        geoFire = new GeoFire(driversLocationRef);

                        saveDataInFirebaseDatabase();

                    }catch (IOException e){
                        MessagesClass.showToastMsg(e.getMessage(),getActivity());
                    }

                    //  saveDataInFirestore(locationResult);
                }

            }
        });

        registerOnlineSystem();





    }

    private void saveDataInFirebaseDatabase() {

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(3000); // location update time
        locationRequest.setFastestInterval(3000); // location update from the other apps in phone
        locationRequest.setSmallestDisplacement(10f); // Set the minimum displacement between location updates in meters

        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null) {
                    return;
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (googleMap != null) {
                            //   markOnMap(locationResult.getLastLocation(),16,);

                            try{

                                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), // add current driver location to firebase database. path same as currentUserRef. otherwise data not delete when app close
                                        new GeoLocation(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()), new GeoFire.CompletionListener() {
                                            @Override
                                            public void onComplete(String key, DatabaseError error) {

                                                if (error!=null){
                                                    MessagesClass.showToastMsg(error.getMessage(),getActivity());
                                                }else {
                                                    MessagesClass.showToastMsg("You are online",getActivity());
                                                }

                                            }
                                        });

                            }catch (Exception e){
                                MessagesClass.showToastMsg(e.getMessage(),getActivity());
                            }

                            //  saveDataInFirestore(locationResult);
                        }


                    }
                });

            }
        };


        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, Looper.myLooper());






    }

    private void saveDataInFirestore(LocationResult locationResult) {


        try {
            if (FirebaseAuth.getInstance().getUid() != null) { // when user close the app, uId() gets null. if its null we stop this service

                Map<String, Object> userLocation = new HashMap<>();
                userLocation.put("geo_point", (new GeoPoint(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude())));

                FirebaseFirestore.getInstance().document("driverLastLocation/"+FirebaseAuth.getInstance().getUid()).update(userLocation).
                        addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                               // MessagesClass.showToastMsg("Succes",getContext());

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        MessagesClass.showToastMsg(""+e.getMessage(),getContext());

                    }
                });
            }
        } catch (NullPointerException e) { // when user close the app, uId() gets null. if its null we stop this service
        }
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        try {

            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.vcab_maps_style));
            if (!success) {
                MessagesClass.showToastMsg("style not applied", getActivity());
            }

        } catch (Exception e) {
            MessagesClass.showToastMsg(e.getMessage(), getActivity());
        }

        googleMap.getUiSettings().setZoomControlsEnabled(true);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.setOnMyLocationButtonClickListener(() -> { // -> lamda mark replaced new inner methods

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

            }
           /* fusedLocationProviderClient.getLastLocation()

                    .addOnFailureListener(e ->
                            MessagesClass.showToastMsg(e.getMessage(), getActivity()))

                    .addOnSuccessListener(location -> {
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                    });*/
            return true;
        });

        //custom view for my location button in google map
        View locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1"))
                .getParent()).findViewById(Integer.parseInt("2"));

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        // position on right bottom
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
        layoutParams.setMargins(0, 10, 10, 0);


        markOnMap(new LatLng(6.8649, 79.8997), 16, "Nugegoda", "Nugegoda");

    }

    public void markOnMap(LatLng latLng, float zoomLevel, String locationName, String snippet) {

        googleMap.clear(); // clear map and remove markers

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