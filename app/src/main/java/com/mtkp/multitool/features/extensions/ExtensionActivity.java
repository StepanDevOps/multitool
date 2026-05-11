package com.mtkp.multitool.features.extensions;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.mtkp.multitool.R;
import com.mtkp.multitool.data.local.AppDatabase;
import com.mtkp.multitool.data.local.InstalledExtensionEntity;
import com.mtkp.multitool.data.repository.ExtensionsRepository;
import com.mtkp.multitool.data.remote.RemoteDataSource;
import com.mtkp.multitool.data.remote.dto.ExtensionDto;
import com.mtkp.multitool.data.remote.dto.RatingDto;
import com.mtkp.multitool.data.settings.SettingsStorage;
import com.mtkp.multitool.extensions.ExtensionDeveloperApi;
import com.mtkp.multitool.extensions.ExtensionManager;
import com.mtkp.multitool.extensions.LoadedExtension;
import com.mtkp.multitool.features.settings.SettingsActivity;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private MaterialButton editMetadataButton;
    private MaterialButton addReviewButton;
    private MaterialButton primaryActionButton;
    private MaterialButton secondaryActionButton;
    private RecyclerView reviewsRecyclerView;
    private TextView reviewsEmptyView;

    private ExtensionItem extensionItem;
    private ExtensionManager extensionManager;
    private ExtensionDeveloperApi developerApi;
    private RemoteDataSource remoteDataSource;
    private SettingsStorage settingsStorage;
    private AppDatabase appDatabase;
    private ExtensionsRepository extensionsRepository;
    private InstalledExtensionEntity installedExtensionEntity;
    private ExtensionDto remoteExtensionDto;
    private int requestedRemoteExtensionId = -1;
    private boolean isOwner;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final List<RatingDto> reviews = new ArrayList<>();
    private ReviewAdapter reviewAdapter;
    private final NumberFormat numberFormat = NumberFormat.getIntegerInstance(Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_extension);

        initViews();
        setupWindowInsets();
        extensionManager = new ExtensionManager(getApplicationContext());
        remoteDataSource = new RemoteDataSource(getApplicationContext());
        developerApi = new ExtensionDeveloperApi(remoteDataSource);
        settingsStorage = new SettingsStorage(getApplicationContext());
        appDatabase = AppDatabase.getInstance(getApplicationContext());
        extensionsRepository = new ExtensionsRepository(getApplicationContext());
        setupReviewsRecyclerView();
        extensionItem = resolveExtension();
        setupToolbar();
        if (requestedRemoteExtensionId >= 0) {
            loadRemoteExtensionDetails();
        } else {
            bindExtension();
            setupActions();
        }
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
        editMetadataButton = findViewById(R.id.btn_edit_metadata);
        addReviewButton = findViewById(R.id.btn_add_review);
        primaryActionButton = findViewById(R.id.btn_primary_action);
        secondaryActionButton = findViewById(R.id.btn_secondary_action);
        reviewsRecyclerView = findViewById(R.id.rv_extension_reviews);
        reviewsEmptyView = findViewById(R.id.tv_extension_reviews_empty);
    }

    private void setupReviewsRecyclerView() {
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter(reviews);
        reviewsRecyclerView.setAdapter(reviewAdapter);
        reviewsEmptyView.setVisibility(View.GONE);
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
        requestedRemoteExtensionId = parseRemoteExtensionId(extensionId);
        ExtensionItem item = extensionId == null ? null : ExtensionsCatalog.findById(extensionId);
        if (item == null) {
            item = ExtensionsCatalog.getMockExtensions().get(0);
        }
        return item;
    }

    private int parseRemoteExtensionId(String value) {
        try {
            return value == null ? -1 : Integer.parseInt(value);
        } catch (Exception ignored) {
            return -1;
        }
    }

    private void loadRemoteExtensionDetails() {
        executor.execute(() -> {
            try {
                remoteExtensionDto = remoteDataSource.fetchExtensionById(requestedRemoteExtensionId);
                installedExtensionEntity = appDatabase.installedExtensionDao().getByExtensionId(requestedRemoteExtensionId);
                boolean installed = installedExtensionEntity != null;
                boolean updateAvailable = installed && remoteExtensionDto.version != null
                        && !remoteExtensionDto.version.equalsIgnoreCase(installedExtensionEntity.installedVersion);
                extensionItem = buildUiModel(remoteExtensionDto, installed, updateAvailable);
                isOwner = remoteExtensionDto.authorId != null
                        && remoteExtensionDto.authorId == settingsStorage.getUserId();

                runOnUiThread(() -> {
                    bindExtension();
                    setupActions();
                    updateOwnerVisibility();
                    loadReviews();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    remoteExtensionDto = null;
                    installedExtensionEntity = null;
                    isOwner = false;
                    requestedRemoteExtensionId = -1;
                    Snackbar.make(root, getString(R.string.extension_action_mock_not_supported), Snackbar.LENGTH_SHORT).show();
                    bindExtension();
                    setupActions();
                    updateOwnerVisibility();
                });
            }
        });
    }

    private ExtensionItem buildUiModel(ExtensionDto dto, boolean installed, boolean updateAvailable) {
        int icon = resolveIconForRemote(dto.name);
        int installs = dto.downloads > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) dto.downloads;
        String shortDescription = dto.shortDescription == null ? "" : dto.shortDescription;
        String detailedDescription = dto.detailedDescription == null ? shortDescription : dto.detailedDescription;
        String author = dto.authorName == null ? getString(R.string.app_name) : dto.authorName;
        String version = dto.version == null ? "1.0.0" : dto.version;
        return new ExtensionItem(
                String.valueOf(dto.id),
                dto.name == null ? getString(R.string.extension_screen_title) : dto.name,
                author,
                version,
                installs,
                dto.rating,
                toCategoryResIds(dto.categories),
                shortDescription,
                detailedDescription,
                icon,
                installed,
                updateAvailable
        );
    }

    private int resolveIconForRemote(String name) {
        if (name == null) {
            return R.drawable.ic_account_box;
        }
        String normalized = name.toLowerCase(Locale.ROOT);
        if (normalized.contains("note")) return R.drawable.ic_notes;
        if (normalized.contains("weather")) return R.drawable.ic_home;
        if (normalized.contains("favorite")) return R.drawable.ic_favorite;
        return R.drawable.ic_account_box;
    }

    private int[] toCategoryResIds(List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return new int[]{R.string.category_other};
        }
        List<Integer> ids = new ArrayList<>();
        for (String category : categories) {
            String normalized = category == null ? "" : category.toLowerCase(Locale.ROOT);
            if (normalized.contains("product")) ids.add(R.string.category_productivity);
            else if (normalized.contains("personal")) ids.add(R.string.category_personalization);
            else if (normalized.contains("educ")) ids.add(R.string.category_education);
            else if (normalized.contains("media")) ids.add(R.string.category_media);
            else if (normalized.contains("util")) ids.add(R.string.category_utilities);
            else ids.add(R.string.category_other);
        }
        int[] res = new int[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            res[i] = ids.get(i);
        }
        return res;
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
        updateOwnerVisibility();

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

    private void updateOwnerVisibility() {
        boolean loggedIn = settingsStorage != null && !TextUtils.isEmpty(settingsStorage.getAuthToken());
        boolean canReview = loggedIn && requestedRemoteExtensionId >= 0;
        editMetadataButton.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        addReviewButton.setVisibility(canReview ? View.VISIBLE : View.GONE);
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
        editMetadataButton.setOnClickListener(v -> showEditMetadataDialog());
        addReviewButton.setOnClickListener(v -> showReviewDialog());
    }

    private void installOrUpdateExtension() {
        if (remoteExtensionDto == null) {
            showActionMessage(getString(R.string.extension_action_mock_not_supported));
            return;
        }

        setButtonsLoading(true);
        extensionsRepository.installAndActivate(
                remoteExtensionDto.id,
                remoteExtensionDto.version == null ? extensionItem.getVersion() : remoteExtensionDto.version,
                ExtensionManager.DEFAULT_ENTRY_CLASS,
                new ExtensionsRepository.ResultCallback<LoadedExtension>() {
                    @Override
                    public void onSuccess(LoadedExtension result) {
                        runOnUiThread(() -> {
                            setButtonsLoading(false);
                            Snackbar.make(root, getString(R.string.extension_action_install_success, result.displayName), Snackbar.LENGTH_LONG).show();
                            loadRemoteExtensionDetails();
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
        if (installedExtensionEntity == null) {
            showActionMessage(getString(R.string.extension_action_mock_not_supported));
            return;
        }

        setButtonsLoading(true);
        extensionsRepository.removeWithCleanup(installedExtensionEntity.id, new ExtensionsRepository.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                runOnUiThread(() -> {
                    setButtonsLoading(false);
                    Snackbar.make(root, result ? getString(R.string.extension_action_delete_success) : getString(R.string.extension_action_delete_not_found), Snackbar.LENGTH_SHORT).show();
                    loadRemoteExtensionDetails();
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

    private void loadReviews() {
        if (remoteExtensionDto == null) {
            return;
        }

        developerApi.getReviews(remoteExtensionDto.id, 1, 10, new ExtensionDeveloperApi.Callback<List<RatingDto>>() {
            @Override
            public void onSuccess(List<RatingDto> data) {
                runOnUiThread(() -> {
                    reviews.clear();
                    if (data != null) {
                        List<RatingDto> sorted = new ArrayList<>(data);
                        sorted.sort(Comparator.comparingLong(r -> -r.createdAt));
                        reviews.addAll(sorted);
                    }
                    reviewAdapter.notifyDataSetChanged();
                    reviewsEmptyView.setVisibility(reviews.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }

            @Override
            public void onError(Throwable throwable) {
                runOnUiThread(() -> {
                    reviews.clear();
                    reviewAdapter.notifyDataSetChanged();
                    reviewsEmptyView.setText(getString(R.string.extension_reviews_empty));
                    reviewsEmptyView.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void showEditMetadataDialog() {
        if (remoteExtensionDto == null || !isOwner) {
            showActionMessage(getString(R.string.extension_action_mock_not_supported));
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_extension_edit, null, false);
        TextInputEditText etName = dialogView.findViewById(R.id.et_extension_name);
        TextInputEditText etShort = dialogView.findViewById(R.id.et_extension_short_description);
        TextInputEditText etDetailed = dialogView.findViewById(R.id.et_extension_detailed_description);

        etName.setText(remoteExtensionDto.name);
        etShort.setText(remoteExtensionDto.shortDescription);
        etDetailed.setText(remoteExtensionDto.detailedDescription);

        new AlertDialog.Builder(this)
                .setTitle(R.string.extension_action_edit_metadata)
                .setView(dialogView)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String name = etName.getText() == null ? "" : etName.getText().toString().trim();
                    String shortDescription = etShort.getText() == null ? "" : etShort.getText().toString().trim();
                    String detailedDescription = etDetailed.getText() == null ? "" : etDetailed.getText().toString().trim();
                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(shortDescription)) {
                        Snackbar.make(root, getString(R.string.extension_edit_invalid_input), Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    developerApi.updateExtensionMetadata(
                            remoteExtensionDto.id,
                            name,
                            shortDescription,
                            detailedDescription,
                            remoteExtensionDto.categories,
                            new ExtensionDeveloperApi.Callback<ExtensionDto>() {
                                @Override
                                public void onSuccess(ExtensionDto data) {
                                    runOnUiThread(() -> {
                                        Snackbar.make(root, getString(R.string.extension_edit_success), Snackbar.LENGTH_SHORT).show();
                                        loadRemoteExtensionDetails();
                                    });
                                }

                                @Override
                                public void onError(Throwable throwable) {
                                    runOnUiThread(() -> Snackbar.make(root, getString(R.string.extension_edit_error, throwable.getMessage()), Snackbar.LENGTH_LONG).show());
                                }
                            }
                    );
                })
                .show();
    }

    private void showReviewDialog() {
        if (remoteExtensionDto == null || TextUtils.isEmpty(settingsStorage.getAuthToken())) {
            showActionMessage(getString(R.string.extension_action_mock_not_supported));
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_extension_review, null, false);
        MaterialButtonToggleGroup ratingGroup = dialogView.findViewById(R.id.toggle_review_rating);
        TextInputEditText etReview = dialogView.findViewById(R.id.et_review_text);
        ratingGroup.check(R.id.chip_rating_5);

        new AlertDialog.Builder(this)
                .setTitle(R.string.extension_action_add_review)
                .setView(dialogView)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    int rating = resolveSelectedRating(ratingGroup);
                    String review = etReview.getText() == null ? "" : etReview.getText().toString().trim();

                    developerApi.submitReview(remoteExtensionDto.id, rating, review, new ExtensionDeveloperApi.Callback<RatingDto>() {
                        @Override
                        public void onSuccess(RatingDto data) {
                            runOnUiThread(() -> {
                                Snackbar.make(root, getString(R.string.extension_review_success), Snackbar.LENGTH_SHORT).show();
                                loadReviews();
                            });
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            runOnUiThread(() -> Snackbar.make(root, getString(R.string.extension_review_error, throwable.getMessage()), Snackbar.LENGTH_LONG).show());
                        }
                    });
                })
                .show();
    }

    private int resolveSelectedRating(MaterialButtonToggleGroup group) {
        int checkedId = group.getCheckedButtonId();
        if (checkedId == R.id.chip_rating_1) return 1;
        if (checkedId == R.id.chip_rating_2) return 2;
        if (checkedId == R.id.chip_rating_3) return 3;
        if (checkedId == R.id.chip_rating_4) return 4;
        return 5;
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
        editMetadataButton.setEnabled(!loading);
        addReviewButton.setEnabled(!loading);
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

    private class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

        private final List<RatingDto> items;
        private final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());

        ReviewAdapter(List<RatingDto> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_extension_review, parent, false);
            return new ReviewViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
            holder.bind(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ReviewViewHolder extends RecyclerView.ViewHolder {

            private final TextView authorView;
            private final TextView ratingView;
            private final TextView textView;

            ReviewViewHolder(@NonNull View itemView) {
                super(itemView);
                authorView = itemView.findViewById(R.id.tv_review_author);
                ratingView = itemView.findViewById(R.id.tv_review_rating);
                textView = itemView.findViewById(R.id.tv_review_text);
            }

            void bind(RatingDto review) {
                String authorName = review.authorUsername != null && !review.authorUsername.isEmpty()
                    ? review.authorUsername
                    : ("User #" + review.userId);
                authorView.setText(authorName);
                ratingView.setText(review.rating + "/5");
                String reviewText = TextUtils.isEmpty(review.review) ? "—" : review.review;
                textView.setText(reviewText + "\n" + dateFormat.format(new Date(review.createdAt)));
            }
        }
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

