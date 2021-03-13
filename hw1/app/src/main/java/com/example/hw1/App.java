package com.example.hw1;

import android.app.Application;
import android.util.Log;

import com.scichart.charting.visuals.SciChartSurface;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        setSciChartLicense();
    }

    private void setSciChartLicense() {
        String licence = "wgR43AXYwAzbzvGlQl2H7lWF4Grwff8Et6L5B5oigpKSS4R2B/DYGum/Dz0nZ+FEOgW+4PkaYzdCUP4VZYgWWSf+TNSvpdx9bTAl7vMzHrtCEwe5kSF8Qzw00uMAfkiziVHUhkL91wr6QQqI6i45l/Zb36nxRxoNRKXP3L1Ujccb8/63R9geWsn25XuCGnLtsjF1WsJbNuOEmdSvJ/kdHmpyAjzfwgQXfC5SGxS+jznlbDCZixrobN95Z+4vCT9rox5QZx2hJytuqowxdekJpVL9+aex9uv6UeEGS0gt/lWSqjPpUppfQylailkXgoQFIO4Nw3m39ihSC7KSJrk4MQtd7wma6CnJPJTWH5Ntvy2TWQrVw1VcyhHkFQIEgLo/FwNu62B3mMlE7gWYuf/JsEqLo3rmyT+6Gu/k9HLQggcZmAvuJRSgcmCAT7QYK6w+eN4CCC9Id7dSJQMpJK0LiDDbsYQM70zLWW4aYfECKCUjxYZPU2dhsfghfG5YJat5nKoGTZp5ERetPPY=";

        try {
            SciChartSurface.setRuntimeLicenseKey(licence);
            Log.d("ScichartLicence", "Licence is ok");
        } catch (Exception e) {
            Log.e("ScichartLicence", "Error when setting the licence", e);
        }
    }

}
