package com.mtkp.multitool.features.extensions;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.mtkp.multitool.R;
import com.mtkp.multitool.data.remote.ApiRequestException;
import com.mtkp.multitool.data.local.AppDatabase;
import com.mtkp.multitool.data.local.InstalledExtensionEntity;
import com.mtkp.multitool.data.remote.dto.ExtensionDto;
import com.mtkp.multitool.data.remote.dto.UploadVersionResponseDto;
import com.mtkp.multitool.data.repository.ExtensionsRepository;
import com.mtkp.multitool.data.settings.SettingsStorage;
import com.mtkp.multitool.extensions.ExtensionDeveloperApi;
import com.mtkp.multitool.extensions.ExtensionManager;
import com.mtkp.multitool.features.settings.SettingsActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Экран магазина расширений.
 * <p>
 * Пока это только красивый mock-интерфейс: карточки, поиск, фильтр,
 * сортировка и верхнее меню с кнопкой добавления своего плагина.
 */
public class ExtensionsShopActivity extends AppCompatActivity {

    private View root;
    private View appBarLayout;
    private Toolbar toolbar;
    private TextInputEditText searchInput;
    private ChipGroup categoryChipGroup;
    private MaterialButtonToggleGroup sortToggleGroup;
    private BottomNavigationView bottomNavigation;
    private RecyclerView recyclerView;
    private View loadingView;
    private View emptyView;
    private View errorView;
    private TextView emptyMessageView;
    private TextView errorMessageView;
    private View emptyRetryButton;
    private View errorRetryButton;
    private ExtensionsShopAdapter adapter;

    private List<ExtensionItem> sourceItems = new ArrayList<>();
    private String currentQuery = "";
    private final Set<Integer> selectedCategoryResIds = new HashSet<>();
    private ExtensionsCatalogManager.SortMode currentSortMode = ExtensionsCatalogManager.SortMode.POPULAR;
    private boolean isUpdatingCategorySelection;
    private ExtensionManager extensionManager;
    private ExtensionsRepository extensionsRepository;
    private ExtensionDeveloperApi developerApi;
    private AppDatabase appDatabase;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ActivityResultLauncher<String[]> publishJarPicker;
    private Uri selectedPublishJarUri;
    private String selectedPublishJarName;
    private TextView selectedJarTextView;
    private boolean isLoading;
    private String lastErrorMessage;
    private boolean firstResume = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.extensions_shop);

        initViews();
        setupWindowInsets();
        setupToolbar();
        setupRecyclerView();
        extensionManager = new ExtensionManager(getApplicationContext());
        extensionsRepository = new ExtensionsRepository(getApplicationContext());
        developerApi = new ExtensionDeveloperApi(getApplicationContext());
        appDatabase = AppDatabase.getInstance(getApplicationContext());
        setupBottomNavigation();
        setupSearch();
        setupCategoryFilter();
        setupSorting();
        registerPublishJarPicker();
        loadExtensionsFromApi();
    }

    private void initViews() {
        root = findViewById(R.id.extensions_shop_root);
        appBarLayout = findViewById(R.id.appbar_extensions_shop);
        toolbar = findViewById(R.id.toolbar_extensions_shop);
        searchInput = findViewById(R.id.et_extensions_search);
        categoryChipGroup = findViewById(R.id.chip_group_extension_categories);
        sortToggleGroup = findViewById(R.id.toggle_group_extension_sort);
        bottomNavigation = findViewById(R.id.bottomNavigationShop);
        recyclerView = findViewById(R.id.rv_extensions_shop);
        loadingView = findViewById(R.id.layout_extensions_shop_loading);
        emptyView = findViewById(R.id.layout_extensions_shop_empty);
        errorView = findViewById(R.id.layout_extensions_shop_error);
        emptyMessageView = findViewById(R.id.tv_extensions_shop_empty);
        errorMessageView = findViewById(R.id.tv_extensions_shop_error);
        emptyRetryButton = findViewById(R.id.btn_extensions_shop_empty_retry);
        errorRetryButton = findViewById(R.id.btn_extensions_shop_error_retry);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, 0, systemBars.right, 0);
            appBarLayout.setPadding(
                    appBarLayout.getPaddingLeft(),
                    systemBars.top,
                    appBarLayout.getPaddingRight(),
                    appBarLayout.getPaddingBottom()
            );
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.nav_store);
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new GridLayoutManager(this, resolveSpanCount()));
        recyclerView.setHasFixedSize(true);
        adapter = new ExtensionsShopAdapter(this::openExtension, this::handleExtensionAction);
        recyclerView.setAdapter(adapter);
        emptyRetryButton.setOnClickListener(v -> loadExtensionsFromApi());
        errorRetryButton.setOnClickListener(v -> loadExtensionsFromApi());
        showLoadingState();
    }

    private void loadExtensionsFromApi() {
        isLoading = true;
        lastErrorMessage = null;
        showLoadingState();
        extensionManager.listAvailable(1, 50, new ExtensionManager.Callback<>() {
            @Override
            public void onSuccess(List<ExtensionDto> result) {
                executor.execute(() -> {
                    List<ExtensionItem> mapped = mapRemoteItems(result == null ? Collections.emptyList() : result);
                    runOnUiThread(() -> {
                        isLoading = false;
                        lastErrorMessage = null;
                        sourceItems = mapped;
                        applyFilters();
                    });
                });
            }

            @Override
            public void onError(Throwable t) {
                runOnUiThread(() -> {
                    isLoading = false;
                    lastErrorMessage = t == null ? "unknown" : t.getMessage();
                    sourceItems = new ArrayList<>();
                    showErrorState(lastErrorMessage);
                });
            }
        });
    }

    private List<ExtensionItem> mapRemoteItems(List<ExtensionDto> remoteItems) {
        List<ExtensionItem> result = new ArrayList<>();
        Map<Integer, InstalledExtensionEntity> installedMap = new HashMap<>();
        List<InstalledExtensionEntity> installedExtensions = appDatabase.installedExtensionDao().getAll();
        if (installedExtensions != null) {
            for (InstalledExtensionEntity entity : installedExtensions) {
                if (entity != null) {
                    installedMap.put(entity.extensionId, entity);
                }
            }
        }

        for (ExtensionDto dto : remoteItems) {
            InstalledExtensionEntity installedExtension = installedMap.get(dto.id);
            boolean installed = installedExtension != null;
            boolean updateAvailable = installed
                    && dto.version != null
                    && !TextUtils.isEmpty(installedExtension.installedVersion)
                    && !dto.version.equalsIgnoreCase(installedExtension.installedVersion);
            String version = dto.version == null ? "1.0.0" : dto.version;
            String author = dto.authorName == null ? "Unknown" : dto.authorName;
            String shortDescription = dto.shortDescription == null ? "" : dto.shortDescription;
            String detailed = dto.detailedDescription == null ? shortDescription : dto.detailedDescription;
            int[] categoryRes = mapCategories(dto.categories);

            result.add(new ExtensionItem(
                    String.valueOf(dto.id),
                    dto.name == null ? ("Extension " + dto.id) : dto.name,
                    author,
                    version,
                    (int) dto.downloads,
                    dto.rating,
                    categoryRes,
                    shortDescription,
                    detailed,
                    R.drawable.ic_account_box,
                    installed,
                    updateAvailable
            ));
        }
        return result;
    }

    private void showLoadingState() {
        loadingView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
    }

    private void showErrorState(String message) {
        errorMessageView.setText(getString(R.string.extension_shop_error, message == null ? "unknown" : message));
        loadingView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void showEmptyState(boolean filteredEmpty) {
        emptyMessageView.setText(filteredEmpty ? R.string.extension_shop_empty_query : R.string.extension_shop_empty);
        loadingView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
    }

    private void showContentState() {
        loadingView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private int[] mapCategories(List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return new int[]{R.string.category_other};
        }
        List<Integer> result = new ArrayList<>();
        for (String category : categories) {
            String value = category == null ? "" : category.toLowerCase(Locale.ROOT);
            if (value.contains("product")) {
                result.add(R.string.category_productivity);
            } else if (value.contains("personal")) {
                result.add(R.string.category_personalization);
            } else if (value.contains("educ")) {
                result.add(R.string.category_education);
            } else if (value.contains("media")) {
                result.add(R.string.category_media);
            } else if (value.contains("util")) {
                result.add(R.string.category_utilities);
            } else {
                result.add(R.string.category_other);
            }
        }
        int[] mapped = new int[result.size()];
        for (int i = 0; i < result.size(); i++) {
            mapped[i] = result.get(i);
        }
        return mapped;
    }

    private int resolveSpanCount() {
        int widthDp = getResources().getConfiguration().screenWidthDp;
        if (widthDp >= 840) {
            return 4;
        }
        if (widthDp >= 600) {
            return 3;
        }
        return 2;
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_store);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, com.mtkp.multitool.MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_extensions) {
                ExtensionsBottomSheetFragment.newInstance()
                        .show(getSupportFragmentManager(), "extensions_bottom_sheet");
                return true;
            }
            return itemId == R.id.nav_store;
        });
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s == null ? "" : s.toString();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupCategoryFilter() {
        categoryChipGroup.check(R.id.chip_category_all);
        categoryChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (isUpdatingCategorySelection) {
                return;
            }

            List<Integer> activeCheckedIds = checkedIds;

            isUpdatingCategorySelection = true;

            // Если ничего не выбрано, включаем "Все" обратно.
            if (activeCheckedIds.isEmpty()) {
                categoryChipGroup.check(R.id.chip_category_all);
                selectedCategoryResIds.clear();
                isUpdatingCategorySelection = false;
                applyFilters();
                return;
            }

            // Если выбрали конкретные категории вместе с "Все",
            // снимаем "Все" и оставляем только конкретные.
            if (activeCheckedIds.contains(R.id.chip_category_all) && activeCheckedIds.size() > 1) {
                android.view.View allChip = categoryChipGroup.findViewById(R.id.chip_category_all);
                if (allChip instanceof com.google.android.material.chip.Chip) {
                    ((com.google.android.material.chip.Chip) allChip).setChecked(false);
                }
                activeCheckedIds = categoryChipGroup.getCheckedChipIds();
            }

            selectedCategoryResIds.clear();
            if (!activeCheckedIds.contains(R.id.chip_category_all)) {
                for (int checkedId : activeCheckedIds) {
                    int categoryResId = mapCategoryChipToStringRes(checkedId);
                    if (categoryResId != 0) {
                        selectedCategoryResIds.add(categoryResId);
                    }
                }
            }

            isUpdatingCategorySelection = false;
            applyFilters();
        });
    }

    private void setupSorting() {
        sortToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }

            if (checkedId == R.id.btn_sort_newest) {
                currentSortMode = ExtensionsCatalogManager.SortMode.NEWEST;
            } else if (checkedId == R.id.btn_sort_alphabetical) {
                currentSortMode = ExtensionsCatalogManager.SortMode.ALPHABETICAL;
            } else {
                currentSortMode = ExtensionsCatalogManager.SortMode.POPULAR;
            }
            applyFilters();
        });
    }

    private void applyFilters() {
        List<ExtensionItem> filtered = ExtensionsCatalogManager.applyAll(
                sourceItems,
                currentQuery,
                selectedCategoryResIds,
                currentSortMode
        );
        adapter.submitList(filtered);
        if (isLoading) {
            showLoadingState();
        } else if (lastErrorMessage != null) {
            showErrorState(lastErrorMessage);
        } else if (filtered.isEmpty()) {
            showEmptyState(!sourceItems.isEmpty());
        } else {
            showContentState();
        }
    }

    private int mapCategoryChipToStringRes(int chipId) {
        if (chipId == R.id.chip_category_all) {
            return 0;
        } else if (chipId == R.id.chip_category_productivity) {
            return R.string.category_productivity;
        } else if (chipId == R.id.chip_category_personalization) {
            return R.string.category_personalization;
        } else if (chipId == R.id.chip_category_education) {
            return R.string.category_education;
        } else if (chipId == R.id.chip_category_media) {
            return R.string.category_media;
        } else if (chipId == R.id.chip_category_utilities) {
            return R.string.category_utilities;
        } else if (chipId == R.id.chip_category_other) {
            return R.string.category_other;
        }
        return 0;
    }

    private void openExtension(ExtensionItem item) {
        Intent intent = new Intent(this, ExtensionActivity.class);
        intent.putExtra(ExtensionActivity.EXTRA_EXTENSION_ID, item.getId());
        startActivity(intent);
    }

    private void handleExtensionAction(ExtensionItem item) {
        if (item.isInstalled() && !item.isUpdateAvailable()) {
            openExtension(item);
            return;
        }

        installOrUpdateFromShop(item);
    }

    private void installOrUpdateFromShop(ExtensionItem item) {
        if (item == null) {
            return;
        }

        Snackbar.make(root, R.string.extension_shop_installing, Snackbar.LENGTH_SHORT).show();
        extensionsRepository.installAndActivate(
                Integer.parseInt(item.getId()),
                item.getVersion(),
                ExtensionManager.DEFAULT_ENTRY_CLASS,
                new ExtensionsRepository.ResultCallback<>() {
                    @Override
                    public void onSuccess(com.mtkp.multitool.extensions.LoadedExtension result) {
                        runOnUiThread(() -> {
                            Snackbar.make(root, getString(R.string.extension_action_install_success, result.displayName), Snackbar.LENGTH_LONG).show();
                            loadExtensionsFromApi();
                        });
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        runOnUiThread(() -> Snackbar.make(root, getString(R.string.extension_action_install_error, throwable.getMessage()), Snackbar.LENGTH_LONG).show());
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (firstResume) {
            firstResume = false;
            return;
        }
        loadExtensionsFromApi();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_extensions_shop, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_shop_add_extension) {
            handleAddExtensionClick();
            return true;
        }

        if (item.getItemId() == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void registerPublishJarPicker() {
        publishJarPicker = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri == null) {
                        return;
                    }
                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (SecurityException ignored) {
                    }
                    selectedPublishJarUri = uri;
                    selectedPublishJarName = resolveDisplayName(uri);
                    if (selectedJarTextView != null) {
                        selectedJarTextView.setText(selectedPublishJarName == null
                                ? getString(R.string.extension_publish_no_file_selected)
                                : selectedPublishJarName);
                    }
                }
        );
    }

    private void handleAddExtensionClick() {
        SettingsStorage storage = new SettingsStorage(getApplicationContext());
        if (TextUtils.isEmpty(storage.getAuthToken())) {
            Snackbar.make(root, R.string.extension_shop_login_required, Snackbar.LENGTH_SHORT).show();
            startActivity(new Intent(this, SettingsActivity.class));
            return;
        }

        selectedPublishJarUri = null;
        selectedPublishJarName = null;
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_extension_publish, null, false);
        TextInputEditText nameInput = dialogView.findViewById(R.id.et_publish_extension_name);
        TextInputEditText shortInput = dialogView.findViewById(R.id.et_publish_extension_short_description);
        TextInputEditText detailedInput = dialogView.findViewById(R.id.et_publish_extension_detailed_description);
        TextInputEditText versionInput = dialogView.findViewById(R.id.et_publish_extension_version);
        TextInputEditText releaseNotesInput = dialogView.findViewById(R.id.et_publish_extension_release_notes);
        TextInputEditText changelogInput = dialogView.findViewById(R.id.et_publish_extension_changelog);
        View chooseJarButton = dialogView.findViewById(R.id.btn_publish_choose_jar);
        selectedJarTextView = dialogView.findViewById(R.id.tv_publish_selected_jar);

        chooseJarButton.setOnClickListener(v -> publishJarPicker.launch(new String[]{
                "application/java-archive",
                "application/octet-stream",
                "*/*"
        }));

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.extension_shop_add_title)
                .setView(dialogView)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, null)
                .show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = nameInput.getText() == null ? "" : nameInput.getText().toString().trim();
            String shortDescription = shortInput.getText() == null ? "" : shortInput.getText().toString().trim();
            String detailedDescription = detailedInput.getText() == null ? "" : detailedInput.getText().toString().trim();
            String version = versionInput.getText() == null ? "" : versionInput.getText().toString().trim();
            String releaseNotes = releaseNotesInput.getText() == null ? "" : releaseNotesInput.getText().toString().trim();
            String changelog = changelogInput.getText() == null ? "" : changelogInput.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(shortDescription)) {
                Snackbar.make(root, R.string.extension_edit_invalid_input, Snackbar.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(version)) {
                Snackbar.make(root, R.string.extension_publish_version_required, Snackbar.LENGTH_SHORT).show();
                return;
            }
            if (selectedPublishJarUri == null) {
                Snackbar.make(root, R.string.extension_publish_file_required, Snackbar.LENGTH_SHORT).show();
                return;
            }

            String jarName = selectedPublishJarName == null ? resolveDisplayName(selectedPublishJarUri) : selectedPublishJarName;
            if (jarName == null || !jarName.toLowerCase(Locale.ROOT).endsWith(".jar")) {
                Snackbar.make(root, R.string.extension_publish_invalid_file, Snackbar.LENGTH_SHORT).show();
                return;
            }

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            uploadExtensionFlow(
                    dialog,
                    name,
                    shortDescription,
                    detailedDescription,
                    version,
                    releaseNotes,
                    changelog,
                    selectedPublishJarUri
            );
        });
    }

    private void uploadExtensionFlow(
            AlertDialog dialog,
            String name,
            String shortDescription,
            String detailedDescription,
            String version,
            String releaseNotes,
            String changelog,
            Uri jarUri
    ) {
        developerApi.createExtension(
                name,
                shortDescription,
                detailedDescription,
                Collections.emptyList(),
                new ExtensionDeveloperApi.Callback<>() {
                    @Override
                    public void onSuccess(ExtensionDto data) {
                        executor.execute(() -> {
                            try {
                                File tempJar = copyUriToTempJar(jarUri, name, version);
                                developerApi.uploadVersion(
                                        data.id,
                                        version,
                                        releaseNotes,
                                        tempJar,
                                        changelog,
                                        new ExtensionDeveloperApi.Callback<UploadVersionResponseDto>() {
                                            @Override
                                            public void onSuccess(UploadVersionResponseDto uploadData) {
                                                runOnUiThread(() -> {
                                                    dialog.dismiss();
                                                    Snackbar.make(root, R.string.extension_publish_upload_success, Snackbar.LENGTH_LONG).show();
                                                    loadExtensionsFromApi();
                                                });
                                            }

                                            @Override
                                            public void onError(Throwable throwable) {
                                                runOnUiThread(() -> {
                                                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                                    Snackbar.make(root, getPublishErrorMessage(throwable, R.string.extension_publish_upload_error), Snackbar.LENGTH_LONG).show();
                                                });
                                            }
                                        }
                                );
                            } catch (Exception e) {
                                runOnUiThread(() -> {
                                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                    Snackbar.make(root, getPublishErrorMessage(e, R.string.extension_publish_upload_error), Snackbar.LENGTH_LONG).show();
                                });
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        runOnUiThread(() -> {
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                            Snackbar.make(root, getPublishErrorMessage(throwable, R.string.extension_publish_create_error), Snackbar.LENGTH_LONG).show();
                        });
                    }
                }
        );
    }

    private File copyUriToTempJar(Uri uri, String extensionName, String version) throws Exception {
        String safeName = extensionName == null ? "extension" : extensionName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String safeVersion = version == null ? "1.0.0" : version.replaceAll("[^a-zA-Z0-9._-]", "_");
        File out = new File(getCacheDir(), safeName + "-" + safeVersion + ".jar");
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(out)) {
            if (inputStream == null) {
                throw new IllegalStateException("Cannot open selected file");
            }
            byte[] buffer = new byte[8 * 1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
        }
        return out;
    }

    private String resolveDisplayName(Uri uri) {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    return cursor.getString(index);
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return uri.getLastPathSegment();
    }

    private String getPublishErrorMessage(Throwable throwable, int fallbackResId) {
        if (throwable instanceof ApiRequestException) {
            int code = ((ApiRequestException) throwable).getCode();
            if (code == 403) {
                return getString(R.string.extension_publish_forbidden);
            }
            if (code == 401) {
                return getString(R.string.extension_shop_login_required);
            }
            if (code == 413) {
                return throwable.getMessage() == null ? getString(fallbackResId, "HTTP 413") : throwable.getMessage();
            }
            if (code >= 500) {
                return throwable.getMessage() == null ? getString(fallbackResId, "server error") : throwable.getMessage();
            }
            return throwable.getMessage() == null
                    ? getString(fallbackResId, "HTTP " + code)
                    : throwable.getMessage();
        }

        String message = throwable == null ? null : throwable.getMessage();
        if (TextUtils.isEmpty(message)) {
            return getString(fallbackResId, "unknown error");
        }
        return getString(fallbackResId, message);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}
