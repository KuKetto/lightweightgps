package pe.saydomonkos.lightweightgps;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class PostProcess {

    private LinkedHashMap<String, Pair<Float, Float>> readFile(Context context, String filename) throws IOException {
        LinkedHashMap<String, Pair<Float, Float>> container = new LinkedHashMap<String,Pair<Float, Float>>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(context.getExternalFilesDir("/logs"), filename))));
        while (reader.ready()) {
            String[] line = reader.readLine().split(";");
            if (line[0].equals("INIT") || line[0].equals("EOF")) continue;
            Pair<Float, Float> coordinates = new Pair<Float, Float>(Float.valueOf(line[1]), Float.valueOf(line[2]));
            container.put(line[0], coordinates);
        }
        return container;
    }

    public HistoryItem runDistanceCalculationOnThreads(Context context,  String filename) {
        try {
            LinkedHashMap<String, Pair<Float, Float>> container = readFile(context, filename);
            ArrayList<Double> distances = new ArrayList<Double>();
            String[] containerIndexes = container.keySet().toArray(new String[container.size()]);
            Thread[] threads = new Thread[container.size() - 1];
            for (int i = 0; i < container.size()-1; i++) {
                float lat1 = container.get(containerIndexes[i]).first;
                float lon1 = container.get(containerIndexes[i]).second;
                float lat2 = container.get(containerIndexes[i+1]).first;
                float lon2 = container.get(containerIndexes[i+1]).second;
                Runnable runnable = () -> {
                    distances.add(calculateDistance(lat1, lat2, lon1, lon2));
                };
                threads[i] = new Thread(runnable);
                threads[i].start();
            }
            for (Thread thread : threads) thread.join();
            double sum = 0;
            Log.i("distances", String.valueOf(distances.size()));
            Log.i("file", filename);
            Log.i("container", String.valueOf(container.size()));
            Log.i("container indexes", String.valueOf(containerIndexes.length));

            for (int i = 0; i < distances.size(); i++) sum += distances.get(i);
            Timestamp start = Timestamp.valueOf(containerIndexes[0].replace("_", " "));
            Timestamp end = Timestamp.valueOf(containerIndexes[containerIndexes.length - 1].replace("_", " "));
            double actualDuration = Math.abs(end.getTime() - start.getTime());
            double hours = ((actualDuration / (1000*60*60)) % 24);
            double averageVelocity = (sum / 1000) / hours;

            HistoryItem historyItem = new HistoryItem().totalDistance(sum).averageVelocity(averageVelocity);
            return historyItem;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return new HistoryItem();
    }

    public HistoryItem calculateVelocities(Context context, String filename) {
        try {
            LinkedHashMap<String, Pair<Float, Float>> container = readFile(context, filename);
            ArrayList<Double> distances = new ArrayList<Double>();
            String[] containerIndexes = container.keySet().toArray(new String[container.size()]);

            double[] velocities = new double[0];
            double currentDistance = 0;
            Timestamp currentDistanceStartTime = Timestamp.valueOf(containerIndexes[0].replace("_", " "));
            for (int i = 0; i < container.size()-1; i++) {
                float lat1 = container.get(containerIndexes[i]).first;
                float lon1 = container.get(containerIndexes[i]).second;
                float lat2 = container.get(containerIndexes[i+1]).first;
                float lon2 = container.get(containerIndexes[i+1]).second;

                currentDistance += calculateDistance(lat1, lat2, lon1, lon2);
                if (currentDistance > 1000) {
                    Timestamp currentDistanceStopTime = Timestamp.valueOf(containerIndexes[i+1].replace("_", " "));
                    double duration = Math.abs(currentDistanceStopTime.getTime() - currentDistanceStartTime.getTime());
                    double hours = ((duration / (1000*60*60)) % 24);
                    double velocity = (currentDistance / 1000) / hours;

                    velocities = Arrays.copyOf(velocities, velocities.length + 1);
                    velocities[velocities.length - 1] = velocity;

                    currentDistance = currentDistance - 1000;
                    currentDistanceStartTime = Timestamp.valueOf(containerIndexes[i+1].replace("_", " "));
                } else continue;
            }
            Timestamp stopTime = Timestamp.valueOf(containerIndexes[containerIndexes.length - 1].replace("_", " "));
            double actualDuration = Math.abs(stopTime.getTime() - currentDistanceStartTime.getTime());
            double hours = ((actualDuration / (1000*60*60)) % 24);
            double velocity = (currentDistance / 1000) / hours;

            HistoryItem historyItem = new HistoryItem().velocities(velocities).remainingDistance(currentDistance).velocityOnRemainingDistance(velocity);
            return historyItem;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new HistoryItem();
    }

    private double calculateDistance(float lat1, float lat2, float lon1, float lon2) {
        double φ1 = lat1 * Math.PI/180;
        double φ2 = lat2 * Math.PI/180;
        double Δφ = (lat2-lat1) * Math.PI/180;
        double Δλ = (lon2-lon1) * Math.PI/180;

        double a = Math.sin(Δφ/2) * Math.sin(Δφ/2) + Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ/2) * Math.sin(Δλ/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return 6371e3 * c;
    }
}
