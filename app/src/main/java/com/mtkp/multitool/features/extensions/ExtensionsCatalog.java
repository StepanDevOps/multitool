package com.mtkp.multitool.features.extensions;

import com.mtkp.multitool.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Заглушка каталога расширений.
 * <p>
 * В будущем вместо этого класса здесь будет загрузка данных из JAR/API,
 * а пока он отдаёт готовый список mock-расширений для интерфейса.
 */
public final class ExtensionsCatalog {

    private ExtensionsCatalog() {
        // Утилитный класс.
    }

    /**
     * Возвращает список mock-расширений для магазина.
     */
    public static List<ExtensionItem> getMockExtensions() {
        List<ExtensionItem> items = new ArrayList<>();

        items.add(new ExtensionItem(
                "notes_plus",
                "Notes+",
                "Stepan Lab",
                "1.4.2",
                12450,
                4.8f,
                R.string.category_productivity,
                "Fast notes with checklists, tags and a tiny focus mode.",
                "# Notes+\n\n## Что умеет\n- Быстрые заметки\n- Списки задач\n- Теги и закрепление\n\n**Плюс для учёбы:** удобный шаблон для будущего API-плагина.",
                R.drawable.ic_notes,
                true,
                true
        ));

        items.add(new ExtensionItem(
                "weather_now",
                "Weather Now",
                "Yaroslav Studio",
                "2.1.0",
                8930,
                4.6f,
                R.string.category_utilities,
                "Simple weather card with quick forecast and current conditions.",
                "# Weather Now\n\n- Температура на сегодня\n- Прогноз на 5 дней\n- Лаконичная карточка для главного экрана",
                R.drawable.ic_home,
                false,
                false
        ));

        items.add(new ExtensionItem(
                "focus_timer",
                "Focus Timer",
                "Daniyar UI",
                "1.0.7",
                4580,
                4.4f,
                R.string.category_productivity,
                "Pomodoro timer with soft sounds and full-screen focus mode.",
                "# Focus Timer\n\n## Режимы\n- Рабочий интервал\n- Короткий перерыв\n- Длинный перерыв\n\n**Совет:** подойдёт для учебных сессий и домашних заданий.",
                R.drawable.ic_favorite,
                true,
                false
        ));

        items.add(new ExtensionItem(
                "study_cards",
                "Study Cards",
                "Multitool Team",
                "3.0.1",
                6700,
                4.9f,
                R.string.category_education,
                "Flashcards for learning terms, formulas and small quizzes.",
                "# Study Cards\n\n- Карточки для запоминания\n- Мини-тесты\n- Простой шаблон для образовательных модулей",
                R.drawable.ic_account_box,
                true,
                false
        ));

        items.add(new ExtensionItem(
                "media_booster",
                "Media Booster",
                "Multitool Lab",
                "0.9.5",
                2140,
                4.1f,
                R.string.category_media,
                "Compact player helper with shortcuts for audio and video content.",
                "# Media Booster\n\n## Особенности\n- Быстрые действия\n- Удобные ярлыки\n- Заготовка под будущие медиа-плагины",
                R.drawable.ic_baseline_note_add_24,
                false,
                false
        ));

        items.add(new ExtensionItem(
                "design_pack",
                "Design Pack",
                "Stepan + Daniyar",
                "1.8.3",
                15200,
                4.7f,
                R.string.category_personalization,
                "Theme helpers and quick layout presets for visual experiments.",
                "# Design Pack\n\n- Визуальные пресеты\n- Наборы карточек\n- Быстрые темы оформления\n\n**Важно:** этот плагин демонстрирует будущую систему персонализации.",
                R.drawable.ic_more_vert,
                true,
                true
        ));

        items.add(new ExtensionItem(
                "misc_tools",
                "Misc Tools",
                "Open Lab",
                "1.2.0",
                3180,
                4.0f,
                R.string.category_other,
                "Small helper widgets for everything that does not fit other groups.",
                "# Misc Tools\n\n- Разные мелкие утилиты\n- Заготовки для тестов\n- Песочница для будущих идей",
                R.drawable.ic_home,
                false,
                false
        ));

        return Collections.unmodifiableList(items);
    }

    /**
     * Находим расширение по его идентификатору.
     */
    public static ExtensionItem findById(String id) {
        for (ExtensionItem item : getMockExtensions()) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }
}

