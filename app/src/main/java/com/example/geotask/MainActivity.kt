package com.example.geotask

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar

/**
 * [AppCompatActivity] that has a Splash screen and checks if Google Services are available.
 *
 * @author Alexander Gorin
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isServiceEnabled()) {
            startActivity(Intent(this, MapsActivity::class.java))
            finish()
        }
    }

    private fun isServiceEnabled(): Boolean {
        val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        when {
            status == ConnectionResult.SUCCESS -> return true
            GoogleApiAvailability.getInstance().isUserResolvableError(status) -> {
                GoogleApiAvailability.getInstance().getErrorDialog(this, status, DIALOG_REQUEST)
                    .run {
                        show()
                    }
            }
            else -> {
                Toast.makeText(this, getString(R.string.no_services), Snackbar.LENGTH_LONG).show()
            }
        }
        return false
    }

    private companion object {
        const val DIALOG_REQUEST = 111
    }
}