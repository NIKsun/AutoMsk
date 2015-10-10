package com.develop.searchmycarandroid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;


import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class CreateRequestActivity extends Activity implements OnClickListener {
    DBHelper dbHelper;
    Tracker mTracker;
    Boolean isUseSearchInAvito = null;
    Boolean isUseSearchInDrom = null;
    Boolean isUseSearchInAuto = null;
    final String SAVED_TEXT_WITH_VERSION = "checkVersion";
    final String NEW_VERSION_MATERIAL = "MaterialVersion";
    final String DO_NOT_REMIND = "DontRemind";
    String MaterialHttp;
    static Dialog dialogPicker ;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_req_menu, menu);
        menu.findItem(R.id.cr_req_menu_Avito).setChecked(isUseSearchInAvito);
        menu.findItem(R.id.cr_req_menu_Drom).setChecked(isUseSearchInDrom);
        menu.findItem(R.id.cr_req_menu_Auto).setChecked(isUseSearchInAuto);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateRequestActivity.this);
        if(item.getTitle().equals("Справка")) {
            builder.setTitle("Справка").setMessage("Приложние создано для того, чтобы облегчить утомительный процесс поиска Вашего будущего автомобиля. Для этого в нем имеются мониторы, которые будут вести поиск по указанным параметрам на ведущих автомобильных порталах России.\nВы используете первую версию приложения, однако Ваши отзывы и предложения помогут нам сделать его лучше и эффективнее.\nУдачных покупок!").setCancelable(true).setNegativeButton("Назад",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
        else if(item.getTitle().equals("Искать на avito.ru"))
        {
            isUseSearchInAvito = !isUseSearchInAvito;
            SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
            sPref.edit().putBoolean("isUseSearchInAvito",isUseSearchInAvito).commit();
            item.setChecked(isUseSearchInAvito);
        }
        else if(item.getTitle().equals("Искать на drom.ru"))
        {
            isUseSearchInDrom = !isUseSearchInDrom;
            SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
            sPref.edit().putBoolean("isUseSearchInDrom",isUseSearchInDrom).commit();
            item.setChecked(isUseSearchInDrom);
        }
        else
        {
            isUseSearchInAuto = !isUseSearchInAuto;
            SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
            sPref.edit().putBoolean("isUseSearchInAuto",isUseSearchInAuto).commit();
            item.setChecked(isUseSearchInAuto);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume(){
        super.onResume();
        SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);

        Intent checkIntent = new Intent(getApplicationContext(), MonitoringWork.class);
        Boolean alrarmIsActive = false;
        for(int i=1;i<=3;i++)
            if (PendingIntent.getService(getApplicationContext(), i, checkIntent, PendingIntent.FLAG_NO_CREATE) != null)
                alrarmIsActive = true;

        if(!alrarmIsActive)
            sPref.edit().putString("SearchMyCarService_status", "false;false;false").commit();

        isUseSearchInAvito = sPref.getBoolean("isUseSearchInAvito",true);
        isUseSearchInDrom = sPref.getBoolean("isUseSearchInDrom",true);
        isUseSearchInAuto = sPref.getBoolean("isUseSearchInAuto",true);

        Button b = (Button) findViewById(R.id.marka_button);
        Button b1 = (Button) findViewById(R.id.model_button);
        if(sPref.contains("SelectedMark"))
        {
//nen

            String posMark = sPref.getString("SelectedMark", "Любая");
            if(posMark.equals("Любая"))
            {
                sPref.edit().putString("SelectedModel", "Любая").commit();
                b.setText("Марка");
                b1.setText("Модель");
                Button b5 = (Button) findViewById(R.id.clear_marka);
                b5.setVisibility(View.INVISIBLE);
            }
            else{
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                b.setText(posMark);

                Button b3 = (Button) findViewById(R.id.clear_marka);
                b3.setVisibility(View.VISIBLE);

                if(sPref.contains("SelectedModel"))
                {

//nen
                    String  posModel = sPref.getString("SelectedModel", "Любая");
                    if(posModel.equals("Любая")){
                        b1.setText("Модель");
                        Button b5 = (Button) findViewById(R.id.clear_model);
                        b5.setVisibility(View.INVISIBLE);
                    }
                    else{
                        b1.setText(posModel);
                        Button b4 = (Button) findViewById(R.id.clear_model);
                        b4.setVisibility(View.VISIBLE);
                    }

                }
                db.close();
            }


        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_create_request);
        dbHelper = new DBHelper(this);

        Thread threadAvito = new Thread(new Runnable() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            public void run() {
                Document doc;
                SharedPreferences sPref;
                try {
                    doc = Jsoup.connect("https://play.google.com/store/apps/details?id=com.develop.searchmycarandroid").userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; ru-RU; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").timeout(12000).get();

                    PackageManager packageManager;
                    PackageInfo packageInfo;
                    packageManager=getPackageManager();

                    packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
                    Element mainElems = doc.select("#body-content > div > div > div.main-content > div.details-wrapper.apps-secondary-color > div > div.details-section-contents > div:nth-child(4) > div.content").first();

                    Element mainElems2 = doc.select("#body-content > div > div > div.main-content > div:nth-child(1) > div > div.details-section.description.simple.contains-text-link.apps-secondary-color > div.details-section-contents.show-more-container.more > div.show-more-content.text-body").first();


                    if (mainElems2.text().contains("Авто Русь") || (mainElems2.text().contains("АвтоРусь")))
                    {
                        sPref = getPreferences(MODE_PRIVATE);
                        SharedPreferences.Editor ed = sPref.edit();
                        ed.putBoolean(NEW_VERSION_MATERIAL, true);
                        ed.commit();
                        MaterialHttp = mainElems2.text().substring(mainElems2.text().indexOf("http"));
                        MaterialHttp = MaterialHttp.substring(0, MaterialHttp.indexOf(" "));

                        //mainElems2.text().substring()

                    }
                    else
                    {
                        sPref = getPreferences(MODE_PRIVATE);
                        SharedPreferences.Editor ed = sPref.edit();
                        ed.putBoolean(NEW_VERSION_MATERIAL, false);
                        ed.commit();

                    }
                    if (!packageInfo.versionName.equals(mainElems.text()))
                    {
                        sPref = getPreferences(MODE_PRIVATE);
                        SharedPreferences.Editor ed = sPref.edit();
                        ed.putBoolean(SAVED_TEXT_WITH_VERSION, false);
                        ed.commit();
                    }
                    else
                    {
                        sPref = getPreferences(MODE_PRIVATE);
                        SharedPreferences.Editor ed = sPref.edit();
                        ed.putBoolean(SAVED_TEXT_WITH_VERSION, true);
                        ed.commit();

                    }
                    //SharedPreferences sPrefRemind;
                    //sPrefRemind = getPreferences(MODE_PRIVATE);
                    //sPrefRemind.edit().putBoolean(DO_NOT_REMIND, false).commit();
                }
                catch (HttpStatusException e)
                {
                    //bulAvito[0] =false;
                    return;
                }
                catch (IOException e)
                {
                    //connectionAvitoSuccess[0] = false;
                    return;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

            }
        });


        SharedPreferences sPrefVersion;
        sPrefVersion = getPreferences(MODE_PRIVATE);
        Boolean isNewVersion;
        isNewVersion = sPrefVersion.getBoolean(SAVED_TEXT_WITH_VERSION, true);
        threadAvito.start();

        boolean remind=true;
        if (!isNewVersion)
        {

            Log.d("aaffa", "isNewVersion= "+isNewVersion);
            Log.d("aaffa", "не новая версия!!! Так записано. Возможно, поток еще не отработал");
            SharedPreferences sPref12;
            sPref12 = getPreferences(MODE_PRIVATE);
            String isNewVersion12;

            PackageManager packageManager;
            PackageInfo packageInfo;
            packageManager=getPackageManager();

            try {
                packageInfo=packageManager.getPackageInfo(getPackageName(), 0);
                isNewVersion12 = sPref12.getString("OldVersionName", packageInfo.versionName);

                if (!isNewVersion12.equals(packageInfo.versionName))
                {
                    Log.d("aaffa", "записанная в shared запись версии НЕ совпадает с действительной=" + isNewVersion12);

                    SharedPreferences sPref;
                    sPref = getPreferences(MODE_PRIVATE);
                    SharedPreferences.Editor ed = sPref.edit();
                    ed.putBoolean(SAVED_TEXT_WITH_VERSION, false);
                    ed.commit();

                    SharedPreferences sPrefRemind;
                    sPrefRemind = getPreferences(MODE_PRIVATE);
                    sPrefRemind.edit().putBoolean(DO_NOT_REMIND, false).commit();
                }
                else
                {
                    remind = false;
                    Log.d("aaffa", "записанная в shared запись версии  совпадает с действительной");
                }


                SharedPreferences sPrefRemind;
                sPrefRemind = getPreferences(MODE_PRIVATE);
                sPrefRemind.edit().putString("OldVersionName", packageInfo.versionName).commit();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            SharedPreferences sPrefRemind;
            sPrefRemind = getPreferences(MODE_PRIVATE);
            Boolean newVersionMaterial;
            newVersionMaterial = sPrefRemind.getBoolean(NEW_VERSION_MATERIAL, false);

            if (newVersionMaterial) {
                int adMobCounter = sPrefRemind.getInt("NewVersionCounter",1);
                if(adMobCounter == 3) {
                    sPrefRemind.edit().putInt("NewVersionCounter",1).commit();

                    AlertDialog.Builder ad;
                    ad = new AlertDialog.Builder(CreateRequestActivity.this);
                    ad.setTitle("Новая версия.");
                    ad.setMessage("Скачайте новую версию приложения: Авто Русь!");

                    ad.setPositiveButton("Скачать приложение", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {

                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MaterialHttp));
                            startActivity(intent);
                        }
                    });
                    ad.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                        }
                    });
                    ad.setNeutralButton("Не напоминать", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            SharedPreferences sPrefRemind;
                            sPrefRemind = getPreferences(MODE_PRIVATE);
                            sPrefRemind.edit().putBoolean(DO_NOT_REMIND, true).commit();
                        }
                    });
                    ad.setCancelable(true);
                    ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                        }
                    });

                    ad.show();
                }
                else
                    sPrefRemind.edit().putInt("AdMobCounter",adMobCounter+1).commit();

            }
        }

        SharedPreferences sPrefRemind;
        sPrefRemind = getPreferences(MODE_PRIVATE);
        Boolean dontRemind;
        dontRemind = sPrefRemind.getBoolean(DO_NOT_REMIND, false);
        Log.d("aaffa", "dontRemind= "+dontRemind.toString());
        Log.d("aaffa", "remind= "+remind);

        if ((!dontRemind) && (!remind)) {

            AlertDialog.Builder ad;
            ad = new AlertDialog.Builder(CreateRequestActivity.this);
            ad.setTitle("Обновление.");
            ad.setMessage("Вы хотите обновить приложение?");
            ad.setPositiveButton("Обновить", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.develop.searchmycarandroid"));
                    startActivity(intent);
                }
            });
            ad.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                }
            });
            ad.setNeutralButton("Не напоминать", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                    SharedPreferences sPrefRemind;
                    sPrefRemind = getPreferences(MODE_PRIVATE);
                    sPrefRemind.edit().putBoolean(DO_NOT_REMIND, true).commit();
                }
            });
            ad.setCancelable(true);
            ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                }
            });

            ad.show();
        }

            AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName("Start activity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        // clear year
        SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
        sPref.edit().putInt("AdMobCounter", 1).commit();
        sPref.edit().putInt("StartYear", 1970).commit();
        sPref.edit().putInt("EndYear", 1900 + new Date().getYear()).commit();
        //clear price
        sPref.edit().putInt("StartPrice", 0).commit();
        sPref.edit().putInt("EndPrice", 10000000).commit();
        //clear engine volume
        sPref.edit().putInt("StartVolume", 0).commit();
        sPref.edit().putInt("EndVolume", 36).commit();
        //clear probeg
        sPref.edit().putInt("Probeg", 61).commit();

        //clear trans
        sPref.edit().putString("Trans","").commit();
        //clear engine type
        sPref.edit().putString("EngineType","").commit();
        //clear privod
        sPref.edit().putString("Privod","").commit();
        //clear body
        sPref.edit().putString("Body","").commit();


    }

    public void onClick(View v) {
        //dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();



        switch (v.getId()) {
            case R.id.buttonSearch:
                if(!isUseSearchInAuto && !isUseSearchInDrom && !isUseSearchInAvito) {
                    Toast.makeText(this, "Вы не указали сайт для поиска в настройках", Toast.LENGTH_LONG).show();
                    break;
                }

                Intent intent = new Intent(this, ListOfCars.class);
                SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor ed = sPref.edit();

                Integer startYear = sPref.getInt("StartYear", 1970);
                String startYeard = "";
                if(startYear != 1970)
                    startYeard = String.valueOf(startYear);
                Integer endYear = sPref.getInt("EndYear",2015);
                String endYeard = "";
                if(endYear != 2015)
                    endYeard = String.valueOf(endYear);

                Integer startPrice = sPref.getInt("StartPrice",0);
                Integer endPrice = sPref.getInt("EndPrice",100000000);

                //volume is another!!!!!!! (0-36)
                int startVolume = sPref.getInt("StartVolume",0);
                int endVolume = sPref.getInt("EndVolume",36);
                String[] volume_arr_avto = new String[]{"0.0","0.6","0.7","0.8","0.9","1.0","1.1","1.2","1.3","1.4","1.5","1.6","1.7","1.8","1.9","2.0","2.1","2.2","2.3","2.4","2.5","2.6","2.7","2.8","2.9","3.0","3.1","3.2","3.3","3.4","3.5","4.0","4.5","5.0","5.5","6.0","10.0"};
                String[] volume_arr_avito = new String[]{"15775", "15776", "15777", "15778", "15779", "15780", "15781", "15782", "15783", "15784", "15785", "15786", "15787", "15788", "15789", "15790", "15791", "15792", "15793", "15794", "15795", "15796", "15797", "15798", "15799", "15800", "15801", "15802", "15803", "15804", "15805", "15810", "15815", "15820", "15825", "15830", "15831"};
                String[] volume_arr_drom = new String[]{"0.0","0.0","0.7","0.8","1.0","1.0","1.1","1.2","1.3","1.4","1.5","1.6","1.7","1.8","1.9","2.0","2.2","2.2","2.3","2.4","2.5","2.7","2.7","2.8","3.0","3.0","3.2","3.2","3.3","3.5","3.5","4.0","4.5","5.0","5.5","6.0",""};


                //probeg is another (0-61)
                int probegval = sPref.getInt("Probeg",61);
                String[] probeg_arr_avto = new String[]{"0","5000","10000","15000","20000","25000","30000","35000","40000","45000","50000","55000", "60000", "65000", "70000","75000","80000","85000","90000","95000","100000","110000","120000","130000","140000","150000","160000","170000","180000","190000","200000","210000","220000","230000","240000","250000","260000","270000","280000","290000","300000","310000","320000","330000","340000","350000","360000","370000","380000","390000","400000","410000","420000","430000","440000","450000","460000","470000","480000","490000","500000","100000000"};
                String[] probeg_arr_avito = new String[]{"15483", "15486", "15487", "15490", "15492", "15494", "15496", "15498", "15500", "15502", "15505", "15506", "15509", "15510", "15512", "15513", "15516", "15517", "15520", "15521", "15524", "15527", "15528", "15531", "15533", "15535", "15536", "15539", "15540", "15542", "15544", "15545", "15546", "15547", "15548", "15554", "15556", "15557", "15558", "15559", "15560", "15561", "15562", "15563", "15564", "15565", "15566", "15567", "15568", "15569", "15570", "15571", "15572", "15573", "15574", "15575", "15576", "15577", "15578", "15579", "15581", "15582"};

                //trans
                String trans_str = sPref.getString("Trans", "");
                if(trans_str.equals(""))
                    trans_str="1234";
                String[] trans = trans_str.split("");

                String[] trans_arr_avito = {"","860-","861-","14754-","14753-"};
                String trans_avito_req = ".185_";

                String[] trans_arr_avto = {"","&search%5Bgearbox%5D%5Bautomatic_auto%5D=1","&search%5Bgearbox%5D%5Bmanual_all%5D=1","&search%5Bgearbox%5D%5Bautomatic_robot%5D=1","&search%5Bgearbox%5D%5Bautomatic_variator%5D=1"};
                String trans_avto_req = "";


                //trans unique drom ccka
                String[] trans_arr_drom = {"","&transmission=2","&transmission=1"}; //2 auto 1 man
                String trans_drom_req = "";

                if(trans_str.length()==1){
                    if(trans[1].equals("2"))
                        trans_drom_req = trans_arr_drom[2];
                    else
                        trans_drom_req = trans_arr_drom[1];
                }
                else{
                    if(trans[2].equals("2"))
                        trans_drom_req = trans_arr_drom[0];
                    else
                        trans_drom_req = trans_arr_drom[1];
                }


                for(int i = 1; i < trans.length; ++i){
                    trans_avto_req+=trans_arr_avto[Integer.parseInt(trans[i])];
                    trans_avito_req+=trans_arr_avito[Integer.parseInt(trans[i])];
                }
                if(trans_str.equals("1234"))
                    trans_avto_req="";



                //enginetype
                String engine_str = sPref.getString("EngineType", "");
                if(engine_str.equals(""))
                    engine_str="12345";
                String[] engine = engine_str.split("");

                String[] engine_arr_avito = {"","862-","864-","863-","14752-","14751-"};
                String engine_avito_req = ".186_";

                String[] engine_arr_avto = {"","&search%5Bengine_type%5D%5Bgasoline%5D=1","&search%5Bengine_type%5D%5B1260%5D=1","&search%5Bengine_type%5D%5B1256%5D=1","&search%5Bengine_type%5D%5B1262%5D=1","&search%5Bengine_type%5D%5B1257%5D=1"};
                String engine_avto_req = "";

                //enginetype unique for drom
                String[] engine_arr_drom = {"","&fueltype=1","&fueltype=2"}; // 1 - benz 2 - diz
                String engine_drom_req = "";

                boolean drom_req_true = true;
                if(engine_str.length()==1){
                    if(engine[1].equals("1"))
                        engine_drom_req = engine_arr_drom[1];
                    else{
                        if(engine[1].equals("2"))
                            engine_drom_req = engine_arr_drom[2];
                        else
                            drom_req_true = false;
                    }
                }
                else
                if(engine[2].equals("2"))
                    engine_drom_req = engine_arr_drom[0];
                else {
                    if(engine[1].equals("1"))
                        engine_drom_req = engine_arr_drom[1];
                    else{
                        if(engine[1].equals("2"))
                            engine_drom_req = engine_arr_drom[2];
                        else
                            drom_req_true = false;
                    }
                }



                for(int i = 1; i < engine.length; ++i){
                    engine_avto_req+=engine_arr_avto[Integer.parseInt(engine[i])];
                    engine_avito_req +=engine_arr_avito[Integer.parseInt(engine[i])];
                }
                if(engine_str.equals("12345"))
                    engine_avto_req="";


                //privod
                String privod_str = sPref.getString("Privod","");
                if(privod_str.equals(""))
                    privod_str="123";
                String[] privod = privod_str.split("");

                String[] privod_arr_avito = {"","8851-","8852-","8853-"};
                String privod_avito_req = ".695_";

                String[] privod_arr_avto = {"","&search%5Bdrive%5D%5B180%5D=1","&search%5Bdrive%5D%5B181%5D=1","&search%5Bdrive%5D%5B7%5D=1"};
                String privod_avto_req = "";

                String[] privod_arr_drom = {"","&privod=1","&privod=2","&privod=3"}; //luboi per zad poln
                String privod_drom_req = "";

                if(privod_str.length()==3){
                    privod_drom_req = privod_arr_drom[0];
                }
                else
                    privod_drom_req = privod_arr_drom[Integer.parseInt(privod[1])];



                for(int i = 1; i < privod.length; ++i){
                    privod_avto_req+=privod_arr_avto[Integer.parseInt(privod[i])];
                    privod_avito_req+=privod_arr_avito[Integer.parseInt(privod[i])];
                }
                if(privod_str.equals("123"))
                    privod_avto_req="";


                //body
                String body_str = sPref.getString("Body","");
                if(body_str.equals(""))
                    body_str="1234567890";
                String[] body = body_str.split("");

                String[] body_arr_avito = {"","869-","872-","870-","4804-","4806-","867-","866-","865-","871-","868-"};
                String body_avito_req = ".187_";

                String[] body_arr_avto = {"","&search%5Bbody_type%5D%5Bg_sedan%5D=1","&search%5Bbody_type%5D%5Bg_hatchback%5D=1","&search%5Bbody_type%5D%5Bg_wagon%5D=1","&search%5Bbody_type%5D%5Bg_offroad%5D=1","&search%5Bbody_type%5D%5Bg_minivan%5D=1","&search%5Bbody_type%5D%5Bg_limousine%5D=1","&search%5Bbody_type%5D%5Bg_coupe%5D=1","&search%5Bbody_type%5D%5Bg_cabrio%5D=1","&search%5Bbody_type%5D%5Bg_furgon%5D=1","&search%5Bbody_type%5D%5Bg_pickup%5D=1"};
                String body_avto_req = "";

                String[] body_arr_drom = {"","&frametype10=10","&frametype5=5","&frametype3=3","&frametype7=7",
                        "&frametype6=6","","&frametype1=1","&frametype11=11",
                        "","&frametype12=12"};
                String body_drom_req = "";

                for(int i = 1; i < body.length; ++i){
                    if(Integer.parseInt(body[i])==0){
                        body_avto_req += body_arr_avto[10];
                        body_avito_req += body_arr_avito[10];
                        body_drom_req += body_arr_drom[10];
                        continue;
                    }
                    body_avto_req+=body_arr_avto[Integer.parseInt(body[i])];
                    body_avito_req+=body_arr_avito[Integer.parseInt(body[i])];
                    body_drom_req+=body_arr_drom[Integer.parseInt(body[i])];
                }
                if(body_str.equals("1234567890")) {
                    body_avto_req = "";
                    body_drom_req = "";
                }


                //constructor for auto.ru
                String begin = "http://auto.ru/cars";
                String end = "/all/?sort%5Bcreate_date%5D=desc&search%5Bgeo_region%5D=38%2C87";
                String year1="&search%5Byear%5D%5Bmin%5D=";
                String year2="&search%5Byear%5D%5Bmax%5D=";
                String price1="&search%5Bprice%5D%5Bmin%5D="+startPrice+"%D1%80%D1%83%D0%B1.";
                String price2="&search%5Bprice%5D%5Bmax%5D="+endPrice+"%D1%80%D1%83%D0%B1.";
                String photo ="";
                String eng_vol1 = "&search%5Bengine_volume%5D%5Bmin%5D=";
                String eng_vol2 = "&search%5Bengine_volume%5D%5Bmax%5D=";
                String probeg = "&search%5Brun%5D%5Bmax%5D="+probeg_arr_avto[probegval]+"%D0%BA%D0%BC";

                //constructor for avito
                String begin_avito = "https://www.avito.ru/moskva/avtomobili";
                Map<Integer, String> map = new HashMap<Integer, String>();
                //region map create
                map.put(1970,"782");
                map.put(1980,"873");
                map.put(1985,"878");
                map.put(1990,"883");
                map.put(1991,"884");
                map.put(1992,"885");
                map.put(1993,"886");
                map.put(1994,"887");
                map.put(1995,"888");
                map.put(1996,"889");
                map.put(1997,"890");
                map.put(1998,"891");
                map.put(1999,"892");
                map.put(2000,"893");
                map.put(2001,"894");
                map.put(2002,"895");
                map.put(2003,"896");
                map.put(2004,"897");
                map.put(2005,"898");
                map.put(2006,"899");
                map.put(2007,"900");
                map.put(2008,"901");
                map.put(2009,"902");
                map.put(2010,"2844");
                map.put(2011,"2845");
                map.put(2012,"6045");
                map.put(2013,"8581");
                map.put(2014,"11017");
                map.put(2015,"13978");
                //endregion
                String startYearAvito = map.get(startYear);
                String endYearAvito = map.get(endYear);
                String year1a = "188_";
                String year2a = "b";
                String price1a = "&pmin=";
                String price2a = "&pmax=";
                String photoa = "";
                String eng_vol1a = "1374_";
                String eng_vol2a = "b";
                String probega = "1375_"+"15483"+"b"+probeg_arr_avito[probegval];

                //constructor for drom
                String begin_drom = "http://moscow.drom.ru/auto";
                String end_drom = "/all/page@@@page/?order_d=dsc";
                String photodrom ="";
                String price1drom = "&minprice=";
                String price2drom = "&maxprice=";
                String year1drom = "&minyear=";
                String year2drom = "&maxyear=";
                String eng_vol1drom = "&mv=";
                String eng_vol2drom = "&xv=";


                //photo
                CheckBox is_photo = (CheckBox) this.findViewById(R.id.checkBox);
                if(is_photo.isChecked()){
                    photo ="&search%5Bphoto%5D%5B1%5D=1";
                    photoa = "&i=1";
                    photodrom = "&ph=1";
                }


//тут
                String posMarkString = sPref.getString("SelectedMark", "Любая");
                String posModelString = sPref.getString("SelectedModel", "Любая" );

                //get mark
                String marka = "";
                String markaavito = "";
                String markadrom = "";
                String marka_for_dialog = "###";

                if(!posMarkString.equals("Любая")) {
                    Cursor cursorMark = db.query("marksTable", null, "markauser=?", new String[]{posMarkString}, null, null, null);
                    cursorMark.moveToFirst();

                    marka = "/" + cursorMark.getString(cursorMark.getColumnIndex("markarequest"));
                    markaavito = "/" + cursorMark.getString(cursorMark.getColumnIndex("markarequestavito"));
                    markadrom = "/" + cursorMark.getString(cursorMark.getColumnIndex("markarequestdrom"));
                    marka_for_dialog = cursorMark.getString(cursorMark.getColumnIndex("markauser"));
                }
                //get model
                String model = "";
                String modelavito = "";
                String modeldrom = "";
                String model_for_dialog = "###";
                if(!posModelString.equals("Любая")) {
                    Cursor cursorModel = db.query("modelsTable", null, "modeluser=?", new String[]{posModelString}, null, null, null);
                    cursorModel.moveToFirst();
                    model = "/" + cursorModel.getString(cursorModel.getColumnIndex("modelrequest"));
                    modelavito = "/" + cursorModel.getString(cursorModel.getColumnIndex("modelrequestavito"));
                    modeldrom = "/" + cursorModel.getString(cursorModel.getColumnIndex("modelrequestdrom"));
                    model_for_dialog = cursorModel.getString(cursorModel.getColumnIndex("modeluser"));
                }

                sPref.edit().putString("marka_for_dialog",marka_for_dialog).commit();
                sPref.edit().putString("model_for_dialog",model_for_dialog).commit();

                //put two plus one request
                String requestauto = "###";
                String requestavito = "###";
                String requestdrom = "###";
                if(!(marka.equals("/###")) && !(model.equals("/###"))&& isUseSearchInAuto)
                    requestauto = begin + marka + model + end + year1 + startYear.toString() + year2 + endYear.toString() + price1 + price2+photo+eng_vol1+volume_arr_avto[startVolume]+eng_vol2+volume_arr_avto[endVolume]+probeg+body_avto_req+privod_avto_req+trans_avto_req+engine_avto_req;
                if(!(markaavito.equals("/###")) && !(modelavito.equals("/###")) && isUseSearchInAvito)
                    requestavito = begin_avito+markaavito+modelavito+"/?"+photoa+price1a+startPrice+price2a+endPrice+"&f="+year1a+startYearAvito+year2a+endYearAvito+"."+eng_vol1a+volume_arr_avito[startVolume]+eng_vol2a+volume_arr_avito[endVolume]+"."+probega+body_avito_req+privod_avito_req+trans_avito_req+engine_avito_req;

                if(!(markadrom.equals("/###")) && !(modeldrom.equals("/###")) && drom_req_true!=false && isUseSearchInDrom)
                    requestdrom = begin_drom + markadrom + modeldrom + end_drom + photodrom + price1drom + startPrice + price2drom
                            + endPrice + year1drom + startYeard + year2drom+ endYeard
                            + eng_vol1drom + volume_arr_drom[startVolume] + eng_vol2drom + volume_arr_drom[endVolume] + trans_drom_req
                            + engine_drom_req + privod_drom_req + body_drom_req + "&go_search=2";

                ed.putString("SearchMyCarRequest", requestauto);
                ed.putString("SearchMyCarRequestAvito", requestavito);
                ed.putString("SearchMyCarRequestDrom", requestdrom);

                ed.putBoolean("SearchMyCarIsFromService", false);
                ed.putInt("SearchMyCarServiceID", -1);
                ed.commit();
                startActivity(intent);

                break;
            case R.id.marka_button:

                //clear model
                sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
                sPref.edit().putString("SelectedModel", "Любая").commit();

                Cursor cursor = db.query("marksTable", null, null, null, null, null, null);
                String strToParse = "Любая@@@";

                if (cursor.moveToFirst()) {
                    int MarkColIndex = cursor.getColumnIndex("markauser");
                    do {

                        strToParse += cursor.getString(MarkColIndex) + "@@@";
                    } while (cursor.moveToNext());
                }
                String[] marks_arr = strToParse.split("@@@");
                Intent intent2 = new Intent(this, ListOfMarkActivity.class);
                intent2.putExtra("Marks",marks_arr);
                startActivity(intent2);
                break;
            case R.id.model_button:

                SharedPreferences sPref2 = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
//тут
                String pos = sPref2.getString("SelectedMark", "Любая");
                if(pos.equals("Любая")){
                    Toast t = Toast.makeText(getApplicationContext(),"Для начала выберите марку",Toast.LENGTH_SHORT);
                    t.show();
                    break;
                }

                Cursor cursor3 = db.query("marksTable", null, "markauser=?", new String[]{pos}, null, null, null);
                cursor3.moveToFirst();
                Integer markId = cursor3.getColumnIndex("id");
                String markIdValue = cursor3.getString(markId);
                Cursor cursor2 = db.query("modelsTable", null, "marka_id=?", new String[]{markIdValue}, null, null, null);
                String strToParse2 = "Любая@@@";

                if (cursor2.moveToFirst()) {
                    int ModelColIndex = cursor2.getColumnIndex("modeluser");
                    do {

                        strToParse2 += cursor2.getString(ModelColIndex) + "@@@";
                    } while (cursor2.moveToNext());
                }
                String[] models_arr = strToParse2.split("@@@");
                Intent intent3 = new Intent(this, ListOfMarkActivity.class);
                intent3.putExtra("Models",models_arr);
                startActivity(intent3);
                break;
            case R.id.year_button:
                showPickerDialog();
                break;
            case R.id.price_button:
                showPickerPrice();
                break;
            case R.id.engine_volume_button:
                showPickerEngineVolume();
                break;
            case R.id.probeg_button:
                showPickerProbeg();
                break;
            case R.id.trans_button:
                showMultiChecker(1);
                break;
            case R.id.engine_button:
                showMultiChecker(2);
                break;
            case R.id.privod_button:
                showMultiChecker(3);
                break;
            case R.id.body_button:
                showMultiChecker(4);
                break;
            default:
                break;
        }
        db.close();
    }

    class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {

            super(context, "pcars27DB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table modelsTable ("
                    + "id integer primary key autoincrement,"
                    + "modeluser text," + "modelrequest text,"+"modelrequestavito text,"+"modelrequestdrom text,"
                    + "marka_id integer" + ");");

            db.execSQL("create table marksTable ("
                    + "id integer primary key autoincrement,"
                    + "markauser text," + "markarequest text," +"markarequestavito text,"+"markarequestdrom text"
                    + ");");

            /**
             * 1. for user
             * 2. for request
             * 3. id
             */
            //String[] marks_arr; //= new String[]{"audi","bmw","ac"};
            //region str1

            String marks_one_str = TextUtils.join("",new String[]{"AC @@@ ac @@@ ac @@@ ac @@@ 1 @@@ Acura @@@ acura @@@ acura @@@ acura @@@ 2 @@@ Alfa Romeo @@@ alfa-romeo @@@ alfa_romeo @@@ alfa_romeo @@@ 3 @@@ Alpina @@@ alpina @@@ ### @@@ alpina @@@ 4 @@@ Alpine @@@ alpine @@@ ### @@@ ### @@@ 5 @@@ AM General @@@ am-general @@@ ### @@@ ### @@@ 6 @@@ Ariel @@@ ariel @@@ ### @@@ ### @@@ 7 @@@ Aro @@@ aro @@@ aro @@@ ### @@@ 8 @@@ Asia @@@ asia @@@ asia @@@ asia @@@ 9 @@@ Aston Martin @@@ aston-martin @@@ aston_martin @@@ aston_martin @@@ 10 @@@ Audi @@@ audi @@@ audi @@@ audi @@@ 11 @@@ Austin @@@ austin @@@ ### @@@ ### @@@ 12 @@@ Autobianchi @@@ autobianchi @@@ ### @@@ ### @@@ 13 @@@ Baltijas Dzips @@@ baltijas-dzips @@@ ### @@@ ### @@@ 14 @@@ BAW @@@ ### @@@ baw @@@ ### @@@ 15 @@@ Beijing @@@ beijing @@@ ### @@@ ### @@@ 16 @@@ Bentley @@@ bentley @@@ bentley @@@ bentley @@@ 17 @@@ Bertone @@@ bertone @@@ ### @@@ ### @@@ 18 @@@ Bitter @@@ bitter @@@ ### @@@ ### @@@ 19 @@@ BMW @@@ bmw @@@ bmw @@@ bmw @@@ 20 @@@ Brabus @@@ brabus @@@ ### @@@ ### @@@ 21 @@@ Brilliance @@@ brilliance @@@ brilliance @@@ brilliance @@@ 22 @@@ Bristol @@@ bristol @@@ ### @@@ ### @@@ 23 @@@ Bufori @@@ bufori @@@ ### @@@ ### @@@ 24 @@@ Bugatti @@@ bugatti @@@ ### @@@ bugatti @@@ 25 @@@ Buick @@@ buick @@@ buick @@@ buick @@@ 26 @@@ BYD @@@ byd @@@ byd @@@ byd @@@ 27 @@@ Byvin @@@ byvin @@@ ### @@@ ### @@@ 28 @@@ Cadillac @@@ cadillac @@@ cadillac @@@ cadillac @@@ 29 @@@ Callaway @@@ callaway @@@ ### @@@ ### @@@ 30 @@@ Carbodies @@@ carbodies @@@ ### @@@ ### @@@ 31 @@@ Caterham @@@ caterham @@@ ### @@@ ### @@@ 32 @@@ Changan @@@ changan @@@ changan @@@ changan @@@ 33 @@@ ChangFeng @@@ changfeng @@@ ### @@@ ### @@@ 34 @@@ Chery @@@ chery @@@ chery @@@ chery @@@ 35 @@@ Chevrolet @@@ chevrolet @@@ chevrolet @@@ chevrolet @@@ 36 @@@ Chrysler @@@ chrysler @@@ chrysler @@@ chrysler @@@ 37 @@@ Citroen @@@ citroen @@@ citroen @@@ citroen @@@ 38 @@@ Cizeta @@@ cizeta @@@ ### @@@ ### @@@ 39 @@@ Coggiola @@@ coggiola @@@ ### @@@ ### @@@ 40 @@@ Dacia @@@ dacia @@@ dacia @@@ dacia @@@ 41 @@@ Dadi @@@ dadi @@@ ### @@@ ### @@@ 42 @@@ Daewoo @@@ daewoo @@@ daewoo @@@ daewoo @@@ 43 @@@ DAF @@@ daf @@@ ### @@@ ### @@@ 44 @@@ Daihatsu @@@ daihatsu @@@ daihatsu @@@ daihatsu @@@ 45 @@@ Daimler @@@ daimler @@@ ### @@@ daimler @@@ 46 @@@ Datsun @@@ datsun @@@ datsun @@@ datsun @@@ 47 @@@ De Tomaso @@@ de-tomaso @@@ ### @@@ ### @@@ 48 @@@ DeLorean @@@ delorean @@@ ### @@@ ### @@@ 49 @@@ Derways @@@ derways @@@ ### @@@ derways @@@ 50 @@@ Dodge @@@ dodge @@@ dodge @@@ dodge @@@ 51 @@@ DongFeng @@@ dongfeng @@@ dong_feng @@@ dongfeng @@@ 52 @@@ Doninvest @@@ doninvest @@@ ### @@@ ### @@@ 53 @@@ Donkervoort @@@ donkervoort @@@ ### @@@ ### @@@ 54 @@@ DS @@@ ds @@@ ### @@@ ### @@@ 55 @@@ E-Car @@@ e-car @@@ ### @@@ ### @@@ 56 @@@ Eagle @@@ eagle @@@ ### @@@ eagle @@@ 57 @@@ Eagle Cars @@@ eagle-cars @@@ ### @@@ ### @@@ 58 @@@ Ecomotors @@@ ecomotors @@@ ### @@@ ### @@@ 59 @@@ FAW @@@ faw @@@ faw @@@ faw @@@ 60 @@@ Ferrari @@@ ferrari @@@ ferrari @@@ ferrari @@@ 61 @@@ Fiat @@@ fiat @@@ fiat @@@ fiat @@@ 62 @@@ Fisker @@@ fisker @@@ ### @@@ fisker @@@ 63 @@@ Ford @@@ ford @@@ ford @@@ ford @@@ 64 @@@ Foton @@@ foton @@@ ### @@@ ### @@@ 65 @@@ FSO @@@ fso @@@ ### @@@ ### @@@ 66 @@@ Fuqi @@@ fuqi @@@ ### @@@ ### @@@ 67 @@@ Geely @@@ geely @@@ geely @@@ geely @@@ 68 @@@ Geo @@@ geo @@@ ### @@@ geo @@@ 69 @@@ GMC @@@ gmc @@@ gmc @@@ gmc @@@ 70 @@@ Gonow @@@ gonow @@@ ### @@@ ### @@@ 71 @@@ Gordon @@@ gordon @@@ ### @@@ ### @@@ 72 @@@ Great Wall @@@ great-wall @@@ great_wall @@@ great_wall @@@ 73 @@@ Hafei @@@ hafei @@@ hafei @@@ hafei @@@ 74 @@@ Haima @@@ haima @@@ haima @@@ haima @@@ 75 @@@ Haval @@@ haval @@@ haval @@@ haval @@@ 76 @@@ Hawtai @@@ hawtai @@@ ### @@@ ### @@@ 77 @@@ Hindustan @@@ hindustan @@@ ### @@@ ### @@@ 78 @@@ Holden @@@ holden @@@ ### @@@ ### @@@ 79 @@@ Honda @@@ honda @@@ honda @@@ honda @@@ 80 @@@ HuangHai @@@ huanghai @@@ huanghai @@@ ### @@@ 81 @@@ Hummer @@@ hummer @@@ hummer @@@ hummer @@@ 82 @@@ Hyundai @@@ hyundai @@@ hyundai @@@ hyundai @@@ 83 @@@ Infiniti @@@ infiniti @@@ infiniti @@@ infiniti @@@ 84 @@@ Innocenti @@@ innocenti @@@ ### @@@ ### @@@ 85 @@@ Invicta @@@ invicta @@@ ### @@@ ### @@@ 86 @@@ Iran Khodro @@@ ikco @@@ iran_khodro @@@ iran_khodro @@@ 87 @@@ Isdera @@@ isdera @@@ ### @@@ ### @@@ 88 @@@ Isuzu @@@ isuzu @@@ isuzu @@@ isuzu @@@ 89 @@@ IVECO @@@ iveco @@@ iveco @@@ ### @@@ 90 @@@ JAC @@@ jac @@@ jac @@@ jac @@@ 91 @@@ Jaguar @@@ jaguar @@@ jaguar @@@ jaguar @@@ 92 @@@ Jeep @@@ jeep @@@ jeep @@@ jeep @@@ 93 @@@ Jensen @@@ jensen @@@ ### @@@ ### @@@ 94 @@@ JMC @@@ jmc @@@ jmc @@@ ### @@@ 95 @@@ Kia @@@ kia @@@ kia @@@ kia @@@ 96 @@@ Koenigsegg @@@ koenigsegg @@@ ### @@@ koenigsegg @@@ 97 @@@ KTM @@@ ktm @@@ ### @@@ ### @@@ 98 @@@ Lamborghini @@@ lamborghini @@@ lamborghini @@@ lamborghini @@@ 99 @@@ Lancia @@@ lancia @@@ lancia @@@ lancia @@@ 100 @@@ Land Rover @@@ land-rover @@@ land_rover @@@ land_rover @@@ 101 @@@ Landwind @@@ landwind @@@ ldv @@@ ### @@@ 102 @@@ Lexus @@@ lexus @@@ lexus @@@ lexus @@@ 103 @@@ Liebao Motor @@@ liebao-motor @@@ ### @@@ ### @@@ 104 @@@ Lifan @@@ lifan @@@ lifan @@@ lifan @@@ 105 @@@ Lincoln @@@ lincoln @@@ lincoln @@@ lincoln @@@ 106 @@@ Lotus @@@ lotus @@@ ### @@@ lotus @@@ 107 @@@ LTI @@@ lti @@@ ### @@@ ### @@@ 108 @@@ Luxgen @@@ luxgen @@@ ### @@@ luxgen @@@ 109 @@@ Mahindra @@@ mahindra @@@ ### @@@ ### @@@ 110 @@@ Marcos @@@ marcos @@@ ### @@@ ### @@@ 111 @@@ Marlin @@@ marlin @@@ ### @@@ ### @@@ 112 @@@ Marussia @@@ marussia @@@ ### @@@ marussia @@@ 113 @@@ Maruti @@@ maruti @@@ ### @@@ ### @@@ 114 @@@ Maserati @@@ maserati @@@ maserati @@@ maserati @@@ 115 @@@ Maybach @@@ maybach @@@ maybach @@@ maybach @@@ 116 @@@ Mazda @@@ mazda @@@ mazda @@@ mazda @@@ 117 @@@ McLaren @@@ mclaren @@@ ### @@@ mclaren @@@ 118 @@@ Mega @@@ mega @@@ ### @@@ ### @@@ 119 @@@ Mercedes-Benz @@@ mercedes @@@ mercedes-benz @@@ mercedes-benz @@@ 120 @@@ Mercury @@@ mercury @@@ mercury @@@ mercury @@@ 121 @@@ Metrocab @@@ metrocab @@@ ### @@@ ### ","@@@ 122 @@@ MG @@@ mg @@@ mg @@@ ### @@@ 123 @@@ Microcar @@@ microcar @@@ ### @@@ ### @@@ 124 @@@ Minelli @@@ minelli @@@ ### @@@ ### @@@ 125 @@@ MINI @@@ mini @@@ mini @@@ mini @@@ 126 @@@ Mitsubishi @@@ mitsubishi @@@ mitsubishi @@@ mitsubishi @@@ 127 @@@ Mitsuoka @@@ mitsuoka @@@ ### @@@ mitsuoka @@@ 128 @@@ Morgan @@@ morgan @@@ ### @@@ ### @@@ 129 @@@ Morris @@@ morris @@@ ### @@@ ### @@@ 130 @@@ Nissan @@@ nissan @@@ nissan @@@ nissan @@@ 131 @@@ Noble @@@ noble @@@ ### @@@ ### @@@ 132 @@@ Oldsmobile @@@ oldsmobile @@@ oldsmobile @@@ oldsmobile @@@ 133 @@@ Opel @@@ opel @@@ opel @@@ opel @@@ 134 @@@ Osca @@@ osca @@@ ### @@@ ### @@@ 135 @@@ Pagani @@@ pagani @@@ ### @@@ pagani @@@ 136 @@@ Panoz @@@ panoz @@@ ### @@@ ### @@@ 137 @@@ Perodua @@@ perodua @@@ ### @@@ ### @@@ 138 @@@ Peugeot @@@ peugeot @@@ peugeot @@@ peugeot @@@ 139 @@@ PGO @@@ pgo @@@ ### @@@ ### @@@ 140 @@@ Piaggio @@@ piaggio @@@ ### @@@ ### @@@ 141 @@@ Plymouth @@@ plymouth @@@ plymouth @@@ plymouth @@@ 142 @@@ Pontiac @@@ pontiac @@@ pontiac @@@ pontiac @@@ 143 @@@ Porsche @@@ porsche @@@ porsche @@@ porsche @@@ 144 @@@ Premier @@@ premier @@@ ### @@@ ### @@@ 145 @@@ Proton @@@ proton @@@ proton @@@ proton @@@ 146 @@@ PUCH @@@ puch @@@ ### @@@ ### @@@ 147 @@@ Puma @@@ puma @@@ ### @@@ ### @@@ 148 @@@ Qoros @@@ qoros @@@ ### @@@ ### @@@ 149 @@@ Qvale @@@ qvale @@@ ### @@@ ### @@@ 150 @@@ Reliant @@@ reliant @@@ ### @@@ ### @@@ 151 @@@ Renaissance @@@ renaissance @@@ ### @@@ ### @@@ 152 @@@ Renault @@@ renault @@@ renault @@@ renault @@@ 153 @@@ Renault Samsung @@@ renault-samsung @@@ ### @@@ renault_samsung @@@ 154 @@@ Rezvani @@@ rezvani @@@ ### @@@ ### @@@ 155 @@@ Rimac @@@ rimac @@@ ### @@@ ### @@@ 156 @@@ Rolls-Royce @@@ rolls-royce @@@ rolls_royce @@@ rolls-royce @@@ 157 @@@ Ronart @@@ ronart @@@ ### @@@ ### @@@ 158 @@@ Rover @@@ rover @@@ rover @@@ rover @@@ 159 @@@ Saab @@@ saab @@@ saab @@@ saab @@@ 160 @@@ Saleen @@@ saleen @@@ ### @@@ ### @@@ 161 @@@ Santana @@@ santana @@@ ### @@@ ### @@@ 162 @@@ Saturn @@@ saturn @@@ saturn @@@ saturn @@@ 163 @@@ Scion @@@ scion @@@ scion @@@ scion @@@ 164 @@@ SEAT @@@ seat @@@ seat @@@ seat @@@ 165 @@@ ShuangHuan @@@ shuanghuan @@@ ### @@@ ### @@@ 166 @@@ Skoda @@@ skoda @@@ skoda @@@ skoda @@@ 167 @@@ Smart @@@ smart @@@ smart @@@ smart @@@ 168 @@@ Soueast @@@ soueast @@@ ### @@@ ### @@@ 169 @@@ Spectre @@@ spectre @@@ ### @@@ ### @@@ 170 @@@ Spyker @@@ spyker @@@ ### @@@ ### @@@ 171 @@@ SsangYong @@@ ssang-yong @@@ ssangyong @@@ ssang_yong @@@ 172 @@@ Subaru @@@ subaru @@@ subaru @@@ subaru @@@ 173 @@@ Suzuki @@@ suzuki @@@ suzuki @@@ suzuki @@@ 174 @@@ Talbot @@@ talbot @@@ ### @@@ ### @@@ 175 @@@ TATA @@@ tata @@@ ### @@@ ### @@@ 176 @@@ Tatra @@@ tatra @@@ ### @@@ ### @@@ 177 @@@ Tazzari @@@ tazzari @@@ ### @@@ ### @@@ 178 @@@ Tesla @@@ tesla @@@ tesla @@@ tesla @@@ 179 @@@ Tianma @@@ tianma @@@ ### @@@ ### @@@ 180 @@@ Tianye @@@ tianye @@@ ### @@@ tianye @@@ 181 @@@ Tofas @@@ tofas @@@ ### @@@ ### @@@ 182 @@@ Toyota @@@ toyota @@@ toyota @@@ toyota @@@ 183 @@@ Trabant @@@ trabant @@@ ### @@@ ### @@@ 184 @@@ Tramontana @@@ tramontana @@@ ### @@@ ### @@@ 185 @@@ Triumph @@@ triumph @@@ ### @@@ ### @@@ 186 @@@ TVR @@@ tvr @@@ ### @@@ tvr @@@ 187 @@@ Ultima @@@ ultima @@@ ### @@@ ### @@@ 188 @@@ Vauxhall @@@ vauxhall @@@ ### @@@ ### @@@ 189 @@@ Vector @@@ vector @@@ ### @@@ ### @@@ 190 @@@ Venturi @@@ venturi @@@ ### @@@ ### @@@ 191 @@@ Volkswagen @@@ volkswagen @@@ volkswagen @@@ volkswagen @@@ 192 @@@ Volvo @@@ volvo @@@ volvo @@@ volvo @@@ 193 @@@ Vortex @@@ vortex @@@ vortex @@@ vortex @@@ 194 @@@ Wartburg @@@ wartburg @@@ ### @@@ ### @@@ 195 @@@ Westfield @@@ westfield @@@ ### @@@ ### @@@ 196 @@@ Wiesmann @@@ wiesmann @@@ ### @@@ wiesmann @@@ 197 @@@ Xin Kai @@@ xin-kai @@@ ### @@@ xin_kai @@@ 198 @@@ Zastava @@@ zastava @@@ ### @@@ ### @@@ 199 @@@ Zenvo @@@ zenvo @@@ ### @@@ ### @@@ 200 @@@ Zotye @@@ zotye @@@ ### @@@ ### @@@ 201 @@@ ZX @@@ zx @@@ zx @@@ zx @@@ 202 @@@ Автокам @@@ avtokam @@@ ### @@@ ### @@@ 203 @@@ Астро @@@ astro @@@ ### @@@ ### @@@ 204 @@@ Бронто @@@ bronto @@@ ### @@@ ### @@@ 205 @@@ ВАЗ (Lada) @@@ vaz @@@ vaz_lada @@@ lada @@@ 206 @@@ ВИС @@@ ### @@@ vis @@@ ### @@@ 207 @@@ ГАЗ @@@ gaz @@@ gaz @@@ gaz @@@ 208 @@@ Ё-мобиль @@@ e-mobil @@@ ### @@@ ### @@@ 209 @@@ ЗАЗ @@@ zaz @@@ zaz @@@ zaz @@@ 210 @@@ ЗИЛ @@@ zil @@@ zil @@@ ### @@@ 211 @@@ ИЖ @@@ izh @@@ izh @@@ izh @@@ 212 @@@ КамАЗ @@@ kamaz @@@ ### @@@ ### @@@ 213 @@@ Канонир @@@ kanonir @@@ ### @@@ ### @@@ 214 @@@ ЛуАЗ @@@ luaz @@@ luaz @@@ luaz @@@ 215 @@@ Москвич @@@ moscvich @@@ moskvich @@@ moskvitch @@@ 216 @@@ РАФ @@@ ### @@@ raf @@@ ### @@@ 217 @@@ СеАЗ @@@ seaz @@@ ### @@@ ### @@@ 218 @@@ СМЗ @@@ smz @@@ ### @@@ ### @@@ 219 @@@ ТагАЗ @@@ tagaz @@@ tagaz @@@ tagaz @@@ 220 @@@ УАЗ @@@ uaz @@@ uaz @@@ uaz @@@ 221"});

            //endregion
            String[] marks_split = marks_one_str.split(" @@@ ");
            for(int j=0; j<marks_split.length;j+=5){
                ContentValues cv = new ContentValues();
                //cv.put("id", marks_split[i+2]);
                cv.put("markauser", marks_split[j]);
                cv.put("markarequest", marks_split[j+1]);
                cv.put("markarequestavito", marks_split[j+2]);
                cv.put("markarequestdrom", marks_split[j+3]);
                db.insert("marksTable", null, cv);
            }
            /**
             * 1. for user
             * 2. for request
             * 3. for avito
             * 4. marka_id
             */
            //region str2
            String models_one_str = TextUtils.join("",new String[]{"378 GT Zagato @@@ 378-gt-zagato @@@ ### @@@ ### @@@ 1 @@@ Ace @@@ ace @@@ ### @@@ ### @@@ 1 @@@ Aceca @@@ aceca @@@ aceca @@@ ### @@@ 1 @@@ Cobra @@@ cobra @@@ cobra @@@ cobra @@@ 1 @@@ CL @@@ cl @@@ ### @@@ cl @@@ 2 @@@ CSX @@@ csx @@@ csx @@@ ### @@@ 2 @@@ EL @@@ el @@@ el @@@ ### @@@ 2 @@@ ILX @@@ ilx @@@ ### @@@ ilx @@@ 2 @@@ Integra @@@ integra @@@ ### @@@ integra @@@ 2 @@@ Legend @@@ legend @@@ ### @@@ legend @@@ 2 @@@ MDX @@@ mdx @@@ mdx @@@ mdx @@@ 2 @@@ NSX @@@ nsx @@@ ### @@@ nsx @@@ 2 @@@ RDX @@@ rdx @@@ rdx @@@ rdx @@@ 2 @@@ RL @@@ rl @@@ ### @@@ rl @@@ 2 @@@ RLX @@@ rlx @@@ ### @@@ rlx @@@ 2 @@@ RSX @@@ rsx @@@ rsx @@@ rsx @@@ 2 @@@ SLX @@@ slx @@@ ### @@@ ### @@@ 2 @@@ TL @@@ tl @@@ tl @@@ tl @@@ 2 @@@ TLX @@@ tlx @@@ tlx @@@ tlx @@@ 2 @@@ TSX @@@ tsx @@@ tsx @@@ tsx @@@ 2 @@@ ZDX @@@ zdx @@@ zdx @@@ zdx @@@ 2 @@@ 6 @@@ 6 @@@ ### @@@ ### @@@ 3 @@@ 33 @@@ 33 @@@ 33 @@@ 33 @@@ 3 @@@ 75 @@@ 75 @@@ ### @@@ 75 @@@ 3 @@@ 90 @@@ 90 @@@ ### @@@ ### @@@ 3 @@@ 145 @@@ 145 @@@ 145 @@@ 145 @@@ 3 @@@ 146 @@@ 146 @@@ 146 @@@ 146 @@@ 3 @@@ 147 @@@ 147 @@@ 147 @@@ 147 @@@ 3 @@@ 155 @@@ 155 @@@ 155 @@@ 155 @@@ 3 @@@ 156 @@@ 156 @@@ 156 @@@ 156 @@@ 3 @@@ 159 @@@ 159 @@@ 159 @@@ 159 @@@ 3 @@@ 164 @@@ 164 @@@ 164 @@@ 164 @@@ 3 @@@ 166 @@@ 166 @@@ 166 @@@ 166 @@@ 3 @@@ 4C @@@ 4c @@@ ### @@@ 4c @@@ 3 @@@ 8C Competizione @@@ 8c @@@ ### @@@ 8c @@@ 3 @@@ Alfasud @@@ alfasud @@@ ### @@@ ### @@@ 3 @@@ Alfetta @@@ alfetta @@@ ### @@@ ### @@@ 3 @@@ Arna @@@ arna @@@ ### @@@ ### @@@ 3 @@@ Brera @@@ brera @@@ brera @@@ brera @@@ 3 @@@ Disco Volante @@@ disco-volante @@@ ### @@@ ### @@@ 3 @@@ Giulia @@@ giulia @@@ ### @@@ giulia @@@ 3 @@@ Giulietta @@@ giulietta @@@ giulietta @@@ giulietta @@@ 3 @@@ GT @@@ gt @@@ gt @@@ gt @@@ 3 @@@ GTA Coupe @@@ gta_coupe @@@ ### @@@ ### @@@ 3 @@@ GTV @@@ gtv @@@ ### @@@ gtv @@@ 3 @@@ MiTo @@@ mito @@@ mito @@@ mi-to @@@ 3 @@@ Montreal @@@ montreal @@@ ### @@@ ### @@@ 3 @@@ RZ @@@ rz @@@ ### @@@ ### @@@ 3 @@@ Spider @@@ spider @@@ spider @@@ spider @@@ 3 @@@ Sprint @@@ sprint @@@ ### @@@ ### @@@ 3 @@@ SZ @@@ sz @@@ ### @@@ ### @@@ 3 @@@ B10 @@@ b10 @@@ ### @@@ b10 @@@ 4 @@@ B11 @@@ b11 @@@ ### @@@ b11 @@@ 4 @@@ B12 @@@ b12 @@@ ### @@@ b12 @@@ 4 @@@ B3 @@@ b3 @@@ ### @@@ b3 @@@ 4 @@@ B4 @@@ b4 @@@ ### @@@ b4 @@@ 4 @@@ B5 @@@ b5 @@@ ### @@@ b5 @@@ 4 @@@ B6 @@@ b6 @@@ ### @@@ b6 @@@ 4 @@@ B7 @@@ b7 @@@ ### @@@ b7 @@@ 4 @@@ B8 @@@ b8 @@@ ### @@@ b8 @@@ 4 @@@ B9 @@@ b9 @@@ ### @@@ b9 @@@ 4 @@@ C1 @@@ c1 @@@ ### @@@ c1 @@@ 4 @@@ C2 @@@ c2 @@@ ### @@@ c2 @@@ 4 @@@ D10 @@@ d10 @@@ ### @@@ d10 @@@ 4 @@@ D3 @@@ d3 @@@ ### @@@ d3 @@@ 4 @@@ D5 @@@ d5 @@@ ### @@@ d5 @@@ 4 @@@ Roadster @@@ roadster @@@ ### @@@ roadster @@@ 4 @@@ XD3 @@@ xd3 @@@ ### @@@ xd3 @@@ 4 @@@ A110 @@@ a110 @@@ ### @@@ ### @@@ 5 @@@ A310 @@@ a310 @@@ ### @@@ ### @@@ 5 @@@ A610 @@@ a610 @@@ ### @@@ ### @@@ 5 @@@ GTA @@@ gta @@@ ### @@@ ### @@@ 5 @@@ HMMWV (Humvee) @@@ hmmwv @@@ ### @@@ ### @@@ 6 @@@ Atom @@@ atom @@@ ### @@@ ### @@@ 7 @@@ 10 @@@ 10 @@@ ### @@@ ### @@@ 8 @@@ 24 @@@ 24 @@@ 24 @@@ ### @@@ 8 @@@ Spartana @@@ ### @@@ spartana @@@ ### @@@ 8 @@@ Hi-topic @@@ ### @@@ hi-topic @@@ ### @@@ 9 @@@ Retona @@@ retona @@@ ### @@@ ### @@@ 9 @@@ Rocsta @@@ rocsta @@@ rocstra @@@ rocsta @@@ 9 @@@ Bulldog @@@ bulldog @@@ ### @@@ ### @@@ 10 @@@ Cygnet @@@ cygnet @@@ cygnet @@@ cygnet @@@ 10 @@@ DB7 @@@ db7 @@@ db7 @@@ db7 @@@ 10 @@@ DB9 @@@ db9-coupe @@@ db9 @@@ db9 @@@ 10 @@@ DBS @@@ dbs @@@ dbs @@@ dbs @@@ 10 @@@ Lagonda @@@ lagonda @@@ ### @@@ ### @@@ 10 @@@ One-77 @@@ one-77 @@@ ### @@@ one-77 @@@ 10 @@@ Rapide @@@ rapide @@@ rapide @@@ rapide @@@ 10 @@@ Rapide S @@@ ### @@@ rapide_s @@@ ### @@@ 10 @@@ Tickford Capri @@@ tickford_capri @@@ ### @@@ ### @@@ 10 @@@ V12 Vanquish @@@ v12-vanquish @@@ ### @@@ vanquish @@@ 10 @@@ V12 Vantage @@@ v12-vantage @@@ ### @@@ vantage @@@ 10 @@@ V12 Zagato @@@ v12-zagato @@@ ### @@@ zagato @@@ 10 @@@ V8 Vantage @@@ v8-vantage @@@ v8_vantage @@@ v8 @@@ 10 @@@ V8 Vantage S @@@ ### @@@ v8_vantage_s @@@ v8 @@@ 10 @@@ V8 Zagato @@@ v8-zagato @@@ ### @@@ v8 @@@ 10 @@@ Vanquish @@@ ### @@@ vanquish @@@ vanquish_volante @@@ 10 @@@ Virage @@@ virage @@@ ### @@@ virage @@@ 10 @@@ 50 @@@ 50 @@@ ### @@@ ### @@@ 11 @@@ 80 @@@ 80 @@@ 80 @@@ 80 @@@ 11 @@@ 100 @@@ 100 @@@ 100 @@@ 100 @@@ 11 @@@ 200 @@@ 200 @@@ 200 @@@ 200 @@@ 11 @@@ A1 @@@ a1 @@@ a1 @@@ a1 @@@ 11 @@@ A2 @@@ a2 @@@ a2 @@@ a2 @@@ 11 @@@ A3 @@@ a3 @@@ a3 @@@ a3 @@@ 11 @@@ A4 @@@ a4 @@@ a4 @@@ a4 @@@ 11 @@@ A4 Allroad Quattro @@@ a4-allroad @@@ a4_allroad_quattro @@@ a4_allroad_quattro @@@ 11 @@@ A5 @@@ a5 @@@ a5 @@@ a5 @@@ 11 @@@ A6 @@@ a6 @@@ a6 @@@ a6 @@@ 11 @@@ A6 Allroad Quattro @@@ a6-allroad @@@ a6_allroad_quattro @@@ a6_allroad_quattro @@@ 11 @@@ A7 @@@ a7 @@@ a7 @@@ a7 @@@ 11 @@@ A8 @@@ a8 @@@ a8 @@@ a8 @@@ 11 @@@ Cabriolet @@@ cabriolet @@@ cabriolet @@@ cabriolet @@@ 11 @@@ Coupe @@@ coupe @@@ coupe @@@ coupe @@@ 11 @@@ NSU RO 80 @@@ nsu-ro-80 @@@ ### @@@ ### @@@ 11 @@@ Q3 @@@ q3 @@@ q3 @@@ q3 @@@ 11 @@@ Q5 @@@ q5 @@@ q5 @@@ q5 @@@ 11 @@@ Q7 @@@ q7 @@@ q7 @@@ q7 @@@ 11 @@@ quattro @@@ quattro @@@ quattro @@@ quattro @@@ 11 @@@ R8 @@@ r8 @@@ r8 @@@ r8 @@@ 11 @@@ RS Q3 @@@ rs-q3 @@@ rs_q3 @@@ rs_q3 @@@ 11 @@@ RS2 @@@ rs2 @@@ ### @@@ ### @@@ 11 @@@ RS3 @@@ rs3 @@@ ### @@@ rs3 @@@ 11 @@@ RS4 @@@ rs4 @@@ rs4 @@@ rs4 @@@ 11 @@@ RS5 @@@ rs5 @@@ rs5 @@@ rs5 @@@ 11 @@@ RS6 @@@ rs6 @@@ rs6 @@@ rs6 @@@ 11 @@@ RS7 @@@ rs7 @@@ rs7 @@@ rs7 @@@ 11 @@@ S1 @@@ s1 @@@ ### @@@ ### @@@ 11 @@@ S2 @@@ s2 @@@ s2 @@@ s2 @@@ 11 @@@ S3 @@@ s3 @@@ s3 @@@ s3 @@@ 11 @@@ S4 @@@ s4 @@@ s4 @@@ s4 @@@ 11 @@@ S5 @@@ s5 @@@ s5 @@@ s5 @@@ 11 @@@ S6 @@@ s6 @@@ s6 @@@ s6 @@@ 11 @@@ S7 @@@ s7 @@@ ### @@@ s7 @@@ 11 @@@ S8 @@@ s8 @@@ s8 @@@ s8 @@@ 11 @@@ SQ5 @@@ sq5 @@@ ### @@@ sq5 @@@ 11 @@@ TT @@@ tt @@@ tt @@@ tt @@@ 11 @@@ TT RS @@@ tt-rs @@@ tt_rs @@@ tt_rs @@@ 11 @@@ TTS @@@ tts @@@ tts @@@ tts @@@ 11 @@@ V8 @@@ v8 @@@ v8 @@@ v8 @@@ 11 @@@ Allegro @@@ allegro @@@ ### @@@ ### @@@ 12 @@@ Ambassador @@@ ambassador @@@ ### @@@ ### @@@ 12 @@@ Maestro @@@ ma","estro @@@ ### @@@ ### @@@ 12 @@@ Maxi @@@ maxi @@@ ### @@@ ### @@@ 12 @@@ Metro @@@ metro @@@ ### @@@ ### @@@ 12 @@@ Mini @@@ mini @@@ ### @@@ ### @@@ 12 @@@ Montego @@@ montego @@@ ### @@@ ### @@@ 12 @@@ Princess @@@ princess @@@ ### @@@ ### @@@ 12 @@@ A 112 @@@ a-112 @@@ ### @@@ ### @@@ 13 @@@ BD-1322 @@@ bd @@@ ### @@@ ### @@@ 14 @@@ Fenix @@@ ### @@@ fenix @@@ ### @@@ 15 @@@ Tonik @@@ ### @@@ tonik @@@ ### @@@ 15 @@@ BJ2020 @@@ bj2020 @@@ ### @@@ ### @@@ 16 @@@ BJ212 @@@ bj212 @@@ ### @@@ ### @@@ 16 @@@ Arnage @@@ arnage @@@ arnage @@@ arnage @@@ 17 @@@ Azure @@@ azure @@@ azure @@@ azure @@@ 17 @@@ Brooklands @@@ brooklands @@@ brooklands @@@ ### @@@ 17 @@@ Continental @@@ continental @@@ continental @@@ continental @@@ 17 @@@ Continental Flying Spur @@@ continental-flying-sp @@@ continental_flying_spur @@@ ### @@@ 17 @@@ Continental GT @@@ continental-gt @@@ continental_gt @@@ continental_gt @@@ 17 @@@ Continental GTC @@@ ### @@@ continental_gtc @@@ ### @@@ 17 @@@ Continental Supersports @@@ ### @@@ continental_supersports @@@ ### @@@ 17 @@@ Eight @@@ eight @@@ ### @@@ eight @@@ 17 @@@ Flying Spur @@@ flying_spur @@@ ### @@@ flying_spur @@@ 17 @@@ Mulsanne @@@ mulsanne @@@ mulsanne @@@ mulsanne @@@ 17 @@@ Turbo R @@@ turbo_r @@@ turbo_r @@@ turbo_r @@@ 17 @@@ Freeclimber @@@ freeclimber @@@ ### @@@ ### @@@ 18 @@@ Type 3 @@@ type-3 @@@ ### @@@ ### @@@ 19 @@@ 02 (E10) @@@ 02-e10 @@@ ### @@@ ### @@@ 20 @@@ 1 серия @@@ 1er @@@ 1 @@@ 1-series @@@ 20 @@@ 1M @@@ 1m @@@ 1m @@@ ### @@@ 20 @@@ 2 серия @@@ 2er @@@ 2 @@@ 2-series @@@ 20 @@@ 2er Active Tourer @@@ 2er-active-tourer @@@ ### @@@ 2-series_active_tourer @@@ 20 @@@ 2er Grand Tourer @@@ 2er-grand-tourer @@@ ### @@@ 2-series_gran_tourer @@@ 20 @@@ 3 серия GT @@@ ### @@@ 3_gt @@@ 3-series_gran_turismo @@@ 20 @@@ 3 серия @@@ 3er @@@ 3 @@@ 3-series @@@ 20 @@@ 4 серия @@@ 4er @@@ 4 @@@ 4-series @@@ 20 @@@ 5 серия GT @@@ ### @@@ 5_gt @@@ 5-series_gran_turismo @@@ 20 @@@ 5 серия @@@ 5er @@@ 5 @@@ 5-series @@@ 20 @@@ 6 серия @@@ 6er @@@ 6 @@@ 6-series @@@ 20 @@@ 7 серия @@@ 7er @@@ 7 @@@ 7-series @@@ 20 @@@ 8er @@@ 8er @@@ 8 @@@ 8-series @@@ 20 @@@ i3 @@@ i3 @@@ i3 @@@ i3 @@@ 20 @@@ i8 @@@ i8 @@@ i8 @@@ i8 @@@ 20 @@@ M3 @@@ m3 @@@ m3 @@@ m3 @@@ 20 @@@ M4 @@@ m4 @@@ m4 @@@ m4 @@@ 20 @@@ M5 @@@ m5 @@@ m5 @@@ m5 @@@ 20 @@@ M6 @@@ m6 @@@ m6 @@@ m6 @@@ 20 @@@ X1 @@@ x1 @@@ x1 @@@ x1 @@@ 20 @@@ X3 @@@ x3 @@@ x3 @@@ x3 @@@ 20 @@@ X4 @@@ x4 @@@ x4 @@@ x4 @@@ 20 @@@ X5 @@@ x5 @@@ x5 @@@ x5 @@@ 20 @@@ X5 M @@@ x5m @@@ x5m @@@ ### @@@ 20 @@@ X6 @@@ x6 @@@ x6 @@@ x6 @@@ 20 @@@ X6 M @@@ x6m @@@ x6m @@@ ### @@@ 20 @@@ Z1 @@@ z1 @@@ ### @@@ z1 @@@ 20 @@@ Z3 @@@ z3 @@@ z3 @@@ z3 @@@ 20 @@@ Z3 M @@@ z3m @@@ z3m @@@ ### @@@ 20 @@@ Z4 @@@ z4 @@@ z4 @@@ z4 @@@ 20 @@@ Z4 M @@@ z4m @@@ z4m @@@ ### @@@ 20 @@@ Z8 @@@ z8 @@@ z8 @@@ z8 @@@ 20 @@@ 7.3S @@@ 73s @@@ ### @@@ ### @@@ 21 @@@ M V12 @@@ mv12 @@@ ### @@@ ### @@@ 21 @@@ SV12 @@@ sv12 @@@ ### @@@ ### @@@ 21 @@@ FRV (BS2) @@@ frv @@@ ### @@@ ### @@@ 22 @@@ H230 @@@ h230 @@@ ### @@@ h230 @@@ 22 @@@ H530 @@@ h530 @@@ h530 @@@ h530 @@@ 22 @@@ M1 (BS6) @@@ m1 @@@ ### @@@ m1 @@@ 22 @@@ M2 (BS4) @@@ m2 @@@ m2 @@@ m2 @@@ 22 @@@ M3 (BC3) @@@ m3 @@@ m3 @@@ m3 @@@ 22 @@@ V5 @@@ v5 @@@ v5 @@@ v5 @@@ 22 @@@ Blenheim @@@ blenheim @@@ ### @@@ ### @@@ 23 @@@ Blenheim Speedster @@@ blenheim-speedster @@@ ### @@@ ### @@@ 23 @@@ Fighter @@@ fighter @@@ ### @@@ ### @@@ 23 @@@ Geneva @@@ geneva @@@ ### @@@ ### @@@ 24 @@@ La Joya @@@ la-joya @@@ ### @@@ ### @@@ 24 @@@ EB 110 @@@ eb-110 @@@ ### @@@ ### @@@ 25 @@@ EB 112 @@@ eb-112 @@@ ### @@@ ### @@@ 25 @@@ EB Veyron 16.4 @@@ eb-16-4 @@@ ### @@@ veyron @@@ 25 @@@ Century @@@ century @@@ century @@@ century @@@ 26 @@@ Electra @@@ electra @@@ electra @@@ ### @@@ 26 @@@ Enclave @@@ enclave @@@ ### @@@ enclave @@@ 26 @@@ Encore @@@ encore @@@ ### @@@ encore @@@ 26 @@@ Estate Wagon @@@ estate-wagon @@@ ### @@@ ### @@@ 26 @@@ Excelle @@@ excelle @@@ excelle @@@ ### @@@ 26 @@@ GL8 @@@ gl8 @@@ ### @@@ ### @@@ 26 @@@ LaCrosse @@@ lacrosse @@@ ### @@@ lacrosse @@@ 26 @@@ LeSabre @@@ le-sabre @@@ lesabre @@@ lesabre @@@ 26 @@@ Lucerne @@@ lucerne @@@ ### @@@ lucerne @@@ 26 @@@ Park Avenue @@@ park-avenue @@@ park_avenue @@@ park_avenue @@@ 26 @@@ Rainer @@@ rainer @@@ ### @@@ rainier @@@ 26 @@@ Reatta @@@ reatta @@@ ### @@@ ### @@@ 26 @@@ Regal @@@ regal @@@ regal @@@ regal @@@ 26 @@@ Rendezvous @@@ rendezvous @@@ ### @@@ rendezvous @@@ 26 @@@ Riviera @@@ rivera @@@ riviera @@@ rivera @@@ 26 @@@ Roadmaster @@@ roadmaster @@@ roadmaster @@@ roadmaster @@@ 26 @@@ Skylark @@@ skylark @@@ skylark @@@ skylark @@@ 26 @@@ Terraza @@@ terraza @@@ ### @@@ terraza @@@ 26 @@@ Verano @@@ verano @@@ ### @@@ ### @@@ 26 @@@ E6 @@@ e6 @@@ ### @@@ ### @@@ 27 @@@ F0 @@@ f0 @@@ ### @@@ f0 @@@ 27 @@@ F2 @@@ ### @@@ f2 @@@ ### @@@ 27 @@@ F3 @@@ f3 @@@ f3 @@@ f3 @@@ 27 @@@ F5 @@@ f5 @@@ ### @@@ f5 @@@ 27 @@@ F6 @@@ f6 @@@ ### @@@ f6 @@@ 27 @@@ F8 @@@ f8 @@@ ### @@@ f8 @@@ 27 @@@ Flyer @@@ flyer @@@ flyer @@@ flyer @@@ 27 @@@ G3 @@@ g3 @@@ ### @@@ g3 @@@ 27 @@@ G6 @@@ g6 @@@ ### @@@ g6 @@@ 27 @@@ L3 @@@ l3 @@@ ### @@@ ### @@@ 27 @@@ BD132J (CoCo) @@@ bd132j @@@ ### @@@ ### @@@ 28 @@@ BD326J (Moca) @@@ bd326j @@@ ### @@@ ### @@@ 28 @@@ Allante @@@ allante @@@ ### @@@ ### @@@ 29 @@@ ATS @@@ ats @@@ ats @@@ ats @@@ 29 @@@ ATS-V @@@ ats-v @@@ ### @@@ ### @@@ 29 @@@ BLS @@@ bls @@@ bls @@@ bls @@@ 29 @@@ Brougham @@@ brougham @@@ brougham @@@ brougham @@@ 29 @@@ Catera @@@ catera @@@ ### @@@ catera @@@ 29 @@@ CTS @@@ cts @@@ cts @@@ cts @@@ 29 @@@ CTS-V @@@ cts-v @@@ cts-v @@@ ### @@@ 29 @@@ De Ville @@@ deville @@@ de_ville @@@ deville @@@ 29 @@@ DTS @@@ dts @@@ dts @@@ dts @@@ 29 @@@ Eldorado @@@ eldorado @@@ eldorado @@@ eldorado @@@ 29 @@@ ELR @@@ elr @@@ ### @@@ elr @@@ 29 @@@ Escalade @@@ escalade @@@ escalade @@@ escalade @@@ 29 @@@ Fleetwood @@@ fleetwood2 @@@ fleetwood @@@ fleetwood @@@ 29 @@@ LSE @@@ lse @@@ ### @@@ ### @@@ 29 @@@ Seville @@@ seville @@@ seville @@@"," seville @@@ 29 @@@ Sixty Special @@@ 60special @@@ ### @@@ ### @@@ 29 @@@ SRX @@@ srx @@@ srx @@@ srx @@@ 29 @@@ STS @@@ sts @@@ sts @@@ sts @@@ 29 @@@ XLR @@@ xlr @@@ ### @@@ xlr @@@ 29 @@@ XTS @@@ xts @@@ ### @@@ ### @@@ 29 @@@ C12 @@@ c12 @@@ ### @@@ ### @@@ 30 @@@ FX4 @@@ fx4 @@@ ### @@@ ### @@@ 31 @@@ 21 @@@ 21 @@@ ### @@@ ### @@@ 32 @@@ CSR @@@ csr @@@ ### @@@ ### @@@ 32 @@@ Seven @@@ seven @@@ ### @@@ ### @@@ 32 @@@ Benni @@@ benni @@@ ### @@@ ### @@@ 33 @@@ CM-8 @@@ sm-8 @@@ ### @@@ ### @@@ 33 @@@ CS35 @@@ cs35 @@@ cs35 @@@ cs35 @@@ 33 @@@ Eado @@@ eado @@@ eado @@@ eado @@@ 33 @@@ Raeton @@@ raeton @@@ ### @@@ raeton @@@ 33 @@@ Z-Shine @@@ z-shine @@@ ### @@@ ### @@@ 33 @@@ Flying @@@ flying @@@ ### @@@ ### @@@ 34 @@@ SUV (CS6) @@@ suv @@@ ### @@@ ### @@@ 34 @@@ Amulet (A15) @@@ amulet @@@ amulet @@@ amulet @@@ 35 @@@ Arrizo 7 @@@ arrizo_7 @@@ ### @@@ arrizo_7 @@@ 35 @@@ Bonus (A13) @@@ bonus @@@ bonus @@@ bonus @@@ 35 @@@ Bonus 3 (E3/A19) @@@ bonus3 @@@ bonus_3 @@@ a19 @@@ 35 @@@ CrossEastar (B14) @@@ crosseastar @@@ crosseastar @@@ crosseastar @@@ 35 @@@ Fora (A21) @@@ fora @@@ fora @@@ fora @@@ 35 @@@ IndiS (S18D) @@@ indis @@@ indis @@@ indis @@@ 35 @@@ Kimo (A1) @@@ kimo @@@ kimo @@@ kimo @@@ 35 @@@ M11 (A3) @@@ m11 @@@ m11 @@@ m11 @@@ 35 @@@ Oriental Son (B11) @@@ oriental-son @@@ ### @@@ b11 @@@ 35 @@@ QQ6 (S21) @@@ qq6 @@@ qq6 @@@ ### @@@ 35 @@@ Sweet (QQ) @@@ sweet @@@ qq @@@ qq @@@ 35 @@@ Tiggo (T11) @@@ tiggo @@@ tiggo @@@ tiggo @@@ 35 @@@ Tiggo 5 @@@ tiggo5 @@@ tiggo_5 @@@ tiggo_5 @@@ 35 @@@ Very @@@ very @@@ very @@@ very @@@ 35 @@@ Alero @@@ alero @@@ ### @@@ alero @@@ 36 @@@ Astra @@@ astra @@@ ### @@@ ### @@@ 36 @@@ Astro @@@ astro @@@ astro @@@ astro @@@ 36 @@@ Avalanche @@@ avalanche @@@ avalanche @@@ avalanche @@@ 36 @@@ Aveo @@@ aveo @@@ aveo @@@ aveo @@@ 36 @@@ Bel Air @@@ ### @@@ bel_air @@@ ### @@@ 36 @@@ Beretta @@@ beretta @@@ ### @@@ ### @@@ 36 @@@ Blazer @@@ blazer @@@ blazer @@@ blazer @@@ 36 @@@ Blazer K5 @@@ blazer_k5 @@@ ### @@@ ### @@@ 36 @@@ C-10 @@@ c-10 @@@ c10 @@@ ### @@@ 36 @@@ Camaro @@@ camaro @@@ camaro @@@ camaro @@@ 36 @@@ Caprice @@@ caprice @@@ caprice @@@ caprice @@@ 36 @@@ Captiva @@@ captiva @@@ captiva @@@ captiva @@@ 36 @@@ Cavalier @@@ cavalier @@@ ### @@@ cavalier @@@ 36 @@@ Celebrity @@@ celebrity @@@ ### @@@ celebrity @@@ 36 @@@ Celta @@@ celta @@@ ### @@@ ### @@@ 36 @@@ Chevette @@@ chevette @@@ ### @@@ ### @@@ 36 @@@ Citation @@@ citation @@@ ### @@@ ### @@@ 36 @@@ Classic @@@ classic @@@ ### @@@ ### @@@ 36 @@@ Cobalt @@@ cobalt @@@ cobalt @@@ cobalt @@@ 36 @@@ Colorado @@@ colorado @@@ ### @@@ colorado @@@ 36 @@@ Corsa @@@ corsa @@@ ### @@@ ### @@@ 36 @@@ Corsica @@@ corsica @@@ ### @@@ corsica @@@ 36 @@@ Corvette @@@ corvette @@@ corvette @@@ corvette @@@ 36 @@@ Cruze @@@ cruze @@@ cruze @@@ cruze @@@ 36 @@@ Cruze (HR) @@@ cruze-hr @@@ ### @@@ ### @@@ 36 @@@ Epica @@@ epica @@@ epica @@@ epica @@@ 36 @@@ Equinox @@@ equinox @@@ ### @@@ equinox @@@ 36 @@@ Evanda @@@ evanda @@@ evanda @@@ evanda @@@ 36 @@@ Express @@@ express @@@ express @@@ express @@@ 36 @@@ HHR @@@ hhr @@@ ### @@@ hhr @@@ 36 @@@ Impala @@@ impala @@@ impala @@@ impala @@@ 36 @@@ Kalos @@@ kalos @@@ ### @@@ ### @@@ 36 @@@ Lacetti @@@ lacetti @@@ lacetti @@@ lacetti @@@ 36 @@@ Lanos @@@ lanos @@@ lanos @@@ lanos @@@ 36 @@@ Lumina @@@ lumina @@@ lumina @@@ lumina @@@ 36 @@@ Lumina APV @@@ lumina_apv @@@ ### @@@ ### @@@ 36 @@@ LUV D-MAX @@@ luv-d-max @@@ ### @@@ ### @@@ 36 @@@ Malibu @@@ malibu @@@ malibu @@@ malibu @@@ 36 @@@ Monte Carlo @@@ monte-carlo @@@ ### @@@ monte_carlo @@@ 36 @@@ Monza @@@ monza @@@ ### @@@ ### @@@ 36 @@@ MW @@@ mw @@@ ### @@@ mw @@@ 36 @@@ Niva @@@ niva @@@ niva @@@ niva @@@ 36 @@@ Nubira @@@ nubira @@@ ### @@@ nubira @@@ 36 @@@ Omega @@@ omega @@@ ### @@@ ### @@@ 36 @@@ Orlando @@@ orlando @@@ orlando @@@ orlando @@@ 36 @@@ Prizm @@@ prizm @@@ ### @@@ ### @@@ 36 @@@ Rezzo @@@ rezzo @@@ rezzo @@@ rezzo @@@ 36 @@@ S-10 Pickup @@@ s-10-pickup @@@ ### @@@ s10 @@@ 36 @@@ Sail @@@ sail @@@ ### @@@ ### @@@ 36 @@@ Silverado @@@ silverado @@@ silverado @@@ Silverado @@@ 36 @@@ Sonic @@@ sonic @@@ ### @@@ ### @@@ 36 @@@ Spark @@@ spark @@@ spark @@@ spark @@@ 36 @@@ SS @@@ ss @@@ ### @@@ ### @@@ 36 @@@ SSR @@@ ssr @@@ ### @@@ ssr @@@ 36 @@@ Starcraft @@@ starcraft @@@ ### @@@ starcraft @@@ 36 @@@ Suburban @@@ suburban @@@ suburban @@@ suburban @@@ 36 @@@ Tahoe @@@ tahoe @@@ tahoe @@@ tahoe @@@ 36 @@@ Tavera @@@ tavera @@@ ### @@@ ### @@@ 36 @@@ Tracker @@@ tracker @@@ tracker @@@ tracker @@@ 36 @@@ TrailBlazer @@@ trailblazer @@@ trailblazer @@@ trailblazer @@@ 36 @@@ Trans Sport @@@ trans-sport @@@ ### @@@ ### @@@ 36 @@@ Traverse @@@ traverse @@@ ### @@@ traverse @@@ 36 @@@ Uplander @@@ uplander @@@ ### @@@ uplander @@@ 36 @@@ Van @@@ van @@@ ### @@@ ### @@@ 36 @@@ Vectra @@@ vectra @@@ ### @@@ ### @@@ 36 @@@ Venture @@@ venture @@@ ### @@@ venture @@@ 36 @@@ Viva @@@ viva @@@ ### @@@ viva @@@ 36 @@@ Volt @@@ volt @@@ ### @@@ volt @@@ 36 @@@ Zafira @@@ zafira @@@ ### @@@ ### @@@ 36 @@@ 300C @@@ 300c @@@ 300c @@@ 300c @@@ 37 @@@ 300C SRT8 @@@ 300c-srt8 @@@ ### @@@ ### @@@ 37 @@@ 300M @@@ 300m @@@ 300m @@@ 300m @@@ 37 @@@ Aspen @@@ aspen @@@ ### @@@ ### @@@ 37 @@@ Cirrus @@@ cirrus @@@ cirrus @@@ cirrus @@@ 37 @@@ Concorde @@@ concorde @@@ concorde @@@ concorde @@@ 37 @@@ Crossfire @@@ crossfire @@@ crossfire @@@ crossfire @@@ 37 @@@ Dynasty @@@ dynasty @@@ ### @@@ ### @@@ 37 @@@ Fifth Avenue @@@ fifth-avenue @@@ fifth_avenue @@@ ### @@@ 37 @@@ Grand Voyager @@@ ### @@@ grand_voyager @@@ grand_voyager @@@ 37 @@@ Imperial @@@ imperial @@@ imperial @@@ ### @@@ 37 @@@ Intrepid @@@ intrepid @@@ intrepid @@@ intrepid @@@ 37 @@@ Le Baron @@@ le-baron @@@ lebaron @@@ le_baron @@@ 37 @@@ LHS @@@ lhs @@@ lhs @@@ lhs @@@ 37 @@@ Nassau @@@ nassau @@@ ### @@@ ### @@@ 37 @@@ Neon @@@ neon @@@ neon @@@ neon @@@ 37 @@@ NEW Yorker @@@ new-yorker @@@ new_yorker @@@ new_yorker @@@ 37 @@@ Pacifica @@@ pacifica @@@ pacifica @@@ pacifica @@@ 37 @@@ Prowler @@","@ prowler @@@ prowler @@@ prowler @@@ 37 @@@ PT Cruiser @@@ pt-cruiser @@@ pt_cruiser @@@ pt_cruiser @@@ 37 @@@ Saratoga @@@ saratoga @@@ saratoga @@@ saratoga @@@ 37 @@@ Sebring @@@ sebring @@@ sebring @@@ sebring @@@ 37 @@@ Stratus @@@ stratus @@@ stratus @@@ stratus @@@ 37 @@@ TC by Maserati @@@ tcbymaserati @@@ ### @@@ ### @@@ 37 @@@ Town & Country @@@ town-country @@@ town__country @@@ town_country @@@ 37 @@@ Viper @@@ viper @@@ ### @@@ ### @@@ 37 @@@ Vision @@@ vision @@@ vision @@@ vision @@@ 37 @@@ Voyager @@@ voyager @@@ voyager @@@ voyager @@@ 37 @@@ 2 CV @@@ 2cv @@@ ### @@@ 2cv @@@ 38 @@@ AMI @@@ ami @@@ ### @@@ ### @@@ 38 @@@ Ax @@@ ax @@@ ### @@@ ax @@@ 38 @@@ Berlingo @@@ berlingo @@@ berlingo @@@ berlingo @@@ 38 @@@ BX @@@ bx @@@ ### @@@ bx @@@ 38 @@@ C3 @@@ c3 @@@ c3 @@@ c3 @@@ 38 @@@ C3 Picasso @@@ c3-picasso @@@ c3_picasso @@@ c3_picasso @@@ 38 @@@ C4 @@@ c4 @@@ c4 @@@ c4 @@@ 38 @@@ C4 Aircross @@@ c4-aircross @@@ c4_aircross @@@ c4_aircross @@@ 38 @@@ C4 Cactus @@@ c4-cactus @@@ ### @@@ c4_cactus @@@ 38 @@@ C4 Picasso @@@ c4-picasso @@@ c4_picasso @@@ c4_picasso @@@ 38 @@@ C5 @@@ c5 @@@ c5 @@@ c5 @@@ 38 @@@ C6 @@@ c6 @@@ ### @@@ c6 @@@ 38 @@@ C8 @@@ c8 @@@ c8 @@@ c8 @@@ 38 @@@ C-Crosser @@@ c-crosser @@@ c-crosser @@@ c-crosser @@@ 38 @@@ C-Elysee @@@ c-elysee @@@ c-elysee @@@ c-elysee @@@ 38 @@@ CX @@@ cx @@@ cx @@@ ### @@@ 38 @@@ C-ZERO @@@ c-zero @@@ c-zero @@@ ### @@@ 38 @@@ DS3 @@@ ds3 @@@ ds3 @@@ ds3 @@@ 38 @@@ DS4 @@@ ds4 @@@ ds4 @@@ ds4 @@@ 38 @@@ DS5 @@@ ds5 @@@ ds5 @@@ ds5 @@@ 38 @@@ Dyane @@@ dyane @@@ ### @@@ ### @@@ 38 @@@ Evasion @@@ evasion @@@ evasion @@@ evasion @@@ 38 @@@ Grand C4 Picasso @@@ ### @@@ grand_c4_picasso @@@ grand_c4_picasso @@@ 38 @@@ GS @@@ gs @@@ ### @@@ ### @@@ 38 @@@ Jumper @@@ ### @@@ jumper @@@ ### @@@ 38 @@@ Jumpy @@@ jumpy @@@ jumpy @@@ jumpy @@@ 38 @@@ LNA @@@ lna @@@ ### @@@ ### @@@ 38 @@@ Nemo @@@ ### @@@ nemo @@@ nemo @@@ 38 @@@ Saxo @@@ saxo @@@ saxo @@@ saxo @@@ 38 @@@ Visa @@@ visa @@@ ### @@@ visa @@@ 38 @@@ Xantia @@@ xantia @@@ xantia @@@ xantia @@@ 38 @@@ XM @@@ xm @@@ xm @@@ xm @@@ 38 @@@ Xsara @@@ xsara @@@ xsara @@@ xsara @@@ 38 @@@ Xsara Picasso @@@ xsara_picasso @@@ xsara_picasso @@@ xsara_picasso @@@ 38 @@@ ZX @@@ zx @@@ ### @@@ zx @@@ 38 @@@ V16t @@@ v16t @@@ ### @@@ ### @@@ 39 @@@ T Rex @@@ t-rex @@@ ### @@@ ### @@@ 40 @@@ 1300 @@@ 1300 @@@ ### @@@ ### @@@ 41 @@@ 1310 @@@ 1310 @@@ ### @@@ ### @@@ 41 @@@ 1410 @@@ 1410 @@@ ### @@@ ### @@@ 41 @@@ Dokker @@@ dokker @@@ ### @@@ dokker @@@ 41 @@@ Duster @@@ duster @@@ duster @@@ duster @@@ 41 @@@ Lodgy @@@ lodgy @@@ ### @@@ lodgy @@@ 41 @@@ Logan @@@ logan @@@ logan @@@ logan @@@ 41 @@@ Nova @@@ nova @@@ ### @@@ supernova @@@ 41 @@@ Sandero @@@ sandero @@@ sandero @@@ sandero @@@ 41 @@@ Solenza @@@ solenza @@@ ### @@@ solenza @@@ 41 @@@ City Leading @@@ city-leading @@@ ### @@@ ### @@@ 42 @@@ Shuttle @@@ shuttle @@@ ### @@@ ### @@@ 42 @@@ Smoothing @@@ smoothing @@@ ### @@@ ### @@@ 42 @@@ Arcadia @@@ arcadia @@@ ### @@@ ### @@@ 43 @@@ Chairman @@@ chairman @@@ ### @@@ ### @@@ 43 @@@ Damas @@@ damas @@@ damas @@@ damas @@@ 43 @@@ Espero @@@ espero @@@ espero @@@ espero @@@ 43 @@@ G2X @@@ g2x @@@ ### @@@ g2x @@@ 43 @@@ Gentra @@@ gentra @@@ gentra @@@ gentra @@@ 43 @@@ Korando @@@ korando @@@ ### @@@ korando @@@ 43 @@@ Labo @@@ ### @@@ labo @@@ ### @@@ 43 @@@ Lanos (Sens) @@@ lanos @@@ ### @@@ lanos @@@ 43 @@@ LE Mans @@@ le-mans @@@ ### @@@ lemans @@@ 43 @@@ Leganza @@@ leganza @@@ leganza @@@ leganza @@@ 43 @@@ Magnus @@@ magnus @@@ magnus @@@ magnus @@@ 43 @@@ Matiz @@@ matiz @@@ matiz @@@ matiz @@@ 43 @@@ Musso @@@ musso @@@ ### @@@ ### @@@ 43 @@@ Nexia @@@ nexia @@@ nexia @@@ nexia @@@ 43 @@@ Prince @@@ prince @@@ ### @@@ prince @@@ 43 @@@ Racer @@@ racer @@@ ### @@@ racer @@@ 43 @@@ Sens @@@ ### @@@ sens @@@ sens @@@ 43 @@@ Tacuma @@@ tacuma @@@ tacuma @@@ tacuma @@@ 43 @@@ Tico @@@ tico @@@ tico @@@ tico @@@ 43 @@@ Tosca @@@ tosca @@@ ### @@@ tosca @@@ 43 @@@ Winstorm @@@  @@@ ### @@@ winstorm @@@ 43 @@@ 46 @@@ 46-2 @@@ ### @@@ ### @@@ 44 @@@ 66 @@@ 66 @@@ ### @@@ ### @@@ 44 @@@ Altis @@@ altis @@@ ### @@@ altis @@@ 45 @@@ Applause @@@ applause @@@ ### @@@ applause @@@ 45 @@@ Atrai @@@ atrai @@@ atrai @@@ atrai @@@ 45 @@@ Be-go @@@ be-go @@@ ### @@@ be-go @@@ 45 @@@ Boon @@@ boon @@@ boon @@@ boon @@@ 45 @@@ Ceria @@@ ceria @@@ ### @@@ ### @@@ 45 @@@ Charade @@@ charade @@@ charade @@@ charade @@@ 45 @@@ Charmant @@@ charmant @@@ ### @@@ ### @@@ 45 @@@ Coo @@@ coo @@@ ### @@@ coo @@@ 45 @@@ Copen @@@ copen @@@ copen @@@ copen @@@ 45 @@@ Cuore @@@ cuore @@@ cuore @@@ cuore @@@ 45 @@@ Delta Wagon @@@ delta-wagon @@@ ### @@@ delta @@@ 45 @@@ Esse @@@ esse @@@ esse @@@ esse @@@ 45 @@@ Feroza @@@ feroza @@@ ### @@@ feroza @@@ 45 @@@ Gran Move @@@ gran-move @@@ ### @@@ grand_move @@@ 45 @@@ Hijet @@@ ### @@@ hijet @@@ hijet @@@ 45 @@@ Leeza @@@ leeza @@@ ### @@@ leeza @@@ 45 @@@ Materia @@@ materia @@@ materia @@@ materia @@@ 45 @@@ MAX @@@ max @@@ ### @@@ max @@@ 45 @@@ Mira @@@ mira @@@ mira @@@ mira @@@ 45 @@@ Mira Gino @@@ mira-gino @@@ ### @@@ mira_gino @@@ 45 @@@ Move @@@ move @@@ move @@@ move @@@ 45 @@@ Naked @@@ naked @@@ ### @@@ naked @@@ 45 @@@ Opti @@@ opti @@@ ### @@@ opti @@@ 45 @@@ Perodua Viva @@@ ### @@@ perodua_viva @@@ ### @@@ 45 @@@ Pyzar @@@ pyzar @@@ pyzar @@@ pyzar @@@ 45 @@@ Rocky @@@ rocky @@@ rocky @@@ rocky @@@ 45 @@@ Sirion @@@ sirion @@@ sirion @@@ sirion @@@ 45 @@@ Sonica @@@ sonica @@@ ### @@@ sonica @@@ 45 @@@ Storia @@@ storia @@@ storia @@@ storia @@@ 45 @@@ Taft @@@ taft @@@ ### @@@ ### @@@ 45 @@@ Tanto @@@ tanto @@@ ### @@@ tanto @@@ 45 @@@ Terios @@@ terios @@@ terios @@@ terios @@@ 45 @@@ Trevis @@@ trevis @@@ ### @@@ trevis @@@ 45 @@@ Wildcat @@@ wildcat @@@ ### @@@ ### @@@ 45 @@@ Xenia @@@ xenia @@@ ### @@@ xenia @@@ 45 @@@ YRV @@@ yrv @@@ yrv @@@ yrv @@@ 45 @@@ DS420 @@@ ds420 @@@ ### @@@ ### @@@ 46 @@@ Sovereign (XJ6) @@@ xj6 @@@ ### @@@ ### @@@ 46 @@@ X300 @@@ x300 @@@ ### @@@ ### @@@ 46 @@@ X308 @@@ x308 @@@ ### @@@ ### @","@@ 46 @@@ X350 @@@ x350 @@@ ### @@@ ### @@@ 46 @@@ XJ40 @@@ xj40 @@@ ### @@@ ### @@@ 46 @@@ XJS @@@ xjs @@@ ### @@@ ### @@@ 46 @@@ 720 @@@ 720 @@@ ### @@@ ### @@@ 47 @@@ 280ZX @@@ 280zx @@@ ### @@@ ### @@@ 47 @@@ Bluebird @@@ bluebird @@@ ### @@@ ### @@@ 47 @@@ Cherry @@@ cherry @@@ ### @@@ ### @@@ 47 @@@ GO @@@ go @@@ ### @@@ go @@@ 47 @@@ GO+ @@@ go-plus @@@ ### @@@ ### @@@ 47 @@@ mi-DO @@@ mi-do @@@ mi-do @@@ mi-do @@@ 47 @@@ on-DO @@@ on-do @@@ on-do @@@ on-do @@@ 47 @@@ Stanza @@@ stanza @@@ ### @@@ ### @@@ 47 @@@ Sunny @@@ sunny @@@ ### @@@ ### @@@ 47 @@@ Urvan @@@ urvan @@@ ### @@@ ### @@@ 47 @@@ Violet @@@ violet @@@ ### @@@ ### @@@ 47 @@@ Bigua @@@ bigua @@@ ### @@@ ### @@@ 48 @@@ Guara @@@ guara @@@ ### @@@ ### @@@ 48 @@@ Mangusta @@@ mangusta @@@ ### @@@ ### @@@ 48 @@@ Pantera @@@ pantera @@@ ### @@@ ### @@@ 48 @@@ Vallelunga @@@ vallelunga @@@ ### @@@ ### @@@ 48 @@@ DMC-12 @@@ dmc-12 @@@ ### @@@ ### @@@ 49 @@@ Antelope @@@ antelope @@@ ### @@@ ### @@@ 50 @@@ Aurora @@@ aurora @@@ ### @@@ aurora @@@ 50 @@@ Cowboy @@@ cowboy @@@ ### @@@ ### @@@ 50 @@@ Land Crown @@@ land-crown @@@ ### @@@ ### @@@ 50 @@@ Plutus @@@ plutus @@@ ### @@@ plutus @@@ 50 @@@ Saladin @@@ saladin @@@ ### @@@ ### @@@ 50 @@@ 600 @@@ 600 @@@ ### @@@ ### @@@ 51 @@@ Aries @@@ aries @@@ ### @@@ ### @@@ 51 @@@ Avenger @@@ avenger @@@ avenger @@@ avenger @@@ 51 @@@ Caliber @@@ caliber @@@ caliber @@@ caliber @@@ 51 @@@ Caravan @@@ caravan @@@ caravan @@@ caravan @@@ 51 @@@ Challenger @@@ challenger @@@ challenger @@@ challenger @@@ 51 @@@ Charger @@@ charger @@@ charger @@@ charger @@@ 51 @@@ Dakota @@@ dakota @@@ dakota @@@ dakota @@@ 51 @@@ Dart @@@ dart @@@ ### @@@ dart @@@ 51 @@@ Daytona @@@ daytona @@@ ### @@@ ### @@@ 51 @@@ Durango @@@ durango @@@ durango @@@ durango @@@ 51 @@@ Grand Caravan @@@ ### @@@ grand_caravan @@@ grand_caravan @@@ 51 @@@ Journey @@@ journey @@@ journey @@@ journey @@@ 51 @@@ Lancer @@@ lancer @@@ ### @@@ ### @@@ 51 @@@ Magnum @@@ magnum @@@ magnum @@@ magnum @@@ 51 @@@ Monaco @@@ monaco @@@ ### @@@ ### @@@ 51 @@@ Nitro @@@ nitro @@@ nitro @@@ nitro @@@ 51 @@@ Omni @@@ omni @@@ ### @@@ ### @@@ 51 @@@ RAM @@@ ram @@@ ram @@@ ram @@@ 51 @@@ Ramcharger @@@ ramcharger @@@ ### @@@ ### @@@ 51 @@@ Shadow @@@ shadow @@@ shadow @@@ ### @@@ 51 @@@ Spirit @@@ spirit @@@ ### @@@ spirit @@@ 51 @@@ Stealth @@@ stealth @@@ stealth @@@ stealth @@@ 51 @@@ H30 Cross @@@ h30-cross @@@ h30_cross @@@ h30_cross @@@ 52 @@@ MPV @@@ mpv @@@ ### @@@ ### @@@ 52 @@@ Oting @@@ oting @@@ ### @@@ ### @@@ 52 @@@ Rich @@@ rich @@@ ### @@@ rich @@@ 52 @@@ S30 @@@ s30 @@@ s30 @@@ s30 @@@ 52 @@@ Assol @@@ assol @@@ ### @@@ ### @@@ 53 @@@ Kondor @@@ kondor @@@ ### @@@ ### @@@ 53 @@@ Orion @@@ orion @@@ ### @@@ ### @@@ 53 @@@ D8 @@@ d8 @@@ ### @@@ ### @@@ 54 @@@ 3 @@@ 3 @@@ ### @@@ ### @@@ 55 @@@ 4 @@@ 4 @@@ ### @@@ ### @@@ 55 @@@ 5 @@@ 5 @@@ ### @@@ ### @@@ 55 @@@ GD04B @@@ gd04b @@@ ### @@@ ### @@@ 56 @@@ Premier @@@ premier @@@ ### @@@ ### @@@ 57 @@@ Summit @@@ summit @@@ ### @@@ ### @@@ 57 @@@ Talon @@@ talon @@@ ### @@@ talon @@@ 57 @@@ Estrima Biro @@@ estrima-biro @@@ ### @@@ ### @@@ 59 @@@ Besturn @@@ ### @@@ besturn @@@ ### @@@ 60 @@@ Besturn B50 @@@ besturn-b50 @@@ besturn_b50 @@@ besturn_b50 @@@ 60 @@@ Besturn B70 @@@ besturn-b70 @@@ ### @@@ besturn_b70 @@@ 60 @@@ Besturn X80 @@@ besturn-x80 @@@ ### @@@ besturn_x80 @@@ 60 @@@ City Golf @@@ city-golf @@@ ### @@@ ### @@@ 60 @@@ Jinn @@@ jinn @@@ jinn @@@ jinn @@@ 60 @@@ Oley @@@ oley @@@ ### @@@ oley @@@ 60 @@@ V2 @@@ v2 @@@ v2 @@@ v2 @@@ 60 @@@ Vita @@@ vita @@@ vita @@@ vita @@@ 60 @@@ 208 @@@ ### @@@ 208 @@@ ### @@@ 61 @@@ 328 @@@ 328 @@@ ### @@@ 328 @@@ 61 @@@ 348 @@@ 348 @@@ 348 @@@ 348 @@@ 61 @@@ 360 @@@ 360 @@@ ### @@@ 360 @@@ 61 @@@ 400 @@@ 400 @@@ ### @@@ ### @@@ 61 @@@ 412 @@@ 412 @@@ ### @@@ ### @@@ 61 @@@ 456 @@@ 456 @@@ ### @@@ 456 @@@ 61 @@@ 458 @@@ 458 @@@ ### @@@ ### @@@ 61 @@@ 550 @@@ 550 @@@ ### @@@ 550_barchetta @@@ 61 @@@ 599 @@@ 599 @@@ ### @@@ 599_gto @@@ 61 @@@ 612 @@@ 612 @@@ ### @@@ 612_scaglietti @@@ 61 @@@ 208/308 @@@ 208-308 @@@ ### @@@ ### @@@ 61 @@@ 458 Italia @@@ ### @@@ 458_italia @@@ 458_italia @@@ 61 @@@ 458 Spider @@@ ### @@@ 458_spider @@@ 458_spider @@@ 61 @@@ 488 GTB @@@ 488-gtb @@@ ### @@@ 488_gtb @@@ 61 @@@ 512 BB @@@ 512-bb @@@ ### @@@ ### @@@ 61 @@@ 512 M @@@ 512-m @@@ ### @@@ ### @@@ 61 @@@ 512 TR @@@ 512tr @@@ ### @@@ ### @@@ 61 @@@ 575M @@@ 575m @@@ ### @@@ 575m_maranello @@@ 61 @@@ 599 GTB Fiorano @@@ ### @@@ 599_gtb_fiorano @@@ ### @@@ 61 @@@ California @@@ california @@@ california @@@ california @@@ 61 @@@ Enzo @@@ enzo @@@ ### @@@ enzo_ferrari @@@ 61 @@@ F12berlinetta @@@ f12berlinetta @@@ f12_berlinetta @@@ F12_berlinetta @@@ 61 @@@ F355 @@@ f355 @@@ ### @@@ f355 @@@ 61 @@@ F40 @@@ f40 @@@ ### @@@ ### @@@ 61 @@@ F430 @@@ f430 @@@ f430 @@@ f430 @@@ 61 @@@ F50 @@@ f50 @@@ ### @@@ f50 @@@ 61 @@@ FF @@@ ff @@@ ff @@@ ff @@@ 61 @@@ LaFerrari @@@ laferrari @@@ ### @@@ laferrari @@@ 61 @@@ Mondial @@@ mondial @@@ ### @@@ ### @@@ 61 @@@ Testarossa @@@ testarossa @@@ testarossa @@@ testarossa @@@ 61 @@@ 124 @@@ 124 @@@ ### @@@ ### @@@ 62 @@@ 126 @@@ 126 @@@ ### @@@ ### @@@ 62 @@@ 127 @@@ 127 @@@ ### @@@ 127 @@@ 62 @@@ 128 @@@ 128 @@@ ### @@@ ### @@@ 62 @@@ 130 @@@ 130 @@@ ### @@@ ### @@@ 62 @@@ 131 @@@ 131 @@@ ### @@@ ### @@@ 62 @@@ 132 @@@ 132 @@@ ### @@@ ### @@@ 62 @@@ 238 @@@ 238 @@@ ### @@@ ### @@@ 62 @@@ 500 @@@ 500 @@@ 500 @@@ 500 @@@ 62 @@@ 500L @@@ 500l @@@ ### @@@ 500l @@@ 62 @@@ 500X @@@ 500x @@@ ### @@@ ### @@@ 62 @@@ 900T @@@ 900t @@@ ### @@@ ### @@@ 62 @@@ Albea @@@ albea @@@ albea @@@ albea @@@ 62 @@@ Argenta @@@ argenta @@@ ### @@@ ### @@@ 62 @@@ Barchetta @@@ barchetta @@@ barchetta @@@ barchetta @@@ 62 @@@ Brava @@@ brava @@@ brava @@@ brava @@@ 62 @@@ Bravo @@@ bravo @@@ bravo @@@ bravo @@@ 62 @@@ Cinquecento @@@ cinquecento @@@ cinquecento @@@ cinquecento @@@ 62 @@@ Croma @@@ croma @@@ croma @@@ croma @@@ 62 @@@ Doblo @@@ doblo @@@ doblo @@@ doblo @@@ 62 @@@ Ducato @@@ ### @@@ ducato @@@ ### @@@ 62"," @@@ Duna @@@ duna @@@ ### @@@ ### @@@ 62 @@@ Fiorino @@@ fiorino @@@ fiorino @@@ fiorino @@@ 62 @@@ Freemont @@@ freemont @@@ freemont @@@ freemont @@@ 62 @@@ Idea @@@ idea @@@ ### @@@ idea @@@ 62 @@@ Linea @@@ linea @@@ linea @@@ linea @@@ 62 @@@ Marea @@@ marea @@@ marea @@@ marea @@@ 62 @@@ Multipla @@@ multipla @@@ multipla @@@ multipla @@@ 62 @@@ Palio @@@ palio @@@ palio @@@ palio @@@ 62 @@@ Panda @@@ panda @@@ panda @@@ panda @@@ 62 @@@ Punto @@@ punto @@@ punto @@@ punto @@@ 62 @@@ Qubo @@@ qubo @@@ ### @@@ qubo @@@ 62 @@@ Regata @@@ regata @@@ ### @@@ regata @@@ 62 @@@ Ritmo @@@ ritmo @@@ ### @@@ ritmo @@@ 62 @@@ Scudo @@@ scudo @@@ scudo @@@ scudo @@@ 62 @@@ Sedici @@@ sedici @@@ sedici @@@ sedici @@@ 62 @@@ Seicento @@@ seicento @@@ ### @@@ seicento @@@ 62 @@@ Siena @@@ siena @@@ ### @@@ siena @@@ 62 @@@ Stilo @@@ stilo @@@ stilo @@@ stilo @@@ 62 @@@ Strada @@@ strada @@@ ### @@@ strada @@@ 62 @@@ Tempra @@@ tempra @@@ tempra @@@ tempra @@@ 62 @@@ Tipo @@@ tipo @@@ tipo @@@ tipo @@@ 62 @@@ Ulysse @@@ ulysse @@@ ulysse @@@ ulysse @@@ 62 @@@ UNO @@@ uno @@@ uno @@@ uno @@@ 62 @@@ X 1/9 @@@ x-1-9 @@@ ### @@@ ### @@@ 62 @@@ Karma @@@ karma @@@ ### @@@ karma @@@ 63 @@@ Aerostar @@@ aerostar @@@ ### @@@ aerostar @@@ 64 @@@ Aspire @@@ aspire @@@ ### @@@ aspire @@@ 64 @@@ B-MAX @@@ b-max @@@ ### @@@ b-max @@@ 64 @@@ Bronco @@@ bronco @@@ ### @@@ bronco @@@ 64 @@@ Bronco II @@@ bronco_ii @@@ ### @@@ ### @@@ 64 @@@ Capri @@@ capri @@@ ### @@@ capri @@@ 64 @@@ C-MAX @@@ c-max @@@ c-max @@@ c-max @@@ 64 @@@ Consul @@@ consul @@@ ### @@@ ### @@@ 64 @@@ Contour @@@ contour @@@ ### @@@ contour @@@ 64 @@@ Cougar @@@ cougar @@@ ### @@@ cougar @@@ 64 @@@ Crown Victoria @@@ crown-victoria @@@ ### @@@ crown_victoria @@@ 64 @@@ Econoline @@@ econoline @@@ econoline @@@ econoline @@@ 64 @@@ EcoSport @@@ ecosport @@@ ecosport @@@ ecosport @@@ 64 @@@ Edge @@@ edge @@@ edge @@@ edge @@@ 64 @@@ Escape @@@ escape @@@ escape @@@ escape @@@ 64 @@@ Escort @@@ escort @@@ escort @@@ escort @@@ 64 @@@ Escort (North America) @@@ escort_na @@@ ### @@@ ### @@@ 64 @@@ Everest @@@ everest @@@ ### @@@ everest @@@ 64 @@@ Excursion @@@ excursion @@@ excursion @@@ excursion @@@ 64 @@@ Expedition @@@ expedition @@@ expedition @@@ expedition @@@ 64 @@@ Explorer @@@ explorer @@@ explorer @@@ explorer @@@ 64 @@@ Explorer Sport Trac @@@ sport-trac @@@ ### @@@ ### @@@ 64 @@@ F-150 @@@ f-150 @@@ f150 @@@ f150 @@@ 64 @@@ Fairmont @@@ fairmont @@@ ### @@@ ### @@@ 64 @@@ Festiva @@@ festiva @@@ ### @@@ festiva @@@ 64 @@@ Fiesta @@@ fiesta @@@ fiesta @@@ fiesta @@@ 64 @@@ Fiesta ST @@@ fiesta-st @@@ ### @@@ ### @@@ 64 @@@ Five Hundred @@@ five-hundred @@@ ### @@@ five_hundred @@@ 64 @@@ Flex @@@ flex @@@ ### @@@ flex @@@ 64 @@@ Focus @@@ focus @@@ focus @@@ focus @@@ 64 @@@ Focus (North America) @@@ focus-na @@@ ### @@@ ### @@@ 64 @@@ Focus RS @@@ focus-rs @@@ focus_rs @@@ focus_rs @@@ 64 @@@ Focus ST @@@ focus-st @@@ focus_st @@@ focus_st @@@ 64 @@@ Freestar @@@ freestar @@@ ### @@@ freestar @@@ 64 @@@ Freestyle @@@ freestyle @@@ ### @@@ freestyle @@@ 64 @@@ Fusion @@@ fusion @@@ fusion @@@ fusion @@@ 64 @@@ Fusion (North America) @@@ fusion-na @@@ ### @@@ ### @@@ 64 @@@ Galaxy @@@ galaxy @@@ galaxy @@@ galaxy @@@ 64 @@@ Granada @@@ granada @@@ ### @@@ granada @@@ 64 @@@ Granada (North America) @@@ granada-na @@@ ### @@@ ### @@@ 64 @@@ Ixion @@@ ixion @@@ ### @@@ ixion @@@ 64 @@@ KA @@@ ka @@@ ka @@@ ka @@@ 64 @@@ Kuga @@@ kuga @@@ kuga @@@ kuga @@@ 64 @@@ Laser @@@ laser @@@ ### @@@ laser @@@ 64 @@@ LTD Crown Victoria @@@ ltd-crown-victoria @@@ ### @@@ ### @@@ 64 @@@ Maverick @@@ maverick @@@ maverick @@@ maverick @@@ 64 @@@ Mondeo @@@ mondeo @@@ mondeo @@@ mondeo @@@ 64 @@@ Mondeo ST @@@ mondeo-st @@@ ### @@@ ### @@@ 64 @@@ Mustang @@@ mustang @@@ mustang @@@ mustang @@@ 64 @@@ Probe @@@ probe @@@ ### @@@ probe @@@ 64 @@@ Puma @@@ puma @@@ ### @@@ puma @@@ 64 @@@ Ranger @@@ ranger @@@ ranger @@@ ranger @@@ 64 @@@ Ranger (North America) @@@ ranger-na @@@ ### @@@ ### @@@ 64 @@@ Scorpio @@@ scorpio @@@ scorpio @@@ scorpio @@@ 64 @@@ Sierra @@@ sierra @@@ sierra @@@ sierra @@@ 64 @@@ S-MAX @@@ s-max @@@ s-max @@@ s-max @@@ 64 @@@ Spectron @@@ spectron @@@ ### @@@ spectron @@@ 64 @@@ Taunus @@@ taunus @@@ ### @@@ taunus @@@ 64 @@@ Taurus @@@ taurus @@@ taurus @@@ taurus @@@ 64 @@@ Taurus X @@@ taurus-x @@@ ### @@@ ### @@@ 64 @@@ Telstar @@@ telstar @@@ ### @@@ telstar @@@ 64 @@@ Tempo @@@ tempo @@@ ### @@@ tempo @@@ 64 @@@ Thunderbird @@@ thunderbird @@@ ### @@@ thunderbird @@@ 64 @@@ Tourneo @@@ ### @@@ tourneo @@@ ### @@@ 64 @@@ Tourneo Connect @@@ tourneo-connect @@@ tourneo_connect @@@ tourneo_connect @@@ 64 @@@ Tourneo Courier @@@ tourneo-courier @@@ ### @@@ ### @@@ 64 @@@ Tourneo Custom @@@ tourneo-custom @@@ ### @@@ tourneo_custom @@@ 64 @@@ Transit @@@ ### @@@ transit @@@ ### @@@ 64 @@@ Windstar @@@ windstar @@@ ### @@@ windstar @@@ 64 @@@ Midi @@@ midi @@@ ### @@@ ### @@@ 65 @@@ Tunland @@@ tunland @@@ ### @@@ ### @@@ 65 @@@ 125p @@@ 125p @@@ ### @@@ ### @@@ 66 @@@ 126p @@@ 126p @@@ ### @@@ ### @@@ 66 @@@ 127p @@@ 127p @@@ ### @@@ ### @@@ 66 @@@ 132p @@@ 132p @@@ ### @@@ ### @@@ 66 @@@ Polonez @@@ polonez @@@ ### @@@ ### @@@ 66 @@@ 6500 (Land King) @@@ 6500 @@@ ### @@@ ### @@@ 67 @@@ Beauty Leopard @@@ beauty-leopard @@@ ### @@@ ### @@@ 68 @@@ CK (Otaka) @@@ otaka @@@ ### @@@ ck @@@ 68 @@@ Emgrand EC7 @@@ emgrand @@@ emgrand @@@ emgrand @@@ 68 @@@ Emgrand EC8 @@@ emgrand-8 @@@ ### @@@ emgrand_ec8 @@@ 68 @@@ Emgrand X7 @@@ emgrand-x7 @@@ emgrand_x7 @@@ ex7 @@@ 68 @@@ FC (Vision) @@@ fc @@@ vision @@@ fc @@@ 68 @@@ GC6 @@@ gc6 @@@ gc6 @@@ gc6 @@@ 68 @@@ GC9 @@@ gc9 @@@ ### @@@ ### @@@ 68 @@@ Haoqing @@@ haoqing @@@ ### @@@ ### @@@ 68 @@@ LC (Panda) @@@ lc @@@ ### @@@ ### @@@ 68 @@@ LC (Panda) Cross @@@ lc-cross @@@ ### @@@ ### @@@ 68 @@@ MK @@@ mk @@@ mk @@@ mk @@@ 68 @@@ MK Cross @@@ mk-cross @@@ mk_cross @@@ mk_cross @@@ 68 @@@ MR @@@ mr @@@ ### @@@ ### @@@ 68 @@@ SC7 @@@ sc7 @@@ ### @@@ ### @@@ 68 @@@ Spectrum @@@ sp","ectrum @@@ ### @@@ ### @@@ 69 @@@ Storm @@@ storm @@@ ### @@@ shtorm @@@ 69 @@@ Acadia @@@ acadia @@@ acadia @@@ acadia @@@ 70 @@@ Canyon @@@ canynon @@@ ### @@@ canyon @@@ 70 @@@ Envoy @@@ envoy @@@ envoy @@@ envoy @@@ 70 @@@ Jimmy @@@ jimmy @@@ jimmy @@@ jimmy @@@ 70 @@@ Safari @@@ safari @@@ safari @@@ safari @@@ 70 @@@ Savana @@@ savana @@@ savana @@@ savana @@@ 70 @@@ Sonoma @@@ sonoma @@@ ### @@@ sonoma @@@ 70 @@@ Syclone @@@ syclone @@@ ### @@@ ### @@@ 70 @@@ Terrain @@@ terrain @@@ ### @@@ terrain @@@ 70 @@@ Typhoon @@@ typhoon @@@ ### @@@ typhoon @@@ 70 @@@ Vandura @@@ vandura @@@ vandura @@@ vandura @@@ 70 @@@ Yukon @@@ yukon @@@ yukon @@@ yukon @@@ 70 @@@ Troy @@@ troy @@@ ### @@@ ### @@@ 71 @@@ Coolbear @@@ cool-bear @@@ coolbear @@@ coolbear @@@ 73 @@@ Cowry (V80) @@@ cowry @@@ ### @@@ ### @@@ 73 @@@ Deer @@@ deer @@@ deer @@@ deer @@@ 73 @@@ Florid @@@ florid @@@ ### @@@ florid @@@ 73 @@@ Hover @@@ hover @@@ hover @@@ hover @@@ 73 @@@ Hover H3 @@@ hover-h3 @@@ ### @@@ hover_h3 @@@ 73 @@@ Hover H5 @@@ hover-h5 @@@ ### @@@ hover_h5 @@@ 73 @@@ Hover H6 @@@ hover-h6 @@@ ### @@@ hover_h6 @@@ 73 @@@ Hover M1 (Peri 4x4) @@@ hover-m1 @@@ ### @@@ ### @@@ 73 @@@ Hover M2 @@@ hover-m2 @@@ ### @@@ hover_m2 @@@ 73 @@@ Hover M4 @@@ hover_m4 @@@ ### @@@ hover_m4 @@@ 73 @@@ Pegasus @@@ pegasus @@@ pegasus @@@ pegasus @@@ 73 @@@ Peri @@@ peri @@@ ### @@@ peri @@@ 73 @@@ Safe @@@ safe @@@ safe @@@ safe @@@ 73 @@@ Sailor @@@ sailor @@@ sailor @@@ sailor @@@ 73 @@@ Sing RUV @@@ ruv @@@ ### @@@ sing @@@ 73 @@@ Socool @@@ socool @@@ ### @@@ ### @@@ 73 @@@ Sokol @@@ ### @@@ sokol @@@ ### @@@ 73 @@@ Voleex C10 (Phenom) @@@ voleex-c10 @@@ ### @@@ ### @@@ 73 @@@ Voleex C30 @@@ voleex_c30 @@@ ### @@@ voleex_c30 @@@ 73 @@@ Wingle @@@ wingle @@@ wingle @@@ wingle @@@ 73 @@@ Brio @@@ brio @@@ brio @@@ brio @@@ 74 @@@ Princip @@@ princip @@@ princip @@@ princip @@@ 74 @@@ Saibao @@@ saibao @@@ ### @@@ ### @@@ 74 @@@ Sigma @@@ sigma @@@ ### @@@ sigma @@@ 74 @@@ Simbo @@@ simbo @@@ simbo @@@ simbo @@@ 74 @@@ 7 @@@ 7 @@@ 7 @@@ 7 @@@ 75 @@@ H2 @@@ h2 @@@ h2 @@@ h2 @@@ 76 @@@ H6 @@@ h6 @@@ h6 @@@ h6 @@@ 76 @@@ H8 @@@ h8 @@@ ### @@@ h8 @@@ 76 @@@ H9 @@@ h9 @@@ h9 @@@ h9 @@@ 76 @@@ Boliger @@@ boliger @@@ ### @@@ ### @@@ 77 @@@ Contessa @@@ contessa @@@ ### @@@ ### @@@ 78 @@@ Apollo @@@ apollo @@@ ### @@@ ### @@@ 79 @@@ Barina @@@ barina @@@ ### @@@ ### @@@ 79 @@@ Calais @@@ calais @@@ ### @@@ ### @@@ 79 @@@ Commodore @@@ commodore @@@ ### @@@ ### @@@ 79 @@@ Frontera @@@ frontera @@@ ### @@@ ### @@@ 79 @@@ Jackaroo @@@ jackaroo @@@ ### @@@ ### @@@ 79 @@@ Monaro @@@ monaro @@@ ### @@@ ### @@@ 79 @@@ Rodeo @@@ rodeo @@@ ### @@@ ### @@@ 79 @@@ Statesman @@@ statesmann @@@ ### @@@ ### @@@ 79 @@@ UTE @@@ ute @@@ ### @@@ ### @@@ 79 @@@ Accord @@@ accord @@@ accord @@@ accord @@@ 80 @@@ Airwave @@@ airwave @@@ airwave @@@ airwave @@@ 80 @@@ Ascot @@@ ascot @@@ ascot @@@ ascot @@@ 80 @@@ Avancier @@@ avancier @@@ ### @@@ avancier @@@ 80 @@@ Beat @@@ beat @@@ ### @@@ beat @@@ 80 @@@ Capa @@@ capa @@@ ### @@@ capa @@@ 80 @@@ City @@@ city @@@ ### @@@ city @@@ 80 @@@ Civic @@@ civic @@@ civic @@@ civic @@@ 80 @@@ Civic Ferio @@@ civicferio @@@ ### @@@ civic_ferio @@@ 80 @@@ Civic Type R @@@ civic-type-r @@@ ### @@@ civic_type_r @@@ 80 @@@ Concerto @@@ concerto @@@ concerto @@@ concerto @@@ 80 @@@ Crossroad @@@ crossroad @@@ ### @@@ crossroad @@@ 80 @@@ Crosstour @@@ crosstour @@@ crosstour @@@ crosstour @@@ 80 @@@ CR-V @@@ cr-v @@@ cr-v @@@ cr-v @@@ 80 @@@ CR-X @@@ cr-x @@@ ### @@@ cr-x @@@ 80 @@@ CR-Z @@@ cr-z @@@ cr-z @@@ cr-z @@@ 80 @@@ Domani @@@ domani @@@ ### @@@ domani @@@ 80 @@@ Edix @@@ edix @@@ ### @@@ edix @@@ 80 @@@ Element @@@ element @@@ element @@@ element @@@ 80 @@@ Elysion @@@ elysion @@@ elysion @@@ elysion @@@ 80 @@@ FCX Clarity @@@ fcx-clarity @@@ ### @@@ fcx_clarity @@@ 80 @@@ Fit @@@ fit @@@ fit @@@ fit @@@ 80 @@@ Fit Aria @@@ fit-aria @@@ ### @@@ fit_aria @@@ 80 @@@ Freed @@@ freed @@@ ### @@@ freed @@@ 80 @@@ FR-V @@@ fr-v @@@ ### @@@ fr-v @@@ 80 @@@ HR-V @@@ hr-v @@@ hr-v @@@ hr-v @@@ 80 @@@ Insight @@@ insight @@@ insight @@@ insight @@@ 80 @@@ Inspire @@@ inspire @@@ ### @@@ inspire @@@ 80 @@@ Integra SJ @@@ integra_sj @@@ ### @@@ integra @@@ 80 @@@ Jazz @@@ jazz @@@ jazz @@@ jazz @@@ 80 @@@ Life @@@ life @@@ life @@@ life @@@ 80 @@@ Logo @@@ logo @@@ logo @@@ logo @@@ 80 @@@ Mobilio @@@ mobilio @@@ mobilio @@@ mobilio @@@ 80 @@@ Odyssey @@@ odyssey @@@ odyssey @@@ odyssey @@@ 80 @@@ Odyssey (North America) @@@ odyssey-na @@@ ### @@@ ### @@@ 80 @@@ Orthia @@@ orthia @@@ orthia @@@ orthia @@@ 80 @@@ Partner @@@ partner @@@ partner @@@ partner @@@ 80 @@@ Passport @@@ passport @@@ ### @@@ ### @@@ 80 @@@ Pilot @@@ pilot @@@ pilot @@@ pilot @@@ 80 @@@ Prelude @@@ prelude @@@ prelude @@@ prelude @@@ 80 @@@ Quint @@@ quint @@@ ### @@@ quintet @@@ 80 @@@ Rafaga @@@ rafaga @@@ ### @@@ rafaga @@@ 80 @@@ Ridgeline @@@ ridgeline @@@ ridgeline @@@ ridgeline @@@ 80 @@@ S2000 @@@ s2000 @@@ ### @@@ s2000 @@@ 80 @@@ Saber @@@ saber @@@ ### @@@ saber @@@ 80 @@@ S-MX @@@ s-mx @@@ ### @@@ s-mx @@@ 80 @@@ Stepwgn @@@ stepwgn @@@ stepwgn @@@ stepwgn @@@ 80 @@@ Stream @@@ stream @@@ stream @@@ stream @@@ 80 @@@ That'S @@@ thats @@@ ### @@@ that~s @@@ 80 @@@ Today @@@ today @@@ ### @@@ today @@@ 80 @@@ Torneo @@@ torneo @@@ ### @@@ torneo @@@ 80 @@@ Vamos @@@ vamos @@@ vamos @@@ vamos @@@ 80 @@@ Vezel @@@ vezel @@@ ### @@@ vezel @@@ 80 @@@ Vigor @@@ vigor @@@ ### @@@ vigor @@@ 80 @@@ Z @@@ z @@@ ### @@@ z @@@ 80 @@@ Zest @@@ zest @@@ ### @@@ zest @@@ 80 @@@ Landscape @@@ landscape @@@ ### @@@ ### @@@ 81 @@@ H1 @@@ h1 @@@ h1 @@@ h1_alpha @@@ 82 @@@ H3 @@@ h3 @@@ h3 @@@ h3 @@@ 82 @@@ Accent @@@ accent @@@ accent @@@ accent @@@ 83 @@@ Aslan @@@ aslan @@@ ### @@@ ### @@@ 83 @@@ Atos @@@ atos @@@ ### @@@ atos @@@ 83 @@@ Avante @@@ avante @@@ avante @@@ avante @@@ 83 @@@ Centennial @@@ centennial @@@ ### @@@ centennial @@@ 83 @@@ Elantra @@@ elantra @@@ elantra @@@ elantra @@@ 83 @@@ Equus @@@ equus @@@ equus @@@ equus @@","@ 83 @@@ Excel @@@ excel @@@ ### @@@ excel @@@ 83 @@@ Galloper @@@ galloper @@@ galloper @@@ galloper @@@ 83 @@@ Genesis @@@ genesis @@@ genesis @@@ genesis @@@ 83 @@@ Genesis Coupe @@@ genesis_coupe @@@ ### @@@ ### @@@ 83 @@@ Getz @@@ getz @@@ getz @@@ getz @@@ 83 @@@ Grace @@@ grace-minivan @@@ ### @@@ grace @@@ 83 @@@ Grandeur @@@ grandeur @@@ grandeur @@@ grandeur @@@ 83 @@@ H-1 (Grand Starex) @@@ ### @@@ h-1_grand_starex @@@ h1 @@@ 83 @@@ H-100 @@@ ### @@@ h-100 @@@ ### @@@ 83 @@@ i10 @@@ i10 @@@ ### @@@ i10 @@@ 83 @@@ i20 @@@ i20 @@@ i20 @@@ i20 @@@ 83 @@@ i30 @@@ i30 @@@ i30 @@@ i30 @@@ 83 @@@ i40 @@@ i40 @@@ i40 @@@ i40 @@@ 83 @@@ ix20 @@@ ix20 @@@ ### @@@ ix20 @@@ 83 @@@ ix35 @@@ ix35 @@@ ix35 @@@ ix35 @@@ 83 @@@ ix55 @@@ ix55 @@@ ix55 @@@ ix55 @@@ 83 @@@ Lantra @@@ lantra @@@ lantra @@@ lantra @@@ 83 @@@ Lavita @@@ lavita @@@ ### @@@ lavita @@@ 83 @@@ Marcia @@@ marcia @@@ ### @@@ marcia @@@ 83 @@@ Matrix @@@ matrix @@@ matrix @@@ matrix @@@ 83 @@@ Maxcruz @@@ maxcruz @@@ ### @@@ maxcruze @@@ 83 @@@ NF @@@ ### @@@ nf @@@ nf @@@ 83 @@@ Pony @@@ pony @@@ ### @@@ pony @@@ 83 @@@ Porter @@@ ### @@@ porter @@@ ### @@@ 83 @@@ Santa Fe @@@ santa-fe @@@ santa_fe @@@ santa_fe @@@ 83 @@@ Santamo @@@ santamo @@@ ### @@@ santamo @@@ 83 @@@ Scoupe @@@ s-coupe @@@ ### @@@ s_coupe @@@ 83 @@@ Solaris @@@ solaris @@@ solaris @@@ solaris @@@ 83 @@@ Sonata @@@ sonata @@@ sonata @@@ sonata @@@ 83 @@@ Starex @@@ starex @@@ starex @@@ starex @@@ 83 @@@ Stellar @@@ stellar @@@ ### @@@ stellar @@@ 83 @@@ Terracan @@@ terracan @@@ terracan @@@ terracan @@@ 83 @@@ Tiburon @@@ tiburon @@@ tiburon @@@ tiburon @@@ 83 @@@ Trajet @@@ trajet @@@ ### @@@ trajet @@@ 83 @@@ Tucson @@@ tucson @@@ tucson @@@ tucson @@@ 83 @@@ Tuscani @@@ tuscani @@@ ### @@@ tuscani @@@ 83 @@@ Veloster @@@ veloster @@@ veloster @@@ veloster @@@ 83 @@@ Veracruz @@@ veracruz @@@ ### @@@ veracruz @@@ 83 @@@ Verna @@@ verna @@@ verna @@@ verna @@@ 83 @@@ XG @@@ xg @@@ ### @@@ ### @@@ 83 @@@ EX @@@ ex @@@ ### @@@ ### @@@ 84 @@@ EX25 @@@ ### @@@ ex25 @@@ ex25 @@@ 84 @@@ EX35 @@@ ### @@@ ex35 @@@ ex35 @@@ 84 @@@ EX37 @@@ ### @@@ ex37 @@@ ex37 @@@ 84 @@@ FX @@@ fx @@@ ### @@@ ### @@@ 84 @@@ FX30 @@@ ### @@@ fx30 @@@ fx30d @@@ 84 @@@ FX35 @@@ ### @@@ fx35 @@@ fx35 @@@ 84 @@@ FX37 @@@ ### @@@ fx37 @@@ fx37 @@@ 84 @@@ FX45 @@@ ### @@@ fx45 @@@ fx45 @@@ 84 @@@ FX50 @@@ ### @@@ fx50 @@@ fx50 @@@ 84 @@@ G @@@ g @@@ ### @@@ ### @@@ 84 @@@ G20 @@@ ### @@@ g20 @@@ g20 @@@ 84 @@@ G25 @@@ ### @@@ g25 @@@ g25 @@@ 84 @@@ G35 @@@ ### @@@ g35 @@@ g35 @@@ 84 @@@ G37 @@@ ### @@@ g37 @@@ g37 @@@ 84 @@@ I @@@ i @@@ ### @@@ i30 @@@ 84 @@@ J @@@ j @@@ ### @@@ ### @@@ 84 @@@ J30 @@@ ### @@@ j30 @@@ j30 @@@ 84 @@@ JX @@@ jx @@@ jx @@@ jx35 @@@ 84 @@@ M @@@ m @@@ ### @@@ ### @@@ 84 @@@ M25 @@@ ### @@@ m25 @@@ m25 @@@ 84 @@@ M35 @@@ ### @@@ m35 @@@ m35 @@@ 84 @@@ M37 @@@ ### @@@ m37 @@@ m37 @@@ 84 @@@ M45 @@@ ### @@@ m45 @@@ m45 @@@ 84 @@@ M56 @@@ ### @@@ m56 @@@ m56 @@@ 84 @@@ Q @@@ q @@@ ### @@@ ### @@@ 84 @@@ Q40 @@@ q40 @@@ ### @@@ ### @@@ 84 @@@ Q45 @@@ ### @@@ q45 @@@ q45 @@@ 84 @@@ Q50 @@@ q50 @@@ q50 @@@ q50 @@@ 84 @@@ Q60 @@@ q60 @@@ ### @@@ q60 @@@ 84 @@@ Q70 @@@ q70 @@@ q70 @@@ q70 @@@ 84 @@@ QX @@@ qx @@@ ### @@@ ### @@@ 84 @@@ QX4 @@@ ### @@@ qx4 @@@ qx4 @@@ 84 @@@ QX50 @@@ qx50 @@@ qx50 @@@ qx50 @@@ 84 @@@ QX56 @@@ ### @@@ qx56 @@@ qx56 @@@ 84 @@@ QX60 @@@ qx60 @@@ qx60 @@@ qx60 @@@ 84 @@@ QX70 @@@ qx70 @@@ qx70 @@@ qx70 @@@ 84 @@@ QX80 @@@ qx80 @@@ qx80 @@@ qx80 @@@ 84 @@@ Elba @@@ elba @@@ ### @@@ ### @@@ 85 @@@ Mille @@@ mille @@@ ### @@@ ### @@@ 85 @@@ Pars @@@ ### @@@ pars @@@ ### @@@ 87 @@@ Paykan @@@ paykan @@@ ### @@@ ### @@@ 87 @@@ Samand @@@ samand @@@ samand @@@ samand @@@ 87 @@@ Soren @@@ soren @@@ ### @@@ soren @@@ 87 @@@ Commendatore 112i @@@ commendatore-112i @@@ ### @@@ ### @@@ 88 @@@ Imperator 108i @@@ imperator-108i @@@ ### @@@ ### @@@ 88 @@@ Spyder @@@ spyder @@@ ### @@@ ### @@@ 88 @@@ Amigo @@@ amigo @@@ ### @@@ amigo @@@ 89 @@@ Ascender @@@ ascender @@@ ### @@@ ascender @@@ 89 @@@ Aska @@@ aska @@@ ### @@@ aska @@@ 89 @@@ Axiom @@@ axiom @@@ axiom @@@ axiom @@@ 89 @@@ Bighorn @@@ bighorn @@@ bighorn @@@ bighorn @@@ 89 @@@ D-Max @@@ d-max @@@ d-max @@@ d-max @@@ 89 @@@ Gemini @@@ gemini @@@ gemini @@@ gemini @@@ 89 @@@ Impulse @@@ impulse @@@ ### @@@ impulse @@@ 89 @@@ KB @@@ kb @@@ ### @@@ ### @@@ 89 @@@ MU @@@ mu @@@ mu @@@ ### @@@ 89 @@@ MU-7 @@@ mu-7 @@@ ### @@@ ### @@@ 89 @@@ MU-X @@@ mu-x @@@ ### @@@ ### @@@ 89 @@@ Piazza @@@ piazza @@@ ### @@@ piazza @@@ 89 @@@ Stylus @@@ stylus @@@ ### @@@ stylus @@@ 89 @@@ TF (Pickup) @@@ tf @@@ ### @@@ tf @@@ 89 @@@ Trooper @@@ trooper @@@ trooper @@@ trooper @@@ 89 @@@ VehiCross @@@ vehicross @@@ vehi_cross @@@ vehicross @@@ 89 @@@ Wizard @@@ wizard @@@ ### @@@ wizard @@@ 89 @@@ Daily @@@ ### @@@ daily @@@ ### @@@ 90 @@@ Massif @@@ massif @@@ ### @@@ ### @@@ 90 @@@ J2 (Yueyue) @@@ j2 @@@ ### @@@ j2 @@@ 91 @@@ J3 (Tongyue,Tojoy) @@@ j3 @@@ ### @@@ j3 @@@ 91 @@@ J5 (Heyue) @@@ j5 @@@ ### @@@ j5 @@@ 91 @@@ J6 (Heyue RS) @@@ j6 @@@ ### @@@ j6 @@@ 91 @@@ J7 (Binyue) @@@ j7 @@@ ### @@@ j7 @@@ 91 @@@ Refine @@@ refine @@@ ### @@@ ### @@@ 91 @@@ Rein @@@ ### @@@ rein @@@ ### @@@ 91 @@@ S1 (Rein) @@@ rein-s1 @@@ ### @@@ s1 @@@ 91 @@@ S5 (Eagle) @@@ eagle-s5 @@@ ### @@@ s5 @@@ 91 @@@ E-type @@@ e-type @@@ e-type @@@ ### @@@ 92 @@@ F-Type @@@ f-type @@@ f-type @@@ f-type @@@ 92 @@@ S-Type @@@ s-type @@@ s-type @@@ s-type @@@ 92 @@@ XE @@@ xe @@@ ### @@@ xe @@@ 92 @@@ XF @@@ xf @@@ xf @@@ xf @@@ 92 @@@ XFR @@@ xfr @@@ xfr @@@ ### @@@ 92 @@@ XJ @@@ xj @@@ xj @@@ xj @@@ 92 @@@ XJ220 @@@ xj220 @@@ ### @@@ ### @@@ 92 @@@ XJR @@@ xjr @@@ xjr @@@ ### @@@ 92 @@@ XK @@@ xk8 @@@ xk @@@ xk @@@ 92 @@@ XKR @@@ xkr @@@ xkr @@@ ### @@@ 92 @@@ X-Type @@@ x-type @@@ x-type @@@ x-type @@@ 92 @@@ Cherokee @@@ cherokee @@@ cherokee @@@ cherokee @@@ 93 @@@ CJ @@@ cj @@@ ### @@@ ### @@@ 93 @@@ Commander @@@ commander @@@ commander @@@ commander @@@ 93 @@@ Compass @@@ compass @@@ compass @@@ compass @@@ 93 @@@ Grand ","Cherokee @@@ grand-cherokee @@@ grand_cherokee @@@ grand_cherokee @@@ 93 @@@ Grand Cherokee SRT8 @@@ grand-cherokee-srt8 @@@ ### @@@ ### @@@ 93 @@@ Grand Wagoneer @@@ grand-wagoneer @@@ ### @@@ ### @@@ 93 @@@ Liberty (North America) @@@ liberty-na @@@ liberty @@@ liberty @@@ 93 @@@ Liberty (Patriot) @@@ patriot @@@ patriot @@@ patriot @@@ 93 @@@ Renegade @@@ renegade @@@ ### @@@ renegade @@@ 93 @@@ Wrangler @@@ wrangler @@@ wrangler @@@ wrangler @@@ 93 @@@ S-V8 @@@ s-v8 @@@ ### @@@ ### @@@ 94 @@@ Baodian @@@ baodian @@@ baodian @@@ ### @@@ 95 @@@ Avella @@@ avella @@@ avella @@@ avella @@@ 96 @@@ Besta @@@ ### @@@ besta @@@ besta @@@ 96 @@@ Cadenza @@@ cadenza @@@ ### @@@ cadenza @@@ 96 @@@ Capital @@@ capital @@@ ### @@@ ### @@@ 96 @@@ Carens @@@ carens @@@ carens @@@ carens @@@ 96 @@@ Carnival @@@ carnival @@@ carnival @@@ carnival @@@ 96 @@@ Cee'd @@@ ceed @@@ ceed @@@ cee~d @@@ 96 @@@ Cee'd GT @@@ ceed-gt @@@ ceed_gt @@@ ### @@@ 96 @@@ Cerato @@@ cerato @@@ cerato @@@ cerato @@@ 96 @@@ Clarus @@@ clarus @@@ clarus @@@ clarus @@@ 96 @@@ Concord @@@ concord @@@ ### @@@ ### @@@ 96 @@@ Elan @@@ elan @@@ ### @@@ elan @@@ 96 @@@ Enterprise @@@ enterprise @@@ ### @@@ enterprise @@@ 96 @@@ Forte @@@ forte @@@ ### @@@ forte @@@ 96 @@@ Joice @@@ joice @@@ ### @@@ joice @@@ 96 @@@ K @@@ ### @@@ k @@@ ### @@@ 96 @@@ K3 @@@ k3 @@@ ### @@@ k3 @@@ 96 @@@ K5 @@@ k5 @@@ ### @@@ k5 @@@ 96 @@@ Magentis @@@ magentis @@@ magentis @@@ magentis @@@ 96 @@@ Mohave @@@ borrego @@@ mohave @@@ mohave @@@ 96 @@@ Morning @@@ morning @@@ ### @@@ morning @@@ 96 @@@ Opirus @@@ opirus @@@ opirus @@@ opirus @@@ 96 @@@ Optima @@@ optima @@@ optima @@@ optima @@@ 96 @@@ Picanto @@@ picanto @@@ picanto @@@ picanto @@@ 96 @@@ Potentia @@@ potentia @@@ ### @@@ potentia @@@ 96 @@@ Pregio @@@ ### @@@ pregio @@@ pregio @@@ 96 @@@ Pride @@@ pride @@@ pride @@@ pride @@@ 96 @@@ Quoris @@@ quoris @@@ quoris @@@ quoris @@@ 96 @@@ Ray @@@ ray @@@ ### @@@ ray @@@ 96 @@@ Rio @@@ rio @@@ rio @@@ rio @@@ 96 @@@ Sedona @@@ sedona @@@ ### @@@ sedona @@@ 96 @@@ Sephia @@@ sephia @@@ sephia @@@ sephia @@@ 96 @@@ Shuma @@@ shuma @@@ shuma @@@ shuma @@@ 96 @@@ Sorento @@@ sorento @@@ sorento @@@ sorento @@@ 96 @@@ Soul @@@ soul @@@ soul @@@ soul @@@ 96 @@@ Spectra @@@ spectra @@@ spectra @@@ spectra @@@ 96 @@@ Sportage @@@ sportage @@@ sportage @@@ sportage @@@ 96 @@@ Venga @@@ venga @@@ venga @@@ venga @@@ 96 @@@ Visto @@@ visto @@@ visto @@@ visto @@@ 96 @@@ X-Trek @@@ x-trek @@@ x-trek @@@ x-trek @@@ 96 @@@ Agera @@@ agera @@@ ### @@@ agera @@@ 97 @@@ CC8S @@@ cc8s @@@ ### @@@ cc8s @@@ 97 @@@ CCR @@@ ccr @@@ ### @@@ ccr @@@ 97 @@@ CCX @@@ ccx @@@ ### @@@ ccx @@@ 97 @@@ One:1 @@@ one-1 @@@ ### @@@ ### @@@ 97 @@@ X-Bow @@@ x-bow @@@ ### @@@ ### @@@ 98 @@@ Aventador @@@ aventador @@@ aventador @@@ aventador @@@ 99 @@@ Countach @@@ countach @@@ countach @@@ countach @@@ 99 @@@ Diablo @@@ diablo @@@ ### @@@ diablo @@@ 99 @@@ Espada @@@ espada @@@ ### @@@ espada @@@ 99 @@@ Gallardo @@@ gallardo @@@ gallardo @@@ gallardo @@@ 99 @@@ HuracĘn @@@ huracan @@@ ### @@@ huracan @@@ 99 @@@ Jalpa @@@ jalpa @@@ jalpa @@@ ### @@@ 99 @@@ Jarama @@@ jarama @@@ ### @@@ jarama @@@ 99 @@@ LM001 @@@ lm-001 @@@ ### @@@ ### @@@ 99 @@@ LM002 @@@ lm-002 @@@ lm002 @@@ lm_002 @@@ 99 @@@ Murcielago @@@ murcielago @@@ murcielago @@@ murcielago @@@ 99 @@@ Reventon @@@ reventon @@@ ### @@@ reventon @@@ 99 @@@ Sesto Elemento @@@ sesto-elemento @@@ ### @@@ ### @@@ 99 @@@ Urraco @@@ urraco @@@ ### @@@ ### @@@ 99 @@@ Veneno @@@ veneno @@@ ### @@@ ### @@@ 99 @@@ Beta @@@ beta @@@ ### @@@ ### @@@ 100 @@@ Dedra @@@ dedra @@@ ### @@@ dedra @@@ 100 @@@ Delta @@@ delta @@@ delta @@@ delta @@@ 100 @@@ Fulvia @@@ fulvia @@@ ### @@@ ### @@@ 100 @@@ Gamma @@@ gamma @@@ ### @@@ ### @@@ 100 @@@ Hyena @@@ hyena @@@ ### @@@ ### @@@ 100 @@@ Kappa @@@ kappa @@@ kappa @@@ kappa @@@ 100 @@@ Lybra @@@ lybra @@@ lybra @@@ lybra @@@ 100 @@@ Musa @@@ musa @@@ ### @@@ musa @@@ 100 @@@ Phedra @@@ phedra @@@ ### @@@ phedra @@@ 100 @@@ Prisma @@@ prisma @@@ prisma @@@ prisma @@@ 100 @@@ Thema @@@ thema @@@ thema @@@ thema @@@ 100 @@@ Thesis @@@ thesis @@@ thesis @@@ thesis @@@ 100 @@@ Trevi @@@ trevi @@@ ### @@@ ### @@@ 100 @@@ Y10 @@@ y10 @@@ ### @@@ y @@@ 100 @@@ Ypsilon @@@ ypsilon @@@ ypsilon @@@ ypsilon @@@ 100 @@@ Zeta @@@ zeta @@@ ### @@@ zeta @@@ 100 @@@ Defender @@@ defender @@@ defender @@@ defender @@@ 101 @@@ Discovery @@@ discovery @@@ discovery @@@ discovery @@@ 101 @@@ Discovery Sport @@@ discovery-sport @@@ ### @@@ discovery_sport @@@ 101 @@@ Freelander @@@ freelander @@@ freelander @@@ freelander @@@ 101 @@@ Range Rover @@@ range-rover @@@ range_rover @@@ range_rover @@@ 101 @@@ Range Rover Evoque @@@ evoque @@@ range_rover_evoque @@@ range_rover_evoque @@@ 101 @@@ Range Rover Sport @@@ range_rover_sport @@@ range_rover_sport @@@ range_rover_sport @@@ 101 @@@ Series I @@@ series-i @@@ ### @@@ ### @@@ 101 @@@ Series II @@@ series-ii @@@ ### @@@ ### @@@ 101 @@@ Series III @@@ series-iii @@@ series_i @@@ ### @@@ 101 @@@ Fashion (CV9) @@@ fashion @@@ ### @@@ ### @@@ 102 @@@ Forward @@@ forward @@@ ### @@@ ### @@@ 102 @@@ Maxus @@@ ### @@@ maxus @@@ ### @@@ 102 @@@ Х9 @@@ x9 @@@ ### @@@ ### @@@ 102 @@@ CT @@@ ct @@@ ct @@@ ct200h @@@ 103 @@@ ES @@@ es @@@ es @@@ es200 @@@ 103 @@@ GX @@@ gx @@@ gx @@@ gx460 @@@ 103 @@@ HS @@@ hs @@@ hs @@@ hs250h @@@ 103 @@@ IS @@@ is @@@ is @@@ is200 @@@ 103 @@@ IS F @@@ is-f @@@ is_f @@@ is_f @@@ 103 @@@ LFA @@@ lfa @@@ ### @@@ lfa @@@ 103 @@@ LS @@@ ls @@@ ls @@@ ls400 @@@ 103 @@@ LX @@@ lx @@@ lx @@@ lx450 @@@ 103 @@@ NX @@@ nx @@@ nx @@@ nx200 @@@ 103 @@@ RC @@@ rc @@@ ### @@@ rc300h @@@ 103 @@@ RC F @@@ rc-f @@@ ### @@@ rc_f @@@ 103 @@@ RX @@@ rx @@@ rx @@@ rx200t @@@ 103 @@@ SC @@@ sc @@@ sc @@@ sc300 @@@ 103 @@@ Leopard @@@ leopard @@@ ### @@@ ### @@@ 104 @@@ Breez (520) @@@ 520 @@@ breez @@@ breez @@@ 105 @@@ Cebrium (720) @@@ cebrium @@@ cebrium @@@ cebrium @@@ 105 @@@ Celliya (530) @@@ celliya @@@ celliya @@@ celliya @@@ 105 @@@ Smily @@@ s","mily @@@ smily @@@ smily @@@ 105 @@@ Solano @@@ solano @@@ solano @@@ solano @@@ 105 @@@ X50 @@@ x50 @@@ x50 @@@ x50 @@@ 105 @@@ X60 @@@ x60 @@@ x60 @@@ x60 @@@ 105 @@@ Aviator @@@ aviator @@@ aviator @@@ aviator @@@ 106 @@@ Mark @@@ ### @@@ mark @@@ ### @@@ 106 @@@ Mark LT @@@ mark-lt @@@ ### @@@ mark_lt @@@ 106 @@@ Mark VII @@@ mark7 @@@ ### @@@ ### @@@ 106 @@@ Mark VIII @@@ mark-viii @@@ ### @@@ mark_viii @@@ 106 @@@ MKC @@@ mkc @@@ ### @@@ mkc @@@ 106 @@@ MKS @@@ mks @@@ ### @@@ mks @@@ 106 @@@ MKT @@@ mkt @@@ ### @@@ mkt @@@ 106 @@@ MKX @@@ mkx @@@ mkx @@@ mkx @@@ 106 @@@ MKZ @@@ mkz @@@ ### @@@ mkz @@@ 106 @@@ Navigator @@@ navigator @@@ navigator @@@ navigator @@@ 106 @@@ Town Car @@@ towncar @@@ town_car @@@ town_car @@@ 106 @@@ 340R @@@ 340r @@@ ### @@@ ### @@@ 107 @@@ Eclat @@@ eclat @@@ ### @@@ ### @@@ 107 @@@ Elise @@@ elise @@@ ### @@@ elise @@@ 107 @@@ Elite @@@ elite @@@ ### @@@ ### @@@ 107 @@@ Esprit @@@ esprit @@@ ### @@@ esprit @@@ 107 @@@ Europa @@@ europa @@@ ### @@@ ### @@@ 107 @@@ Europa S @@@ europa-s @@@ ### @@@ europa_s @@@ 107 @@@ Evora @@@ evora @@@ ### @@@ evora @@@ 107 @@@ Exige @@@ exige @@@ ### @@@ exige @@@ 107 @@@ TX @@@ tx @@@ ### @@@ ### @@@ 108 @@@ Luxgen5 @@@ luxgen5 @@@ ### @@@ 5_sedan @@@ 109 @@@ Luxgen7 MPV @@@ luxgen7-mpv @@@ ### @@@ 7_mpv @@@ 109 @@@ Luxgen7 SUV @@@ luxgen7-suv @@@ ### @@@ 7_suv @@@ 109 @@@ U6 Turbo @@@ u6-turbo @@@ ### @@@ ### @@@ 109 @@@ U7 Turbo @@@ u7-turbo @@@ ### @@@ ### @@@ 109 @@@ Armada @@@ armada @@@ ### @@@ ### @@@ 110 @@@ Bolero @@@ bolero @@@ ### @@@ ### @@@ 110 @@@ CJ-3 @@@ jeep-cj-3 @@@ ### @@@ ### @@@ 110 @@@ Marshal @@@ marshal @@@ ### @@@ ### @@@ 110 @@@ MM @@@ mm @@@ ### @@@ ### @@@ 110 @@@ NC 640 DP @@@ nc640dp @@@ ### @@@ ### @@@ 110 @@@ Verito @@@ verito @@@ ### @@@ ### @@@ 110 @@@ Xylo @@@ xylo @@@ ### @@@ ### @@@ 110 @@@ GTS @@@ gts @@@ ### @@@ ### @@@ 111 @@@ LM 400 @@@ lm-400 @@@ ### @@@ ### @@@ 111 @@@ LM 500 @@@ lm-500 @@@ ### @@@ ### @@@ 111 @@@ Mantis @@@ mantis @@@ ### @@@ ### @@@ 111 @@@ Marcasite @@@ marcasite @@@ ### @@@ ### @@@ 111 @@@ 5EXi @@@ 5exi @@@ ### @@@ ### @@@ 112 @@@ Sportster @@@ sportster @@@ ### @@@ ### @@@ 112 @@@ B1 @@@ b1 @@@ ### @@@ b1 @@@ 113 @@@ B2 @@@ b2 @@@ ### @@@ b2 @@@ 113 @@@ 800 @@@ 800 @@@ ### @@@ ### @@@ 114 @@@ 1000 @@@ 1000 @@@ ### @@@ ### @@@ 114 @@@ Alto @@@ alto @@@ ### @@@ ### @@@ 114 @@@ Baleno @@@ baleno @@@ ### @@@ ### @@@ 114 @@@ Esteem @@@ esteem @@@ ### @@@ ### @@@ 114 @@@ Gypsy @@@ gypsy @@@ ### @@@ ### @@@ 114 @@@ Versa @@@ versa @@@ ### @@@ ### @@@ 114 @@@ Wagon R @@@ wagon-r @@@ ### @@@ ### @@@ 114 @@@ Zen @@@ zen @@@ ### @@@ ### @@@ 114 @@@ 228 @@@ 228 @@@ ### @@@ ### @@@ 115 @@@ 420 @@@ 420 @@@ ### @@@ ### @@@ 115 @@@ 3200 GT @@@ 3200-gt @@@ 3200_gt @@@ 3200gt @@@ 115 @@@ 4200 GT @@@ 4200gt @@@ ### @@@ ### @@@ 115 @@@ 4300 GT Coupe @@@ ### @@@ 4300_gt_coupe @@@ coupe @@@ 115 @@@ Barchetta Stradale @@@ barchetta-stradale @@@ ### @@@ ### @@@ 115 @@@ Biturbo @@@ biturbo @@@ ### @@@ ### @@@ 115 @@@ Bora @@@ bora @@@ ### @@@ ### @@@ 115 @@@ Chubasco @@@ chubasco @@@ ### @@@ ### @@@ 115 @@@ Ghibli @@@ ghibli @@@ ghibli @@@ ghibli @@@ 115 @@@ GranSport @@@ ### @@@ gransport @@@ gransport @@@ 115 @@@ GranTurismo @@@ granturismo @@@ granturismo @@@ granturismo @@@ 115 @@@ Indy @@@ indy @@@ ### @@@ ### @@@ 115 @@@ Karif @@@ karif @@@ ### @@@ ### @@@ 115 @@@ Khamsin @@@ khamsin @@@ ### @@@ ### @@@ 115 @@@ Kyalami @@@ kyalami @@@ ### @@@ ### @@@ 115 @@@ MC12 @@@ mc12 @@@ ### @@@ mc12 @@@ 115 @@@ Merak @@@ merak @@@ ### @@@ ### @@@ 115 @@@ Mexico @@@ mexico @@@ ### @@@ ### @@@ 115 @@@ Quattroporte @@@ quattroporte @@@ quattroporte @@@ quattroporte @@@ 115 @@@ Royale @@@ royale @@@ ### @@@ ### @@@ 115 @@@ Shamal @@@ shamal @@@ ### @@@ ### @@@ 115 @@@ 57 @@@ 57 @@@ 57 @@@ 57 @@@ 116 @@@ 62 @@@ 62 @@@ 62 @@@ 62 @@@ 116 @@@ 57S @@@ ### @@@ 57s @@@ ### @@@ 116 @@@ 2 @@@ 2 @@@ 2 @@@ mazda2 @@@ 117 @@@ 3 @@@ 3 @@@ 3 @@@ mazda3 @@@ 117 @@@ 5 @@@ 5 @@@ 5 @@@ mazda5 @@@ 117 @@@ 6 @@@ 6 @@@ 6 @@@ mazda6 @@@ 117 @@@ 121 @@@ 121 @@@ 121 @@@ 121 @@@ 117 @@@ 323 @@@ 323 @@@ 323 @@@ 323 @@@ 117 @@@ 616 @@@ 616 @@@ ### @@@ ### @@@ 117 @@@ 626 @@@ 626 @@@ 626 @@@ 626 @@@ 117 @@@ 818 @@@ 818 @@@ ### @@@ ### @@@ 117 @@@ 929 @@@ 929 @@@ ### @@@ 929 @@@ 117 @@@ 1000 @@@ 1000 @@@ ### @@@ ### @@@ 117 @@@ 1300 @@@ 1300 @@@ ### @@@ ### @@@ 117 @@@ 3 MPS @@@ 3-mps @@@ 3_mps @@@ mazda3_mps @@@ 117 @@@ 6 MPS @@@ 6-mps @@@ 6_mps @@@ ### @@@ 117 @@@ Atenza @@@ atenza @@@ atenza @@@ atenza @@@ 117 @@@ Axela @@@ axela @@@ ### @@@ axela @@@ 117 @@@ AZ-1 @@@ az-1 @@@ ### @@@ ### @@@ 117 @@@ AZ-Offroad @@@ az-offroad @@@ ### @@@ az-offroad @@@ 117 @@@ AZ-Wagon @@@ az-wagon @@@ ### @@@ az-wagon @@@ 117 @@@ Biante @@@ biante @@@ ### @@@ biante @@@ 117 @@@ Bongo @@@ bongo @@@ bongo @@@ bongo @@@ 117 @@@ Bongo Friendee @@@ bongo-friendee @@@ ### @@@ bongo_friendee @@@ 117 @@@ B-series @@@ b-serie @@@ b-series @@@ b-series @@@ 117 @@@ BT-50 @@@ bt-50 @@@ bt-50 @@@ bt-50 @@@ 117 @@@ Capella @@@ capella @@@ capella @@@ capella @@@ 117 @@@ Carol @@@ carol @@@ ### @@@ carol @@@ 117 @@@ Cronos @@@ cronos @@@ ### @@@ cronos @@@ 117 @@@ CX-3 @@@ cx-3 @@@ ### @@@ cx-3 @@@ 117 @@@ CX-5 @@@ cx-5 @@@ cx-5 @@@ cx-5 @@@ 117 @@@ CX-7 @@@ cx-7 @@@ cx-7 @@@ cx-7 @@@ 117 @@@ CX-9 @@@ cx-9 @@@ cx-9 @@@ cx-9 @@@ 117 @@@ Demio @@@ demio @@@ demio @@@ demio @@@ 117 @@@ Efini MS-6 @@@ efini-ms-6 @@@ ### @@@ efini_ms-6 @@@ 117 @@@ Efini MS-8 @@@ efini-ms-8 @@@ ### @@@ efini_ms-8 @@@ 117 @@@ Efini MS-9 @@@ efini-ms-9 @@@ ### @@@ efini_ms-9 @@@ 117 @@@ Eunos 300 @@@ eunos-300 @@@ ### @@@ eunos_300 @@@ 117 @@@ Eunos 500 @@@ eunos-500 @@@ ### @@@ eunos_500 @@@ 117 @@@ Eunos 800 @@@ eunos-800 @@@ ### @@@ eunos_800 @@@ 117 @@@ Eunos Cosmo @@@ eunos-cosmo @@@ ### @@@ eunos_cosmo @@@ 117 @@@ Familia @@@ familia @@@ familia @@@ familia @@@ 117 @@@ Lantis @@@ lantis @@@ ### @@@ lantis @@@ 117 @@@ Laputa @@@ laputa @@@ ### @@@ laputa @@@ 117 @@@ Luce @@@ luci @@@ ### @@@ luce @@@ 117 @@@ Millenia @@@ millenia @@@ millenia @@@ milleni","a @@@ 117 @@@ MX-3 @@@ mx-3 @@@ mx-3 @@@ mx-3 @@@ 117 @@@ MX-5 @@@ mx-5 @@@ mx-5 @@@ mx-5 @@@ 117 @@@ MX-6 @@@ mx-6 @@@ ### @@@ mx-6 @@@ 117 @@@ Navajo @@@ navajo @@@ ### @@@ navajo @@@ 117 @@@ Persona @@@ persona @@@ ### @@@ persona @@@ 117 @@@ Premacy @@@ premacy @@@ premacy @@@ premacy @@@ 117 @@@ Proceed Levante @@@ levante @@@ ### @@@ proceed_levante @@@ 117 @@@ Proceed Marvie @@@ proceed-marvie @@@ ### @@@ proceed_marvie @@@ 117 @@@ Protege @@@ protege @@@ protege @@@ protege @@@ 117 @@@ Revue @@@ revue @@@ ### @@@ autozam_revue @@@ 117 @@@ RX-7 @@@ rx-7 @@@ ### @@@ rx-7 @@@ 117 @@@ RX-8 @@@ rx-8 @@@ rx-8 @@@ rx-8 @@@ 117 @@@ Scrum @@@ scrum @@@ ### @@@ autozam_scrum @@@ 117 @@@ Sentia @@@ sentia @@@ ### @@@ sentia @@@ 117 @@@ Spiano @@@ spiano @@@ ### @@@ spiano @@@ 117 @@@ Tribute @@@ tribute @@@ tribute @@@ tribute @@@ 117 @@@ Verisa @@@ verisa @@@ ### @@@ verisa @@@ 117 @@@ Xedos 6 @@@ xedos-6 @@@ xedos_6 @@@ xedos_6 @@@ 117 @@@ Xedos 9 @@@ xedos-9 @@@ xedos_9 @@@ xedos_9 @@@ 117 @@@ 540C @@@ 540c @@@ ### @@@ ### @@@ 118 @@@ 570S @@@ 570s @@@ ### @@@ ### @@@ 118 @@@ 650S @@@ 650s @@@ ### @@@ 650s @@@ 118 @@@ F1 @@@ f1 @@@ ### @@@ f1 @@@ 118 @@@ MP4-12C @@@ mp4-12c @@@ ### @@@ 12c @@@ 118 @@@ P1 @@@ p1 @@@ ### @@@ p1 @@@ 118 @@@ Club @@@ club @@@ ### @@@ ### @@@ 119 @@@ Track @@@ track @@@ ### @@@ ### @@@ 119 @@@ 190 (W201) @@@ 190-w201 @@@ 190_w201 @@@ 190 @@@ 120 @@@ A-klasse @@@ a-klasse @@@ a-klass @@@ a-class @@@ 120 @@@ A-klasse AMG @@@ a-klasse-amg @@@ ### @@@ ### @@@ 120 @@@ AMG GLE @@@ amg-gle @@@ ### @@@ ### @@@ 120 @@@ AMG GLE Coupe @@@ amg-gle-coupe @@@ ### @@@ ### @@@ 120 @@@ AMG GT @@@ amg-gt @@@ ### @@@ amg_gt @@@ 120 @@@ B-klasse @@@ b-klasse @@@ b-klass @@@ b-class @@@ 120 @@@ Citan @@@ citan @@@ ### @@@ ### @@@ 120 @@@ C-klasse @@@ c-klasse @@@ c-klass @@@ c-class @@@ 120 @@@ C-klasse AMG @@@ c-klasse-amg @@@ c-klass_amg @@@ ### @@@ 120 @@@ CLA-klasse @@@ cla-klasse @@@ cla-klass @@@ cla-class @@@ 120 @@@ CLA-klasse AMG @@@ cla-classe-amg @@@ ### @@@ ### @@@ 120 @@@ CLC-klasse @@@ clc-klasse @@@ ### @@@ clc-class @@@ 120 @@@ CLK-klasse @@@ clk-klasse @@@ clk-klass @@@ clk-class @@@ 120 @@@ CLK-klasse AMG @@@ clk-klasse-amg @@@ ### @@@ ### @@@ 120 @@@ CL-klasse @@@ cl-klasse @@@ cl-klass @@@ cl-class @@@ 120 @@@ CL-klasse AMG @@@ cl-klasse-amg @@@ ### @@@ ### @@@ 120 @@@ CLS-klasse @@@ cls-klasse @@@ cls-klass @@@ cls-class @@@ 120 @@@ CLS-klasse AMG @@@ cls-klasse-amg @@@ ### @@@ ### @@@ 120 @@@ E-klasse @@@ e-klasse @@@ e-klass @@@ e-class @@@ 120 @@@ E-klasse AMG @@@ e-klasse-amg @@@ e-klass_amg @@@ ### @@@ 120 @@@ G-klasse @@@ g-klasse @@@ g-klass @@@ g-class @@@ 120 @@@ G-klasse AMG @@@ g-klasse-amg @@@ ### @@@ ### @@@ 120 @@@ G-klasse AMG 6x6 @@@ g-klasse-amg-6x6 @@@ ### @@@ ### @@@ 120 @@@ GLA-klasse @@@ gla-klasse @@@ gla-klass @@@ gla-class @@@ 120 @@@ GLA-klasse AMG @@@ gla-klasse-amg @@@ ### @@@ ### @@@ 120 @@@ GLC-klasse @@@ glc-klasse @@@ ### @@@ glc-class @@@ 120 @@@ GLE-klasse @@@ gle @@@ ### @@@ gle-class @@@ 120 @@@ GLE Coupe @@@ gle-coupe @@@ ### @@@ ### @@@ 120 @@@ GLK-klasse @@@ glk-klasse @@@ glk-klass @@@ glk-class @@@ 120 @@@ GL-klasse @@@ gl-klasse @@@ gl-klass @@@ gl-class @@@ 120 @@@ GL-klasse AMG @@@ gl-klasse-amg @@@ g-klass_amg @@@ ### @@@ 120 @@@ Maybach S-klasse @@@ maybach-s-klasse @@@ ### @@@ ### @@@ 120 @@@ M-klasse @@@ m-klasse @@@ m-klass @@@ m-class @@@ 120 @@@ M-klasse AMG @@@ m-klasse-amg @@@ ### @@@ ### @@@ 120 @@@ R-klasse @@@ r-klasse @@@ r-klass @@@ r-class @@@ 120 @@@ R-klasse AMG @@@ r-klasse-amg @@@ ### @@@ ### @@@ 120 @@@ S-klasse @@@ s-klasse @@@ s-klass @@@ s-class @@@ 120 @@@ S-klasse AMG @@@ s-klasse-amg @@@ s-klass_amg @@@ ### @@@ 120 @@@ SLK-klasse @@@ slk-klasse @@@ slk-klass @@@ slk-class @@@ 120 @@@ SLK-klasse AMG @@@ slk-klasse-amg @@@ ### @@@ ### @@@ 120 @@@ SL-klasse @@@ sl-klasse @@@ sl-klass @@@ sl-class @@@ 120 @@@ SL-klasse AMG @@@ sl-klasse-amg @@@ ### @@@ ### @@@ 120 @@@ SLR McLaren @@@ slr-mclaren @@@ ### @@@ slr_mclaren @@@ 120 @@@ SLS AMG @@@ sls-amg @@@ ### @@@ sls_amg @@@ 120 @@@ Sprinter @@@ ### @@@ sprinter @@@ ### @@@ 120 @@@ Sprinter Classic @@@ ### @@@ sprinter_classic @@@ ### @@@ 120 @@@ Vaneo @@@ vaneo @@@ ### @@@ vaneo @@@ 120 @@@ Viano @@@ viano @@@ viano @@@ viano @@@ 120 @@@ Vito @@@ vito @@@ vito @@@ vito @@@ 120 @@@ V-klasse @@@ v-klasse @@@ v-klass @@@ v-class @@@ 120 @@@ W114 @@@ w114 @@@ ### @@@ ### @@@ 120 @@@ W115 @@@ w115 @@@ ### @@@ ### @@@ 120 @@@ W123 @@@ w123 @@@ w123 @@@ ### @@@ 120 @@@ W124 @@@ w124 @@@ w124 @@@ ### @@@ 120 @@@ Grand Marquis @@@ grand-marquis @@@ grand_marquis @@@ grand_marquis @@@ 121 @@@ Marauder @@@ marauder @@@ ### @@@ marauder @@@ 121 @@@ Mariner @@@ mariner @@@ mariner @@@ mariner @@@ 121 @@@ Marquis @@@ marquis @@@ ### @@@ marquis @@@ 121 @@@ Milan @@@ milan @@@ ### @@@ milan @@@ 121 @@@ Monterey @@@ monterey @@@ ### @@@ monterey @@@ 121 @@@ Mountaineer @@@ mountaineer @@@ ### @@@ mountaineer @@@ 121 @@@ Mystique @@@ mystique @@@ ### @@@ mystique @@@ 121 @@@ Sable @@@ sable @@@ sable @@@ sable @@@ 121 @@@ Topaz @@@ topaz @@@ ### @@@ topaz @@@ 121 @@@ Tracer @@@ tracer @@@ ### @@@ tracer @@@ 121 @@@ Villager @@@ villager @@@ villager @@@ villager @@@ 121 @@@ Metrocab I @@@ i @@@ ### @@@ ### @@@ 122 @@@ Metrocab II (TTT) @@@ ii @@@ ### @@@ ### @@@ 122 @@@ 3 @@@ 3 @@@ ### @@@ ### @@@ 123 @@@ 5 @@@ mg5 @@@ ### @@@ ### @@@ 123 @@@ 6 @@@ mg6 @@@ ### @@@ ### @@@ 123 @@@ 350 @@@ 350 @@@ ### @@@ ### @@@ 123 @@@ F @@@ f @@@ ### @@@ ### @@@ 123 @@@ MGB @@@ mgb @@@ ### @@@ ### @@@ 123 @@@ Midget @@@ midget @@@ ### @@@ ### @@@ 123 @@@ RV8 @@@ rv8 @@@ ### @@@ ### @@@ 123 @@@ TF @@@ tf @@@ ### @@@ ### @@@ 123 @@@ Xpower SV @@@ xpower-sv @@@ ### @@@ ### @@@ 123 @@@ ZR @@@ zr @@@ ### @@@ ### @@@ 123 @@@ ZS @@@ zs @@@ ### @@@ ### @@@ 123 @@@ ZT @@@ zt @@@ zt @@@ ### @@@ 123 @@@ F8C @@@ f8c @@@ ### @@@ ### @@@ 124 @@@ M.Go @@@ m-go @@@ ### @@@ ### @@@ 124 @@@ M8 @@@ m8 @@@ ### @@@ ### @@@ 124 @@@ MC @@@ mc @@@ ### @@@ ### @@@ 124 @@@ Virgo @@@ virgo @@@ ### @@@"," ### @@@ 124 @@@ TF 1800 @@@ tf-1800 @@@ ### @@@ ### @@@ 125 @@@ Cabrio @@@ cabrio @@@ ### @@@ cabrio @@@ 126 @@@ Clubman @@@ clubman @@@ cooper_clubman @@@ clubman @@@ 126 @@@ Clubvan @@@ clubvan @@@ ### @@@ ### @@@ 126 @@@ Cooper @@@ ### @@@ cooper @@@ ### @@@ 126 @@@ Cooper Paceman @@@ ### @@@ cooper_paceman @@@ ### @@@ 126 @@@ Cooper S @@@ ### @@@ cooper_s @@@ cooper_s @@@ 126 @@@ Cooper S Clubman @@@ ### @@@ cooper_s_clubman @@@ ### @@@ 126 @@@ Cooper S Countryman @@@ ### @@@ cooper_s_countryman @@@ ### @@@ 126 @@@ Countryman @@@ countryman @@@ cooper_countryman @@@ countryman @@@ 126 @@@ Hatch @@@ hatch @@@ ### @@@ hatch @@@ 126 @@@ John Cooper Works @@@ ### @@@ john_cooper_works @@@ ### @@@ 126 @@@ John Cooper Works Countryman @@@ ### @@@ john_cooper_works_countryman @@@ ### @@@ 126 @@@ John Cooper Works Paceman @@@ ### @@@ john_cooper_works_paceman @@@ ### @@@ 126 @@@ One @@@ ### @@@ one @@@ ### @@@ 126 @@@ One Clubman @@@ ### @@@ one_clubman @@@ ### @@@ 126 @@@ One Countryman @@@ ### @@@ one_countryman @@@ ### @@@ 126 @@@ Paceman @@@ paceman @@@ ### @@@ paceman @@@ 126 @@@ 3000 GT @@@ 3000gt @@@ 3000_gt @@@ 3000gt @@@ 127 @@@ Airtrek @@@ airtrek @@@ airtrek @@@ airtrek @@@ 127 @@@ ASX @@@ asx @@@ asx @@@ asx @@@ 127 @@@ Attrage @@@ attrage @@@ ### @@@ attrage @@@ 127 @@@ Carisma @@@ carisma @@@ carisma @@@ carisma @@@ 127 @@@ Celeste @@@ celeste @@@ ### @@@ ### @@@ 127 @@@ Chariot @@@ chariot @@@ chariot @@@ chariot @@@ 127 @@@ Colt @@@ colt @@@ colt @@@ colt @@@ 127 @@@ Cordia @@@ cordia @@@ ### @@@ cordia @@@ 127 @@@ Debonair @@@ debonair @@@ ### @@@ debonair @@@ 127 @@@ Delica @@@ delica @@@ delica @@@ delica @@@ 127 @@@ Diamante @@@ diamante @@@ diamante @@@ diamante @@@ 127 @@@ Dingo @@@ dingo @@@ ### @@@ dingo @@@ 127 @@@ Dion @@@ dion @@@ ### @@@ dion @@@ 127 @@@ Eclipse @@@ eclipse @@@ eclipse @@@ eclipse @@@ 127 @@@ eK @@@ ek @@@ ### @@@ ek-custom @@@ 127 @@@ Emeraude @@@ emeraude @@@ ### @@@ emeraude @@@ 127 @@@ Endeavor @@@ endeavor @@@ ### @@@ endeavor @@@ 127 @@@ Eterna @@@ eterna @@@ ### @@@ eterna @@@ 127 @@@ FTO @@@ fto @@@ ### @@@ fto @@@ 127 @@@ Fuso Canter @@@ ### @@@ fuso_canter @@@ ### @@@ 127 @@@ Galant @@@ galant @@@ galant @@@ galant @@@ 127 @@@ Grandis @@@ grandis @@@ grandis @@@ grandis @@@ 127 @@@ GTO @@@ gto @@@ ### @@@ gto @@@ 127 @@@ i-MiEV @@@ i-miev @@@ ### @@@ i-miev @@@ 127 @@@ L200 @@@ l200 @@@ l200 @@@ l200 @@@ 127 @@@ Lancer Cargo @@@ lancer-cargo @@@ ### @@@ ### @@@ 127 @@@ Lancer Evolution @@@ lancer_evolution @@@ lancer_evolution @@@ lancer_evolution @@@ 127 @@@ Lancer Ralliart @@@ lancer-ralliart @@@ ### @@@ ### @@@ 127 @@@ Legnum @@@ legnum @@@ legnum @@@ legnum @@@ 127 @@@ Libero @@@ libero @@@ libero @@@ libero @@@ 127 @@@ Minica @@@ minica @@@ ### @@@ minica @@@ 127 @@@ Minicab @@@ minicab @@@ ### @@@ minicab @@@ 127 @@@ Mirage @@@ mirage @@@ mirage @@@ mirage @@@ 127 @@@ Montero @@@ montero @@@ montero @@@ montero @@@ 127 @@@ Montero Sport @@@ montero_sport @@@ montero_sport @@@ montero_sport @@@ 127 @@@ Outlander @@@ outlander @@@ outlander @@@ outlander @@@ 127 @@@ Pajero @@@ pajero @@@ pajero @@@ pajero @@@ 127 @@@ Pajero iO @@@ pajero-io @@@ ### @@@ pajero_io @@@ 127 @@@ Pajero Junior @@@ pajero-junior @@@ ### @@@ pajero_junior @@@ 127 @@@ Pajero Mini @@@ pajero-mini @@@ pajero_mini @@@ pajero_mini @@@ 127 @@@ Pajero Pinin @@@ pajero_pinin @@@ pajero_pinin @@@ pajero_pinin @@@ 127 @@@ Pajero Sport @@@ pajero_sport @@@ pajero_sport @@@ pajero_sport @@@ 127 @@@ Pistachio @@@ pistachio @@@ ### @@@ pistachio @@@ 127 @@@ Proudia @@@ proudia @@@ ### @@@ proudia @@@ 127 @@@ Raider @@@ raider @@@ ### @@@ raider @@@ 127 @@@ RVR @@@ rvr @@@ rvr @@@ rvr @@@ 127 @@@ Sapporo @@@ sapporo @@@ ### @@@ sapporo @@@ 127 @@@ Space Gear @@@ space-gear @@@ ### @@@ space_gear @@@ 127 @@@ Space Runner @@@ space-runner @@@ space_runner @@@ space_runner @@@ 127 @@@ Space Star @@@ space-star @@@ space_star @@@ space_star @@@ 127 @@@ Space Wagon @@@ space-wagon @@@ space_wagon @@@ space_wagon @@@ 127 @@@ Starion @@@ starion @@@ ### @@@ starion @@@ 127 @@@ Toppo @@@ toppo @@@ ### @@@ toppo @@@ 127 @@@ Town Box @@@ town-box @@@ ### @@@ town_box @@@ 127 @@@ Tredia @@@ tredia @@@ ### @@@ ### @@@ 127 @@@ Galue @@@ galue @@@ ### @@@ galue @@@ 128 @@@ Galue 204 @@@ galue-204 @@@ ### @@@ galue204 @@@ 128 @@@ Himiko @@@ himiko @@@ ### @@@ himiko @@@ 128 @@@ Le-Seyde @@@ le-seyde @@@ ### @@@ le-seyde @@@ 128 @@@ Like @@@ like @@@ ### @@@ like @@@ 128 @@@ Nouera @@@ nouera @@@ ### @@@ nouera @@@ 128 @@@ Orochi @@@ orochi @@@ ### @@@ orochi @@@ 128 @@@ Ryoga @@@ ryoga @@@ ### @@@ ryoga @@@ 128 @@@ Viewt @@@ viewt @@@ ### @@@ viewt @@@ 128 @@@ Yuga @@@ yuga @@@ ### @@@ yuga @@@ 128 @@@ Zero 1 @@@ zero-1 @@@ ### @@@ zero_1 @@@ 128 @@@ 42098 @@@ 42098 @@@ ### @@@ ### @@@ 129 @@@ 3 Wheeler @@@ 3-wheller @@@ ### @@@ ### @@@ 129 @@@ 4 Seater @@@ 4-seater @@@ ### @@@ ### @@@ 129 @@@ Aero 8 @@@ aero8 @@@ ### @@@ ### @@@ 129 @@@ Aero Coupe @@@ aero-coupe @@@ ### @@@ ### @@@ 129 @@@ Aero SuperSports @@@ aero-supersports @@@ ### @@@ ### @@@ 129 @@@ AeroMax @@@ aeromax @@@ ### @@@ ### @@@ 129 @@@ Plus 4 @@@ plus-four @@@ ### @@@ ### @@@ 129 @@@ Plus 8 @@@ plus-eight @@@ ### @@@ ### @@@ 129 @@@ Marina @@@ marina @@@ ### @@@ ### @@@ 130 @@@ 100NX @@@ 100nx @@@ ### @@@ 100nx @@@ 131 @@@ 180SX @@@ 180sx @@@ ### @@@ 180sx @@@ 131 @@@ 200SX @@@ 200sx @@@ ### @@@ 200sx @@@ 131 @@@ 240SX @@@ 240sx @@@ ### @@@ 240sx @@@ 131 @@@ 300ZX @@@ 300zx @@@ ### @@@ 300zx @@@ 131 @@@ 350Z @@@ 350z @@@ ### @@@ 350z @@@ 131 @@@ 370Z @@@ 370z @@@ ### @@@ 370z @@@ 131 @@@ AD @@@ ad @@@ ### @@@ adwagon @@@ 131 @@@ Almera @@@ almera @@@ almera @@@ almera @@@ 131 @@@ Almera Classic @@@ almera-classic @@@ almera_classic @@@ almera_classic @@@ 131 @@@ Almera Tino @@@ almera-tino @@@ ### @@@ ### @@@ 131 @@@ Altima @@@ altima @@@ ### @@@ altima @@@ 131 @@@ Avenir @@@ avenir @@@ ### @@@ avenir @@@ 131 @@@ Bassara @@@ bassara @@@ ### @@@ bassara @@@ 131 @@@ BE-1 @@@ be-1 @@@ ### @@@ be-1 @@@ 131 @@@ Bluebird S","ylphy @@@ bluebird_sylphy @@@ ### @@@ bluebird_sylphy @@@ 131 @@@ Cedric @@@ cedric @@@ ### @@@ cedric @@@ 131 @@@ Cefiro @@@ cefiro @@@ cefiro @@@ cefiro @@@ 131 @@@ Cima @@@ cima @@@ ### @@@ cima @@@ 131 @@@ Clipper @@@ clipper @@@ ### @@@ clipper @@@ 131 @@@ Crew @@@ crew @@@ ### @@@ crew @@@ 131 @@@ Cube @@@ cube @@@ cube @@@ cube @@@ 131 @@@ Datsun @@@ datsun @@@ ### @@@ datsun @@@ 131 @@@ Dualis @@@ dualis @@@ ### @@@ dualis @@@ 131 @@@ Elgrand @@@ elgrand @@@ ### @@@ elgrand @@@ 131 @@@ Expert @@@ expert @@@ ### @@@ expert @@@ 131 @@@ Fairlady Z @@@ fairlady @@@ ### @@@ fairlady_z @@@ 131 @@@ Figaro @@@ figaro @@@ ### @@@ figaro @@@ 131 @@@ Fuga @@@ fuga @@@ ### @@@ fuga @@@ 131 @@@ Gloria @@@ gloria @@@ ### @@@ gloria @@@ 131 @@@ GT-R @@@ gt-r @@@ gt-r @@@ gt-r @@@ 131 @@@ Juke @@@ juke @@@ juke @@@ juke @@@ 131 @@@ Juke Nismo @@@ juke-nismo @@@ ### @@@ ### @@@ 131 @@@ Lafesta @@@ lafesta @@@ ### @@@ lafesta @@@ 131 @@@ Langley @@@ langley @@@ ### @@@ langley @@@ 131 @@@ Largo @@@ largo @@@ ### @@@ largo @@@ 131 @@@ Laurel @@@ laurel @@@ laurel @@@ laurel @@@ 131 @@@ Leaf @@@ leaf @@@ ### @@@ leaf @@@ 131 @@@ Lucino @@@ lucino @@@ ### @@@ lucino @@@ 131 @@@ March @@@ march @@@ march @@@ march @@@ 131 @@@ Maxima @@@ maxima @@@ maxima @@@ maxima @@@ 131 @@@ Micra @@@ micra @@@ micra @@@ micra @@@ 131 @@@ Mistral @@@ mistral @@@ ### @@@ mistral @@@ 131 @@@ Moco @@@ moco @@@ ### @@@ moco @@@ 131 @@@ Murano @@@ murano @@@ murano @@@ murano @@@ 131 @@@ Navara (Frontier) @@@ navara @@@ navara @@@ navara @@@ 131 @@@ Note @@@ note @@@ note @@@ note @@@ 131 @@@ NP 300 @@@ np300 @@@ ### @@@ np300 @@@ 131 @@@ NV200 @@@ nv200 @@@ ### @@@ nv200 @@@ 131 @@@ NV350 Caravan @@@ nv350-caravan @@@ ### @@@ nv350_caravan @@@ 131 @@@ NX Coupe @@@ nx-coupe @@@ ### @@@ nx-coupe @@@ 131 @@@ Otti (Dayz) @@@ otti @@@ ### @@@ otti @@@ 131 @@@ Pao @@@ pao @@@ ### @@@ pao @@@ 131 @@@ Pathfinder @@@ pathfinder @@@ pathfinder @@@ pathfinder @@@ 131 @@@ Patrol @@@ patrol @@@ patrol @@@ patrol @@@ 131 @@@ Pino @@@ pino @@@ ### @@@ pino @@@ 131 @@@ Pixo @@@ pixo @@@ ### @@@ pixo @@@ 131 @@@ Prairie @@@ prairie @@@ ### @@@ prairie @@@ 131 @@@ Presage @@@ presage @@@ ### @@@ presage @@@ 131 @@@ Presea @@@ presea @@@ ### @@@ presea @@@ 131 @@@ President @@@ president @@@ ### @@@ president @@@ 131 @@@ Primastar @@@ primastar @@@ ### @@@ ### @@@ 131 @@@ Primera @@@ primera @@@ primera @@@ primera @@@ 131 @@@ Pulsar @@@ pulsar @@@ ### @@@ pulsar @@@ 131 @@@ Qashqai @@@ qashqai @@@ qashqai @@@ qashqai @@@ 131 @@@ Qashqai+2 @@@ qashqai-plus-2 @@@ qashqai2 @@@ qashqai_2 @@@ 131 @@@ Quest @@@ quest @@@ ### @@@ quest @@@ 131 @@@ Rasheen @@@ rasheen @@@ ### @@@ rasheen @@@ 131 @@@ R'nessa @@@ rnessa @@@ ### @@@ r~nessa @@@ 131 @@@ Rogue @@@ rogue @@@ ### @@@ rogue @@@ 131 @@@ Roox @@@ roox @@@ ### @@@ roox @@@ 131 @@@ Sentra @@@ sentra @@@ sentra @@@ sentra @@@ 131 @@@ Serena @@@ serena @@@ serena @@@ serena @@@ 131 @@@ Silvia @@@ silvia @@@ ### @@@ silvia @@@ 131 @@@ Skyline @@@ skyline @@@ skyline @@@ skyline @@@ 131 @@@ Skyline Crossover @@@ skyline_crossover @@@ ### @@@ skyline_crossover @@@ 131 @@@ Stagea @@@ stagea @@@ ### @@@ stagea @@@ 131 @@@ Teana @@@ teana @@@ teana @@@ teana @@@ 131 @@@ Terrano @@@ terrano @@@ terrano @@@ terrano @@@ 131 @@@ Terrano Regulus @@@ terrano_regulus @@@ ### @@@ terrano_regulus @@@ 131 @@@ Tiida @@@ tiida @@@ tiida @@@ tiida @@@ 131 @@@ Tino @@@ tino @@@ ### @@@ tino @@@ 131 @@@ Titan @@@ titan @@@ ### @@@ titan @@@ 131 @@@ Vanette @@@ vanette @@@ vanette @@@ vanette @@@ 131 @@@ Wingroad @@@ wingroad @@@ wingroad @@@ wingroad @@@ 131 @@@ X-Terra @@@ x-terra @@@ ### @@@ xterra @@@ 131 @@@ X-Trail @@@ x-trail @@@ x-trail @@@ x-trail @@@ 131 @@@ M12 GTO @@@ m12-gto @@@ ### @@@ ### @@@ 132 @@@ M600 @@@ m600 @@@ ### @@@ ### @@@ 132 @@@ Achieva @@@ achieva @@@ achieva @@@ achieva @@@ 133 @@@ Bravada @@@ bravada @@@ ### @@@ bravada @@@ 133 @@@ Cutlass @@@ cutlass @@@ cutlass @@@ ### @@@ 133 @@@ Cutlass Calais @@@ cutlass-calais @@@ ### @@@ ### @@@ 133 @@@ Cutlass Ciera @@@ cutlass-ciera @@@ ### @@@ cutlass_siera @@@ 133 @@@ Cutlass Supreme @@@ cutlass-supreme @@@ ### @@@ cutlass_supreme @@@ 133 @@@ Eighty-Eight @@@ eighty-eight @@@ ### @@@ ### @@@ 133 @@@ Intrigue @@@ intrigue @@@ ### @@@ ### @@@ 133 @@@ Ninety-Eight @@@ ninety-eight @@@ ninety-eight @@@ ### @@@ 133 @@@ Silhouette @@@ silhouette @@@ ### @@@ silhouette @@@ 133 @@@ Adam @@@ adam @@@ ### @@@ adam @@@ 134 @@@ Admiral @@@ admiral @@@ admiral @@@ ### @@@ 134 @@@ Agila @@@ agila @@@ agila @@@ agila @@@ 134 @@@ Ampera @@@ ampera @@@ ### @@@ ### @@@ 134 @@@ Antara @@@ antara @@@ antara @@@ antara @@@ 134 @@@ Ascona @@@ ascona @@@ ascona @@@ ascona @@@ 134 @@@ Astra @@@ astra @@@ astra @@@ astra @@@ 134 @@@ Astra GTC @@@ ### @@@ astra_gtc @@@ astra_gtc @@@ 134 @@@ Astra OPC @@@ astra-opc @@@ astra_opc @@@ ### @@@ 134 @@@ Calibra @@@ calibra @@@ calibra @@@ calibra @@@ 134 @@@ Campo @@@ campo @@@ ### @@@ campo @@@ 134 @@@ Cascada @@@ cascada @@@ ### @@@ cascada @@@ 134 @@@ Combo @@@ combo @@@ combo @@@ combo @@@ 134 @@@ Corsa @@@ corsa @@@ corsa @@@ corsa @@@ 134 @@@ Corsa OPC @@@ corsa-opc @@@ corsa_opc @@@ ### @@@ 134 @@@ Diplomat @@@ diplomat @@@ ### @@@ diplomat @@@ 134 @@@ Frontera @@@ frontera @@@ frontera @@@ frontera @@@ 134 @@@ GT @@@ gt @@@ gt @@@ gt @@@ 134 @@@ Insignia @@@ insignia @@@ insignia @@@ insignia @@@ 134 @@@ Insignia OPC @@@ insignia-opc @@@ insignia_opc @@@ ### @@@ 134 @@@ Kadett @@@ kadett @@@ kadett @@@ kadett @@@ 134 @@@ Karl @@@ karl @@@ ### @@@ karl @@@ 134 @@@ Manta @@@ manta @@@ manta @@@ manta @@@ 134 @@@ Meriva @@@ meriva @@@ meriva @@@ meriva @@@ 134 @@@ Meriva OPC @@@ meriva-opc @@@ ### @@@ ### @@@ 134 @@@ Mokka @@@ mokka @@@ mokka @@@ mokka @@@ 134 @@@ Movano @@@ ### @@@ movano @@@ movano @@@ 134 @@@ Monterey @@@ monterey @@@ monterey @@@ monterey @@@ 134 @@@ Omega @@@ omega @@@ omega @@@ omega @@@ 134 @@@ Rekord @@@ rekord @@@ rekord @@@ rekord @@@ 134 @@@ Senator @@@ senator @@@ senator @@@ senator ","@@@ 134 @@@ Signum @@@ signum @@@ signum @@@ signum @@@ 134 @@@ Sintra @@@ sintra @@@ sintra @@@ sintra @@@ 134 @@@ Speedster @@@ speedster @@@ ### @@@ speedster @@@ 134 @@@ Tigra @@@ tigra @@@ tigra @@@ tigra @@@ 134 @@@ Vectra @@@ vectra @@@ vectra @@@ vectra @@@ 134 @@@ Vectra OPC @@@ vectra-opc @@@ ### @@@ ### @@@ 134 @@@ Vita @@@ vita @@@ vita @@@ vita @@@ 134 @@@ Vivaro @@@ vivaro @@@ vivaro @@@ vivaro @@@ 134 @@@ Zafira @@@ zafira @@@ zafira @@@ zafira @@@ 134 @@@ Zafira OPC @@@ zafira-opc @@@ ### @@@ ### @@@ 134 @@@ 2500 GT @@@ 2500-gt @@@ ### @@@ ### @@@ 135 @@@ Huayra @@@ huayra @@@ ### @@@ huayra @@@ 136 @@@ Zonda @@@ zonda @@@ ### @@@ zonda @@@ 136 @@@ Esperante @@@ esperante @@@ ### @@@ ### @@@ 137 @@@ Alza @@@ alza @@@ ### @@@ ### @@@ 138 @@@ Kancil @@@ kancil @@@ ### @@@ ### @@@ 138 @@@ Kelisa @@@ kelisa @@@ ### @@@ ### @@@ 138 @@@ Kembara @@@ kembara @@@ ### @@@ ### @@@ 138 @@@ Kenari @@@ kenari @@@ ### @@@ ### @@@ 138 @@@ MyVi @@@ myvi @@@ ### @@@ ### @@@ 138 @@@ Nautica @@@ nautica @@@ ### @@@ ### @@@ 138 @@@ 104 @@@ 104 @@@ ### @@@ ### @@@ 139 @@@ 106 @@@ 106 @@@ 106 @@@ 106 @@@ 139 @@@ 107 @@@ 107 @@@ 107 @@@ 107 @@@ 139 @@@ 108 @@@ 108 @@@ ### @@@ 108 @@@ 139 @@@ 204 @@@ 204 @@@ ### @@@ ### @@@ 139 @@@ 205 @@@ 205 @@@ 205 @@@ 205 @@@ 139 @@@ 206 @@@ 206 @@@ 206 @@@ 206 @@@ 139 @@@ 207 @@@ 207 @@@ 207 @@@ 207 @@@ 139 @@@ 208 @@@ 208 @@@ 208 @@@ 208 @@@ 139 @@@ 301 @@@ 301 @@@ 301 @@@ 301 @@@ 139 @@@ 304 @@@ 304 @@@ ### @@@ ### @@@ 139 @@@ 305 @@@ 305 @@@ ### @@@ ### @@@ 139 @@@ 306 @@@ 306 @@@ 306 @@@ 306 @@@ 139 @@@ 307 @@@ 307 @@@ 307 @@@ 307 @@@ 139 @@@ 308 @@@ 308 @@@ 308 @@@ 308 @@@ 139 @@@ 309 @@@ 309 @@@ 309 @@@ 309 @@@ 139 @@@ 405 @@@ 405 @@@ 405 @@@ 405 @@@ 139 @@@ 406 @@@ 406 @@@ 406 @@@ 406 @@@ 139 @@@ 407 @@@ 407 @@@ 407 @@@ 407 @@@ 139 @@@ 408 @@@ 408 @@@ 408 @@@ 408 @@@ 139 @@@ 504 @@@ 504 @@@ 504 @@@ 504 @@@ 139 @@@ 505 @@@ 505 @@@ 505 @@@ ### @@@ 139 @@@ 508 @@@ 508 @@@ 508 @@@ 508 @@@ 139 @@@ 604 @@@ 604 @@@ ### @@@ ### @@@ 139 @@@ 605 @@@ 605 @@@ 605 @@@ 605 @@@ 139 @@@ 607 @@@ 607 @@@ 607 @@@ 607 @@@ 139 @@@ 806 @@@ 806 @@@ 806 @@@ 806 @@@ 139 @@@ 807 @@@ 807 @@@ 807 @@@ 807 @@@ 139 @@@ 1007 @@@ 1007 @@@ 1007 @@@ ### @@@ 139 @@@ 2008 @@@ 2008 @@@ 2008 @@@ ### @@@ 139 @@@ 3008 @@@ 3008 @@@ 3008 @@@ ### @@@ 139 @@@ 4007 @@@ 4007 @@@ 4007 @@@ ### @@@ 139 @@@ 4008 @@@ 4008 @@@ 4008 @@@ ### @@@ 139 @@@ 5008 @@@ 5008 @@@ 5008 @@@ ### @@@ 139 @@@ 205 GTi @@@ 205-gti @@@ ### @@@ ### @@@ 139 @@@ 208 GTi @@@ 208-gti @@@ ### @@@ ### @@@ 139 @@@ 308 GTi @@@ 308-gti @@@ ### @@@ ### @@@ 139 @@@ Bipper @@@ bipper @@@ ### @@@ ### @@@ 139 @@@ Boxer @@@ ### @@@ boxer @@@ ### @@@ 139 @@@ iOn @@@ ion @@@ ### @@@ ### @@@ 139 @@@ RCZ @@@ rcz @@@ rcz @@@ rcz @@@ 139 @@@ Cevennes @@@ cevennes @@@ ### @@@ ### @@@ 140 @@@ Hemera @@@ hemera @@@ ### @@@ ### @@@ 140 @@@ Speedster II @@@ speedster_ii @@@ ### @@@ ### @@@ 140 @@@ Acclaim @@@ acclaim @@@ ### @@@ acclaim @@@ 142 @@@ Breeze @@@ breeze @@@ breeze @@@ breeze @@@ 142 @@@ Caravelle @@@ caravelle @@@ ### @@@ ### @@@ 142 @@@ Sundance @@@ sundance @@@ ### @@@ ### @@@ 142 @@@ Turismo @@@ turismo @@@ ### @@@ ### @@@ 142 @@@ 6000 @@@ 6000 @@@ ### @@@ 6000 @@@ 143 @@@ Aztec @@@ aztec @@@ ### @@@ aztek @@@ 143 @@@ Bonneville @@@ bonneville @@@ bonneville @@@ bonneville @@@ 143 @@@ Fiero @@@ fiero @@@ ### @@@ fiero @@@ 143 @@@ Firebird @@@ firebird @@@ firebird @@@ firebird @@@ 143 @@@ G4 @@@ g4 @@@ ### @@@ ### @@@ 143 @@@ G5 @@@ g5 @@@ ### @@@ ### @@@ 143 @@@ G8 @@@ g8 @@@ ### @@@ ### @@@ 143 @@@ Grand AM @@@ grand-am @@@ grand_am @@@ grand_am @@@ 143 @@@ Grand Prix @@@ grand-prix @@@ grand_prix @@@ grand_prix @@@ 143 @@@ LeMans @@@ lemans @@@ lemans @@@ lemans @@@ 143 @@@ Montana @@@ montana @@@ ### @@@ montana @@@ 143 @@@ Parisienne @@@ parisienne @@@ parisienne @@@ ### @@@ 143 @@@ Phoenix @@@ phoenix @@@ ### @@@ ### @@@ 143 @@@ Solstice @@@ solstice @@@ ### @@@ solstice @@@ 143 @@@ Sunbird @@@ sunbird @@@ ### @@@ ### @@@ 143 @@@ Sunfire @@@ sunfire @@@ sunfire @@@ sunfire @@@ 143 @@@ Tempest @@@ tempest @@@ tempest @@@ ### @@@ 143 @@@ Torrent @@@ torrent @@@ ### @@@ torrent @@@ 143 @@@ Vibe @@@ vibe @@@ vibe @@@ vibe @@@ 143 @@@ 356 @@@ ### @@@ 356 @@@ ### @@@ 144 @@@ 911 @@@ 911 @@@ ### @@@ 911 @@@ 144 @@@ 914 @@@ 914 @@@ ### @@@ ### @@@ 144 @@@ 924 @@@ 924 @@@ 924 @@@ 924 @@@ 144 @@@ 928 @@@ 928 @@@ 928 @@@ 928 @@@ 144 @@@ 944 @@@ 944 @@@ 944 @@@ 944 @@@ 144 @@@ 959 @@@ 959 @@@ ### @@@ ### @@@ 144 @@@ 968 @@@ 968 @@@ ### @@@ ### @@@ 144 @@@ 911 Carrera @@@ ### @@@ 911_carrera @@@ ### @@@ 144 @@@ 911 Carrera 4 @@@ ### @@@ 911_carrera_4 @@@ ### @@@ 144 @@@ 911 Carrera 4S @@@ ### @@@ 911_carrera_4s @@@ ### @@@ 144 @@@ 911 Carrera S @@@ ### @@@ 911_carrera_s @@@ ### @@@ 144 @@@ 911 GT2 @@@ 911-gt2 @@@ ### @@@ ### @@@ 144 @@@ 911 GT3 @@@ 911-gt3 @@@ 911_gt3 @@@ ### @@@ 144 @@@ 911 GT3 RS @@@ ### @@@ 911_gt3_rs @@@ ### @@@ 144 @@@ 911 Targa 4S @@@ ### @@@ 911_targa_4s @@@ ### @@@ 144 @@@ 911 Turbo S @@@ ### @@@ 911_turbo_s @@@ ### @@@ 144 @@@ 918 Spyder @@@ 918 @@@ ### @@@ ### @@@ 144 @@@ Boxster @@@ boxster @@@ boxster @@@ boxster @@@ 144 @@@ Boxster S @@@ ### @@@ boxster_s @@@ ### @@@ 144 @@@ Carrera GT @@@ carrera-gt @@@ ### @@@ carrera @@@ 144 @@@ Cayenne @@@ cayenne @@@ cayenne @@@ cayenne @@@ 144 @@@ Cayenne GTS @@@ ### @@@ cayenne_gts @@@ ### @@@ 144 @@@ Cayenne S @@@ ### @@@ cayenne_s @@@ ### @@@ 144 @@@ Cayenne Turbo @@@ ### @@@ cayenne_turbo @@@ ### @@@ 144 @@@ Cayenne Turbo S @@@ ### @@@ cayenne_turbo_s @@@ ### @@@ 144 @@@ Cayman @@@ cayman @@@ cayman @@@ cayman @@@ 144 @@@ Cayman GT4 @@@ cayman-gt4 @@@ ### @@@ ### @@@ 144 @@@ Cayman S @@@ ### @@@ cayman_s @@@ ### @@@ 144 @@@ Macan @@@ macan @@@ ### @@@ macan @@@ 144 @@@ Macan S @@@ ### @@@ macan_s @@@ ### @@@ 144 @@@ Macan Turbo @@@ ### @@@ macan_turbo @@@ ### @@@ 144 @@@ Panamera @@@ panamera @@@ panamera @@@ panamera @@@ 144 @@@ Panamera 4 @@@ ### @@@ panamera_4 @@@ ### @@@ 144 @@@ Panamera 4S @@@ ### @@@ panamera_4s @@@ ### @@@ 144 @@@ Panamera S @@@ ### @@@ pan","amera_s @@@ ### @@@ 144 @@@ Panamera Turbo @@@ ### @@@ panamera_turbo @@@ ### @@@ 144 @@@ 118NE @@@ 118ne @@@ ### @@@ ### @@@ 145 @@@ Padmini @@@ padmini @@@ ### @@@ ### @@@ 145 @@@ Exora @@@ exora @@@ ### @@@ ### @@@ 146 @@@ Gen-2 @@@ gen-2 @@@ ### @@@ gen2 @@@ 146 @@@ Inspira @@@ inspira @@@ ### @@@ iswara @@@ 146 @@@ Juara @@@ juara @@@ juara @@@ ### @@@ 146 @@@ Perdana @@@ perdana @@@ ### @@@ perdana @@@ 146 @@@ Preve @@@ preve @@@ ### @@@ ### @@@ 146 @@@ Saga @@@ saga @@@ ### @@@ saga @@@ 146 @@@ Satria @@@ satria @@@ ### @@@ satria_neo @@@ 146 @@@ Waja @@@ waja @@@ ### @@@ waja @@@ 146 @@@ Wira (400 Series) @@@ wira @@@ ### @@@ ### @@@ 146 @@@ G-modell @@@ g-modell @@@ ### @@@ ### @@@ 147 @@@ Pinzgauer @@@ pinzgauer @@@ ### @@@ ### @@@ 147 @@@ GTB @@@ gtb @@@ ### @@@ ### @@@ 148 @@@ GTE @@@ gte @@@ ### @@@ ### @@@ 148 @@@ Scimitar Sabre @@@ scimitar-sabre @@@ ### @@@ ### @@@ 151 @@@ Tropica Roadster @@@ tropica @@@ ### @@@ ### @@@ 152 @@@ 4 @@@ 4 @@@ ### @@@ ### @@@ 153 @@@ 9 @@@ 9 @@@ 9 @@@ ### @@@ 153 @@@ 11 @@@ 11 @@@ 11 @@@ 11 @@@ 153 @@@ 12 @@@ 12 @@@ 12 @@@ ### @@@ 153 @@@ 14 @@@ 14 @@@ ### @@@ ### @@@ 153 @@@ 15 @@@ 15 @@@ ### @@@ ### @@@ 153 @@@ 16 @@@ 16 @@@ ### @@@ ### @@@ 153 @@@ 17 @@@ 17 @@@ ### @@@ ### @@@ 153 @@@ 18 @@@ 18 @@@ ### @@@ ### @@@ 153 @@@ 19 @@@ 19 @@@ 19 @@@ 19 @@@ 153 @@@ 20 @@@ 20 @@@ 20 @@@ ### @@@ 153 @@@ 21 @@@ 21 @@@ 21 @@@ 21 @@@ 153 @@@ 25 @@@ 25 @@@ 25 @@@ 25 @@@ 153 @@@ 30 @@@ 30 @@@ ### @@@ ### @@@ 153 @@@ Avantime @@@ avantime @@@ ### @@@ avantime @@@ 153 @@@ Captur @@@ captur @@@ ### @@@ captur @@@ 153 @@@ Clio @@@ clio @@@ clio @@@ clio @@@ 153 @@@ Clio RS @@@ clio-rs @@@ clio_rs @@@ ### @@@ 153 @@@ Clio V6 @@@ clio-v6 @@@ ### @@@ ### @@@ 153 @@@ Espace @@@ espace @@@ espace @@@ espace @@@ 153 @@@ Estafette @@@ estafette @@@ ### @@@ ### @@@ 153 @@@ Fluence @@@ fluence @@@ fluence @@@ fluence @@@ 153 @@@ Fuego @@@ fuego @@@ ### @@@ ### @@@ 153 @@@ Grand Scenic @@@ ### @@@ grand_scenic @@@ grand_scenic @@@ 153 @@@ Kadjar @@@ kadjar @@@ ### @@@ kadjar @@@ 153 @@@ Kangoo @@@ kangoo @@@ kangoo @@@ kangoo @@@ 153 @@@ Koleos @@@ koleos @@@ koleos @@@ koleos @@@ 153 @@@ Laguna @@@ laguna @@@ laguna @@@ laguna @@@ 153 @@@ Latitude @@@ latitude @@@ latitude @@@ latitude @@@ 153 @@@ Logan @@@ logan @@@ logan @@@ logan @@@ 153 @@@ Master @@@ ### @@@ master @@@ ### @@@ 153 @@@ Megane @@@ megane @@@ megane @@@ megane @@@ 153 @@@ Megane RS @@@ megane-rs @@@ megane_rs @@@ ### @@@ 153 @@@ Modus @@@ modus @@@ modus @@@ modus @@@ 153 @@@ Safrane @@@ safrane @@@ safrane @@@ safrane @@@ 153 @@@ Sandero Stepway @@@ ### @@@ sandero_stepway @@@ sandero_stepway @@@ 153 @@@ Scenic @@@ scenic @@@ scenic @@@ scenic @@@ 153 @@@ Sport Spider @@@ sport-spider @@@ ### @@@ sport_spider @@@ 153 @@@ Symbol @@@ symbol @@@ symbol @@@ symbol @@@ 153 @@@ Talisman @@@ talisman @@@ ### @@@ talisman @@@ 153 @@@ Trafic @@@ trafic-bus @@@ trafic @@@ trafic @@@ 153 @@@ Twingo @@@ twingo @@@ twingo @@@ twingo @@@ 153 @@@ Twizy @@@ twizy @@@ twizy @@@ ### @@@ 153 @@@ Vel Satis @@@ vel-satis @@@ vel_satis @@@ vel_satis @@@ 153 @@@ Wind @@@ wind @@@ ### @@@ wind @@@ 153 @@@ ZOE @@@ zoe @@@ ### @@@ ### @@@ 153 @@@ SM3 @@@ sm3 @@@ ### @@@ sm3 @@@ 154 @@@ SM5 @@@ sm5 @@@ ### @@@ sm5 @@@ 154 @@@ SM7 @@@ sm7 @@@ ### @@@ sm7 @@@ 154 @@@ Beast @@@ beast @@@ ### @@@ ### @@@ 155 @@@ Concept_One @@@ concept-one @@@ ### @@@ ### @@@ 156 @@@ Corniche @@@ corniche @@@ corniche_cabrio @@@ corniche @@@ 157 @@@ Ghost @@@ ghost @@@ ghost @@@ ghost @@@ 157 @@@ Park Ward @@@ park-ward @@@ park_ward @@@ park_ward @@@ 157 @@@ Phantom @@@ phantom @@@ phantom @@@ phantom @@@ 157 @@@ Silver Seraph @@@ silver-seraph @@@ silver_seraph @@@ silver_seraph @@@ 157 @@@ Silver Spur @@@ silver-spur @@@ silver_spur @@@ silver_spur @@@ 157 @@@ Wraith @@@ wraith @@@ wraith @@@ wraith @@@ 157 @@@ Lightning @@@ lightning @@@ ### @@@ ### @@@ 158 @@@ 45 @@@ 45 @@@ 45 @@@ 45 @@@ 159 @@@ 75 @@@ 75 @@@ 75 @@@ 75 @@@ 159 @@@ 100 @@@ 100 @@@ ### @@@ ### @@@ 159 @@@ 200 @@@ 200 @@@ 200 @@@ 200 @@@ 159 @@@ Mini MK @@@ ### @@@ mini_mk @@@ mini @@@ 159 @@@ P6 @@@ p6 @@@ ### @@@ ### @@@ 159 @@@ SD1 @@@ sd1 @@@ ### @@@ ### @@@ 159 @@@ Streetwise @@@ streetwise @@@ streetwise @@@ streetwise @@@ 159 @@@ 90 @@@ 90 @@@ ### @@@ ### @@@ 160 @@@ 95 @@@ 95 @@@ 95 @@@ ### @@@ 160 @@@ 96 @@@ 96 @@@ 96 @@@ ### @@@ 160 @@@ 99 @@@ 99 @@@ ### @@@ 99 @@@ 160 @@@ 900 @@@ 900 @@@ 900 @@@ 900 @@@ 160 @@@ 9000 @@@ 9000 @@@ 9000 @@@ 9000 @@@ 160 @@@ 42072 @@@ 42072 @@@ 42072 @@@ 42072 @@@ 160 @@@ 42133 @@@ 42133 @@@ 42133 @@@ 42133 @@@ 160 @@@ 9-2X @@@ 9-2x @@@ 9-2x @@@ ### @@@ 160 @@@ 9-4X @@@ 9-4x @@@ ### @@@ ### @@@ 160 @@@ 9-7X @@@ 9-7x @@@ 9-7x @@@ ### @@@ 160 @@@ PS-10 @@@ ps-10 @@@ ### @@@ ### @@@ 162 @@@ Aura @@@ aura @@@ ### @@@ ### @@@ 163 @@@ LW @@@ lw @@@ ### @@@ ### @@@ 163 @@@ Outlook @@@ outlook @@@ ### @@@ ### @@@ 163 @@@ Relay @@@ relay @@@ ### @@@ relay @@@ 163 @@@ Sky @@@ sky @@@ ### @@@ sky @@@ 163 @@@ SL @@@ sl @@@ sl @@@ sl1 @@@ 163 @@@ SW @@@ sw @@@ ### @@@ ### @@@ 163 @@@ VUE @@@ vue @@@ vue @@@ vue @@@ 163 @@@ FR-S @@@ fr-s @@@ ### @@@ fr-s @@@ 164 @@@ iM @@@ im @@@ ### @@@ im @@@ 164 @@@ iQ @@@ iq @@@ ### @@@ iq @@@ 164 @@@ tC @@@ tc @@@ tc @@@ tc @@@ 164 @@@ xA @@@ xa @@@ xa @@@ xa @@@ 164 @@@ xB @@@ xb @@@ xb @@@ xb @@@ 164 @@@ xD @@@ xd @@@ ### @@@ xd @@@ 164 @@@ 133 @@@ 133 @@@ ### @@@ ### @@@ 165 @@@ Alhambra @@@ alhambra @@@ alhambra @@@ alhambra @@@ 165 @@@ Altea @@@ altea @@@ altea @@@ altea @@@ 165 @@@ Altea Freetrack @@@ ### @@@ altea_freetrack @@@ ### @@@ 165 @@@ Altea XL @@@ ### @@@ altea_xl @@@ ### @@@ 165 @@@ Arosa @@@ arosa @@@ arosa @@@ arosa @@@ 165 @@@ Cordoba @@@ cordoba @@@ cordoba @@@ cordoba @@@ 165 @@@ Exeo @@@ exeo @@@ ### @@@ exeo @@@ 165 @@@ Fura @@@ fura @@@ ### @@@ ### @@@ 165 @@@ Ibiza @@@ ibiza @@@ ibiza @@@ ibiza @@@ 165 @@@ Ibiza Cupra @@@ ibiza-cupra @@@ ### @@@ ### @@@ 165 @@@ Leon @@@ leon @@@ leon @@@ leon @@@ 165 @@@ Leon Cupra @@@ leon-cupra @@@ ### @@@ ### @@@ 165 @@@ Leon FR @@@ ### @@@ leon","_fr @@@ ### @@@ 165 @@@ Malaga @@@ malaga @@@ ### @@@ ### @@@ 165 @@@ Marbella @@@ marbella @@@ ### @@@ marbella @@@ 165 @@@ Mii @@@ mii @@@ ### @@@ mii @@@ 165 @@@ Ronda @@@ ronda @@@ ### @@@ ronda @@@ 165 @@@ Toledo @@@ toledo @@@ toledo @@@ toledo @@@ 165 @@@ Noble @@@ noble @@@ ### @@@ ### @@@ 166 @@@ Sceo @@@ sceo @@@ ### @@@ ### @@@ 166 @@@ 100 Series @@@ 100-series @@@ ### @@@ ### @@@ 167 @@@ Citigo @@@ citigo @@@ ### @@@ citigo @@@ 167 @@@ Fabia @@@ fabia @@@ fabia @@@ fabia @@@ 167 @@@ Fabia RS @@@ fabia-rs @@@ fabia_rs @@@ ### @@@ 167 @@@ Favorit @@@ favorit @@@ ### @@@ favorit @@@ 167 @@@ Felicia @@@ felicia @@@ felicia @@@ felicia @@@ 167 @@@ Octavia @@@ octavia @@@ octavia @@@ octavia @@@ 167 @@@ Octavia RS @@@ octavia-rs @@@ octavia_rs @@@ ### @@@ 167 @@@ Octavia Scout @@@ ### @@@ octavia_scout @@@ ### @@@ 167 @@@ Praktik @@@ ### @@@ praktik @@@ praktik @@@ 167 @@@ Rapid @@@ rapid @@@ rapid @@@ rapid @@@ 167 @@@ Roomster @@@ roomster @@@ roomster @@@ roomster @@@ 167 @@@ Roomster Scout @@@ ### @@@ roomster_scout @@@ ### @@@ 167 @@@ Superb @@@ superb @@@ superb @@@ superb @@@ 167 @@@ Yeti @@@ yeti @@@ yeti @@@ yeti @@@ 167 @@@ Crossblade @@@ ### @@@ crossblade @@@ rossblade @@@ 168 @@@ Forfour @@@ forfour @@@ forfour @@@ forfour @@@ 168 @@@ Fortwo @@@ fortwo @@@ fortwo @@@ fortwo @@@ 168 @@@ Lioncel @@@ lioncel @@@ ### @@@ ### @@@ 169 @@@ R42 @@@ r42 @@@ ### @@@ ### @@@ 170 @@@ Actyon @@@ actyon @@@ actyon @@@ actyon @@@ 172 @@@ Actyon Sports @@@ actyon_sports @@@ ### @@@ actyon_sports @@@ 172 @@@ Istana @@@ ### @@@ istana @@@ istana @@@ 172 @@@ Kallista @@@ kallista @@@ ### @@@ ### @@@ 172 @@@ Korando Family @@@ korando-family @@@ ### @@@ korando_family @@@ 172 @@@ Korando Sports @@@ korando_sports @@@ ### @@@ korando_sports @@@ 172 @@@ Kyron @@@ kyron @@@ kyron @@@ kyron @@@ 172 @@@ Nomad @@@ nomad @@@ ### @@@ ### @@@ 172 @@@ Rexton @@@ rexton @@@ rexton @@@ rexton @@@ 172 @@@ Rodius @@@ rodius @@@ rodius @@@ rodius @@@ 172 @@@ Stavic @@@ stavic @@@ stavic @@@ stavic @@@ 172 @@@ Tivoli @@@ tivoli @@@ ### @@@ tivoli @@@ 172 @@@ Alcyone @@@ alcyone @@@ ### @@@ alcyone @@@ 173 @@@ Baja @@@ baja @@@ ### @@@ baja @@@ 173 @@@ BRZ @@@ brz @@@ brz @@@ brz @@@ 173 @@@ Dex @@@ dex @@@ ### @@@ dex @@@ 173 @@@ Domingo @@@ domingo @@@ domingo @@@ domingo @@@ 173 @@@ Exiga @@@ exiga @@@ ### @@@ exiga @@@ 173 @@@ Forester @@@ forester @@@ forester @@@ forester @@@ 173 @@@ Impreza @@@ impreza @@@ impreza @@@ impreza @@@ 173 @@@ Impreza WRX @@@ impreza-wrx @@@ ### @@@ impreza_wrx @@@ 173 @@@ Impreza WRX STi @@@ impreza-wrx-sti @@@ ### @@@ impreza_wrx_sti @@@ 173 @@@ Justy @@@ justy @@@ justy @@@ justy @@@ 173 @@@ Legacy @@@ legacy @@@ legacy @@@ legacy @@@ 173 @@@ Leone @@@ leone @@@ leone @@@ leone @@@ 173 @@@ Levorg @@@ levorg @@@ ### @@@ levorg @@@ 173 @@@ Lucra @@@ lucra @@@ ### @@@ lucra @@@ 173 @@@ Outback @@@ outback @@@ outback @@@ outback @@@ 173 @@@ Pleo @@@ pleo @@@ pleo @@@ pleo @@@ 173 @@@ R1 @@@ r1 @@@ ### @@@ r1 @@@ 173 @@@ R2 @@@ r2 @@@ r2 @@@ r2 @@@ 173 @@@ Sambar @@@ sambar @@@ sambar @@@ sambar @@@ 173 @@@ Stella @@@ stella @@@ stella @@@ stella @@@ 173 @@@ SVX @@@ svx @@@ svx @@@ svx @@@ 173 @@@ Traviq @@@ traviq @@@ ### @@@ traviq @@@ 173 @@@ Trezia @@@ trezia @@@ ### @@@ trezia @@@ 173 @@@ Tribeca @@@ tribeca @@@ tribeca @@@ tribeca @@@ 173 @@@ Vivio @@@ vivio @@@ vivio @@@ vivio @@@ 173 @@@ WRX @@@ wrx @@@ wrx @@@ ### @@@ 173 @@@ WRX STi @@@ wrx-sti @@@ wrx_sti @@@ ### @@@ 173 @@@ XT @@@ xt @@@ ### @@@ xt @@@ 173 @@@ XV @@@ xv @@@ xv @@@ xv @@@ 173 @@@ Aerio @@@ aerio @@@ aerio @@@ aerio @@@ 174 @@@ Cappuccino @@@ cappuccino @@@ cappuccino @@@ cappuccino @@@ 174 @@@ Celerio @@@ celerio @@@ ### @@@ celerio @@@ 174 @@@ Cervo @@@ cervo @@@ ### @@@ cervo @@@ 174 @@@ Ertiga @@@ ertiga @@@ ### @@@ ### @@@ 174 @@@ Escudo @@@ escudo @@@ escudo @@@ escudo @@@ 174 @@@ Every @@@ every @@@ every @@@ every @@@ 174 @@@ Every Landy @@@ ### @@@ every_landy @@@ ### @@@ 174 @@@ Forenza @@@ forenza @@@ forenza @@@ forenza @@@ 174 @@@ Grand Vitara @@@ grand-vitara @@@ grand_vitara @@@ grand_vitara @@@ 174 @@@ Ignis @@@ ignis @@@ ignis @@@ ignis @@@ 174 @@@ Jimny @@@ jimny @@@ jimny @@@ jimny @@@ 174 @@@ Kei @@@ kei @@@ ### @@@ kei @@@ 174 @@@ Kizashi @@@ kizashi @@@ kizashi @@@ kizashi @@@ 174 @@@ Landy @@@ landy @@@ ### @@@ landy @@@ 174 @@@ Liana @@@ liana @@@ liana @@@ liana @@@ 174 @@@ MR Wagon @@@ mr-wagon @@@ mr_wagon @@@ mr_wagon @@@ 174 @@@ Palette @@@ palette @@@ ### @@@ palette @@@ 174 @@@ Reno @@@ reno @@@ ### @@@ ### @@@ 174 @@@ Samurai @@@ samurai @@@ samurai @@@ samurai @@@ 174 @@@ Sidekick @@@ ### @@@ sidekick @@@ sidekick @@@ 174 @@@ Solio @@@ solio @@@ ### @@@ solio @@@ 174 @@@ Spacia @@@ spacia @@@ ### @@@ spacia @@@ 174 @@@ Splash @@@ splash @@@ splash @@@ splash @@@ 174 @@@ Swift @@@ swift @@@ swift @@@ swift @@@ 174 @@@ SX4 @@@ sx4 @@@ sx4 @@@ sx4 @@@ 174 @@@ Verona @@@ verona @@@ ### @@@ verona @@@ 174 @@@ Vitara @@@ vitara @@@ vitara @@@ vitara @@@ 174 @@@ Wagon R+ @@@ wagon-rplus @@@ ### @@@ wagon_r_plus @@@ 174 @@@ X-90 @@@ x-90 @@@ ### @@@ x-90 @@@ 174 @@@ XL7 @@@ xl7 @@@ xl7 @@@ xl7 @@@ 174 @@@ 1510 @@@ 1510 @@@ ### @@@ ### @@@ 175 @@@ Horizon @@@ horizon @@@ ### @@@ ### @@@ 175 @@@ Samba @@@ samba @@@ ### @@@ ### @@@ 175 @@@ Solara @@@ solara @@@ ### @@@ ### @@@ 175 @@@ Tagora @@@ tagora @@@ ### @@@ ### @@@ 175 @@@ Aria @@@ aria @@@ ### @@@ ### @@@ 176 @@@ Estate @@@ estate @@@ ### @@@ ### @@@ 176 @@@ Indica @@@ indica @@@ ### @@@ ### @@@ 176 @@@ Indigo @@@ indigo @@@ ### @@@ ### @@@ 176 @@@ Nano @@@ nano @@@ ### @@@ ### @@@ 176 @@@ Sumo @@@ sumo @@@ ### @@@ ### @@@ 176 @@@ T613 @@@ 613 @@@ ### @@@ ### @@@ 177 @@@ T700 @@@ 700 @@@ ### @@@ ### @@@ 177 @@@ Zero @@@ zero @@@ ### @@@ ### @@@ 178 @@@ Model S @@@ model-s @@@ model_s @@@ model_s @@@ 179 @@@ Dogan @@@ dogan @@@ ### @@@ ### @@@ 182 @@@ Kartal @@@ kartal @@@ ### @@@ ### @@@ 182 @@@ Murat 131 @@@ 131 @@@ ### @@@ ### @@@ 182 @@@ Serce @@@ serce @@@ ### @@@ ### @@@ 182 @@@ ƌahin @@@ sahin @@@ ### @@@ ","### @@@ 182 @@@ 4Runner @@@ 4runner @@@ 4runner @@@ 4runner @@@ 183 @@@ Allex @@@ allex @@@ ### @@@ allex @@@ 183 @@@ Allion @@@ allion @@@ ### @@@ allion @@@ 183 @@@ Alphard @@@ alphard @@@ ### @@@ alphard @@@ 183 @@@ Altezza @@@ altezza @@@ ### @@@ altezza @@@ 183 @@@ Aristo @@@ aristo @@@ ### @@@ aristo @@@ 183 @@@ Aurion @@@ aurion @@@ ### @@@ aurion @@@ 183 @@@ Auris @@@ auris @@@ auris @@@ auris @@@ 183 @@@ Avalon @@@ avalon @@@ ### @@@ avalon @@@ 183 @@@ Avensis @@@ avensis @@@ avensis @@@ avensis @@@ 183 @@@ Avensis Verso @@@ avensis-verso @@@ ### @@@ avensis_verso @@@ 183 @@@ Aygo @@@ aygo @@@ ### @@@ aygo @@@ 183 @@@ bB @@@ bb @@@ bb @@@ bb @@@ 183 @@@ Belta @@@ belta @@@ ### @@@ belta @@@ 183 @@@ Blade @@@ blade @@@ ### @@@ blade @@@ 183 @@@ Blizzard @@@ blizzard @@@ ### @@@ blizzard @@@ 183 @@@ Brevis @@@ brevis @@@ ### @@@ brevis @@@ 183 @@@ Caldina @@@ caldina @@@ caldina @@@ caldina @@@ 183 @@@ Cami @@@ cami @@@ ### @@@ cami @@@ 183 @@@ Camry @@@ camry @@@ camry @@@ camry @@@ 183 @@@ Camry (Japan) @@@ camry-jp @@@ ### @@@ ### @@@ 183 @@@ Camry Solara @@@ camry-solara @@@ ### @@@ ### @@@ 183 @@@ Carina @@@ carina @@@ carina @@@ carina @@@ 183 @@@ Carina ED @@@ carina-ed @@@ ### @@@ carina_ed @@@ 183 @@@ Celica @@@ celica @@@ celica @@@ celica @@@ 183 @@@ Celsior @@@ celsior @@@ ### @@@ celsior @@@ 183 @@@ Chaser @@@ chaser @@@ chaser @@@ chaser @@@ 183 @@@ Corolla @@@ corolla @@@ corolla @@@ corolla @@@ 183 @@@ Corolla Rumion @@@ corolla-rumion @@@ ### @@@ corolla_rumion @@@ 183 @@@ Corolla Spacio @@@ corolla-spacio @@@ ### @@@ corolla_spacio @@@ 183 @@@ Corolla Verso @@@ corolla-verso @@@ ### @@@ corolla_verso @@@ 183 @@@ Corona @@@ corona @@@ corona @@@ corona @@@ 183 @@@ Cressida @@@ cressida @@@ ### @@@ cressida @@@ 183 @@@ Cresta @@@ cresta @@@ ### @@@ cresta @@@ 183 @@@ Crown @@@ crown @@@ crown @@@ crown @@@ 183 @@@ Crown Majesta @@@ crown_majesta @@@ ### @@@ crown_majesta @@@ 183 @@@ Curren @@@ curren @@@ ### @@@ curren @@@ 183 @@@ Cynos @@@ cynos @@@ ### @@@ cynos @@@ 183 @@@ Duet @@@ duet @@@ ### @@@ duet @@@ 183 @@@ Echo @@@ echo @@@ ### @@@ echo @@@ 183 @@@ Estima @@@ estima @@@ ### @@@ estima @@@ 183 @@@ FJ Cruiser @@@ fj-cruiser @@@ ### @@@ fj_cruiser @@@ 183 @@@ Fortuner @@@ fortuner @@@ ### @@@ fortuner @@@ 183 @@@ FunCargo @@@ funcargo @@@ ### @@@ funcargo @@@ 183 @@@ Gaia @@@ gaia @@@ ### @@@ gaia @@@ 183 @@@ Granvia @@@ granvia @@@ ### @@@ granvia @@@ 183 @@@ GT86 @@@ gt-86 @@@ ### @@@ gt_86 @@@ 183 @@@ Harrier @@@ harrier @@@ ### @@@ harrier @@@ 183 @@@ HiAce @@@ hiace @@@ hiace @@@ hiace @@@ 183 @@@ Highlander @@@ highlander @@@ highlander @@@ highlander @@@ 183 @@@ Hilux @@@ hilux @@@ hilux @@@ hilux_pick_up @@@ 183 @@@ Hilux Surf @@@ hilux-surf @@@ ### @@@ hilux_surf @@@ 183 @@@ Innova @@@ innova @@@ ### @@@ innova @@@ 183 @@@ Ipsum @@@ ipsum @@@ ### @@@ ipsum @@@ 183 @@@ ISis @@@ isis @@@ ### @@@ isis @@@ 183 @@@ Ist @@@ ist @@@ ### @@@ ist @@@ 183 @@@ Kluger @@@ kluger @@@ ### @@@ kluger_v @@@ 183 @@@ Land Cruiser @@@ landcruiser @@@ land_cruiser @@@ land_cruiser @@@ 183 @@@ Land Cruiser Prado @@@ land-cruiser-prado @@@ land_cruiser_prado @@@ land_cruiser_prado @@@ 183 @@@ LiteAce @@@ liteace @@@ ### @@@ lite_ace @@@ 183 @@@ Mark II @@@ mark-ii @@@ mark_ii @@@ mark_ii @@@ 183 @@@ Mark X @@@ mark-x @@@ ### @@@ mark_x @@@ 183 @@@ Mark X ZiO @@@ mark-x-zio @@@ ### @@@ mark_x_zio @@@ 183 @@@ MasterAce Surf @@@ masterace @@@ ### @@@ master_ace_surf @@@ 183 @@@ Mega Cruiser @@@ megacruiser @@@ ### @@@ mega_cruiser @@@ 183 @@@ MR2 @@@ mr2 @@@ ### @@@ mr2 @@@ 183 @@@ Nadia @@@ nadia @@@ ### @@@ nadia @@@ 183 @@@ Noah @@@ noah @@@ noah @@@ noah @@@ 183 @@@ Opa @@@ opa @@@ ### @@@ opa @@@ 183 @@@ Origin @@@ origin @@@ ### @@@ origin @@@ 183 @@@ Paseo @@@ paseo @@@ ### @@@ paseo @@@ 183 @@@ Passo @@@ passo @@@ ### @@@ passo @@@ 183 @@@ Passo Sette @@@ passo-sette @@@ ### @@@ passo_sette @@@ 183 @@@ Picnic @@@ picnic @@@ ### @@@ picnic @@@ 183 @@@ Platz @@@ platz @@@ ### @@@ platz @@@ 183 @@@ Porte @@@ porte @@@ ### @@@ porte @@@ 183 @@@ Premio @@@ premio @@@ ### @@@ premio @@@ 183 @@@ Previa @@@ previa @@@ ### @@@ previa @@@ 183 @@@ Prius @@@ prius @@@ prius @@@ prius @@@ 183 @@@ Prius c @@@ prius-c @@@ ### @@@ prius_c @@@ 183 @@@ Prius v (+) @@@ priusv @@@ ### @@@ prius_v @@@ 183 @@@ ProAce @@@ proace @@@ ### @@@ ### @@@ 183 @@@ Probox @@@ probox @@@ ### @@@ probox @@@ 183 @@@ Progres @@@ progres @@@ ### @@@ progres @@@ 183 @@@ Pronard @@@ pronard @@@ ### @@@ pronard @@@ 183 @@@ Ractis @@@ ractis @@@ ### @@@ ractis @@@ 183 @@@ Raum @@@ raum @@@ ### @@@ raum @@@ 183 @@@ RAV 4 @@@ rav4 @@@ rav4 @@@ rav4 @@@ 183 @@@ Regius @@@ regius @@@ ### @@@ regius @@@ 183 @@@ RegiusAce @@@ regiusace @@@ ### @@@ regius_ace @@@ 183 @@@ Rush @@@ rush @@@ ### @@@ rush @@@ 183 @@@ Sai @@@ sai @@@ ### @@@ sai @@@ 183 @@@ Scepter @@@ scepter @@@ ### @@@ scepter @@@ 183 @@@ Sequoia @@@ sequoia @@@ sequoia @@@ sequoia @@@ 183 @@@ Sera @@@ sera @@@ ### @@@ sera @@@ 183 @@@ Sienna @@@ sienna @@@ sienna @@@ sienna @@@ 183 @@@ Sienta @@@ sienta @@@ ### @@@ sienta @@@ 183 @@@ Soarer @@@ soarer @@@ ### @@@ soarer @@@ 183 @@@ Soluna @@@ soluna @@@ ### @@@ soluna @@@ 183 @@@ Sparky @@@ sparky @@@ ### @@@ sparky @@@ 183 @@@ Sprinter Carib @@@ sprinter-carib @@@ ### @@@ sprinter_carib @@@ 183 @@@ Sprinter Marino @@@ sprinter-marino @@@ ### @@@ sprinter_marino @@@ 183 @@@ Sprinter Trueno @@@ sprinter-trueno @@@ ### @@@ sprinter_trueno @@@ 183 @@@ Starlet @@@ starlet @@@ ### @@@ starlet @@@ 183 @@@ Succeed @@@ succeed @@@ ### @@@ succeed @@@ 183 @@@ Supra @@@ supra @@@ ### @@@ supra @@@ 183 @@@ Tacoma @@@ tacoma @@@ ### @@@ tacoma @@@ 183 @@@ Tercel @@@ tercel @@@ ### @@@ tercel @@@ 183 @@@ TownAce @@@ townace @@@ town_ace @@@ town_ace @@@ 183 @@@ Tundra @@@ tundra @@@ tundra @@@ tundra @@@ 183 @@@ Urban Cruiser @@@ urban-cruiser @@@ ### @@@ urban_cruiser @@@ 183 @@@ Vanguard @@@ vanguard @@@ ### @@@ vanguard @@@ 183 @@@ Vellfire @@@ vellfire @@@ ### @@@ vellfire @@@ 183 @@@ Venza @@@ venza ","@@@ venza @@@ venza @@@ 183 @@@ Verossa @@@ verossa @@@ ### @@@ verossa @@@ 183 @@@ Verso @@@ verso @@@ verso @@@ verso @@@ 183 @@@ Verso-S @@@ verso-s @@@ ### @@@ verso-s @@@ 183 @@@ Vios @@@ vios @@@ ### @@@ vios @@@ 183 @@@ Vista @@@ vista @@@ ### @@@ vista @@@ 183 @@@ Vitz @@@ vitz @@@ vitz @@@ vitz @@@ 183 @@@ Voltz @@@ voltz @@@ ### @@@ voltz @@@ 183 @@@ Voxy @@@ voxy @@@ ### @@@ voxy @@@ 183 @@@ WiLL @@@ will @@@ ### @@@ will @@@ 183 @@@ WiLL Cypha @@@ will-cypha @@@ ### @@@ will_cypha @@@ 183 @@@ Windom @@@ windom @@@ ### @@@ windom @@@ 183 @@@ Wish @@@ wish @@@ ### @@@ wish @@@ 183 @@@ Yaris @@@ yaris @@@ yaris @@@ yaris @@@ 183 @@@ Yaris Verso @@@ yaris_verso_p2 @@@ ### @@@ ### @@@ 183 @@@ 42005 @@@ 1dot1 @@@ ### @@@ ### @@@ 184 @@@ P 601 @@@ p601 @@@ ### @@@ ### @@@ 184 @@@ Tramontana @@@ tramontana @@@ ### @@@ ### @@@ 185 @@@ TR7 @@@ tr7 @@@ ### @@@ ### @@@ 186 @@@ TR8 @@@ tr8 @@@ ### @@@ ### @@@ 186 @@@ 280 @@@ 280 @@@ ### @@@ ### @@@ 187 @@@ 390 @@@ 390 @@@ ### @@@ ### @@@ 187 @@@ 400 @@@ 400 @@@ ### @@@ ### @@@ 187 @@@ 420 @@@ 420 @@@ ### @@@ ### @@@ 187 @@@ 450 @@@ 450 @@@ ### @@@ ### @@@ 187 @@@ Cerbera @@@ cerbera @@@ ### @@@ cerbera @@@ 187 @@@ Chimaera @@@ chimaera @@@ ### @@@ chimaera @@@ 187 @@@ Griffith @@@ griffith @@@ ### @@@ griffith @@@ 187 @@@ Sagaris @@@ sagaris @@@ ### @@@ sagaris @@@ 187 @@@ S-Series @@@ s @@@ ### @@@ ### @@@ 187 @@@ Tamora @@@ tamora @@@ ### @@@ tamora @@@ 187 @@@ Tuscan @@@ tuscan @@@ ### @@@ tuscan @@@ 187 @@@ GTR @@@ gtr @@@ ### @@@ ### @@@ 188 @@@ Carlton @@@ carlton @@@ ### @@@ ### @@@ 189 @@@ Ventora @@@ ventora @@@ ### @@@ ### @@@ 189 @@@ Viceroy @@@ viceroy @@@ ### @@@ ### @@@ 189 @@@ Victor @@@ victor @@@ ### @@@ ### @@@ 189 @@@ VXR8 @@@ vxr8 @@@ ### @@@ ### @@@ 189 @@@ M12 @@@ m12 @@@ ### @@@ ### @@@ 190 @@@ W8 Twin Turbo @@@ w8-twin-turbo @@@ ### @@@ ### @@@ 190 @@@ 210 @@@ 210 @@@ ### @@@ ### @@@ 191 @@@ 260 LM @@@ 260-lm @@@ ### @@@ ### @@@ 191 @@@ 300 Atlantique @@@ 300-atlantique @@@ ### @@@ ### @@@ 191 @@@ 400 GT @@@ 400-gt @@@ ### @@@ ### @@@ 191 @@@ 181 @@@ 181 @@@ ### @@@ ### @@@ 192 @@@ Amarok @@@ amarok @@@ amarok @@@ amarok @@@ 192 @@@ Bora @@@ bora @@@ bora @@@ bora @@@ 192 @@@ Beetle @@@ beetle @@@ beetle @@@ beetle @@@ 192 @@@ Caddy @@@ caddy @@@ caddy @@@ caddy @@@ 192 @@@ Corrado @@@ corrado @@@ corrado @@@ ### @@@ 192 @@@ Crafter @@@ ### @@@ crafter @@@ ### @@@ 192 @@@ CrossPolo @@@ ### @@@ crosspolo @@@ ### @@@ 192 @@@ Derby @@@ derby @@@ ### @@@ derby @@@ 192 @@@ Eos @@@ eos @@@ eos @@@ eos @@@ 192 @@@ Fox @@@ fox @@@ fox @@@ fox @@@ 192 @@@ Golf @@@ golf @@@ golf @@@ golf @@@ 192 @@@ Golf Country @@@ golfcountry @@@ golf_country @@@ country @@@ 192 @@@ Golf GTI @@@ golf-gti @@@ golf_gti @@@ ### @@@ 192 @@@ Golf Plus @@@ golf_plus @@@ golf_plus @@@ golf_plus @@@ 192 @@@ Golf R @@@ golf-r @@@ golf_r @@@ ### @@@ 192 @@@ Golf Sportsvan @@@ golf-sportsvan @@@ ### @@@ ### @@@ 192 @@@ Iltis @@@ iltis @@@ ### @@@ ### @@@ 192 @@@ Jetta @@@ jetta @@@ jetta @@@ jetta @@@ 192 @@@ Kaefer @@@ kaefer @@@ kaefer @@@ kaefer @@@ 192 @@@ Lupo @@@ lupo @@@ lupo @@@ lupo @@@ 192 @@@ Lupo GTI @@@ lupo-gti @@@ ### @@@ ### @@@ 192 @@@ Multivan @@@ multivan @@@ multivan @@@ multivan @@@ 192 @@@ New Beetle @@@ ### @@@ new_beetle @@@ ### @@@ 192 @@@ Passat @@@ passat @@@ passat @@@ passat @@@ 192 @@@ Passat (North America) @@@ passat-na @@@ ### @@@ ### @@@ 192 @@@ Passat CC @@@ passat-cc @@@ passat_cc @@@ passat_cc @@@ 192 @@@ Phaeton @@@ phaeton @@@ phaeton @@@ phaeton @@@ 192 @@@ Pointer @@@ pointer @@@ pointer @@@ pointer @@@ 192 @@@ Polo @@@ polo @@@ polo @@@ polo @@@ 192 @@@ Polo GTI @@@ polo-gti @@@ ### @@@ ### @@@ 192 @@@ Polo R WRC @@@ polo-r-wrc @@@ ### @@@ ### @@@ 192 @@@ Routan @@@ routan @@@ ### @@@ routan @@@ 192 @@@ Santana @@@ santana @@@ ### @@@ santana @@@ 192 @@@ Scirocco @@@ scirocco @@@ scirocco @@@ scirocco @@@ 192 @@@ Scirocco R @@@ scirocco-r @@@ ### @@@ ### @@@ 192 @@@ Sharan @@@ sharan @@@ sharan @@@ sharan @@@ 192 @@@ Taro @@@ taro @@@ ### @@@ taro @@@ 192 @@@ Tiguan @@@ tiguan @@@ tiguan @@@ tiguan @@@ 192 @@@ Touareg @@@ touareg @@@ touareg @@@ touareg @@@ 192 @@@ Touran @@@ touran @@@ touran @@@ touran @@@ 192 @@@ Transporter @@@ transporter @@@ transporter @@@ transporter @@@ 192 @@@ Type 4 @@@ type4 @@@ ### @@@ ### @@@ 192 @@@ up! @@@ up @@@ ### @@@ up @@@ 192 @@@ Vento @@@ vento @@@ vento @@@ vento @@@ 192 @@@ XL1 @@@ xl1 @@@ ### @@@ ### @@@ 192 @@@ 66 @@@ 66 @@@ ### @@@ ### @@@ 193 @@@ 240 @@@ ### @@@ 240 @@@ 240 @@@ 193 @@@ 440 @@@ 440 @@@ 440 @@@ 440 @@@ 193 @@@ 460 @@@ 460 @@@ 460 @@@ 460 @@@ 193 @@@ 480 @@@ 480 @@@ ### @@@ ### @@@ 193 @@@ 740 @@@ 740 @@@ 740 @@@ 740 @@@ 193 @@@ 760 @@@ 760 @@@ 760 @@@ 760 @@@ 193 @@@ 780 @@@ 780 @@@ ### @@@ ### @@@ 193 @@@ 850 @@@ 850 @@@ 850 @@@ 850 @@@ 193 @@@ 940 @@@ 940 @@@ 940 @@@ 940 @@@ 193 @@@ 960 @@@ 960 @@@ 960 @@@ 960 @@@ 193 @@@ 140 Series @@@ 140 @@@ ### @@@ ### @@@ 193 @@@ 240 Series @@@ 240 @@@ ### @@@ ### @@@ 193 @@@ 260 Series @@@ 260 @@@ ### @@@ ### @@@ 193 @@@ 300 Series @@@ 300 @@@ ### @@@ ### @@@ 193 @@@ C30 @@@ c30 @@@ c30 @@@ c30 @@@ 193 @@@ C70 @@@ c70 @@@ c70 @@@ c70 @@@ 193 @@@ Laplander @@@ laplander @@@ ### @@@ ### @@@ 193 @@@ S40 @@@ s40 @@@ s40 @@@ s40 @@@ 193 @@@ S60 @@@ s60 @@@ s60 @@@ s60 @@@ 193 @@@ S60 Cross Country @@@ s60-cross-country @@@ ### @@@ ### @@@ 193 @@@ S70 @@@ s70 @@@ s70 @@@ s70 @@@ 193 @@@ S80 @@@ s80 @@@ s80 @@@ s80 @@@ 193 @@@ S90 @@@ s90 @@@ s90 @@@ s90 @@@ 193 @@@ V40 @@@ v40 @@@ v40 @@@ v40 @@@ 193 @@@ V40 Cross Country @@@ v40-cross-country @@@ v40_cross_country @@@ ### @@@ 193 @@@ V50 @@@ v50 @@@ v50 @@@ v50 @@@ 193 @@@ V60 @@@ v60 @@@ v60 @@@ v60 @@@ 193 @@@ V60 Cross Country @@@ v60-cross-country @@@ ### @@@ ### @@@ 193 @@@ V70 @@@ v70 @@@ v70 @@@ V70 @@@ 193 @@@ V90 @@@ v90 @@@ v90 @@@ v90 @@@ 193 @@@ XC60 @@@ xc60 @@@ xc60 @@@ xc60 @@@ 193 @@@ XC70 @@@ xc70 @@@ xc70 @@@ xc70 @@@ 193 @@@ XC90 @@@ xc90 @@@ xc90 @@@ xc90 @@@ 193 @@@ Corda @@@ corda @@@ corda @@@ corda @@@ 194 @@@ Estina @@@ estina @@@ esti","na @@@ estina @@@ 194 @@@ Tingo @@@ tingo @@@ tingo @@@ tingo @@@ 194 @@@ 353 @@@ 353 @@@ ### @@@ ### @@@ 195 @@@ 42064 @@@ 42064 @@@ ### @@@ ### @@@ 195 @@@ SEi & Sport @@@ sport @@@ ### @@@ ### @@@ 196 @@@ SEiGHT @@@ seight @@@ ### @@@ ### @@@ 196 @@@ Pickup X3 @@@ pickup-x3 @@@ ### @@@ pickup_x3 @@@ 198 @@@ SR-V X3 @@@ sr-v @@@ ### @@@ sr-v @@@ 198 @@@ SUV X3 @@@ suv-x3 @@@ ### @@@ suv_x3 @@@ 198 @@@ 10 @@@ 10 @@@ ### @@@ ### @@@ 199 @@@ Florida @@@ florida @@@ ### @@@ ### @@@ 199 @@@ Skala @@@ skala @@@ ### @@@ ### @@@ 199 @@@ Yugo @@@ yugo @@@ ### @@@ ### @@@ 199 @@@ ST1 @@@ st1 @@@ ### @@@ ### @@@ 200 @@@ Nomad (RX6400) @@@ rx-6400 @@@ ### @@@ ### @@@ 201 @@@ Grand Tiger @@@ grandtiger @@@ grandtiger @@@ grandtiger @@@ 202 @@@ Landmark @@@ landmark @@@ landmark @@@ landmark @@@ 202 @@@ 2160 @@@ 2160 @@@ ### @@@ ### @@@ 203 @@@ 2163 @@@ 2163 @@@ ### @@@ ### @@@ 203 @@@ 3101 @@@ 3101 @@@ ### @@@ ### @@@ 203 @@@ Ока-Астро 11301 @@@ 11301 @@@ ### @@@ ### @@@ 204 @@@ Рысь @@@ ris @@@ ### @@@ ### @@@ 205 @@@ 2101 @@@ 2101 @@@ 2101 @@@ 2101 @@@ 206 @@@ 2102 @@@ 2102 @@@ 2102 @@@ 2102 @@@ 206 @@@ 2103 @@@ 2103 @@@ 2103 @@@ 2103 @@@ 206 @@@ 2104 @@@ 2104 @@@ 2104 @@@ 2104 @@@ 206 @@@ 2105 @@@ 2105 @@@ 2105 @@@ 2105 @@@ 206 @@@ 2106 @@@ 2106 @@@ 2106 @@@ 2106 @@@ 206 @@@ 2107 @@@ 2107 @@@ 2107 @@@ 2107 @@@ 206 @@@ 2108 @@@ 2108 @@@ 2108 @@@ 2108 @@@ 206 @@@ 2109 @@@ 2109 @@@ 2109 @@@ 2109 @@@ 206 @@@ 2110 @@@ 2110 @@@ 2110 @@@ 2110 @@@ 206 @@@ 2111 @@@ 2111 @@@ 2111 @@@ 2111 @@@ 206 @@@ 2112 @@@ 2112 @@@ 2112 @@@ 2112 @@@ 206 @@@ 2113 @@@ 2113 @@@ 2113_samara @@@ 2113 @@@ 206 @@@ 2114 @@@ 2114 @@@ 2114_samara @@@ 2114 @@@ 206 @@@ 2115 @@@ 2115 @@@ 2115_samara @@@ 2115 @@@ 206 @@@ 2123 @@@ 2123 @@@ 2123 @@@ ### @@@ 206 @@@ 2129 @@@ 2129 @@@ 2129 @@@ ### @@@ 206 @@@ 2328 @@@ 2328 @@@ ### @@@ ### @@@ 206 @@@ 2329 @@@ 2329 @@@ 2329 @@@ 2329_4x4_pickup @@@ 206 @@@ 21099 @@@ 21099 @@@ 21099 @@@ 21099 @@@ 206 @@@ 1111 Ока @@@ 1111-oka @@@ 1111_oka @@@ oka @@@ 206 @@@ 2120 Надежда @@@ 2120 @@@ 2120_nadezhda @@@ 2120_nadezhda @@@ 206 @@@ 2121 (4x4) @@@ 2121 @@@ ### @@@ 2121_4x4_niva @@@ 206 @@@ 2131 (4x4) @@@ 2131 @@@ ### @@@ 2131_4x4_niva @@@ 206 @@@ 4x4 (Нива) @@@ ### @@@ 4x4_niva @@@ ### @@@ 206 @@@ Granta @@@ granta @@@ granta @@@ granta @@@ 206 @@@ Kalina @@@ kalina @@@ kalina @@@ kalina @@@ 206 @@@ Largus @@@ largus @@@ largus @@@ largus @@@ 206 @@@ Priora @@@ priora @@@ priora @@@ priora @@@ 206 @@@ Revolution @@@ revolution @@@ ### @@@ ### @@@ 206 @@@ Vesta @@@ vesta @@@ ### @@@ vesta @@@ 206 @@@ XRAY concept @@@ xray @@@ ### @@@ xray @@@ 206 @@@ 1705 @@@ ### @@@ 1705 @@@ ### @@@ 207 @@@ 2345 @@@ ### @@@ 2345 @@@ ### @@@ 207 @@@ 2346 @@@ ### @@@ 2346 @@@ ### @@@ 207 @@@ 2347 @@@ ### @@@ 2347 @@@ ### @@@ 207 @@@ 2349 @@@ ### @@@ 2349 @@@ ### @@@ 207 @@@ 69 @@@ 69 @@@ 69 @@@ ### @@@ 208 @@@ 12 ЗИМ @@@ ### @@@ 12_zim @@@ 12_zim @@@ 208 @@@ 13 «Чайка» @@@ 13 @@@ 13_chayka @@@ chaika @@@ 208 @@@ 14 «Чайка» @@@ 14 @@@ 14_chayka @@@ ### @@@ 208 @@@ 21 «Волга» @@@ 21 @@@ 21_volga @@@ 21_volga @@@ 208 @@@ 22 «Волга» @@@ 22 @@@ 22 @@@ 22_volga @@@ 208 @@@ 2308 «Атаман» @@@ 2308 @@@ ### @@@ ### @@@ 208 @@@ 2330 «Тигр» @@@ 2330 @@@ ### @@@ ### @@@ 208 @@@ 24 «Волга» @@@ 24 @@@ 24_volga @@@ 24_volga @@@ 208 @@@ 3102 «Волга» @@@ 3102 @@@ 3102_volga @@@ 3102_volga @@@ 208 @@@ 310221 Волга @@@ ### @@@ 310221_volga @@@ ### @@@ 208 @@@ 31029 «Волга» @@@ 31029 @@@ 31029_volga @@@ 31029_volga @@@ 208 @@@ 3103 «Волга» @@@ 3103 @@@ ### @@@ ### @@@ 208 @@@ 3105 «Волга» @@@ 3105 @@@ ### @@@ ### @@@ 208 @@@ 3110 «Волга» @@@ 3110 @@@ 3110_volga @@@ 3110_volga @@@ 208 @@@ 31105 «Волга» @@@ 31105 @@@ 31105_volga @@@ 31105_volga @@@ 208 @@@ 3111 «Волга» @@@ 3111 @@@ 3111_volga @@@ 3111_volga @@@ 208 @@@ M-1 @@@ m1 @@@ m-1 @@@ m1 @@@ 208 @@@ Volga Siber @@@ siber @@@ volga_siber @@@ volga_siber @@@ 208 @@@ ГАЗель @@@ ### @@@ gazel @@@ gaz @@@ 208 @@@ ГАЗель 2705 @@@ ### @@@ gazel_2705 @@@ ### @@@ 208 @@@ ГАЗель 3221 @@@ ### @@@ gazel_3221 @@@ ### @@@ 208 @@@ ГАЗель 3302 @@@ ### @@@ gazel_3302 @@@ ### @@@ 208 @@@ ГАЗель 33023 @@@ ### @@@ gazel_33023 @@@ ### @@@ 208 @@@ ГАЗель 33025 @@@ ### @@@ gazel_33025 @@@ ### @@@ 208 @@@ ГАЗель Next @@@ ### @@@ gazel_next @@@ ### @@@ 208 @@@ ГАЗель Бизнес @@@ ### @@@ gazel_biznes @@@ ### @@@ 208 @@@ М-20 «Победа» @@@ m-20 @@@ 20_pobeda @@@ pobeda @@@ 208 @@@ Соболь @@@ ### @@@ sobol @@@ ### @@@ 208 @@@ Соболь 2217 @@@ ### @@@ sobol_2217 @@@ ### @@@ 208 @@@ Соболь 2310 @@@ ### @@@ sobol_2310 @@@ ### @@@ 208 @@@ Соболь 2752 @@@ ### @@@ sobol_2752 @@@ ### @@@ 208 @@@ Ё-Кроссовер @@@ e-krossover @@@ ### @@@ ### @@@ 209 @@@ 965 @@@ 965 @@@ 965 @@@ ### @@@ 210 @@@ 966 @@@ 966 @@@ 966_zaporozhets @@@ zaporozhets @@@ 210 @@@ 968 @@@ 968 @@@ 968_zaporozhets @@@ zaporozhets @@@ 210 @@@ 1102 «Таврия» @@@ 1102 @@@ 1102_tavriya @@@ tavria @@@ 210 @@@ 1103 «Славута» @@@ 1103 @@@ 1103_slavuta @@@ slavuta @@@ 210 @@@ 1105 «Дана» @@@ 1105 @@@ ### @@@ dana @@@ 210 @@@ Chance @@@ chance @@@ chance @@@ chance @@@ 210 @@@ Forza @@@ forza @@@ ### @@@ forza @@@ 210 @@@ Vida @@@ vida @@@ vida @@@ vida @@@ 210 @@@ 111 @@@ ### @@@ 111 @@@ ### @@@ 211 @@@ 114 @@@ 114 @@@ ### @@@ ### @@@ 211 @@@ 117 @@@ 117 @@@ ### @@@ ### @@@ 211 @@@ 4104 @@@ 4104 @@@ 4104 @@@ ### @@@ 211 @@@ 4105 @@@ ### @@@ 4105 @@@ ### @@@ 211 @@@ 2715 @@@ ### @@@ 2715 @@@ 2715 @@@ 212 @@@ 2717 @@@ 2717 @@@ 2717 @@@ 2717 @@@ 212 @@@ 2125 «Комби» @@@ 2125 @@@ 2125 @@@ 2125 @@@ 212 @@@ 2126 «Ода» @@@ 2126 @@@ 2126 @@@ 2126_oda @@@ 212 @@@ 21261 «Фабула» @@@ 21261 @@@ ### @@@ 21261_oda_wagon @@@ 212 @@@ Москвич-412 @@@ 412 @@@ ### @@@ ### @@@ 212 @@@ 2317 @@@ 2317 @@@ ### @@@ ### @@@ 214 @@@ 967 @@@ 967 @@@ 967 @@@ ### @@@ 215 @@@ 969 @@@ 969 @@@ 969 @@@ ### @@@ 215 @@","@ 1302 Волынь @@@ 1302 @@@ ### @@@ ### @@@ 215 @@@ 401 @@@ 401 @@@ 401 @@@ ### @@@ 216 @@@ 402 @@@ 402 @@@ 402 @@@ ### @@@ 216 @@@ 403 @@@ 403 @@@ 403 @@@ ### @@@ 216 @@@ 410 @@@ 410 @@@ 410 @@@ ### @@@ 216 @@@ 411 @@@ 411 @@@ ### @@@ ### @@@ 216 @@@ 412 @@@ 412 @@@ 412 @@@ ### @@@ 216 @@@ 423 @@@ 423 @@@ 423 @@@ ### @@@ 216 @@@ 426 @@@ 426 @@@ 426 @@@ ### @@@ 216 @@@ 427 @@@ 427 @@@ ### @@@ ### @@@ 216 @@@ 2136 @@@ 2136 @@@ ### @@@ ### @@@ 216 @@@ 2137 @@@ 2137 @@@ 2137 @@@ ### @@@ 216 @@@ 2138 @@@ 2138 @@@ 2138 @@@ ### @@@ 216 @@@ 2140 @@@ 2140 @@@ 2140 @@@ ### @@@ 216 @@@ 2141 @@@ 2141 @@@ 2141 @@@ ### @@@ 216 @@@ Дуэт @@@ duet @@@ ### @@@ ### @@@ 216 @@@ Юрий Долгорукий @@@ yuriy-dolgorukiy @@@ ### @@@ ### @@@ 216 @@@ Иван Калита @@@ ivan-kalita @@@ ivan_kalita @@@ ### @@@ 216 @@@ Князь Владимир @@@ knyz-vladimir @@@ knyaz_vladimir @@@ ### @@@ 216 @@@ Святогор @@@ svyatogor @@@ svyatogor @@@ ### @@@ 216 @@@ 2203 @@@ ### @@@ 2203 @@@ ### @@@ 217 @@@ 1111 Ока @@@ 1111 @@@ ### @@@ ### @@@ 218 @@@ С-3Д @@@ s3d @@@ ### @@@ ### @@@ 219 @@@ С-3А @@@ s3m @@@ ### @@@ ### @@@ 219 @@@ Aquila @@@ aquila @@@ aquila @@@ aquila @@@ 220 @@@ C190 @@@ c190 @@@ c190 @@@ c190 @@@ 220 @@@ C-30 @@@ c-30 @@@ ### @@@ c30 @@@ 220 @@@ Road Partner @@@ road-partner @@@ road_partner @@@ road_partner @@@ 220 @@@ Tager @@@ tager @@@ tager @@@ tager @@@ 220 @@@ Vega @@@ vega @@@ vega @@@ vega @@@ 220 @@@ Vortex Estina @@@ ### @@@ vortex_estina @@@ estina @@@ 220 @@@ Vortex Tingo @@@ ### @@@ vortex_tingo @@@ tingo @@@ 220 @@@ 469 @@@ 469 @@@ 469 @@@ 469 @@@ 221 @@@ 2206 @@@ ### @@@ 2206 @@@ ### @@@ 221 @@@ 3151 @@@ 3151 @@@ 3151 @@@ 3151 @@@ 221 @@@ 3153 @@@ 3153 @@@ 3153 @@@ 3153 @@@ 221 @@@ 3159 @@@ 3159 @@@ 3159 @@@ 3159 @@@ 221 @@@ 3160 @@@ 3160 @@@ 3160 @@@ ### @@@ 221 @@@ 31512 @@@ ### @@@ 31512 @@@ ### @@@ 221 @@@ 31514 @@@ ### @@@ 31514 @@@ ### @@@ 221 @@@ 31519 @@@ ### @@@ 31519 @@@ ### @@@ 221 @@@ 3162 Simbir @@@ 3162 @@@ ### @@@ ### @@@ 221 @@@ 452 Буханка @@@ ### @@@ 452_buhanka @@@ buhanka @@@ 221 @@@ Hunter @@@ hunter @@@ hunter @@@ hunter @@@ 221 @@@ Pickup @@@ pickup @@@ pickup @@@ pickup @@@ 221 @@@ Симбир @@@ ### @@@ simbir @@@ simbir @@@ 221"});
            //endregion
            String[] models_split = models_one_str.split(" @@@ ");
            for(int i=0; i<models_split.length;i+=5){
                ContentValues cv = new ContentValues();
                //cv.put("id", models_split[i]);
                cv.put("modeluser", models_split[i]);
                cv.put("modelrequest", models_split[i+1]);
                cv.put("modelrequestavito", models_split[i+2]);
                cv.put("modelrequestdrom", models_split[i+3]);
                cv.put("marka_id", models_split[i+4]);
                db.insert("modelsTable", null, cv);
            }

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    public void showPickerDialog()
    {
        final Dialog dialogPicker = new Dialog(CreateRequestActivity.this);
        dialogPicker.setTitle("Год выпуска");
        dialogPicker.setContentView(R.layout.number_picker);

        final NumberPicker np1 = (NumberPicker) dialogPicker.findViewById(R.id.numberPicker1);
        np1.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        final NumberPicker np2 = (NumberPicker) dialogPicker.findViewById(R.id.numberPicker2);
        np2.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
        String oldYear1 = String.valueOf(sPref.getInt("StartYear", 2010));
        String oldYear2 = String.valueOf(sPref.getInt("EndYear", 2015));


        Integer count_year = 1900 + new Date().getYear() - 1970 - 9 -4 -4 +1;
        np1.setMinValue(1);
        np1.setMaxValue(count_year);
        np2.setMinValue(1);
        np2.setMaxValue(count_year);


        final String[] year_arr = new String[count_year];
        year_arr[0]="1970";
        year_arr[1]="1980";
        year_arr[2]="1985";
        year_arr[3]="1990";
        for(int i=4; i<year_arr.length; ++i){
            year_arr[i] = String.valueOf(Integer.parseInt(year_arr[i - 1])+1);
        }
        np1.setDisplayedValues(year_arr);
        np2.setDisplayedValues(year_arr);

        for(int j=0;j<year_arr.length;++j){
            if(year_arr[j].equals(oldYear1))
                np1.setValue(j+1);
            if(year_arr[j].equals(oldYear2))
                np2.setValue(j+1);
        }

        Button ok = (Button) dialogPicker.findViewById(R.id.buttonPicker);
        ok.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v) {

                Integer start = Integer.parseInt(year_arr[np1.getValue() - 1]);
                Integer end = Integer.parseInt(year_arr[np2.getValue() - 1]);
                if(start > end)
                {
                    Toast t = Toast.makeText(getApplicationContext(),"Параметры заданы некорректно",Toast.LENGTH_SHORT);
                    t.show();
                }
                else
                {
                    SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
                    sPref.edit().putInt("StartYear", Integer.parseInt(year_arr[np1.getValue() - 1])).commit();
                    sPref.edit().putInt("EndYear", Integer.parseInt(year_arr[np2.getValue() - 1])).commit();

                    Button b2 = (Button) findViewById(R.id.year_button);
                    b2.setText("Год выпуска: с " + year_arr[np1.getValue() - 1] + " по " + year_arr[np2.getValue() - 1]);

                    Button b3 = (Button) findViewById(R.id.clear_year);
                    b3.setVisibility(View.VISIBLE);


                    dialogPicker.dismiss();
                }
            }
        });
        dialogPicker.show();
    }
    public void showPickerPrice()
    {
        final Dialog dialogPicker = new Dialog(CreateRequestActivity.this);
        dialogPicker.setTitle("Ценовой диапазон");
        dialogPicker.setContentView(R.layout.number_picker);

        final NumberPicker np1 = (NumberPicker) dialogPicker.findViewById(R.id.numberPicker1);
        final NumberPicker np2 = (NumberPicker) dialogPicker.findViewById(R.id.numberPicker2);

        np2.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        np1.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
        String oldPrice1 = String.valueOf(sPref.getInt("StartPrice", 2010));
        String oldPrice2 = String.valueOf(sPref.getInt("EndPrice", 2015));

        Integer count_price = 96+80;
        np1.setMinValue(1);
        np1.setMaxValue(count_price);
        np2.setMinValue(1);
        np2.setMaxValue(count_price);

        final String[] price_arr = new String[count_price];
        for(int i=0; i<price_arr.length; ++i){
            if(i<51){
                price_arr[i]= String.valueOf(i*10000);
            }
            else
            if(i<76){
                price_arr[i]= String.valueOf(Integer.parseInt(price_arr[i-1].replace(" ",""))+20000);
            }
            else
            if(i<96) {
                price_arr[i] = String.valueOf(Integer.parseInt(price_arr[i - 1].replace(" ","")) + 50000);
            }
            else
                price_arr[i] = String.valueOf(Integer.parseInt(price_arr[i - 1].replace(" ","")) + 100000);

            int len = price_arr[i].length(), counter;
            String result = "";
            if(len%3!=0)
                result = price_arr[i].substring(0,len%3)+" ";
            counter = len%3;
            while (counter < len)
            {
                result += price_arr[i].substring(counter,counter+3)+" ";
                counter+=3;
            }
            price_arr[i] = result.substring(0,result.length()-1);
        }
        np1.setDisplayedValues(price_arr);
        np2.setDisplayedValues(price_arr);


        for(int j=0;j<price_arr.length;++j){
            if(price_arr[j].equals(oldPrice1))
                np1.setValue(j+1);
            if(price_arr[j].equals(oldPrice2))
                np2.setValue(j+1);
        }



        Button ok = (Button) dialogPicker.findViewById(R.id.buttonPicker);
        ok.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v) {

                Integer start = Integer.parseInt(price_arr[np1.getValue() - 1].replace(" ",""));
                Integer end = Integer.parseInt(price_arr[np2.getValue() - 1].replace(" ",""));
                if(start > end)
                {
                    Toast t = Toast.makeText(getApplicationContext(),"Параметры заданы некорректно",Toast.LENGTH_SHORT);
                    t.show();
                }
                else {

                    SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
                    sPref.edit().putInt("StartPrice", start).commit();
                    sPref.edit().putInt("EndPrice", end).commit();

                    Button b2 = (Button) findViewById(R.id.price_button);
                    b2.setText("Цена: от " + price_arr[np1.getValue() - 1] + " до " + price_arr[np2.getValue() - 1]);

                    Button b3 = (Button) findViewById(R.id.clear_price);
                    b3.setVisibility(View.VISIBLE);

                    dialogPicker.dismiss();
                }
            }
        });
        dialogPicker.show();
    }
    public void showPickerEngineVolume()
    {
        final Dialog dialogPicker = new Dialog(CreateRequestActivity.this);
        dialogPicker.setTitle("Объём двигателя");
        dialogPicker.setContentView(R.layout.number_picker);

        SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
        int oldVolume1 = sPref.getInt("StartVolume", 0);
        int oldVolume2 = sPref.getInt("EndVolume", 36);

        final NumberPicker np1 = (NumberPicker) dialogPicker.findViewById(R.id.numberPicker1);
        final NumberPicker np2 = (NumberPicker) dialogPicker.findViewById(R.id.numberPicker2);

        np2.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        np1.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);


        final String[] value_arr = new String[]{"0.0","0.6","0.7","0.8","0.9","1.0","1.1","1.2","1.3","1.4","1.5","1.6","1.7","1.8","1.9","2.0","2.1","2.2","2.3","2.4","2.5","2.6","2.7","2.8","2.9","3.0","3.1","3.2","3.3","3.4","3.5","4.0","4.5","5.0","5.5","6.0","6.0+"};
        np1.setMinValue(1);
        np1.setMaxValue(value_arr.length);
        np2.setMinValue(1);
        np2.setMaxValue(value_arr.length);

        np1.setDisplayedValues(value_arr);
        np2.setDisplayedValues(value_arr);


        np1.setValue(oldVolume1 + 1);
        np2.setValue(oldVolume2 + 1);


        Button ok = (Button) dialogPicker.findViewById(R.id.buttonPicker);
        ok.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v) {

                if(np1.getValue() > np2.getValue())
                {
                    Toast t = Toast.makeText(getApplicationContext(),"Параметры заданы некорректно",Toast.LENGTH_SHORT);
                    t.show();
                }
                else {

                    SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
                    sPref.edit().putInt("StartVolume", np1.getValue() - 1).commit();
                    sPref.edit().putInt("EndVolume", np2.getValue() - 1).commit();

                    Button b2 = (Button) findViewById(R.id.engine_volume_button);
                    b2.setText("Объём : от " + value_arr[np1.getValue() - 1] + " до " + value_arr[np2.getValue() - 1]);

                    Button b3 = (Button) findViewById(R.id.clear_enginevolume);
                    b3.setVisibility(View.VISIBLE);

                    dialogPicker.dismiss();
                }
            }
        });
        dialogPicker.show();
    }
    public void showPickerProbeg()
    {
        final Dialog dialogPicker = new Dialog(CreateRequestActivity.this);
        dialogPicker.setTitle("Пробег до (тыс. км.):");
        dialogPicker.setContentView(R.layout.one_picker);

        SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
        int oldProbeg = sPref.getInt("Probeg", 61);

        final NumberPicker np1 = (NumberPicker) dialogPicker.findViewById(R.id.onenumberPicker);

        np1.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        final String[] probeg_arr = new String[]{"0","5","10","15","20","25","30","35","40","45","50","55", "60", "65", "70","75","80","85","90","95","100","110","120","130","140","150","160","170","180","190","200","210","220","230","240","250","260","270","280","290","300","310","320","330","340","350","360","370","380","390","400","410","420","430","440","450","460","470","480","490","500","500+"};
        np1.setMinValue(1);
        np1.setMaxValue(probeg_arr.length);
        np1.setDisplayedValues(probeg_arr);
        np1.setValue(oldProbeg + 1);

        Button ok = (Button) dialogPicker.findViewById(R.id.onebuttonPicker);
        ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
                sPref.edit().putInt("Probeg", np1.getValue() - 1).commit();

                Button b2 = (Button) findViewById(R.id.probeg_button);
                b2.setText("Пробег до : " + probeg_arr[np1.getValue() - 1] + " тыс. км.");

                Button b3 = (Button) findViewById(R.id.clear_probeg);
                b3.setVisibility(View.VISIBLE);

                dialogPicker.dismiss();

            }
        });
        dialogPicker.show();
    }
    public void showMultiChecker(int n){
        final Dialog dialog = new Dialog(CreateRequestActivity.this);
        Button ok;
        final String[] result = {""};
        final SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
        switch (n){
            case 1:
                dialog.setTitle("Коробка передач");
                dialog.setContentView(R.layout.trans_checkbox);
                dialog.show();

                String old = sPref.getString("Trans","");
                if(!old.equals("")) {
                    String[] older = old.split("");
                    CheckBox[] t = new CheckBox[4];
                    t[0] = (CheckBox) dialog.findViewById(R.id.checkBoxtrans1);
                    t[1] = (CheckBox) dialog.findViewById(R.id.checkBoxtrans2);
                    t[2] = (CheckBox) dialog.findViewById(R.id.checkBoxtrans3);
                    t[3] = (CheckBox) dialog.findViewById(R.id.checkBoxtrans4);
                    for (int i = 1; i < older.length; ++i) {
                        t[Integer.parseInt(older[i])-1].setChecked(true);
                    }
                }

                ok = (Button) dialog.findViewById(R.id.transCheckBox);
                ok.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        CheckBox auto = (CheckBox) dialog.findViewById(R.id.checkBoxtrans1);
                        CheckBox man = (CheckBox) dialog.findViewById(R.id.checkBoxtrans2);
                        CheckBox rob = (CheckBox) dialog.findViewById(R.id.checkBoxtrans3);
                        CheckBox var = (CheckBox) dialog.findViewById(R.id.checkBoxtrans4);
                        result[0] = "";
                        if(auto.isChecked()){
                            result[0] += "1";
                        }
                        if(man.isChecked()){
                            result[0] += "2";
                        }
                        if(rob.isChecked()){
                            result[0] += "3";
                        }
                        if(var.isChecked()){
                            result[0] += "4";
                        }

                        sPref.edit().putString("Trans", result[0]).commit();

                        Button b2 = (Button) findViewById(R.id.trans_button);
                        Integer count = result[0].length();
                        Button b3 = (Button) findViewById(R.id.clear_trans);

                        if(count != 0){
                            b2.setText("КПП ("+count+")");
                            b3.setVisibility(View.VISIBLE);
                        }
                        else
                            b2.setText("КПП");


                        dialog.dismiss();

                    }
                });
                break;
            case 2:
                dialog.setTitle("Тип двигателя");
                dialog.setContentView(R.layout.engine_type_checkbox);
                dialog.show();


                String old1 = sPref.getString("EngineType","");
                if(!old1.equals("")) {
                    String[] older = old1.split("");
                    CheckBox[] t = new CheckBox[5];
                    t[0] = (CheckBox) dialog.findViewById(R.id.checkBoxenginetype1);
                    t[1] = (CheckBox) dialog.findViewById(R.id.checkBoxenginetype2);
                    t[2] = (CheckBox) dialog.findViewById(R.id.checkBoxenginetype3);
                    t[3] = (CheckBox) dialog.findViewById(R.id.checkBoxenginetype4);
                    t[4] = (CheckBox) dialog.findViewById(R.id.checkBoxenginetype5);
                    for (int i = 1; i < older.length; ++i) {
                        t[Integer.parseInt(older[i])-1].setChecked(true);
                    }
                }

                ok = (Button) dialog.findViewById(R.id.enginetypeCheckBox);
                ok.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        CheckBox eng1 = (CheckBox) dialog.findViewById(R.id.checkBoxenginetype1);
                        CheckBox eng2 = (CheckBox) dialog.findViewById(R.id.checkBoxenginetype2);
                        CheckBox eng3 = (CheckBox) dialog.findViewById(R.id.checkBoxenginetype3);
                        CheckBox eng4 = (CheckBox) dialog.findViewById(R.id.checkBoxenginetype4);
                        CheckBox eng5 = (CheckBox) dialog.findViewById(R.id.checkBoxenginetype5);
                        result[0] = "";
                        if (eng1.isChecked()) {
                            result[0] += "1";
                        }
                        if (eng2.isChecked()) {
                            result[0] += "2";
                        }
                        if (eng3.isChecked()) {
                            result[0] += "3";
                        }
                        if (eng4.isChecked()) {
                            result[0] += "4";
                        }
                        if (eng5.isChecked()) {
                            result[0] += "5";
                        }

                        sPref.edit().putString("EngineType", result[0]).commit();

                        Button b2 = (Button) findViewById(R.id.engine_button);
                        Integer count = result[0].length();

                        Button b3 = (Button) findViewById(R.id.clear_engine);

                        if(count != 0){
                            b2.setText("Тип двигателя ("+count+")");
                            b3.setVisibility(View.VISIBLE);
                        }
                        else
                            b2.setText("Тип двигателя");

                        dialog.dismiss();

                    }
                });
                break;
            case 3:
                dialog.setTitle("Привод");
                dialog.setContentView(R.layout.privod_checkbox);
                dialog.show();

                String old2 = sPref.getString("Privod","");
                if(!old2.equals("")) {
                    String[] older = old2.split("");
                    CheckBox[] t = new CheckBox[3];
                    t[0] = (CheckBox) dialog.findViewById(R.id.checkBoxprivod1);
                    t[1] = (CheckBox) dialog.findViewById(R.id.checkBoxprivod2);
                    t[2] = (CheckBox) dialog.findViewById(R.id.checkBoxprivod3);
                    for (int i = 1; i < older.length; ++i) {
                        t[Integer.parseInt(older[i])-1].setChecked(true);
                    }
                }

                ok = (Button) dialog.findViewById(R.id.privodCheckBox);
                ok.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        CheckBox pr1 = (CheckBox) dialog.findViewById(R.id.checkBoxprivod1);
                        CheckBox pr2 = (CheckBox) dialog.findViewById(R.id.checkBoxprivod2);
                        CheckBox pr3 = (CheckBox) dialog.findViewById(R.id.checkBoxprivod3);
                        result[0] = "";
                        if (pr1.isChecked()) {
                            result[0] += "1";
                        }
                        if (pr2.isChecked()) {
                            result[0] += "2";
                        }
                        if (pr3.isChecked()) {
                            result[0] += "3";
                        }

                        sPref.edit().putString("Privod", result[0]).commit();

                        Button b2 = (Button) findViewById(R.id.privod_button);
                        b2.setText("Привод (" + result[0].length() + ")");
                        Integer count = result[0].length();
                        Button b3 = (Button) findViewById(R.id.clear_privod);
                        if(count != 0){
                            b2.setText("Привод (" + count + ")");
                            b3.setVisibility(View.VISIBLE);
                        }
                        else
                            b2.setText("Привод");

                        dialog.dismiss();

                    }
                });
                break;
            case 4:
                dialog.setTitle("Тип кузова");
                dialog.setContentView(R.layout.body_checkbox);
                dialog.show();

                String old3 = sPref.getString("Body","");
                if(!old3.equals("")) {
                    String[] older = old3.split("");
                    CheckBox[] t = new CheckBox[10];
                    t[0] = (CheckBox) dialog.findViewById(R.id.checkBoxbody1);
                    t[1]  = (CheckBox) dialog.findViewById(R.id.checkBoxbody2);
                    t[2]  = (CheckBox) dialog.findViewById(R.id.checkBoxbody3);
                    t[3]  = (CheckBox) dialog.findViewById(R.id.checkBoxbody4);
                    t[4]  = (CheckBox) dialog.findViewById(R.id.checkBoxbody5);
                    t[5]  = (CheckBox) dialog.findViewById(R.id.checkBoxbody6);
                    t[6]  = (CheckBox) dialog.findViewById(R.id.checkBoxbody7);
                    t[7]  = (CheckBox) dialog.findViewById(R.id.checkBoxbody8);
                    t[8]  = (CheckBox) dialog.findViewById(R.id.checkBoxbody9);
                    t[9]  = (CheckBox) dialog.findViewById(R.id.checkBoxbody10);
                    for (int i = 1; i < older.length; ++i) {
                        if(Integer.parseInt(older[i])==0){
                            t[9].setChecked(true);
                            continue;
                        }
                        t[Integer.parseInt(older[i])-1].setChecked(true);
                    }
                }

                ok = (Button) dialog.findViewById(R.id.bodyCheckBox);
                ok.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        CheckBox b1 = (CheckBox) dialog.findViewById(R.id.checkBoxbody1);
                        CheckBox b2 = (CheckBox) dialog.findViewById(R.id.checkBoxbody2);
                        CheckBox b3 = (CheckBox) dialog.findViewById(R.id.checkBoxbody3);
                        CheckBox b4 = (CheckBox) dialog.findViewById(R.id.checkBoxbody4);
                        CheckBox b5 = (CheckBox) dialog.findViewById(R.id.checkBoxbody5);
                        CheckBox b6 = (CheckBox) dialog.findViewById(R.id.checkBoxbody6);
                        CheckBox b7 = (CheckBox) dialog.findViewById(R.id.checkBoxbody7);
                        CheckBox b8 = (CheckBox) dialog.findViewById(R.id.checkBoxbody8);
                        CheckBox b9 = (CheckBox) dialog.findViewById(R.id.checkBoxbody9);
                        CheckBox b10 = (CheckBox) dialog.findViewById(R.id.checkBoxbody10);
                        result[0] = "";
                        if (b1.isChecked()) {
                            result[0] += "1";
                        }
                        if (b2.isChecked()) {
                            result[0] += "2";
                        }
                        if (b3.isChecked()) {
                            result[0] += "3";
                        }
                        if (b4.isChecked()) {
                            result[0] += "4";
                        }
                        if (b5.isChecked()) {
                            result[0] += "5";
                        }
                        if (b6.isChecked()) {
                            result[0] += "6";
                        }
                        if (b7.isChecked()) {
                            result[0] += "7";
                        }
                        if (b8.isChecked()) {
                            result[0] += "8";
                        }
                        if (b9.isChecked()) {
                            result[0] += "9";
                        }
                        if (b10.isChecked()) {
                            result[0] += "0";
                        }

                        sPref.edit().putString("Body", result[0]).commit();

                        Button button = (Button) findViewById(R.id.body_button);
                        button.setText("Тип кузова (" + result[0].length() + ")");

                        Button button1 = (Button) findViewById(R.id.clear_body);

                        Integer count = result[0].length();
                        if(count != 0){
                            button.setText("Тип кузова (" + result[0].length() + ")");
                            button1.setVisibility(View.VISIBLE);
                        }
                        else
                            button.setText("Тип кузова");

                        dialog.dismiss();

                    }
                });
                break;
            default:
                break;
        }

    }
    public void clearClick(View v) {
        SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
        Button b2;
        Button b3;
        switch (v.getId()) {
            case R.id.clear_year:
                // clear year
                sPref.edit().putInt("StartYear", 1970).commit();
                sPref.edit().putInt("EndYear", 1900 + new Date().getYear()).commit();
                b2 = (Button) findViewById(R.id.year_button);
                b2.setText("Год выпуска");
                b3 = (Button) findViewById(R.id.clear_year);
                b3.setVisibility(View.INVISIBLE);
                break;
            case R.id.clear_price:
                //clear price
                sPref.edit().putInt("StartPrice", 0).commit();
                sPref.edit().putInt("EndPrice", 10000000).commit();
                b2 = (Button) findViewById(R.id.price_button);
                b2.setText("Цена");
                b3 = (Button) findViewById(R.id.clear_price);
                b3.setVisibility(View.INVISIBLE);
                break;
            case R.id.clear_enginevolume:
                //clear engine volume
                sPref.edit().putInt("StartVolume", 0).commit();
                sPref.edit().putInt("EndVolume", 36).commit();
                b2 = (Button) findViewById(R.id.engine_volume_button);
                b2.setText("Объём двигателя");
                b3 = (Button) findViewById(R.id.clear_enginevolume);
                b3.setVisibility(View.INVISIBLE);
                break;
            case R.id.clear_probeg:
                //clear probeg
                sPref.edit().putInt("Probeg", 61).commit();
                b2 = (Button) findViewById(R.id.probeg_button);
                b2.setText("Пробег");
                b3 = (Button) findViewById(R.id.clear_probeg);
                b3.setVisibility(View.INVISIBLE);
                break;
            case R.id.clear_marka:
                b2 = (Button) findViewById(R.id.marka_button);
                b2.setText("Марка");

                sPref.edit().putString("SelectedMark", "Любая").commit();
                sPref.edit().putString("SelectedModel", "Любая").commit();

                b3 = (Button) findViewById(R.id.clear_marka);
                b3.setVisibility(View.INVISIBLE);
            case R.id.clear_model:
                b2 = (Button) findViewById(R.id.model_button);
                b2.setText("Модель");

                sPref.edit().putString("SelectedModel", "Любая").commit();

                b3 = (Button) findViewById(R.id.clear_model);
                b3.setVisibility(View.INVISIBLE);
                break;
            case R.id.clear_trans:
                b2 = (Button) findViewById(R.id.trans_button);
                b2.setText("КПП");

                sPref.edit().putString("Trans", "").commit();

                b3 = (Button) findViewById(R.id.clear_trans);
                b3.setVisibility(View.INVISIBLE);
                break;
            case R.id.clear_engine:
                b2 = (Button) findViewById(R.id.engine_button);
                b2.setText("Тип двигателя");

                sPref.edit().putString("EngineType", "").commit();

                b3 = (Button) findViewById(R.id.clear_engine);
                b3.setVisibility(View.INVISIBLE);
                break;
            case R.id.clear_privod:
                b2 = (Button) findViewById(R.id.privod_button);
                b2.setText("Привод");

                sPref.edit().putString("Privod", "").commit();

                b3 = (Button) findViewById(R.id.clear_privod);
                b3.setVisibility(View.INVISIBLE);
                break;
            case R.id.clear_body:
                b2 = (Button) findViewById(R.id.body_button);
                b2.setText("Тип кузова");

                sPref.edit().putString("Body", "").commit();

                b3 = (Button) findViewById(R.id.clear_body);
                b3.setVisibility(View.INVISIBLE);
                break;
            default:
                break;

        }
    }
    public void onClickSettings(View v)
    {
        openOptionsMenu();
    }
    public void onClickMonitor(View v)
    {
        mTracker.send(new HitBuilders.EventBuilder().setCategory("Additional features").setAction("Open monitor's menu").build());
        final Dialog dialog = new Dialog(this);
        dialog.setTitle("Меню мониторов");
        dialog.setContentView(R.layout.find_monitor);

        SharedPreferences sPref = getSharedPreferences("SearchMyCarPreferences", Context.MODE_PRIVATE);
        String status = sPref.getString("SearchMyCarService_status", "false;false;false");
        final String[] stat = status.split(";");

        Button butMonTitle1 = (Button) dialog.findViewById(R.id.bot_mon1_title);
        Button butMonTitle2 = (Button) dialog.findViewById(R.id.bot_mon2_title);
        Button butMonTitle3 = (Button) dialog.findViewById(R.id.bot_mon3_title);
        if (stat[0].equals("true"))
            butMonTitle1.setText(Html.fromHtml("Монитор 1 <font color=green face=cursive>(запущен)</font>"));
        else{
            butMonTitle1.setClickable(false);
            butMonTitle1.setText(Html.fromHtml("Монитор 1 <font color=#D05555 face=cursive>(выключен)</font>"));
        }
        if (stat[1].equals("true"))
            butMonTitle2.setText(Html.fromHtml("Монитор 2 <font color=green face=cursive>(запущен)</font>"));
        else{
            butMonTitle2.setClickable(false);
            butMonTitle2.setText(Html.fromHtml("Монитор 2 <font color=#D05555 face=cursive>(выключен)</font>"));
        }
        if (stat[2].equals("true"))
            butMonTitle3.setText(Html.fromHtml("Монитор 3 <font color=green face=cursive>(запущен)</font>"));
        else{
            butMonTitle3.setClickable(false);
            butMonTitle3.setText(Html.fromHtml("Монитор 3 <font color=#D05555 face=cursive>(выключен)</font>"));
        }

        TextView tvDesc1 = (TextView) dialog.findViewById(R.id.tv_mon1_desc);
        if(!sPref.getString("SearchMyCarService_shortMessage1","###").equals("###"))
            tvDesc1.setText(sPref.getString("SearchMyCarService_shortMessage1", "###"));
        TextView tvDesc2 = (TextView) dialog.findViewById(R.id.tv_mon2_desc);
        if(!sPref.getString("SearchMyCarService_shortMessage2","###").equals("###"))
            tvDesc2.setText(sPref.getString("SearchMyCarService_shortMessage2","###"));
        TextView tvDesc3 = (TextView) dialog.findViewById(R.id.tv_mon3_desc);
        if(!sPref.getString("SearchMyCarService_shortMessage3","###").equals("###"))
            tvDesc3.setText(sPref.getString("SearchMyCarService_shortMessage3","###"));


        OnClickListener monitorClick = new OnClickListener()
        {
            @Override
            public void onClick(View v) {
                int monitorNumber = 0;
                switch(v.getId())
                {
                    case R.id.bot_mon1_title: monitorNumber=1;break;
                    case R.id.bot_mon2_title: monitorNumber=2;break;
                    case R.id.bot_mon3_title: monitorNumber=3;break;
                }
                if(stat[monitorNumber-1].equals("true"))
                {
                    Intent intent = new Intent(CreateRequestActivity.this, NotificationActivity.class);
                    intent.putExtra("NotificationMessage", monitorNumber);
                    startActivity(intent);
                }
                dialog.dismiss();
            }
        };
        butMonTitle1.setOnClickListener(monitorClick);
        butMonTitle2.setOnClickListener(monitorClick);
        butMonTitle3.setOnClickListener(monitorClick);

        dialog.show();
    }


}
