package com.vcab.driver.authentication;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vcab.driver.MainActivity;
import com.vcab.driver.Messages_Common_Class;
import com.vcab.driver.R;
import com.vcab.driver.SessionManagement;
import com.vcab.driver.model.DriverInfoModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserDetailsActivity extends AppCompatActivity {


    EditText email,phone_number,fName;
    ProgressBar progress_bar;
    Button newAccount;
    CircleImageView profile_pic;
    Uri imagePath;
    String imageUriAccessToken;


    ActivityResultLauncher<Intent> getImageResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        imagePath = data.getData();
                        profile_pic.setImageURI(imagePath);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);


        fName= findViewById(R.id.fName);
        phone_number= findViewById(R.id.phone_number);
        email= findViewById(R.id.email);
        progress_bar= findViewById(R.id.progress_bar);
        newAccount= findViewById(R.id.newAccount);

        profile_pic = findViewById(R.id.profile_pic);
        profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                getImageResultLauncher.launch(intent);
            }
        });


        newAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (fName.getText().toString().isEmpty() || phone_number.getText().toString().isEmpty() || phone_number.getText().toString().isEmpty()) {
                    Messages_Common_Class.showToastMsg("Enter all fields",UserDetailsActivity.this);
                }else if (imagePath == null) {
                    Toast.makeText(getApplicationContext(), "Image is Empty", Toast.LENGTH_SHORT).show();
                }
                else {

                    progress_bar.setVisibility(View.VISIBLE);
                    newAccount.setEnabled(false);

                    sendImageToStorage();

                }
            }
        });
    }

    private void sendImageToStorage() {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Images").child("Profile Pic").child("Drivers").child(FirebaseAuth.getInstance().getUid());

        //Image compresesion
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        ///putting image to storage

        UploadTask uploadTask = storageReference.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        imageUriAccessToken = uri.toString();
                        Toast.makeText(getApplicationContext(), "Uri get success", Toast.LENGTH_SHORT).show();
                        sendDataToCloudFirestore();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progress_bar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), "URI get Failed", Toast.LENGTH_SHORT).show();
                    }
                });

                Toast.makeText(getApplicationContext(), "Image is uploaded", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progress_bar.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), "Image Not UpLoaded", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendDataToCloudFirestore() {

        DriverInfoModel user = new DriverInfoModel(fName.getText().toString(),phone_number.getText().toString(),email.getText().toString(),imageUriAccessToken,"update later",0.0);

        Map<String, Object> userLocation = new HashMap<>();
        userLocation.put("geo_point", (new GeoPoint(0.0,0.0)));
        userLocation.put("city", "city");
        userLocation.put("time_date", new Date());

        String fireStorePath="users/drivers/userData/"+FirebaseAuth.getInstance().getUid();
        String fireStorePathToLastLocation="driverLastLocation/"+FirebaseAuth.getInstance().getUid();

        FirebaseFirestore.getInstance().document(fireStorePath).set(user);

        FirebaseFirestore.getInstance().document(fireStorePathToLastLocation).set(userLocation)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        progress_bar.setVisibility(View.INVISIBLE);
                        newAccount.setEnabled(true);

                        new SessionManagement().setFBToken(getApplicationContext(), "update later");

                        startActivity(new Intent(UserDetailsActivity.this, MainActivity.class));
                        finish();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                progress_bar.setVisibility(View.VISIBLE);
                newAccount.setEnabled(false);

                Messages_Common_Class.showToastMsg(e.getMessage(),UserDetailsActivity.this);

            }
        });
    }
}