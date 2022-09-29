package com.arindo.nura;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by bmaxard on 26/09/2016.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMessagingService";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
    //super.onMessageReceived(remoteMessage);
    //It is Optional
        Log.e("TAG", "From : " + remoteMessage.getFrom());
        Log.e("TAG", "Notification Message Body : " + remoteMessage.getNotification().getBody());
        //sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());

        /*if(remoteMessage.getData().containsKey("title") && remoteMessage.getData().containsKey("msg")) {
            Log.e("Post Title", remoteMessage.getData().get("title").toString());
            Log.e("Post Msg", remoteMessage.getData().get("msg").toString());
            Log.e("Post Jenis Message", remoteMessage.getData().get("jmsg").toString());

            String jmsg = remoteMessage.getData().get("jmsg").toString();
            if(jmsg.equals("reqcsrprogress")) {
                String a = remoteMessage.getData().get("tiket").toString();
                String b = remoteMessage.getData().get("status").toString();
                try {
                    //UpdateStatusRequest update = new UpdateStatusRequest();
                    //if (update.updateStatus(this, a, b) == true) {
                        sendNotification(remoteMessage.getData().get("title").toString(), remoteMessage.getData().get("msg").toString());
                    //}
                } catch (Exception e) {
                    Log.e("Error", "Notifikasi Status Progress" + e.toString());
                }
            }
        }*/
    }
    /**
     * this method is only generating push notification
     */
    private void sendNotification(String title, String messageBody){
        Intent intent = new Intent(this, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("push", 1); // notifikasi progres (status) permohonan
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_grey_ride)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0,notificationBuilder.build());
    }
}
