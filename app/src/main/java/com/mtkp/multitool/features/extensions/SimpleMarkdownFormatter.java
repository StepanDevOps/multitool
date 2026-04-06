package com.mtkp.multitool.features.extensions;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BulletSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Простой форматтер markdown для демонстрации.
 * <p>
 * Нам не нужен полноценный рендерер, но хочется красиво показать
 * заголовки, списки и жирный текст в mock-описании расширения.
 */
public final class SimpleMarkdownFormatter {

    private static final Pattern BOLD_PATTERN = Pattern.compile("\\*\\*(.+?)\\*\\*");

    private SimpleMarkdownFormatter() {
        // Утилитный класс.
    }

    /**
     * Применяем упрощённую markdown-разметку к TextView.
     */
    public static void apply(TextView textView, String markdownText) {
        if (textView == null) {
            return;
        }

        if (markdownText == null || markdownText.trim().isEmpty()) {
            textView.setText("");
            return;
        }

        SpannableStringBuilder builder = new SpannableStringBuilder();
        String[] lines = markdownText.split("\\r?\\n");

        for (String rawLine : lines) {
            String line = rawLine == null ? "" : rawLine;
            int start = builder.length();

            if (line.startsWith("# ")) {
                builder.append(line.substring(2)).append('\n');
                builder.setSpan(new StyleSpan(Typeface.BOLD), start, builder.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setSpan(new RelativeSizeSpan(1.35f), start, builder.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                continue;
            }

            if (line.startsWith("## ")) {
                builder.append(line.substring(3)).append('\n');
                builder.setSpan(new StyleSpan(Typeface.BOLD), start, builder.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setSpan(new RelativeSizeSpan(1.15f), start, builder.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                continue;
            }

            if (line.startsWith("- ")) {
                builder.append(line.substring(2)).append('\n');
                builder.setSpan(new BulletSpan(16), start, builder.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                continue;
            }

            builder.append(line).append('\n');
        }

        applyInlineBold(builder);
        textView.setText(builder);
    }

    private static void applyInlineBold(SpannableStringBuilder builder) {
        Matcher matcher = BOLD_PATTERN.matcher(builder);
        while (matcher.find()) {
            String fullMatch = matcher.group(0);
            String boldText = matcher.group(1);
            if (fullMatch == null || boldText == null) {
                continue;
            }

            int start = builder.toString().indexOf(fullMatch);
            while (start >= 0) {
                int end = start + fullMatch.length();
                builder.replace(start, end, boldText);
                builder.setSpan(new StyleSpan(Typeface.BOLD), start, start + boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                start = builder.toString().indexOf(fullMatch, start + boldText.length());
            }
            matcher = BOLD_PATTERN.matcher(builder);
        }
    }
}

