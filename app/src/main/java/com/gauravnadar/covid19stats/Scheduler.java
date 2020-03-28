package com.gauravnadar.covid19stats;

import android.app.Activity;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.Api;


import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Scheduler extends JobService {

Boolean jobCancelled = false;

    FileOutputStream out = null;
    FileOutputStream out2 = null;
    FileOutputStream out3 = null;
    FileOutputStream Dailyout = null;
    int task = 0;

    public static String TAG = "Job";
    private MainActivity.Worker2 worker;
    Handler handler = new Handler(Looper.getMainLooper()){

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Log.e("XXXXXXXXXXX", (String) msg.obj);
        }
    };


    @Override
    public boolean onStartJob(JobParameters jobParameters) {

worker = new MainActivity.Worker2(getApplicationContext());

        Log.d(TAG, "onStart");

       loadFiles(jobParameters);


        return true;
    }

    private void loadFiles(final JobParameters jobParameters) {


        //work here

        task = 0;

        if(jobCancelled)
        {

        }


        DateFormat df = new SimpleDateFormat("MM-dd-yyyy");
       Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        String date = df.format(cal.getTime());

       df.setTimeZone(TimeZone.getTimeZone("UTC"));
       final String date2 = df.format(cal.getTime());

        Log.e("date", date);
        Log.e("date", date2);



                worker.execute(new Runnable() {
                    @Override
                    public void run() {





                        try {
                            out = openFileOutput("stats.csv", MODE_PRIVATE);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        OkHttpClient client = new OkHttpClient();
                        String url = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

                        Request request = new Request.Builder()
                                .url(url)
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                Log.e("failed", e.getMessage());
                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                                //Log.e("Success", response.body().string());
                                final String data = response.body().string().toString();






                                try {
                            out.write(data.getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        finally {
                            if (out != null) {
                                try {
                                    out.close();
                                    //loadData();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        Message msg = Message.obtain();
                        msg.obj = "task1 is done";
                        handler.sendMessage(msg);
                    }
                });


           /*     new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            out.write(data.getBytes());
                            task++;
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (out != null) {
                                try {
                                    out.close();
                                    //loadData();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        Log.d(TAG, "done1");

                    }
                }).start();*/
            }
        });






                worker.execute(new Runnable() {
                    @Override
                    public void run() {




                        OkHttpClient client2 = new OkHttpClient();
                        String url2 = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_deaths_global.csv";

                        Request request2 = new Request.Builder()
                                .url(url2)
                                .build();


                        try {
                            out2 = openFileOutput("deaths.csv", MODE_PRIVATE);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        client2.newCall(request2).enqueue(new Callback() {
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {

                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {


                                final String data = response.body().string().toString();






                        try {
                            if(response.code()==200) {
                                out2.write(data.getBytes());
                                Log.e("error", "not null");
                            }
                            else
                            {
                                Log.e("error", "null");
                            }
                            task++;
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("error", e.getMessage());

                        } finally {
                            if (out2 != null) {
                                try {
                                    out2.close();

                                    //getDeaths();
                                } catch (IOException e) {
                                    e.printStackTrace();

                                }
                            }
else {

                            }
                        }
                        Message msg = Message.obtain();
                        msg.obj = "task2 is done";
                        handler.sendMessage(msg);

                    }
                });

        /*        new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            out2.write(data.getBytes());
                            task++;
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (out2 != null) {
                                try {
                                    out2.close();
                                    //getDeaths();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        Log.d(TAG, "done2");
                    }
                }).start();*/


            }


        });










                worker.execute(new Runnable() {
                    @Override
                    public void run() {




                        OkHttpClient client3 = new OkHttpClient();
                        String url3 = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_recovered_global.csv";

                        Request request3 = new Request.Builder()
                                .url(url3)
                                .build();


                        try {
                            out3 = openFileOutput("recovered.csv", MODE_PRIVATE);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        client3.newCall(request3).enqueue(new Callback() {
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {

                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {


                                final String data2 = response.body().string().toString();






                        try {
                            out3.write(data2.getBytes());
                            task++;
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (out3 != null) {
                                try {
                                    out3.close();
                                    //getRecovered();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        Message msg = Message.obtain();
                        msg.obj = "task3 is done";
                        handler.sendMessage(msg);

                    }
                });

         /*       new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            out3.write(data2.getBytes());
                            task++;
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (out3 != null) {
                                try {
                                    out3.close();
                                    //getRecovered();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        Log.d(TAG, "done3");
                    }
                }).start();*/
                ;
            }
        });





               worker.execute(new Runnable() {
                   @Override
                   public void run() {






                       try {
                           Dailyout = openFileOutput("daily.csv", MODE_PRIVATE);

                       } catch (FileNotFoundException e) {
                           e.printStackTrace();
                       }


                       OkHttpClient clientD = new OkHttpClient();
                       String urlD = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_daily_reports/"+date2+".csv";

                       Request requestD = new Request.Builder()
                               .url(urlD)
                               .build();

                       clientD.newCall(requestD).enqueue(new Callback() {
                           @Override
                           public void onFailure(@NotNull Call call, @NotNull IOException e) {
                               Log.e("failed", e.getMessage());
                           }

                           @Override
                           public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                               //Log.e("Success", response.body().string());
                               final String dataD = response.body().string().toString();








                               try {
                           Dailyout.write(dataD.getBytes());
                           task++;
                       } catch (IOException e) {
                           e.printStackTrace();
                       } finally {
                           if (out != null) {
                               try {
                                   Dailyout.close();

                                   //                loadDailyReports();

                               } catch (IOException e) {
                                   e.printStackTrace();
                               }
                           }
                       }
                       Message msg = Message.obtain();
                       msg.obj = "task4 is done";
                       handler.sendMessage(msg);
                               jobFinished(jobParameters, false);
                               worker.quit();

                   }
               });

               /* new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            Dailyout.write(dataD.getBytes());
                            task++;
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (out != null) {
                                try {
                                    Dailyout.close();

                    //                loadDailyReports();

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        Log.d(TAG, "done4");

                    }
                }).start();*/




            }



        });






    }



    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d(TAG, "cancelled");
        jobCancelled = true;
        return true;
    }






}
