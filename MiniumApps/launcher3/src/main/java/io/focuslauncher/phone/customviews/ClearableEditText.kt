package io.focuslauncher.phone.customviews

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import io.focuslauncher.R

class ClearableEditText @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = android.R.attr.editTextStyle) : AppCompatEditText(context!!, attrs, defStyleAttr), TextWatcher {
    private var mClearIconDrawable: Drawable? = null
    private var mIsClearIconShown = false
    private var mClearIconDrawWhenFocused = true
    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(attrs, R.styleable.ClearableEditText, defStyle, 0)
        if (a.hasValue(R.styleable.ClearableEditText_clearIconDrawable)) {
            mClearIconDrawable = a.getDrawable(R.styleable.ClearableEditText_clearIconDrawable)
            if (mClearIconDrawable != null) {
                mClearIconDrawable!!.callback = this
            }
        }
        mClearIconDrawWhenFocused = a
            .getBoolean(R.styleable.ClearableEditText_clearIconDrawWhenFocused, true)
        a.recycle()
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        // no operation
    }

    override fun afterTextChanged(s: Editable) {}
    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        return if (mIsClearIconShown) ClearIconSavedState(superState, true) else superState
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is ClearIconSavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        val savedState = state
        super.onRestoreInstanceState(savedState.superState)
        mIsClearIconShown = savedState.isClearIconShown
        showClearIcon(mIsClearIconShown)
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (hasFocus()) {
//            showClearIcon(!TextUtils.isEmpty(s));
            showClearIcon(true)
        }
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
//        showClearIcon(
//                (!mClearIconDrawWhenFocused || focused) && !TextUtils.isEmpty(getText().toString()));
        showClearIcon(
            !mClearIconDrawWhenFocused || focused
        )
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isClearIconTouched(event)) {
            text = null
            event.action = MotionEvent.ACTION_CANCEL
            showClearIcon(false)
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm?.hideSoftInputFromWindow(windowToken, 0)
            clearFocus()
            return false
        }
        return super.onTouchEvent(event)
    }

    private fun isClearIconTouched(event: MotionEvent): Boolean {
        if (!mIsClearIconShown) {
            return false
        }
        val touchPointX = event.x.toInt()
        val widthOfView = width
        val compoundPaddingRight = compoundPaddingRight
        return touchPointX >= widthOfView - compoundPaddingRight
    }

    private fun showClearIcon(show: Boolean) {
        if (show) {
            // show icon on the right
            if (mClearIconDrawable != null) {
                setCompoundDrawablesWithIntrinsicBounds(null, null, mClearIconDrawable, null)
            } else {
                setCompoundDrawablesWithIntrinsicBounds(0, 0, DEFAULT_CLEAR_ICON_RES_ID, 0)
            }
        } else {
            // remove icon
            setCompoundDrawables(null, null, null, null)
        }
        mIsClearIconShown = show
    }

    protected class ClearIconSavedState : BaseSavedState {
        val isClearIconShown: Boolean

        private constructor(source: Parcel) : super(source) {
            isClearIconShown = source.readByte().toInt() != 0
        }

        internal constructor(superState: Parcelable?, isClearIconShown: Boolean) : super(superState) {
            this.isClearIconShown = isClearIconShown
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeByte((if (isClearIconShown) 1 else 0).toByte())
        }

        companion object {
            @JvmField val CREATOR: Parcelable.Creator<ClearIconSavedState?> = object : Parcelable.Creator<ClearIconSavedState?> {
                override fun createFromParcel(source: Parcel): ClearIconSavedState? {
                    return ClearIconSavedState(source)
                }

                override fun newArray(size: Int): Array<ClearIconSavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    companion object {
        private const val DEFAULT_CLEAR_ICON_RES_ID = R.drawable.ic_close_blue
    }

    init {
        init(attrs, defStyleAttr)
    }
}
