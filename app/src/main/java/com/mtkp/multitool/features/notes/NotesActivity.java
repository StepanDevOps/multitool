package com.mtkp.multitool.features.notes;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.mtkp.multitool.R;
import com.mtkp.multitool.data.local.NoteEntity;
import com.mtkp.multitool.features.settings.SettingsActivity;

import java.util.List;

public class NotesActivity extends AppCompatActivity implements NotesContract.View {

    private NotesContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        // Верхняя панель экрана заметок.
        Toolbar toolbar = findViewById(R.id.toolbar_notes);
        setSupportActionBar(toolbar);

        // Создаём презентер и связываем его с экраном.
        presenter = new NotesPresenter();

        // Сообщаем презентеру, что этот экран готов принимать команды.
        presenter.attachView(this);

        // Просим загрузить заметки для первого отображения.
        presenter.loadNotes();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Отвязываем экран, чтобы презентер не держал на него ссылку.
        presenter.detachView();
    }

    // Методы из NotesContract.View.

    @Override
    public void displayNotes(List<NoteEntity> notes) {
        // Позже здесь будет установка адаптера и обновление списка.
    }

    @Override
    public void navigateToAddNote() {

    }

    @Override
    public void showError(String message) {
        // Для простоты пока можно использовать Toast или Snackbar.
    }

    @Override
    public void showLoading() { /* показать индикатор загрузки */ }

    @Override
    public void hideLoading() { /* скрыть индикатор загрузки */ }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}