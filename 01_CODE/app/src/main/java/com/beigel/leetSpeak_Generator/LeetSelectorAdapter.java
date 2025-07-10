package com.beigel.leetSpeak_Generator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class LeetSelectorAdapter extends RecyclerView.Adapter<LeetSelectorAdapter.EnhancedLeetViewHolder> {

    private List<LeetOption> leetOptions;
    private final OnLeetSelectedListener listener;

    public interface OnLeetSelectedListener {
        void onLeetSelected(LeetOption leetOption);
        void onLeetPreview(LeetOption leetOption);
        void onEditLeet(LeetOption leetOption);
        void onToggleFavorite(LeetOption leetOption);
        void onQuickTest(LeetOption leetOption);
        void onShowTable(LeetOption leetOption); // Neue Methode für Tabellen-Anzeige
    }

    public LeetSelectorAdapter(List<LeetOption> leetOptions, OnLeetSelectedListener listener) {
        this.leetOptions = leetOptions;
        this.listener = listener;
    }

    public void updateOptions(List<LeetOption> newOptions) {
        this.leetOptions = newOptions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EnhancedLeetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leet_selector, parent, false);
        return new EnhancedLeetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EnhancedLeetViewHolder holder, int position) {
        LeetOption option = leetOptions.get(position);
        holder.bind(option, listener);
    }

    @Override
    public int getItemCount() {
        return leetOptions.size();
    }

    static class EnhancedLeetViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final ImageView iconLeet;
        private final TextView textLeetName;
        private final TextView textLeetDescription;
        private final TextView textPreview;
        private final ImageView iconSelected;
        private final ImageView iconFavorite;
        private final MaterialButton buttonEdit;
        private final MaterialButton buttonQuickTest;
        private final MaterialButton buttonShowTable; // Neuer Button für Tabelle
        private final View previewSection;

        public EnhancedLeetViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            iconLeet = itemView.findViewById(R.id.iconLeet);
            textLeetName = itemView.findViewById(R.id.textLeetName);
            textLeetDescription = itemView.findViewById(R.id.textLeetDescription);
            textPreview = itemView.findViewById(R.id.textPreview);
            iconSelected = itemView.findViewById(R.id.iconSelected);
            iconFavorite = itemView.findViewById(R.id.iconFavorite);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonQuickTest = itemView.findViewById(R.id.buttonQuickTest);
            buttonShowTable = itemView.findViewById(R.id.buttonShowTable); // Neue Referenz
            previewSection = itemView.findViewById(R.id.previewSection);
        }

        public void bind(LeetOption option, OnLeetSelectedListener listener) {
            // Basis-Informationen setzen
            iconLeet.setImageResource(option.getIconResId());
            textLeetName.setText(option.getName());
            textLeetDescription.setText(option.getDescription());

            // Preview aktualisieren
            String preview = generatePreview(option);
            textPreview.setText(preview);

            // Selected State
            iconSelected.setVisibility(option.isSelected() ? View.VISIBLE : View.GONE);

            // Favorite State
            iconFavorite.setVisibility(option.isFavorite() ? View.VISIBLE : View.GONE);

            // Edit Button nur für Custom Leets
            buttonEdit.setVisibility(option.isCustom() ? View.VISIBLE : View.GONE);

            // Card Stroke für ausgewähltes Element
            if (option.isSelected()) {
                cardView.setStrokeWidth(3);
                cardView.setStrokeColor(itemView.getContext().getResources()
                        .getColor(R.color.accent, itemView.getContext().getTheme()));
                cardView.setCardElevation(8f);
            } else {
                cardView.setStrokeWidth(1);
                cardView.setStrokeColor(android.graphics.Color.TRANSPARENT);
                cardView.setCardElevation(2f);
            }

            // Click Listeners
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLeetSelected(option);
                }
            });

            // Hover effect for preview
            cardView.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus && listener != null) {
                    listener.onLeetPreview(option);
                }
            });

            buttonEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditLeet(option);
                }
            });

            buttonQuickTest.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQuickTest(option);
                    AnimationHelper.pulse(buttonQuickTest, 1);
                }
            });

            // Neuer Tabellen-Button Click Listener
            buttonShowTable.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onShowTable(option);
                    AnimationHelper.pulse(buttonShowTable, 1);
                }
            });

            // Long Click für Favoriten
            cardView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onToggleFavorite(option);
                    AnimationHelper.pulse(iconFavorite, 1);
                }
                return true;
            });

            // Favorite Icon Click
            iconFavorite.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onToggleFavorite(option);
                    AnimationHelper.pulse(iconFavorite, 1);
                }
            });
        }

        private String generatePreview(LeetOption option) {
            // Generate preview based on leet option
            switch (option.getMode()) {
                case ProfileManager.MODE_SIMPLE:
                    return LeetTranslator.translate("Hello", LeetTranslator.TranslationMode.SIMPLE, null);
                case ProfileManager.MODE_EXTENDED:
                    return LeetTranslator.translate("Hello", LeetTranslator.TranslationMode.EXTENDED, null);
                case ProfileManager.MODE_CUSTOM:
                    // This would need access to the profile repository
                    // For now, return a placeholder
                    return "H3ll0"; // Placeholder
                default:
                    return "H3110";
            }
        }
    }
}