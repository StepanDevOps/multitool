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
import com.mtkp.multitool.data.local.AppDatabase;
import com.mtkp.multitool.data.local.CachedExtensionEntity;
import com.mtkp.multitool.data.local.InstalledExtensionEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * BottomSheetDialogFragment для отображения списка установленных расширений.
 * Открывается снизу вверх при нажатии на "Установленные расширения" в навигации.
 */
public class ExtensionsBottomSheetFragment extends BottomSheetDialogFragment {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<String> extensionNames = new ArrayList<>();
    private ExtensionListAdapter adapter;

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

        adapter = new ExtensionListAdapter(extensionNames);
        recyclerView.setAdapter(adapter);

        loadInstalledExtensions();
    }

    private void loadInstalledExtensions() {
        final android.content.Context appContext = requireContext().getApplicationContext();
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(appContext);
            List<String> names = new ArrayList<>();
            for (InstalledExtensionEntity entity : db.installedExtensionDao().getAll()) {
                if (entity == null || entity.isHidden) continue;
                CachedExtensionEntity cached = db.cachedExtensionDao().getById(entity.extensionId);
                if (cached != null && cached.name != null && !cached.name.isEmpty()) {
                    names.add(cached.name + " v" + entity.installedVersion);
                } else {
                    names.add(String.format(Locale.getDefault(), "Extension %d", entity.extensionId));
                }
            }

            if (names.isEmpty()) {
                names.add("No installed extensions yet");
            }

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    extensionNames.clear();
                    extensionNames.addAll(names);
                    adapter.notifyDataSetChanged();
                });
            }
        });
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





