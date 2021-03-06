package com.example.pocketbook.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.pocketbook.R;
import com.example.pocketbook.activity.HomeActivity;
import com.example.pocketbook.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static com.example.pocketbook.util.FirebaseIntegrity.getUserFromFirestore;

// FirebaseMessaging to handle sending notifications between users and displaying them
public class FirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        super.onMessageReceived(remoteMessage);
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            sendOreoNotification(remoteMessage);
        }
        else{
            sendNotification(remoteMessage);
        }

    }

    /**
     * method to send notifications for android oreo and above
     * @param remoteMessage: the message carrying the notification data
     */

    private void sendOreoNotification(RemoteMessage remoteMessage){
        // get the data of the notification
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String date = remoteMessage.getData().get("date");
        String group = remoteMessage.getData().get("group");
        String icon = remoteMessage.getData().get("icon");
        String receiver = remoteMessage.getData().get("receiver");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (receiver != null) {
            FirebaseFirestore.getInstance().collection("users").document(receiver)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                User user = getUserFromFirestore(document);

                                // functionality to click on a notification
                                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra("NOTI_FRAG", true);
                                intent.putExtra("CURRENT_USER", user);
                                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


                                OreoNotification oreoNotification = new OreoNotification(getApplicationContext());
                                Notification.Builder builder = oreoNotification.getOreoNotification(title, body, icon, group, pendingIntent);

                                // generate an id for the notification from its date
                                int j = 0;
                                String[] s = date.split("[-:.]");
                                for (int i = 0; i < s.length; i++) {
                                    j += Integer.parseInt(s[i].replace(" ", ""));
                                }

                                // show the notification and assign it a unique id so it does not get overwritten
                                oreoNotification.getManager().notify(j, builder.build());

                            } else {
                                Log.d("FAILED_TO_GET_USER_FROM_FIRESTORE", "get failed with ", task.getException());
                            }
                        }
                    });
        }

    }

    /**
     * method to send notifications for android before oreo
     * @param remoteMessage: message carrying notification data
     */
    private void sendNotification(RemoteMessage remoteMessage){
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String date = remoteMessage.getData().get("date");
        String icon = remoteMessage.getData().get("icon");
        RemoteMessage.Notification notification = remoteMessage.getNotification();


        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // generate an id for the notification from its date
        int j = 0;
        String[] s = date.split("[-:.]");
        for (int i = 0; i < s.length; i++) {
            j+= Integer.parseInt(s[i].replace(" ",""));
        }

        // show the notification and assign it a unique id so it does not get overwritten
        manager.notify(j, builder.build());

    }
}
