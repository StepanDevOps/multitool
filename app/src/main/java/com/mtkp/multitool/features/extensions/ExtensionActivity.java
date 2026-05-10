package com.mtkp.multitool.features.extensions;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.mtkp.multitool.R;
import com.mtkp.multitool.extensions.ExtensionManager;
import com.mtkp.multitool.extensions.LoadedExtension;
import com.mtkp.multitool.features.settings.SettingsActivity;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Экран одного расширения.
 * <p>
 * Здесь показываем mock-информацию: логотип, заголовок, автора, версию,
 * краткое описание и упрощённый markdown-блок.
 */
public class ExtensionActivity extends AppCompatActivity {

    public static final String EXTRA_EXTENSION_ID = "extra_extension_id";

    private View root;
    private View appBarLayout;
    private Toolbar toolbar;
    private ImageView iconView;
    private TextView titleView;
    private TextView authorView;
    private TextView versionView;
    private TextView categoryView;
    private TextView installsView;
    private TextView ratingView;
    private TextView shortDescriptionView;
    private TextView markdownDescriptionView;
    private MaterialButton primaryActionButton;
    private MaterialButton secondaryActionButton;

    private ExtensionItem extensionItem;
    private ExtensionManager extensionManager;
    private final NumberFormat numberFormat = NumberFormat.getIntegerInstance(Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_extension);

        initViews();
        setupWindowInsets();
        extensionItem = resolveExtension();
        extensionManager = new ExtensionManager(getApplicationContext());
        setupToolbar();
        bindExtension();
        setupActions();
    }

    private void initViews() {
        root = findViewById(R.id.extension_root);
        appBarLayout = findViewById(R.id.appbar_extension);
        toolbar = findViewById(R.id.toolbar_extension);
        iconView = findViewById(R.id.iv_extension_header_icon);
        titleView = findViewById(R.id.tv_extension_title);
        authorView = findViewById(R.id.tv_extension_author);
        versionView = findViewById(R.id.tv_extension_version);
        categoryView = findViewById(R.id.tv_extension_category);
        installsView = findViewById(R.id.tv_extension_installs);
        ratingView = findViewById(R.id.tv_extension_rating);
        shortDescriptionView = findViewById(R.id.tv_extension_short_description);
        markdownDescriptionView = findViewById(R.id.tv_extension_markdown_description);
        primaryActionButton = findViewById(R.id.btn_primary_action);
        secondaryActionButton = findViewById(R.id.btn_secondary_action);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
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

    private ExtensionItem resolveExtension() {
        String extensionId = getIntent().getStringExtra(EXTRA_EXTENSION_ID);
        ExtensionItem item = extensionId == null ? null : ExtensionsCatalog.findById(extensionId);
        if (item == null) {
            item = ExtensionsCatalog.getMockExtensions().get(0);
        }
        return item;
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.extension_screen_title);
        }
    }

    private void bindExtension() {
        iconView.setImageResource(extensionItem.getIconResId());
        titleView.setText(extensionItem.getTitle());
        authorView.setText(getString(R.string.extension_author_value, extensionItem.getAuthor()));
        versionView.setText(getString(R.string.extension_version_value, extensionItem.getVersion()));
        categoryView.setText(getString(R.string.extension_category_value, formatCategories(extensionItem.getCategoryResIds())));
        installsView.setText(getString(R.string.extension_installs_value, numberFormat.format(extensionItem.getInstalls())));
        ratingView.setText(getString(R.string.extension_rating_value, extensionItem.getRating()));
        shortDescriptionView.setText(extensionItem.getShortDescription());
        SimpleMarkdownFormatter.apply(markdownDescriptionView, extensionItem.getMarkdownDescription());

        updateActionButtons();
    }

    private void updateActionButtons() {
        if (!extensionItem.isInstalled()) {
            primaryActionButton.setText(R.string.extension_action_install);
            secondaryActionButton.setVisibility(View.GONE);
        } else if (extensionItem.isUpdateAvailable()) {
            primaryActionButton.setText(R.string.extension_action_update);
            secondaryActionButton.setText(R.string.extension_action_delete);
            secondaryActionButton.setVisibility(View.VISIBLE);
        } else {
            primaryActionButton.setText(R.string.extension_action_check_updates);
            secondaryActionButton.setText(R.string.extension_action_delete);
            secondaryActionButton.setVisibility(View.VISIBLE);
        }
    }

    private void setupActions() {
        primaryActionButton.setOnClickListener(v -> {
            if (!extensionItem.isInstalled()) {
                installOrUpdateExtension();
            } else if (extensionItem.isUpdateAvailable()) {
                installOrUpdateExtension();
            } else {
                showActionMessage(getString(R.string.extension_action_check_updates));
            }
        });

        secondaryActionButton.setOnClickListener(v -> deleteExtensionBinary());
    }

    private void installOrUpdateExtension() {
        int extensionId = resolveRemoteExtensionId();
        if (extensionId < 0) {
            showActionMessage(getString(R.string.extension_action_mock_not_supported));
            return;
        }

        setButtonsLoading(true);
        extensionManager.downloadAndLoad(
                extensionId,
                extensionItem.getVersion(),
                ExtensionManager.DEFAULT_ENTRY_CLASS,
                new ExtensionManager.Callback<LoadedExtension>() {
                    @Override
                    public void onSuccess(LoadedExtension result) {
                        runOnUiThread(() -> {
                            setButtonsLoading(false);
                            Snackbar.make(
                                    root,
                                    getString(R.string.extension_action_install_success, result.displayName),
                                    Snackbar.LENGTH_LONG
                            ).show();
                        });
                    }

                    @Override
                    public void onError(Throwable t) {
                        runOnUiThread(() -> {
                            setButtonsLoading(false);
                            Snackbar.make(
                                    root,
                                    getString(R.string.extension_action_install_error, t.getMessage()),
                                    Snackbar.LENGTH_LONG
                            ).show();
                        });
                    }
                }
        );
    }

    private void deleteExtensionBinary() {
        int extensionId = resolveRemoteExtensionId();
        if (extensionId < 0) {
            showActionMessage(getString(R.string.extension_action_mock_not_supported));
            return;
        }

        File file = new File(getFilesDir(), "extensions/" + extensionId + "-" + extensionItem.getVersion() + ".jar");
        setButtonsLoading(true);
        extensionManager.uninstall(file, new ExtensionManager.Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                runOnUiThread(() -> {
                    setButtonsLoading(false);
                    Snackbar.make(root, result
                            ? getString(R.string.extension_action_delete_success)
                            : getString(R.string.extension_action_delete_not_found), Snackbar.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(Throwable t) {
                runOnUiThread(() -> {
                    setButtonsLoading(false);
                    Snackbar.make(root, getString(R.string.extension_action_delete_error, t.getMessage()), Snackbar.LENGTH_LONG).show();
                });
            }
        });
    }

    private int resolveRemoteExtensionId() {
        try {
            return Integer.parseInt(extensionItem.getId());
        } catch (Exception ignored) {
            return -1;
        }
    }

    private void setButtonsLoading(boolean loading) {
        primaryActionButton.setEnabled(!loading);
        secondaryActionButton.setEnabled(!loading);
    }

    private String formatCategories(int[] categoryResIds) {
        if (categoryResIds == null || categoryResIds.length == 0) {
            return getString(R.string.category_other);
        }

        List<String> names = new ArrayList<>();
        for (int categoryResId : categoryResIds) {
            names.add(getString(categoryResId));
        }
        return TextUtils.join(", ", names);
    }

    private void showActionMessage(String action) {
        Snackbar.make(root, getString(R.string.extension_action_placeholder, action), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_extension_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        if (item.getItemId() == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

