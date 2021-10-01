package io.focuslauncher.phone.customviews

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class RobotoRegularTextView : AppCompatTextView {
    constructor(context: Context?) : super(context!!) {
        init(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context!!, attrs, defStyleAttr) {}

    private fun init(attrs: AttributeSet?) {
        if (attrs != null) {
            try {
                val myTypeface = Typeface.createFromAsset(context.assets, "fonts/robotoregular.ttf")
                typeface = myTypeface
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}