package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import com.abbyy.ocrsdk.BusCardSettings;
import com.abbyy.ocrsdk.Client;
import com.abbyy.ocrsdk.Task;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Adapted form ABBYY Cloud OCR Github Sample Project
 */
public class OCRAsyncTask extends DialogAsyncTask<String, String, Boolean> {
    private static final int TASK_ID = 42;
    private static final String TAG = "OCRAsyncTask";
    private static final String instIdName = "installationId";

    public OCRAsyncTask(String initialStatus, Context context, Callbacks callbacks) {
        super(initialStatus, context, callbacks, TASK_ID);
    }

    @Override
    protected Boolean doInBackground(String... params) {
        String inputFile = params[0];
        String tempFile = params[1];

        try {
            //Setup a Client to interact with the cloud OCR service
            Client restClient = new Client();
            //Set the ID and password for our application
            restClient.applicationId = "Business Card Contact";
            restClient.password = "g0aDEnHkRELtduP3qlynsErQ";

            //If we are running for the first time, obtain the installation id
            SharedPreferences settings = super.getContext()
                    .getSharedPreferences("abbyy_settings", Context.MODE_PRIVATE);
            if(!settings.contains(instIdName)) {
                //Get installation id from server using device id
                String deviceId = Settings.Secure
                        .getString(super.getContext().getContentResolver(),
                                Settings.Secure.ANDROID_ID);

                //Obtain installation id from server
                publishProgress( "First run: obtaining installation id..");
                String installationId = restClient.activateNewInstallation(deviceId);
                publishProgress( "Done. Installation id is '" + installationId + "'");

                SharedPreferences.Editor editor = settings.edit();
                editor.putString(instIdName, installationId);
                editor.commit();
            }

            String installationId = settings.getString(instIdName, "");
            restClient.applicationId += installationId;

            publishProgress("Uploading image...");

            String language = "English";

            BusCardSettings busCardSettings = new BusCardSettings();
            busCardSettings.setLanguage(language);
            busCardSettings.setOutputFormat(BusCardSettings.OutputFormat.xml);
            Task task = restClient.processBusinessCard(inputFile, busCardSettings);

            publishProgress("Uploading...");

            while ( task.isTaskActive() ) {
                // Check on the OCR task status every 3 seconds, more frequently than every
                // 2 seconds is discouraged
                Thread.sleep(3000);
                publishProgress("Processing..");
                task = restClient.getTaskStatus(task.Id);
            }

            if(task.Status == Task.TaskStatus.Completed) {
                publishProgress("Downloading..");
                //FileOutputStream fos = super.getContext()
                //        .openFileOutput(tempFile, Context.MODE_PRIVATE);
                File tmpFile = new File(super.getContext()
                        .getExternalFilesDir(Environment.DIRECTORY_PICTURES),tempFile);
                FileOutputStream fos = new FileOutputStream(tmpFile);

                try {
                    restClient.downloadResult(task, fos);
                } finally {
                    fos.close();
                }

                publishProgress("Ready");
            } else if( task.Status == Task.TaskStatus.NotEnoughCredits ) {
                throw new Exception("Not enough credits to process task. " +
                        "Add more pages to your application's account.");
            } else {
                throw new Exception("Task failed");
            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error during OCR: " + e.getMessage(), e);
            return false;
        }
    }
}
