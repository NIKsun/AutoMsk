package com.develop.searchmycarandroid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


public class NotificationActivity extends Activity {
    Toast toastErrorConnection, toastErrorCarList, toastErrorConnectionAuto,toastErrorConnectionAvito;
    String lastCarDateAvito, lastCarDateAuto;
    int monitorNumber;
    LoadListViewMonitor loader = new LoadListViewMonitor();
    Boolean imageLoaderMayRunning;
    Thread imageLoader = null;
    AlarmManager am;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Справка");
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(NotificationActivity.this);
        builder.setTitle("Справка").setMessage("Для изменения периода мониторинга текущего списка сдвиньте" +
                " ползунок периода в нужное положение." +
                " Слишком частый мониторинг может привести к быстой разрядке аккумулятора").setCancelable(true).setNegativeButton("Отмена",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        loader.cancel(true);
        imageLoaderMayRunning = false;
        super.onDestroy();
        monitorNumber = intent.getIntExtra("NotificationMessage", 0);
        SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
        String requestAuto = sPref.getString("SearchMyCarServiceRequestAuto" + monitorNumber, "");
        String requestAvito = sPref.getString("SearchMyCarServiceRequestAvito" + monitorNumber, "");
        lastCarDateAuto = sPref.getString("SearchMyCarService_LastCarDateAuto" + monitorNumber, "###");
        lastCarDateAvito = sPref.getString("SearchMyCarService_LastCarDateAvito" + monitorNumber, "###");
        loader = new LoadListViewMonitor();
        loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestAuto, requestAvito);
        final TextView textView = (TextView) findViewById(R.id.textViewSeekBar );
        SeekBar seekBar = (SeekBar) findViewById(R.id.seekbar);

        seekBar.setProgress(sPref.getInt("SearchMyCarService_period" + monitorNumber, 0));
        textView.setText("Период\n" + String.valueOf(seekBar.getProgress() + 4) + " мин.");

        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int progress = 0;

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                        textView.setText("Период\n" + String.valueOf(progresValue + 4) + " мин.");
                        progress = progresValue;
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
                        sPref.edit().putInt("SearchMyCarService_period" + monitorNumber, progress).commit();

                        Intent serviceIntent = new Intent(getApplicationContext(), MonitoringWork.class);
                        serviceIntent.putExtra("SearchMyCarService_serviceID", monitorNumber);
                        PendingIntent pIntent = PendingIntent.getService(getApplicationContext(), monitorNumber, serviceIntent, 0);
                        am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + (progress+4)*60000, (progress+4)*60000, pIntent);

                        Toast.makeText(getApplicationContext(), "Новый период мониторинга установлен", Toast.LENGTH_SHORT).show();
                    }
                });
        super.onNewIntent(intent);
    }

    @Override
    protected void onDestroy() {
        loader.cancel(true);
        imageLoaderMayRunning = false;
        super.onDestroy();
    }
    @Override
    protected void onPause() {
        imageLoaderMayRunning = false;
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monitor_list_of_cars);

        am = (AlarmManager) getSystemService(ALARM_SERVICE);

        toastErrorConnection = Toast.makeText(getApplicationContext(),
                "Связь с сервером не установлена :(", Toast.LENGTH_SHORT);
        toastErrorConnectionAvito = Toast.makeText(getApplicationContext(),
                "Не удалось загрузить списко автомобилей Avito", Toast.LENGTH_SHORT);
        toastErrorConnectionAuto = Toast.makeText(getApplicationContext(),
                "Не удалось загрузить списко автомобилей Auto.ru", Toast.LENGTH_SHORT);
        toastErrorCarList = Toast.makeText(getApplicationContext(),
                "По вашему запросу ничего не найдено", Toast.LENGTH_SHORT);

        monitorNumber = getIntent().getIntExtra("NotificationMessage", 0);

        SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
        String requestAuto = sPref.getString("SearchMyCarServiceRequestAuto" + monitorNumber, "");
        String requestAvito = sPref.getString("SearchMyCarServiceRequestAvito" + monitorNumber, "");
        lastCarDateAuto = sPref.getString("SearchMyCarService_LastCarDateAuto" + monitorNumber, "###");
        lastCarDateAvito = sPref.getString("SearchMyCarService_LastCarDateAvito" + monitorNumber, "###");
        loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestAuto, requestAvito);

        final TextView textView = (TextView) findViewById(R.id.textViewSeekBar );
        SeekBar seekBar = (SeekBar) findViewById(R.id.seekbar);

        seekBar.setProgress(sPref.getInt("SearchMyCarService_period" + monitorNumber, 0));
        textView.setText("Период\n" + String.valueOf(seekBar.getProgress() + 4) + " мин.");

        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int progress = 0;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                        textView.setText("Период\n" + String.valueOf(progresValue + 4) + " мин.");
                        progress = progresValue;
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
                        sPref.edit().putInt("SearchMyCarService_period"+monitorNumber,progress).commit();

                        Intent serviceIntent = new Intent(getApplicationContext(), MonitoringWork.class);
                        serviceIntent.putExtra("SearchMyCarService_serviceID", monitorNumber);
                        PendingIntent pIntent = PendingIntent.getService(getApplicationContext(), monitorNumber, serviceIntent, 0);
                        am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + (progress + 4) * 60000, (progress + 4) * 60000, pIntent);

                        Toast.makeText(getApplicationContext(),"Новый период установлен",Toast.LENGTH_SHORT).show();
                    }
                });
    }




    public void onClickStart(View v) {
        AlertDialog.Builder ad;
        ad = new AlertDialog.Builder(NotificationActivity.this);
        ad.setTitle("Остановить монитор " + monitorNumber+"?");
        ad.setMessage("Поисх авто по данным характеристикам прекратиться.");
        ad.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
                String[] newStatus = sPref.getString("SearchMyCarService_status", "false;false;false").split(";");
                newStatus[monitorNumber - 1] = "false";
                sPref.edit().putString("SearchMyCarService_shortMessage" + monitorNumber, "###").commit();
                sPref.edit().putString("SearchMyCarService_status", newStatus[0] + ";" + newStatus[1] + ";" + newStatus[2]).commit();
                Toast.makeText(NotificationActivity.this, "Монитор " + monitorNumber + " остановлен", Toast.LENGTH_LONG).show();
                Intent serviceIntent = new Intent(getApplicationContext(), MonitoringWork.class);
                PendingIntent pIntent = PendingIntent.getService(getApplicationContext(), monitorNumber, serviceIntent, 0);
                am.cancel(pIntent);
                finish();
            }
        });
        ad.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {  }
        });
        ad.setCancelable(true);
        ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) { }
        });
        ad.show();
    }

    class LoadListViewMonitor extends AsyncTask<String, String, Cars> {
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
                        doc = Jsoup.connect(params[1]).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; ru-RU; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").timeout(12000).get();
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
                    doc = Jsoup.connect(params[0]).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; ru-RU; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").timeout(12000).get();
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
            if(!connectionAvitoSuccess[0])
                toastErrorConnectionAvito.show();
            if(!connectionAutoSuccess)
                toastErrorConnectionAuto.show();
            publishProgress("Загрузка с Avito.ru");
            while (threadAvito.isAlive()); //waiting
            publishProgress("Подготовка результата");
            if(!bulAvito[0] && !bulAvto)
            {
                toastErrorCarList.show();
                return null;
            }
            SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor ed = sPref.edit();
            if(!bulAvito[0] || !connectionAvitoSuccess[0])
                carsAvito[0] = new Cars(0);
            else
                ed.putString("SearchMyCarService_LastCarDateAvito" + monitorNumber, String.valueOf(carsAvito[0].getCarDateLong(0)));;
            if(!bulAvto || !connectionAutoSuccess)
                carsAvto[0] = new Cars(0);
            else
                ed.putString("SearchMyCarService_LastCarDateAuto" + monitorNumber, String.valueOf(carsAvto[0].getCarDateLong(0)));
            ed.commit();

            if(carsAvto[0].getLenth() == 0 && carsAvito[0].getLenth() == 0)
            {
                toastErrorConnection.show();
                return null;
            }

            Cars cars = Cars.merge(carsAvto[0], carsAvito[0]);
            Bitmap LoadingImage = BitmapFactory.decodeResource(getResources(), R.drawable.res);
            images = new Bitmap[cars.getLenth()];
            for(int i=0;i<cars.getLenth();i++)
                images[i] = LoadingImage;

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

            ListView lv = (ListView) findViewById(R.id.listViewMonitor);
            lv.setAdapter(new ListViewAdapter(NotificationActivity.this, result, images, lastCarDateAuto, lastCarDateAvito));

            imageLoaderMayRunning = true;
            startThread(result);
        }

        private void startThread(final Cars result) {
            final Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                public void run() {
                    for (int i = 0; i < result.getLenth(); i++) {
                        try {
                            if(imageLoaderMayRunning)
                                images[i] = BitmapFactory.decodeStream((InputStream) new URL(result.getImg(i)).getContent());
                            else
                                return;

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        handler.post(new Runnable() {
                            public void run() {
                                ListView lv = (ListView) findViewById(R.id.listViewMonitor);
                                lv.invalidateViews();
                            }
                        });
                    }
                }
            };
            imageLoader = new Thread(runnable);
            imageLoader.start();
        }
    }
}