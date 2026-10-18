/**
 * Files to group hard coded constants shared across the app
 */
object Constants {

    /** These values are used for app drawer gestures and
     * left/right swipes for shortcut apps in the home. */

    /** Delta distance threshold of the finger swipe for the appdrawer,
     * expressed in dp, will be converted to float in the code
    */
    const val SWIPE_DISTANCE_THRESHOLD = 130

    const val SWIPE_VELOCITY_THRESHOLD = 2_000f

    const val VELOCITY_DIR_MULTIP = 2f



    const val APPDRAWER_ANIMATION_DURATION: Long = 300  // milliseconds

    const val CLOCK_UPDATE_INTERVAL: Long = 30_000  // milliseconds
}