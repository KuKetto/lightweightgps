package pe.saydomonkos.lightweightgps;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

public class HistoryActivity extends AppCompatActivity {

    private ListView historyListView;
    private Animation animation;
    private String[] historyListViewItems;
    private String[] filenames;
    private String[] startTimes;
    private String[] endTimes;
    private PostProcess postProcess;
    private boolean requestSuccessful;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean userLoggedIn = false;
        String userID = "";
        String token = "";
        Bundle data = getIntent().getExtras();
        if (data != null) {
            userLoggedIn = data.getBoolean("userLoggedIn");
            userID = data.getString("userID");
            token = data.getString("token");
        }

        setContentView(R.layout.activity_history);
        getSupportActionBar().hide();
        historyListViewItems = new String[0];
        filenames = new String[0];
        startTimes = new String[0];
        endTimes = new String[0];
        getData();
        historyListView = findViewById(R.id.history_list_view);
        animation = AnimationUtils.loadAnimation(this, R.anim.animation1);
        historyListView.setAdapter(new HistoryViewAdapter(this, historyListViewItems));

        postProcess = new PostProcess();

        if (!userLoggedIn) {
            historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    HistoryItem historyItem = postProcess.runDistanceCalculationOnThreads(HistoryActivity.this, filenames[i]);
                    Intent historyItemIntent = new Intent(HistoryActivity.this, HistoryItemActivity.class);
                    historyItemIntent.putExtra("totalDistance", historyItem.getTotalDistance());
                    historyItemIntent.putExtra("averageVelocity", historyItem.getAverageVelocity());
                    historyItemIntent.putExtra("filename", filenames[i]);
                    HistoryActivity.this.startActivity(historyItemIntent);
                }
            });
        } else {
            String finalUserID = userID;
            String finalToken = token;
            historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    HistoryItem historyItem = postProcess.runDistanceCalculationOnThreads(HistoryActivity.this, filenames[i]);
                    HistoryItem velocityHistoryItemData = postProcess.calculateVelocities(HistoryActivity.this, filenames[i]);
                    String startTime = startTimes[i];
                    String endTime = endTimes[i];
                    requestSuccessful = false;
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                URL url = new URL("http://87.229.85.225:42069/track");
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                conn.setRequestMethod("POST");
                                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                                conn.setRequestProperty("Accept","application/json");
                                conn.setRequestProperty("Authorization", "Bearer " + finalToken);

                                conn.setDoOutput(true);
                                conn.setDoInput(true);

                                JSONObject jsonParam = new JSONObject();
                                jsonParam.put("userID", finalUserID);
                                jsonParam.put("startTime", startTime);
                                jsonParam.put("endTime", endTime);
                                jsonParam.put("totalDistance", historyItem.getTotalDistance());
                                jsonParam.put("averageVelocity", historyItem.getAverageVelocity());

                                JSONArray velocities = new JSONArray();
                                for (int j = 0; j < velocityHistoryItemData.getVelocities().length; j++) {
                                    JSONObject velocity = new JSONObject();
                                    velocity.put("from", j*1000);
                                    velocity.put("to", (j+1)*1000);
                                    velocity.put("velocity", velocityHistoryItemData.getVelocities()[j]);
                                    velocities.put(velocity);
                                }
                                JSONObject velocity = new JSONObject();
                                velocity.put("from", velocityHistoryItemData.getVelocities().length*1000);
                                velocity.put("to", velocityHistoryItemData.getVelocities().length*1000 + velocityHistoryItemData.getRemainingDistance());
                                velocity.put("velocity", velocityHistoryItemData.getVelocityOnRemainingDistance());
                                velocities.put(velocity);

                                jsonParam.put("velocities", velocities);

                                Log.i("JSON", jsonParam.toString());
                                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                                os.writeBytes(jsonParam.toString());

                                os.flush();
                                os.close();

                                if (conn.getResponseCode() == 200) {
                                   setRequestSuccessful(true);
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

                    if (requestSuccessful) {
                        Log.i("success","OK");
                        doToast("successfully synced");
                    }

                }
            });
        }
    }

    private void getData() {
        File logsDir = new File(String.valueOf(getExternalFilesDir("/logs")));
        File[] listOfFiles = logsDir.listFiles();
        for (File file : listOfFiles) {
            //remove .csv from filename
            String filename = file.getName().split("\\.")[0];

            /*
            log data after splitting by '-'
                LOG
                FROM
                yyyy_MM_dd
                HH:mm:ss
                TO
                yyyy_MM_dd
                HH:mm:ss
                ELAPSED
                long int
                MILLIS
            */

            String[] logData = filename.split("-");
            String data = ("Tracking between " + logData[2] + " " + logData[3] + " and " + logData[5] + " " + logData[6]).replace("_",".");
            int seconds = (int) (Integer.parseInt(logData[8]) / 1000) % 60 ;
            int minutes = (int) ((Integer.parseInt(logData[8]) / (1000*60)) % 60);
            int hours   = (int) ((Integer.parseInt(logData[8]) / (1000*60*60)) % 24);
            data += "\nTracking time: " + String.valueOf(hours) + " hour(s) " + String.valueOf(minutes) + " minute(s) " + String.valueOf(seconds) + "second(s)";

            historyListViewItems = Arrays.copyOf(historyListViewItems, historyListViewItems.length + 1);
            historyListViewItems[historyListViewItems.length - 1] = data;
            filenames = Arrays.copyOf(filenames, filenames.length + 1);
            filenames[filenames.length - 1] = file.getName();
            startTimes = Arrays.copyOf(startTimes, startTimes.length + 1);
            startTimes[startTimes.length - 1] = (logData[2] + " " + logData[3]).replace("_",".");
            endTimes = Arrays.copyOf(endTimes, endTimes.length + 1);
            endTimes[endTimes.length - 1] = (logData[5] + " " + logData[6]).replace("_",".");
        }
    }

    void doToast(String message) {
        Toast.makeText(HistoryActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    void setRequestSuccessful(boolean result) {
        requestSuccessful = result;
    }
}
