package com.simonimal.ministart.model

/**
 * Custom data structure holding information for an app on the device
 *
 * @property label The title of the app, as it would appear on the list of apps
 * @property packageName Used to launch the app and uniquely identify it
 */
data class AppInfo(
    val label: String,
    val packageName: String
)