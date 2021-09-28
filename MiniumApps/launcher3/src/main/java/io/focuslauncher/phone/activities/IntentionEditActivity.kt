package io.focuslauncher.phone.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.transition.Explode
import android.transition.Slide
import android.transition.Transition
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.core.content.ContextCompat
import io.focuslauncher.R
import io.focuslauncher.databinding.ActivityIntentionEditBinding
import io.focuslauncher.phone.helper.FirebaseHelper
import io.focuslauncher.phone.utils.PrefSiempo
import io.focuslauncher.phone.utils.UIUtils
import io.focuslauncher.phone.utils.bindView
import io.focuslauncher.phone.utils.lifecycleProperty

class IntentionEditActivity : CoreActivity(), View.OnClickListener {
    private var strIntentField: String? = null
    private var startTime: Long = 0

    private var binding: ActivityIntentionEditBinding? by lifecycleProperty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val enterTrans: Transition = Explode()
        window.enterTransition = enterTrans
        val returnTrans: Transition = Slide()
        window.returnTransition = returnTrans
        binding = bindView(ActivityIntentionEditBinding::inflate)
        val window = window
        //window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.statusBarColor = resources.getColor(R.color.bg_permissionscreenstatusbar)
        val decor = window.decorView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        initView()
        bindView()
    }

    fun bindView() {
        binding?.toolbar?.apply {
            setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
            setNavigationOnClickListener {
                UIUtils.hideSoftKeyboard(this@IntentionEditActivity, window.decorView.windowToken)
                finish()
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
        }
        strIntentField = PrefSiempo.getInstance(this).read(PrefSiempo.DEFAULT_INTENTION, "")
        binding?.edtIntention?.apply {
            setText(strIntentField)
            setHorizontallyScrolling(false)
            maxLines = 2
            filters = arrayOf<InputFilter>(LengthFilter(48))
            setSelection(text?.length ?: 0)

            setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId ==
                    EditorInfo.IME_ACTION_NEXT || event.action == KeyEvent.ACTION_DOWN
                ) {
                    PrefSiempo.getInstance(this@IntentionEditActivity)
                        .write(PrefSiempo.DEFAULT_INTENTION, strIntentField)
                    UIUtils.hideSoftKeyboard(this@IntentionEditActivity, window.decorView.windowToken)
                    if (!strIntentField.equals("", ignoreCase = true)) {
                        runAnimation()
                    } else {
                        finish()
                    }
                    return@OnEditorActionListener true
                }
                false
            })
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    if (s.toString().trim { it <= ' ' }.isNotEmpty()) {
                        binding?.imgClear?.visibility = View.VISIBLE
                        binding?.hint?.text = getString(R.string.your_intention)
                    } else {
                        binding?.imgClear?.visibility = View.GONE
                        binding?.hint?.text = getString(R.string.what_s_your_intention)
                    }
                    if (PrefSiempo.getInstance(this@IntentionEditActivity).read(PrefSiempo.DEFAULT_INTENTION, "")
                            .equals(s.toString(), ignoreCase = true)
                    ) {
                        binding?.txtSave?.visibility = View.GONE
                    } else {
                        strIntentField = s.toString().trim { it <= ' ' }
                        binding?.txtSave?.visibility = View.VISIBLE
                    }
                }
            })
        }
        binding?.txtSave?.visibility = View.GONE
        if (strIntentField?.trim { it <= ' ' }?.length?.let { it > 0 } == true) {
            binding?.imgClear?.visibility = View.VISIBLE
            binding?.hint?.text = getString(R.string.your_intention)
        } else {
            binding?.hint?.text = getString(R.string.what_s_your_intention)
            binding?.imgClear?.visibility = View.GONE
            txtHelp()
        }

    }

    fun imgClear() {
        binding?.edtIntention?.setText("")
    }

    fun txtSave() {
        PrefSiempo.getInstance(this@IntentionEditActivity).write(PrefSiempo.DEFAULT_INTENTION, strIntentField)
        UIUtils.hideSoftKeyboard(this@IntentionEditActivity, window.decorView.windowToken)
        if (!strIntentField.equals("", ignoreCase = true)) {
            runAnimation()
        } else {
            finish()
        }
    }

    private fun runAnimation() {
        binding?.top?.apply {
            val layoutParams = layoutParams as MarginLayoutParams
            layoutParams.setMargins(0, 0, 0, 0)
            requestLayout()
            cardElevation = 0f
            radius = 0f
        }

        binding?.toolbar
            ?.animate()
            ?.alpha(0.0f)
            ?.duration = 200
        binding?.imgClear?.animate()?.alpha(0.0f)?.duration = 200
        binding?.linHelpWindow?.animate()?.alpha(0.0f)?.duration = 200
        binding?.hint?.text = getString(R.string.your_intention)
        //        hint.setVisibility(View.GONE);
        binding?.imgClear?.visibility = View.GONE
        binding?.edtIntention?.isFocusable = false
        val anim = ValueAnimator.ofInt(
            (binding?.linEditText?.measuredHeight ?: 0) + UIUtils.getStatusBarHeight(this) + 40,
            binding?.pauseContainer?.height ?: 0
        )
        anim.addUpdateListener { valueAnimator ->
            val animVal = valueAnimator.animatedValue as Int
            val layoutParams = binding?.linEditText?.layoutParams
            layoutParams?.height = animVal
            binding?.toolbar?.visibility = View.GONE
            binding?.linEditText?.layoutParams = layoutParams
        }
        val from = window.navigationBarColor
        val to = ContextCompat.getColor(this, R.color.dialog_blue) // new color to
        // animate to
        val colorAnimation = ValueAnimator.ofArgb(from, to)
        colorAnimation.addUpdateListener { animator -> window.navigationBarColor = (animator.animatedValue as Int) }
        colorAnimation.addUpdateListener { animator -> window.statusBarColor = (animator.animatedValue as Int) }
        colorAnimation.duration = 500
        colorAnimation.start()
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                Handler().postDelayed({
                    finish()
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                }, 1500)
            }
        })
        anim.duration = 500
        anim.start()
    }

    fun txtOne() {
        binding?.viewFlipper?.apply {
            when (displayedChild) {
                0 -> {
                    binding?.linHelpWindow?.visibility = View.GONE
                    binding?.txtHelp?.visibility = View.VISIBLE
                    inAnimation = null
                    outAnimation = null
                    displayedChild = 0
                }
                1 -> {
                    binding?.txtOne?.text = "CLOSE"
                    binding?.txtTwo?.text = "NEXT"
                    setInAnimation(this@IntentionEditActivity, R.anim.in_from_left)
                    setOutAnimation(this@IntentionEditActivity, R.anim.out_to_right)
                    showPrevious()
                }
                2 -> {
                    setInAnimation(this@IntentionEditActivity, R.anim.in_from_left)
                    setOutAnimation(this@IntentionEditActivity, R.anim.out_to_right)
                    showPrevious()
                }
                3 -> {
                    setInAnimation(this@IntentionEditActivity, R.anim.in_from_left)
                    setOutAnimation(this@IntentionEditActivity, R.anim.out_to_right)
                    showPrevious()
                    binding?.txtOne?.text = "PREVIOUS"
                    binding?.txtTwo?.text = "NEXT"
                }
            }
        }
    }

    fun txtTwo() {
        binding?.viewFlipper?.apply {
            if (displayedChild == 0) {
                binding?.txtOne?.text = "PREVIOUS"
                binding?.txtTwo?.text = "NEXT"
                setInAnimation(this@IntentionEditActivity, R.anim.in_from_right)
                setOutAnimation(this@IntentionEditActivity, R.anim.out_to_left)
                showNext()
            } else if (displayedChild == 1) {
                binding?.txtOne?.text = "PREVIOUS"
                binding?.txtTwo?.text = "NEXT"
                setInAnimation(this@IntentionEditActivity, R.anim.in_from_right)
                setOutAnimation(this@IntentionEditActivity, R.anim.out_to_left)
                showNext()
            } else if (displayedChild == 2) {
                binding?.txtOne?.text = "PREVIOUS"
                binding?.txtTwo?.text = "CLOSE"
                setInAnimation(this@IntentionEditActivity, R.anim.in_from_right)
                setOutAnimation(this@IntentionEditActivity, R.anim.out_to_left)
                showNext()
            } else if (displayedChild == 3) {
                binding?.linHelpWindow?.visibility = View.GONE
                binding?.txtHelp?.visibility = View.VISIBLE
            }
        }
    }

    fun txtHelp() {
        binding?.apply {
            txtOne.text = "CLOSE"
            txtTwo.text = "NEXT"
            txtHelp.visibility = View.GONE
            viewFlipper.inAnimation = null
            viewFlipper.outAnimation = null
            viewFlipper.displayedChild = 0
        }
        binding?.linHelpWindow?.visibility = View.VISIBLE
    }

    private fun initView() {
        binding?.apply {
            txtSave.setOnClickListener(this@IntentionEditActivity)
            imgClear.setOnClickListener(this@IntentionEditActivity)
            txtHelp.setOnClickListener(this@IntentionEditActivity)
            txtOne.setOnClickListener(this@IntentionEditActivity)
            txtTwo.setOnClickListener(this@IntentionEditActivity)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.txtSave -> txtSave()
            R.id.imgClear -> imgClear()
            R.id.txtHelp -> txtHelp()
            R.id.txtOne -> txtOne()
            R.id.txtTwo -> txtTwo()
            else -> {
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startTime = System.currentTimeMillis()
    }

    override fun onPause() {
        super.onPause()
        FirebaseHelper.getInstance().logScreenUsageTime(this.javaClass.simpleName, startTime)
    }
}