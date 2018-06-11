package pt.ipleiria.markmyrhythm.Model;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

import pt.ipleiria.markmyrhythm.Activitty.NewChallengeActivity;
import pt.ipleiria.markmyrhythm.R;

public class AlarmReceiver extends BroadcastReceiver
{
    public static volatile AlarmReceiver instance = null;
    private static String channelId = "1";
    private static NotificationCompat.Builder mBuilder;

    synchronized public static AlarmReceiver getInstance() {
        if(instance == null) {
            instance = new AlarmReceiver();
        }
        return instance;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //Wake up every day
        //this.scheduleAlarm(context, 24);
        Log.i("DEBUG", "Alarm fired");
        NewChallengeActivity.getHourActivityLastWeek_2(context);
    }


    public void cancelNotification(Context context)
    {
        Intent intent = new Intent(context, NotificationAlarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(sender);
        }
    }

    /**
     * Schedule Alarm in the specified time after the current time
     * @param context Application Context
     * @param hours Hours after the current time is going to ring the Alarm
     */
    public void scheduleAlarm(Context context, int hours)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        //  System.out.println("TIME: ");
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinutes = calendar.get(Calendar.MINUTE);
        // System.out.println(currentHour);
        //  System.out.println(currentMinutes);

        int diffHours = (24-currentHour) + 2;
        int diffMinutes = 1;//60-currentMinutes;
        //System.out.println("FALTAM " + diffHours + " HORAS  E " + diffMinutes + " MINUTOS");
        calendar.add(Calendar.HOUR, diffHours);
        calendar.add(Calendar.MINUTE, diffMinutes);

       // System.out.println("MILLIS: " + calendar.getTimeInMillis());
        long millisInterval = hours * (60 * 60 * 1000); ; // 24 Horas
        Intent intentAlarm = new Intent(context, AlarmReceiver.class);
        // Get the Alarm Service.
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(alarmManager != null){
            // Set the alarm for a particular time.
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), millisInterval, PendingIntent.getBroadcast(context, 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT) );
        }
        Log.i("DEBUG", "Alarm Scheduled");
    }


}