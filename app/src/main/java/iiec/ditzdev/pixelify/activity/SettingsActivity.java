package iiec.ditzdev.pixelify.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import iiec.ditzdev.pixelify.components.SettingsRadioCheck;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import iiec.ditzdev.pixelify.databinding.ActivitySettingsBinding;
import iiec.ditzdev.pixelify.R;

public class SettingsActivity extends AppCompatActivity {
    
    private ActivitySettingsBinding binding;
    private static final String PREFS_USER = "userPrefs";
    private static final String PREFS_DANGEROUS = "prefs_dangerous";
    private SharedPreferences prefs;
    private boolean isThemeSelected = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });
        prefs = getSharedPreferences(PREFS_USER, MODE_PRIVATE);
        isThemeSelected = prefs.contains("theme_mode");
        boolean isDangerousOn = prefs.getBoolean(PREFS_DANGEROUS, true /* default true? */);
        binding.btnSetDanger.setChecked(isDangerousOn);
        binding.btnSetDanger.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(PREFS_DANGEROUS, !binding.btnSetDanger.isChecked());
                editor.apply();
            binding.btnSetDanger.setChecked(!binding.btnSetDanger.isChecked());
        });
        binding.btnTheme.setOnClickListener(v -> showDialogTheme());
        binding.btnSourceCode.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/DitzDev/pixelify")));
        });
        binding.btnBug.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/DitzDev/pixelify/issues")));
        });
        binding.btnLicense.setOnClickListener(v -> {
            startActivity(new Intent(this, OssLicensesMenuActivity.class));
        });
    }
    
    private void showDialogTheme() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View showView = inflater.inflate(R.layout.custom_settheme_dialog, null);
        
        SettingsRadioCheck setFollowSystem = showView.findViewById(R.id.set_folow_system);
        SettingsRadioCheck setDarkTheme = showView.findViewById(R.id.set_theme_dark);
        SettingsRadioCheck setLightTheme = showView.findViewById(R.id.set_theme_light);
        setFollowSystem.setOnClickListener(v -> {
            setFollowSystem.setChecked(true);
            setDarkTheme.setChecked(false);
            setLightTheme.setChecked(false);
        });
        
        setDarkTheme.setOnClickListener(v -> {
            setFollowSystem.setChecked(false);
            setDarkTheme.setChecked(true);
            setLightTheme.setChecked(false);
        });
        
        setLightTheme.setOnClickListener(v -> {
            setFollowSystem.setChecked(false);
            setDarkTheme.setChecked(false);
            setLightTheme.setChecked(true);
        });
        
        int currentTheme = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        switch (currentTheme) {
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                setFollowSystem.setChecked(true);
                setDarkTheme.setChecked(false);
                setLightTheme.setChecked(false);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                setFollowSystem.setChecked(false);
                setDarkTheme.setChecked(true);
                setLightTheme.setChecked(false);
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
                setFollowSystem.setChecked(false);
                setDarkTheme.setChecked(false);
                setLightTheme.setChecked(true);
                break;
        }
        
        new MaterialAlertDialogBuilder(this)
            .setView(showView)
            .setPositiveButton(getString(R.string.action_string_ok), (dialog, which) -> {
                int selectedTheme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                SharedPreferences.Editor editor = prefs.edit();
                if (setFollowSystem.isChecked()) {
                    selectedTheme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                } else if (setDarkTheme.isChecked()) {
                    selectedTheme = AppCompatDelegate.MODE_NIGHT_YES;
                } else if (setLightTheme.isChecked()) {
                    selectedTheme = AppCompatDelegate.MODE_NIGHT_NO;
                }
                SharedPreferences.Editor editorTheme = prefs.edit();
                editorTheme.putInt("theme_mode", selectedTheme);
                editorTheme.apply();
                AppCompatDelegate.setDefaultNightMode(selectedTheme);
                isThemeSelected = true;
                dialog.dismiss();
                recreate();
            })
            .setNegativeButton(getString(R.string.action_string_cancel), (dialog, which) -> dialog.dismiss())
            .show();
    }
}
