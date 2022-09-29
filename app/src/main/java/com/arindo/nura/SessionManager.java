package com.arindo.nura;

import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SessionManager {
	// Shared Preferences reference
		SharedPreferences pref;
		// Editor reference for Shared preferences
		Editor editor;
		// Context
		Context _context;
		// Shared pref mode
		int PRIVATE_MODE = 0;
		
		// Sharedpref file name
	    private static final String PREFER_NAME = "AndroidPref";
	    // All Shared Preferences Keys
	    private static final String IS_TMP_ORDER = "IsTmpOrder";
	    // User name (make variable public to access from outside)
		public static final String KEY_ID = "id";
	    public static final String KEY_QTY = "qty";
	    public static final String KEY_PRICE = "price";
		
		// Constructor
		public SessionManager(Context context){
	        this._context = context;
	        pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
	        editor = pref.edit();
	    }
		
		//Create login session
	    public void createTmpOrderSession(String id, String qty, String price){
	        // Storing tmp value as TRUE
	        editor.putBoolean(IS_TMP_ORDER, true);
	        // Storing name in pref
			editor.putString(KEY_ID, id);
	        editor.putString(KEY_QTY, qty);
	        editor.putString(KEY_PRICE, price);
	        editor.commit();
	    }   
	    
	    /*public boolean checkLogin(){
	        // Check login status
	        if(!this.isUserLoggedIn()){
	            // user is not logged in redirect him to Login Activity
	            Intent i = new Intent(_context, Login.class);
	            // Closing all the Activities from stack
	            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            // Add new Flag to start new Activity
	            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            // Staring Login Activity
	            _context.startActivity(i);
	            return true;
	        }
	        return false;
	    }*/
	    
	    public HashMap<String, String> getTmpDetails(){
	        //Use hashmap to store tmp credentials
	        HashMap<String, String> tmp = new HashMap<String, String>();
			tmp.put(KEY_ID, pref.getString(KEY_ID, null));
			tmp.put(KEY_QTY, pref.getString(KEY_QTY, null));
			tmp.put(KEY_PRICE, pref.getString(KEY_PRICE, null));
	        return tmp;
	    }
	    
	    /*public void logoutUser(){
	        // Clearing all user data from Shared Preferences
	        editor.clear();
	        editor.commit();
	        // After logout redirect user to Login Activity
	        Intent i = new Intent(_context, Login.class);
	        // Closing all the Activities
	        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        // Add new Flag to start new Activity
	        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        // Staring Login Activity
	        _context.startActivity(i);
	    }*/
	    
	    // Check for temporari
	    public boolean IsTmpOrder(){
	        return pref.getBoolean(IS_TMP_ORDER, false);
	    }
}
