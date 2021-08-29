package io.focuslauncher.phone.managers;

import android.content.Context;
import android.widget.Toast;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.EvernoteUtil;
import com.evernote.client.android.asyncclient.EvernoteCallback;
import com.evernote.client.android.asyncclient.EvernoteNoteStoreClient;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.focuslauncher.phone.app.CoreApplication;
import io.focuslauncher.phone.log.Tracer;

import static io.focuslauncher.phone.utils.DataUtils.NOTE_BODY;
import static io.focuslauncher.phone.utils.DataUtils.NOTE_TITLE;

/**
 * Created by Shahab on 2/7/2017.
 */
@SuppressWarnings("DefaultFileTemplate")
public class EvernoteManager {

    public void createSiempoNotebook() {
        if (!EvernoteSession.getInstance().isLoggedIn()) {
            return;
        }

        EvernoteNoteStoreClient noteStoreClient = EvernoteSession.getInstance().getEvernoteClientFactory().getNoteStoreClient();
        Notebook siempoNotebook = new Notebook();
        siempoNotebook.setName("Siempo");
        siempoNotebook.setDefaultNotebook(true);
        siempoNotebook.setPublished(true);
        noteStoreClient.createNotebookAsync(siempoNotebook, new EvernoteCallback<Notebook>() {
            @Override
            public void onSuccess(Notebook result) {
                Tracer.i("Note || Siempo notebook created: " + result);
            }

            @Override
            public void onException(Exception exception) {
                Tracer.e(exception, "Siempo notebook creation failed: " + exception.getMessage());
            }
        });
    }

    public void createNote(JSONObject newNoteObject) {
        if (!EvernoteSession.getInstance().isLoggedIn()) {
            return;
        }

        EvernoteNoteStoreClient noteStoreClient = EvernoteSession.getInstance().getEvernoteClientFactory().getNoteStoreClient();

        Note note = new Note();
        try {
            note.setTitle(newNoteObject.getString(NOTE_TITLE));
            note.setContent(EvernoteUtil.NOTE_PREFIX + newNoteObject.getString(NOTE_BODY) + EvernoteUtil.NOTE_SUFFIX);

        } catch (JSONException e) {
            CoreApplication.getInstance().logException(e);
            Tracer.e(e, e.getMessage());
        }

        noteStoreClient.createNoteAsync(note, new EvernoteCallback<Note>() {
            @Override
            public void onSuccess(Note result) {
                Tracer.i("Note || Siempo note successfully created: " + result);
                //Toast.makeText(getApplicationContext(), result.getTitle() + " has been created", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onException(Exception exception) {
                Tracer.e(exception, "Error creating note: " + exception.getMessage());
            }
        });
    }

    public void listNoteBooks(String guid) {
        if (!EvernoteSession.getInstance().isLoggedIn()) {
            return;
        }

        EvernoteNoteStoreClient noteStoreClient = EvernoteSession.getInstance().getEvernoteClientFactory().getNoteStoreClient();
        noteStoreClient.listNotebooksAsync(new EvernoteCallback<List<Notebook>>() {
            @Override
            public void onSuccess(List<Notebook> result) {
                List<String> namesList = new ArrayList<>(result.size());
                for (Notebook notebook : result) {
                    namesList.add(notebook.getName());
                    Tracer.i("Note || List notebooks: " + notebook);
                }
            }

            @Override
            public void onException(Exception exception) {
                Tracer.e(exception, "Error retrieving notebooks: " + exception.getMessage());
            }
        });

        /*
        //create filter for findNotesMetadata
        NoteFilter filter = new NoteFilter();
        //set the notebook guid filter to the GUID of the default notebook
        filter.setNotebookGuid(guid); //change this to the GUID of the notebook you want
        //create a new result spec for findNotesMetadata
        NotesMetadataResultSpec resultSpec = new NotesMetadataResultSpec();
        //set the result spec to include titles
        resultSpec.setIncludeTitle(true);
        //call findNotesMetadata on the note store

        noteStoreClient.findNotesMetadataAsync(filter, 0, 100, resultSpec, new EvernoteCallback<NotesMetadataList>() {
            @Override
            public void onSuccess(NotesMetadataList result) {
            }

            @Override
            public void onException(Exception exception) {

            }
        });
        */
    }

    public void deleteNote(final Context context) {
        if (!EvernoteSession.getInstance().isLoggedIn()) {
            return;
        }

        EvernoteNoteStoreClient noteStoreClient = EvernoteSession.getInstance().getEvernoteClientFactory().getNoteStoreClient();
        noteStoreClient.deleteNoteAsync("", new EvernoteCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                Toast.makeText(context.getApplicationContext(), "Note has been deleted successfully", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onException(Exception exception) {
                Tracer.e(exception, "Error deleting note: " + exception.getMessage());
            }
        });
    }

    public void sync() {

    }
}
