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

public class LeetSelectorAdapter extends RecyclerView.Adapter<LeetSelectorAdapter.LeetViewHolder> {

    private final List<LeetOption> leetOptions;
    private final OnLeetSelectedListener listener;

    public interface OnLeetSelectedListener {
        void onLeetSelected(LeetOption leetOption);
        void onEditLeet(LeetOption leetOption);
        void onToggleFavorite(LeetOption leetOption);
    }

    public LeetSelectorAdapter(List<LeetOption> leetOptions, OnLeetSelectedListener listener) {
        this.leetOptions = leetOptions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LeetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leet_selector, parent, false);
        return new LeetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeetViewHolder holder, int position) {
        LeetOption option = leetOptions.get(position);
        holder.bind(option, listener);
    }

    @Override
    public int getItemCount() {
        return leetOptions.size();
    }

    static class LeetViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final ImageView iconLeet;
        private final TextView textLeetName;
        private final TextView textLeetDescription;
        private final ImageView iconSelected;
        private final ImageView iconFavorite;
        private final MaterialButton buttonEdit;

        public LeetViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            iconLeet = itemView.findViewById(R.id.iconLeet);
            textLeetName = itemView.findViewById(R.id.textLeetName);
            textLeetDescription = itemView.findViewById(R.id.textLeetDescription);
            iconSelected = itemView.findViewById(R.id.iconSelected);
            iconFavorite = itemView.findViewById(R.id.iconFavorite);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
        }

        public void bind(LeetOption option, OnLeetSelectedListener listener) {
            // Basis-Informationen setzen
            iconLeet.setImageResource(option.getIconResId());
            textLeetName.setText(option.getName());
            textLeetDescription.setText(option.getDescription());

            // Selected State
            iconSelected.setVisibility(option.isSelected() ? View.VISIBLE : View.GONE);

            // Favorite State
            iconFavorite.setVisibility(option.isFavorite() ? View.VISIBLE : View.GONE);

            // Edit Button nur für Custom Leets
            buttonEdit.setVisibility(option.isCustom() ? View.VISIBLE : View.GONE);

            // Card Stroke für ausgewähltes Element
            if (option.isSelected()) {
                cardView.setStrokeWidth(2);
                cardView.setStrokeColor(itemView.getContext().getResources()
                        .getColor(R.color.accent, itemView.getContext().getTheme()));
            } else {
                cardView.setStrokeWidth(1);
                cardView.setStrokeColor(android.graphics.Color.TRANSPARENT);
            }

            // Click Listeners
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLeetSelected(option);
                }
            });

            buttonEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditLeet(option);
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
    }
}