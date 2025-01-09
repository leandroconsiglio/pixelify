package iiec.ditzdev.pixelify.others;

import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.List;
import iiec.ditzdev.pixelify.models.ResolutionTemplate;
import iiec.ditzdev.pixelify.components.SettingsRadioCheck;
import iiec.ditzdev.pixelify.R;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.TextView;
import android.widget.Button;
import java.util.stream.Collectors;

public class ResolutionBottomSheetFragment extends BottomSheetDialogFragment {
    private List<ResolutionTemplate> templates;
    private OnResolutionSelectedListener listener;
    private LinearLayout containerLayout;
    private List<SettingsRadioCheck> radioChecks;
    private int selectedPosition = 0;

    public interface OnResolutionSelectedListener {
        void onResolutionSelected(ResolutionTemplate template);
    }

    public static ResolutionBottomSheetFragment newInstance(List<ResolutionTemplate> templates) {
        ResolutionBottomSheetFragment fragment = new ResolutionBottomSheetFragment();
        fragment.templates = templates;
        return fragment;
    }

    public void setOnResolutionSelectedListener(OnResolutionSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resolution_bottom_sheet, container, false);
        
        containerLayout = view.findViewById(R.id.containerLayout);
        TextView titleText = view.findViewById(R.id.titleText);
        Button selectButton = view.findViewById(R.id.selectButton);

        titleText.setText(getString(R.string.string_template_title));
        
        radioChecks = new ArrayList<>();
        for (int i = 0; i < templates.size(); i++) {
            ResolutionTemplate template = templates.get(i);
            
            SettingsRadioCheck radioCheck = new SettingsRadioCheck(getContext());
            radioCheck.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            radioCheck.setTitle(template.getDeviceName());
            radioCheck.setSubtitle(String.format("%dx%d - %s", 
                template.getLogicalWidth(),
                template.getLogicalHeight(),
                template.getPpi()));
            radioCheck.setClickable(true);
            radioCheck.setFocusable(true);
            TypedValue typedValue = new TypedValue();
            requireContext()
                    .getTheme()
                    .resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
            radioCheck.setBackgroundResource(typedValue.resourceId);
            final int position = i;
            radioCheck.setOnClickListener(v -> {
                selectedPosition = position;
                for (int j = 0; j < radioChecks.size(); j++) {
                    SettingsRadioCheck check = radioChecks.get(j);
                    boolean shouldBeChecked = j == position;
                    check.setChecked(shouldBeChecked);
                }
            });
            
            radioCheck.setOnRadioChangeListener(isChecked -> {
                if (isChecked) {
                    selectedPosition = position;
                    for (int j = 0; j < radioChecks.size(); j++) {
                        if (j != position) {
                            radioChecks.get(j).setChecked(false);
                        }
                    }
                }
            });
            
            radioChecks.add(radioCheck);
            containerLayout.addView(radioCheck);
        }
        
        if (!radioChecks.isEmpty()) {
            radioChecks.get(0).setChecked(true);
        }

        selectButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onResolutionSelected(templates.get(selectedPosition));
            }
            dismiss();
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
                FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet != null) {
                    BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });
    }
}