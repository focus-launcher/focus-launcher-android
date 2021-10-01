package io.focuslauncher.phone.colorpicker

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.*
import io.focuslauncher.R
import io.focuslauncher.phone.colorpicker.ColorPickerSwatch.OnColorSelectedListener

/**
 * A color picker custom view which creates an grid of color squares.  The number of squares per
 * row (and the padding between the squares) is determined by the user
 */
class ColorPickerPalette : TableLayout {
    var mOnColorSelectedListener: OnColorSelectedListener? = null
    private var mSwatchLength = 0
    private var mMarginSize = 0
    private var mNumColumns = 0

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?) : super(context) {}

    /**
     * Initialize the size, columns, and listener.  Size should be a pre-defined size (SIZE_LARGE
     * or SIZE_SMALL) from ColorPickerDialogFragment
     */
    fun init(size: Int, columns: Int, listener: OnColorSelectedListener?) {
        mNumColumns = columns
        val res = resources
        if (size == ColorPickerDialog.Companion.SIZE_LARGE) {
            mSwatchLength = res.getDimensionPixelSize(R.dimen.color_swatch_large)
            mMarginSize = res.getDimensionPixelSize(R.dimen.color_swatch_margins_large)
        } else {
            mSwatchLength = res.getDimensionPixelSize(R.dimen.color_swatch_small)
            mMarginSize = res.getDimensionPixelSize(R.dimen.color_swatch_margins_small)
        }
        mOnColorSelectedListener = listener
    }

    private fun createTableRow(): TableRow {
        val row = TableRow(context)
        val params = ViewGroup.LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        row.layoutParams = params
        return row
    }
    /**
     * Adds swatches to table in a serpentine format
     */
    /**
     * Adds swatches to table in a serpentine format
     */
    @JvmOverloads
    fun drawPalette(colors: IntArray?, selectedColor: Int, colorContentDescriptions: Array<String>? = null) {
        if (colors == null) return
        removeAllViews()
        var tableElements = 0
        var rowElements = 0
        var rowNumber = 0

        // Fills the table with swatches based on the array of colors
        var row = createTableRow()
        for (color in colors) {
            val colorSwatch: View = createColorSwatch(color, selectedColor)
            addSwatchToRow(row, colorSwatch, rowNumber)
            tableElements++
            rowElements++
            if (rowElements == mNumColumns) {
                addView(row)
                row = createTableRow()
                rowElements = 0
                rowNumber++
            }
        }

        // Create blank views to fill the row if the last row has not been filled
        if (rowElements > 0) {
            while (rowElements != mNumColumns) {
                addSwatchToRow(row, createBlankSpace(), rowNumber)
                rowElements++
            }
            addView(row)
        }
    }

    /**
     * Creates a blank space to fill the row
     */
    private fun createBlankSpace(): ImageView {
        val view = ImageView(context)
        val params = TableRow.LayoutParams(mSwatchLength, mSwatchLength)
        params.setMargins(mMarginSize, mMarginSize, mMarginSize, mMarginSize)
        view.layoutParams = params
        return view
    }

    /**
     * Creates a color swatch
     */
    private fun createColorSwatch(color: Int, selectedColor: Int): ColorPickerSwatch {
        val view = ColorPickerSwatch(
            context, color,
            color == selectedColor, mOnColorSelectedListener
        )
        val params = TableRow.LayoutParams(mSwatchLength, mSwatchLength)
        params.setMargins(mMarginSize, mMarginSize, mMarginSize, mMarginSize)
        view.layoutParams = params
        return view
    }

    companion object {
        /**
         * Appends a swatch to the end of the row for even-numbered rows (starting with row 0),
         * to the beginning of a row for odd-numbered rows
         */
        private fun addSwatchToRow(row: TableRow, swatch: View, rowNumber: Int) {
            if (rowNumber % 2 == 0) {
                row.addView(swatch)
            } else {
                row.addView(swatch, 0)
            }
        }
    }
}
