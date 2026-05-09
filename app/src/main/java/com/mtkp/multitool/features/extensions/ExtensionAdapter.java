package com.mtkp.multitool.features.extensions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.mtkp.multitool.R;

import java.util.List;

/**
 * Адаптер для карточек расширений на главном экране.
 *
 * Он показывает обычные карточки и отдельную карточку с плюсом
 * для добавления нового расширения.
 */
public class ExtensionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Extension> extensionList;
    private final OnExtensionActionListener listener;

    public interface OnExtensionActionListener {
        void onEditMenuClicked(Extension extension, View anchorView);
        void onAddNewExtension();
    }

    /**
     * Передаём список расширений и обработчик действий по карточкам.
     */
    public ExtensionAdapter(List<Extension> extensionList, OnExtensionActionListener listener) {
        this.extensionList = extensionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ADD) {
            // Карточка с плюсом для добавления нового расширения.
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_extension_card, parent, false);
            return new AddExtensionViewHolder(view);
        } else {
            // Обычная карточка установленного расширения.
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_extension_card, parent, false);
            return new ExtensionViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_ADD) {
            ((AddExtensionViewHolder) holder).bind();
        } else {
            ((ExtensionViewHolder) holder).bind(extensionList.get(position), position);
        }
    }

    @Override
    public int getItemCount() {
        // +1 нужен для карточки добавления нового расширения.
        return extensionList.size() + 1; // +1 для кнопки добавления
    }

    @Override
    public int getItemViewType(int position) {
        if (position == extensionList.size()) {
            return VIEW_TYPE_ADD;
        }
        return VIEW_TYPE_EXTENSION;
    }

    private static final int VIEW_TYPE_EXTENSION = 0;
    private static final int VIEW_TYPE_ADD = 1;

    /**
     * ViewHolder для обычной карточки расширения.
     */
    public class ExtensionViewHolder extends RecyclerView.ViewHolder {
        TextView tvExtensionName;
        ImageButton btnEditMenu;

        public ExtensionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExtensionName = itemView.findViewById(R.id.tv_extension_name);
            btnEditMenu = itemView.findViewById(R.id.btn_edit_menu);
        }

        public void bind(Extension extension, int position) {
            tvExtensionName.setText(extension.getName());

            btnEditMenu.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditMenuClicked(extension, v);
                }
            });
        }
    }

    /**
     * ViewHolder для карточки добавления нового расширения.
     */
    public class AddExtensionViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardExtension;
        TextView tvExtensionName;
        ImageButton btnEditMenu;
        ImageView ivExtensionIcon;

        public AddExtensionViewHolder(@NonNull View itemView) {
            super(itemView);
            cardExtension = itemView.findViewById(R.id.card_extension);
            tvExtensionName = itemView.findViewById(R.id.tv_extension_name);
            btnEditMenu = itemView.findViewById(R.id.btn_edit_menu);
            ivExtensionIcon = itemView.findViewById(R.id.iv_extension_icon);
        }

        public void bind() {
            tvExtensionName.setText("+");
            tvExtensionName.setTextSize(32);
            ivExtensionIcon.setAlpha(0.5f);
            btnEditMenu.setVisibility(View.GONE);

            cardExtension.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddNewExtension();
                }
            });
        }
    }

    /**
     * Простая модель данных одного расширения.
     */
    public static class Extension {
        private final String name;
        private final String description;
        private final int iconResId;

        public Extension(String name, String description, int iconResId) {
            this.name = name;
            this.description = description;
            this.iconResId = iconResId;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getIconResId() { return iconResId; }
    }
}




