package pe.saydomonkos.lightweightgps;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;

public class UserActivity extends AppCompatActivity {

    Button localFiles;
    Button syncedFiles;
    String[] userItems;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.user_layout);

        localFiles = findViewById(R.id.localFilesBtn);
        syncedFiles = findViewById(R.id.syncedFilesBtn);

        String token = "";
        Bundle data = getIntent().getExtras();
        if (data != null) {
            token = data.getString("token");
        };

        userItems = new String[0];

        String userID = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String[] chunks = token.split("\\.");
            Log.i("token", token);
            Log.i("chunks", String.valueOf(chunks.length));
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payload = new String(decoder.decode(chunks[1]));
            payload = payload.replace("{","")
                    .replace("}","");
            String[] payloadData = payload.split(",");
            userID =  payloadData[0].replace("\"id\":","").replace("\"","");
        }


        String finalUserID = userID;
        String finalToken = token;
        localFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent localHistory = new Intent(UserActivity.this, HistoryActivity.class);
                localHistory.putExtra("userID", finalUserID);
                localHistory.putExtra("userLoggedIn", true);
                localHistory.putExtra("token", finalToken);
                UserActivity.this.startActivity(localHistory);
            }
        });

        syncedFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userItems = new String[0];
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL url = new URL("http://87.229.85.225:42069/track/" + finalUserID);
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
                                    String trackID = subitems[0].replace("\"trackID\":", "").replace("\"","");
                                    String startTime = subitems[1].replace("\"startTime\":", "").replace("\"","");
                                    String endTime = subitems[2].replace("\"endTime\":", "").replace("\"","");
                                    String totalDistance = subitems[3].replace("\"totalDistance\":", "").replace("\"","");
                                    String averageVelocity = subitems[4].replace("\"averageVelocity\":", "").replace("\"","");

                                    addNewItem("trackID: " + trackID + "\nBetween " + startTime +
                                            " and " + endTime + "\n" +
                                            String.format("Total distance of: %.2f m", Double.valueOf(totalDistance)) +
                                            "\n" + String.format("With average velocity: %.2f km/h", Double.valueOf(averageVelocity)));
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

                if (userItems.length > 0) {
                    Intent syncedHistory = new Intent(UserActivity.this, SyncedHistory.class);
                    syncedHistory.putExtra("userSyncedHistory", userItems);
                    syncedHistory.putExtra("token", finalToken);
                    UserActivity.this.startActivity(syncedHistory);
                } else {
                    Toast.makeText(UserActivity.this, "You don't have any synced track data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void addNewItem(String item) {
        userItems = Arrays.copyOf(userItems, userItems.length + 1);
        userItems[userItems.length - 1] = item;
    }
}
