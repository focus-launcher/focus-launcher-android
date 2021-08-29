package io.focuslauncher.phone.utils;

import android.content.Context;
import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import io.focuslauncher.R;
import io.focuslauncher.phone.activities.CoreActivity;
import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.log.Tracer;



    /*
    *   JSON file structure:
    *
    *   root_OBJ:{
    *       notes_ARR:[
    *           newNote_OBJ:{
    *             "title":"", "body":"", "colour":"", "favoured":true/false,
    *                   "fontSize":14/18/22, "hideBody":true/false},
    *           newNote_OBJ:{
    *             "title":"", "body":"", "colour":"", "favoured":true/false,
    *                   "fontSize":14/18/22, "hideBody":true/false}, etc
    *       ]
    *   };
    */


public class DataUtils {

    public static final String NOTES_FILE_NAME = "notes.json"; // Local notes file name
    public static final String BACKUP_FOLDER_PATH = "/Notes"; // Backup folder path
    public static final String BACKUP_FILE_NAME = "ebbNotes_backup.json"; // Backup file name
    // Note data constants used in intents and in key-value store
    public static final int NEW_NOTE_REQUEST = 60000;
    public static final String NOTE_REQUEST_CODE = "requestCode";
    public static final String NOTE_TITLE = "title";
    public static final String NOTE_BODY = "body";
    public static final String NOTE_COLOUR = "colour";
    public static final String NOTE_FAVOURED = "favoured";
    public static final String NOTE_FONT_SIZE = "fontSize";
    public static final String NOTE_HIDE_BODY = "hideBody";
    private static final String NOTES_ARRAY_NAME = "notes"; // Root object name
    public static int height = 0;

    /**
     * Wrap 'notes' array into a root object and store in file 'toFile'
     *
     * @param toFile File to store notes into
     * @param notes  Array of notes to be saved
     * @return true if successfully saved, false otherwise
     */
    public static boolean saveData(File toFile, JSONArray notes) {
        Boolean successful = false;

        JSONObject root = new JSONObject();

        // If passed notes not null -> wrap in root JSONObject
        if (notes != null) {
            try {
                root.put(NOTES_ARRAY_NAME, notes);

            } catch (JSONException e) {
                CoreApplication.getInstance().logException(e);
                e.printStackTrace();
                return false;
            }
        }

        // If passed notes null -> return false
        else
            return false;

        // If file is backup and it doesn't exist -> create file
        if (toFile == CoreActivity.getBackupPath()) {
            if (isExternalStorageReadable() && isExternalStorageWritable()) {
                if (!toFile.exists()) {
                    try {
                        Boolean created = toFile.createNewFile();

                        // If file failed to create -> return false
                        if (!created)
                            return false;

                    } catch (IOException e) {
                        CoreApplication.getInstance().logException(e);
                        e.printStackTrace();
                        return false; // If file creation threw exception -> return false
                    }
                }
            }

            // If external storage not readable/writable -> return false
            else
                return false;
        }

        // If file is local and it doesn't exist -> create file
        else if (toFile == CoreActivity.getLocalPath() && !toFile.exists()) {
            try {
                Boolean created = toFile.createNewFile();

                // If file failed to create -> return false
                if (!created)
                    return false;

            } catch (IOException e) {
                CoreApplication.getInstance().logException(e);
                e.printStackTrace();
                return false; // If file creation threw exception -> return false
            }
        }


        BufferedWriter bufferedWriter = null;

        try {
            // Initialize BufferedWriter with FileWriter and write root object to file
            bufferedWriter = new BufferedWriter(new FileWriter(toFile));
            bufferedWriter.write(root.toString());

            // If we got to this stage without throwing an exception -> set successful to true
            successful = true;

        } catch (IOException e) {
            CoreApplication.getInstance().logException(e);
            // If something went wrong in try block -> set successful to false
            successful = false;
            e.printStackTrace();

        } finally {
            // Finally, if bufferedWriter not null -> flush and close it
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.flush();
                    bufferedWriter.close();

                } catch (IOException e) {
                    CoreApplication.getInstance().logException(e);
                    e.printStackTrace();
                }
            }
        }

        return successful;
    }


    /**
     * Read from file 'fromFile' and return parsed JSONArray of notes
     *
     * @param fromFile File we are reading from
     * @return JSONArray of notes
     */
    public static JSONArray retrieveData(File fromFile) {
        JSONArray notes = null;

        // If file is backup and it doesn't exist -> return null
        if (fromFile == CoreActivity.getBackupPath()) {
            if (isExternalStorageReadable() && !fromFile.exists()) {
                return null;
            }
        }

        /*
         * If file is local and it doesn't exist ->
         * Initialize notes JSONArray as new and save into local file
         */
        else if (fromFile == CoreActivity.getLocalPath() && !fromFile.exists()) {
            notes = new JSONArray();

            Boolean successfulSaveToLocal = saveData(fromFile, notes);

            // If save successful -> return new notes
            if (successfulSaveToLocal) {
                return notes;
            }

            // Else -> return null
            return null;
        }


        JSONObject root = null;
        BufferedReader bufferedReader = null;

        try {
            // Initialize BufferedReader, read from 'fromFile' and store into root object
            bufferedReader = new BufferedReader(new FileReader(fromFile));

            StringBuilder text = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                text.append(line);
            }

            root = new JSONObject(text.toString());

        } catch (IOException | JSONException e) {
            CoreApplication.getInstance().logException(e);
            e.printStackTrace();

        } finally {
            // Finally, if bufferedReader not null -> close it
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();

                } catch (IOException e) {
                    CoreApplication.getInstance().logException(e);
                    e.printStackTrace();
                }
            }
        }

        // If root is not null -> get notes array from root object
        if (root != null) {
            try {
                notes = root.getJSONArray(NOTES_ARRAY_NAME);

            } catch (JSONException e) {
                CoreApplication.getInstance().logException(e);
                e.printStackTrace();
            }
        }

        // Return fetches notes < May return null! >
        return notes;
    }


    /**
     * Create new JSONArray of notes from 'from' without the notes at positions in 'selectedNotes'
     *
     * @param from          Main notes array to delete from
     * @param selectedNotes ArrayList of Integer which represent note positions to be deleted
     * @return New JSONArray of notes without the notes at positions 'selectedNotes'
     */
    public static JSONArray deleteNotes(JSONArray from, ArrayList<Integer> selectedNotes) {
        // Init new JSONArray
        JSONArray newNotes = new JSONArray();

        // Loop through main notes
        for (int i = 0; i < from.length(); i++) {
            // If array of positions to delete doesn't contain current position -> put in new array
            if (!selectedNotes.contains(i)) {
                try {
                    newNotes.put(from.get(i));

                } catch (JSONException e) {
                    CoreApplication.getInstance().logException(e);
                    e.printStackTrace();
                }
            }
        }

        // Finally, return the new notes
        return newNotes;
    }


    /**
     * Check if external storage is writable or not
     *
     * @return true if writable, false otherwise
     */
    public static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * Check if external storage is readable or not
     *
     * @return true if readable, false otherwise
     */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();

        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    /**
     * Save not from the IF field.
     *
     * @param context
     * @param title
     */
    public static void saveNotes(Context context, String title) {

        JSONObject newNoteObject = null;
        File localPath = new File(context.getFilesDir() + "/" + DataUtils.NOTES_FILE_NAME);

        // Init notes array
        JSONArray notes = new JSONArray();

        // Retrieve from local path
        JSONArray tempNotes = DataUtils.retrieveData(localPath);

        // If not null -> equal main notes to retrieved notes
        if (tempNotes != null)
            notes = tempNotes;

        Tracer.i("All notes: ", notes);

        try {
            // Add new note to array
            newNoteObject = new JSONObject();
//            newNoteObject.put(NOTE_TITLE, SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT).format(new Date()));
            newNoteObject.put(DataUtils.NOTE_TITLE, getTitle(title));
            // newNoteObject.put(NOTE_BODY, intent.getStringExtra(NOTE_BODY));
            newNoteObject.put(DataUtils.NOTE_BODY, "");
            newNoteObject.put(DataUtils.NOTE_COLOUR, "#FFFFFF");
            newNoteObject.put(DataUtils.NOTE_FAVOURED, false);
            newNoteObject.put(DataUtils.NOTE_FONT_SIZE, 18);
            newNoteObject.put(DataUtils.NOTE_HIDE_BODY, false);

            notes.put(newNoteObject);

            Tracer.i("New note: " + newNoteObject);

        } catch (JSONException e) {
            CoreApplication.getInstance().logException(e);
            Tracer.e(e, e.getMessage());
        }

        // If newNoteObject not null -> save notes array to local file and notify adapter

        Boolean saveSuccessful = DataUtils.saveData(localPath, notes);

        if (saveSuccessful) {
            UIUtils.toast(context, context.getString(R.string.msg_noteCreated));
        }
    }

    private static String getTitle(String body) {
        if (body.isEmpty()) return "";
        String[] splits = body.split(" ");
        String ret = "";

        if (splits.length > 0) {
            ret += splits[0];
            ret += " ";
        }

        if (splits.length > 1) {
            ret += splits[1];
            ret += " ";
        }


        if (splits.length > 2) {
            ret += splits[2];
        }

        return ret;

    }
}
