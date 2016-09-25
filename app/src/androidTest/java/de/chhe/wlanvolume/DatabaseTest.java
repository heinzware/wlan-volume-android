package de.chhe.wlanvolume;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import de.chhe.wlanvolume.model.entity.WlanVolume;
import de.chhe.wlanvolume.model.persistence.DatabaseHelper;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * This class tests the {@link de.chhe.wlanvolume.model.persistence.DatabaseHelper}
 * and its methods to persist a WlanVolume-Object.
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

        //create an WlanVolume-object and save it
        WlanVolume wlanVolume1 = new WlanVolume();
        String wlanVolume1Ssid = "My Test Wlan Number 1";
        wlanVolume1.setSsid(wlanVolume1Ssid);
        wlanVolume1.setVolume(60);
        long id1 = db.saveWlanVolume(wlanVolume1);
        wlanVolume1.setId(id1);

        //get saved object by SSID and test it
        WlanVolume wlanVolume1BySsid = db.getWlanVolumeBySsid(wlanVolume1Ssid);
        assertNotNull(wlanVolume1BySsid);
        assertEquals(wlanVolume1Ssid, wlanVolume1BySsid.getSsid());
        assertEquals(60, wlanVolume1BySsid.getVolume().intValue());
        assertEquals(id1, wlanVolume1BySsid.getId().longValue());

        //get saved object by ID and test it
        WlanVolume wlanVolume1ById = db.getWlanVolumeById(id1);
        assertNotNull(wlanVolume1ById);
        assertEquals(wlanVolume1Ssid, wlanVolume1ById.getSsid());
        assertEquals(60, wlanVolume1ById.getVolume().intValue());
        assertEquals(id1, wlanVolume1ById.getId().longValue());

        //update the WlanVolume-object
        String wlanVolume2Ssid = "My Test Wlan Number 2";
        wlanVolume1.setSsid(wlanVolume2Ssid);
        db.saveWlanVolume(wlanVolume1);

        //get updated object by SSID and test it
        wlanVolume1BySsid = db.getWlanVolumeBySsid(wlanVolume2Ssid);
        assertNotNull(wlanVolume1BySsid);
        assertEquals(wlanVolume2Ssid, wlanVolume1BySsid.getSsid());
        assertEquals(60, wlanVolume1BySsid.getVolume().intValue());
        assertEquals(id1, wlanVolume1BySsid.getId().longValue());

        //get updated object by ID and test it
        wlanVolume1ById = db.getWlanVolumeById(id1);
        assertNotNull(wlanVolume1ById);
        assertEquals(wlanVolume2Ssid, wlanVolume1ById.getSsid());
        assertEquals(60, wlanVolume1ById.getVolume().intValue());
        assertEquals(id1, wlanVolume1ById.getId().longValue());

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

        //create an WlanVolume-object and save it
        WlanVolume wlanVolume1 = new WlanVolume();
        String wlanVolume1Ssid = "My Test Wlan Number 1";
        wlanVolume1.setSsid(wlanVolume1Ssid);
        wlanVolume1.setVolume(60);
        long id1 = db.saveWlanVolume(wlanVolume1);
        wlanVolume1.setId(id1);

        //delete the WlanVolume-Object
        db.deleteWlanVolume(wlanVolume1);

        //get updated object by SSID and test it
        WlanVolume wlanVolume1BySsid = db.getWlanVolumeBySsid(wlanVolume1Ssid);
        assertNull(wlanVolume1BySsid);

        //get updated object by ID and test it
        WlanVolume wlanVolume1ById = db.getWlanVolumeBySsid(wlanVolume1Ssid);
        assertNull(wlanVolume1ById);

        db.clearDatabase();

        db.close();
    }
}
