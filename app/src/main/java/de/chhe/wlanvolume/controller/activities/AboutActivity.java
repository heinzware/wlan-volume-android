package de.chhe.wlanvolume.controller.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import de.chhe.wlanvolume.controller.fragments.AboutFragment;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new AboutFragment())
                .commit();
    }
}
