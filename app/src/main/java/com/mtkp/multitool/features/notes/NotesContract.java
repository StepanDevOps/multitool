package com.mtkp.multitool.features.notes;

import com.mtkp.multitool.core.BasePresenter;
import com.mtkp.multitool.core.BaseView;
import com.mtkp.multitool.data.local.NoteEntity;

import java.util.List;

// Правила взаимодействия
public interface NotesContract {
    interface View extends BaseView {
        void displayNotes(List<NoteEntity> notes); // Данияр реализует это (показ списка)
        void navigateToAddNote();                  // Переход на экран создания
    }

    interface Presenter {
        void loadNotes();          // Степан реализует это (запрос данных у Ярика)
        void onAddNoteClicked();   // Реакция на нажатие кнопки "+"

        void attachView(NotesActivity notesActivity);

        void detachView();
    }
}
