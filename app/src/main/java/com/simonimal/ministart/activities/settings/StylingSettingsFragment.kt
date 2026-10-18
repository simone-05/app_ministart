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


class StylingFragment : Fragment(R.layout.fragment_styling_settings) {
    private var _binding: FragmentStylingSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPrefs: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentStylingSettingsBinding.bind(view)

        sharedPrefs = requireContext().getSharedPreferences(Prefs.FILE, Context.MODE_PRIVATE)

        setupButtons()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupButtons() {

        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        useWallpapersSwitchSetup()

        // When the button to select dark wallpaper is pressed,
        // starts the system's built-in intent to change wallpaper
        binding.selectSystemWallpaperButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_SET_WALLPAPER))
        }

        activateDarkWallpaperSwitchSetup()

        // If not dark wallpaper selected,
        // the button must be active
        if (!sharedPrefs.getBoolean(Prefs.Wallpaper.IS_DARK_WALLPAPER_SELECTED, false)) {
            selectDarkWallpaperButtonAppearance(true)
        }
        // Starts the selection of the dark wallpaper
        binding.selectDarkWallpaperButton.setOnClickListener {
            openImagePicker()
        }

        textColorRadioGroupSetup()

        // Button to close this Activity
        binding.backButton.setOnClickListener {
            customExitActivity()
        }
    }

    /**
     * Function to setup the switch to use the custom wallpapers
     */
    private fun useWallpapersSwitchSetup() {
        // Set the custom wallpaper switch according to saved prefs
        binding.useWallpapersSwitch.isChecked = sharedPrefs.getBoolean(Prefs.Wallpaper.IS_USING_WALLPAPER, false)
        // And set the other buttons and sections according to its value (for visibility things)
        if (binding.useWallpapersSwitch.isChecked) enableCustomWallpaper() else disableCustomWallpaper()

        // Switch for the custom wallpapers use
        binding.useWallpapersSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) enableCustomWallpaper() else disableCustomWallpaper()
        }
    }

    /**
     * Function to setup the switch to use the dark wallpaper
     */
    private fun activateDarkWallpaperSwitchSetup() {
        // Show the switch if the dark wallpaper has been set
        binding.activateDarkWallpaperSwitch.visibility =
        if (sharedPrefs.getBoolean(Prefs.Wallpaper.IS_DARK_WALLPAPER_SELECTED, false)) {
            View.VISIBLE
        } else {
            View.GONE
        }
        // Set the dark wallpaper use switch according to saved prefs
        binding.activateDarkWallpaperSwitch.isChecked = sharedPrefs.getBoolean(Prefs.Wallpaper.IS_DARK_WALLPAPER_USED, false)

        // Setting activation state of the select dark wallpaper button
        selectDarkWallpaperButtonAppearance(binding.activateDarkWallpaperSwitch.isChecked)

        /* Enables / Disables the dark wallpaper,
        * and sets the shared prefs flag accordingly.
        * Indipendently if it has been previously selected or not*/
        binding.activateDarkWallpaperSwitch.setOnCheckedChangeListener { _, isChecked ->
            selectDarkWallpaperButtonAppearance(isChecked)
            // Save the flag in the shared prefs
            sharedPrefs.edit().putBoolean(Prefs.Wallpaper.IS_DARK_WALLPAPER_USED, isChecked).apply()
        }
    }

    /**
     * Function to setup the settings for the text color
     */
    private fun textColorRadioGroupSetup() {
        // Set the text color radio button according to the saved prefs, when first opening this Activity
        binding.textColorRadioGroup.check(
            if (sharedPrefs.getBoolean(Prefs.Text.IS_DARK, false)) {
                binding.darkTextRadioButton.id
            } else {
                binding.lightTextRadioButton.id
            }
        )

        // Puts the text color flag in the shared prefs according to the text color radio button
        binding.textColorRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                binding.lightTextRadioButton.id -> {
                    sharedPrefs.edit().putBoolean(Prefs.Text.IS_DARK, false).apply()
                    setResult(RESULT_OK, Intent().apply{
                        putExtra("text_color_setting_changed", true)
                    })
                }
                binding.darkTextRadioButton.id -> {
                    sharedPrefs.edit().putBoolean(Prefs.Text.IS_DARK, true).apply()
                    setResult(RESULT_OK, Intent().apply{
                        putExtra("text_color_setting_changed", true)
                    })
                }
                else -> { }
            }
        }
    }

    /**
     * Run when the custom wallpaper's switch is activated
     * shows the wallpapers settings and text color,
     * sets their appearance and sets the shared prefs wallpaper flags
     */
    private fun enableCustomWallpaper() {
        sharedPrefs.edit().putBoolean(Prefs.Wallpaper.IS_USING_WALLPAPER, true).apply()
        setResult(RESULT_OK, Intent().apply{
            putExtra("wallpaper_setting_changed", true)
        })

        binding.textColorLayout.visibility = View.VISIBLE
        binding.selectSystemWallpaperButton.visibility = View.VISIBLE
        binding.selectDarkWallpaperButton.visibility = View.VISIBLE
        binding.darkWallpaperLayout.visibility = View.VISIBLE

        val isDarkWallpaperSelected = sharedPrefs.getBoolean(Prefs.Wallpaper.IS_DARK_WALLPAPER_SELECTED, false)

        if (isDarkWallpaperSelected) {
            // Dark wallpaper selected
            updateWallpaperHelperText("Disable the switch to set a white/black background")
            updateDarkWallpaperButton(true)
        } else {
            // Just system wallpaper active
            updateWallpaperHelperText("Disable the switch to set a white/black background, or set a dark mode wallpaper to automatically switch according to system's dark theme")
            updateDarkWallpaperButton(false)
        }
    }

    /**
     * Run when the custom wallpapers in use switch is disabled,
     * to hide the wallpaper and texts settings,
     * and set the shared prefs flag
     */
    private fun disableCustomWallpaper() {
        updateWallpaperHelperText("To change wallpaper activate the switch")
        binding.selectSystemWallpaperButton.visibility = View.GONE
        binding.selectDarkWallpaperButton.visibility = View.GONE
        binding.textColorLayout.visibility = View.GONE
        binding.darkWallpaperLayout.visibility = View.GONE

        sharedPrefs.edit().putBoolean(Prefs.Wallpaper.IS_USING_WALLPAPER, false).apply()
        setResult(RESULT_OK, Intent().apply{
            putExtra("wallpaper_setting_changed", true)
        })
    }

    /**
     * To select the custom wallpaper to use in dark mode
     */
    private fun openImagePicker() {
        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e("SettingsActivity Error", "Dark wallpaper selection failed", e)
            updateDarkWallpaperButton(false)
        }
    }

    private fun startCrop(sourceUri: Uri) {
        try {
            // Create a temporary file for the cropped output
            val destFile = File(cacheDir, "cropped_temp.jpg")
            val destUri = Uri.fromFile(destFile)

            // Use ACTION_CROP intent
            val cropIntent = Intent("com.android.camera.action.CROP")
            cropIntent.setDataAndType(sourceUri, "image/*")

            // Grant permissions
            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

            // Set crop parameters
            cropIntent.putExtra("crop", "true")
            cropIntent.putExtra("aspectX", 9)
            cropIntent.putExtra("aspectY", 16)
            cropIntent.putExtra("outputX", 1080)
            cropIntent.putExtra("outputY", 1920)
            cropIntent.putExtra("scale", true)
            cropIntent.putExtra("return-data", false)
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, destUri)
            cropIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())

            // Check if crop activity exists
            if (cropIntent.resolveActivity(requireContext().packageManager) != null) {
                // This call fails, and we go to the catch
                cropLauncher.launch(cropIntent)
            } else {
                // No crop activity available, save directly
                saveWallpaperImage(sourceUri)
            }
        } catch (e: Exception) {
            // It goes here
            e.printStackTrace()
            // Fallback: save without cropping
            saveWallpaperImage(sourceUri)
        }
    }

    private fun saveWallpaperImage(sourceUri: Uri) {
        // First check if we have a cropped temp file
        val croppedFile = File(cacheDir, "cropped_temp.jpg")
        val bitmap = if (croppedFile.exists() && croppedFile.length() > 0) {
            // Use the cropped image
            BitmapFactory.decodeFile(croppedFile.absolutePath)
        } else {
            // Use the original image
            val inputStream = contentResolver.openInputStream(sourceUri)
            val bmp = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            bmp
        }

        if (bitmap != null) {
            // Save bitmap to internal storage
            val filename = "wallpaper_dark.jpg"

            val file = File(requireContext().filesDir, filename)
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()

            // Clean up temp file
            if (croppedFile.exists()) {
                croppedFile.delete()
            }

            // Save preferences
            val editor = sharedPrefs.edit()

            editor.putBoolean(Prefs.Wallpaper.IS_DARK_WALLPAPER_SELECTED, true)
            editor.putString(Prefs.Wallpaper.DARK_WALLPAPER_PATH, file.absolutePath)
            binding.activateDarkWallpaperSwitch.visibility = View.VISIBLE
            binding.activateDarkWallpaperSwitch.isChecked = true
            updateDarkWallpaperButton(true)
            Toast.makeText(requireContext(), "Change Dark Wallpaper", Toast.LENGTH_SHORT).show()

            editor.apply()
        } else {
            Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Changes the appearance of the select dark wallpaper button,
     * enables/disables state and style
     */
    private fun selectDarkWallpaperButtonAppearance(isActive: Boolean) {
        // Change the enabled state of the select dark wallpaper button
        binding.selectDarkWallpaperButton.isEnabled = isActive

        if (isActive) {
            binding.selectDarkWallpaperButton.alpha = 1f
        } else {
            binding.selectDarkWallpaperButton.alpha = .5f
        }
    }

    private fun updateWallpaperHelperText(text: String) {
        binding.wallpaperHelperText.text = text
    }

    /**
     * Updates the text and color of the dark wallpaper button based on mode and selection state.
     *
     * @param isWallpaperSelected true if a wallpaper is selected, false otherwise
     */
    private fun updateDarkWallpaperButton(isWallpaperSelected: Boolean) {
        if (isWallpaperSelected) {
            binding.selectDarkWallpaperButton.text = "Change Dark Wallpaper"

            val typedValue = TypedValue()
            theme.resolveAttribute(
                android.R.attr.colorPrimary,
                typedValue,
                true
            )
            val colorPrimary = typedValue.data

            binding.selectDarkWallpaperButton.setBackgroundColor(colorPrimary)
        } else {
            binding.selectDarkWallpaperButton.text = "No Dark Wallpaper"
            binding.selectDarkWallpaperButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red))
        }
    }

}

//     private var pendingImageUri: Uri? = null

//     private lateinit var sharedPrefs: SharedPreferences
//     private lateinit var binding: ActivitySettingsBinding

//     private val imagePickerLauncher = registerForActivityResult(
//         ActivityResultContracts.StartActivityForResult()
//     ) { result ->
//         if (result.resultCode == Activity.RESULT_OK) {
//             result.data?.data?.let { uri ->
//                 pendingImageUri = uri
//                 startCrop(uri)
//             }
//         }
//     }

//     private val cropLauncher = registerForActivityResult(
//         ActivityResultContracts.StartActivityForResult()
//     ) { result ->
//         if (result.resultCode == Activity.RESULT_OK) {
//             // Crop was successful, save the original URI
//             pendingImageUri?.let { uri ->
//                 saveWallpaperImage(uri)
//             }
//         } else {
//             // Crop cancelled or failed, still save the image without crop
//             pendingImageUri?.let { uri ->
//                 saveWallpaperImage(uri)
//             }
//         }
//     }

//     override fun onCreate(savedInstanceState: Bundle?) {
//         super.onCreate(savedInstanceState)
//         binding = ActivitySettingsBinding.inflate(layoutInflater)
//         setContentView(binding.root)

//         // Extend layout content behind the status bar
//         WindowCompat.setDecorFitsSystemWindows(window, false)
//         // Make status bar and navbar transparent
//         setStatusBarBackground(Color.TRANSPARENT)
//         setNavBarBackground(Color.TRANSPARENT)
//         // Set status bar text color according to system's dark/light theme
//         setStatusBarTextColorBlack(!((resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES))
//         // Set navigation bar text color according to system's dark/light theme
//         setNavBarTextColorBlack(!((resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES))

//         sharedPrefs = getSharedPreferences(Prefs.FILE, MODE_PRIVATE)

//         // Instantiating the layout components:

//         /* Fade in the background of the activity
//         Since the window's background is transparent,
//         on activity change we would have a small flicker of background
//         */
//         binding.settingsRootLayout.post {
//             binding.settingsRootLayout.animate()
//                 .alpha(1f)
//                 .setDuration(500)
//                 .start()
//         }

//         setupButtons()
//     }

//     /**
//      * Function to group the setting of the behaviour
//      * of the layout components of this activity:
//      * button clicks, switches, radios etc...
//      */
//     private fun setupButtons() {
//         useWallpapersSwitchSetup()

//         // When the button to select dark wallpaper is pressed,
//         // starts the system's built-in intent to change wallpaper
//         binding.selectSystemWallpaperButton.setOnClickListener {
//             startActivity(Intent(Intent.ACTION_SET_WALLPAPER))
//         }

//         activateDarkWallpaperSwitchSetup()

//         // If not dark wallpaper selected,
//         // the button must be active
//         if (!sharedPrefs.getBoolean(Prefs.Wallpaper.IS_DARK_WALLPAPER_SELECTED, false)) {
//             selectDarkWallpaperButtonAppearance(true)
//         }
//         // Starts the selection of the dark wallpaper
//         binding.selectDarkWallpaperButton.setOnClickListener {
//             openImagePicker()
//         }

//         textColorRadioGroupSetup()

//         // Button to close this Activity
//         binding.backButton.setOnClickListener {
//             customExitActivity()
//         }

//         // Handling the phone's back button press to close this Activity
//         onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
//             override fun handleOnBackPressed() {
//                 customExitActivity()
//             }
//         })
//     }

//     /**
//      * Function to setup the switch to use the custom wallpapers
//      */
//     private fun useWallpapersSwitchSetup() {
//         // Set the custom wallpaper switch according to saved prefs
//         binding.useWallpapersSwitch.isChecked = sharedPrefs.getBoolean(Prefs.Wallpaper.IS_USING_WALLPAPER, false)
//         // And set the other buttons and sections according to its value (for visibility things)
//         if (binding.useWallpapersSwitch.isChecked) enableCustomWallpaper() else disableCustomWallpaper()

//         // Switch for the custom wallpapers use
//         binding.useWallpapersSwitch.setOnCheckedChangeListener { _, isChecked ->
//             if (isChecked) enableCustomWallpaper() else disableCustomWallpaper()
//         }
//     }

//     /**
//      * Function to setup the switch to use the dark wallpaper
//      */
//     private fun activateDarkWallpaperSwitchSetup() {
//         // Show the switch if the dark wallpaper has been set
//         binding.activateDarkWallpaperSwitch.visibility =
//         if (sharedPrefs.getBoolean(Prefs.Wallpaper.IS_DARK_WALLPAPER_SELECTED, false)) {
//             View.VISIBLE
//         } else {
//             View.GONE
//         }
//         // Set the dark wallpaper use switch according to saved prefs
//         binding.activateDarkWallpaperSwitch.isChecked = sharedPrefs.getBoolean(Prefs.Wallpaper.IS_DARK_WALLPAPER_USED, false)

//         // Setting activation state of the select dark wallpaper button
//         selectDarkWallpaperButtonAppearance(binding.activateDarkWallpaperSwitch.isChecked)

//         /* Enables / Disables the dark wallpaper,
//         * and sets the shared prefs flag accordingly.
//         * Indipendently if it has been previously selected or not*/
//         binding.activateDarkWallpaperSwitch.setOnCheckedChangeListener { _, isChecked ->
//             selectDarkWallpaperButtonAppearance(isChecked)
//             // Save the flag in the shared prefs
//             sharedPrefs.edit().putBoolean(Prefs.Wallpaper.IS_DARK_WALLPAPER_USED, isChecked).apply()
//         }
//     }

//     /**
//      * Function to setup the settings for the text color
//      */
//     private fun textColorRadioGroupSetup() {
//         // Set the text color radio button according to the saved prefs, when first opening this Activity
//         binding.textColorRadioGroup.check(
//             if (sharedPrefs.getBoolean(Prefs.Text.IS_DARK, false)) {
//                 binding.darkTextRadioButton.id
//             } else {
//                 binding.lightTextRadioButton.id
//             }
//         )

//         // Puts the text color flag in the shared prefs according to the text color radio button
//         binding.textColorRadioGroup.setOnCheckedChangeListener { group, checkedId ->
//             when (checkedId) {
//                 binding.lightTextRadioButton.id -> {
//                     sharedPrefs.edit().putBoolean(Prefs.Text.IS_DARK, false).apply()
//                     setResult(RESULT_OK, Intent().apply{
//                         putExtra("text_color_setting_changed", true)
//                     })
//                 }
//                 binding.darkTextRadioButton.id -> {
//                     sharedPrefs.edit().putBoolean(Prefs.Text.IS_DARK, true).apply()
//                     setResult(RESULT_OK, Intent().apply{
//                         putExtra("text_color_setting_changed", true)
//                     })
//                 }
//                 else -> { }
//             }
//         }
//     }

//     /**
//      * Run when the custom wallpaper's switch is activated
//      * shows the wallpapers settings and text color,
//      * sets their appearance and sets the shared prefs wallpaper flags
//      */
//     private fun enableCustomWallpaper() {
//         sharedPrefs.edit().putBoolean(Prefs.Wallpaper.IS_USING_WALLPAPER, true).apply()
//         setResult(RESULT_OK, Intent().apply{
//             putExtra("wallpaper_setting_changed", true)
//         })

//         binding.textColorLayout.visibility = View.VISIBLE
//         binding.selectSystemWallpaperButton.visibility = View.VISIBLE
//         binding.selectDarkWallpaperButton.visibility = View.VISIBLE
//         binding.darkWallpaperLayout.visibility = View.VISIBLE

//         val isDarkWallpaperSelected = sharedPrefs.getBoolean(Prefs.Wallpaper.IS_DARK_WALLPAPER_SELECTED, false)

//         if (isDarkWallpaperSelected) {
//             // Dark wallpaper selected
//             updateWallpaperHelperText("Disable the switch to set a white/black background")
//             updateDarkWallpaperButton(true)
//         } else {
//             // Just system wallpaper active
//             updateWallpaperHelperText("Disable the switch to set a white/black background, or set a dark mode wallpaper to automatically switch according to system's dark theme")
//             updateDarkWallpaperButton(false)
//         }
//     }

//     /**
//      * Run when the custom wallpapers in use switch is disabled,
//      * to hide the wallpaper and texts settings,
//      * and set the shared prefs flag
//      */
//     private fun disableCustomWallpaper() {
//         updateWallpaperHelperText("To change wallpaper activate the switch")
//         binding.selectSystemWallpaperButton.visibility = View.GONE
//         binding.selectDarkWallpaperButton.visibility = View.GONE
//         binding.textColorLayout.visibility = View.GONE
//         binding.darkWallpaperLayout.visibility = View.GONE

//         sharedPrefs.edit().putBoolean(Prefs.Wallpaper.IS_USING_WALLPAPER, false).apply()
//         setResult(RESULT_OK, Intent().apply{
//             putExtra("wallpaper_setting_changed", true)
//         })
//     }

//     /**
//      * To select the custom wallpaper to use in dark mode
//      */
//     private fun openImagePicker() {
//         try {
//             val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//             imagePickerLauncher.launch(intent)
//         } catch (e: Exception) {
//             Log.e("SettingsActivity Error", "Dark wallpaper selection failed", e)
//             updateDarkWallpaperButton(false)
//         }
//     }

//     private fun startCrop(sourceUri: Uri) {
//         try {
//             // Create a temporary file for the cropped output
//             val destFile = File(cacheDir, "cropped_temp.jpg")
//             val destUri = Uri.fromFile(destFile)

//             // Use ACTION_CROP intent
//             val cropIntent = Intent("com.android.camera.action.CROP")
//             cropIntent.setDataAndType(sourceUri, "image/*")

//             // Grant permissions
//             cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//             cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

//             // Set crop parameters
//             cropIntent.putExtra("crop", "true")
//             cropIntent.putExtra("aspectX", 9)
//             cropIntent.putExtra("aspectY", 16)
//             cropIntent.putExtra("outputX", 1080)
//             cropIntent.putExtra("outputY", 1920)
//             cropIntent.putExtra("scale", true)
//             cropIntent.putExtra("return-data", false)
//             cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, destUri)
//             cropIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())

//             // Check if crop activity exists
//             if (cropIntent.resolveActivity(packageManager) != null) {
//                 // This call fails, and we go to the catch
//                 cropLauncher.launch(cropIntent)
//             } else {
//                 // No crop activity available, save directly
//                 saveWallpaperImage(sourceUri)
//             }
//         } catch (e: Exception) {
//             // It goes here
//             e.printStackTrace()
//             // Fallback: save without cropping
//             saveWallpaperImage(sourceUri)
//         }
//     }

//     private fun saveWallpaperImage(sourceUri: Uri) {
//         // First check if we have a cropped temp file
//         val croppedFile = File(cacheDir, "cropped_temp.jpg")
//         val bitmap = if (croppedFile.exists() && croppedFile.length() > 0) {
//             // Use the cropped image
//             BitmapFactory.decodeFile(croppedFile.absolutePath)
//         } else {
//             // Use the original image
//             val inputStream = contentResolver.openInputStream(sourceUri)
//             val bmp = BitmapFactory.decodeStream(inputStream)
//             inputStream?.close()
//             bmp
//         }

//         if (bitmap != null) {
//             // Save bitmap to internal storage
//             val filename = "wallpaper_dark.jpg"

//             val file = File(filesDir, filename)
//             val outputStream = FileOutputStream(file)
//             bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
//             outputStream.flush()
//             outputStream.close()

//             // Clean up temp file
//             if (croppedFile.exists()) {
//                 croppedFile.delete()
//             }

//             // Save preferences
//             val editor = sharedPrefs.edit()

//             editor.putBoolean(Prefs.Wallpaper.IS_DARK_WALLPAPER_SELECTED, true)
//             editor.putString(Prefs.Wallpaper.DARK_WALLPAPER_PATH, file.absolutePath)
//             binding.activateDarkWallpaperSwitch.visibility = View.VISIBLE
//             binding.activateDarkWallpaperSwitch.isChecked = true
//             updateDarkWallpaperButton(true)
//             Toast.makeText(this, "Change Dark Wallpaper", Toast.LENGTH_SHORT).show()

//             editor.apply()
//         } else {
//             Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
//         }
//     }

//     /**
//      * Changes the appearance of the select dark wallpaper button,
//      * enables/disables state and style
//      */
//     private fun selectDarkWallpaperButtonAppearance(isActive: Boolean) {
//         // Change the enabled state of the select dark wallpaper button
//         binding.selectDarkWallpaperButton.isEnabled = isActive

//         if (isActive) {
//             binding.selectDarkWallpaperButton.alpha = 1f
//         } else {
//             binding.selectDarkWallpaperButton.alpha = .5f
//         }
//     }

//     private fun updateWallpaperHelperText(text: String) {
//         binding.wallpaperHelperText.text = text
//     }

//     /**
//      * Updates the text and color of the dark wallpaper button based on mode and selection state.
//      *
//      * @param isWallpaperSelected true if a wallpaper is selected, false otherwise
//      */
//     private fun updateDarkWallpaperButton(isWallpaperSelected: Boolean) {
//         if (isWallpaperSelected) {
//             binding.selectDarkWallpaperButton.text = "Change Dark Wallpaper"

//             val typedValue = TypedValue()
//             theme.resolveAttribute(
//                 android.R.attr.colorPrimary,
//                 typedValue,
//                 true
//             )
//             val colorPrimary = typedValue.data

//             binding.selectDarkWallpaperButton.setBackgroundColor(colorPrimary)
//         } else {
//             binding.selectDarkWallpaperButton.text = "No Dark Wallpaper"
//             binding.selectDarkWallpaperButton.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
//         }
//     }

//     private fun customExitActivity() {
//         binding.settingsRootLayout.animate()
//             .alpha(0f)
//             .setDuration(100)
//             .withEndAction {
//                 finish()
//             }
//             .start()
//     }
// }