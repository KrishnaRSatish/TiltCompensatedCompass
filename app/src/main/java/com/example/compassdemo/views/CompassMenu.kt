package com.example.compassdemo.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.compassdemo.R
import com.example.compassdemo.decorations.CustomBottomSheetDialogFragment
import com.example.compassdemo.decorations.ripple.DynamicRippleLinearLayout
import com.example.compassdemo.decorations.switchview.SwitchView
import com.example.compassdemo.prefrences.CompassPreferences
import com.example.compassdemo.prefrences.CompassPreferences.setDirectionCode

class CompassMenu : CustomBottomSheetDialogFragment() {

    private lateinit var toggleCode: SwitchView
    private lateinit var codeSwitchContainer: DynamicRippleLinearLayout
    private lateinit var toggleGimbalLock: SwitchView
    private lateinit var gimbalLockSwitchContainer: DynamicRippleLinearLayout
    private lateinit var rotationVectorSwitchContainer: DynamicRippleLinearLayout
    private lateinit var toggleRotationVectorSwitch: SwitchView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.compass_menu, container, false)
        toggleCode = view.findViewById(R.id.toggle_code)
        toggleGimbalLock = view.findViewById(R.id.toggle_gimbal_lock)
        toggleRotationVectorSwitch = view.findViewById(R.id.toggle_rotation_vector)
        codeSwitchContainer = view.findViewById(R.id.compass_menu_show_code)
        gimbalLockSwitchContainer = view.findViewById(R.id.compass_menu_gimbal_lock)
        rotationVectorSwitchContainer = view.findViewById(R.id.compass_menu_rotation_vector)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toggleCode.isChecked = CompassPreferences.getDirectionCode()
        toggleGimbalLock.isChecked = CompassPreferences.isUsingGimbalLock()

        if (CompassPreferences.getVectorSensorUsed()) {
            toggleRotationVectorSwitch.isChecked = true
            toggleGimbalLock.setSwitchEnabled(false)
            toggleGimbalLock.isEnabled = false
            gimbalLockSwitchContainer.setLayoutEnabled(false)
        }else{
            toggleRotationVectorSwitch.isChecked = false
        }

        codeSwitchContainer.setOnClickListener {
            toggleCode.isChecked = !toggleCode.isChecked
        }

        toggleCode.setOnCheckedChangeListener { isChecked: Boolean ->
            setDirectionCode(isChecked)
        }

        toggleGimbalLock.setOnCheckedChangeListener {
            CompassPreferences.setUseGimbalLock(it)
        }

        toggleRotationVectorSwitch.setOnCheckedChangeListener {
            CompassPreferences.setVectorSensorUsed(it)
            toggleGimbalLock.setSwitchEnabled(!it)
            toggleGimbalLock.isEnabled = !it
            gimbalLockSwitchContainer.setLayoutEnabled(!it)
        }

        gimbalLockSwitchContainer.setOnClickListener {
            toggleGimbalLock.isChecked = !toggleGimbalLock.isChecked
        }

        rotationVectorSwitchContainer.setOnClickListener {
            toggleRotationVectorSwitch.isChecked = !toggleRotationVectorSwitch.isChecked
        }
    }

}