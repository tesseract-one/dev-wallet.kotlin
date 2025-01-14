package one.tesseract.devwallet.ui.settings.key

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import one.tesseract.devwallet.entity.KeySettings
import one.tesseract.devwallet.settings.KeySettingsProvider

class KeySettingsViewModel : ViewModel() {
    private val _cache = MutableLiveData<KeySettings>().apply {
        value = KeySettings("")
    }
    val cache: MutableLiveData<KeySettings> = _cache

    private val _mnemonic = MutableLiveData<String>().apply {
        value = cache.value?.mnemonic
    }
    val mnemonic: MutableLiveData<String> = _mnemonic

    val dirty: LiveData<Boolean> = mnemonic.switchMap { mnemonic ->
        cache.map { cache ->
            !mnemonic.equals(cache.mnemonic)
        }
    }

    lateinit var provider: KeySettingsProvider

    fun save() {
        val settings = KeySettings(mnemonic.value!!)

        provider.save(settings)
        cache.value = settings
    }

    fun load() {
        val loaded = provider.load()

        cache.value = loaded
        mnemonic.value = loaded.mnemonic
    }
}