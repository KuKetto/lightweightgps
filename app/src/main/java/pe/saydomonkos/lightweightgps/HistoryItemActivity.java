package pe.saydomonkos.lightweightgps;

import static pe.saydomonkos.lightweightgps.HistoryViewAdapter.getRandom;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class HistoryItemActivity  extends AppCompatActivity {

    private String file;

    private double totalDistance;
    private double averageVelocity;
    private TextView totalDistanceTextView;
    private TextView averageVelocityTextView;
    private Button calcVelocityBtn;
    private HistoryItem historyItem;
    private boolean wasCalcVelocityBtnPressed = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_item);
        getSupportActionBar().hide();

        totalDistanceTextView = findViewById(R.id.totalDistanceTextView);
        averageVelocityTextView = findViewById(R.id.averageVelocityTextView);
        calcVelocityBtn = findViewById(R.id.velocityCalcBtn);

        historyItem = new HistoryItem();
        if (savedInstanceState != null) {
            wasCalcVelocityBtnPressed = savedInstanceState.getBoolean("wasCalcVelocityBtnPressed");
            Bundle historyItemBundle = savedInstanceState.getBundle("historyItemBundle");
            historyItem.totalDistance(historyItemBundle.getDouble("totalDistance"));
            historyItem.averageVelocity(historyItemBundle.getDouble("averageVelocity"));
            if (wasCalcVelocityBtnPressed) {
                historyItem.remainingDistance(historyItemBundle.getDouble("remainingDistance"));
                historyItem.velocityOnRemainingDistance(historyItemBundle.getDouble("velocityOnRemainingDistance"));
                historyItem.velocities(historyItemBundle.getDoubleArray("velocities"));
                initScrollView(historyItem);
            }

            if (totalDistance == -1) {
                totalDistanceTextView.setText("Unexpected error");
                averageVelocityTextView.setText("Unexpected error");
            } else {
                totalDistanceTextView.setText(String.format("Total distance: %.2f m", historyItem.getTotalDistance()) );
                averageVelocityTextView.setText(String.format("Average velocity: %.2f km/h", historyItem.getAverageVelocity()));
            }
        } else {
            Bundle data = getIntent().getExtras();
            if (data != null) {
                file = data.getString("filename");
                totalDistance = data.getDouble("totalDistance");
                averageVelocity = data.getDouble("averageVelocity");

                historyItem.totalDistance(totalDistance).averageVelocity(averageVelocity);

                totalDistanceTextView.setText(String.format("Total distance: %.2f m", totalDistance) );
                averageVelocityTextView.setText(String.format("Average velocity: %.2f km/h", averageVelocity));
            } else {
                totalDistanceTextView.setText("Unexpected error");
                averageVelocityTextView.setText("Unexpected error");
                historyItem.totalDistance(-1).averageVelocity(-1);
            }
        }

        setViewBackground(totalDistanceTextView, HistoryItemActivity.this);
        setViewBackground(averageVelocityTextView, HistoryItemActivity.this);
        setViewBackground(calcVelocityBtn, HistoryItemActivity.this);

        calcVelocityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wasCalcVelocityBtnPressed = true;
                PostProcess postProcess = new PostProcess();
                HistoryItem velocityHistoryItemData = postProcess.calculateVelocities(HistoryItemActivity.this, file);
                initScrollView(velocityHistoryItemData);
                historyItem.velocities(velocityHistoryItemData.getVelocities())
                            .remainingDistance(velocityHistoryItemData.getRemainingDistance())
                            .velocityOnRemainingDistance(velocityHistoryItemData.getVelocityOnRemainingDistance());
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        /* on screen rotation application is recreated or
        while the app is in the background, it could be killed to free up ram
        so we need to save our application state to be able to restore it later
        */
        super.onSaveInstanceState(outState);
        outState.putBoolean("wasCalcVelocityBtnPressed", wasCalcVelocityBtnPressed);
        outState.putBundle("historyItemBundle", historyItem.getHistoryItemBundled());
    }

    private void initScrollView(HistoryItem historyItem) {
        calcVelocityBtn.setVisibility(View.GONE);
        RelativeLayout relativeLayout = findViewById(R.id.relativeScrollView);
        double[] velocities = historyItem.getVelocities();
        for (int i = 0; i < velocities.length; i++) {
            TextView textView = new TextView(HistoryItemActivity.this);
            setViewBackground(textView, HistoryItemActivity.this);
            textView.setId(42000 + i);
            textView.setTextSize(22);
            textView.setText(String.format("\t\t%d - %d\t\t\n%.2f km/h", i * 1000, (i + 1) * 1000, velocities[i]));
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 20, 0, 0);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, 42000 + i);
            if (i > 0) layoutParams.addRule(RelativeLayout.BELOW, 42000 + (i-1));
            textView.setLayoutParams(layoutParams);
            textView.setGravity(Gravity.CENTER);
            textView.setWidth(400);
            relativeLayout.addView(textView);
        }
        TextView textView = new TextView(HistoryItemActivity.this);
        setViewBackground(textView, HistoryItemActivity.this);
        textView.setTextSize(22);
        textView.setId(42000 + velocities.length);
        textView.setText(String.format("\t\t%d - %.2f\t\t\n%.2f km/h",
                velocities.length * 1000, velocities.length * 1000 + historyItem.getRemainingDistance(),
                historyItem.getVelocityOnRemainingDistance()));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 20, 0, 0);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, 42000 + velocities.length);
        layoutParams.addRule(RelativeLayout.BELOW, 42000 + (velocities.length - 1));
        textView.setLayoutParams(layoutParams);
        textView.setGravity(Gravity.CENTER);
        textView.setWidth(400);
        relativeLayout.addView(textView);
    }

    public static void setViewBackground(View view, Context context) {
        int randomNumber = getRandom(7);
        switch (randomNumber) {
            case 1:
                view.setBackground(ContextCompat.getDrawable(context, R.drawable.gradient1_rounded_textview));
                break;
            case 2:
                view.setBackground(ContextCompat.getDrawable(context, R.drawable.gradient2_rounded_textview));
                break;
            case 3:
                view.setBackground(ContextCompat.getDrawable(context, R.drawable.gradient3_rounded_textview));
                break;
            case 4:
                view.setBackground(ContextCompat.getDrawable(context, R.drawable.gradient4_rounded_textview));
                break;
            case 5:
                view.setBackground(ContextCompat.getDrawable(context, R.drawable.gradient5_rounded_textview));
                break;
            case 6:
                view.setBackground(ContextCompat.getDrawable(context, R.drawable.gradient6_rounded_textview));
                break;
            case 7:
                view.setBackground(ContextCompat.getDrawable(context, R.drawable.gradient7_rounded_textview));
                break;
            default:
                view.setBackground(ContextCompat.getDrawable(context, R.drawable.gradient8_rounded_textview));
                break;
        }
    }
}
