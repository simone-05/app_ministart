package com.simonimal.ministart.activities.settings

import com.simonimal.ministart.databinding.ActivitySettingsBinding
import com.simonimal.ministart.R
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import java.io.File
import java.io.FileOutputStream
import com.google.android.material.materialswitch.MaterialSwitch
import android.widget.RadioGroup
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.button.MaterialButtonGroup
import androidx.core.view.WindowCompat
import android.util.Log
import android.content.SharedPreferences
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import android.util.TypedValue
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator

import com.simonimal.ministart.extensions.setStatusBarBackground
import com.simonimal.ministart.extensions.setNavBarBackground
import com.simonimal.ministart.extensions.setStatusBarTextColorBlack
import com.simonimal.ministart.extensions.setNavBarTextColorBlack


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Extend layout content behind the status bar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Make status bar and navbar transparent
        setStatusBarBackground(Color.TRANSPARENT)
        setNavBarBackground(Color.TRANSPARENT)
        // Set status bar text color according to system's dark/light theme
        setStatusBarTextColorBlack(!((resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES))
        // Set navigation bar text color according to system's dark/light theme
        setNavBarTextColorBlack(!((resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES))

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.settingsContainer, SettingsMainFragment())
                .commit()
        }
    }
}