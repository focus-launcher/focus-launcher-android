package io.focuslauncher.phone.activities

import android.widget.AbsListView.MultiChoiceModeListener
import com.evernote.client.android.login.EvernoteLoginFragment
import io.focuslauncher.phone.adapters.NoteAdapter
import org.json.JSONObject
import org.json.JSONException
import io.focuslauncher.phone.app.CoreApplication
import org.json.JSONArray
import android.os.Bundle
import android.os.Build
import android.content.pm.ActivityInfo
import io.focuslauncher.R
import android.content.Intent
import androidx.core.view.MenuItemCompat
import com.evernote.client.android.EvernoteSession
import io.focuslauncher.phone.managers.EvernoteManager
import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.os.Environment
import android.view.*
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import io.focuslauncher.databinding.ActivityMainNotesBinding
import io.focuslauncher.phone.helper.FirebaseHelper
import io.focuslauncher.phone.log.Tracer
import io.focuslauncher.phone.utils.DataUtils
import io.focuslauncher.phone.utils.bindView
import io.focuslauncher.phone.utils.lifecycleProperty
import java.io.File
import java.lang.Exception
import java.util.ArrayList

class NoteListActivity : CoreActivity(), AdapterView.OnItemClickListener, Toolbar.OnMenuItemClickListener,
    MultiChoiceModeListener, SearchView.OnQueryTextListener, EvernoteLoginFragment.ResultCallback {

    private var binding: ActivityMainNotesBinding? by lifecycleProperty()

    private var notesAdapter: NoteAdapter? = null
    private var searchMenu: MenuItem? = null
    private var realIndexesOfSearchResults: ArrayList<Int>? = null
    private var lastFirstVisibleItem = -1
    private var newNoteButtonBaseYCoordinate = 0f
    private var backupCheckDialog: AlertDialog? = null
    private var backupOKDialog: AlertDialog? = null
    private var restoreCheckDialog: AlertDialog? = null
    private var restoreFailedDialog: AlertDialog? = null
    private var startTime: Long = 0


    fun setFavourite(favourite: Boolean, position: Int) {
        var newFavourite: JSONObject? = null

        // Get note at position and store in newFavourite
        try {
            newFavourite = notes!!.getJSONObject(position)
        } catch (e: JSONException) {
            CoreApplication.getInstance().logException(e)
            e.printStackTrace()
        }
        if (newFavourite != null) {
            if (favourite) {
                // Set favoured to true
                try {
                    newFavourite.put(DataUtils.NOTE_FAVOURED, true)
                } catch (e: JSONException) {
                    CoreApplication.getInstance().logException(e)
                    e.printStackTrace()
                }

                // If favoured note is not at position 0
                // Sort notes array so favoured note is first
                if (position > 0) {
                    val newArray = JSONArray()
                    try {
                        newArray.put(0, newFavourite)
                    } catch (e: JSONException) {
                        CoreApplication.getInstance().logException(e)
                        e.printStackTrace()
                    }

                    // Copy contents to new sorted array without favoured element
                    for (i in 0 until notes!!.length()) {
                        if (i != position) {
                            try {
                                newArray.put(notes!![i])
                            } catch (e: JSONException) {
                                CoreApplication.getInstance().logException(e)
                                e.printStackTrace()
                            }
                        }
                    }

                    // Equal main notes array with new sorted array and reset adapter
                    notes = newArray
                    notesAdapter = NoteAdapter(this@NoteListActivity, notes)
                    binding?.listView?.apply {
                        adapter = notesAdapter
                        post { smoothScrollToPosition(0) }
                    }
                } else {
                    try {
                        notes!!.put(position, newFavourite)
                    } catch (e: JSONException) {
                        CoreApplication.getInstance().logException(e)
                        e.printStackTrace()
                    }
                    notesAdapter!!.notifyDataSetChanged()
                }
            } else {
                try {
                    newFavourite.put(DataUtils.NOTE_FAVOURED, false)
                    notes!!.put(position, newFavourite)
                    val newArrFav = JSONArray()
                    val newArrunFAv = JSONArray()
                    for (i in 0 until notes!!.length()) {
                        val note = notes!!.getJSONObject(i)
                        val `val` = note[DataUtils.NOTE_FAVOURED].toString()
                        if (`val`.equals("true", ignoreCase = true)) {
                            newArrFav.put(notes!![i])
                        } else {
                            newArrunFAv.put(notes!![i])
                        }
                    }
                    try {
                        for (i in 0 until newArrunFAv.length()) {
                            val jsonObject = newArrunFAv.getJSONObject(i)
                            newArrFav.put(jsonObject)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    notes = newArrFav
                    notesAdapter!!.setAdapterData(notes)
                    notesAdapter!!.notifyDataSetChanged()
                } catch (e: JSONException) {
                    CoreApplication.getInstance().logException(e)
                    e.printStackTrace()
                }
            }
            // Save notes to local file
            DataUtils.saveData(localPath, notes)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Init realIndexes array
        realIndexesOfSearchResults = ArrayList()
        // Initialize local file path and backup file path
        localPath = File(filesDir.toString() + "/" + DataUtils.NOTES_FILE_NAME)
        val backupFolder = File(
            Environment.getExternalStorageDirectory().toString() +
                    DataUtils.BACKUP_FOLDER_PATH
        )
        if (DataUtils.isExternalStorageReadable() && DataUtils.isExternalStorageWritable() && !backupFolder.exists()) backupFolder.mkdir()
        backupPath = File(backupFolder, DataUtils.BACKUP_FILE_NAME)

        // Android version >= 18 -> set orientation userPortrait
        requestedOrientation =
            if (Build.VERSION.SDK_INT >= 18) ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        // Init notes array
        notes = JSONArray()

        // Retrieve from local path
        val tempNotes = DataUtils.retrieveData(localPath)

        // If not null -> equal main notes to retrieved notes
        if (tempNotes != null) notes = tempNotes
        binding = bindView(ActivityMainNotesBinding::inflate)
        // Init layout components
        initToolbar()
        newNoteButtonBaseYCoordinate = binding?.fab?.y ?: 0f

        // Initialize NoteAdapter with notes array
        notesAdapter = NoteAdapter(this@NoteListActivity, notes)


        binding?.listView?.apply {
            adapter = notesAdapter
            onItemClickListener = this@NoteListActivity
            choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
            setMultiChoiceModeListener(this@NoteListActivity)
            setOnScrollListener(object : AbsListView.OnScrollListener {
                override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                    if (lastFirstVisibleItem == -1) lastFirstVisibleItem = view.firstVisiblePosition

                    if (view.firstVisiblePosition > lastFirstVisibleItem) newNoteButtonVisibility(false) else if (view.firstVisiblePosition < lastFirstVisibleItem &&
                        !deleteActive && !searchActive
                    ) {
                        newNoteButtonVisibility(true)
                    }
                    lastFirstVisibleItem = view.firstVisiblePosition
                }

                override fun onScroll(
                    view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int,
                    totalItemCount: Int
                ) {
                }
            })
        }


        // If newNote button clicked -> Start NotesEditActivity intent with NEW_NOTE_REQUEST as request
        binding?.fab?.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@NoteListActivity, NotesEditActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            intent.putExtra(DataUtils.NOTE_REQUEST_CODE, DataUtils.NEW_NOTE_REQUEST)
            startActivityForResult(intent, DataUtils.NEW_NOTE_REQUEST)
        })

        // If no notes -> show 'Press + to add new note' text, invisible otherwise
        if (notes?.length() == 0) binding?.noNotes?.visibility = View.VISIBLE
        else binding?.noNotes?.visibility = View.INVISIBLE
        initDialogs(this)
        if (intent.getBooleanExtra(EXTRA_OPEN_LATEST, false)) {
            openEditActivity(notesAdapter!!.count - 1)
        }
    }

    override fun onResume() {
        super.onResume()
        startTime = System.currentTimeMillis()
        Tracer.i("Notes onResume called")
        // Retrieve from local path
        val tempNotes = DataUtils.retrieveData(localPath)
        Tracer.i("All notes: $tempNotes")

        // If not null -> equal main notes to retrieved notes
        if (tempNotes != null) {
            notes = tempNotes
            notesAdapter!!.setAdapterData(notes)
            notesAdapter!!.notifyDataSetChanged()
        }
    }

    /**
     * Initialize toolbar with required components such as
     * - title, menu/OnMenuItemClickListener and searchView -
     */
    protected fun initToolbar() = binding?.toolbarMain?.toolbar?.apply {
        setTitle(R.string.app_name)

        // Inflate menu_main to be displayed in the toolbar
        inflateMenu(R.menu.menu_main)

        // Set an OnMenuItemClickListener to handle menu item clicks
        //toolbar.setOnMenuItemClickListener(this);
        setNavigationOnClickListener { finish() }
        val menu = menu
        if (menu != null) {
            // Get 'Search' menu item
            searchMenu = menu.findItem(R.id.action_search)
            if (searchMenu != null) {
                // If the item menu not null -> get it's support action view
                val searchView = MenuItemCompat.getActionView(searchMenu) as SearchView
                if (searchView != null) {
                    // If searchView not null -> set query hint and open/query/close listeners
                    searchView.queryHint = getString(R.string.label_search)
                    searchView.setOnQueryTextListener(this@NoteListActivity)
                    MenuItemCompat.setOnActionExpandListener(searchMenu,
                        object : MenuItemCompat.OnActionExpandListener {
                            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                                searchActive = true
                                newNoteButtonVisibility(false)
                                // Disable long-click on listView to prevent deletion
                                binding?.listView?.isLongClickable = false

                                // Init realIndexes array
                                realIndexesOfSearchResults = ArrayList()
                                for (i in 0 until notes!!.length()) realIndexesOfSearchResults!!.add(i)
                                notesAdapter!!.notifyDataSetChanged()
                                return true
                            }

                            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                                searchEnded()
                                return true
                            }
                        })
                }
            }
        }
    }

    /**
     * Implementation of AlertDialogs such as
     * - backupCheckDialog, backupOKDialog, restoreCheckDialog, restoreFailedDialog -
     *
     * @param context The Activity context of the dialogs; in this case DashboardActivity context
     */
    protected fun initDialogs(context: Context?) {
        /*
         * Backup check dialog
         *  If not sure -> dismiss
         *  If yes -> check if notes length > 0
         *    If yes -> save current notes to backup file in backupPath
         */
        backupCheckDialog = AlertDialog.Builder(context)
            .setTitle(R.string.label_backup)
            .setMessage(R.string.prompt_backup)
            .setPositiveButton(R.string.label_yes) { dialog, which -> // If note array not empty -> continue
                if (notes!!.length() > 0) {
                    val backupSuccessful = DataUtils.saveData(backupPath, notes)
                    if (backupSuccessful) showBackupSuccessfulDialog() else {
                        val toast = Toast.makeText(
                            applicationContext,
                            resources.getString(R.string.msg_backupUnsuccessful),
                            Toast.LENGTH_SHORT
                        )
                        toast.show()
                    }
                } else {
                    val toast = Toast.makeText(
                        applicationContext,
                        resources.getString(R.string.msg_noNotesToBackup),
                        Toast.LENGTH_SHORT
                    )
                    toast.show()
                }
            }
            .setNegativeButton(R.string.label_no) { dialog, which -> dialog.dismiss() }
            .create()


        // Dialog to display backup was successfully created in backupPath
        backupOKDialog = AlertDialog.Builder(context)
            .setTitle(R.string.title_backupCreated)
            .setMessage(
                getString(R.string.msg_backupCreated) + " "
                        + backupPath.absolutePath
            )
            .setNeutralButton(android.R.string.ok) { dialog, which -> dialog.dismiss() }
            .create()


        /*
         * Restore check dialog
         *  If not sure -> dismiss
         *  If yes -> check if backup notes exists
         *    If not -> display restore failed dialog
         *    If yes -> retrieve notes from backup file and store into local file
         */restoreCheckDialog = AlertDialog.Builder(context)
            .setTitle(R.string.label_restore)
            .setMessage(R.string.prompt_restore)
            .setPositiveButton(R.string.label_yes) { dialog, which ->
                val tempNotes = DataUtils.retrieveData(backupPath)

                // If backup file exists -> copy backup notes to local file
                if (tempNotes != null) {
                    val restoreSuccessful = DataUtils.saveData(localPath, tempNotes)
                    if (restoreSuccessful) {
                        notes = tempNotes
                        notesAdapter = NoteAdapter(this@NoteListActivity, notes)
                        binding?.listView?.adapter = notesAdapter
                        val toast = Toast.makeText(
                            applicationContext,
                            resources.getString(R.string.msg_notesRestored),
                            Toast.LENGTH_SHORT
                        )
                        toast.show()

                        // If no notes -> show 'Press + to add new note' text, invisible otherwise
                        if (notes?.length() == 0) binding?.noNotes?.visibility = View.VISIBLE
                        else binding?.noNotes?.visibility = View.INVISIBLE
                    } else {
                        val toast = Toast.makeText(
                            applicationContext,
                            resources.getString(R.string.msg_restoreUnsuccessful),
                            Toast.LENGTH_SHORT
                        )
                        toast.show()
                    }
                } else showRestoreFailedDialog()
            }
            .setNegativeButton(R.string.label_no) { dialog, which -> dialog.dismiss() }
            .create()


        // Dialog to display restore failed when no backup file found
        restoreFailedDialog = AlertDialog.Builder(context)
            .setTitle(R.string.title_restoreFailed)
            .setMessage(R.string.msg_restoreFailed)
            .setNeutralButton(android.R.string.ok) { dialog, which -> dialog.dismiss() }
            .create()
    }

    // Method to dismiss backup check and show backup successful dialog
    protected fun showBackupSuccessfulDialog() {
        backupCheckDialog!!.dismiss()
        backupOKDialog!!.show()
    }

    // Method to dismiss restore check and show restore failed dialog
    protected fun showRestoreFailedDialog() {
        restoreCheckDialog!!.dismiss()
        restoreFailedDialog!!.show()
    }

    /**
     * If item clicked in list view -> Start NotesEditActivity intent with position as requestCode
     */
    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        openEditActivity(position)
    }

    private fun openEditActivity(position: Int) {
        try {
            if (realIndexesOfSearchResults != null) {
                val intent = Intent(this, NotesEditActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

                // If search is active -> use position from realIndexesOfSearchResults for NotesEditActivity
                if (searchActive && null != realIndexesOfSearchResults) {
                    val newPosition = realIndexesOfSearchResults!![position]
                    try {
                        // Package selected note content and send to NotesEditActivity
                        intent.putExtra(
                            DataUtils.NOTE_TITLE,
                            notes!!.getJSONObject(newPosition).getString(DataUtils.NOTE_TITLE)
                        )
                        intent.putExtra(
                            DataUtils.NOTE_BODY,
                            notes!!.getJSONObject(newPosition).getString(DataUtils.NOTE_BODY)
                        )
                        intent.putExtra(
                            DataUtils.NOTE_COLOUR,
                            notes!!.getJSONObject(newPosition).getString(DataUtils.NOTE_COLOUR)
                        )
                        intent.putExtra(
                            DataUtils.NOTE_FONT_SIZE,
                            notes!!.getJSONObject(newPosition).getInt(DataUtils.NOTE_FONT_SIZE)
                        )
                        if (notes!!.getJSONObject(newPosition).has(DataUtils.NOTE_HIDE_BODY)) {
                            intent.putExtra(
                                DataUtils.NOTE_HIDE_BODY,
                                notes!!.getJSONObject(newPosition).getBoolean(DataUtils.NOTE_HIDE_BODY)
                            )
                        } else intent.putExtra(DataUtils.NOTE_HIDE_BODY, false)
                    } catch (e: JSONException) {
                        CoreApplication.getInstance().logException(e)
                        e.printStackTrace()
                    }
                    intent.putExtra(DataUtils.NOTE_REQUEST_CODE, newPosition)
                    startActivityForResult(intent, newPosition)
                } else {
                    try {
                        // Package selected note content and send to NotesEditActivity
                        intent.putExtra(
                            DataUtils.NOTE_TITLE,
                            notes!!.getJSONObject(position).getString(DataUtils.NOTE_TITLE)
                        )
                        intent.putExtra(
                            DataUtils.NOTE_BODY,
                            notes!!.getJSONObject(position).getString(DataUtils.NOTE_BODY)
                        )
                        intent.putExtra(
                            DataUtils.NOTE_COLOUR,
                            notes!!.getJSONObject(position).getString(DataUtils.NOTE_COLOUR)
                        )
                        intent.putExtra(
                            DataUtils.NOTE_FONT_SIZE,
                            notes!!.getJSONObject(position).getInt(DataUtils.NOTE_FONT_SIZE)
                        )
                        if (notes!!.getJSONObject(position).has(DataUtils.NOTE_HIDE_BODY)) {
                            intent.putExtra(
                                DataUtils.NOTE_HIDE_BODY,
                                notes!!.getJSONObject(position).getBoolean(DataUtils.NOTE_HIDE_BODY)
                            )
                        } else intent.putExtra(DataUtils.NOTE_HIDE_BODY, false)
                    } catch (e: JSONException) {
                        CoreApplication.getInstance().logException(e)
                        e.printStackTrace()
                    }
                    intent.putExtra(DataUtils.NOTE_REQUEST_CODE, position)
                    startActivityForResult(intent, position)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onMenuItemClick(menuItem: MenuItem): Boolean {
        val id = menuItem.itemId

        // 'Backup notes' pressed -> show backupCheckDialog
        if (id == R.id.action_backup) {
            backupCheckDialog!!.show()
            return true
        }

        // 'Restore notes' pressed -> show restoreCheckDialog
        if (id == R.id.action_restore) {
            restoreCheckDialog!!.show()
            return true
        }
        if (id == R.id.action_evernote) {
            if (EvernoteSession.getInstance().isLoggedIn) {
                EvernoteManager().sync()
            } else {
                EvernoteSession.getInstance().authenticate(this)
            }
            return true
        }

        return false
    }

    /**
     * During multi-choice menu_delete selection mode, callback method if items checked changed
     *
     * @param mode     ActionMode of selection
     * @param position Position checked
     * @param id       ID of item, if exists
     * @param checked  true if checked, false otherwise
     */
    override fun onItemCheckedStateChanged(mode: ActionMode, position: Int, id: Long, checked: Boolean) {
        // If item checked -> add to array
        if (checked) checkedArray.add(position) else {
            var index = -1

            // Loop through array and find currentIndexDashboard of item unchecked
            for (i in checkedArray.indices) {
                if (position == checkedArray[i]) {
                    index = i
                    break
                }
            }

            // If currentIndexDashboard was found -> remove the item
            if (index != -1) checkedArray.removeAt(index)
        }

        // Set Toolbar title to 'x Selected'
        mode.title = checkedArray.size.toString() + " " + getString(R.string.label_selected)
        notesAdapter!!.notifyDataSetChanged()
    }

    /**
     * Callback method when 'Delete' icon pressed
     *
     * @param mode ActionMode of selection
     * @param item MenuItem clicked, in our case just action_delete
     * @return true if clicked, false otherwise
     */
    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        if (item.itemId == R.id.action_delete) {
            AlertDialog.Builder(this)
                .setMessage(R.string.title_notesDelete)
                .setPositiveButton(android.R.string.ok) { dialog, which -> // Pass notes and checked items for deletion array to 'deleteNotes'
                    notes = DataUtils.deleteNotes(notes, checkedArray)

                    // Create and set new adapter with new notes array
                    notesAdapter = NoteAdapter(this@NoteListActivity, notes)
                    binding?.listView?.adapter = notesAdapter

                    // Attempt to save notes to local file
                    val saveSuccessful = DataUtils.saveData(localPath, notes)

                    // If save successful -> toast successfully deleted
                    if (saveSuccessful) {
                        val toast = Toast.makeText(
                            applicationContext,
                            resources.getString(R.string.msg_deleteSuccessful),
                            Toast.LENGTH_SHORT
                        )
                        toast.show()
                    }

                    // Smooth scroll to top
                    binding?.listView?.post { binding?.listView?.smoothScrollToPosition(0) }

                    // If no notes -> show 'Press + to add new note' text, invisible otherwise
                    if (notes?.length() == 0) binding?.noNotes?.visibility = View.VISIBLE
                    else binding?.noNotes?.visibility = View.INVISIBLE
                    mode.finish()
                }
                .setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.dismiss() }
                .show()
            return true
        }
        return false
    }

    // Long click detected on ListView item -> start selection ActionMode (delete mode)
    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.menuInflater.inflate(R.menu.menu_delete, menu) // Inflate 'menu_delete' menu
        deleteActive = true // Set deleteActive to true as we entered delete mode
        newNoteButtonVisibility(false) // Hide newNote button
        notesAdapter!!.notifyDataSetChanged() // Notify adapter to hide favourite buttons
        return true
    }

    // Selection ActionMode finished (delete mode ended)
    override fun onDestroyActionMode(mode: ActionMode) {
        checkedArray = ArrayList() // Reset checkedArray
        deleteActive = false // Set deleteActive to false as we finished delete mode
        newNoteButtonVisibility(true) // Show newNote button
        notesAdapter!!.notifyDataSetChanged() // Notify adapter to show favourite buttons
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return false
    }

    protected fun newNoteButtonVisibility(isVisible: Boolean) {
        binding?.fab?.apply {
            if (isVisible) {
                animate().cancel()
                animate().translationY(newNoteButtonBaseYCoordinate)
            } else {
                animate().cancel()
                animate().translationY(newNoteButtonBaseYCoordinate + 500)
            }
        }
    }

    /**
     * Callback method for 'searchView' menu item widget text change
     *
     * @param s String which changed
     * @return true if text changed and logic finished, false otherwise
     */
    override fun onQueryTextChange(s: String): Boolean {
        var s = s
        s = s.toLowerCase() // Turn string into lowercase

        // If query text length longer than 0
        if (s.length > 0) {
            // Create new JSONArray and reset realIndexes array
            val notesFound = JSONArray()
            realIndexesOfSearchResults = ArrayList()

            // Loop through main notes list
            for (i in 0 until notes!!.length()) {
                var note: JSONObject? = null

                // Get note at position i
                try {
                    note = notes!!.getJSONObject(i)
                } catch (e: JSONException) {
                    CoreApplication.getInstance().logException(e)
                    e.printStackTrace()
                }

                // If note not null and title/body contain query text
                // -> Put in new notes array and add i to realIndexes array
                if (note != null) {
                    try {
                        if (note.getString(DataUtils.NOTE_TITLE).toLowerCase().contains(s) ||
                            note.getString(DataUtils.NOTE_BODY).toLowerCase().contains(s)
                        ) {
                            notesFound.put(note)
                            realIndexesOfSearchResults!!.add(i)
                        }
                    } catch (e: JSONException) {
                        CoreApplication.getInstance().logException(e)
                        e.printStackTrace()
                    }
                }
            }

            // Create and set adapter with notesFound to refresh ListView
            val searchAdapter = NoteAdapter(this@NoteListActivity, notesFound)
            binding?.listView?.adapter = searchAdapter
        } else {
            realIndexesOfSearchResults = ArrayList()
            for (i in 0 until notes!!.length()) realIndexesOfSearchResults!!.add(i)
            notesAdapter = NoteAdapter(this@NoteListActivity, notes)
            binding?.listView?.adapter = notesAdapter
        }
        return false
    }

    override fun onQueryTextSubmit(s: String): Boolean {
        return false
    }

    /**
     * When search mode is finished
     * Collapse searchView widget, searchActive to false, reset adapter, enable listView long clicks
     * and show newNote button
     */
    protected fun searchEnded() {
        searchActive = false
        notesAdapter = NoteAdapter(this@NoteListActivity, notes)
        binding?.listView?.adapter = notesAdapter
        binding?.listView?.isLongClickable = true
        newNoteButtonVisibility(true)
    }

    /**
     * Callback method when NotesEditActivity finished adding new note or editing existing note
     *
     * @param requestCode requestCode for intent sent, in our case either NEW_NOTE_REQUEST or position
     * @param resultCode  resultCode from activity, either RESULT_OK or RESULT_CANCELED
     * @param data        Data bundle passed back from NotesEditActivity
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            // If search was active -> call 'searchEnded' method
            if (searchActive && searchMenu != null) searchMenu!!.collapseActionView()

            // Get extras
            var mBundle: Bundle? = null
            if (data != null) mBundle = data.extras
            if (mBundle != null) {
                // If new note was saved
                if (requestCode == DataUtils.NEW_NOTE_REQUEST) {
                    var newNoteObject: JSONObject? = null
                    try {
                        // Add new note to array
                        newNoteObject = JSONObject()
                        newNoteObject.put(DataUtils.NOTE_TITLE, mBundle.getString(DataUtils.NOTE_TITLE))
                        newNoteObject.put(DataUtils.NOTE_BODY, mBundle.getString(DataUtils.NOTE_BODY))
                        newNoteObject.put(DataUtils.NOTE_COLOUR, mBundle.getString(DataUtils.NOTE_COLOUR))
                        newNoteObject.put(DataUtils.NOTE_FAVOURED, false)
                        newNoteObject.put(DataUtils.NOTE_FONT_SIZE, mBundle.getInt(DataUtils.NOTE_FONT_SIZE))
                        newNoteObject.put(DataUtils.NOTE_HIDE_BODY, mBundle.getBoolean(DataUtils.NOTE_HIDE_BODY))
                        notes!!.put(newNoteObject)
                    } catch (e: JSONException) {
                        CoreApplication.getInstance().logException(e)
                        e.printStackTrace()
                    }

                    // If newNoteObject not null -> save notes array to local file and notify adapter
                    if (newNoteObject != null) {
                        notesAdapter!!.notifyDataSetChanged()
                        val saveSuccessful = DataUtils.saveData(localPath, notes)
                        EvernoteManager().createNote(newNoteObject)
                        if (saveSuccessful) {
                            val toast = Toast.makeText(
                                applicationContext,
                                resources.getString(R.string.msg_noteCreated),
                                Toast.LENGTH_SHORT
                            )
                            toast.show()
                        }

                        // If no notes -> show 'Press + to add new note' text, invisible otherwise
                        if (notes!!.length() == 0) binding?.noNotes?.visibility = View.VISIBLE
                        else binding?.noNotes?.visibility = View.INVISIBLE
                    }
                } else {
                    var newNoteObject: JSONObject? = null
                    try {
                        // Update array item with new note data
                        newNoteObject = notes!!.getJSONObject(requestCode)
                        newNoteObject.put(DataUtils.NOTE_TITLE, mBundle.getString(DataUtils.NOTE_TITLE))
                        newNoteObject.put(DataUtils.NOTE_BODY, mBundle.getString(DataUtils.NOTE_BODY))
                        newNoteObject.put(DataUtils.NOTE_COLOUR, mBundle.getString(DataUtils.NOTE_COLOUR))
                        newNoteObject.put(DataUtils.NOTE_FONT_SIZE, mBundle.getInt(DataUtils.NOTE_FONT_SIZE))
                        newNoteObject.put(DataUtils.NOTE_HIDE_BODY, mBundle.getBoolean(DataUtils.NOTE_HIDE_BODY))

                        // Update note at position 'requestCode'
                        notes!!.put(requestCode, newNoteObject)
                    } catch (e: JSONException) {
                        CoreApplication.getInstance().logException(e)
                        e.printStackTrace()
                    }

                    // If newNoteObject not null -> save notes array to local file and notify adapter
                    if (newNoteObject != null) {
                        notesAdapter!!.notifyDataSetChanged()
                        val saveSuccessful = DataUtils.saveData(localPath, notes)
                        if (saveSuccessful) {
                            val toast = Toast.makeText(
                                applicationContext,
                                resources.getString(R.string.msg_noteSaved),
                                Toast.LENGTH_SHORT
                            )
                            toast.show()
                        }
                    }
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            val mBundle: Bundle?

            // If data is not null, has "request" extra and is new note -> get extras to bundle
            if (data != null && data.hasExtra("request") && requestCode == DataUtils.NEW_NOTE_REQUEST) {
                mBundle = data.extras

                // If new note discarded -> toast empty note discarded
                if (mBundle != null && mBundle.getString("request") != null && mBundle.getString("request") == "discard") {
                    val toast = Toast.makeText(
                        applicationContext,
                        resources.getString(R.string.msg_emptyNoteDiscarded),
                        Toast.LENGTH_SHORT
                    )
                    toast.show()
                }
                if (mBundle != null && mBundle.getString("request") != null && mBundle.getString("request") == "HOME") {
                    val tempNotes = DataUtils.retrieveData(localPath)
                    Tracer.i("All notes: $tempNotes")

                    // If not null -> equal main notes to retrieved notes
                    if (tempNotes != null) {
                        notes = tempNotes
                        notesAdapter!!.setAdapterData(notes)
                        notesAdapter!!.notifyDataSetChanged()
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * If back button pressed while search is active -> collapse view and end search mode
     */
    override fun onBackPressed() {
        if (searchActive && searchMenu != null) {
            searchMenu!!.collapseActionView()
            return
        }
        super.onBackPressed()
    }

    /**
     * Orientation changed callback method
     * If orientation changed -> If any AlertDialog is showing, dismiss it to prevent WindowLeaks
     *
     * @param newConfig New Configuration passed by system
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        if (backupCheckDialog != null && backupCheckDialog!!.isShowing) backupCheckDialog!!.dismiss()
        if (backupOKDialog != null && backupOKDialog!!.isShowing) backupOKDialog!!.dismiss()
        if (restoreCheckDialog != null && restoreCheckDialog!!.isShowing) restoreCheckDialog!!.dismiss()
        if (restoreFailedDialog != null && restoreFailedDialog!!.isShowing) restoreFailedDialog!!.dismiss()
        super.onConfigurationChanged(newConfig)
    }

    override fun onLoginFinished(successful: Boolean) {
        if (successful) {
            EvernoteManager().createSiempoNotebook()
            EvernoteManager().listNoteBooks("")
            //new EvernoteManager().listNoteBooks(this);
        }
    }

    override fun onPause() {
        super.onPause()
        FirebaseHelper.getInstance().logScreenUsageTime(this@NoteListActivity.javaClass.simpleName, startTime)
    }

    override fun onStop() {
        super.onStop()
        searchEnded()
    }

    companion object {
        const val EXTRA_OPEN_LATEST = "open_latest"

        @JvmField
        var checkedArray = ArrayList<Int>()

        @JvmField
        var deleteActive = false // True if delete mode is active, false otherwise

        @JvmField
        var searchActive = false
        private var notes // Main notes array
                : JSONArray? = null
    }
}