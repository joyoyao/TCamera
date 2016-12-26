package com.abcew.camera;

import android.app.Application;

/**
 * Created by laputan on 16/11/2.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ImgSdk.init(this);
    }


}
