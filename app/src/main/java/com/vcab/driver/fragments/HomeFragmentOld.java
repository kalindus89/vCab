package com.vcab.driver.fragments;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.vcab.driver.Messages_Common_Class;
import com.vcab.driver.R;
import com.vcab.driver.model.DriverRequestReceived;
import com.vcab.driver.retrofit_remote.IGoogleApiInterface;
import com.vcab.driver.retrofit_remote.RetrofitClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

public class HomeFragmentOld extends Fragment implements OnMapReadyCallback {

    SupportMapFragment mapFragment;
    GoogleMap googleMap;

    //update current locations
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallBack;

    DatabaseReference onlineRef, currentUserRef, driversLocationRef;
    GeoFire geoFire;

    Chip chip_decline;
    CardView layout_accept;
    CircularProgressBar circularProgressBar;
    TextView txt_estimate_time, txt_estimate_distance;

    //For route
    // Disposables,they're useful when e.g. you make a long-running HTTP request
    //CompositeDisposable is just a class to keep all your disposables in the same place to you can dispose all of then at once.
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IGoogleApiInterface iGoogleApiInterface; // for api request through retrofit
    private Polyline blackPolyline, greyPolyline;
    private PolylineOptions grayPolylineOptions, blackPolylineOptions;
    private List<LatLng> polylineList;


    ValueEventListener onlineValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {

            if (snapshot.exists() && currentUserRef != null) {
                //   System.out.println("aaaaaaa ");
                //    currentUserRef.onDisconnect().removeValue(); // delete data when app close . only data added initially
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

        chip_decline = v.findViewById(R.id.chip_decline);
        layout_accept = v.findViewById(R.id.layout_accept);
        circularProgressBar = v.findViewById(R.id.circularProgressBar);
        txt_estimate_time = v.findViewById(R.id.txt_estimate_time);
        txt_estimate_distance = v.findViewById(R.id.txt_estimate_distance);

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
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSelectPlaceEvent(DriverRequestReceived driverRequestReceived) {

        //get Current driver Location

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (fusedLocationProviderClient == null) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                drawPath(driverRequestReceived, location);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Messages_Common_Class.showToastMsg(e.getMessage(), getContext());

            }
        });


    }

    private void drawPath(DriverRequestReceived driverRequestReceived, Location location) {
        iGoogleApiInterface = RetrofitClient.getInstance().create(IGoogleApiInterface.class);
        compositeDisposable
                .add(iGoogleApiInterface.getDirection("driving",
                        "less_driving", new StringBuilder().append(location.getLatitude())
                                .append(",")
                                .append(location.getLongitude())
                                .toString(), driverRequestReceived.getPickupLocation(),

                        getString(R.string.google_map_api_key))
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(returnResults -> {

                            // Log.d("Api_return",returnResults);

                            try {

                                JSONObject jsonObject = new JSONObject(returnResults);
                                JSONArray jsonArray = jsonObject.getJSONArray("routes");

                                for (int i = 0; i < jsonArray.length(); i++) {

                                    JSONObject route = jsonArray.getJSONObject(i);
                                    JSONObject poly = route.getJSONObject("overview_polyline");
                                    String polyline = poly.getString("points");
                                    polylineList = Messages_Common_Class.decodePoly(polyline);

                                }
                                //polyline animation. black to gray

                                //grey polyline animations
                                grayPolylineOptions = new PolylineOptions();
                                grayPolylineOptions.color(Color.GRAY);
                                grayPolylineOptions.width(12);
                                grayPolylineOptions.startCap(new SquareCap());
                                grayPolylineOptions.jointType(JointType.ROUND);
                                grayPolylineOptions.addAll(polylineList);

                                greyPolyline = googleMap.addPolyline(grayPolylineOptions);

                                //black polyline animations
                                blackPolylineOptions = new PolylineOptions();
                                blackPolylineOptions.color(Color.BLACK);
                                blackPolylineOptions.width(5);
                                blackPolylineOptions.startCap(new SquareCap());
                                blackPolylineOptions.jointType(JointType.ROUND);
                                blackPolylineOptions.addAll(polylineList);

                                blackPolyline = googleMap.addPolyline(blackPolylineOptions);

                                // car moving animator
                                ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 100);
                                valueAnimator.setDuration(1100);// car moving time from one location to another
                                valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
                                valueAnimator.setInterpolator(new LinearInterpolator());
                                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator value) {

                                        List<LatLng> points = greyPolyline.getPoints();
                                        int percentValue = (int) value.getAnimatedValue();
                                        int size = points.size();
                                        int newPoints = (int) (size * (percentValue / 100f));
                                        List<LatLng> p = points.subList(0, newPoints);
                                        blackPolyline.setPoints(p);

                                    }
                                });

                                valueAnimator.start();

                                LatLng origin = new LatLng(location.getLatitude(), location.getLongitude()); // driver current location
                                LatLng destination = new LatLng(Double.parseDouble(driverRequestReceived.getPickupLocation().split(",")[0]),
                                        Double.parseDouble(driverRequestReceived.getPickupLocation().split(",")[1])); // customer location


                                LatLngBounds latLngBounds = new LatLngBounds.Builder()
                                        .include(origin)
                                        .include(destination)
                                        .build();

                                //add car icon for origin

                                JSONObject object = jsonArray.getJSONObject(0);
                                JSONArray legs = object.getJSONArray("legs"); // In this array has information about distance, end_location,start_address,start_location and many
                                //https://developers.google.com/maps/documentation/directions/get-directions#DirectionsLeg
                                JSONObject legObject = legs.getJSONObject(0);

                                JSONObject time = legObject.getJSONObject("duration");
                                String duration = time.getString("text");

                                JSONObject distanceEstimate = legObject.getJSONObject("distance");
                                String distance = distanceEstimate.getString("text");

                                txt_estimate_time.setText(duration);
                                txt_estimate_distance.setText(distance);

                                googleMap.addMarker(new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.defaultMarker())
                                        .position(destination)
                                        .title("Pickup Location"));

                                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 160));
                                googleMap.moveCamera(CameraUpdateFactory.zoomTo(googleMap.getCameraPosition().zoom - 1));

                                //show trip accept/decline layout
                                chip_decline.setVisibility(View.VISIBLE);
                                layout_accept.setVisibility(View.VISIBLE);

                                //count down for circular progressBar
                                Observable.interval(100, TimeUnit.MILLISECONDS)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .doOnNext(new Consumer<Long>() {
                                            @Override
                                            public void accept(Long aLong) throws Exception {

                                                circularProgressBar.setProgress(circularProgressBar.getProgress() + 1f);

                                            }
                                        })
                                        .takeUntil(new Predicate<Long>() {
                                            @Override
                                            public boolean test(@NonNull Long aLong) throws Exception {
                                                return aLong == 200; //20 seconds to decline
                                            }
                                        })
                                        .doOnComplete(new Action() {
                                            @Override
                                            public void run() throws Exception {
                                                Messages_Common_Class.showToastMsg("Fake accept action", getActivity());
                                            }
                                        })
                                        .subscribe();


                            } catch (Exception e) {
                                Log.d("aaaaaaaaerr", e.getMessage());
                                // Messages_Common_Class.showToastMsg("Error in web service direction",getActivity());
                            }

                        }));


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

        compositeDisposable.clear();
        if (EventBus.getDefault().hasSubscriberForEvent(DriverRequestReceived.class)) {
            EventBus.getDefault().removeStickyEvent(DriverRequestReceived.class);
            EventBus.getDefault().unregister(this);
        }

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

                Messages_Common_Class.showToastMsg(e.getMessage(), getContext());

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

                    try {
                        addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        String cityName = addressList.get(0).getLocality();

                        //  addressList.get(0).getLocality() city name
                        //addressList.get(0).getSubLocality()
                        // addressList.get(0).getAddressLine(0) address eg Kidelpitiya - Gorokgoda - Kahawala Rd, Sri Lanka
                        //addressList.get(0).getAdminArea() Western Province
                        //addressList.get(0).getSubAdminArea()  Kalutara

                        if (cityName == null) {
                            cityName = addressList.get(0).getAddressLine(0);
                        }
                        //Query
                        driversLocationRef = FirebaseDatabase.getInstance().getReference("DriversLocation").child(cityName); //DriversLocation path
                        currentUserRef = driversLocationRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid());//path inside DriversLocation

                        geoFire = new GeoFire(driversLocationRef);

                        saveDataInFirebaseDatabase();

                    } catch (IOException e) {
                        Messages_Common_Class.showToastMsg(e.getMessage(), getActivity());
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
        locationRequest.setInterval(15000); // location update time
        locationRequest.setFastestInterval(10000); // location update from the other apps in phone
        locationRequest.setSmallestDisplacement(50f); // Set the minimum displacement between location updates in meters

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

                            try {

                                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), // add current driver location to firebase database. path same as currentUserRef. otherwise data not delete when app close
                                        new GeoLocation(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()), new GeoFire.CompletionListener() {
                                            @Override
                                            public void onComplete(String key, DatabaseError error) {

                                                if (error != null) {
                                                    Messages_Common_Class.showToastMsg(error.getMessage(), getActivity());
                                                } else {
                                                    Messages_Common_Class.showToastMsg("You are online", getActivity());
                                                }

                                            }
                                        });

                            } catch (Exception e) {
                                Messages_Common_Class.showToastMsg(e.getMessage(), getActivity());
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

                FirebaseFirestore.getInstance().document("driverLastLocation/" + FirebaseAuth.getInstance().getUid()).update(userLocation).
                        addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                // MessagesClass.showToastMsg("Succes",getContext());

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Messages_Common_Class.showToastMsg("" + e.getMessage(), getContext());

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
                Messages_Common_Class.showToastMsg("style not applied", getActivity());
            }

        } catch (Exception e) {
            Messages_Common_Class.showToastMsg(e.getMessage(), getActivity());
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