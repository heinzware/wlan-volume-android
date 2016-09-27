package de.chhe.wlanvolume.model.persistence;

import android.provider.BaseColumns;


class WlanVolumeContract {

    private WlanVolumeContract(){}

    static abstract class WlanVolumeTable implements BaseColumns {
        //table_name
        static final String TABLE_NAME       = "WlanVolume";
        //column names
        static final String COLUMN_NAME_SSID     = "ssid";
        static final String COLUMN_NAME_VOLUME   = "volume";
        static final String[] ALL_COLUMNS        = {_ID, COLUMN_NAME_SSID, COLUMN_NAME_VOLUME};
        //constraint names
        private static final String CONSTRAINT_VOLUME   = "volume_constraint";
    }

    static final String SQL_CREATE_WLAN_VOLUME_TABLE = "CREATE TABLE "
            + WlanVolumeTable.TABLE_NAME + " ("
            + WlanVolumeTable._ID + DatabaseHelper.PRIMARY_KEY + DatabaseHelper.COMMA_SEP
            + WlanVolumeTable.COLUMN_NAME_SSID + DatabaseHelper.TEXT_TYPE + DatabaseHelper.NOT_NULL_CONSTRAINT + DatabaseHelper.UNIQUE_CONSTRAINT +  DatabaseHelper.COMMA_SEP
            + WlanVolumeTable.COLUMN_NAME_VOLUME + DatabaseHelper.INTEGER_TYPE + DatabaseHelper.COMMA_SEP
            + "CONSTRAINT " + WlanVolumeTable.CONSTRAINT_VOLUME + " CHECK ("
            + WlanVolumeTable.COLUMN_NAME_VOLUME +  " <= 100 AND " + WlanVolumeTable.COLUMN_NAME_VOLUME + " >= 0));";

    static final String SQL_DELETE_WLAN_VOLUME_TABLE = DatabaseHelper.DELETE_TABLE + WlanVolumeTable.TABLE_NAME;
}
