package com.hayukleung.tsa.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.hayukleung.tsa.R;
import com.hayukleung.tsa.databinding.ActivityMainBinding;

/**
 * TrySophixApp
 * <p>
 * liangxiaxu@aobi.com
 * 2017-12-18 18:50
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    }
}
