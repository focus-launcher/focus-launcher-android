package io.focuslauncher.phone.utils

import android.view.LayoutInflater
import androidx.activity.ComponentActivity
import androidx.viewbinding.ViewBinding

fun <VB : ViewBinding> ComponentActivity.bindView(inflate: (LayoutInflater) -> VB): VB {
    val binding = inflate(LayoutInflater.from(this))
    setContentView(binding.root)
    return binding
}