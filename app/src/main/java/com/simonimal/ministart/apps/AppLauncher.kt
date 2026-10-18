package com.simonimal.ministart.apps

import android.content.Context


class AppLauncher(
    private val context: Context
) {

    /**
     * Function to launch an app from its package name
     *
     * @param packageName The name of the package
     * @return True if launched successfully, False if failed
     * @throws Exception If there was an error while launching
     */
    fun launch(packageName: String): Boolean {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}