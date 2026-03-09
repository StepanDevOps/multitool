package com.mtkp.multitool.features.notes;

import com.mtkp.multitool.core.BasePresenter;
import com.mtkp.multitool.data.local.NoteEntity;
import java.util.ArrayList;
import java.util.List;

/**
 * Имплементация презентера для заметок.
 * Наследуемся от BasePresenter и реализуем методы нашего Контракта.
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
        // fakeNotes.add(new NoteEntity("Купить молоко", "Срочно!"));

        if (fakeNotes.isEmpty()) {
            // Если пусто — можно вызвать метод для показа картинки "Пусто"
        } else {
            view.displayNotes(fakeNotes); // Отдаем данные на экран
        }

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