package io.focuslauncher.phone.customviews

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import androidx.appcompat.widget.AppCompatEditText

/**
 * Created by rajeshjadi on 11/1/18.
 */
class LockEditText /* Must use this constructor in order for the layout files to instantiate the class properly */
(context: Context?, attrs: AttributeSet?) : AppCompatEditText(context!!, attrs) {
    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK &&
            event.action == KeyEvent.ACTION_UP
        ) {
            Log.e("onKeyPreIme ", "" + event)
            true
        } else {
            false
        }
        // Log.e("onKeyPreIme ",""+event);
    }
}
