package com.mtkp.multitool.features.extensions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Небольшой набор переиспользуемых операций для каталога расширений.
 * <p>
 * Здесь отдельно живут поиск, фильтрация и сортировка, чтобы потом
 * эти же методы можно было подключить к другой активности.
 */
public final class ExtensionsCatalogManager {

    /**
     * Варианты сортировки каталога.
     */
    public enum SortMode {
        POPULAR,
        NEWEST,
        ALPHABETICAL
    }

    private ExtensionsCatalogManager() {
        // Утилитный класс.
    }

    /**
     * Фильтруем список по текстовому запросу.
     */
    public static List<ExtensionItem> filterByQuery(List<ExtensionItem> source, String query) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }

        String normalizedQuery = query == null ? "" : query.trim().toLowerCase();
        if (normalizedQuery.isEmpty()) {
            return new ArrayList<>(source);
        }

        List<ExtensionItem> result = new ArrayList<>();
        for (ExtensionItem item : source) {
            String title = item.getTitle().toLowerCase();
            String author = item.getAuthor().toLowerCase();
            String description = item.getShortDescription().toLowerCase();
            if (title.contains(normalizedQuery)
                    || author.contains(normalizedQuery)
                    || description.contains(normalizedQuery)) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * Фильтруем список по нескольким категориям.
     * <p>
     * Если список категорий пустой, фильтр отключён.
     */
    public static List<ExtensionItem> filterByCategories(List<ExtensionItem> source,
                                                         Set<Integer> selectedCategoryResIds) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }

        if (selectedCategoryResIds == null || selectedCategoryResIds.isEmpty()) {
            return new ArrayList<>(source);
        }

        List<ExtensionItem> result = new ArrayList<>();
        for (ExtensionItem item : source) {
            int[] categoryIds = item.getCategoryResIds();
            for (int id : categoryIds) {
                if (selectedCategoryResIds.contains(id)) {
                    result.add(item);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Сортируем список по выбранному правилу.
     */
    public static List<ExtensionItem> sort(List<ExtensionItem> source, SortMode sortMode) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }

        List<ExtensionItem> result = new ArrayList<>(source);
        if (sortMode == SortMode.NEWEST) {
            result.sort((left, right) -> right.getVersion().compareToIgnoreCase(left.getVersion()));
        } else if (sortMode == SortMode.ALPHABETICAL) {
            result.sort(Comparator.comparing(ExtensionItem::getTitle, String.CASE_INSENSITIVE_ORDER));
        } else {
            result.sort((left, right) -> Float.compare(right.getRating(), left.getRating()));
        }
        return result;
    }

    /**
     * Применяем сразу поиск, фильтр и сортировку.
     */
    public static List<ExtensionItem> applyAll(List<ExtensionItem> source,
                                               String query,
                                               Set<Integer> selectedCategoryResIds,
                                               SortMode sortMode) {
        List<ExtensionItem> result = filterByQuery(source, query);
        result = filterByCategories(result, selectedCategoryResIds);
        return sort(result, sortMode);
    }
}

