package de.chhe.wlanvolume.model.entity;

/**
 * Created by christoph on 24.09.16.
 */

public class WlanVolume {

    private Long id;
    private String ssid;
    private Integer volume;

    public WlanVolume(){}

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
}
