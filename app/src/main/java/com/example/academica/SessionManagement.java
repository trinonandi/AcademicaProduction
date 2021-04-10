package com.example.academica;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;


public class SessionManagement {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    public static final String LOGIN="login";
    private String session;
        public SessionManagement(Context context)
        {
            sharedPreferences = context.getSharedPreferences(LOGIN,MODE_PRIVATE);
            editor = sharedPreferences.edit();

            editor.putString(LOGIN,LOGIN);
            editor.commit();
            editor.apply();

        }

        public void setLogin(String LOGIN) {

            this.session = session;
            editor.putString(LOGIN,LOGIN);
            editor.commit();
            editor.apply();


        }

        public String getLogin(){
            return sharedPreferences.getString(LOGIN,"");

        }
    }

