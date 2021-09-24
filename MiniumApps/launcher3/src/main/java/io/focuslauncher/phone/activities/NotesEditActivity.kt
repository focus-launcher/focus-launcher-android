package io.focuslauncher.phone.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.MenuItem
import android.view.View.OnTouchListener
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import de.greenrobot.event.Subscribe
import io.focuslauncher.R
import io.focuslauncher.databinding.ActivityEditBinding
import io.focuslauncher.phone.app.CoreApplication
import io.focuslauncher.phone.colorpicker.ColorPickerDialog
import io.focuslauncher.phone.event.HomePress
import io.focuslauncher.phone.helper.FirebaseHelper
import io.focuslauncher.phone.log.Tracer
import io.focuslauncher.phone.managers.EvernoteManager
import io.focuslauncher.phone.utils.DataUtils
import io.focuslauncher.phone.utils.UIUtils
import io.focuslauncher.phone.utils.bindView
import io.focuslauncher.phone.utils.lifecycleProperty
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class NotesEditActivity : CoreActivity(), Toolbar.OnMenuItemClickListener {
    // Layout components
    private var menuHideBody: MenuItem? = null

    private val colourArr: Array<String> by lazy { resources.getStringArray(R.array.colours) }
    private val colourArrResId: IntArray by lazy { colourArr.map { Color.parseColor(it) }.toIntArray() }
    private val fontSizeArr: IntArray = intArrayOf(14, 18, 22)
    private val fontSizeNameArr: Array<String> by lazy { resources.getStringArray(R.array.fontSizeNames) }

    private var fontDialog: AlertDialog? = null
    private var saveChangesDialog: AlertDialog? = null
    private var colorPickerDialog: ColorPickerDialog? = null

    private var imm: InputMethodManager? = null
    private var bundle: Bundle? = null
    private var colour: String? = "#FFFFFF" // white default
    private var fontSize = 18 // Medium default
    private var hideBody = false
    private var startTime: Long = 0

    private var binding: ActivityEditBinding? by lifecycleProperty()

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = bindView(ActivityEditBinding::inflate)

        val scrollView = findViewById<ScrollView>(R.id.scrollView)
        imm = this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        initToolbar()

        scrollView.setOnTouchListener(OnTouchListener { v, event ->
            if (binding?.bodyEdit?.isFocused == false) {
                binding?.bodyEdit?.requestFocus()
                binding?.bodyEdit?.setSelection(binding?.bodyEdit?.text?.length ?: 0)
                // Force show keyboard
                imm!!.toggleSoftInput(
                    InputMethodManager.SHOW_FORCED,
                    InputMethodManager.HIDE_IMPLICIT_ONLY
                )
                return@OnTouchListener true
            }
            false
        })

        bundle = intent.extras
        if (bundle != null) {
            Tracer.i("Notes Edit" + bundle!!.getInt(DataUtils.NOTE_REQUEST_CODE))
            if (bundle!!.getInt(DataUtils.NOTE_REQUEST_CODE) != DataUtils.NEW_NOTE_REQUEST) {
                colour = bundle!!.getString(DataUtils.NOTE_COLOUR)
                if (TextUtils.isEmpty(colour)) {
                    colour = "#FFFFFF"
                }
                fontSize = bundle!!.getInt(DataUtils.NOTE_FONT_SIZE)
                hideBody = bundle!!.getBoolean(DataUtils.NOTE_HIDE_BODY)
                binding?.titleEdit?.setText(bundle!!.getString(DataUtils.NOTE_TITLE))
                binding?.bodyEdit?.setText(bundle!!.getString(DataUtils.NOTE_BODY))
                binding?.bodyEdit?.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
                if (hideBody) menuHideBody!!.setTitle(R.string.label_showNoteBody)
            } else if (bundle!!.getInt(DataUtils.NOTE_REQUEST_CODE) == DataUtils.NEW_NOTE_REQUEST) {
                binding?.titleEdit?.requestFocus()
                imm!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
            }


            // Set background colour to note colour
            if (!TextUtils.isEmpty(colour)) {
                binding?.relativeLayoutEdit?.setBackgroundColor(Color.parseColor(colour))
            }
        }
        initDialogs(this)
    }

    protected fun initToolbar() = binding?.toolbarEdit?.toolbar?.apply {
        title = ""
        // Set a 'Back' navigation icon in the Toolbar and handle the click
        setNavigationIcon(R.drawable.abc_ic_ab_back_material)
        setNavigationOnClickListener { onBackPressed() }

        // Inflate menu_edit to be displayed in the toolbar
        inflateMenu(R.menu.menu_edit)

        // Set an OnMenuItemClickListener to handle menu item clicks
        setOnMenuItemClickListener(this@NotesEditActivity)
        menu?.findItem(R.id.action_hide_show_body)
    }

    protected fun initDialogs(context: Context?) {
        // Colour picker dialog
        colorPickerDialog = ColorPickerDialog.newInstance(
            R.string.title_noteColor,
            colourArrResId, Color.parseColor(colour), 3,
            if (isTablet(this)) ColorPickerDialog.SIZE_LARGE else ColorPickerDialog.SIZE_SMALL
        )

        // Colour picker listener in colour picker dialog
        colorPickerDialog?.setOnColorSelectedListener { color -> // Format selected colour to string
            val selectedColourAsString = String.format("#%06X", 0xFFFFFF and color)

            // Check which colour is it and equal to main colour
            for (aColour in colourArr) if (aColour == selectedColourAsString) colour = aColour

            // Re-set background colour
            binding?.relativeLayoutEdit!!.setBackgroundColor(Color.parseColor(colour))
        }


        // Font size picker dialog
        fontDialog = AlertDialog.Builder(context)
            .setTitle(R.string.title_fontSize)
            .setItems(fontSizeNameArr) { dialog, which -> // Font size updated with new pick
                fontSize = fontSizeArr[which]
                binding?.bodyEdit?.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
            }
            .setNeutralButton(android.R.string.cancel) { dialog, which -> dialog.dismiss() }
            .create()


        // 'Save changes?' dialog
        saveChangesDialog = AlertDialog.Builder(context)
            .setMessage(R.string.title_saveChanges)
            .setPositiveButton(R.string.label_yes) { dialog, which -> // If 'Yes' clicked -> check if title is empty
                // If title not empty -> save and go back; Otherwise toast
                if (!isEmpty(binding?.titleEdit)) saveChanges() else toastEditTextCannotBeEmpty()
            }
            .setNegativeButton(R.string.label_no) { dialog, which -> // If 'No' clicked in new note -> put extra 'discard' to show toast
                if (bundle != null && bundle!!.getInt(DataUtils.NOTE_REQUEST_CODE) ==
                    DataUtils.NEW_NOTE_REQUEST
                ) {
                    val intent = Intent()
                    intent.putExtra("request", "discard")
                    setResult(RESULT_CANCELED, intent)
                    imm!!.hideSoftInputFromWindow(binding?.titleEdit?.windowToken, 0)
                    dialog.dismiss()
                    finish()
                    overridePendingTransition(0, 0)
                }
            }
            .create()
    }

    /**
     * Item clicked in Toolbar menu callback method
     *
     * @param item Item clicked
     * @return true if click detected and logic finished, false otherwise
     */
    override fun onMenuItemClick(item: MenuItem): Boolean {
        val id = item.itemId

        // Note colour menu item clicked -> show colour picker dialog
        if (id == R.id.action_note_colour) {
            colorPickerDialog!!.show(fragmentManager, "colourPicker")
            return true
        }

        // Font size menu item clicked -> show font picker dialog
        if (id == R.id.action_font_size) {
            fontDialog!!.show()
            return true
        }

        // If 'Hide note body in list' or 'Show note body in list' clicked
        if (id == R.id.action_hide_show_body) {
            // If hideBody false -> set to true and change menu item text to 'Show note body in list'
            if (!hideBody) {
                hideBody = true
                menuHideBody!!.setTitle(R.string.label_showNoteBody)

                // Toast note body will be hidden
                val toast = Toast.makeText(
                    applicationContext,
                    resources.getString(R.string.msg_noteWillBeHidden),
                    Toast.LENGTH_SHORT
                )
                toast.show()
            } else {
                hideBody = false
                menuHideBody!!.setTitle(R.string.label_hideNoteBody)

                // Toast note body will be shown
                val toast = Toast.makeText(
                    applicationContext,
                    resources.getString(R.string.msg_noteWillBeShown),
                    Toast.LENGTH_SHORT
                )
                toast.show()
            }
            return true
        }
        return false
    }

    /**
     * Create an Intent with title, body, colour, font size and hideBody extras
     * Set RESULT_OK and go back to DashboardActivity
     */
    protected fun saveChanges() {
        val intent = Intent()

        // Package everything and send back to activity with OK
        intent.putExtra(DataUtils.NOTE_TITLE, binding?.titleEdit?.text?.toString() ?: "")
        intent.putExtra(DataUtils.NOTE_BODY, binding?.bodyEdit?.text?.toString() ?: "")
        intent.putExtra(DataUtils.NOTE_COLOUR, colour)
        intent.putExtra(DataUtils.NOTE_FONT_SIZE, fontSize)
        intent.putExtra(DataUtils.NOTE_HIDE_BODY, hideBody)
        setResult(RESULT_OK, intent)
        imm!!.hideSoftInputFromWindow(binding?.titleEdit?.windowToken, 0)
        finish()
        overridePendingTransition(0, 0)
    }

    /**
     * Back or navigation '<-' pressed
     */
    override fun onBackPressed() {
        // New note -> show 'Save changes?' dialog
        try {
            if (bundle != null && bundle!!.getInt(DataUtils.NOTE_REQUEST_CODE) == DataUtils.NEW_NOTE_REQUEST) saveChangesDialog!!.show() else {
                /*
             * If title is not empty -> Check if note changed
             *  If yes -> saveChanges
             *  If not -> hide keyboard if showing and finish
             */
                if (!isEmpty(binding?.titleEdit)) {
                    if (bundle != null && bundle!!.containsKey(DataUtils.NOTE_TITLE) && binding?.titleEdit?.text.toString() != bundle!!.getString(
                            DataUtils.NOTE_TITLE
                        ) ||
                        bundle!!.containsKey(DataUtils.NOTE_BODY) && binding?.bodyEdit?.text.toString() != bundle!!.getString(
                            DataUtils.NOTE_BODY
                        ) ||
                        bundle!!.containsKey(DataUtils.NOTE_BODY) && colour != bundle!!.getString(DataUtils.NOTE_COLOUR) || fontSize != bundle!!.getInt(
                            DataUtils.NOTE_FONT_SIZE
                        ) || hideBody != bundle!!.getBoolean(DataUtils.NOTE_HIDE_BODY)
                    ) {
                        saveChanges()
                    } else {
                        imm!!.hideSoftInputFromWindow(binding?.titleEdit?.windowToken, 0)
                        finish()
                        overridePendingTransition(0, 0)
                    }
                } else toastEditTextCannotBeEmpty()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Check if passed EditText text is empty or not
     *
     * @param editText The EditText widget to check
     * @return true if empty, false otherwise
     */
    protected fun isEmpty(editText: EditText?): Boolean {
        return editText!!.text.toString().trim { it <= ' ' }.length == 0
    }

    /**
     * Show Toast for 'Title cannot be empty'
     */
    protected fun toastEditTextCannotBeEmpty() {
        val toast = Toast.makeText(
            applicationContext,
            resources.getString(R.string.msg_titleCannotBeEmpty),
            Toast.LENGTH_LONG
        )
        toast.show()
    }

    /**
     * If current window loses focus -> hide keyboard
     *
     * @param hasFocus parameter passed by system; true if focus changed, false otherwise
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) if (imm != null && binding?.titleEdit != null) imm!!.hideSoftInputFromWindow(
            binding?.titleEdit?.windowToken,
            0
        )
    }

    /**
     * Orientation changed callback method
     * If orientation changed -> If any AlertDialog is showing -> dismiss it to prevent WindowLeaks
     *
     * @param newConfig Configuration passed by system
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        if (colorPickerDialog != null && colorPickerDialog!!.isDialogShowing) colorPickerDialog!!.dismiss()
        if (fontDialog != null && fontDialog!!.isShowing) fontDialog!!.dismiss()
        if (saveChangesDialog != null && saveChangesDialog!!.isShowing) saveChangesDialog!!.dismiss()
        super.onConfigurationChanged(newConfig)
    }

    @Subscribe
    fun homePress(event: HomePress?) {
        try {
            if (UIUtils.isMyLauncherDefault(this)) {
                // onBackPressed();
                if (!isEmpty(binding?.titleEdit)) {
                    saveChanges1()
                    val intent = Intent()
                    intent.putExtra("request", "HOME")
                    setResult(RESULT_CANCELED, intent)
                    imm!!.hideSoftInputFromWindow(binding?.titleEdit?.windowToken, 0)
                    finish()
                    overridePendingTransition(0, 0)
                } else {
                    toastEditTextCannotBeEmpty()
                }
            } else {
                if (!isEmpty(binding?.titleEdit)) {
                    saveChanges1()
                    val intent = Intent()
                    intent.putExtra("request", "")
                    setResult(RESULT_CANCELED, intent)
                    imm!!.hideSoftInputFromWindow(binding?.titleEdit?.windowToken, 0)
                    finish()
                    overridePendingTransition(0, 0)
                }
            }
        } catch (e: Exception) {
            CoreApplication.getInstance().logException(e)
            Tracer.e(e, e.message)
        }
    }

    private fun saveChanges1() {
        if (bundle != null) {
            Tracer.i("Notes Edit  1" + bundle!!.getInt(DataUtils.NOTE_REQUEST_CODE))
            var notes = JSONArray()
            val tempNotes = DataUtils.retrieveData(localPath)
            // If not null -> equal main notes to retrieved notes
            if (tempNotes != null) notes = tempNotes
            // If current note is not new -> initialize colour, font, hideBody and EditTexts
            if (bundle!!.getInt(DataUtils.NOTE_REQUEST_CODE) != DataUtils.NEW_NOTE_REQUEST) {
                run {
                    var newNoteObject: JSONObject? = null
                    try {

                        // Update array item with new note data
                        newNoteObject = notes.getJSONObject(bundle!!.getInt(DataUtils.NOTE_REQUEST_CODE))
                        newNoteObject?.put(DataUtils.NOTE_TITLE, binding?.titleEdit?.text?.toString() ?: "")
                        newNoteObject?.put(DataUtils.NOTE_BODY, binding?.bodyEdit?.text.toString())
                        newNoteObject?.put(DataUtils.NOTE_COLOUR, colour)
                        newNoteObject?.put(DataUtils.NOTE_FONT_SIZE, fontSize)
                        newNoteObject?.put(DataUtils.NOTE_HIDE_BODY, hideBody)

                        // Update note at position 'requestCode'
                        notes.put(bundle!!.getInt(DataUtils.NOTE_REQUEST_CODE), newNoteObject)
                    } catch (e: JSONException) {
                        CoreApplication.getInstance().logException(e)
                        e.printStackTrace()
                    }

                    // If newNoteObject not null -> save notes array to local file and notify adapter
                    if (newNoteObject != null) {
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
            } else if (bundle!!.getInt(DataUtils.NOTE_REQUEST_CODE) == DataUtils.NEW_NOTE_REQUEST) {
                var newNoteObject: JSONObject? = null
                try {
                    // Add new note to array
                    newNoteObject = JSONObject()
                    newNoteObject!!.put(DataUtils.NOTE_TITLE, binding?.titleEdit?.text?.toString() ?: "")
                    newNoteObject!!.put(DataUtils.NOTE_BODY, binding?.bodyEdit?.text.toString())
                    newNoteObject!!.put(DataUtils.NOTE_COLOUR, colour)
                    newNoteObject!!.put(DataUtils.NOTE_FAVOURED, false)
                    newNoteObject!!.put(DataUtils.NOTE_FONT_SIZE, fontSize)
                    newNoteObject!!.put(DataUtils.NOTE_HIDE_BODY, hideBody)
                    notes.put(newNoteObject)
                } catch (e: JSONException) {
                    CoreApplication.getInstance().logException(e)
                    e.printStackTrace()
                }

                // If newNoteObject not null -> save notes array to local file and notify adapter
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
            }
        }
    }

    override fun onPause() {
        super.onPause()
        FirebaseHelper.getInstance().logScreenUsageTime(this@NotesEditActivity.javaClass.simpleName, startTime)
    }

    override fun onResume() {
        super.onResume()
        startTime = System.currentTimeMillis()
    }

    companion object {
        /**
         * Check if current device has tablet screen size or not
         *
         * @param context current application context
         * @return true if device is tablet, false otherwise
         */
        fun isTablet(context: Context): Boolean {
            return context.resources.configuration.screenLayout and
                    Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
        }
    }
}