package com.radar.radarshop;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "radarshop.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_USERS   = "users";
    public static final String COL_ID        = "id";
    public static final String COL_FIRST     = "firstname";
    public static final String COL_LAST      = "lastname";
    public static final String COL_EMAIL     = "email";
    public static final String COL_PASSWORD  = "password_hash";
    public static final String COL_PHONE     = "phone";
    public static final String COL_STREET    = "street";
    public static final String COL_CITY      = "city";
    public static final String COL_STATE     = "state";
    public static final String COL_ZIP       = "zip";
    public static final String COL_COUNTRY   = "country";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + TABLE_USERS + " (" +
                        COL_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_FIRST    + " TEXT, " +
                        COL_LAST     + " TEXT, " +
                        COL_EMAIL    + " TEXT UNIQUE COLLATE NOCASE, " +
                        COL_PASSWORD + " TEXT, " +
                        COL_PHONE    + " TEXT, " +
                        COL_STREET   + " TEXT, " +
                        COL_CITY     + " TEXT, " +
                        COL_STATE    + " TEXT, " +
                        COL_ZIP      + " TEXT, " +
                        COL_COUNTRY  + " TEXT" +
                        ");"
        );
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_users_email ON " + TABLE_USERS + " (" + COL_EMAIL + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    /* -------------------- Auth API -------------------- */

    /** Inserts a user. Returns true on success, false if email exists or error. */
    public boolean insertUser(String first, String last, String email, String plainPassword) {
        String normEmail = normalizeEmail(email);
        if (normEmail.isEmpty()) return false;
        if (userExists(normEmail)) return false;

        String hashed = hashPassword(plainPassword);
        if (hashed == null) return false;

        ContentValues cv = new ContentValues();
        cv.put(COL_FIRST, nonNull(first));
        cv.put(COL_LAST, nonNull(last));
        cv.put(COL_EMAIL, normEmail);
        cv.put(COL_PASSWORD, hashed);

        long rowId = -1;
        SQLiteDatabase db = getWritableDatabase();
        try {
            rowId = db.insert(TABLE_USERS, null, cv);
        } catch (Exception ignored) {}
        return rowId != -1;
    }

    /** Validates a user by email + password. */
    public boolean validateUser(String email, String plainPassword) {
        String normEmail = normalizeEmail(email);
        if (normEmail.isEmpty()) return false;

        String hashed = hashPassword(plainPassword);
        if (hashed == null) return false;

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = null;
        try {
            c = db.query(
                    TABLE_USERS,
                    new String[]{COL_PASSWORD},
                    COL_EMAIL + "=?",
                    new String[]{normEmail},
                    null, null, null
            );
            if (c != null && c.moveToFirst()) {
                String stored = c.getString(0);
                return hashed.equals(stored);
            }
        } finally {
            if (c != null) c.close();
        }
        return false;
    }

    public boolean userExists(String email) {
        String normEmail = normalizeEmail(email);
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = null;
        try {
            c = db.query(TABLE_USERS, new String[]{COL_ID}, COL_EMAIL + "=?", new String[]{normEmail}, null, null, null);
            return c != null && c.moveToFirst();
        } finally {
            if (c != null) c.close();
        }
    }

    /* -------------------- Profile API -------------------- */

    /** Simple DTO for user profile. */
    public static class UserProfile {
        public final String first;
        public final String last;
        public final String email;
        public final String phone;
        public final String street;
        public final String city;
        public final String state;
        public final String zip;
        public final String country;

        public UserProfile(String first, String last, String email,
                           String phone, String street, String city,
                           String state, String zip, String country) {
            this.first = first;
            this.last = last;
            this.email = email;
            this.phone = phone;
            this.street = street;
            this.city = city;
            this.state = state;
            this.zip = zip;
            this.country = country;
        }

        public String fullName() {
            String f = (first == null ? "" : first.trim());
            String l = (last  == null ? "" : last.trim());
            return (f + " " + l).trim();
        }
    }

    /** Load profile by email. Returns null if not found. */
    public UserProfile getUserProfile(String email) {
        String normEmail = normalizeEmail(email);
        if (normEmail.isEmpty()) return null;

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = null;
        try {
            c = db.query(
                    TABLE_USERS,
                    new String[]{COL_FIRST, COL_LAST, COL_EMAIL, COL_PHONE, COL_STREET, COL_CITY, COL_STATE, COL_ZIP, COL_COUNTRY},
                    COL_EMAIL + "=?",
                    new String[]{normEmail},
                    null, null, null
            );
            if (c != null && c.moveToFirst()) {
                return new UserProfile(
                        c.getString(0), // first
                        c.getString(1), // last
                        c.getString(2), // email
                        c.getString(3), // phone
                        c.getString(4), // street
                        c.getString(5), // city
                        c.getString(6), // state
                        c.getString(7), // zip
                        c.getString(8)  // country
                );
            }
        } finally {
            if (c != null) c.close();
        }
        return null;
    }

    /** Update core profile fields for a user. */
    public boolean updateProfile(String email, String first, String last, String phone,
                                 String street, String city, String state, String zip, String country) {
        String normEmail = normalizeEmail(email);
        if (normEmail.isEmpty()) return false;

        ContentValues cv = new ContentValues();
        cv.put(COL_FIRST, nonNull(first));
        cv.put(COL_LAST, nonNull(last));
        cv.put(COL_PHONE, nonNull(phone));
        cv.put(COL_STREET, nonNull(street));
        cv.put(COL_CITY, nonNull(city));
        cv.put(COL_STATE, nonNull(state));
        cv.put(COL_ZIP, nonNull(zip));
        cv.put(COL_COUNTRY, nonNull(country));

        SQLiteDatabase db = getWritableDatabase();
        int rows = db.update(TABLE_USERS, cv, COL_EMAIL + "=?", new String[]{normEmail});
        return rows > 0;
    }

    /** Change password with aligned hashing. */
    public boolean changePassword(String email, String newPlainPassword) {
        String normEmail = normalizeEmail(email);
        String hashed = hashPassword(newPlainPassword);
        if (normEmail.isEmpty() || hashed == null) return false;

        ContentValues cv = new ContentValues();
        cv.put(COL_PASSWORD, hashed);

        SQLiteDatabase db = getWritableDatabase();
        int rows = db.update(TABLE_USERS, cv, COL_EMAIL + "=?", new String[]{normEmail});
        return rows > 0;
    }

    /* -------------------- Helpers -------------------- */

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.US);
    }

    private static String nonNull(String s) {
        return s == null ? "" : s.trim();
    }

    private static String hashPassword(String plain) {
        if (plain == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(plain.getBytes());
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
