package pe.saydomonkos.lightweightgps;

import static pe.saydomonkos.lightweightgps.HistoryItemActivity.setViewBackground;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

public class SyncedHistoryItem extends AppCompatActivity {

    String[] from;
    String[] to;
    Double[] velocity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.synced_history_item);
        RelativeLayout relativeLayout = findViewById(R.id.relativeSyncedHistoryScrollView);

        Bundle data = getIntent().getExtras();
        String trackID = "";
        String token = "";
        if (data != null) {
            trackID = data.getString("trackID");
            token = data.getString("token");
        }

        from = new String[0];
        to = new String[0];
        velocity = new Double[0];

        String finalTrackID = trackID;
        String finalToken = token;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://87.229.85.225:42069/track/data/" + finalTrackID);
                    Log.i("url", url.toString());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Authorization", "Bearer " + finalToken);

                    if (conn.getResponseCode() == 200) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line+"\n");
                        }
                        br.close();
                        String response = sb.toString();

                        response = response.replace("[","")
                                .replace("]","")
                                .replace("{","");

                        String[] items;
                        if (response.contains("},")) {
                            items = response.split("\\},");
                            items[items.length-1] = items[items.length-1].replace("}","");
                        } else {
                            items = new String[1];
                            items[0] = response.replace("}","");
                        }


                        for (int i = 0; i < items.length; i++) {
                            String[] subitems = items[i].split(",");
                            String timeFrom = subitems[0].replace("\"from\":", "").replace("\"","");
                            String timeTo = subitems[1].replace("\"to\":", "").replace("\"","");
                            Double currentVelocity = Double.valueOf(subitems[2].replace("\"velocity\":", "").replace("\"",""));

                            addNewSyncedHistoryItem(timeFrom, timeTo, currentVelocity);
                        }


                    }

                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                    Log.i("MSG" , conn.getResponseMessage());

                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        to[to.length - 1] = String.format("%.3f", Double.valueOf(to[to.length - 1]));

        for (int i = 0; i < velocity.length; i++) {
            TextView textView = new TextView(SyncedHistoryItem.this);
            setViewBackground(textView, SyncedHistoryItem.this);
            textView.setId(42000 + i);
            textView.setTextSize(22);
            textView.setText(String.format("\t\t%s - %s\t\t\n%.2f km/h", from[i], to[i], velocity[i]));
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 20, 0, 0);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, 42000 + i);
            if (i > 0) layoutParams.addRule(RelativeLayout.BELOW, 42000 + (i-1));
            textView.setLayoutParams(layoutParams);
            textView.setGravity(Gravity.CENTER);
            textView.setWidth(400);
            relativeLayout.addView(textView);
        }
    }

    void addNewSyncedHistoryItem(String timeFrom, String timeTo, Double currentVelocity) {
        from = Arrays.copyOf(from, from.length + 1);
        from[from.length - 1] = timeFrom;
        to = Arrays.copyOf(to, to.length + 1);
        to[to.length - 1] = timeTo;
        velocity = Arrays.copyOf(velocity, velocity.length + 1);
        velocity[velocity.length - 1] = currentVelocity;
    }
}
