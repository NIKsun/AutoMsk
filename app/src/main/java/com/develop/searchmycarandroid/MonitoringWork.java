package com.develop.searchmycarandroid;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Date;

/**
 * Created by Никита on 03.09.2015.
 */
public class MonitoringWork extends Service {
    NotificationManager nm;


    public class ServiceThread implements Runnable {
        public int serviceId;
        public String requestAvito, requestAuto;
        public ServiceThread(int Id, String requestAvito, String requestAuto) {
            this.serviceId=Id;
            this.requestAuto = requestAuto;
            this.requestAvito = requestAvito;
        }
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public void run() {
            try {
                ServiceProcess(serviceId, requestAvito, requestAuto);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void onCreate() {
        super.onCreate();
        nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
        String[] status = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE).getString("SearchMyCarService_status", "false;false;false").split(";");
        int number = intent.getIntExtra("SearchMyCarService_serviceID",0);
        Log.d("Alarm", String.valueOf(number));
        if(number == 0)
            return START_STICKY;
        if(status[number-1].equals("true")) {
            String requestAuto = sPref.getString("SearchMyCarServiceRequestAuto" + number, "");
            String requestAvito = sPref.getString("SearchMyCarServiceRequestAvito" + number, "");
            Runnable st = new ServiceThread(number, requestAvito, requestAuto);
            new Thread(st).start();
        }
        return START_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
    }
    public IBinder onBind(Intent intent) {
        return null;
    }




    void ServiceProcess(final int serviceID, final String requestAvito, final String requestAuto) throws InterruptedException {
        SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
        final String lastCarDateAuto = sPref.getString("SearchMyCarService_LastCarDateAuto" + serviceID, "###");
        final String lastCarDateAvito = sPref.getString("SearchMyCarService_LastCarDateAvito" + serviceID, "###");
        final int[] counter = {0};
        int tryCounter = 0;
        final boolean[] isSuccess = {false};

        while(!isSuccess[0] && tryCounter < 3) {
            Log.d("AlarmT", "start");
            Thread t = new Thread(new Runnable() {
                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                public void run() {
                    counter[0] = 0;
                    Document doc = null;
                    Elements mainElems;
                    boolean isConnected = true, isConnectedAvito = true;
                    if(!requestAuto.equals("###")) {
                        try {
                            doc = Jsoup.connect(requestAuto).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; ru-RU; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").timeout(12000).get();
                        } catch (IOException e) {
                            isConnected = false;
                        }
                        if(isConnected) {
                            mainElems = doc.select("body > div.branding_fix > div.content.content_style > article > div.clearfix > div.b-page-wrapper > div.b-page-content").first().children();

                            Elements listOfCars = null;
                            for (int i = 0; i < mainElems.size(); i++) {
                                String className = mainElems.get(i).className();
                                if ((className.indexOf("widget widget_theme_white sales-list") == 0) && (className.length() == 36)) {
                                    listOfCars = mainElems.get(i).select("div.sales-list-item");
                                    break;
                                }
                            }
                            if (listOfCars != null) {
                                Date buf;
                                if (lastCarDateAuto.equals("###")) {
                                    for (int i = 0; i < listOfCars.size(); i++) {
                                        buf = Cars.getDateAuto(listOfCars.get(i).select("table > tbody > tr").first());
                                        if (buf != null)
                                            counter[0]++;
                                    }
                                } else {
                                    for (int i = 0; i < listOfCars.size(); i++) {
                                        buf = Cars.getDateAuto(listOfCars.get(i).select("table > tbody > tr").first());
                                        if (buf != null && Long.parseLong(lastCarDateAuto)/1000 < buf.getTime()/1000)
                                            counter[0]++;
                                    }

                                }
                            }
                        }
                    }
                    if(!requestAvito.equals("###")) {
                        try {
                            doc = Jsoup.connect(requestAvito).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; ru-RU; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").timeout(12000).get();
                        } catch (Exception e) {
                            if(isConnected)
                                isConnectedAvito = false;
                            else
                                return;
                        }
                        mainElems = doc.select("#catalog > div.layout-internal.col-12.js-autosuggest__search-list-container > div.l-content.clearfix > div.clearfix > div.catalog.catalog_table > div.catalog-list.clearfix").first().children();

                        if (lastCarDateAvito.equals("###")) {
                            for (int i = 0; i < mainElems.size(); i++)
                                for (int j = 0; j < mainElems.get(i).children().size(); j++)
                                    counter[0]++;
                        } else {
                            for (int i = 0; i < mainElems.size(); i++)
                                for (int j = 0; j < mainElems.get(i).children().size(); j++) {
                                    if (Long.parseLong(lastCarDateAvito)/1000 < Cars.getDateAvito(mainElems.get(i).children().get(j)).getTime()/1000)
                                        counter[0]++;
                                }
                        }

                    }
                    if(isConnected && isConnectedAvito) {
                        isSuccess[0] = true;
                    }
                }
            });
            t.start();
            while (t.isAlive());
            tryCounter ++;
        }
        Log.d("AlarmFinish", String.valueOf(counter[0]));
        if(counter[0] != 0) {
            sendNotification(counter[0], serviceID);
        }

    }

    void sendNotification(int countOfNewCars, int serviceID) {
        Notification notif = new Notification(R.drawable.status_bar, "Новое авто!",
                System.currentTimeMillis());

        Intent intent = new Intent(this, NotificationActivity.class);
        intent.putExtra("NotificationMessage", serviceID);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, serviceID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String shrtMessage = "";
        SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
        if(!sPref.getString("SearchMyCarService_shortMessage"+serviceID,"###").equals("###"))
            shrtMessage = sPref.getString("SearchMyCarService_shortMessage"+serviceID,"###");
        else
            shrtMessage = "авто";

        if(countOfNewCars == 1)
            notif.setLatestEventInfo(this, "Монитор "+serviceID, "Найден "+countOfNewCars+" новый "+shrtMessage, pIntent);
        else
            notif.setLatestEventInfo(this, "Монитор "+serviceID, "Найдено "+countOfNewCars+" новых "+shrtMessage, pIntent);
        Uri ringURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notif.number = countOfNewCars;
        notif.sound = ringURI;
        notif.flags |= Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_SOUND | Notification.FLAG_ONLY_ALERT_ONCE;
        nm.notify(serviceID, notif);
    }

}
