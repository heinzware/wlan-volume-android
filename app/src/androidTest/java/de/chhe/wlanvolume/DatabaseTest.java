package de.chhe.wlanvolume;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import de.chhe.wlanvolume.model.entity.WifiVolume;
import de.chhe.wlanvolume.model.persistence.DatabaseHelper;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * This class tests the {@link de.chhe.wlanvolume.model.persistence.DatabaseHelper}
 * and its methods to persist a WifiVolume-Object.
 */
@RunWith(AndroidJUnit4.class)
public class DatabaseTest {

    @Test
    public void wlanVolumeSaveTest() throws Exception{

        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        //get an instance of DatabaseHelper with this context
        DatabaseHelper db = DatabaseHelper.getInstance(appContext);

        //clear database before testing
        db.clearDatabase();

        //create an WifiVolume-object and save it
        WifiVolume wifiVolume1 = new WifiVolume();
        String wlanVolume1Ssid = "My Test Wlan Number 1";
        wifiVolume1.setSsid(wlanVolume1Ssid);
        wifiVolume1.setVolume(60);
        long id1 = db.saveWifiVolume(wifiVolume1);
        wifiVolume1.setId(id1);

        //get saved object by SSID and test it
        WifiVolume wifiVolume1BySsid = db.getWifiVolumeBySsid(wlanVolume1Ssid);
        assertNotNull(wifiVolume1BySsid);
        assertEquals(wlanVolume1Ssid, wifiVolume1BySsid.getSsid());
        assertEquals(60, wifiVolume1BySsid.getVolume().intValue());
        assertEquals(id1, wifiVolume1BySsid.getId().longValue());

        //get saved object by ID and test it
        WifiVolume wifiVolume1ById = db.getWifiVolumeById(id1);
        assertNotNull(wifiVolume1ById);
        assertEquals(wlanVolume1Ssid, wifiVolume1ById.getSsid());
        assertEquals(60, wifiVolume1ById.getVolume().intValue());
        assertEquals(id1, wifiVolume1ById.getId().longValue());

        //update the WifiVolume-object
        String wlanVolume2Ssid = "My Test Wlan Number 2";
        wifiVolume1.setSsid(wlanVolume2Ssid);
        db.saveWifiVolume(wifiVolume1);

        //get updated object by SSID and test it
        wifiVolume1BySsid = db.getWifiVolumeBySsid(wlanVolume2Ssid);
        assertNotNull(wifiVolume1BySsid);
        assertEquals(wlanVolume2Ssid, wifiVolume1BySsid.getSsid());
        assertEquals(60, wifiVolume1BySsid.getVolume().intValue());
        assertEquals(id1, wifiVolume1BySsid.getId().longValue());

        //get updated object by ID and test it
        wifiVolume1ById = db.getWifiVolumeById(id1);
        assertNotNull(wifiVolume1ById);
        assertEquals(wlanVolume2Ssid, wifiVolume1ById.getSsid());
        assertEquals(60, wifiVolume1ById.getVolume().intValue());
        assertEquals(id1, wifiVolume1ById.getId().longValue());

        db.clearDatabase();

        db.close();
    }

    @Test
    public void wlanVolumeDeleteTest() throws Exception{

        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        //get an instance of DatabaseHelper with this context
        DatabaseHelper db = DatabaseHelper.getInstance(appContext);

        //clear database before testing
        db.clearDatabase();

        //create an WifiVolume-object and save it
        WifiVolume wifiVolume1 = new WifiVolume();
        String wlanVolume1Ssid = "My Test Wlan Number 1";
        wifiVolume1.setSsid(wlanVolume1Ssid);
        wifiVolume1.setVolume(60);
        long id1 = db.saveWifiVolume(wifiVolume1);
        wifiVolume1.setId(id1);

        //delete the WifiVolume-Object
        db.deleteWifiVolume(wifiVolume1);

        //get updated object by SSID and test it
        WifiVolume wifiVolume1BySsid = db.getWifiVolumeBySsid(wlanVolume1Ssid);
        assertNull(wifiVolume1BySsid);

        //get updated object by ID and test it
        WifiVolume wifiVolume1ById = db.getWifiVolumeBySsid(wlanVolume1Ssid);
        assertNull(wifiVolume1ById);

        db.clearDatabase();

        db.close();
    }
}
