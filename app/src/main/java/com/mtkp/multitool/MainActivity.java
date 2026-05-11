package com.mtkp.multitool;

import android.os.Bundle;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mtkp.multitool.data.local.AppDatabase;
import com.mtkp.multitool.data.local.CachedExtensionEntity;
import com.mtkp.multitool.data.local.InstalledExtensionEntity;
import com.mtkp.multitool.features.extensions.ExtensionAdapter;
import com.mtkp.multitool.features.extensions.ExtensionMenuManager;
import com.mtkp.multitool.features.extensions.ExtensionsBottomSheetFragment;
import com.mtkp.multitool.features.extensions.ExtensionActivity;
import com.mtkp.multitool.features.extensions.ExtensionsShopActivity;
import com.mtkp.multitool.features.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Главный экран приложения.
 * <p>
 * На нём показываются карточки расширений, нижняя навигация
 * и кнопка перехода в настройки.
 */
public class MainActivity extends AppCompatActivity {

    private RecyclerView rvExtensions;
    private BottomNavigationView bottomNavigation;
    private Toolbar toolbar;
    private final List<ExtensionAdapter.Extension> extensionList = new ArrayList<>();
    private ExtensionAdapter extensionAdapter;
    private AppDatabase appDatabase;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Применить edge-to-edge
        View rootView = findViewById(R.id.main);
        View appBarLayout = findViewById(R.id.appBarLayout);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            appBarLayout.setPadding(
                    appBarLayout.getPaddingLeft(),
                    systemBars.top,
                    appBarLayout.getPaddingRight(),
                    appBarLayout.getPaddingBottom()
            );
            return insets;
        });
        ViewCompat.requestApplyInsets(rootView);

        // Инициализация элементов UI
        initializeViews();
        setupRecyclerView();
        setupBottomNavigation();
        setupToolbar();
        appDatabase = AppDatabase.getInstance(getApplicationContext());
        loadInstalledExtensions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInstalledExtensions();
    }

    /**
     * Инициализация всех элементов UI
     */
    private void initializeViews() {
        rvExtensions = findViewById(R.id.rv_extensions);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        toolbar = findViewById(R.id.toolbar);
    }

    /**
     * Настройка RecyclerView с GridLayoutManager (3 колонки)
     */
    private void setupRecyclerView() {
        // Сетка карточек расширений на главном экране.
        // Позже количество колонок можно будет подстраивать под размер экрана.
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        rvExtensions.setLayoutManager(gridLayoutManager);

        // Адаптер связывает список данных с карточками на экране.
        extensionAdapter = new ExtensionAdapter(extensionList, new ExtensionAdapter.OnExtensionActionListener() {
            @Override
            public void onExtensionClicked(ExtensionAdapter.Extension extension) {
                openExtension(extension);
            }

            @Override
            public void onEditMenuClicked(ExtensionAdapter.Extension extension, View anchorView) {
                showEditMenu(extension, anchorView);
            }

            @Override
            public void onAddNewExtension() {
                Toast.makeText(MainActivity.this, "Add new extension", Toast.LENGTH_SHORT).show();
                // TODO: Открыть диалог для добавления нового расширения
            }
        });

        rvExtensions.setAdapter(extensionAdapter);
    }

    private void loadInstalledExtensions() {
        executor.execute(() -> {
            List<ExtensionAdapter.Extension> mapped = new ArrayList<>();
            List<InstalledExtensionEntity> installed = appDatabase.installedExtensionDao().getAll();
            for (InstalledExtensionEntity entity : installed) {
                if (entity == null || entity.isHidden) {
                    continue;
                }

                CachedExtensionEntity cached = appDatabase.cachedExtensionDao().getById(entity.extensionId);
                String title = cached != null && cached.name != null && !cached.name.isEmpty()
                        ? cached.name
                        : String.format(Locale.getDefault(), "Extension %d", entity.extensionId);
                String description = cached != null && cached.shortDescription != null
                        ? cached.shortDescription
                        : entity.installedVersion;
                int icon = resolveIconForExtension(entity.extensionId, title);
                mapped.add(new ExtensionAdapter.Extension(
                        String.valueOf(entity.extensionId),
                        title,
                        description,
                        icon
                ));
            }

            runOnUiThread(() -> {
                extensionList.clear();
                extensionList.addAll(mapped);
                extensionAdapter.notifyDataSetChanged();
            });
        });
    }

    private int resolveIconForExtension(int extensionId, String title) {
        if (title != null) {
            String normalized = title.toLowerCase(Locale.ROOT);
            if (normalized.contains("note")) return R.drawable.ic_notes;
            if (normalized.contains("weather")) return R.drawable.ic_home;
            if (normalized.contains("favorite")) return R.drawable.ic_favorite;
        }
        switch (extensionId % 4) {
            case 0: return R.drawable.ic_notes;
            case 1: return R.drawable.ic_home;
            case 2: return R.drawable.ic_favorite;
            default: return R.drawable.ic_account_box;
        }
    }

    /**
     * Настройка BottomNavigationView
     */
    private void setupBottomNavigation() {
        // Нижнее меню: главная, список расширений и магазин.
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Главная страница (текущая)
                return true;
            } else if (itemId == R.id.nav_extensions) {
                // Открыть BottomSheet со списком расширений
                showInstalledExtensionsBottomSheet();
                return false;
            } else if (itemId == R.id.nav_store) {
                // Открыть экран магазина расширений.
                startActivity(new Intent(this, ExtensionsShopActivity.class));
                overridePendingTransition(0, 0);
                // Не оставляем выбранным store на экране, с которого уходим.
                return false;
            }
            return false;
        });
    }


    /**
     * Настройка Toolbar с меню
     */
    private void setupToolbar() {
        // Подключаем Toolbar как верхнюю панель экрана.
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Показать всплывающее меню редактирования для карточки
     */
    private void showEditMenu(ExtensionAdapter.Extension extension, View anchorView) {
        // Всплывающее меню для действий над карточкой расширения.
        ExtensionMenuManager.showEditMenu(this, anchorView, new ExtensionMenuManager.OnMenuItemClickListener() {
            @Override
            public void onResize() {
                Toast.makeText(MainActivity.this, "Resize " + extension.getName(), Toast.LENGTH_SHORT).show();
                // TODO: Реализовать логику изменения размера
            }

            @Override
            public void onDelete() {
                Toast.makeText(MainActivity.this, "Delete " + extension.getName(), Toast.LENGTH_SHORT).show();
                // TODO: Подключить локальное удаление/деактивацию через repository
            }

            @Override
            public void onClose() {
                Toast.makeText(MainActivity.this, "Menu closed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Показать BottomSheet со списком установленных расширений
     */
    private void showInstalledExtensionsBottomSheet() {
        // Меню поверх экрана с простым списком установленных расширений.
        ExtensionsBottomSheetFragment bottomSheet = ExtensionsBottomSheetFragment.newInstance();
        bottomSheet.show(getSupportFragmentManager(), "extensions_bottom_sheet");
    }

    private void openExtension(ExtensionAdapter.Extension extension) {
        Intent intent = new Intent(this, ExtensionActivity.class);
        intent.putExtra(ExtensionActivity.EXTRA_EXTENSION_ID, extension.getId());
        startActivity(intent);
    }
}