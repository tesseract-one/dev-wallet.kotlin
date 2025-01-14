package one.tesseract.devwallet.ui.sign

import android.os.Bundle
import android.os.Parcelable
import android.widget.Button

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit

import one.tesseract.activity.detached.Launcher
import one.tesseract.activity.detached.finishDetachedActivity
import one.tesseract.activity.detached.getExtras

import one.tesseract.devwallet.R
import one.tesseract.devwallet.entity.request.SubstrateAccount
import one.tesseract.devwallet.entity.request.SubstrateSign
import one.tesseract.devwallet.entity.request.TestError
import one.tesseract.devwallet.entity.request.TestSign
import one.tesseract.devwallet.ui.sign.fragments.substrate.account.SubstrateAccountFragment
import one.tesseract.devwallet.ui.sign.fragments.substrate.sign.SubstrateSignFragment
import one.tesseract.devwallet.ui.sign.fragments.test.error.TestErrorFragment
import one.tesseract.devwallet.ui.sign.fragments.test.sign.TestSignFragment

class SignActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST = "request"

        suspend fun <T: Parcelable>requestUserConfirmation(launcher: Launcher, request: T): Boolean {
            val bundle = Bundle()

            bundle.putParcelable(REQUEST, request)

            return launcher.startDetachedActivityForResult<Boolean>(SignActivity::class.java, bundle).second
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = getExtras() ?: throw RuntimeException("No Extras :(")
        @Suppress("DEPRECATION") val request: Any = extras.getParcelable(REQUEST) ?: throw RuntimeException("No Request")

        val fragment = when (request) {
            is TestSign -> {
                TestSignFragment(request)
            }
            is TestError -> {
                TestErrorFragment(request)
            }
            is SubstrateAccount -> {
                SubstrateAccountFragment(request)
            }
            is SubstrateSign -> {
                SubstrateSignFragment(request)
            }
            else -> {
                throw RuntimeException("Please, don't send garbage here")
            }
        }

        if(savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true) //must be here. otherwise compat mode
                replace(R.id.transactionFragmentContainerView, fragment)
            }
        }

        setContentView(R.layout.activity_sign)

        val buttonSign = findViewById<Button>(R.id.buttonSign)
        val buttonCancel = findViewById<Button>(R.id.buttonCancel)

        buttonSign.setOnClickListener {
            this.finishDetachedActivity(RESULT_OK, true)
        }

        buttonCancel.setOnClickListener {
            this.finishDetachedActivity(RESULT_CANCELED, false)
        }
    }
}