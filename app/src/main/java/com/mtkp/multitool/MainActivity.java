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
import com.mtkp.multitool.features.extensions.ExtensionAdapter;
import com.mtkp.multitool.features.extensions.ExtensionMenuManager;
import com.mtkp.multitool.features.extensions.ExtensionsBottomSheetFragment;
import com.mtkp.multitool.features.extensions.ExtensionsShopActivity;
import com.mtkp.multitool.features.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

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

        // Фейковые данные, пока не подключили реальное хранилище.
        List<ExtensionAdapter.Extension> extensionList = new ArrayList<>();
        extensionList.add(new ExtensionAdapter.Extension("Notes", "Note taking", R.drawable.ic_notes));
        extensionList.add(new ExtensionAdapter.Extension("Weather", "Weather info", R.drawable.ic_home));
        extensionList.add(new ExtensionAdapter.Extension("Favorites", "Favorites", R.drawable.ic_favorite));

        // Адаптер связывает список данных с карточками на экране.
        ExtensionAdapter extensionAdapter = new ExtensionAdapter(extensionList, new ExtensionAdapter.OnExtensionActionListener() {
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
                // TODO: Реализовать логику удаления расширения
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
}