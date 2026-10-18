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
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.math.abs

import com.simonimal.ministart.extensions.*
import com.simonimal.ministart.activities.settings.SettingsActivity
import com.simonimal.ministart.model.AppInfo
import com.simonimal.ministart.ui.animations.ViewAnimations
import com.simonimal.ministart.ui.widgets.NestedScrollGesture
import com.simonimal.ministart.ui.widgets.AppTextView
import com.simonimal.ministart.apps.AppRepository
import com.simonimal.ministart.apps.AppLauncher
import com.simonimal.ministart.activities.home.AppDrawer


class AppDrawer(
    private val homeActivity: HomeActivity,
    private val binding: ActivityHomeBinding,
    private val appLauncher: AppLauncher,
    private val lifecycleScope: LifecycleCoroutineScope
) {

    private val searchEditText = binding.searchEditText
    private val appsContainer = binding.appDrawerAppsContainer
    private val scrollView = binding.appDrawerScrollView
    private val drawerLayout = binding.appDrawerLayout

    private var filteredApps = listOf<AppInfo>()
    private var background = binding.appDrawerCard
    private var homeScreenTop: Float = 0f
    private var homeScreenBottom: Float = 0f
    private var drawerClosedY: Float = 0f

    var isDrawerVisible: Boolean = false

    fun init() {
        AppRepository.addListener {
            filteredApps = AppRepository.getApps(homeActivity)
            displayFilteredApps()
        }

        var isAtTop: Boolean = true
        var swipeDir: Int = 0
        var totDy: Float = 0f

        binding.homeScreenLayout.post{
            homeScreenBottom = binding.homeScreenLayout.height.toFloat()
            drawerClosedY = homeScreenBottom -1000f
            homeScreenTop = binding.homeScreenLayout.y.toFloat()
        }

        // Setup search functionality
        setupSearch()

        // scrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
        //     if (scrollY == 0) {
        //         isAtTop = true
        //     } else {
        //         isAtTop = false
        //     }
        // }

        drawerLayout.onGestureDownListener =
        NestedScrollGesture.OnGestureDownListener { x, y ->
            homeActivity.hideKeyboard(searchEditText)
            isAtTop = !scrollView.canScrollVertically(-1)
            drawerLayout.canChildScrollUp = {
                !scrollView.canScrollVertically(-1)
            }
        }

        drawerLayout.onGestureMoveListener =
        NestedScrollGesture.OnGestureMoveListener { verticalSwipeDir, _, dy, _, deltaY ->
            swipeDir = verticalSwipeDir
            if (isAtTop && swipeDir == 1) {
                scrollView.post {
                    scrollView.scrollTo(0, 0)
                }
                totDy = dy
                drawerLayout.translationY += deltaY
                val newY = drawerLayout.y

                val linInterpValue = 1- (newY / drawerClosedY).coerceIn(0f, 1f)
                val newAlpha = kotlin.math.ln(1 + 9 * linInterpValue) / kotlin.math.ln(10f)

                background.alpha = newAlpha
                drawerLayout.alpha = newAlpha
                binding.homeClockLinearLayout.alpha = 1 - newAlpha
                binding.homeAppsLinearLayout.alpha = 1 - newAlpha
            }
        }

        drawerLayout.onGestureEndListener =
        NestedScrollGesture.OnGestureEndListener { vx, vy ->
            if (isAtTop && swipeDir == 1) {
                if (vy > Constants.SWIPE_VELOCITY_THRESHOLD || totDy > homeActivity.dp(Constants.SWIPE_DISTANCE_THRESHOLD).toFloat()) {
                    hideAppDrawer()
                } else {
                    if (drawerLayout.y < (homeScreenBottom + homeScreenTop)/2f) showAppDrawer(keep=true) else hideAppDrawer()
                }
            }
        }
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                filteredApps = AppRepository.filterApps(
                    AppRepository.getApps(homeActivity),
                    query
                )
                displayFilteredApps()
                if (filteredApps.size == 1 && query.isNotBlank()) {
                    appLauncher.launch(filteredApps.first().packageName)
                    lifecycleScope.launch {
                        delay(500)
                        hideAppDrawer()
                    }
                }
            }
        })
    }

    /**
     * Display filtered apps in drawer
     */
    private fun displayFilteredApps() {
        appsContainer.removeAllViews()

        for (app in filteredApps) {
            val appTextView = AppTextView(homeActivity)
            appTextView.text = app.label
            appTextView.textSize = 30f
            appTextView.setTextColor(homeActivity.uiTextColor)
            appTextView.bindPackage(app.packageName)

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,     // width
                LinearLayout.LayoutParams.WRAP_CONTENT      // height
            )
            val margin = (20 * homeActivity.resources.displayMetrics.density).toInt()
            layoutParams.setMargins(0, margin, 0, 0)
            appTextView.layoutParams = layoutParams

            appsContainer.addView(appTextView)
        }
    }

    fun showAppDrawer(keep: Boolean = false) {
        val springAnim = SpringAnimation(drawerLayout, SpringAnimation.Y, homeScreenTop).apply {
            spring.dampingRatio = SpringForce.DAMPING_RATIO_LOW_BOUNCY
            spring.stiffness = SpringForce.STIFFNESS_MEDIUM
            addEndListener { _, _, _, _ ->
                // optional code when animation ends
            }
        }
        springAnim.start()

        val otherAnimations: List<Pair<View, ViewPropertyAnimator.() -> Unit>> = listOf(
            drawerLayout to { alpha(1f) },
            background to { alpha(1f) },
            binding.homeClockLinearLayout to { alpha(0f) },
            binding.homeAppsLinearLayout to { alpha(0f) }
        )

        otherAnimations.forEach { (view, anim) ->
            view.animate().apply {
                duration = Constants.APPDRAWER_ANIMATION_DURATION
                anim(this)
                start()
            }
        }

        if (!keep) {
            scrollView.post {
                scrollView.scrollTo(0, 0)
            }

            searchEditText.setText("", TextView.BufferType.EDITABLE)
            displayFilteredApps()
        }

        // Set drawer's search bar text color
        searchEditText.setTextColor(homeActivity.uiTextColor)
        searchEditText.setHintTextColor(Color.argb(128, Color.red(homeActivity.uiTextColor), Color.green(homeActivity.uiTextColor), Color.blue(homeActivity.uiTextColor)))

        // Focus search bar
        searchEditText.requestFocus()
        homeActivity.showKeyboard(searchEditText)
        isDrawerVisible = true
    }

    fun hideAppDrawer() {
        val animations: List<Pair<View, ViewPropertyAnimator.() -> Unit>> = listOf(
            drawerLayout to {
                y(drawerClosedY);
                alpha(0f);
                withEndAction{
                    drawerLayout.visibility = View.GONE
                }},
            background to { alpha(0f) },
            binding.homeClockLinearLayout to { alpha(1f) },
            binding.homeAppsLinearLayout to { alpha(1f) }
        )

        animations.forEach { (view, anim) ->
            view.animate().apply {
                setDuration(Constants.APPDRAWER_ANIMATION_DURATION)
                anim(this)
                start()
            }
        }

        searchEditText.setText("")
        searchEditText.clearFocus()
        homeActivity.hideKeyboard(searchEditText)
        isDrawerVisible = false
    }

}