package com.beigel.leetSpeak_Generator;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class IconSelectorDialog extends AppCompatDialog {

    private OnIconSelectedListener listener;
    private int selectedIconResId = R.drawable.ic_custom_mode;

    // Interface für Callback wenn ein Icon ausgewählt wurde
    public interface OnIconSelectedListener {
        void onIconSelected(int iconResId);
    }

    // Verfügbare Icons für die Auswahl
    private static final int[] AVAILABLE_ICONS = {
            R.drawable.ic_custom_mode,
            R.drawable.ic_simple_mode,
            R.drawable.ic_extended_mode,
            R.drawable.ic_about,
            R.drawable.ic_add_profile,
            R.drawable.ic_edit,
            R.drawable.ic_fab_add,
            R.drawable.ic_favorite_border,
            R.drawable.ic_favorite
            // Weitere Icons können hier hinzugefügt werden
    };

    public IconSelectorDialog(@NonNull Context context, OnIconSelectedListener listener) {
        super(context);
        this.listener = listener;
    }

    public IconSelectorDialog(@NonNull Context context, OnIconSelectedListener listener, int initialIconResId) {
        super(context);
        this.listener = listener;
        this.selectedIconResId = initialIconResId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Dialog-Builder erstellen
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle(R.string.icon_selection);

        // Layout aufblasen
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_icon_selection, null);
        GridLayout gridLayout = view.findViewById(R.id.iconGrid);

        // Icons zum Grid hinzufügen
        int column = 0;
        int row = 0;
        int iconSize = (int) getContext().getResources().getDimension(R.dimen.icon_size);

        for (int iconResId : AVAILABLE_ICONS) {
            ImageView iconView = new ImageView(getContext());
            iconView.setImageResource(iconResId);
            iconView.setPadding(16, 16, 16, 16);
            iconView.setBackgroundResource(R.drawable.ripple_effect);

            // Setze das ausgewählte Icon hervor
            if (iconResId == selectedIconResId) {
                iconView.setBackgroundResource(R.drawable.selected_icon_background);
            }

            // Klick-Listener
            iconView.setOnClickListener(v -> {
                selectedIconResId = iconResId;
                // Alle Icons zurücksetzen
                for (int i = 0; i < gridLayout.getChildCount(); i++) {
                    gridLayout.getChildAt(i).setBackgroundResource(R.drawable.ripple_effect);
                }
                // Ausgewähltes Icon hervorheben
                v.setBackgroundResource(R.drawable.selected_icon_background);
            });

            // Zum Grid hinzufügen
            GridLayout.Spec rowSpec = GridLayout.spec(row);
            GridLayout.Spec colSpec = GridLayout.spec(column);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, colSpec);
            params.width = iconSize;
            params.height = iconSize;
            params.setMargins(8, 8, 8, 8);

            gridLayout.addView(iconView, params);

            // Nächste Position im Grid
            column++;
            if (column >= 4) {
                column = 0;
                row++;
            }
        }

        builder.setView(view);

        // Buttons hinzufügen
        builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
            if (listener != null) {
                listener.onIconSelected(selectedIconResId);
            }
            dismiss();
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dismiss());

        // Dialog erstellen und anzeigen
        AlertDialog dialog = builder.create();
        dialog.show();

        // Den aktuellen Dialog schließen, da wir den AlertDialog anzeigen
        dismiss();
    }
}