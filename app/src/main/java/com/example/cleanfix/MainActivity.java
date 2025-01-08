package com.example.cleanfix;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.cleanfix.databinding.ActivityMainBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();

        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            apiAvailability.getErrorDialog(this, resultCode, 0).show();
        } else {
            Log.i("AuthSSO", "apiAvailability SUCCESS");
        }
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        Log.i("AuthSSO", "FirebaseAuth" + mAuth);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestProfile()
                .requestEmail()
                .build();
        Log.i("AuthSSO", "default_web_client_id " + getString(R.string.default_web_client_id));
        Log.i("AuthSSO", "gso " + gso);

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Log.i("AuthSSO", "GoogleSignIn" + mGoogleSignInClient);

        // Set up Google Sign-In button
        Button signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(view -> signIn());
        Log.i("AuthSSO", "GoogleSignIn" + signInButton);

        // Check if the user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Log.w("AuthSSO", "currentUser" + currentUser);

        if (currentUser != null) {
            // User is signed in, navigate to next activity
            setContentView(R.layout.fragment_new);
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        Log.i("AuthSSO", "signIn " + signInIntent);
        startActivityForResult(signInIntent, RC_SIGN_IN);
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
                Toast.makeText(MainActivity.this, "Google Sign-In failed. Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
                        moveToNavigationFragment();
                    } else {
                        // If sign in fails, display a message to the user
                        Log.i("MainActivity", "signInWithCredential:failure", task.getException());
                        Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void moveToNavigationFragment() {
        // Set the navigation layout
        setContentView(R.layout.fragment_navigation);

        // Set up bottom navigation
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(navView, navController);
    }
}
