package com.umnvd.sensetestapp.data;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import androidx.annotation.StringRes;

import com.umnvd.sensetestapp.R;
import com.umnvd.sensetestapp.models.DataPoint;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CSVFileReader {

    private static volatile CSVFileReader instance;

    private final ContentResolver contentResolver;
    private final ExecutorService executor;
    private final Handler mainHandler;

    private Uri cachedUri;
    private List<DataPoint> cachedPoints;

    public static CSVFileReader getInstance(Context applicationContext) {
        if (instance == null) {
            synchronized (CSVFileReader.class) {
                if (instance == null) instance = new CSVFileReader(applicationContext);
            }
        }
        return instance;
    }

    public void read(Uri uri, SuccessCallback successCallback, ErrorCallback errorCallBack) {
        if (uri.equals(cachedUri) && cachedPoints != null && !cachedPoints.isEmpty()) {
            successCallback.onSuccess(cachedPoints);
        } else {
            executor.submit(() -> {
                try {
                    List<DataPoint> points = readCSVFile(uri);
                    postSuccess(points, successCallback);
                } catch (FileNotFoundException e) {
                    postError(R.string.file_not_found_error, errorCallBack);
                } catch (IOException e) {
                    postError(R.string.file_read_error, errorCallBack);
                } catch (NumberFormatException e) {
                    postError(R.string.csv_parse_error, errorCallBack);
                }
            });
        }
    }

    private List<DataPoint> readCSVFile(Uri uri) throws IOException {
        List<DataPoint> result = new ArrayList<>();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(contentResolver.openInputStream(uri))
        );

        String line;
        while ((line = reader.readLine()) != null) {
            String[] row = line.split(",");
            result.add(new DataPoint(
                    Integer.parseInt(row[0]),
                    Integer.parseInt(row[1])
            ));
        }

        cachedUri = uri;
        cachedPoints = result;
        return result;
    }

    private void postSuccess(List<DataPoint> points, SuccessCallback callback) {
        if (callback != null) {
            mainHandler.post(() -> callback.onSuccess(points));
        }
    }

    private void postError(int messageId, ErrorCallback callback) {
        if (callback != null) {
            mainHandler.post(() -> callback.onError(messageId));
        }
    }

    private CSVFileReader(Context applicationContext) {
        contentResolver = applicationContext.getContentResolver();
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(applicationContext.getMainLooper());
    }

    public interface SuccessCallback { void onSuccess(List<DataPoint> points);}
    public interface ErrorCallback { void onError(@StringRes int messageId);}

}
