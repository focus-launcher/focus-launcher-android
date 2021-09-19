package io.focuslauncher.phone.utils

import android.app.Fragment
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class LifecycleProperty<O, T>: ReadWriteProperty<O, T?>, LifecycleEventObserver {

    private var property: T? = null

    override fun getValue(thisRef: O, property: KProperty<*>): T? {
        return this.property
    }

    override fun setValue(thisRef: O, property: KProperty<*>, value: T?) {
        this.property = value
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            source.lifecycle.removeObserver(this)
        }
    }
}

fun <T> ComponentActivity.lifecycleProperty(): LifecycleProperty<ComponentActivity, T> {
    val property = LifecycleProperty<ComponentActivity, T>()
    if (lifecycle.currentState != Lifecycle.State.DESTROYED) {
        lifecycle.addObserver(property)
    }
    return property
}