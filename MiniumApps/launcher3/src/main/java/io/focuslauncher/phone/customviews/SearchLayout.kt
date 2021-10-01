package io.focuslauncher.phone.customviews

import android.content.Context
import android.content.SharedPreferences
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import androidx.cardview.widget.CardView
import com.eyeem.chips.BubbleStyle
import com.eyeem.chips.ChipsEditText
import de.greenrobot.event.EventBus
import de.greenrobot.event.Subscribe
import io.focuslauncher.R
import io.focuslauncher.phone.activities.DashboardActivity
import io.focuslauncher.phone.event.SearchLayoutEvent
import io.focuslauncher.phone.token.TokenCompleteType
import io.focuslauncher.phone.token.TokenManager
import io.focuslauncher.phone.token.TokenUpdateEvent

/**
 * Created by Shahab on 2/16/2017.
 */
class SearchLayout : CardView {
    @JvmField
    var txtSearchBox: ChipsEditText? = null
    var btnClear: ImageView? = null
    private val launcherPrefs: SharedPreferences? = null
    private lateinit var inflateLayout: View
    private val formattedTxt = StringBuilder()
    private var isWatching = true

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        isWatching = true
        inflateLayout = inflate(context, R.layout.search_layout, this)
        txtSearchBox = inflateLayout.findViewById(R.id.txtSearchBox)
        btnClear = inflateLayout.findViewById(R.id.btnClear)
        btnClear?.setOnClickListener(OnClickListener { txtSearchBox?.setText("") })
        cardElevation = 4.0f
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(R.attr.theme_base_color, typedValue, true)
        val color = typedValue.data
        setCardBackgroundColor(color)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        setupViews()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventBus.getDefault().register(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        EventBus.getDefault().unregister(this)
    }

    fun setupViews() {
        txtSearchBox!!.addTextChangedListener(object : TextWatcherExtended() {
            override fun afterTextChanged(s: Editable) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int, backSpace: Boolean) {
                if (start <= 2 && s.toString() == "@" && backSpace) {
                } else if (s.toString().length == 1 && s.toString() == "@" && backSpace) {
                    txtSearchBox!!.setText("")
                } else {
                    handleAfterTextChanged(s.toString())
                }
                DashboardActivity.isTextLenghGreater = s.toString()
            }
        })
    }

    fun askFocus() {
        if (DashboardActivity.isTextLenghGreater.length > 0) {
            DashboardActivity.isTextLenghGreater = DashboardActivity.isTextLenghGreater.trim { it <= ' ' }
            handleAfterTextChanged(DashboardActivity.isTextLenghGreater)
        } else {
            if (launcherPrefs!!.getBoolean("isKeyBoardDisplay", false) && txtSearchBox != null) txtSearchBox!!.requestFocus()
            if (btnClear != null) btnClear!!.visibility = INVISIBLE
            if (txtSearchBox != null) txtSearchBox!!.setText("")
        }
    }

    private fun handleAfterTextChanged(s: String) {
        if (isWatching) {
            EventBus.getDefault().post(SearchLayoutEvent(s))
        }
    }

    @Subscribe
    fun tokenManagerEvent(event: TokenUpdateEvent?) {
        buildFormattedText()
        updateSearchField()
    }

    private fun updateSearchField() {
        val splits = formattedTxt.toString().split("\\|").toTypedArray()
        val newText = StringBuilder()
        var space = false
        for (s in splits) {
            if (space) {
                if (!s.startsWith(" ")) newText.append(" ")
            }
            space = true
            newText.append(s.replace("\\^".toRegex(), "").replace("~".toRegex(), ""))
        }
        if (formattedTxt.toString().endsWith("|")) newText.append(" ")
        isWatching = false
        txtSearchBox!!.setText(newText.toString())
        isWatching = true
        var startPos = 0
        var endPos = 0
        for (s in splits) {
            endPos += s.length
            if (s.startsWith("^")) {
                txtSearchBox!!.currentBubbleStyle = BubbleStyle.build(context, R.style.bubble_style_selected)
                txtSearchBox!!.makeChip(startPos, endPos - 1, false, null)
            } else if (s.startsWith("~")) {
                txtSearchBox!!.currentBubbleStyle = BubbleStyle.build(context, R.style.bubble_style_empty)
                txtSearchBox!!.makeChip(startPos, endPos - 1, false, null)
            } else {
                endPos++ // space
            }
            startPos = endPos
        }
        txtSearchBox!!.setSelection(newText.length)
    }

    private fun buildFormattedText() {
        formattedTxt.append("")
        for (item in TokenManager.getInstance().items) {
            if (item.completeType == TokenCompleteType.FULL) {
                if (item.isChipable()) {
                    formattedTxt.append("^")
                }
                formattedTxt.append(item.title).append("|")
            } else if (item.completeType == TokenCompleteType.HALF) {
                if (item.isChipable()) {
                    formattedTxt.append("~")
                }
                formattedTxt.append(item.title).append("|")
            } else {
                formattedTxt.append(item.title)
            }
        }
    }

    abstract inner class TextWatcherExtended : TextWatcher {
        private var lastLength = 0
        abstract fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int, backSpace: Boolean)
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            lastLength = s.length
        }

        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            onTextChanged(charSequence, i, i1, i2, lastLength > charSequence.length)
        }
    }

    companion object {
        private const val TAG = "SearchLayout"
    }
}