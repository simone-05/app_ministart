package com.simonimal.ministart.activities.settings

import android.widget.Toast


class SettingsMainFragment : Fragment(R.layout.fragment_settings_main) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val stylingButton = view.findViewById<MaterialButton>(R.id.stylingButton)

        val homeAppsButton = view.findViewById<MaterialButton>(R.id.homeAppsButton)

        stylingButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.settingsContainer,
                    StylingSettingsFragment()
                )
                .addToBackStack(null)
                .commit()
        }

        homeAppsButton.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Home Apps not implemented yet",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}