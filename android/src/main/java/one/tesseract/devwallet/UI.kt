package one.tesseract.devwallet

import android.os.Parcelable

import one.tesseract.activity.ActivityMonitor
import one.tesseract.activity.detached.Launcher

import one.tesseract.devwallet.ui.sign.SignActivity

@Suppress("unused") //The class in used from Rust
class UI(application: Application) {
    private val launcher: Launcher = Launcher(ActivityMonitor(application))

    suspend fun <T: Parcelable>requestUserConfirmation(request: T): Boolean {
        return SignActivity.requestUserConfirmation(launcher, request)
    }
}