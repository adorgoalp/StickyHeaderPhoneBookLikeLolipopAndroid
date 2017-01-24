package com.iftalab.iphonebook;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {

    FrameLayout viewHolder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewHolder = (FrameLayout) findViewById(R.id.phoneBookHolder);
        Fragment fragment = new PhoneBookFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.phoneBookHolder,fragment,PhoneBookFragment.TAG).commit();
    }


}
