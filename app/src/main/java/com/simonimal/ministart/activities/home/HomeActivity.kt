package com.simonimal.ministart.activities.home

import com.simonimal.ministart.databinding.ActivityHomeBinding
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import android.widget.LinearLayout
import android.widget.FrameLayout
import android.widget.TextView
import android.content.Intent
import android.widget.Toast
import android.util.Log
import android.provider.AlarmClock
import android.os.Handler
import android.view.WindowManager
import android.view.ViewPropertyAnimator
import androidx.core.view.WindowCompat
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts

import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.graphics.Color
import android.view.VelocityTracker
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce

import android.view.View
import android.widget.EditText
import androidx.core.widget.NestedScrollView
import com.google.android.material.card.MaterialCardView
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.content.pm.PackageManager
import android.view.inputmethod.InputMethodManager
import android.content.res.Configuration
import android.content.SharedPreferences
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.content.Context
import android.view.WindowInsetsController
import java.io.File
import java.util.Locale
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.app.WallpaperManager
import android.graphics.drawable.BitmapDrawable
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.math.abs

import com.simonimal.ministart.extensions.*
import com.simonimal.ministart.activities.settings.SettingsActivity
import com.simonimal.ministart.model.AppInfo
import com.simonimal.ministart.ui.animations.ViewAnimations
import com.simonimal.ministart.ui.widgets.GestureInterceptLayout
import com.simonimal.ministart.ui.widgets.AppTextView
import com.simonimal.ministart.apps.AppRepository
import com.simonimal.ministart.apps.AppLauncher
import com.simonimal.ministart.activities.home.AppDrawer


class HomeActivity : AppCompatActivity() {

    // packages apps list to show in the home
    private val defaultHomeApps = listOf(
        "com.whatsapp",
        "com.android.chrome",
        "com.google.android.apps.maps",
        "com.duolingo",
        "org.breezyweather"
    )

    /* Custom way to launch the Settings activity,
    so that i can detect if the custom wallpaper or the text color settings
    were changed, so that i can update the ui colors and wallpaper, instead
    of doing it always in onResume
    */
    private val settingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult

            if (data.getBooleanExtra("text_color_setting_changed", false)) {
                updateUiColors()
            }
            if (data.getBooleanExtra("wallpaper_setting_changed", false)) {
                updateUiColors()
                updateWallpaper()
            }
        }
    }

    private var velocityTracker: VelocityTracker? = null

    private var isSystemDarkModeOn: Boolean = false
    private var isUsingWallpaper: Boolean = false

    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var appLauncher: AppLauncher
    private lateinit var appDrawer: AppDrawer
    private lateinit var binding: ActivityHomeBinding
    private lateinit var packageReceiver: BroadcastReceiver

    private var homeScreenBottom: Float = 0f
    private var homeScreenTop: Float = 0f
    private var drawerClosedY: Float = 0f

    var uiTextColor = Color.WHITE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**
         * Automatically update the list of shown apps when an app
         * is installed or removed from the device
         */
        packageReceiver = object : BroadcastReceiver() {
            // What to do when a trigger is received (update the saved/cached list of apps)
            override fun onReceive(context: Context?, intent: Intent?) {
                // appDrawer.refreshApps()
                AppRepository.refreshApps(this@HomeActivity)
            }
        }
        // The triggers to listen to (the apps added changed or uninstalled from the device)
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        // We register the receiver with the corresponding triggers (filters)
        registerReceiver(
            packageReceiver,
            filter,
            Context.RECEIVER_NOT_EXPORTED
        )

        // Extend layout content behind the statusbar and
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Status bar and navbar background color to transparent
        setStatusBarBackground(Color.TRANSPARENT)
        setNavBarBackground(Color.TRANSPARENT)

        sharedPrefs = getSharedPreferences(Prefs.FILE, MODE_PRIVATE)

        // Save current system's dark/light theme mode
        isSystemDarkModeOn = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        isUsingWallpaper = sharedPrefs.getBoolean(Prefs.Wallpaper.IS_USING_WALLPAPER, false)

        appLauncher = AppLauncher(this)
        appDrawer = AppDrawer(
            this,
            binding,
            appLauncher,
            lifecycleScope
        )

        binding.homeScreenLayout.post{
            homeScreenBottom = binding.homeScreenLayout.height.toFloat()
            drawerClosedY = homeScreenBottom -1000f
            homeScreenTop = binding.homeScreenLayout.y.toFloat()
            appDrawer.hideAppDrawer()
        }

        appDrawer.init()
        setupClock()
        setupDate()
        setupHomeApps()
        setupGestures()
        updateUiColors()
        updateWallpaper()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (appDrawer.isDrawerVisible) {
                    appDrawer.hideAppDrawer()
                } else {
                    // Don't call super to prevent exiting launcher
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        sharedPrefs = getSharedPreferences(Prefs.FILE, MODE_PRIVATE)
        appDrawer.hideAppDrawer()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(packageReceiver)
    }

    /**
     * Setup clock
     */
    private fun setupClock() {
        val clockTextView = binding.clockTextView
        // val timeFormat = DateFormat.getBestDateTimePattern(Locale.getDefault(), "HH:mm")     // 12 hour format with am/pm
        val timeFormat = DateFormat
            .getBestDateTimePattern(Locale.getDefault(), "hm")
            .replace("a", "").trim()    // 12 hour format without am/pm

        ViewAnimations.textClickAnimations(clockTextView, true, false)

        val handler = android.os.Handler(mainLooper)
        val runnable = object : Runnable {
            override fun run() {
                val currentTime = java.text.SimpleDateFormat(timeFormat, Locale.getDefault()).format(java.util.Date())
                clockTextView.text = currentTime
                clockTextView.setTextColor(uiTextColor)
                // launch clock app on click
                clockTextView.setOnClickListener {
                    try {
                        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(this@HomeActivity, "No clock app found", Toast.LENGTH_SHORT).show()
                    }
                }
                handler.postDelayed(this, Constants.CLOCK_UPDATE_INTERVAL)
            }
        }
        handler.post(runnable)
    }

    /**
     * Setup date: month day and weekday
     */
    private fun setupDate() {
        val dateTextView = binding.dateTextView
        val dateFormat = DateFormat.getBestDateTimePattern(Locale.getDefault(), "EEEE, d")
        val currentDate = java.text.SimpleDateFormat(dateFormat, Locale.getDefault()).format(java.util.Date())

        ViewAnimations.textClickAnimations(dateTextView, true, false)

        dateTextView.text = currentDate
        dateTextView.setTextColor(uiTextColor)
        // launch clendar app on click
        dateTextView.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_APP_CALENDAR)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this@HomeActivity, "No calendar app found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Setup home apps
     */
    private fun setupHomeApps() {
        val homeAppsContainer = binding.homeAppsLinearLayout
        var homeApps = loadHomeAppsFromPrefs()
        if (homeApps.isEmpty()) {
            homeApps = defaultHomeApps
            saveHomeAppsToPrefs(homeApps)
        }

        homeAppsContainer.removeAllViews()

        val packageManager = packageManager

        for (packageName in homeApps) {
            try {
                val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                if (launchIntent != null) {
                    val appInfo = packageManager.getApplicationInfo(packageName, 0)
                    val appLabel = packageManager.getApplicationLabel(appInfo).toString()

                    val appTextView = AppTextView(this)
                    appTextView.text = appLabel
                    appTextView.textSize = 30f
                    appTextView.setTextColor(uiTextColor)
                    appTextView.bindPackage(packageName)
                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    appTextView.layoutParams = layoutParams
                    val margin = (15 * resources.displayMetrics.density).toInt()
                    // just top margin
                    layoutParams.setMargins(0, margin, 0, 0)

                    homeAppsContainer.addView(appTextView)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    /**
     * Implemented gestures:
     * - doble tap to sleep
     * - swipe up to open app drawer
     * - swipe down to open notifications
     * - long tap to open settings
     * - swipe left for camera
     * - swipe right for phone dialer
     */
    private fun setupGestures() {
        var swipeDir: Int = 0
        var totDy: Float = 0f

        binding.homeScreenLayout.onLongPressListener =
        GestureInterceptLayout.OnLongPressListener {
            if (!appDrawer.isDrawerVisible) {
                settingsLauncher.launch(Intent(this, SettingsActivity::class.java))
            }
        }

        binding.homeScreenLayout.onDoubleTapListener =
        GestureInterceptLayout.OnDoubleTapListener {
            if (!appDrawer.isDrawerVisible) {
                try {
                    Runtime.getRuntime().exec(arrayOf("su", "-c", "input keyevent 26"))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        binding.homeScreenLayout.onGestureMoveListener =
        GestureInterceptLayout.OnGestureMoveListener { verticalSwipeDir, _, dy, _, deltaY ->
            binding.appDrawerLayout.visibility = View.VISIBLE
            swipeDir = verticalSwipeDir
            totDy = dy
            binding.appDrawerLayout.translationY += deltaY
            val newY = binding.appDrawerLayout.y

            val linInterpValue = 1- (newY / drawerClosedY).coerceIn(0f, 1f)
            val newAlpha = kotlin.math.ln(1 + 9 * linInterpValue) / kotlin.math.ln(10f)

            binding.appDrawerCard.alpha = newAlpha
            binding.appDrawerLayout.alpha = newAlpha
            binding.homeClockLinearLayout.alpha = 1 - newAlpha
            binding.homeAppsLinearLayout.alpha = 1 - newAlpha
        }

        binding.homeScreenLayout.onGestureEndListener =
        GestureInterceptLayout.OnGestureEndListener { vx, vy ->
            /* Horizontal swipes
            */
            if (abs(vx) > abs(vy) * Constants.VELOCITY_DIR_MULTIP && abs(vx) > Constants.SWIPE_VELOCITY_THRESHOLD) {
                if (vx > 0) appLauncher.launch("com.google.android.dialer") else appLauncher.launch("com.google.android.GoogleCameraEng")
            }

            /* Notifications swipe down
            */
            if (swipeDir == 1 && !appDrawer.isDrawerVisible && (
                totDy > this.dp(Constants.SWIPE_DISTANCE_THRESHOLD).toFloat() || vy > Constants.SWIPE_VELOCITY_THRESHOLD)) {
                try {
                    val statusBarService = getSystemService("statusbar")
                    val statusBarManager = Class.forName("android.app.StatusBarManager")
                    val method = statusBarManager.getMethod("expandNotificationsPanel")
                    method.invoke(statusBarService)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }

            /* Drawer swipes
            */
            if (abs(vy) > Constants.SWIPE_VELOCITY_THRESHOLD) {
                // The velocity of the swipe is enough
                if (vy > 0) appDrawer.hideAppDrawer() else appDrawer.showAppDrawer()
            } else {
                // The velocity of the swipe is not enough
                if (abs(totDy) > this.dp(Constants.SWIPE_DISTANCE_THRESHOLD).toFloat()) {
                    // The distance of the swipe is enough
                    if (totDy > 0) appDrawer.hideAppDrawer() else appDrawer.showAppDrawer()
                } else {
                    // The distance is not enough, we put back the drawer,
                    // We have to check in which half of the screen is the drawer
                    if (binding.appDrawerLayout.y < (homeScreenBottom + homeScreenTop)/2f) appDrawer.showAppDrawer() else appDrawer.hideAppDrawer()
                }
            }
        }
    }

    /**
     * Sets the wallpaper, status bar and navbar
     * according to the light/dark theme,
     * and according to the custom wallpaper used or selected
     */
    private fun updateWallpaper() {
        val isUsingWallpaper = sharedPrefs.getBoolean(Prefs.Wallpaper.IS_USING_WALLPAPER, false)
        val isDarkWallpaperSelected = sharedPrefs.getBoolean(Prefs.Wallpaper.IS_DARK_WALLPAPER_SELECTED, false)
        val isDarkWallpaperUsed = sharedPrefs.getBoolean(Prefs.Wallpaper.IS_DARK_WALLPAPER_USED, false)

        if (isUsingWallpaper) {
            // Using the custom selected wallpaper/s
            if (isSystemDarkModeOn && isDarkWallpaperSelected && isDarkWallpaperUsed) {
                // Set dark wallpaper
                try {
                    val wallpaperPath = sharedPrefs.getString(Prefs.Wallpaper.DARK_WALLPAPER_PATH, null)
                    val bitmap = BitmapFactory.decodeFile(wallpaperPath)
                    val drawable = BitmapDrawable(resources, bitmap)
                    setBackground(binding.homeScreenLayout, drawable)
                } catch (e: Exception) {
                    setBackground(binding.homeScreenLayout, Color.BLACK)
                    Log.e("TAG", "Couldn't load custom dark wallpaper")
                    e.printStackTrace()
                }
            } else {
                // Set light wallpaper
                try {
                    setBackground(binding.homeScreenLayout)
                } catch (e: Exception) {
                    setBackground(binding.homeScreenLayout, Color.WHITE)
                    Log.e("TAG", "Error setting light wallpaper")
                    e.printStackTrace()
                }
            }
        } else {
            // No custom wallpaper/s selected -> use just black or white background according to system theme
            if (isSystemDarkModeOn) {
                // dark system theme -> black background
                setBackground(binding.homeScreenLayout, Color.BLACK)
            } else {
                // light system theme -> white background
                setBackground(binding.homeScreenLayout, Color.WHITE)
            }
        }
    }

    /**
     * Sets the ui elements', navbar and status bar texts according to the
     * current system ui, or if custom wallpaper is set, to the specified
     * setting in the settings.
     */
    private fun updateUiColors() {
        val isUsingWallpaper = sharedPrefs.getBoolean(Prefs.Wallpaper.IS_USING_WALLPAPER, false)

        if (isUsingWallpaper) {
            uiTextColor = getTextColorBlack(sharedPrefs.getBoolean(Prefs.Text.IS_DARK, false))
        } else {
            uiTextColor = if (isSystemDarkModeOn) Color.WHITE else Color.BLACK
        }

        // Update status bar text color
        setStatusBarTextColorBlack(uiTextColor == Color.BLACK)
        // Update navigation bar colors
        if (isUsingWallpaper) {
            setNavBarTextColorBlack(false)
        } else {
            setNavBarTextColorBlack(uiTextColor == Color.BLACK)
        }
        // Update clock text color
        binding.clockTextView.setTextColor(uiTextColor)
        // Update date text color
        binding.dateTextView.setTextColor(uiTextColor)
        // Update home apps text color
        for (i in 0 until binding.homeAppsLinearLayout.childCount) {
            val child = binding.homeAppsLinearLayout.getChildAt(i)
            if (child is TextView) {
                child.setTextColor(uiTextColor)
            }
        }
    }

}
