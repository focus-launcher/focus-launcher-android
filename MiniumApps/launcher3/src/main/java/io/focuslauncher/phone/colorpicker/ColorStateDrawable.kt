package io.focuslauncher.phone.colorpicker

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable

/**
 * A drawable which sets its color filter to a color specified by the user, and changes to a
 * slightly darker color when pressed or focused
 */
class ColorStateDrawable(layers: Array<Drawable>, private val mColor: Int) : LayerDrawable(layers) {
    override fun onStateChange(states: IntArray): Boolean {
        var pressedOrFocused = false
        for (state in states) {
            if (state == android.R.attr.state_pressed || state == android.R.attr.state_focused) {
                pressedOrFocused = true
                break
            }
        }
        if (pressedOrFocused) {
            super.setColorFilter(getPressedColor(mColor), PorterDuff.Mode.SRC_ATOP)
        } else {
            super.setColorFilter(mColor, PorterDuff.Mode.SRC_ATOP)
        }
        return super.onStateChange(states)
    }

    override fun isStateful(): Boolean {
        return true
    }

    companion object {
        private const val PRESSED_STATE_MULTIPLIER = 0.70f

        /**
         * Given a particular color, adjusts its value by a multiplier
         */
        private fun getPressedColor(color: Int): Int {
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)
            hsv[2] = hsv[2] * PRESSED_STATE_MULTIPLIER
            return Color.HSVToColor(hsv)
        }
    }
}