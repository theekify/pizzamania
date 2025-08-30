package com.example.pizzamania;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "pizzamania.db";
    public static final int DB_VERSION = 7; // Updated version for profile image

    // Users table
    public static final String T_USERS = "users";
    public static final String C_ID = "id";
    public static final String C_NAME = "name";
    public static final String C_EMAIL = "email";
    public static final String C_PASS = "password";
    public static final String C_IS_ADMIN = "is_admin";
    public static final String C_PROFILE_IMAGE = "profile_image"; // New column

    // Branches table
    public static final String T_BRANCHES = "branches";
    public static final String B_ID = "id";
    public static final String B_NAME = "name";
    public static final String B_ADDRESS = "address";
    public static final String B_PHONE = "phone";
    public static final String B_LATITUDE = "latitude";
    public static final String B_LONGITUDE = "longitude";

    private static final String[] ADMIN_EMAILS = {"admin1@pizzamania.com", "admin2@pizzamania.com"};
    private static final String[] ADMIN_NAMES = {"Admin One", "Admin Two"};
    private static final String ADMIN_PASSWORD = "admin123";

    public DatabaseHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Users
        db.execSQL("CREATE TABLE " + T_USERS + " (" +
                C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                C_NAME + " TEXT, " +
                C_EMAIL + " TEXT UNIQUE, " +
                C_PASS + " TEXT, " +
                C_IS_ADMIN + " INTEGER DEFAULT 0, " +
                C_PROFILE_IMAGE + " TEXT)");

        // Branches with coordinates
        db.execSQL("CREATE TABLE " + T_BRANCHES + " (" +
                B_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                B_NAME + " TEXT, " +
                B_ADDRESS + " TEXT, " +
                B_PHONE + " TEXT, " +
                B_LATITUDE + " REAL, " +
                B_LONGITUDE + " REAL)");

        insertDefaultAdmins(db);
        insertDefaultBranches(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + T_USERS + " ADD COLUMN " + C_IS_ADMIN + " INTEGER DEFAULT 0");
            insertDefaultAdmins(db);
        }
        if (oldVersion < 5) {
            db.execSQL("DROP TABLE IF EXISTS pizzas");
        }
        if (oldVersion < 6) {
            // Add latitude and longitude columns
            db.execSQL("ALTER TABLE " + T_BRANCHES + " ADD COLUMN " + B_LATITUDE + " REAL");
            db.execSQL("ALTER TABLE " + T_BRANCHES + " ADD COLUMN " + B_LONGITUDE + " REAL");
            insertDefaultBranches(db);
        }
        if (oldVersion < 7) {
            // Add profile image column
            db.execSQL("ALTER TABLE " + T_USERS + " ADD COLUMN " + C_PROFILE_IMAGE + " TEXT");
        }
    }

    private void insertDefaultAdmins(SQLiteDatabase db) {
        for (int i = 0; i < ADMIN_EMAILS.length; i++) {
            Cursor cursor = db.rawQuery("SELECT 1 FROM " + T_USERS + " WHERE " + C_EMAIL + "=?",
                    new String[]{ADMIN_EMAILS[i]});

            if (!cursor.moveToFirst()) {
                ContentValues cv = new ContentValues();
                cv.put(C_NAME, ADMIN_NAMES[i]);
                cv.put(C_EMAIL, ADMIN_EMAILS[i]);
                cv.put(C_PASS, ADMIN_PASSWORD);
                cv.put(C_IS_ADMIN, 1);
                cv.put(C_PROFILE_IMAGE, ""); // Empty profile image
                db.insert(T_USERS, null, cv);
            }
            cursor.close();
        }
    }

    private void insertDefaultBranches(SQLiteDatabase db) {
        // Colombo Branch
        ContentValues colombo = new ContentValues();
        colombo.put(B_NAME, "Colombo Branch");
        colombo.put(B_ADDRESS, "123 Galle Road, Colombo");
        colombo.put(B_PHONE, "011-2345678");
        colombo.put(B_LATITUDE, 6.9271);
        colombo.put(B_LONGITUDE, 79.8612);
        db.insertWithOnConflict(T_BRANCHES, null, colombo, SQLiteDatabase.CONFLICT_IGNORE);

        // Galle Branch
        ContentValues galle = new ContentValues();
        galle.put(B_NAME, "Galle Branch");
        galle.put(B_ADDRESS, "456 Main Street, Galle");
        galle.put(B_PHONE, "091-2345678");
        galle.put(B_LATITUDE, 6.0535);
        galle.put(B_LONGITUDE, 80.2210);
        db.insertWithOnConflict(T_BRANCHES, null, galle, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void ensureAdminAccountsExist() {
        insertDefaultAdmins(this.getWritableDatabase());
    }

    public boolean userExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            String query = "SELECT 1 FROM " + T_USERS + " WHERE " + C_EMAIL + " = ?";
            cursor = db.rawQuery(query, new String[]{email});
            return cursor.moveToFirst();
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    public boolean insertUser(String name, String email, String pass, boolean isAdmin) {
        ContentValues cv = new ContentValues();
        cv.put(C_NAME, name);
        cv.put(C_EMAIL, email);
        cv.put(C_PASS, pass);
        cv.put(C_IS_ADMIN, isAdmin ? 1 : 0);
        cv.put(C_PROFILE_IMAGE, ""); // Empty profile image
        return getWritableDatabase().insert(T_USERS, null, cv) != -1;
    }

    public boolean checkLogin(String email, String pass) {
        Cursor c = getReadableDatabase().rawQuery("SELECT 1 FROM " + T_USERS +
                        " WHERE " + C_EMAIL + "=? AND " + C_PASS + "=?",
                new String[]{email, pass});
        boolean ok = c.moveToFirst();
        c.close();
        return ok;
    }

    public int getUserIdByEmail(String email) {
        Cursor c = getReadableDatabase().rawQuery("SELECT " + C_ID +
                        " FROM " + T_USERS + " WHERE " + C_EMAIL + "=?",
                new String[]{email});
        int id = -1;
        if (c.moveToFirst()) id = c.getInt(0);
        c.close();
        return id;
    }

    public boolean isUserAdmin(String email) {
        Cursor c = getReadableDatabase().rawQuery("SELECT " + C_IS_ADMIN +
                        " FROM " + T_USERS + " WHERE " + C_EMAIL + "=?",
                new String[]{email});
        boolean admin = false;
        if (c.moveToFirst()) admin = c.getInt(0) == 1;
        c.close();
        return admin;
    }

    public String getUserNameByEmail(String email) {
        Cursor c = getReadableDatabase().rawQuery("SELECT " + C_NAME +
                        " FROM " + T_USERS + " WHERE " + C_EMAIL + "=?",
                new String[]{email});
        String name = "";
        if (c.moveToFirst()) name = c.getString(0);
        c.close();
        return name;
    }

    public String getUserProfileImage(String email) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT " + C_PROFILE_IMAGE + " FROM " + T_USERS + " WHERE " + C_EMAIL + "=?",
                new String[]{email}
        );

        String imagePath = "";
        if (c.moveToFirst()) {
            imagePath = c.getString(0);
        }
        c.close();
        return imagePath;
    }

    public boolean updateUserProfileImage(String email, String imagePath) {
        ContentValues values = new ContentValues();
        values.put(C_PROFILE_IMAGE, imagePath);

        int rowsAffected = getWritableDatabase().update(
                T_USERS,
                values,
                C_EMAIL + " = ?",
                new String[]{email}
        );

        return rowsAffected > 0;
    }

    public boolean insertBranch(String name, String address, String phone, double latitude, double longitude) {
        ContentValues v = new ContentValues();
        v.put(B_NAME, name);
        v.put(B_ADDRESS, address);
        v.put(B_PHONE, phone);
        v.put(B_LATITUDE, latitude);
        v.put(B_LONGITUDE, longitude);
        return getWritableDatabase().insert(T_BRANCHES, null, v) != -1;
    }

    public List<Branch> getBranches() {
        List<Branch> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM " + T_BRANCHES, null);
        if (c.moveToFirst()) {
            do {
                list.add(new Branch(
                        c.getInt(0),
                        c.getString(1),
                        c.getString(2),
                        c.getString(3),
                        c.getDouble(4),
                        c.getDouble(5)
                ));
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public boolean updateBranch(int id, String name, String address, String phone, double latitude, double longitude) {
        ContentValues v = new ContentValues();
        v.put(B_NAME, name);
        v.put(B_ADDRESS, address);
        v.put(B_PHONE, phone);
        v.put(B_LATITUDE, latitude);
        v.put(B_LONGITUDE, longitude);
        return getWritableDatabase().update(T_BRANCHES, v, B_ID + "=?",
                new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deleteBranch(int id) {
        return getWritableDatabase().delete(T_BRANCHES, B_ID + "=?",
                new String[]{String.valueOf(id)}) > 0;
    }

    public void debugListAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + T_USERS, null);

        if (c.moveToFirst()) {
            do {
                int id = c.getInt(c.getColumnIndexOrThrow(C_ID));
                String name = c.getString(c.getColumnIndexOrThrow(C_NAME));
                String email = c.getString(c.getColumnIndexOrThrow(C_EMAIL));
                int isAdmin = c.getInt(c.getColumnIndexOrThrow(C_IS_ADMIN));
                String profileImage = c.getString(c.getColumnIndexOrThrow(C_PROFILE_IMAGE));

                android.util.Log.d("DB_DEBUG", "User: " + id + ", " + name + ", " + email +
                        ", Admin: " + isAdmin + ", Image: " + profileImage);
            } while (c.moveToNext());
        }
        c.close();
    }
}