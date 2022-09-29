package com.arindo.nura;

import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by bmaxard on 26/09/2016.
 */
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "MyFirebaseInstanceIDService";
    @Override
    public void onTokenRefresh() {
        //super.onTokenRefresh();
        //getting registration token
        FirebaseApp.initializeApp(getBaseContext());
        String refreshToken = FirebaseInstanceId.getInstance().getToken();

        //Displaying token in Logcat
        Log.e("TAG", "Refreshed Token : " + refreshToken);
        //sendRegistrationToServer(refreshToken);

    }

    //private void sendRegistrationToServer(String token) {
        //You can implement this method to store the token on your server
        //Not required for current project
    //}
}
