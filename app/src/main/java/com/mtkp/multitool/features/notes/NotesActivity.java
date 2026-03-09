package com.mtkp.multitool.features.notes;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.mtkp.multitool.R;
import com.mtkp.multitool.data.local.NoteEntity;
import java.util.List;

public class NotesActivity extends AppCompatActivity implements NotesContract.View {

    private NotesContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        // 1. Создаем экземпляр презентера (твой код)
        presenter = new NotesPresenter();

        // 2. ГОВОРИМ ПРЕЗЕНТЕРУ: "Я твой экран, командуй мной!"
        presenter.attachView(this);

        // Теперь можно попросить его загрузить данные
        presenter.loadNotes();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 3. ВАЖНО: "Я закрываюсь, забудь про меня"
        // Это защищает от вылетов, если данные придут, когда экран уже закрыт
        presenter.detachView();
    }

    // --- Реализация методов из NotesContract.View ---

    @Override
    public void displayNotes(List<NoteEntity> notes) {
        // Здесь Данияр напишет код, который обновит список на экране
    }

    @Override
    public void navigateToAddNote() {

    }

    @Override
    public void showError(String message) {
        // Здесь можно показать Toast (всплывающее уведомление)
    }

    @Override
    public void showLoading() { /* показать крутилку */ }

    @Override
    public void hideLoading() { /* скрыть крутилку */ }
}