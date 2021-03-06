package com.vcab.driver;

import android.content.Context;
import android.content.ContextWrapper;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vcab.driver.firebase_notification.FCMResponse;
import com.vcab.driver.firebase_notification.FCMSendData;
import com.vcab.driver.firebase_notification.IFCMService;
import com.vcab.driver.firebase_notification.RetrofitFCMClient;
import com.vcab.driver.model.DriverInfoModel;
import com.vcab.driver.model.NotifyToCustomerEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class Messages_Common_Class {


    public static DriverInfoModel driverInfo;

    public static void showToastMsg(String msg, Context context) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showSnackBar(String msg, View view) {

        Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show();
    }

    //DECODE POLY--- gives the direction array
    public static List<LatLng> decodePoly(String encoded) {
        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;

            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    public static void sendDeclineRequest(View view, Context context, String customerUid) {

        CompositeDisposable compositeDisposable = new CompositeDisposable();
        IFCMService ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);

        //get Customer Token to send notification
        DocumentReference nycRef = FirebaseFirestore.getInstance().document("users/customers/userData/" + customerUid);

        nycRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        String driverToken = document.get("firebaseToken").toString();

                        Map<String, String> notificationData = new HashMap<>();
                        notificationData.put("title", "Decline");
                        notificationData.put("body", "This message represent for action driver DECLINE");
                        notificationData.put("driverUid", FirebaseAuth.getInstance().getCurrentUser().getUid());

                        FCMSendData fcmSendData = new FCMSendData(driverToken, notificationData);

                        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<FCMResponse>() {
                                    @Override
                                    public void accept(FCMResponse fcmResponse) throws Exception {

                                        if (fcmResponse.getSuccess() == 0) {

                                            compositeDisposable.clear();
                                            showSnackBar("Failed to send request to customer", view);

                                        } else {
                                            showSnackBar("Request Decline Success!", view);

                                        }

                                    }
                                }, new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
                                        compositeDisposable.clear();
                                        showSnackBar(throwable.getMessage(), view);
                                    }
                                }));


                    } else {
                    }
                } else {
                    Messages_Common_Class.showSnackBar("Try again. No customer token found", view);
                }
            }
        });

    }

    public static void sendAcceptRequestToCustomer(View view, Context context, String customerUid, String ticketID) {

        CompositeDisposable compositeDisposable = new CompositeDisposable();
        IFCMService ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);

        //get Customer Token to send notification
        DocumentReference nycRef = FirebaseFirestore.getInstance().document("users/customers/userData/" + customerUid);

        nycRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        String driverToken = document.get("firebaseToken").toString();

                        Map<String, String> notificationData = new HashMap<>();
                        notificationData.put("title", "Accept");
                        notificationData.put("body", "This message represent for action driver ACCEPT");
                        notificationData.put("driverUid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        notificationData.put("tripId", ticketID);

                        FCMSendData fcmSendData = new FCMSendData(driverToken, notificationData);

                        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<FCMResponse>() {
                                    @Override
                                    public void accept(FCMResponse fcmResponse) throws Exception {

                                        if (fcmResponse.getSuccess() == 0) {

                                            compositeDisposable.clear();
                                            showSnackBar("Failed to send request to customer", view);

                                        } else {
                                            showSnackBar("Request Accept Success!", view);

                                        }

                                    }
                                }, new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
                                        compositeDisposable.clear();
                                        showSnackBar(throwable.getMessage(), view);
                                    }
                                }));


                    } else {
                    }
                } else {
                    compositeDisposable.clear();
                    Messages_Common_Class.showSnackBar("Try again. No customer token found", view);
                }
            }
        });
    }

    public static void sendNotifyToCustomer(Context context, View view, String customerUid) {


        CompositeDisposable compositeDisposable = new CompositeDisposable();
        IFCMService ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);

        //get Customer Token to send notification
        DocumentReference nycRef = FirebaseFirestore.getInstance().document("users/customers/userData/" + customerUid);

        nycRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        String driverToken = document.get("firebaseToken").toString();

                        Map<String, String> notificationData = new HashMap<>();
                        notificationData.put("title", "Driver arrived");
                        notificationData.put("body", "Your driver has arrived!");
                        notificationData.put("driverUid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        notificationData.put("customerUid", customerUid);

                        showSnackBar(driverToken,view);

                        FCMSendData fcmSendData = new FCMSendData(driverToken, notificationData);

                        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<FCMResponse>() {
                                    @Override
                                    public void accept(FCMResponse fcmResponse) throws Exception {

                                        if (fcmResponse.getSuccess() == 0) {

                                            compositeDisposable.clear();
                                            showSnackBar("Failed to send request to customer", view);

                                        } else {
                                            EventBus.getDefault().postSticky(new NotifyToCustomerEvent());
                                            showSnackBar("Request Accept Success!", view);

                                        }

                                    }
                                }, new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
                                        compositeDisposable.clear();
                                        showSnackBar(throwable.getMessage(), view);
                                    }
                                }));


                    } else {
                    }
                } else {
                    compositeDisposable.clear();
                    Messages_Common_Class.showSnackBar("Try again. No customer token found", view);
                }
            }
        });


    }

    public static void sendDeclineAndRemoveTripRequest(View view, Context context, String customerUid) {

        CompositeDisposable compositeDisposable = new CompositeDisposable();
        IFCMService ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);


        FirebaseDatabase.getInstance().getReference("Trips")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showSnackBar(e.getMessage(), view);
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //get Customer Token to send notification
                DocumentReference nycRef = FirebaseFirestore.getInstance().document("users/customers/userData/" + customerUid);

                nycRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {

                                String driverToken = document.get("firebaseToken").toString();

                                Map<String, String> notificationData = new HashMap<>();
                                notificationData.put("title", "DeclineAndRemoveTrip");
                                notificationData.put("body", "Drive has decline your trip request");
                                notificationData.put("driverUid", FirebaseAuth.getInstance().getCurrentUser().getUid());

                                FCMSendData fcmSendData = new FCMSendData(driverToken, notificationData);

                                compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                                        .subscribeOn(Schedulers.newThread())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Consumer<FCMResponse>() {
                                            @Override
                                            public void accept(FCMResponse fcmResponse) throws Exception {

                                                if (fcmResponse.getSuccess() == 0) {

                                                    compositeDisposable.clear();
                                                    showSnackBar("Failed to send request to customer", view);

                                                } else {
                                                    showSnackBar("Decline Success!", view);

                                                }

                                            }
                                        }, new Consumer<Throwable>() {
                                            @Override
                                            public void accept(Throwable throwable) throws Exception {
                                                compositeDisposable.clear();
                                                showSnackBar(throwable.getMessage(), view);
                                            }
                                        }));


                            } else {
                            }
                        } else {
                            compositeDisposable.clear();
                            Messages_Common_Class.showSnackBar("Try again. No customer token found", view);
                        }
                    }
                });
            }
        });


    }

    public static void sendCompleteTripToCustomer(View view, String customerUid, String tripId) {

        CompositeDisposable compositeDisposable = new CompositeDisposable();
        IFCMService ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);


        FirebaseDatabase.getInstance().getReference("Trips")
                .child(tripId).removeValue().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showSnackBar(e.getMessage(), view);
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                //get Customer Token to send notification
                DocumentReference nycRef = FirebaseFirestore.getInstance().document("users/customers/userData/" + customerUid);

                nycRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {

                                String driverToken = document.get("firebaseToken").toString();

                                Map<String, String> notificationData = new HashMap<>();
                                notificationData.put("title", "DriverCompleteTrip");
                                notificationData.put("body", "Drive has completed your trip");
                                notificationData.put("driverUid", FirebaseAuth.getInstance().getCurrentUser().getUid());

                                FCMSendData fcmSendData = new FCMSendData(driverToken, notificationData);

                                compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                                        .subscribeOn(Schedulers.newThread())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Consumer<FCMResponse>() {
                                            @Override
                                            public void accept(FCMResponse fcmResponse) throws Exception {

                                                if (fcmResponse.getSuccess() == 0) {

                                                    compositeDisposable.clear();
                                                    showSnackBar("Failed to send request to customer", view);

                                                } else {
                                                    showSnackBar("You have completed the trip", view);

                                                }

                                            }
                                        }, new Consumer<Throwable>() {
                                            @Override
                                            public void accept(Throwable throwable) throws Exception {
                                                compositeDisposable.clear();
                                                showSnackBar(throwable.getMessage(), view);
                                            }
                                        }));


                            } else {
                            }
                        } else {
                            compositeDisposable.clear();
                            Messages_Common_Class.showSnackBar("Try again. No customer token found", view);
                        }
                    }
                });
            }
        });


    }
}
