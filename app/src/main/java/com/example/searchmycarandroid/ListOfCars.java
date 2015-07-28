package com.example.searchmycarandroid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.BoringLayout;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.util.List;


public class ListOfCars extends Activity {
    Toast toastErrorConnection, toastErrorCarList;
    AlertDialog.Builder ad;
    String requestAvito, requestAuto, lastCarDate;
    LoadListView loader = new LoadListView();
    LoadListView.LoadImage[] imageLoaders = null;

    @Override
    protected void onDestroy() {
        loader.cancel(true);
        if(imageLoaders != null)
            for(int i=0;i<imageLoaders.length;i++)
                imageLoaders[i].cancel(true);
        super.onDestroy();
        finish();
    }
    @Override
    protected void onPause() {
        if(imageLoaders != null)
            for (int i = 0; i < imageLoaders.length; i++)
                imageLoaders[i].cancel(false);

        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        ActivityManager am = (ActivityManager)this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(1000);
        Boolean serviceRunning = false;
        for (int i=0; i<rs.size(); i++)
        {
            if(rs.get(i).service.getClassName().equals("com.example.searchmycarandroid.MonitoringService")) {
                serviceRunning = true;
                break;
            }
        }

        SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
        String status = sPref.getString("SearchMyCarService_status", "");
        String[] stat;
        if(serviceRunning)
            stat = status.split(";");
        else{
            stat = new String[]{"false","false","false"};
            sPref.edit().putString("SearchMyCarService_status","false;false;false").commit();
        }

        Button b1 = (Button) findViewById(R.id.buttonMonitor1);
        Button b2 = (Button) findViewById(R.id.buttonMonitor2);
        Button b3 = (Button) findViewById(R.id.buttonMonitor3);
        if (stat[0].equals("true"))
            b1.setText(Html.fromHtml("Монитор 1<br><font color=green face=cursive>запущен</font>"));
        else
            b1.setText(Html.fromHtml("Монитор 1<br><font color=#2E2E2E face=cursive>выключен</font>"));
        if (stat[1].equals("true"))
            b2.setText(Html.fromHtml("Монитор 2<br><font color=green face=cursive>запущен</font>"));
        else
            b2.setText(Html.fromHtml("Монитор 2<br><font color=#2E2E2E face=cursive>выключен</font>"));
        if (stat[2].equals("true"))
            b3.setText(Html.fromHtml("Монитор 3<br><font color=green face=cursive>запущен</font>"));
        else
            b3.setText(Html.fromHtml("Монитор 3<br><font color=#2E2E2E face=cursive>выключен</font>"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listofcars);

        toastErrorConnection = Toast.makeText(getApplicationContext(),
                "Связь с сервером не установлена :(", Toast.LENGTH_SHORT);
        toastErrorCarList = Toast.makeText(getApplicationContext(),
                "По вашему запросу ничего не найдено", Toast.LENGTH_SHORT);


        SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
        requestAuto = sPref.getString("SearchMyCarRequest", "");
        requestAvito = sPref.getString("SearchMyCarRequestAvito", "");
        lastCarDate = null;
        loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestAuto, requestAvito);
    }

    int buttonNumber=0;
    public void onClickStart(View v) {

        ad = new AlertDialog.Builder(ListOfCars.this);
        ad.setTitle("Запустить мониторинг?");
        ad.setMessage("Будут приходить уведомления о поступлении новых авто.");
        ad.setPositiveButton("Запустить монитор", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor ed = sPref.edit();
                ed.putString("SearchMyCarServiceRequestAuto" + buttonNumber, requestAuto);
                ed.putString("SearchMyCarServiceRequestAvito" + buttonNumber, requestAvito);
                ed.putString("SearchMyCarServiceLastCarDate" + buttonNumber, lastCarDate);
                ed.putInt("SearchMyCarService_period" + buttonNumber, 6);
                Log.i("Bar23", String.valueOf(buttonNumber));
                String[] newStatus = sPref.getString("SearchMyCarService_status", "").split(";");
                newStatus[buttonNumber - 1] = "true";
                ed.putString("SearchMyCarService_status", newStatus[0] + ";" + newStatus[1] + ";" + newStatus[2]);
                ed.commit();
                Intent serviceIntent = new Intent(ListOfCars.this, MonitoringService.class);
                serviceIntent.putExtra("SearchMyCarService_serviceID", buttonNumber);
                serviceIntent.putExtra("SearchMyCarService_command", "start");
                startService(serviceIntent);
                switch (buttonNumber) {
                    case 1:
                        Button b1 = (Button) findViewById(R.id.buttonMonitor1);
                        b1.setText(Html.fromHtml("Монитор 1<br><font color=green face=cursive>запущен</font>"));
                        break;
                    case 2:
                        Button b2 = (Button) findViewById(R.id.buttonMonitor2);
                        b2.setText(Html.fromHtml("Монитор 2<br><font color=green face=cursive>запущен</font>"));
                        break;
                    case 3:
                        Button b3 = (Button) findViewById(R.id.buttonMonitor3);
                        b3.setText(Html.fromHtml("Монитор 3<br><font color=green face=cursive>запущен</font>"));
                        break;
                }
                Toast.makeText(ListOfCars.this, "Монитор " + buttonNumber + " запущен с текущими параметрами", Toast.LENGTH_LONG).show();
            }
        });
        ad.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                Toast.makeText(ListOfCars.this, "Вы не изменили параметры мониторинга", Toast.LENGTH_SHORT).show();
            }
        });
        ad.setCancelable(true);
        ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                Toast.makeText(ListOfCars.this, "Вы не изменили параметры мониторинга", Toast.LENGTH_SHORT).show();
            }
        });

        String[] status = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE).getString("SearchMyCarService_status", "").split(";");


        Intent intent = new Intent(ListOfCars.this, NotificationActivity.class);
        switch (v.getId()) {
            case R.id.buttonMonitor1:
                if (status[0].equals("true")) {
                    intent.putExtra("MonitorNumber", 1);
                    startActivity(intent);
                    return;
                } else
                    buttonNumber = 1;
                break;
            case R.id.buttonMonitor2:
                if (status[1].equals("true")) {
                    intent.putExtra("MonitorNumber", 2);
                    startActivity(intent);
                    return;
                } else
                    buttonNumber = 2;
                break;
            case R.id.buttonMonitor3:
                if (status[2].equals("true")) {
                    intent.putExtra("MonitorNumber", 3);
                    startActivity(intent);
                    return;
                } else
                    buttonNumber = 3;
                break;
        }
        if(lastCarDate == null)
            Toast.makeText(getApplicationContext(), "Подождите загрузки списка", Toast.LENGTH_SHORT).show();
        else
            ad.show();
    }

    class LoadListView extends AsyncTask<String, String, Cars> {
        String[] imagesRef;
        Bitmap[] images;
        final Cars[] carsAvto = new Cars[1], carsAvito = new Cars[1];

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressBar pb = (ProgressBar)findViewById(R.id.progressBar);
            pb.setVisibility(View.VISIBLE);
            TextView tv = (TextView)findViewById(R.id.textViewProgressBar);
            tv.setVisibility(View.VISIBLE);
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected Cars doInBackground(final String... params) {
            final Boolean[] bulAvito = {true}, connectionAvitoSuccess = {true};
            Thread threadAvito = new Thread(new Runnable() {
                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                public void run() {
                    Document doc;
                    try {
                        doc = Jsoup.connect(params[1]).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; ru-RU; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").get();
                    }
                    catch (HttpStatusException e)
                    {
                        bulAvito[0] =false;
                        return;
                    }
                    catch (IOException e)
                    {
                        connectionAvitoSuccess[0] = false;
                        return;
                    }
                    Elements mainElems = doc.select("#catalog > div.layout-internal.col-12.js-autosuggest__search-list-container > div.l-content.clearfix > div.clearfix > div.catalog.catalog_table > div.catalog-list.clearfix").first().children();
                    int length = 0;
                    for (int i = 0; i < mainElems.size(); i++)
                        length += mainElems.get(i).children().size();

                    carsAvito[0] = new Cars(length);
                    for (int i = 0; i < mainElems.size(); i++)
                        for (int j = 0; j < mainElems.get(i).children().size(); j++) {
                            carsAvito[0].addFromAvito(mainElems.get(i).children().get(j));
                        }
                    carsAvito[0].sortByDateAvito();

                }
            });
            if(!params[1].equals("###"))
                threadAvito.start();
            else
                bulAvito[0] = false;



            publishProgress("Загрузка с Auto.ru");
            Boolean bulAvto = true, connectionAutoSuccess = true;
            if(!params[0].equals("###")) {
                Document doc = null;
                try {
                    doc = Jsoup.connect(params[0]).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; ru-RU; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").get();
                } catch (IOException e) {
                    connectionAutoSuccess = false;
                }
                if(connectionAutoSuccess) {
                    Elements mainElems = doc.select("body > div.branding_fix > div.content.content_style > article > div.clearfix > div.b-page-wrapper > div.b-page-content").first().children();

                    Elements listOfCars = null;
                    for (int i = 0; i < mainElems.size(); i++) {
                        String className = mainElems.get(i).className();
                        if ((className.indexOf("widget widget_theme_white sales-list") == 0) && (className.length() == 36)) {
                            listOfCars = mainElems.get(i).select("div.sales-list-item");
                            break;
                        }
                    }
                    if (listOfCars == null) {
                        bulAvto = false;
                    }
                    else {
                        carsAvto[0] = new Cars(listOfCars.size());
                        for (int i = 0; i < listOfCars.size(); i++)
                            carsAvto[0].addFromAutoRu(listOfCars.get(i).select("table > tbody > tr").first());
                    }
                }
            }
            else
                bulAvto = false;

            if(!connectionAutoSuccess && !connectionAvitoSuccess[0]) {
                toastErrorConnection.show();
                return null;
            }
            publishProgress("Загрузка с Avito.ru");
            while (threadAvito.isAlive()); //waiting
            publishProgress("Подготовка результата");
            if(!bulAvito[0] && !bulAvto)
            {
                toastErrorCarList.show();
                return null;
            }
            if(!bulAvito[0] || !connectionAvitoSuccess[0])
                carsAvito[0] = new Cars(0);
            if(!bulAvto || !connectionAutoSuccess)
                carsAvto[0] = new Cars(0);

            Cars cars = Cars.merge(carsAvto[0], carsAvito[0]);
            Bitmap LoadingImage = BitmapFactory.decodeResource(getResources(), R.drawable.res);
            images = new Bitmap[cars.getLenth()];
            for(int i=0;i<cars.getLenth();i++)
                images[i] = LoadingImage;

            lastCarDate = cars.getLastCarDate();

            return cars;
        }
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            TextView tv = (TextView)findViewById(R.id.textViewProgressBar);
            tv.setText(values[0]);
        }
        @Override
        protected void onPostExecute(Cars result) {
            super.onPostExecute(result);
            ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
            pb.setVisibility(View.INVISIBLE);
            TextView tv = (TextView)findViewById(R.id.textViewProgressBar);
            tv.setVisibility(View.INVISIBLE);

            if(result == null) {
                finish();
                return;
            }

            Toast.makeText(ListOfCars.this, "Найдено " + carsAvto[0].getLenth() + " ПОСЛЕДНИХ объявлений на Auto.ru и "
                    + carsAvito[0].getLenth() + " на Avito.ru, отсортировано по дате", Toast.LENGTH_LONG).show();

            ListView lv = (ListView) findViewById(R.id.listView);
            lv.setAdapter(new ListViewAdapter(ListOfCars.this, result, images));

            imagesRef = new String[result.getLenth()];
            imageLoaders = new LoadImage[result.getLenth()];
            for (int i = 0; i < result.getLenth(); i++) {
                imagesRef[i] = result.getImg(i);
                imageLoaders[i] = new LoadImage();
                imageLoaders[i].execute(i);
            }

        }

        class LoadImage extends AsyncTask<Integer, Void, Integer> {
            Bitmap bm;

            @Override
            protected void onPreExecute() {
            }

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            protected Integer doInBackground(Integer... params) {
                try {
                    bm = BitmapFactory.decodeStream((InputStream) new URL(imagesRef[params[0]]).getContent());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return params[0];
            }
            @Override
            protected void onPostExecute(Integer result) {
                images[result] = bm;
                ListView lv=(ListView)findViewById(R.id.listView);
                lv.invalidateViews();
            }

        }
    }
}