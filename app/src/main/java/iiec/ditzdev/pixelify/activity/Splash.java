package iiec.ditzdev.pixelify.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import iiec.ditzdev.pixelify.databinding.LayoutSplashBinding;
import iiec.ditzdev.pixelify.R;

public class Splash extends AppCompatActivity {

    private LayoutSplashBinding binding;
    private static final String PREFS_NAME = "PixelifyPrefs";
    private static final String FIRST_TIME_LAUNCH = "first_time_launch";
    private static final String TAG = "Splash";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = LayoutSplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean ifFirstTime = prefs.getBoolean(FIRST_TIME_LAUNCH, true /* default true */);
        if (ifFirstTime) {
            showWarningDialog();
        } else {
            ifHasPermission();
        }
    }

    private void showWarningDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.title_warning_dialog))
                .setMessage(getString(R.string.subtitle_warningfirst_dialog))
                .setCancelable(false)
                .setPositiveButton(
                        getString(R.string.action_understand),
                        (dialog, which) -> {
                            SharedPreferences prefs =
                                    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                            prefs.edit().putBoolean(FIRST_TIME_LAUNCH, false).apply();
                            ifHasPermission(); 
                            dialog.dismiss();
                        })
                .setNegativeButton(
                        getString(R.string.action_reject),
                        (dialog, which) -> {
                            finishAffinity();
                            dialog.dismiss();
                        })
                .show();
    }
     
    private void ifHasPermission() {
        if(hasPermission()) {
            navigateToMain();
        } else {
            navigateToStartup();
        }
    }
    
    /* Method for Checking Permission */
    private boolean hasPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
    }
    
    private void navigateToMain() {
        new Handler().postDelayed(() -> { startActivity(new Intent(this, MainActivity.class)); finish(); }, 3000);
    }
    
    private void navigateToStartup() {
        new Handler().postDelayed(() -> { startActivity(new Intent(this, StartupPage.class)); finish(); }, 3000);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.binding = null;
    }
}