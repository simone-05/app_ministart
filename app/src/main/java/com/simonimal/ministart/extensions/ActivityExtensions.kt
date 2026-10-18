package com.simonimal.ministart.extensions

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.WindowInsetsControllerCompat
import android.view.WindowManager
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import android.graphics.drawable.ColorDrawable
import android.util.Log


/**
 * Set textColor to black or white according to boolean
 */
fun Activity.getTextColorBlack(isBlack: Boolean): Int {
    return if (isBlack) Color.BLACK else Color.WHITE
}

/**
 * Set status bar background color
 */
fun Activity.setStatusBarBackground(color: Int) {
    window.statusBarColor = color
}

/**
 * Set navigation bar background color
 */
fun Activity.setNavBarBackground(color: Int) {
    window.navigationBarColor = color
}

/**
 * Set status bar text color to black or white according to boolean
 */
fun Activity.setStatusBarTextColorBlack(isBlack: Boolean) {
    val controller = WindowInsetsControllerCompat(window, window.decorView)
    controller.isAppearanceLightStatusBars = isBlack
}

/**
 * Set status navigation text color to black or white according to boolean
 */
fun Activity.setNavBarTextColorBlack(isBlack: Boolean) {
    val controller = WindowInsetsControllerCompat(window, window.decorView)
    controller.isAppearanceLightNavigationBars = isBlack
}

/**
 * Set a the background of an activity root component
 * depending on the parameter passed
 * If the parameter is null then the window will be transparent
 * Else it will be either a color or drawable (image)
 */
fun Activity.setBackground(rootView: View, background: Drawable? = null) {
    if (background == null) {
        rootView.setBackgroundColor(Color.TRANSPARENT)
    } else {
        rootView.background = background
    }
}

/**
 * Overload
 */
fun Activity.setBackground(rootView: View, @ColorInt color: Int) {
    rootView.setBackgroundColor(color)
}


fun Activity.hideKeyboard(view: View) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Activity.showKeyboard(view: View) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}