package roberta.heartbeepapp;

import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;

public class HeartBeepApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AndroidThreeTen.init(this);
    }
}
