package de.chhe.wlanvolume.model.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import de.chhe.wlanvolume.model.entity.WlanVolume;

/**
 * Created by christoph on 24.09.16.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    //general values of the database
    private static final String DATABASE_NAME = "wlan_volume.db";
    private static final int DATABASE_VERSION = 1;

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
        db.execSQL(WlanVolumeContract.SQL_CREATE_CAR_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        //nothing to do here cause we're still on version 1
    }


    /*
     * methods to work with WlanVolume-Entities
     */

    public long saveWlanVolume(WlanVolume wlanVolume){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(WlanVolumeContract.WlanVolumeTable.COLUMN_NAME_SSID, wlanVolume.getSsid());
        values.put(WlanVolumeContract.WlanVolumeTable.COLUMN_NAME_VOLUME, wlanVolume.getVolume());

        long result;
        if(wlanVolume.getId() != null) {
            String where = WlanVolumeContract.WlanVolumeTable._ID + " = ?";
            result = db.update(WlanVolumeContract.WlanVolumeTable.TABLE_NAME, values, where, new String[]{wlanVolume.getId() + ""});
            db.close();
            if(result == 1){
                return wlanVolume.getId();
            } else {
                return -1L;
            }
        } else {
            return db.insert(WlanVolumeContract.WlanVolumeTable.TABLE_NAME, null, values);
        }
    }

    @Nullable
    public WlanVolume getWlanVolumeBySsid(@NonNull String ssid){
        String where = WlanVolumeContract.WlanVolumeTable.COLUMN_NAME_SSID + " = ?";
        String[] whereValues = new String[]{ssid};
        return getWlanVolume(where,whereValues);
    }

    @Nullable
    public WlanVolume getWlanVolumeById(long id){
        String where = WlanVolumeContract.WlanVolumeTable._ID + " = ?";
        String[] whereValues = new String[]{id + ""};
        return getWlanVolume(where,whereValues);
    }

    @Nullable
    private WlanVolume getWlanVolume(String where, String[] whereValues){
        SQLiteDatabase db = getReadableDatabase();

        String[] columns = {
                WlanVolumeContract.WlanVolumeTable._ID,
                WlanVolumeContract.WlanVolumeTable.COLUMN_NAME_SSID,
                WlanVolumeContract.WlanVolumeTable.COLUMN_NAME_VOLUME
        };

        try(Cursor cursor = db.query(WlanVolumeContract.WlanVolumeTable.TABLE_NAME, // table name
                columns,                                                            // columns to return
                where,                                                              // columns for WHERE
                whereValues,                                                        // values for WHERE
                null,                                                               // groups
                null,                                                               // filters
                null)) {                                                            // sort order

            if (cursor.getCount() == 1) {
                cursor.moveToFirst();
                int idCol       = cursor.getColumnIndexOrThrow(columns[0]);
                int ssidCol     = cursor.getColumnIndexOrThrow(columns[1]);
                int volumeCol   = cursor.getColumnIndexOrThrow(columns[2]);

                long id     = cursor.getLong(idCol);
                String ssid = cursor.getString(ssidCol);
                int volume  = cursor.getInt(volumeCol);

                WlanVolume wlanVolume = new WlanVolume();
                wlanVolume.setId(id);
                wlanVolume.setSsid(ssid);
                wlanVolume.setVolume(volume);
                cursor.close();

                db.close();
                return wlanVolume;
            }
        }catch(Exception e){
            //TODO: handle exceptions
            db.close();
        }
        return null;
    }
}
