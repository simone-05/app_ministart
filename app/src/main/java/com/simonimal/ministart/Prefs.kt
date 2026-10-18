/**
 * Central registry for all SharedPreferences keys used in the app.
 * Includes documentation, type, and default values.
 */
object Prefs {

    const val FILE = "launcher_prefs"

    object Text {
        /** True if the text color selected is dark from the settings. */
        const val IS_DARK = "is_dark"   // Boolean, default = false
    }

    object Apps {
        /** Ordered list of apps' package names to show in the home screen */
        const val HOME_APPS_LIST = "home_apps_list"  // JSON string
    }

    object Wallpaper {
        /** True if the switch to use the custom wallpaper/s is on. */
        const val IS_USING_WALLPAPER = "is_using_wallpaper"   // Boolean, default = false

        /** True if a dark wallpaper has been selected. */
        const val IS_DARK_WALLPAPER_SELECTED = "is_dark_wallpaper_selected"   // Boolean, default = false

        /** True if the dark wallpaper is used. */
        const val IS_DARK_WALLPAPER_USED = "is_dark_wallpaper_used"   // Boolean, default = false

        /** The path where the dark wallpaper is saved. */
        const val DARK_WALLPAPER_PATH = "dark_wallpaper_path"   // String, default = ""

    }
}
