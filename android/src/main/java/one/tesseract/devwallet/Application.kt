package one.tesseract.devwallet

class Application: android.app.Application() {
    lateinit var core: Core

    override fun onCreate() {
        super.onCreate()

        core = Core(UI(this), applicationInfo.dataDir)

        //getSharedPreferences()
    }
}