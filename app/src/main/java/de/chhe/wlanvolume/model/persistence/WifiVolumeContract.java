package de.chhe.wlanvolume.model.persistence;

import android.provider.BaseColumns;


class WifiVolumeContract {

    private WifiVolumeContract(){}

    static abstract class WifiVolumeTable implements BaseColumns {
        //table_name
        static final String TABLE_NAME       = "WifiVolume";
        //column names
        static final String COLUMN_NAME_SSID     = "ssid";
        static final String COLUMN_NAME_VOLUME   = "volume";
        static final String COLUMN_NAME_NOTIFY   = "notify";
        static final String COLUMN_NAME_COMMENT  = "comment";
        static final String COLUMN_NAME_RESTORE  = "restore";
        static final String[] ALL_COLUMNS        = {_ID, COLUMN_NAME_SSID, COLUMN_NAME_VOLUME, COLUMN_NAME_NOTIFY, COLUMN_NAME_COMMENT, COLUMN_NAME_RESTORE};
        //constraint names
        private static final String CONSTRAINT_VOLUME   = "volume_constraint";
    }

    static final String SQL_CREATE_WLAN_VOLUME_TABLE = "CREATE TABLE "
            + WifiVolumeTable.TABLE_NAME + " ("
            + WifiVolumeTable._ID + DatabaseHelper.PRIMARY_KEY + DatabaseHelper.COMMA_SEP
            + WifiVolumeTable.COLUMN_NAME_SSID + DatabaseHelper.TEXT_TYPE + DatabaseHelper.NOT_NULL_CONSTRAINT + DatabaseHelper.UNIQUE_CONSTRAINT +  DatabaseHelper.COMMA_SEP
            + WifiVolumeTable.COLUMN_NAME_VOLUME + DatabaseHelper.INTEGER_TYPE + DatabaseHelper.COMMA_SEP
            + WifiVolumeTable.COLUMN_NAME_NOTIFY + DatabaseHelper.INTEGER_TYPE + DatabaseHelper.COMMA_SEP
            + WifiVolumeTable.COLUMN_NAME_COMMENT + DatabaseHelper.TEXT_TYPE + DatabaseHelper.COMMA_SEP
            + WifiVolumeTable.COLUMN_NAME_RESTORE + DatabaseHelper.INTEGER_TYPE + DatabaseHelper.COMMA_SEP
            + "CONSTRAINT " + WifiVolumeTable.CONSTRAINT_VOLUME + " CHECK ("
            + WifiVolumeTable.COLUMN_NAME_VOLUME +  " <= 100 AND " + WifiVolumeTable.COLUMN_NAME_VOLUME + " >= 0));";

    static final String SQL_DELETE_WLAN_VOLUME_TABLE = DatabaseHelper.DELETE_TABLE + WifiVolumeTable.TABLE_NAME;
}
