package com.radar.radarshop;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREFS = "auth";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_EMAIL = "email";

    private final SharedPreferences sp;

    public SessionManager(Context ctx) {
        sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void login(String email) {
        sp.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_EMAIL, email == null ? "" : email.trim())
                .apply();
    }

    public void logout() {
        sp.edit()
                .putBoolean(KEY_LOGGED_IN, false)
                .remove(KEY_EMAIL)
                .apply();
    }

    public boolean isLoggedIn() {
        return sp.getBoolean(KEY_LOGGED_IN, false);
    }

    public String getEmail() {
        return sp.getString(KEY_EMAIL, "");
    }
}
