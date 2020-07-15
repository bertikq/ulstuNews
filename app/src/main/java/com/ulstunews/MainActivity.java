package com.ulstunews;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.pdf.PdfDocument;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ulstunews.models.NewModel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.Console;
import java.lang.annotation.Documented;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> listTitle = new ArrayList<>();
    // Название акшиона для вызова второго активити
    public static final String ACTION ="com.eugene.SHOW_SECOND_ACTIVITY";

    private HashMap<String, NewModel> newModels = new HashMap<>();


    private DatabaseHelper databaseHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(this);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.list);
        // Создаем адаптер для отображение заголовков новостей
        adapter = new ArrayAdapter<>(this, R.layout.item_layout, R.id.item, listTitle);

        // Проверяем на наличие интернета
        if (CheckInternetConnection()) {
            // Если есть интернет, то грузим новости с сайта
            new ReadNews_Async().execute();
        }
        else {
            // Если нет интернета, то грузим новости с бд
            new ReadNewsFromDB_Async().execute();
        }
    }

    protected boolean CheckInternetConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            return true;
        }

        return false;
    }



    protected class ReadNews_Async extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... strings) {
            try {
                // Открываем подлкючение к бд
                db = databaseHelper.getReadableDatabase();
                // Получаем html страницу
                Document doc = Jsoup.connect("http://is.ulstu.ru/News?page=0").get();
                // Достаем нужный элемент из html по классу
                Elements elementsNews = doc.select(".news");
                for (Element elementNew : elementsNews){
                    // Берем нужные значения
                    Elements news_title = elementNew.select(".news-title");
                    String title = news_title.text();
                    String title_author = elementNew.select(".news-author-title").text();
                    // Открываем страницу новости и берем контент
                    Document docNew = Jsoup.connect("http://is.ulstu.ru/" + news_title.get(0).select("a").attr("href")).get();
                    String body = docNew.select(".news-body").text();

                    // Создаем модель новости
                    NewModel newModel = new NewModel(title, body, title_author);
                    newModels.put(newModel.getTitle(), newModel);
                    listTitle.add(title);

                    // Создаем запись в бд
                    ContentValues cv = new ContentValues();
                    cv.put(DatabaseHelper.COLUMN_BODY, body);
                    cv.put(DatabaseHelper.COLUMN_AUTHOR, title_author);
                    cv.put(DatabaseHelper.COLUMN_ID, title);
                    db.insert(DatabaseHelper.TABLE, null, cv);
                }
                // Закрываем соединение
                db.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            // Изменяем список новостей
            listView.setAdapter(adapter);

            // Ставим обратчик на нажатие по новости
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    // Передаем параметры
                    Intent intent = new Intent(ACTION);
                    intent.putExtra("nm", newModels.get(listTitle.get(i)));
                    startActivity(intent);
                }
            });
        }
    }

    protected class ReadNewsFromDB_Async extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... strings) {

            // Открываем подлкючение к бд
            db = databaseHelper.getReadableDatabase();
            // Достаем данные из таблицы
            Cursor newsCursor =  db.rawQuery("select * from "+ DatabaseHelper.TABLE, null);

            // Проверяем есть ли данные
            if (newsCursor.moveToFirst()){
                // Берем индексы столбцов
                int idInd =  newsCursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
                int bodyInd = newsCursor.getColumnIndex(DatabaseHelper.COLUMN_BODY);
                int title_authorInd = newsCursor.getColumnIndex(DatabaseHelper.COLUMN_AUTHOR);
                do{
                    // Создаем модель с данным из строки
                    NewModel newModel = new NewModel(newsCursor.getString(idInd), newsCursor.getString(bodyInd), newsCursor.getString(title_authorInd));
                    newModels.put(newModel.getTitle(), newModel);
                    listTitle.add(newModel.getTitle());
                }
                while(newsCursor.moveToNext());
            }
            // Закрываем соединение
            db.close();

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            // Изменяем список новостей
            listView.setAdapter(adapter);

            // Ставим обратчик на нажатие по новости
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    // Передаем параметры
                    Intent intent = new Intent(ACTION);
                    intent.putExtra("nm", newModels.get(listTitle.get(i)));
                    startActivity(intent);
                }
            });
        }
    }
}