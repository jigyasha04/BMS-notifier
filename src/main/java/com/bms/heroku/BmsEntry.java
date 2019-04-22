package com.bms.heroku;

import com.bms.find.SearchForMovie;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BmsEntry {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    private static SearchForMovie searchMovie = new SearchForMovie();

    public static void wakeUpWatchman() {
        searchMovie.searchWithDetails();
        /*Runnable runnable = new Runnable() {
            public void run() {
                // task to run goes here
               if() {
                   //scheduler.shutdown();
                   DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                   LocalDateTime now = LocalDateTime.now();
                   System.out.println("Task Complete at "+ dtf.format(now));
               }

            }
        };*/
        //scheduler.scheduleAtFixedRate(runnable, 0, 5, TimeUnit.MINUTES);
    }

}