package com.mtkp.multitool.features.settings;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.os.LocaleListCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.mtkp.multitool.R;

/**
 * Экран настроек приложения.
 *
 * Здесь находится только UI: переключатели темы и языка,
 * форма профиля и серверная авторизация/регистрация.
 */
public class SettingsActivity extends AppCompatActivity implements SettingsContract.View {

    private static final String TAG = "SettingsActivity";

    private SettingsPresenter presenter;
    private boolean isRestoringState;

    private AppBarLayout appBarLayout;
    private Toolbar toolbar;
    private RadioGroup rgTheme;
    private RadioGroup rgLanguage;
    private TextInputLayout tilUserName;
    private EditText etUserName;
    private ImageView ivAvatarPreview;
    private TextView tvAccountState;
    private TextView tvSectionTitle;
    private android.view.View profileContainer;
    private android.view.View accountContainer;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private MaterialButton btnLogin;

    private final int[] avatarResIds = {
            R.drawable.ic_account_box,
            R.drawable.ic_favorite,
            R.drawable.ic_home
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        // Presenter сам ходит в repository: локальные настройки и серверная авторизация.
        presenter = new SettingsPresenter(getApplicationContext());
        presenter.attachView(this);

        initViews();
        setupEdgeToEdgeInsets();
        setupToolbar();
        setupListeners();
        presenter.loadSettings();
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();
        super.onDestroy();
    }

    private void initViews() {
        appBarLayout = findViewById(R.id.appbar_settings);
        toolbar = findViewById(R.id.toolbar_settings);
        rgTheme = findViewById(R.id.rg_theme);
        rgLanguage = findViewById(R.id.rg_language);
        tilUserName = findViewById(R.id.til_user_name);
        etUserName = findViewById(R.id.et_user_name);
        ivAvatarPreview = findViewById(R.id.iv_avatar_preview);
        tvAccountState = findViewById(R.id.tv_account_state);
        tvSectionTitle = findViewById(R.id.tv_section_title);
        profileContainer = findViewById(R.id.profile_container);
        accountContainer = findViewById(R.id.account_container);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnLogin = findViewById(R.id.btn_login);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.settings);
        }
    }

    private void setupEdgeToEdgeInsets() {
        android.view.View root = findViewById(R.id.settings_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
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

    private void setupListeners() {
        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            if (isRestoringState) {
                return;
            }
            int selectedMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            if (checkedId == R.id.rb_theme_light) {
                selectedMode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (checkedId == R.id.rb_theme_dark) {
                selectedMode = AppCompatDelegate.MODE_NIGHT_YES;
            }
            presenter.onThemeSelected(selectedMode);
        });

        rgLanguage.setOnCheckedChangeListener((group, checkedId) -> {
            if (isRestoringState) {
                return;
            }
            String language;
            if (checkedId == R.id.rb_language_en) {
                language = "en";
            } else if (checkedId == R.id.rb_language_ru) {
                language = "ru";
            } else {
                return;
            }
            presenter.onLanguageSelected(language);
        });

        etUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isRestoringState) {
                    return;
                }
                presenter.onUserNameChanged(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        MaterialButton btnPickAvatar = findViewById(R.id.btn_pick_avatar);
        btnPickAvatar.setOnClickListener(v -> showAvatarPickerDialog());

        MaterialButton btnUseSystemAvatar = findViewById(R.id.btn_system_avatar);
        btnUseSystemAvatar.setOnClickListener(v ->
                showMessage(getString(R.string.system_avatar_unavailable)));

        MaterialButton btnCreateAccount = findViewById(R.id.btn_create_account);
        btnCreateAccount.setOnClickListener(v -> {
            Log.d(TAG, "Create account clicked");
            presenter.onCreateAccountClicked(
                    etUserName.getText() == null ? "" : etUserName.getText().toString().trim(),
                    etEmail.getText() == null ? "" : etEmail.getText().toString().trim(),
                    etPassword.getText() == null ? "" : etPassword.getText().toString(),
                    etConfirmPassword.getText() == null ? "" : etConfirmPassword.getText().toString()
            );
        });

        btnLogin.setOnClickListener(v -> {
            Log.d(TAG, "Login clicked");
            presenter.onLoginClicked(
                    etEmail.getText() == null ? "" : etEmail.getText().toString().trim(),
                    etPassword.getText() == null ? "" : etPassword.getText().toString()
            );
        });

        MaterialButton btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> presenter.onLogoutClicked());
    }

    private void showAvatarPickerDialog() {
        String[] avatars = {
                getString(R.string.avatar_default),
                getString(R.string.avatar_star),
                getString(R.string.avatar_home)
        };

        new AlertDialog.Builder(this)
                .setTitle(R.string.choose_avatar)
                .setItems(avatars, (dialog, which) -> presenter.onAvatarSelected(avatarResIds[which]))
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        if (item.getItemId() == R.id.menu_settings) {
            showMessage(getString(R.string.already_on_settings_screen));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showCurrentSettings(int themeMode, String languageTag, String userName,
                                    int avatarResId, boolean accountCreated) {
        isRestoringState = true;

        if (themeMode == AppCompatDelegate.MODE_NIGHT_NO) {
            rgTheme.check(R.id.rb_theme_light);
        } else if (themeMode == AppCompatDelegate.MODE_NIGHT_YES) {
            rgTheme.check(R.id.rb_theme_dark);
        } else {
            rgTheme.check(R.id.rb_theme_system);
        }

        if ("ru".equals(languageTag)) {
            rgLanguage.check(R.id.rb_language_ru);
        } else {
            rgLanguage.check(R.id.rb_language_en);
        }

        etUserName.setText(userName);
        updateAvatarPreview(avatarResId);
        showAccountCreatedState(accountCreated);

        isRestoringState = false;
    }

    @Override
    public void applyTheme(int themeMode) {
        // Не делаем fade-to-black: в режиме "Как в системе" пересоздание может не произойти.
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }

    @Override
    public void applyLanguage(String languageTag) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag));
    }

    @Override
    public void showUserNameError() {
        tilUserName.setError(getString(R.string.user_name_error_length));
    }

    @Override
    public void clearUserNameError() {
        tilUserName.setError(null);
    }

    @Override
    public void updateAvatarPreview(int avatarResId) {
        ivAvatarPreview.setImageResource(avatarResId);
    }

    @Override
    public void showAccountCreatedState(boolean accountCreated) {
        tvSectionTitle.setText(accountCreated
                ? R.string.profile_section
                : R.string.create_account);

        tvAccountState.setVisibility(android.view.View.GONE);

        profileContainer.setVisibility(accountCreated ? android.view.View.VISIBLE : android.view.View.GONE);
        accountContainer.setVisibility(accountCreated ? android.view.View.GONE : android.view.View.VISIBLE);
    }

    @Override
    public void showInvalidEmailError() {
        showError(getString(R.string.error_invalid_email));
    }

    @Override
    public void showWeakPasswordError() {
        showError(getString(R.string.error_weak_password));
    }

    @Override
    public void showPasswordMismatchError() {
        showError(getString(R.string.error_password_mismatch));
    }

    @Override
    public void showAccountCreatedMessage() {
        showMessage(getString(R.string.account_created_local));
    }

    @Override
    public void showLoggedInMessage() {
        showMessage(getString(R.string.logged_in_local));
    }

    @Override
    public void showLoggedOutMessage() {
        showMessage(getString(R.string.logged_out_local));
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showError(String message) {
        showMessage(message);
    }

    @Override
    public void showLoading() {
    }

    @Override
    public void hideLoading() {
    }
}




