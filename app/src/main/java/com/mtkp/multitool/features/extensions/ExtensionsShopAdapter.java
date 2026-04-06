package com.mtkp.multitool.features.extensions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.card.MaterialCardView;
import com.mtkp.multitool.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Адаптер для сетки карточек в магазине расширений.
 */
public class ExtensionsShopAdapter extends RecyclerView.Adapter<ExtensionsShopAdapter.ExtensionViewHolder> {

    /**
     * Обработчик нажатия на кнопку перехода в карточке.
     */
    public interface OnExtensionClickListener {
        void onExtensionClicked(ExtensionItem item);
    }

    private final List<ExtensionItem> items = new ArrayList<>();
    private final OnExtensionClickListener listener;
    private final NumberFormat numberFormat = NumberFormat.getIntegerInstance(Locale.getDefault());

    public ExtensionsShopAdapter(OnExtensionClickListener listener) {
        this.listener = listener;
    }

    /**
     * Обновляем список карточек.
     */
    public void submitList(List<ExtensionItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExtensionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_extension_shop, parent, false);
        return new ExtensionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExtensionViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ExtensionViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView card;
        private final Chip chipCategory;
        private final Chip chipStatus;
        private final ImageView iconView;
        private final TextView titleView;
        private final TextView descriptionView;
        private final TextView metaView;
        private final MaterialButton actionButton;

        ExtensionViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_extension_shop);
            chipCategory = itemView.findViewById(R.id.chip_category);
            chipStatus = itemView.findViewById(R.id.chip_status);
            iconView = itemView.findViewById(R.id.iv_extension_icon);
            titleView = itemView.findViewById(R.id.tv_extension_title);
            descriptionView = itemView.findViewById(R.id.tv_extension_description);
            metaView = itemView.findViewById(R.id.tv_extension_meta);
            actionButton = itemView.findViewById(R.id.btn_open_extension);
        }

        void bind(ExtensionItem item) {
            card.setOnClickListener(null);
            iconView.setImageResource(item.getIconResId());
            titleView.setText(item.getTitle());
            descriptionView.setText(item.getShortDescription());
            chipCategory.setText(itemView.getContext().getString(item.getCategoryResId()));
            chipStatus.setVisibility(item.isInstalled() ? View.VISIBLE : View.GONE);
            chipStatus.setText(item.isUpdateAvailable()
                    ? itemView.getContext().getString(R.string.extension_badge_update)
                    : itemView.getContext().getString(R.string.extension_badge_installed));
            metaView.setText(itemView.getContext().getString(
                    R.string.extension_shop_meta,
                    item.getRating(),
                    numberFormat.format(item.getInstalls())
            ));

            actionButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExtensionClicked(item);
                }
            });

            card.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExtensionClicked(item);
                }
            });
        }
    }
}


