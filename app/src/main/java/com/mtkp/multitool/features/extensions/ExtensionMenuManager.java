package com.mtkp.multitool.features.extensions;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;
import com.mtkp.multitool.R;

/**
 * Класс для управления PopupMenu редактирования расширения.
 * Показывает меню с опциями масштабирования, удаления и закрытия.
 */
public class ExtensionMenuManager {

    public interface OnMenuItemClickListener {
        void onResize();
        void onDelete();
        void onClose();
    }

    /**
     * Показать PopupMenu для редактирования расширения
     *
     * @param context контекст
     * @param view вид, относительно которого показывать меню
     * @param listener обработчик событий меню
     */
    public static void showEditMenu(Context context, View view, OnMenuItemClickListener listener) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.inflate(R.menu.menu_edit_extension);

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_resize) {
                if (listener != null) {
                    listener.onResize();
                }
                return true;
            } else if (itemId == R.id.menu_delete) {
                if (listener != null) {
                    listener.onDelete();
                }
                return true;
            } else if (itemId == R.id.menu_close) {
                if (listener != null) {
                    listener.onClose();
                }
                return true;
            }
            return false;
        });

        popupMenu.show();
    }
}

