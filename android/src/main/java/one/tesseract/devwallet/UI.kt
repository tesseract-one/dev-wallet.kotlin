package one.tesseract.devwallet

import android.os.Parcelable
import java.util.concurrent.CompletionStage

import one.tesseract.transport.ipc.activity.ActivityMonitor
import one.tesseract.transport.ipc.activity.free.Launcher

import one.tesseract.devwallet.ui.sign.SignActivity

@Suppress("unused") //The class in used from Rust
class UI(application: Application) {
    private val launcher: Launcher = Launcher(ActivityMonitor(application))

    suspend fun <T: Parcelable>requestUserConfirmation(request: T): Boolean {
        return SignActivity.requestUserConfirmation(launcher, request)
    }
}