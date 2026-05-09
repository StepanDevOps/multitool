package com.mtkp.multitool.features.extensions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mtkp.multitool.R;

import java.util.ArrayList;
import java.util.List;

/**
 * BottomSheetDialogFragment для отображения списка установленных расширений.
 * Открывается снизу вверх при нажатии на "Установленные расширения" в навигации.
 */
public class ExtensionsBottomSheetFragment extends BottomSheetDialogFragment {

    public static ExtensionsBottomSheetFragment newInstance() {
        return new ExtensionsBottomSheetFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_extensions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.rv_bottom_sheet_extensions);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Подготовка данных (пока фейковые данные)
        List<String> extensionNames = new ArrayList<>();
        extensionNames.add("Notes");
        extensionNames.add("Weather");
        extensionNames.add("Calculator");
        extensionNames.add("Settings");

        ExtensionListAdapter adapter = new ExtensionListAdapter(extensionNames);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Адаптер для простого списка названий расширений
     */
    private static class ExtensionListAdapter extends RecyclerView.Adapter<ExtensionListAdapter.NameViewHolder> {

        private final List<String> extensionNames;

        public ExtensionListAdapter(List<String> extensionNames) {
            this.extensionNames = extensionNames;
        }

        @NonNull
        @Override
        public NameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_extension_name, parent, false);
            return new NameViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NameViewHolder holder, int position) {
            holder.bind(extensionNames.get(position));
        }

        @Override
        public int getItemCount() {
            return extensionNames.size();
        }

        static class NameViewHolder extends RecyclerView.ViewHolder {

            NameViewHolder(@NonNull View itemView) {
                super(itemView);
            }

            void bind(String name) {
                TextView textView = itemView.findViewById(R.id.tv_extension_list_name);
                textView.setText(name);
            }
        }
    }
}





