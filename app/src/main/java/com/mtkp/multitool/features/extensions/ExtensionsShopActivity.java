package com.mtkp.multitool.features.extensions;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.mtkp.multitool.R;
import com.mtkp.multitool.data.settings.SettingsStorage;
import com.mtkp.multitool.features.settings.SettingsActivity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private ExtensionsShopAdapter adapter;

    private List<ExtensionItem> sourceItems;
    private String currentQuery = "";
    private final Set<Integer> selectedCategoryResIds = new HashSet<>();
    private ExtensionsCatalogManager.SortMode currentSortMode = ExtensionsCatalogManager.SortMode.POPULAR;
    private boolean isUpdatingCategorySelection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.extensions_shop);

        initViews();
        setupWindowInsets();
        setupToolbar();
        setupRecyclerView();
        setupBottomNavigation();
        setupSearch();
        setupCategoryFilter();
        setupSorting();
        applyFilters();
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
        adapter = new ExtensionsShopAdapter(this::openExtension);
        recyclerView.setAdapter(adapter);
        sourceItems = ExtensionsCatalog.getMockExtensions();
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

    private void handleAddExtensionClick() {
        SettingsStorage storage = new SettingsStorage(getApplicationContext());
        if (!storage.isAccountCreated()) {
            Snackbar.make(root, R.string.extension_shop_login_required, Snackbar.LENGTH_SHORT).show();
            startActivity(new Intent(this, SettingsActivity.class));
            return;
        }

        Snackbar.make(root, R.string.extension_upload_later, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}
