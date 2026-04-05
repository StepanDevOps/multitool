package com.mtkp.multitool.features.notes;

import com.mtkp.multitool.core.BasePresenter;
import com.mtkp.multitool.data.local.NoteEntity;
import java.util.ArrayList;
import java.util.List;

/**
 * Презентер экрана заметок.
 *
 * Пока здесь используется фейковый список, чтобы можно было
 * собрать и проверить интерфейс до подключения Room.
 */
public class NotesPresenter extends BasePresenter<NotesContract.View> implements NotesContract.Presenter {

    // Ссылка на репозиторий
    // Пока оставим тут пустой список для теста
    @Override
    public void loadNotes() {
        if (!isViewAttached()) return;

        view.showLoading(); // Говорим Данияру: "Покажи крутилку"

        // Имитируем загрузку данных
        List<NoteEntity> fakeNotes = new ArrayList<>();
        NoteEntity sampleNote = new NoteEntity();
        sampleNote.title = "Первая заметка";
        sampleNote.content = "Это временные данные до подключения Room.";
        fakeNotes.add(sampleNote);

        view.displayNotes(fakeNotes); // Отдаем данные на экран
        view.hideLoading(); // Скрываем крутилку
    }

    @Override
    public void onAddNoteClicked() {
        if (isViewAttached()) {
            view.navigateToAddNote();
        }
    }

    @Override
    public void attachView(NotesActivity notesActivity) {

    }
}