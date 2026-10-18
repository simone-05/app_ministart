package com.simonimal.ministart.extensions

import android.content.Context
import org.json.JSONArray
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt


/**
 * Get the color integer from a theme attribute.
 * Example: val color = context.getColorFromAttr(R.attr.colorBackground)
 */
@ColorInt
fun Context.getColorFromAttr(@AttrRes attr: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
}

fun Context.saveHomeAppsToPrefs(list: List<String>) {
    val json = JSONArray(list).toString()

    getSharedPreferences(Prefs.FILE, Context.MODE_PRIVATE)
        .edit()
        .putString(Prefs.Apps.HOME_APPS_LIST, json)
        .apply()
}

fun Context.loadHomeAppsFromPrefs(): List<String> {
    val prefs = getSharedPreferences(Prefs.FILE, Context.MODE_PRIVATE)
    val jsonString = prefs.getString(Prefs.Apps.HOME_APPS_LIST, "[]")

    val jsonArray = JSONArray(jsonString)
    val list = mutableListOf<String>()

    for (i in 0 until jsonArray.length()) {
        list.add(jsonArray.getString(i))
    }

    return list
}

fun Context.addHomeAppToPrefs(pkg: String) {
    val list = loadHomeAppsFromPrefs().toMutableList()
    if (!list.contains(pkg)) {
        list.add(pkg)
        saveHomeAppsToPrefs(list)
    }
}

fun Context.removeHomeAppToPrefs(pkg: String) {
    val list = loadHomeAppsFromPrefs().toMutableList()
    if (list.remove(pkg)) {
        saveHomeAppsToPrefs(list)
    }
}

fun Context.dp(value: Int): Float {
    return value * resources.displayMetrics.density
}