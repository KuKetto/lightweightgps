package pe.saydomonkos.lightweightgps;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SyncedHistory extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.synced_history);
        getSupportActionBar().hide();

        Bundle data = getIntent().getExtras();
        String[] userSyncedHistory = new String[0];
        String token = "";
        if (data != null) {
            userSyncedHistory = data.getStringArray("userSyncedHistory");
            token = data.getString("token");
        }

        ListView listView = findViewById(R.id.synced_history_list_view);
        listView.setAdapter(new SyncedHistoryViewAdapter(SyncedHistory.this, userSyncedHistory));

        String[] finalUserSyncedHistory = userSyncedHistory;
        String finalToken = token;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String trackID = finalUserSyncedHistory[i].split("\n")[0].replace("trackID: ", "");
                Intent syncedHistoryItem = new Intent(SyncedHistory.this, SyncedHistoryItem.class);
                syncedHistoryItem.putExtra("trackID", trackID);
                syncedHistoryItem.putExtra("token", finalToken);
                SyncedHistory.this.startActivity(syncedHistoryItem);
            }
        });
    }
}
