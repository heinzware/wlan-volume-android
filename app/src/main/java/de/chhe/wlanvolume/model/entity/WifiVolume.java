package de.chhe.wlanvolume.model.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This class represents a WiFi-Network and the settings to describe what should happen
 * when the device is connected to it.
 */
public class WifiVolume implements Parcelable {

    private Long id;
    private String ssid;
    private Integer volume;
    private boolean showNotification;
    private boolean restore;
    private String comment;

    public WifiVolume(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public Integer getVolume() {
        return volume;
    }

    public void setVolume(Integer volume) {
        this.volume = volume;
    }

    public boolean isShowNotification() {
        return showNotification;
    }

    public void setShowNotification(boolean showNotification) {
        this.showNotification = showNotification;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isRestore() {
        return restore;
    }

    public void setRestore(boolean restore) {
        this.restore = restore;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.ssid);
        dest.writeValue(this.volume);
        dest.writeByte(this.showNotification ? (byte) 1 : (byte) 0);
        dest.writeByte(this.restore ? (byte) 1 : (byte) 0);
        dest.writeString(this.comment);
    }

    protected WifiVolume(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.ssid = in.readString();
        this.volume = (Integer) in.readValue(Integer.class.getClassLoader());
        this.showNotification = in.readByte() != 0;
        this.restore = in.readByte() != 0;
        this.comment = in.readString();
    }

    public static final Creator<WifiVolume> CREATOR = new Creator<WifiVolume>() {
        @Override
        public WifiVolume createFromParcel(Parcel source) {
            return new WifiVolume(source);
        }

        @Override
        public WifiVolume[] newArray(int size) {
            return new WifiVolume[size];
        }
    };
}
