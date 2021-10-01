package io.focuslauncher.phone.colorpicker

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import io.focuslauncher.R

/**
 * Creates a circular swatch of a specified color.  Adds a checkmark if marked as checked
 */
@SuppressLint("ViewConstructor")
class ColorPickerSwatch(
    context: Context,
    private val mColor: Int,
    checked: Boolean,
    private val mOnColorSelectedListener: OnColorSelectedListener?
) : FrameLayout(context), View.OnClickListener {
    private val mSwatchImage: ImageView
    private val mCheckmarkImage: ImageView
    protected fun setColor(color: Int) {
        val colorDrawable = arrayOf(context.resources.getDrawable(R.drawable.color_picker_swatch))
        mSwatchImage.setImageDrawable(ColorStateDrawable(colorDrawable, color))
    }

    private fun setChecked(checked: Boolean) {
        if (checked) {
            mCheckmarkImage.visibility = VISIBLE
        } else {
            mCheckmarkImage.visibility = GONE
        }
    }

    override fun onClick(v: View) {
        mOnColorSelectedListener?.onColorSelected(mColor)
    }

    /**
     * Interface for a callback when a color square is selected
     */
    interface OnColorSelectedListener {
        // Called when a specific color square has been selected
        fun onColorSelected(color: Int)
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.color_picker_swatch, this)
        mSwatchImage = findViewById(R.id.color_picker_swatch)
        mCheckmarkImage = findViewById(R.id.color_picker_checkmark)
        setColor(mColor)
        setChecked(checked)
        setOnClickListener(this)
    }
}
