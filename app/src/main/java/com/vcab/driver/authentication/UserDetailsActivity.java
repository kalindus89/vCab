package com.vcab.driver.authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vcab.driver.MainActivity;
import com.vcab.driver.MessagesClass;
import com.vcab.driver.R;
import com.vcab.driver.SplashScreenActivity;
import com.vcab.driver.model.User;

import java.util.Arrays;

public class UserDetailsActivity extends AppCompatActivity {


    EditText email,phone_number,fName;
    ProgressBar progress_bar;
    Button newAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);


        fName= findViewById(R.id.fName);
        phone_number= findViewById(R.id.phone_number);
        email= findViewById(R.id.email);
        progress_bar= findViewById(R.id.progress_bar);
        newAccount= findViewById(R.id.newAccount);

        newAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (fName.getText().toString().isEmpty() || phone_number.getText().toString().isEmpty() || phone_number.getText().toString().isEmpty()) {
                    MessagesClass.showToastMsg("Enter all fields",UserDetailsActivity.this);
                }
                else {

                    progress_bar.setVisibility(View.VISIBLE);
                    newAccount.setEnabled(false);

                    User user = new User(fName.getText().toString(),phone_number.getText().toString(),email.getText().toString());
                    FirebaseFirestore.getInstance().collection("users").document("drivers")
                            .collection("userData").document(FirebaseAuth.getInstance().getUid()).set(user)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            progress_bar.setVisibility(View.INVISIBLE);
                            newAccount.setEnabled(true);

                            startActivity(new Intent(UserDetailsActivity.this, MainActivity.class));
                            finish();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            progress_bar.setVisibility(View.VISIBLE);
                            newAccount.setEnabled(false);

                            MessagesClass.showToastMsg(e.getMessage(),UserDetailsActivity.this);

                        }
                    });


                }



            }
        });
    }
}