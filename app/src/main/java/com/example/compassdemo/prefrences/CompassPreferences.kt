package com.example.compassdemo.prefrences

import com.example.compassdemo.prefrences.SharedPreferences.getSharedPreferences
import com.example.compassdemo.views.PhysicalRotationImageView


/**
 * Only preference related to Compass
 */
object CompassPreferences {

    const val direction_code = "direction_code"
    const val flowerBloom = "flower"
    const val flowerBloomTheme = "flower_theme"
    const val dampingCoefficient = "damping_coefficient"
    const val magneticCoefficient = "magnetic_coefficient"
    const val rotationalInertia = "rotational_inertia"
    const val usePhysicalProperties = "use_physical_properties"
    const val useGimbalLock = "use_gimbal_lock"
    const val useRotationVector = "use_rotation_vector"
    const val logEnabled = "log_enabled"

    //--------------------------------------------------------------------------------------------------//

    fun setDirectionCode(value: Boolean) {
        getSharedPreferences().edit().putBoolean(direction_code, value).apply()
    }

    fun getDirectionCode(): Boolean {
        return getSharedPreferences().getBoolean(direction_code, true)
    }

    //--------------------------------------------------------------------------------------------------//

 fun setVectorSensorUsed(value: Boolean){
     getSharedPreferences().edit().putBoolean(useRotationVector , value).apply()
 }


fun getVectorSensorUsed():Boolean{
    return getSharedPreferences().getBoolean(useRotationVector,false)
}
    //--------------------------------------------------------------------------------------------------//



    //--------------------------------------------------------------------------------------------------//

    fun setDampingCoefficient(value: Float) {
        getSharedPreferences().edit().putFloat(dampingCoefficient, value).apply()
    }

    fun getDampingCoefficient(): Float {
        return getSharedPreferences().getFloat(dampingCoefficient, PhysicalRotationImageView.ALPHA_DEFAULT)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setRotationalInertia(value: Float) {
        getSharedPreferences().edit().putFloat(rotationalInertia, value).apply()
    }

    fun getRotationalInertia(): Float {
        return getSharedPreferences().getFloat(rotationalInertia, PhysicalRotationImageView.INERTIA_MOMENT_DEFAULT)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setMagneticCoefficient(value: Float) {
        getSharedPreferences().edit().putFloat(magneticCoefficient, value).apply()
    }

    fun getMagneticCoefficient(): Float {
        return getSharedPreferences().getFloat(magneticCoefficient, PhysicalRotationImageView.MB_DEFAULT)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setUsePhysicalProperties(value: Boolean) {
        getSharedPreferences().edit().putBoolean(usePhysicalProperties, value).apply()
    }

    fun isUsingPhysicalProperties(): Boolean {
        return getSharedPreferences().getBoolean(usePhysicalProperties, true)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setUseGimbalLock(value: Boolean) {
        getSharedPreferences().edit().putBoolean(useGimbalLock, value).apply()
    }

    fun isUsingGimbalLock(): Boolean {
        return getSharedPreferences().getBoolean(useGimbalLock, false)
    }

   // --------------------------------------------------------------------------------------------//

    fun setLogEnabled(value : Boolean) {
        getSharedPreferences().edit().putBoolean(logEnabled,value).apply()
    }

    fun getLogEnabled():Boolean{
        return getSharedPreferences().getBoolean(logEnabled, false)
    }

}
