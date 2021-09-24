package io.focuslauncher.phone.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log

/*
 Created by rajeshjadi on 10/8/17.
*/
class HomeWatcher(private val mContext: Context) {
    private val mFilter: IntentFilter
    private var mListener: OnHomePressedListener? = null
    private var mRecevier: InnerRecevier? = null
    private var state = ""
    fun setOnHomePressedListener(listener: OnHomePressedListener?) {
        mListener = listener
        mRecevier = InnerRecevier()
    }

    fun startWatch() {
        if (mRecevier != null) {
            try {
                mContext.registerReceiver(mRecevier, mFilter)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopWatch() {
        if (mRecevier != null) {
            try {
                mContext.unregisterReceiver(mRecevier)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    interface OnHomePressedListener {
        fun onHomePressed()
        fun onHomeLongPressed()
    }

    internal inner class InnerRecevier : BroadcastReceiver() {
        val SYSTEM_DIALOG_REASON_KEY = "reason"
        val SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions"
        val SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps"
        val SYSTEM_DIALOG_REASON_HOME_KEY = "homekey"
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action != null && action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
                val reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY)
                if (reason != null) {
                    Log.e(TAG, "action:$action,reason:$reason")
                    if (mListener != null) {
                        if (!state.equals(SYSTEM_DIALOG_REASON_RECENT_APPS, ignoreCase = true) && reason == SYSTEM_DIALOG_REASON_HOME_KEY) {
                            mListener!!.onHomePressed()
                        } else if (reason == SYSTEM_DIALOG_REASON_RECENT_APPS) {
                            mListener!!.onHomeLongPressed()
                        }
                        state = reason
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "hg"
    }

    init {
        mFilter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
    }
}
