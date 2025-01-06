package iiec.ditzdev.pixelify.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.snackbar.Snackbar;
import com.topjohnwu.superuser.Shell;
import iiec.ditzdev.pixelify.databinding.LayoutStartupPageBinding;
import iiec.ditzdev.pixelify.R;
import rikka.shizuku.Shizuku;
import rikka.shizuku.shared.BuildConfig;

public class StartupPage extends AppCompatActivity {
  // Define Request Code For Shizuku
  private static final int REQUEST_CODE_SHIZUKU = 999;
  private static final int REQUEST_CODE_WSS = 99999; // wss: write_secure_settings
  private static final String NAME_PERMISSION = "android.permission.WRITE_SECURE_SETTINGS";
  
  static {
    // Initialize libsu
    Shell.enableVerboseLogging = BuildConfig.DEBUG;
    Shell.setDefaultBuilder(Shell.Builder.create()
        .setFlags(Shell.FLAG_MOUNT_MASTER)
        .setTimeout(10));
  }

  private LayoutStartupPageBinding binding;
  private boolean isRooted = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = LayoutStartupPageBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    binding.btnContinue.setEnabled(false /* Default disabled*/);
    binding.txtDocsShizuku.setMovementMethod(LinkMovementMethod.getInstance());
    binding.txtDocsShizuku.setText(Html.fromHtml(getString(R.string.action_docs_shizuku)));
    
    // Check if device is rooted
    checkRoot();
    
    binding.btnPermShizuku.setOnClickListener(v -> connectToShizuku());
    binding.btnPermRoot.setOnClickListener(v -> requestRootPermission());
    
    if (hasPermission()) {
      binding.btnPermShizuku.setChecked(true);
      binding.btnContinue.setOnClickListener(
          v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
          });
    }
  }

  private void checkRoot() {
    // Check if device is rooted using libsu
    isRooted = Shell.isAppGrantedRoot() == true || Shell.rootAccess();
    binding.btnPermRoot.setEnabled(isRooted);
    
    if (isRooted) {
      // If rooted, show root permission button
      binding.btnPermRoot.setEnabled(true);
    } else {
      // If not rooted, hide root permission button
      binding.btnPermRoot.setEnabled(false);
      binding.btnPermRoot.setSubtitle("Your devices is not Rooted.");     
    }
  }

  private void requestRootPermission() {
    try {
      // Request root permission using libsu
      Shell.su("pm grant " + getPackageName() + " " + NAME_PERMISSION).exec();
      
      if (hasPermission()) {
        binding.btnContinue.setEnabled(true);
        binding.btnPermRoot.setChecked(true);
        Snackbar.make(
            findViewById(android.R.id.content),
            getString(R.string.success_root_permission),
            Snackbar.LENGTH_SHORT)
        .show();
      }
    } catch (Exception e) {
      Log.e("RootPermission", "Failed to get root permission", e);
      Snackbar.make(
          findViewById(android.R.id.content),
          getString(R.string.failed_root_permission),
          Snackbar.LENGTH_SHORT)
      .show();
    }
  }

  private void connectToShizuku() {
    if (Shizuku.pingBinder()) {
      if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
        requestPerm();
      } else {
        // useDefault request Permisson
        Shizuku.requestPermission(REQUEST_CODE_SHIZUKU);
      }
    } else {
      Snackbar.make(
              findViewById(android.R.id.content),
              getString(R.string.no_shizuku_found),
              Snackbar.LENGTH_SHORT)
          .show();
    }
  }

  private void requestPerm() {
    try {
      Shizuku.newProcess(
          new String[] {
            "pm", "grant", getPackageName(), "android.permission.WRITE_SECURE_SETTINGS"
          },
          null,
          null);

      if (hasPermission()) {
        binding.btnContinue.setEnabled(true);
      }
    } catch (Exception e) {
      Log.e("ShizukuPermision", "FAILED_TO_REQUEST_PERM", e);
      Snackbar.make(
              findViewById(android.R.id.content),
              getString(R.string.failed_requestperm_shizuku),
              Snackbar.LENGTH_SHORT)
          .show();
    }
  }

  private boolean hasPermission() {
    try {
      return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SECURE_SETTINGS)
          == PackageManager.PERMISSION_GRANTED;
    } catch (Exception e) {
      return false;
    }
  }

  private final Shizuku.OnRequestPermissionResultListener mReqListener =
      (requestCode, result) -> {
        if (result == PackageManager.PERMISSION_GRANTED) {
          if (requestCode == REQUEST_CODE_SHIZUKU) {
            requestPerm();
          } else if (requestCode == REQUEST_CODE_WSS) {
            if (hasPermission()) {
              binding.btnContinue.setEnabled(true);
            }
          }
        } else {
          Snackbar.make(
                  findViewById(android.R.id.content),
                  getString(R.string.failed_requestperm_shizuku),
                  Snackbar.LENGTH_SHORT)
              .show();
        }
      };

  @Override
  protected void onResume() {
    super.onResume();
    if (hasPermission()) {
      binding.btnContinue.setEnabled(true);
      binding.btnPermShizuku.setChecked(true);
      if (isRooted) {
        binding.btnPermRoot.setChecked(true);
      }
    }
  }

  @Override
  public void onStart() {
    super.onStart();
    Shizuku.addRequestPermissionResultListener(mReqListener);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    this.binding = null;
    Shizuku.removeRequestPermissionResultListener(mReqListener);
  }
}