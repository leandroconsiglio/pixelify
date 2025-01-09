package iiec.ditzdev.pixelify.components;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import iiec.ditzdev.pixelify.R;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

public class BottomSheetDialogBuilder {
    private Context context;
    private BottomSheetDialog dialog;
    private String message;
    private Drawable icon;
    private String positiveButtonText;
    private String negativeButtonText;
    private String neutralButtonText;
    private DialogClickListener positiveButtonListener;
    private DialogClickListener negativeButtonListener;
    private DialogClickListener neutralButtonListener;
    private boolean isCancelable = true;

    public interface DialogClickListener {
        void onClick(BottomSheetDialog dialog);
    }

    public BottomSheetDialogBuilder(@NonNull Context context) {
        this.context = context;
        this.dialog = new BottomSheetDialog(context);
    }

    public BottomSheetDialogBuilder setIcon(@Nullable Drawable icon) {
        this.icon = icon;
        return this;
    }

    public BottomSheetDialogBuilder setMessage(@Nullable String message) {
        this.message = message;
        return this;
    }

    public BottomSheetDialogBuilder setCancelable(boolean cancelable) {
        this.isCancelable = cancelable;
        return this;
    }

    public BottomSheetDialogBuilder setButtonPositive(@Nullable String text, @Nullable DialogClickListener listener) {
        this.positiveButtonText = text;
        this.positiveButtonListener = listener;
        return this;
    }

    public BottomSheetDialogBuilder setButtonNegative(@Nullable String text, @Nullable DialogClickListener listener) {
        this.negativeButtonText = text;
        this.negativeButtonListener = listener;
        return this;
    }

    public BottomSheetDialogBuilder setButtonNeutral(@Nullable String text, @Nullable DialogClickListener listener) {
        this.neutralButtonText = text;
        this.neutralButtonListener = listener;
        return this;
    }

    public BottomSheetDialog show() {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_dialog, null);
        ImageView iconView = dialogView.findViewById(R.id.dialog_icon);
        if (icon != null) {
            iconView.setImageDrawable(icon);
            iconView.setVisibility(View.VISIBLE);
        } else {
            iconView.setVisibility(View.GONE);
        }

        TextView messageView = dialogView.findViewById(R.id.dialog_message);
        if (message != null) {
            messageView.setText(message);
            messageView.setVisibility(View.VISIBLE);
        } else {
            messageView.setVisibility(View.GONE);
        }
        
        MaterialButton positiveButton = dialogView.findViewById(R.id.positive_button);
        if (positiveButtonText != null) {
            positiveButton.setText(positiveButtonText);
            positiveButton.setOnClickListener(v -> {
                if (positiveButtonListener != null) {
                    positiveButtonListener.onClick(dialog);
                } else {
                    dialog.dismiss();
                }
            });
            positiveButton.setVisibility(View.VISIBLE);
        } else {
            positiveButton.setVisibility(View.GONE);
        }

        MaterialButton negativeButton = dialogView.findViewById(R.id.negative_button);
        if (negativeButtonText != null) {
            negativeButton.setText(negativeButtonText);
            negativeButton.setOnClickListener(v -> {
                if (negativeButtonListener != null) {
                    negativeButtonListener.onClick(dialog);
                } else {
                    dialog.dismiss();
                }
            });
            negativeButton.setVisibility(View.VISIBLE);
        } else {
            negativeButton.setVisibility(View.GONE);
        }

        MaterialButton neutralButton = dialogView.findViewById(R.id.neutral_button);
        if (neutralButtonText != null) {
            neutralButton.setText(neutralButtonText);
            neutralButton.setOnClickListener(v -> {
                if (neutralButtonListener != null) {
                    neutralButtonListener.onClick(dialog);
                } else {
                    dialog.dismiss();
                }
            });
            neutralButton.setVisibility(View.VISIBLE);
        } else {
            neutralButton.setVisibility(View.GONE);
        }
        dialog.setCancelable(isCancelable);      
        dialog.setCanceledOnTouchOutside(isCancelable);

        dialog.setContentView(dialogView);
        dialog.show();
        return dialog;
    }
}