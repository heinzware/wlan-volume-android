package de.chhe.wlanvolume.model.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

import de.chhe.wlanvolume.model.entity.WifiVolume;


public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    //general values of the database
    private static final String DATABASE_NAME = "wlan_volume.db";
    private static final int DATABASE_VERSION = 3;

    // Useful SQL query parts
    static final String TEXT_TYPE           = " TEXT";
    static final String INTEGER_TYPE        = " INTEGER";
    static final String NOT_NULL_CONSTRAINT = " NOT NULL";
    static final String UNIQUE_CONSTRAINT   = " UNIQUE";
    static final String PRIMARY_KEY         = " INTEGER PRIMARY KEY AUTOINCREMENT";
    static final String DELETE_TABLE        = "DROP TABLE IF EXISTS ";
    static final String COMMA_SEP           = ",";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context){
        if(instance == null){
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(WifiVolumeContract.SQL_CREATE_WLAN_VOLUME_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1) {
            Log.d(TAG, String.format(Locale.getDefault(), "Upgrading database from version %d to %d.", oldVersion, newVersion));
            try {
                db.beginTransaction();
                String addNotifyQuery = "ALTER TABLE " + WifiVolumeContract.WifiVolumeTable.TABLE_NAME + " ADD COLUMN " + WifiVolumeContract.WifiVolumeTable.COLUMN_NAME_NOTIFY + INTEGER_TYPE + ";";
                String addCommentQuery = "ALTER TABLE " + WifiVolumeContract.WifiVolumeTable.TABLE_NAME + " ADD COLUMN " + WifiVolumeContract.WifiVolumeTable.COLUMN_NAME_COMMENT + TEXT_TYPE + ";";
                db.execSQL(addNotifyQuery);
                db.execSQL(addCommentQuery);
                String setNotifyFalse = "UPDATE " + WifiVolumeContract.WifiVolumeTable.TABLE_NAME + " SET " + WifiVolumeContract.WifiVolumeTable.COLUMN_NAME_NOTIFY + " = 0;";
                db.execSQL(setNotifyFalse);
                oldVersion = 2;
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.d(TAG, String.format(Locale.getDefault(), "Exception while upgrading database from version %d to %d.\n Exception is:\n%s", oldVersion, newVersion, e.getMessage()));
            }
            db.endTransaction();
        }
        if (oldVersion == 2) {
            Log.d(TAG, String.format(Locale.getDefault(), "Upgrading database from version %d to %d.", oldVersion, newVersion));
            try {
                db.beginTransaction();
                String addRestoreQuery = "ALTER TABLE " + WifiVolumeContract.WifiVolumeTable.TABLE_NAME + " ADD COLUMN " + WifiVolumeContract.WifiVolumeTable.COLUMN_NAME_RESTORE + INTEGER_TYPE + ";";
                db.execSQL(addRestoreQuery);
                String setRestoreFalse = "UPDATE " + WifiVolumeContract.WifiVolumeTable.TABLE_NAME + " SET " + WifiVolumeContract.WifiVolumeTable.COLUMN_NAME_RESTORE + " = 0;";
                db.execSQL(setRestoreFalse);
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.d(TAG, String.format(Locale.getDefault(), "Exception while upgrading database from version %d to %d.\n Exception is:\n%s", oldVersion, newVersion, e.getMessage()));
            }
            db.endTransaction();
        }
    }

    public void clearDatabase(){
        try {
            SQLiteDatabase db = getWritableDatabase();
            //drop all tables
            db.execSQL(WifiVolumeContract.SQL_DELETE_WLAN_VOLUME_TABLE);
            //recreate all tables
            db.execSQL(WifiVolumeContract.SQL_CREATE_WLAN_VOLUME_TABLE);
        } catch(Exception e) {
            //TODO: handle exception
        }
    }

    /*
     * methods to work with WifiVolume-Entities
     */

    public long saveWifiVolume(WifiVolume wifiVolume) {
        try {

            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(WifiVolumeContract.WifiVolumeTable.COLUMN_NAME_SSID, wifiVolume.getSsid());
            values.put(WifiVolumeContract.WifiVolumeTable.COLUMN_NAME_VOLUME, wifiVolume.getVolume());
            values.put(WifiVolumeContract.WifiVolumeTable.COLUMN_NAME_NOTIFY, wifiVolume.isShowNotification() ? 1 : 0);
            values.put(WifiVolumeContract.WifiVolumeTable.COLUMN_NAME_RESTORE, wifiVolume.isRestore() ? 1 : 0);
            if (wifiVolume.getComment() != null) values.put(WifiVolumeContract.WifiVolumeTable.COLUMN_NAME_COMMENT, wifiVolume.getComment());

            long result;
            if(wifiVolume.getId() != null) {
                String where = WifiVolumeContract.WifiVolumeTable._ID + " = ?";
                result = db.update(WifiVolumeContract.WifiVolumeTable.TABLE_NAME, values, where, new String[]{wifiVolume.getId() + ""});
                if(result == 1) {
                    return wifiVolume.getId();
                } else {
                    return -1L;
                }
            } else {
                return db.insert(WifiVolumeContract.WifiVolumeTable.TABLE_NAME, null, values);
            }
        } catch(Exception e) {
            //TODO:handle exceptions
            return -1L;
        }
    }

    @Nullable
    public WifiVolume getWifiVolumeBySsid(@NonNull String ssid) {
        String where = WifiVolumeContract.WifiVolumeTable.COLUMN_NAME_SSID + " = ?";
        String[] whereValues = new String[]{ssid};
        return getWifiVolume(where,whereValues);
    }

    @Nullable
    public WifiVolume getWifiVolumeById(long id) {
        String where = WifiVolumeContract.WifiVolumeTable._ID + " = ?";
        String[] whereValues = new String[]{id + ""};
        return getWifiVolume(where,whereValues);
    }

    @Nullable
    private WifiVolume getWifiVolume(String where, String[] whereValues) {

        SQLiteDatabase db = getReadableDatabase();

        try {

            Cursor cursor = db.query(WifiVolumeContract.WifiVolumeTable.TABLE_NAME,
                    WifiVolumeContract.WifiVolumeTable.ALL_COLUMNS,
                    where,
                    whereValues,
                    null,
                    null,
                    null);

            if (cursor.getCount() == 1) {

                cursor.moveToFirst();
                WifiVolume wifiVolume = cursorToWifiVolume(cursor);
                cursor.close();

                db.close();
                return wifiVolume;
            }
        } catch(Exception e) {
            //TODO: handle exceptions
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return null;
    }

    @NonNull
    public ArrayList<WifiVolume> getAllWifiVolumes() {
        try {

            SQLiteDatabase db = getReadableDatabase();

            String sortOrder = WifiVolumeContract.WifiVolumeTable._ID + " ASC";

            Cursor cursor = db.query(WifiVolumeContract.WifiVolumeTable.TABLE_NAME,
                    WifiVolumeContract.WifiVolumeTable.ALL_COLUMNS,
                    null,
                    null,
                    null,
                    null,
                    sortOrder);

            ArrayList<WifiVolume> wifiVolumes = new ArrayList<>();

            while (cursor.moveToNext()) {
                wifiVolumes.add(cursorToWifiVolume(cursor));
            }

            cursor.close();

            return wifiVolumes;
        } catch(Exception e) {
            //TODO: handle exception
        }

        return new ArrayList<>();
    }

    private WifiVolume cursorToWifiVolume(@NonNull Cursor cursor) {

        int idCol       = cursor.getColumnIndex(WifiVolumeContract.WifiVolumeTable.ALL_COLUMNS[0]);
        int ssidCol     = cursor.getColumnIndex(WifiVolumeContract.WifiVolumeTable.ALL_COLUMNS[1]);
        int volumeCol   = cursor.getColumnIndex(WifiVolumeContract.WifiVolumeTable.ALL_COLUMNS[2]);
        int notifyCol   = cursor.getColumnIndex(WifiVolumeContract.WifiVolumeTable.ALL_COLUMNS[3]);
        int commentCol  = cursor.getColumnIndex(WifiVolumeContract.WifiVolumeTable.ALL_COLUMNS[4]);
        int restoreCol  = cursor.getColumnIndex(WifiVolumeContract.WifiVolumeTable.ALL_COLUMNS[5]);

        long id         = cursor.getLong(idCol);
        String ssid     = cursor.getString(ssidCol);
        int volume      = cursor.getInt(volumeCol);
        boolean notify  = cursor.getInt(notifyCol) > 0;
        String comment  = cursor.getString(commentCol);
        boolean restore = cursor.getInt(restoreCol) > 0;

        WifiVolume wifiVolume = new WifiVolume();
        wifiVolume.setId(id);
        wifiVolume.setSsid(ssid);
        wifiVolume.setVolume(volume);
        wifiVolume.setShowNotification(notify);
        wifiVolume.setComment(comment);
        wifiVolume.setRestore(restore);

        return wifiVolume;
    }

    public boolean deleteWifiVolume(@NonNull WifiVolume wifiVolume) {
        try {

            SQLiteDatabase db = getWritableDatabase();

            String where = WifiVolumeContract.WifiVolumeTable._ID + " = ?";

            int rows = db.delete(WifiVolumeContract.WifiVolumeTable.TABLE_NAME,
                    where, new String[]{wifiVolume.getId() + ""});

            return rows == 1;
        } catch(Exception e) {
            //TODO: handle exceptions
        }

        return false;
    }
}
