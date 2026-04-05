package com.mtkp.multitool.features.notes;

import com.mtkp.multitool.core.BaseView;
import com.mtkp.multitool.data.local.NoteEntity;

import java.util.List;

/**
 * Контракт экрана заметок.
 * Через контракт мы заранее договариваемся,
 * какие методы нужны экрану и презентеру.
 */
public interface NotesContract {
    /**
     * Методы, которые нужны экрану заметок.
     * Presenter будет вызывать их, когда нужно обновить интерфейс.
     */
    interface View extends BaseView {
        /**
         * Показать список заметок на экране.
         */
        void displayNotes(List<NoteEntity> notes);

        /**
         * Перейти на экран добавления новой заметки.
         */
        void navigateToAddNote();
    }

    /**
     * Методы, которые принимает презентер.
     * Сюда приходят действия пользователя и запросы на загрузку данных.
     */
    interface Presenter {
        /**
         * Загрузить заметки и передать их на экран.
         */
        void loadNotes();

        /**
         * Реакция на нажатие кнопки добавления.
         */
        void onAddNoteClicked();

        /**
         * Привязать экран к презентеру.
         */
        void attachView(NotesActivity notesActivity);

        /**
         * Отвязать экран от презентера.
         */
        void detachView();
    }
}
