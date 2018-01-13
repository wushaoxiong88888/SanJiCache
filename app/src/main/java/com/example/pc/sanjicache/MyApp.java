package com.example.pc.sanjicache;

import android.app.Application;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by pc on 2017/12/8.
 */

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(configuration);

    }
}
