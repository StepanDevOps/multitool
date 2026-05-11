package com.mtkp.multitool.features.extensions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.mtkp.multitool.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Адаптер для сетки карточек в магазине расширений.
 */
class ExtensionsShopAdapter extends RecyclerView.Adapter<ExtensionsShopAdapter.ExtensionViewHolder> {

    /**
     * Обработчик нажатия на кнопку перехода в карточке.
     */
    public interface OnExtensionClickListener {
        void onExtensionClicked(ExtensionItem item);
    }

    public interface OnExtensionActionListener {
        void onExtensionActionClicked(ExtensionItem item);
    }

    private final List<ExtensionItem> items = new ArrayList<>();
    private final OnExtensionClickListener listener;
    private final OnExtensionActionListener actionListener;

    public ExtensionsShopAdapter(OnExtensionClickListener listener, OnExtensionActionListener actionListener) {
        this.listener = listener;
        this.actionListener = actionListener;
    }

    /**
     * Обновляем список карточек.
     */
    public void submitList(List<ExtensionItem> newItems) {
        int oldSize = items.size();
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        if (oldSize > 0) {
            notifyItemRangeRemoved(0, oldSize);
        }
        if (!items.isEmpty()) {
            notifyItemRangeInserted(0, items.size());
        }
    }

    @NonNull
    @Override
    public ExtensionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_extension_shop, parent, false);
        return new ExtensionViewHolder(view, listener, actionListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ExtensionViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ExtensionViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView card;
        private final ChipGroup chipGroupCategories;
        private final ImageView iconView;
        private final TextView titleView;
        private final TextView descriptionView;
        private final TextView metaView;
        private final MaterialButton actionButton;
        private final OnExtensionClickListener listener;
        private final OnExtensionActionListener actionListener;

        ExtensionViewHolder(@NonNull View itemView,
                          OnExtensionClickListener listener,
                          OnExtensionActionListener actionListener) {
            super(itemView);
            this.listener = listener;
            this.actionListener = actionListener;
            card = itemView.findViewById(R.id.card_extension_shop);
            chipGroupCategories = itemView.findViewById(R.id.chip_group_categories);
            iconView = itemView.findViewById(R.id.iv_extension_icon);
            titleView = itemView.findViewById(R.id.tv_extension_title);
            descriptionView = itemView.findViewById(R.id.tv_extension_description);
            metaView = itemView.findViewById(R.id.tv_extension_meta);
            actionButton = itemView.findViewById(R.id.btn_open_extension);
        }

        void bind(ExtensionItem item) {
            iconView.setImageResource(item.getIconResId());
            titleView.setText(item.getTitle());
            descriptionView.setText(item.getShortDescription());
            renderCategories(item.getCategoryResIds());
            String meta = itemView.getContext().getString(
                    R.string.extension_shop_meta,
                    item.getRating(),
                    java.text.NumberFormat.getIntegerInstance(Locale.getDefault()).format(item.getInstalls())
            );
            if (item.isUpdateAvailable()) {
                meta = meta + " • " + itemView.getContext().getString(R.string.extension_badge_update);
            } else if (item.isInstalled()) {
                meta = meta + " • " + itemView.getContext().getString(R.string.extension_badge_installed);
            }
            metaView.setText(meta);

            if (item.isUpdateAvailable()) {
                actionButton.setText(R.string.extension_action_update);
            } else if (item.isInstalled()) {
                actionButton.setText(R.string.extension_open);
            } else {
                actionButton.setText(R.string.extension_action_install);
            }

            actionButton.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onExtensionActionClicked(item);
                }
            });

            card.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExtensionClicked(item);
                }
            });
        }

        private void renderCategories(int[] categoryResIds) {
            chipGroupCategories.removeAllViews();
            for (int categoryResId : categoryResIds) {
                Chip chip = new Chip(itemView.getContext(), null, com.google.android.material.R.style.Widget_Material3_Chip_Assist);
                chip.setText(categoryResId);
                chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
                chip.setCheckable(false);
                chip.setClickable(false);
                chip.setFocusable(false);
                chipGroupCategories.addView(chip);
            }
        }
    }
}
