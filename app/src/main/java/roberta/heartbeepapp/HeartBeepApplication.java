package roberta.heartbeepapp;

import androidx.multidex.MultiDexApplication;

import com.jakewharton.threetenabp.AndroidThreeTen;

public class HeartBeepApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        AndroidThreeTen.init(this);
    }
}
