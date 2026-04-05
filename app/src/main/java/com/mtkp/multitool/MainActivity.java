package com.mtkp.multitool;

import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity - главный экран приложения
 * Отображает сетку установленных расширений, BottomNavigationView для навигации
 * и Toolbar с меню настроек.
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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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
        // Создание GridLayoutManager с 3 колонками
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        rvExtensions.setLayoutManager(gridLayoutManager);

        // Подготовка фейковых данных для тестирования
        List<ExtensionAdapter.Extension> extensionList = new ArrayList<>();
        extensionList.add(new ExtensionAdapter.Extension("Notes", "Note taking", R.drawable.ic_notes));
        extensionList.add(new ExtensionAdapter.Extension("Weather", "Weather info", R.drawable.ic_home));
        extensionList.add(new ExtensionAdapter.Extension("Favorites", "Favorites", R.drawable.ic_favorite));

        // Создание адаптера с обработчиком событий
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
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Главная страница (текущая)
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_extensions) {
                // Открыть BottomSheet со списком расширений
                showInstalledExtensionsBottomSheet();
                return true;
            } else if (itemId == R.id.nav_store) {
                // Открыть магазин расширений (будущая активность)
                Toast.makeText(this, "Extension Store (Coming Soon)", Toast.LENGTH_SHORT).show();
                // TODO: Открыть StoreActivity
                return true;
            }
            return false;
        });
    }

    /**
     * Настройка Toolbar с меню
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        // Меню инициализируется через menu_main.xml
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
            // Обработка нажатия на три точки (настройки)
            Toast.makeText(this, "Settings (Coming Soon)", Toast.LENGTH_SHORT).show();
            // TODO: Открыть SettingsActivity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Показать всплывающее меню редактирования для карточки
     */
    private void showEditMenu(ExtensionAdapter.Extension extension, View anchorView) {
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
        ExtensionsBottomSheetFragment bottomSheet = ExtensionsBottomSheetFragment.newInstance();
        bottomSheet.show(getSupportFragmentManager(), "extensions_bottom_sheet");
    }
}