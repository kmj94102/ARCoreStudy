package com.example.arcorestudy.material

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.SeekBar
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import com.example.arcorestudy.R
import com.google.ar.core.Pose
import com.google.ar.core.exceptions.*
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color

fun @receiver:ColorInt Int.toArColor(): Color = Color(this)

fun AppCompatActivity.redirectToApplicationSettings() {
    val intent = Intent().apply {
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        data = Uri.fromParts("package", packageName, null)
    }
    startActivity(intent)
}

fun UnavailableException?.message(): Int {
    return when (this) {
        is UnavailableArcoreNotInstalledException -> R.string.exception_arcore_not_installed
        is UnavailableApkTooOldException -> R.string.exception_apk_too_old
        is UnavailableSdkTooOldException -> R.string.exception_sdk_too_old
        is UnavailableDeviceNotCompatibleException -> R.string.exception_device_not_compatible
        is UnavailableUserDeclinedInstallationException -> R.string.exception_user_declined_installation
        else -> R.string.exception_unknown
    }
}

class SimpleSeekBarChangeListener(val block: (Int) -> Unit) : SeekBar.OnSeekBarChangeListener {
    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        block(progress)
    }
}

fun Pose.translation() = Vector3(tx(), ty(), tz())

fun Pose.rotation() = Quaternion(qx(), qy(), qz(), qw())