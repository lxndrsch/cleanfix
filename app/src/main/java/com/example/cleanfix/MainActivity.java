package com.example.cleanfix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.cleanfix.databinding.ActivityMainBinding;
import com.example.cleanfix.ui.NewFragment;
import com.example.cleanfix.ui.SocialFragment;
import com.example.cleanfix.ui.StatusFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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
    private SharedPreferences preferences;
    private final int CAMERA_REQUEST_CODE = 4711;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is authenticated
        SharedPreferences preferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean isAuthenticated = preferences.getBoolean("is_authenticated", false);

        if (!isAuthenticated) {
            // Redirect to SSOActivity
            startActivity(new Intent(this, SSOActivity.class));
            finish();
            return; // Prevent further execution
        }

        // User is authenticated; load main UI
        setContentView(R.layout.activity_main);

        // Camera permissions
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted"); }
        else{
            requestPermissions(new String[]
                    {android.Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
            Log.d(TAG, "Camera permission requested"); }

        // Load the default fragment (e.g., HomeFragment)
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new NewFragment())
                    .commit();
        }

        // Set up Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.navigation_new) {
                selectedFragment = new NewFragment();
            } else if (item.getItemId() == R.id.navigation_status) {
                selectedFragment = new StatusFragment();
            } else if (item.getItemId() == R.id.navigation_social) {
                selectedFragment = new SocialFragment();
            }

            // Replace the fragment
            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, selectedFragment)
                        .commit();
            }

            return true;
        });

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
