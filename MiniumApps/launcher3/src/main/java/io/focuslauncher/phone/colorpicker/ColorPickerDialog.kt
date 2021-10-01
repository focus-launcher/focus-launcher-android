package io.focuslauncher.phone.colorpicker

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.app.FragmentManager
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import io.focuslauncher.R
import io.focuslauncher.phone.colorpicker.ColorPickerSwatch.OnColorSelectedListener

/**
 * A dialog which takes in as input an array of colors and creates a palette allowing the user to
 * select a specific color swatch, which invokes a listener
 */
class ColorPickerDialog : DialogFragment(), OnColorSelectedListener {
    protected var mAlertDialog: AlertDialog? = null
    protected var mTitleResId = R.string.label_color
    var colors: IntArray? = null
        protected set
    protected var mColorContentDescriptions: Array<String>? = null
    protected var mSelectedColor = 0
    protected var mColumns = 0
    protected var mSize = 0
    var isDialogShowing = false
        protected set
    protected var mListener: OnColorSelectedListener? = null
    private var mPalette: ColorPickerPalette? = null
    private var mProgress: ProgressBar? = null
    fun initialize(titleResId: Int, colors: IntArray, selectedColor: Int, columns: Int, size: Int) {
        setArguments(titleResId, columns, size)
        setColors(colors, selectedColor)
    }

    fun setArguments(titleResId: Int, columns: Int, size: Int) {
        val bundle = Bundle()
        bundle.putInt(KEY_TITLE_ID, titleResId)
        bundle.putInt(KEY_COLUMNS, columns)
        bundle.putInt(KEY_SIZE, size)
        arguments = bundle
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray(KEY_COLORS, colors)
        outState.putSerializable(KEY_SELECTED_COLOR, mSelectedColor)
        outState.putStringArray(KEY_COLOR_CONTENT_DESCRIPTIONS, mColorContentDescriptions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mTitleResId = arguments.getInt(KEY_TITLE_ID)
            mColumns = arguments.getInt(KEY_COLUMNS)
            mSize = arguments.getInt(KEY_SIZE)
        }
        if (savedInstanceState != null) {
            colors = savedInstanceState.getIntArray(KEY_COLORS)
            val c = (savedInstanceState.getSerializable(KEY_SELECTED_COLOR) as Int?)
            if (c != null) mSelectedColor = c
            mColorContentDescriptions = savedInstanceState.getStringArray(
                KEY_COLOR_CONTENT_DESCRIPTIONS
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity
        val view = LayoutInflater.from(getActivity()).inflate(R.layout.color_picker_dialog, null)
        mProgress = view.findViewById(android.R.id.progress)
        mPalette = view.findViewById(R.id.color_picker)
        mPalette?.init(mSize, mColumns, this)
        if (colors != null) showPaletteView()
        mAlertDialog = AlertDialog.Builder(activity)
            .setTitle(mTitleResId)
            .setView(view)
            .create()
        return mAlertDialog as AlertDialog
    }

    fun setOnColorSelectedListener(listener: OnColorSelectedListener?) {
        mListener = listener
    }

    override fun onColorSelected(color: Int) {
        if (mListener != null) mListener?.onColorSelected(color)
        if (targetFragment is OnColorSelectedListener) {
            val listener = targetFragment as OnColorSelectedListener
            listener.onColorSelected(color)
        }
        if (color != mSelectedColor) {
            mSelectedColor = color
            // Redraw palette to show checkmark on newly selected color before dismissing
            mPalette?.drawPalette(colors, mSelectedColor)
        }
        dismiss()
    }

    fun showPaletteView() {
        if (mProgress != null && mPalette != null) {
            mProgress?.visibility = View.GONE
            refreshPalette()
            mPalette?.visibility = View.VISIBLE
        }
    }

    fun showProgressBarView() {
        if (mProgress != null && mPalette != null) {
            mProgress?.visibility = View.VISIBLE
            mPalette?.visibility = View.GONE
        }
    }

    fun setColors(colors: IntArray, selectedColor: Int) {
        if (this.colors != colors || mSelectedColor != selectedColor) {
            this.colors = colors
            mSelectedColor = selectedColor
            refreshPalette()
        }
    }

    fun setColorContentDescriptions(colorContentDescriptions: Array<String>) {
        if (mColorContentDescriptions != colorContentDescriptions) {
            mColorContentDescriptions = colorContentDescriptions
            refreshPalette()
        }
    }

    private fun refreshPalette() {
        if (mPalette != null && colors != null) {
            mPalette?.drawPalette(colors, mSelectedColor, mColorContentDescriptions)
        }
    }

    @JvmName("setColors1")
    fun setColors(colors: IntArray) {
        if (this.colors != colors) {
            this.colors = colors
            refreshPalette()
        }
    }

    var selectedColor: Int
        get() = mSelectedColor
        set(color) {
            if (mSelectedColor != color) {
                mSelectedColor = color
                refreshPalette()
            }
        }

    override fun show(manager: FragmentManager, tag: String) {
        if (isDialogShowing) return
        super.show(manager, tag)
        isDialogShowing = true
    }

    override fun onDismiss(dialog: DialogInterface) {
        isDialogShowing = false
        super.onDismiss(dialog)
    }

    companion object {
        const val SIZE_LARGE = 1
        const val SIZE_SMALL = 2
        protected const val KEY_TITLE_ID = "title_id"
        protected const val KEY_COLORS = "colors"
        protected const val KEY_COLOR_CONTENT_DESCRIPTIONS = "color_content_descriptions"
        protected const val KEY_SELECTED_COLOR = "selected_color"
        protected const val KEY_COLUMNS = "columns"
        protected const val KEY_SIZE = "size"
        @JvmStatic
        fun newInstance(
            titleResId: Int,
            colors: IntArray,
            selectedColor: Int,
            columns: Int,
            size: Int
        ): ColorPickerDialog {
            val ret = ColorPickerDialog()
            ret.initialize(titleResId, colors, selectedColor, columns, size)
            return ret
        }
    }
}
