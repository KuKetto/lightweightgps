package pe.saydomonkos.lightweightgps;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Utils {
    public Utils(){}

    public Timestamp getTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    public String getCurrentTimeFormat(Timestamp timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS").format(timestamp);
    }

    public String getFilenameFormat(Timestamp startTimestamp, Timestamp stopTimestamp, long MILLIS) {
        return "LOG-FROM-" + new SimpleDateFormat("yyyy_MM_dd-HH:mm:ss").format(startTimestamp) + "-TO-"
                + new SimpleDateFormat("yyyy_MM_dd-HH:mm:ss").format(stopTimestamp) + "-ELAPSED-"
                + String.valueOf(MILLIS) + "-MILLIS.csv";
    }
}
