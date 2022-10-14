package pe.saydomonkos.lightweightgps;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class LogHandler {
    private String filename = "";
    private FileOutputStream logFileStream;
    private boolean isClosed = true;
    Context context;

    public LogHandler(Context context){
        this.context = context;
    }

    public void renameLogFile(String newFileName) {
        File currentFile = new File(context.getExternalFilesDir("/logs"), filename);
        File renamedFile = new File(context.getExternalFilesDir("/logs"), newFileName);
        currentFile.renameTo(renamedFile);
    }

    public boolean open() {
        try {
            logFileStream = new FileOutputStream(new File(context.getExternalFilesDir("/logs"), this.filename));
            isClosed = false;
            Log.i("ok",this.filename);
            return true;
        } catch (FileNotFoundException e) {
            Log.e("err", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean log(String stringToLog) {
        try {
            logFileStream.write(stringToLog.getBytes(StandardCharsets.UTF_8));
            Log.i("ok",stringToLog);
            return true;
        } catch (IOException e) {
            Log.e("err", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean close() {
        try {
            logFileStream.close();
            Log.i("ok","closed");
            isClosed = true;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public LogHandler filename(String filename) {
        this.filename = filename;
        return this;
    }

    public String getFilename() {
        return filename;
    }

    public boolean isClosed() {
        return isClosed;
    }
}
