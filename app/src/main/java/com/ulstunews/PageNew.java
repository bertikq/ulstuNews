package com.ulstunews;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.ulstunews.models.NewModel;

public class PageNew extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Устанавливаем активити для страницы новости
        setContentView(R.layout.activity_page_new);
        // Достаем аргументы которые нам передал вызывающий класс
        Bundle arguments = getIntent().getExtras();
        if (arguments != null){
            // Собираем модель и отображаем на экране
            NewModel newModel = (NewModel) arguments.getSerializable("nm");
            ((TextView)findViewById(R.id.Title)).setText(newModel.getTitle());
            ((TextView)findViewById(R.id.Body)).setText(newModel.getBody());
        }
    }
}