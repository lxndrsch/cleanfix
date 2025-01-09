package com.example.cleanfix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SSOActivity extends AppCompatActivity {
    private SharedPreferences preferences;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    boolean loginSuccessful;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("AuthSSO", "onCreate");
        setContentView(R.layout.activity_sso);
        preferences = getSharedPreferences("app_prefs", MODE_PRIVATE);

        findViewById(R.id.sign_in_button).setOnClickListener(v -> {
            // Initialize Firebase Auth
            mAuth = FirebaseAuth.getInstance();

            // Configure Google Sign-In
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestProfile()
                .requestEmail()
                .build();

            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

            // Set up Google Sign-In button
            Button signInButton = findViewById(R.id.sign_in_button);
            signInButton.setOnClickListener(view -> signIn());
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.w("AuthSSO", "requestCode " + requestCode + "resultCode " + resultCode + "data " + data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Attempt to get the GoogleSignInAccount
                GoogleSignInAccount account = task.getResult(ApiException.class);
                // If successful, proceed with Firebase authentication or your app logic
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Handle failure
                Log.i("AuthSSO", "Google sign-in failed with code: " + e.getStatusCode());
                Log.i("AuthSSO", "Error Message: " + e.getMessage());
            }
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Save login state
                        preferences.edit().putBoolean("is_authenticated", true).apply();

                        // Navigate to MainActivity
                        startActivity(new Intent(this, MainActivity.class));
                        finish(); // Close SSOActivity
                    } else {
                        // If sign in fails, display a message to the user
                        Log.i("MainActivity", "signInWithCredential:failure", task.getException());
                    }
                });
    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        Log.i("AuthSSO", "signIn " + signInIntent);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
}