package com.vcab.driver.authentication;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vcab.driver.MainActivity;
import com.vcab.driver.MessagesClass;
import com.vcab.driver.R;

import java.util.Arrays;
import java.util.List;

public class UserAuthenticationActivity extends AppCompatActivity {

    private final static int LOGIN_REQUEST_CODE=7171; // any Number
    private List<AuthUI.IdpConfig> provider;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_user_authentication);

        init();
    }
    private void init(){

        provider= Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build()
                ,new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        firebaseAuth = FirebaseAuth.getInstance();


/*        listener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();

            }
        };*/

        //same as above (-> lamda expression)
        listener = myFirebaseAuth->{

            FirebaseUser user =myFirebaseAuth.getCurrentUser();
            if (user !=null){

                startActivity(new Intent(UserAuthenticationActivity.this,MainActivity.class));
                finish();

            }
            else {
                showLoginLayout();
            }
        };

    }
    private void showLoginLayout() {
        AuthMethodPickerLayout authMethodPickerLayout= new AuthMethodPickerLayout.Builder(R.layout.activity_user_authentication)
                .setPhoneButtonId(R.id.loginWithPhone)
                .setGoogleButtonId(R.id.loginWithGoogle)
                .build();

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAuthMethodPickerLayout(authMethodPickerLayout)
                .setAvailableProviders(provider)
                .setIsSmartLockEnabled(false)
                .setTheme(R.style.LoginTheme)
                .build();

        signInLauncher.launch(signInIntent);
    }

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {

                    IdpResponse response = result.getIdpResponse();

                    if(result.getResultCode()==RESULT_OK){

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    }else {
                        MessagesClass.showToastMsg("Failed to sign in: "+response.getError().getMessage(),UserAuthenticationActivity.this);
                    }


                }
            }
    );

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        if(firebaseAuth!=null && listener!=null) {
            firebaseAuth.removeAuthStateListener(listener);
        }
        super.onStop();

    }
}