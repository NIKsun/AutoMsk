package com.develop.searchmycarandroid;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.InterstitialAd;

import java.text.SimpleDateFormat;

public class ListViewAdapter extends BaseAdapter{
    Context context;
    Cars cars;
    Bitmap[] images;
    String lastCarDateAuto, lastCarDateAvito, lastCarIdDrom;
    Boolean isFromMonitor;
    InterstitialAd mInterstitialAd;
    Integer counterIdDrom = 0, startDromPosition;
    Boolean notBlockToWriteStartDromPosition = true;


    private static LayoutInflater inflater=null;
    public ListViewAdapter(NotificationActivity mainActivity, Cars c, Bitmap[] imgs, String lastCarDateAuto, String lastCarDateAvito, String lastCarIdDrom, InterstitialAd mAd) {
        // TODO Auto-generated constructor stub
        context=mainActivity;
        this.lastCarDateAuto=lastCarDateAuto;
        this.lastCarDateAvito=lastCarDateAvito;
        this.lastCarIdDrom = lastCarIdDrom;
        images = imgs;
        cars = c;
        isFromMonitor = true;
        mInterstitialAd = mAd;

        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    public ListViewAdapter(ListOfCars mainActivity, Cars c, Bitmap[] imgs, InterstitialAd mAd) {
        // TODO Auto-generated constructor stub
        context=mainActivity;
        images = imgs;
        cars = c;
        isFromMonitor = false;
        mInterstitialAd = mAd;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return cars.getLength();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder
    {
        TextView tvt;
        TextView tv;
        ImageView img;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder=new Holder();
        View rowView = null;
        if(cars.cars[position].id.equals("separator"))
        {
            rowView = inflater.inflate(R.layout.separator, null);
            rowView.setClickable(false);
            holder.tv = (TextView)rowView.findViewById(R.id.siteName);
            holder.tv.setText(cars.cars[position].href);
            holder.tvt = (TextView)rowView.findViewById(R.id.countOfCars);
            holder.tvt.setText(cars.cars[position].mileage);
        }
        else {
            rowView = inflater.inflate(R.layout.listview, null);
            if (isFromMonitor) {
                if (cars.carFromAuto(position)) {
                    if (lastCarDateAuto.equals("###"))
                        rowView.setBackgroundColor(0xFFC1E1FF);
                    else {
                        if (Long.parseLong(lastCarDateAuto) / 1000 < cars.getCarDateLong(position) / 1000) {//New cars
                            rowView.setBackgroundColor(0xFFC1E1FF);
                        }
                    }
                } else if(cars.carFromAvito(position)) {
                    if (lastCarDateAvito.equals("###"))
                        rowView.setBackgroundColor(0xFFC1E1FF);
                    else {
                        if (Long.parseLong(lastCarDateAvito) / 1000 < cars.getCarDateLong(position) / 1000) {    //New cars
                            rowView.setBackgroundColor(0xFFC1E1FF);
                        }
                    }
                }
                else
                {
                    if(notBlockToWriteStartDromPosition)
                    {
                        startDromPosition=position;
                        notBlockToWriteStartDromPosition=false;
                        while (!lastCarIdDrom.equals(cars.cars[startDromPosition+counterIdDrom].id)) {
                            if(cars.cars[startDromPosition+counterIdDrom].id.equals("separator"))
                                break;
                            counterIdDrom++;
                            if(startDromPosition+counterIdDrom == cars.getLength())
                                break;
                        }
                    }
                    if (lastCarIdDrom.equals("###") || position<startDromPosition+counterIdDrom)
                        rowView.setBackgroundColor(0xFFC1E1FF);
                }
            }
            holder.tv = (TextView) rowView.findViewById(R.id.textView1);
            holder.img = (ImageView) rowView.findViewById(R.id.imageView1);
            SimpleDateFormat format;
            if(cars.getCarDate(position).getHours() == 0 && cars.getCarDate(position).getMinutes() == 0 && cars.getCarDate(position).getSeconds() == 0)
                format = new SimpleDateFormat("dd.MM.yyyy");
            else
                format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            holder.tvt = (TextView) rowView.findViewById(R.id.textViewTime);
            holder.tvt.setText(format.format(cars.getCarDate(position)));
            holder.tv.setText(Html.fromHtml(cars.getMessage(position)));
            holder.tv.setLinksClickable(true);
            holder.img.setImageBitmap(images[position]);

            rowView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub

                    Intent intent = new Intent(context, CarPage.class);
                    intent.putExtra("url", cars.getHref(position));
                    context.startActivity(intent);
                    if (mInterstitialAd.isLoaded())
                        mInterstitialAd.show();
                }
            });
        }
        return rowView;
    }



}