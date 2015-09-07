package com.develop.searchmycarandroid;

import android.annotation.TargetApi;
import android.os.Build;

import org.jsoup.nodes.Element;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Никита on 22.07.2015.
 */


public class Cars {
    class Car {
        String id;
        String href;
        String img;
        String message;
        String price;
        String mileage;
        String year;
        String city;
        Date timeOfCreate;
        Boolean isFromAuto;
        boolean sorted;
    }

    Car[] cars;
    int capacity;
    int lastCar;

    public Cars(int len)
    {
        cars = new Car[len];
        capacity = len;
        lastCar = 0;
    }
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public boolean addFromAutoRu(Element elem)
    {
        if(lastCar >= capacity)
            return false;
        Car currentCar = new Car();
        currentCar.isFromAuto = true;
        if(elem == null){
            return false;
        }
        Pattern pattern = Pattern.compile("card_id\":\"([0-9]+).+created\":([^,]+)");
        Matcher matcher = pattern.matcher(elem.attr("data-stat_params"));
        if(matcher.find()){
            currentCar.id = matcher.group(1);
            //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //try {
                //currentCar.timeOfCreate = format.parse(matcher.group(2));
                currentCar.timeOfCreate = new Date(Long.parseLong(matcher.group(2)));
            //} catch (ParseException e) {
            //    e.printStackTrace();
           // }
        }
        else
            return false;
        currentCar.timeOfCreate.setSeconds(0);

        currentCar.href = elem.select("td.sales-list-cell.sales-list-cell_images > a").first().attr("href");
        currentCar.img = elem.select("td.sales-list-cell.sales-list-cell_images > a > img").first().attr("data-original");
        if(currentCar.img.isEmpty() == true)
            currentCar.img = elem.select("td.sales-list-cell.sales-list-cell_images > a > img").first().attr("src");

        if (currentCar.img.indexOf("/i/all7/img/no-photo-thumb.png") != -1)
            currentCar.img = "http://auto.ru/i/all7/img/no-photo-thumb.png";

        currentCar.message = elem.select("td.sales-list-cell.sales-list-cell_mark_id").first().text();
        currentCar.price =  elem.select("td.sales-list-cell.sales-list-cell_price").first().text();
        currentCar.year =  elem.select("td.sales-list-cell.sales-list-cell_year").first().text();
        currentCar.mileage=  elem.select("td.sales-list-cell.sales-list-cell_run").first().text();
        currentCar.city =  elem.select("td.sales-list-cell.sales-list-cell_poi_id > div.sales-list-region.ico-appear").first().text();
        cars[lastCar] = currentCar;
        lastCar++;
        return true;
    }
    public static Date getDateAuto(Element elem)
    {
        if(elem == null){
            return null;
        }
        Pattern pattern = Pattern.compile("created\":([^,]+)");
        Matcher matcher = pattern.matcher(elem.attr("data-stat_params"));
        if(matcher.find()){
            //SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //try {
            //    Date buf = format.parse(matcher.group(1));
                Date buf = new Date(Long.parseLong(matcher.group(1)));
                buf.setSeconds(0);
                return buf;
            //} catch (ParseException e) {
            //    e.printStackTrace();
            //}
        }
        return null;
    }
    public static Date getDateAvito(Element elem)
    {
        String[] date = elem.select("div.description > div.data > div").text().split(" ");
        Date result = new Date();
        if(date.length == 2)
        {
            if(date[0].equals("Сегодня"))
            {
                result.setMinutes(Integer.parseInt(date[1].split(":")[1]));
                result.setHours(Integer.parseInt(date[1].split(":")[0]));
            }
            else {
                result = new Date(System.currentTimeMillis() - 1000L * 60L * 60L * 24L);
                result.setMinutes(Integer.parseInt(date[1].split(":")[1]));
                result.setHours(Integer.parseInt(date[1].split(":")[0]));
            }
        }
        else
        {
            switch (date[1])
            {
                case "января": result.setMonth(0); break;
                case "февраля": result.setMonth(1); break;
                case "марта": result.setMonth(2); break;
                case "апреля": result.setMonth(3); break;
                case "мая": result.setMonth(4); break;
                case "июня": result.setMonth(5); break;
                case "июля": result.setMonth(6); break;
                case "августа": result.setMonth(7); break;
                case "сентября": result.setMonth(8); break;
                case "октября": result.setMonth(9); break;
                case "ноября": result.setMonth(10); break;
                case "декабря": result.setMonth(11); break;
            }
            result.setMinutes(Integer.parseInt(date[2].split(":")[1]));
            result.setHours(Integer.parseInt(date[2].split(":")[0]));
            result.setDate(Integer.parseInt(date[0]));
        }
        result.setSeconds(0);
        return result;
    }
    public long getCarDateLong(int i)
    {
        return cars[i].timeOfCreate.getTime();
    }
    public Date getCarDate(int i)
    {
        return cars[i].timeOfCreate;
    }
    public String getMessage(int pos)
    {
        String message = "";
        message  += "<h6><font face=fantasy color=#08088A>" + cars[pos].message + "</font></h6>";
        message += "<h6>Цена: " + cars[pos].price +"</h6>";
        message += "<font color=#585858>Год: " + cars[pos].year;
        message += "<br>Пробег: " + cars[pos].mileage;
        message += "<br>Место: " + cars[pos].city + "</font>";
        return message;
    }

    public void sortByDateAvito()
    {
        int pointer = 0;
        while(cars[pointer].sorted)
            pointer++;
        Car[] result = new Car[getLenth()];
        int counter1 = pointer, counter2 = getLenth()-pointer,i=0;
        while ((counter1 != 0) && (counter2 != 0))
        {
            if(cars[pointer-counter1].timeOfCreate.after(cars[getLenth() - counter2].timeOfCreate)) {
                result[i] = cars[pointer - counter1];
                counter1--;
            }
            else {
                result[i] = cars[getLenth() - counter2];
                counter2--;
            }
            i++;
        }
        while(counter1 != 0)
        {
            result[i] = cars[pointer - counter1];
            counter1--;
            i++;
        }
        while(counter2 != 0)
        {
            result[i] = cars[getLenth() - counter2];
            counter2--;
            i++;
        }
        cars = result;
    }

    public static Cars merge(Cars carsAvto, Cars carsAvito)
    {
        Cars result = new Cars(carsAvto.getLenth() + carsAvito.getLenth());
        int counter1 = carsAvto.getLenth(), counter2 = carsAvito.getLenth(),i=0;
        while ((counter1 != 0) && (counter2 != 0))
        {
            if(carsAvto.cars[carsAvto.getLenth()-counter1].timeOfCreate.after(carsAvito.cars[carsAvito.getLenth() - counter2].timeOfCreate)) {
                result.cars[i] = carsAvto.cars[carsAvto.getLenth() - counter1];
                counter1--;
            }
            else {
                result.cars[i] = carsAvito.cars[carsAvito.getLenth() - counter2];
                counter2--;
            }
            i++;
        }
        while(counter1 != 0)
        {
            result.cars[i] = carsAvto.cars[carsAvto.getLenth() - counter1];
            counter1--;
            i++;
        }
        while(counter2 != 0)
        {
            result.cars[i] = carsAvito.cars[carsAvito.getLenth() - counter2];
            counter2--;
            i++;
        }
        result.lastCar = carsAvto.getLenth() + carsAvito.getLenth();
        return result;
    }

    public boolean carFromAuto(int position)
    {
        return cars[position].isFromAuto;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public boolean addFromAvito(Element elem)
    {
        Car currentCar = new Car();
        currentCar.isFromAuto = false;
        if(elem == null){
            return false;
        }
        currentCar.id = elem.attr("id");
        currentCar.img = elem.select("div.b-photo > a > img").attr("data-srcpath");
        if(currentCar.img.isEmpty())
            currentCar.img = elem.select("div.b-photo > a > img").attr("src");
        if(currentCar.img.isEmpty())
            currentCar.img = "//auto.ru/i/all7/img/no-photo-thumb.png";

        currentCar.img = "http:" + currentCar.img;

        currentCar.href = "https://www.avito.ru" + elem.select("div.description > h3 > a").attr("href");


        currentCar.sorted = elem.select("div.description").first().children().hasClass("vas-applied");

        currentCar.message = elem.select("div.description > h3 > a").text();
        currentCar.year = currentCar.message.split(", ")[1];
        currentCar.message = currentCar.message.split(", ")[0];

        String buf = elem.select("div.description > div.about").text();
        if(buf.split("\\.")[0].endsWith("руб")) {
            currentCar.price = buf.split("\\.")[0];
            buf = buf.substring(buf.indexOf('.'));
        }
        else
            currentCar.price = "не указана";

        Pattern pattern = Pattern.compile("([0-9]|\\s)+км");
        Matcher matcher = pattern.matcher(buf);
        if(matcher.find()) {
            currentCar.mileage = matcher.group(0);
            currentCar.message += buf.substring(buf.indexOf(currentCar.mileage)+currentCar.mileage.length());
        }
        else {
            currentCar.mileage = "не указан";
            currentCar.message += buf;
        }

        String[] date = elem.select("div.description > div.data > div").text().split(" ");
        if(date.length == 2)
        {
            if(date[0].equals("Сегодня"))
            {
                currentCar.timeOfCreate = new Date();
                currentCar.timeOfCreate.setMinutes(Integer.parseInt(date[1].split(":")[1]));
                currentCar.timeOfCreate.setHours(Integer.parseInt(date[1].split(":")[0]));
            }
            else {
                currentCar.timeOfCreate = new Date(System.currentTimeMillis() - 1000L * 60L * 60L * 24L);
                currentCar.timeOfCreate.setMinutes(Integer.parseInt(date[1].split(":")[1]));
                currentCar.timeOfCreate.setHours(Integer.parseInt(date[1].split(":")[0]));
            }
        }
        else
        {
            currentCar.timeOfCreate = new Date();
            switch (date[1])
            {
                case "января": currentCar.timeOfCreate.setMonth(0); break;
                case "февраля": currentCar.timeOfCreate.setMonth(1); break;
                case "марта": currentCar.timeOfCreate.setMonth(2); break;
                case "апреля": currentCar.timeOfCreate.setMonth(3); break;
                case "мая": currentCar.timeOfCreate.setMonth(4); break;
                case "июня": currentCar.timeOfCreate.setMonth(5); break;
                case "июля": currentCar.timeOfCreate.setMonth(6); break;
                case "августа": currentCar.timeOfCreate.setMonth(7); break;
                case "сентября": currentCar.timeOfCreate.setMonth(8); break;
                case "октября": currentCar.timeOfCreate.setMonth(9); break;
                case "ноября": currentCar.timeOfCreate.setMonth(10); break;
                case "декабря": currentCar.timeOfCreate.setMonth(11); break;
            }
            currentCar.timeOfCreate.setMinutes(Integer.parseInt(date[2].split(":")[1]));
            currentCar.timeOfCreate.setHours(Integer.parseInt(date[2].split(":")[0]));
            currentCar.timeOfCreate.setDate(Integer.parseInt(date[0]));
        }
        currentCar.timeOfCreate.setSeconds(0);

        if(!elem.select("div.description > div.data > p:nth-child(2)").text().isEmpty())
            currentCar.city = elem.select("div.description > div.data > p:nth-child(2)").text();
        else
            currentCar.city = "не указано";

        cars[lastCar] = currentCar;
        lastCar++;
        return true;
    }

    public String getHref(int pos)
    {
        return cars[pos].href;
    }
    public String getImg(int pos)
    {
        return cars[pos].img;
    }
    public int getLenth()
    {
        return lastCar;
    }
}
