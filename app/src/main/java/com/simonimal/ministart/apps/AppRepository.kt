package com.simonimal.ministart.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.simonimal.ministart.model.AppInfo


object AppRepository {

    private var cachedApps: List<AppInfo>? = null

    // A listener so that when the cache changes the observers gets notified
    private val listeners = mutableListOf<WeakReference<() -> Unit>>()

    /**
     * The 'Subscribe' function for the observers
     */
    fun addListener(listener: () -> Unit) {
        listeners.add(listener)
    }

    /**
     * Call this function when we want to let the observers know an update occurred
     */
    private fun notifyChanged() {
        listeners.forEach { it() }
    }

    /**
     * Retrieves the list of cached installed apps on the device, alphabetically ordered
     *
     * @return The list of installed apps alphabetically ordered
     */
    fun getApps(context: Context): List<AppInfo> {
        if (cachedApps == null) {
            cachedApps = loadApps(context)
        }
        return cachedApps!!
    }

    /**
     * Updates the list of apps saved as an attribute
     * So it basically updates the cached list of apps with the actual installed on the device
     */
    fun refreshApps(context: Context) {
        cachedApps = loadApps(context)
        notifyChanged()  //update for the subscribers
    }

    /**
     * Retrieves the list of installed apps from the device, and saves in an attribute acting as a cache, alphabetically ordered
     */
    private fun loadApps(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .distinctBy {
                "${it.activityInfo.packageName}/${it.activityInfo.name}"
            }
            .map { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                val label = pm.getApplicationLabel(
                    resolveInfo.activityInfo.applicationInfo
                ).toString()

                AppInfo(
                    label = label,
                    packageName = packageName
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    /**
     * Filter a given list of apps by a given query, like a search.
     * Case insensitive
     *
     * @param apps The list of apps to filter
     * @param query The query filter
     * @return The filtered list of apps
     */
    fun filterApps(apps: List<AppInfo>, query: String): List<AppInfo> {
        if (query.isBlank()) {
            return apps
        }
        return apps.filter {
            it.label.contains(query, ignoreCase = true)
        }
    }

}