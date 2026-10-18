package com.simonimal.ministart.ui.widgets

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatTextView

import com.simonimal.ministart.ui.animations.ViewAnimations


class AppTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var packageNameToLaunch: String? = null

    init {
        ViewAnimations.textClickAnimations(
            textView = this,
            fade = true,
            shrink = true
        )

        setupClicks()
    }

    /**
     * Saving the package name (com.example.app) to this object
     */
    fun bindPackage(packageName: String) {
        packageNameToLaunch = packageName
    }

    /**
     * Adding pressed/click animation,
     * launch app and go to app info on long press,
     * to the apps text views
     */
    private fun setupClicks() {
        // Launch app on click
        setOnClickListener {
            packageNameToLaunch?.let { packageName ->
                val launchIntent =
                    context.packageManager.getLaunchIntentForPackage(packageName)
                launchIntent?.let {
                    context.startActivity(it)
                }
            }
        }

        // Open app info on long click
        setOnLongClickListener {
            packageNameToLaunch?.let { packageName ->
                val intent = Intent(
                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    android.net.Uri.parse("package:$packageName")
                )
                context.startActivity(intent)
            }
            true
        }
    }
}