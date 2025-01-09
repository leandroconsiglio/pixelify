package iiec.ditzdev.pixelify.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import iiec.ditzdev.pixelify.components.BottomSheetDialogBuilder;
import iiec.ditzdev.pixelify.databinding.ActivityMainBinding;
import iiec.ditzdev.pixelify.R;
import iiec.ditzdev.pixelify.models.Resolution;
import iiec.ditzdev.pixelify.others.ResolutionBottomSheetFragment;
import iiec.ditzdev.pixelify.utils.NodeRESOUtils;
import iiec.ditzdev.pixelify.utils.ResolutionUtils;
import iiec.ditzdev.pixelify.utils.WM;
import java.util.ArrayList;
import java.util.List;
import iiec.ditzdev.pixelify.models.ResolutionTemplate;
import rikka.shizuku.Shizuku;

public class MainActivity extends AppCompatActivity {

    private WM wm;
    private Resolution defaultReso;
    private NodeRESOUtils utils;
    private ActivityMainBinding binding;
    private SharedPreferences prefs;
    private static final String PREFS_USER = "userPrefs";
    private static final String PREFS_DANGEROUS = "prefs_dangerous";
    private static final int SHIZUKU_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.txtDocsPixelify.setMovementMethod(LinkMovementMethod.getInstance());
        binding.txtDocsPixelify.setText(Html.fromHtml(getString(R.string.docs_greeting_main)));
        binding.btnSettings.setOnClickListener(
                v -> {
                    startActivity(new Intent(this, SettingsActivity.class));
                });
        if (!checkShizukuService()) {
            showShizukuWarningDialog();
            return;
        }
        try {
            wm = new WM(getContentResolver());
            utils = new NodeRESOUtils(wm);
            defaultReso = utils.getRealResolution();
        } catch (Exception e) {
            showSnackbar("Cannot initialize module resolution changer" + e.getMessage());
        }
        try {
            binding.dpiInput.setText(String.valueOf(wm.getRealDensity()));
            binding.widthInput.setText(String.valueOf(defaultReso.getWidth()));
            binding.heightInput.setText(String.valueOf(defaultReso.getHeight()));
        } catch (Exception e) {
            showErrorDialog(
                    getString(R.string.string_cannot_get_default) + "\n" + e.getMessage(),
                    e.toString());
        }
        binding.btnReset.setOnClickListener(v -> resetToDefault());
        binding.btnTemplate.setOnClickListener(v -> showResolutionTemplate());
        prefs = getSharedPreferences(PREFS_USER, MODE_PRIVATE);
        setupListeners();
    }

    private void setupListeners() {
        binding.btnSave.setOnClickListener(
                v -> {
                    boolean isPrefOn = prefs.getBoolean(PREFS_DANGEROUS, true);
                    int newWidth = Integer.parseInt(binding.widthInput.getText().toString());
                    int newHeight = Integer.parseInt(binding.heightInput.getText().toString());
                    int newDpi = Integer.parseInt(binding.dpiInput.getText().toString());
                    if (isDangerousReso(newWidth, newHeight)) {
                        if (isPrefOn) {
                            showWarningDialog(newWidth, newHeight, newDpi);
                        } else {
                            applyResolution(newWidth, newHeight, newDpi);
                        }
                    } else {
                        applyResolution(newWidth, newHeight, newDpi);
                    }
                });
    }

    private void applyResolution(int width, int height, int dpi) {
        try {
            wm.setResolution(width, height);
            wm.setDisplayDensity(dpi);
            showSnackbar(getString(R.string.string_succes_change));
        } catch (Exception e) {
            showErrorDialog(
                    getString(R.string.string_cannot_set_reso) + "\n" + e.getMessage(),
                    e.toString());
        }
    }

    private void resetToDefault() {
        new BottomSheetDialogBuilder(this)
                .setIcon(getDrawable(R.drawable.icon_warning))
                .setMessage(getString(R.string.question_reset))
                .setButtonPositive(
                        getString(R.string.action_continue),
                        v -> {
                            try {
                                wm.clearResolution();
                                wm.clearDisplayDensity();
                                binding.widthInput.setText(String.valueOf(defaultReso.getWidth()));
                                binding.heightInput.setText(
                                        String.valueOf(defaultReso.getHeight()));
                                binding.dpiInput.setText(String.valueOf(wm.getRealDensity()));
                            } catch (Exception e) {
                                showErrorDialog(
                                        getString(R.string.string_cannot_get_default)
                                                + "\n"
                                                + e.getMessage(),
                                        e.toString());
                            }
                           v.dismiss();
                        })
                .setButtonNegative(getString(R.string.action_string_cancel), v -> {
                    v.dismiss();
                })
                .show();
    }

    private void showSnackbar(String teks) {
        Snackbar.make(findViewById(android.R.id.content), teks, Snackbar.LENGTH_SHORT).show();
    }

    private void showWarningDialog(int width, int height, int dpi) {
        new BottomSheetDialogBuilder(this)
                .setIcon(getDrawable(R.drawable.icon_warning))
                .setMessage(
                        getString(
                                R.string.string_dialog_warning,
                                String.valueOf(defaultReso.getWidth()),
                                String.valueOf(defaultReso.getHeight()),
                                String.valueOf(width),
                                String.valueOf(height),
                                String.valueOf(dpi)))
                .setButtonPositive(
                        getString(R.string.action_continue),
                        v -> {
                            applyResolution(width, height, dpi);
                            v.dismiss();
                        })
                .setButtonNegative(
                        getString(R.string.action_string_cancel),
                        v -> {
                            v.dismiss();
                        })
                .show();
    }

    private void showErrorDialog(String teks, String error) {
        new BottomSheetDialogBuilder(this)
                .setIcon(getDrawable(R.drawable.icon_error))
                .setMessage(teks)
                .setButtonPositive(
                        getString(R.string.action_string_ok),
                        v -> {
                            v.dismiss();
                        })
                .setButtonNegative(
                        getString(R.string.string_copy_log),
                        v -> {
                            ClipboardManager manager =
                                    (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData data = ClipData.newPlainText("Error: ", error);
                            manager.setPrimaryClip(data);
                            showSnackbar(getString(R.string.string_copy_message));
                            v.dismiss();
                        })
                .show();
    }

    private boolean isDangerousReso(int width, int height) {
        return Math.abs(width - defaultReso.getWidth()) > defaultReso.getWidth() * 0.5
                || Math.abs(height - defaultReso.getHeight()) > defaultReso.getHeight() * 0.5;
    }
    
    private void showResolutionTemplate() {
        List<ResolutionTemplate> templates = createResolutionTemplates();
        templates.stream().map(ResolutionTemplate::getDeviceName).toArray(String[]::new);
        ResolutionBottomSheetFragment bottomSheet = ResolutionBottomSheetFragment.newInstance(templates);
        bottomSheet.setOnResolutionSelectedListener(template -> {
            Snackbar.make(findViewById(android.R.id.content), "Selected: " + template.getDeviceName(), Snackbar.LENGTH_SHORT).show();
            binding.widthInput.setText(String.valueOf(template.getLogicalWidth()));
            binding.heightInput.setText(String.valueOf(template.getLogicalHeight()));
            binding.dpiInput.setText(String.valueOf(template.getPpi()));         
        });
        bottomSheet.show(getSupportFragmentManager(), "ResolutionBottomSheet");
    }

    private List<ResolutionTemplate> createResolutionTemplates() {
        List<ResolutionTemplate> templates = new ArrayList<>();
        templates.add(new ResolutionTemplate("iPad View", 1536, 2080, 384, 2.0f, "8.3\""));
        templates.add(new ResolutionTemplate("iPad (2021)", 1620, 2160, 264, 2.0f, "10.2\""));
        templates.add(new ResolutionTemplate("iPhone 13 Pro max (2021)", 1284, 2778, 340, 3.0f, "6.68\""));
        return templates;
    }
    
    private boolean checkShizukuService() {
        try {
            return Shizuku.pingBinder();
        } catch (Exception e) {
            return false;
        }
    }
    private void showShizukuWarningDialog() {
        new BottomSheetDialogBuilder(this)
                .setIcon(getDrawable(R.drawable.icon_warning))
                .setMessage(getString(R.string.shizuk_dialog_warning))
                .setButtonPositive(
                        getString(R.string.action_string_ok),
                        v -> {
                            v.dismiss();
                            finish();
                        })
                .setCancelable(false)
                .show();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (!checkShizukuService()) {
            showShizukuWarningDialog();
            return;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.binding = null;
    }
}