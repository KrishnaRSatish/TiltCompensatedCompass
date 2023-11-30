package com.example.compassdemo

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import app.simple.positional.math.Angle.normalizeEulerAngle
import app.simple.positional.math.CompassAzimuth
import app.simple.positional.math.Vector3
import com.example.compassdemo.prefrences.CompassPreferences
import com.example.compassdemo.prefrences.SharedPreferences
import com.example.compassdemo.utils.Direction.getDirectionCodeFromAzimuth
import com.example.compassdemo.utils.Direction.getDirectionNameFromAzimuth
import com.example.compassdemo.utils.ImageLoader.loadImage
import com.example.compassdemo.utils.LocaleHelper
import com.example.compassdemo.decorations.DynamicRippleImageButton
import com.example.compassdemo.decorations.ripple.PhysicalRotationImageView
import com.example.compassdemo.views.CompassMenu
import java.lang.Math.abs

class MainActivity : AppCompatActivity() , SensorEventListener , android.content.SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var dial: PhysicalRotationImageView
    private lateinit var degrees: TextView
    private lateinit var deviceHeadImage : ImageView
    private lateinit var direction : TextView
    private lateinit var menu: DynamicRippleImageButton

    private val accelerometerReadings = FloatArray(3)
    private val magnetometerReadings = FloatArray(3)
    private val rotation = FloatArray(9)
    private val inclination = FloatArray(9)

    private var haveAccelerometerSensor = false
    private var haveMagnetometerSensor = false
    private var showDirectionCode = true
    private var isUserRotatingDial = false
    private var isAnimated = true
    private var isGimbalLock = false

    private var accelerometer = Vector3.zero
    private var magnetometer = Vector3.zero

    private var readingsAlpha = 0.03f
    private var rotationAngle = 0f
    private var flowerBloom = 0
    private var lastDialAngle = 0F
    private var startAngle = 0F
    private val degreesPerRadian = 180 / Math.PI
    private val twoTimesPi = 2.0 * Math.PI


    private lateinit var sensorManager: SensorManager
    private lateinit var sensorAccelerometer: Sensor
    private lateinit var sensorMagneticField: Sensor


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        SharedPreferences.init(this)
        SharedPreferences.getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
        dial = findViewById(R.id.dial)
        degrees = findViewById(R.id.degrees)
        deviceHeadImage = findViewById(R.id.deviceHead)
        direction = findViewById(R.id.direction)
        menu = findViewById(R.id.rippleButton)
        loadImage(R.drawable.compass_dial, dial, this, 0)

        initializeListeners()

        //Register Sensors
        sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        kotlin.runCatching {
            sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)!!
            sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
            haveMagnetometerSensor = true
            haveAccelerometerSensor = true
        }.getOrElse {
            haveAccelerometerSensor = false
            haveMagnetometerSensor = false


            Toast.makeText(this , "Snsor Error" , Toast.LENGTH_LONG).show()
        }

        isAnimated = CompassPreferences.isUsingPhysicalProperties()
        setPhysicalProperties()


    }

    override fun onResume() {
        super.onResume()
        register()

    }

    override fun onPause() {
        super.onPause()
        dial.clearAnimation()
        unregister()
    }

    override fun onDestroy() {
        super.onDestroy()
        SharedPreferences.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: android.content.SharedPreferences?, key: String?) {
        when (key) {
            CompassPreferences.direction_code -> {
                showDirectionCode = CompassPreferences.getDirectionCode()
            }
            CompassPreferences.useGimbalLock -> {
                isGimbalLock = CompassPreferences.isUsingGimbalLock()
            }
        }
    }




    private fun register() {
        if (haveAccelerometerSensor && haveMagnetometerSensor) {
            sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME)
            sensorManager.registerListener(this, sensorMagneticField, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    private fun unregister() {
        if (haveAccelerometerSensor && haveMagnetometerSensor) {
            sensorManager.unregisterListener(this, sensorAccelerometer)
            sensorManager.unregisterListener(this, sensorMagneticField)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                LowPassFilter.smoothAndSetReadings(
                    accelerometerReadings,
                    event.values,
                    readingsAlpha
                )
                accelerometer = Vector3(accelerometerReadings[0], accelerometerReadings[1], accelerometerReadings[2])
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                LowPassFilter.smoothAndSetReadings(
                    magnetometerReadings,
                    event.values,
                    readingsAlpha
                )
                magnetometer = Vector3(magnetometerReadings[0], magnetometerReadings[1], magnetometerReadings[2])
            }
        }

        val angle = if (isGimbalLock) {
            val successfullyCalculatedRotationMatrix = SensorManager.getRotationMatrix(rotation, inclination, accelerometerReadings, magnetometerReadings)

            if (successfullyCalculatedRotationMatrix) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotation, orientation)
                CompassAzimuth.adjustAzimuthForDisplayRotation(((orientation[0] + twoTimesPi) % twoTimesPi * degreesPerRadian).toFloat(),this.windowManager)
            } else {
                0F
            }
        } else {
            CompassAzimuth.calculate(gravity = accelerometer, magneticField = magnetometer, this.windowManager)
        }
        if (!isUserRotatingDial) {
            rotationAngle = angle.normalizeEulerAngle(false)
            viewRotation(rotationAngle, isAnimated)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(this.attributionTag, "onAccuracyChanged: " )
    }


    private fun viewRotation(rotationAngle: Float, animate: Boolean) {
        dial.rotationUpdate(rotationAngle * -1, animate)

        degrees.text = StringBuilder().append(abs(dial.rotation.normalizeEulerAngle(true).toInt())).append("°")

        direction.text = if (showDirectionCode) {
            getDirectionCodeFromAzimuth(this, azimuth = rotationAngle.toDouble()).uppercase(LocaleHelper.getAppLocale())
        } else {
            getDirectionNameFromAzimuth(this, azimuth = rotationAngle.toDouble()).uppercase(LocaleHelper.getAppLocale())
        }
    }



    private fun setPhysicalProperties() {
        val inertia = CompassPreferences.getRotationalInertia()
        val damping = CompassPreferences.getDampingCoefficient()
        val magnetic = CompassPreferences.getMagneticCoefficient()

        dial.setPhysical(inertia, damping, magnetic)
    }

    fun initializeListeners(){
        menu.setOnClickListener {
           showCompassMenu()
        }
    }


    private fun showCompassMenu() {
        val compassMenu = CompassMenu()
        compassMenu.show(supportFragmentManager, compassMenu.tag)
    }


}