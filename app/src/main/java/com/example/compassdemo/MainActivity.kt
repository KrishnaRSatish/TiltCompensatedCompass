package com.example.compassdemo

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
import com.example.compassdemo.views.PhysicalRotationImageView
import com.example.compassdemo.math.LowPassFilter
import com.example.compassdemo.views.CompassMenu
import com.opencsv.CSVWriter
import com.robinhood.ticker.TickerUtils
import com.robinhood.ticker.TickerView
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.lang.Math.abs
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() , SensorEventListener , android.content.SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var dial: PhysicalRotationImageView
    private lateinit var degrees: TextView
    private lateinit var deviceHeadImage : ImageView
    private lateinit var direction : TextView
    private lateinit var menu: DynamicRippleImageButton
    private lateinit var magneticStrengthValueTextView : TickerView
    private lateinit var sensorAccuracyStateTextView : TextView

    private val accelerometerReadings = FloatArray(3)
    private val magnetometerReadings = FloatArray(3)
    private val rotation = FloatArray(9)
    private val inclination = FloatArray(9)

    private var haveAccelerometerSensor = false
    private var haveMagnetometerSensor = false
    private var showDirectionCode = false
    private var isUserRotatingDial = false
    private var isAnimated = true
    private var isGimbalLock = false
    private var isVectorUsed = false
    private var isLogEnabled = false

    private var accelerometer = Vector3.zero
    private var magnetometer = Vector3.zero

    private var readingsAlpha = 0.03f
    private var rotationAngle = 0f
    private val degreesPerRadian = 180 / Math.PI
    private val twoTimesPi = 2.0 * Math.PI
    private var accelerometerAccuracy: Int = SensorManager.SENSOR_STATUS_ACCURACY_HIGH
    private var magneticAccuracy: Int = SensorManager.SENSOR_STATUS_ACCURACY_HIGH


    private lateinit var sensorManager: SensorManager
    private lateinit var sensorAccelerometer: Sensor
    private lateinit var sensorMagneticField: Sensor
    private lateinit var sensorRotationVector: Sensor


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        SharedPreferences.init(this)
        SharedPreferences.getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
        degrees = findViewById(R.id.degrees)
        deviceHeadImage = findViewById(R.id.deviceHead)
        direction = findViewById(R.id.direction)
        menu = findViewById(R.id.rippleButton)
        sensorAccuracyStateTextView = findViewById(R.id.mag_accuracy_value)
        magneticStrengthValueTextView = findViewById(R.id.mag_strength_value)
        magneticStrengthValueTextView.setCharacterLists(TickerUtils.provideNumberList());

        initializeListeners()

        //Register Sensors
        sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        kotlin.runCatching {
            sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)!!
            sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
            sensorRotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)!!
            haveMagnetometerSensor = true
            haveAccelerometerSensor = true
            haveAccelerometerSensor = true
        }.getOrElse {
            haveAccelerometerSensor = false
            haveMagnetometerSensor = false


            Toast.makeText(this , "Snsor Error" , Toast.LENGTH_LONG).show()
        }

        isAnimated = CompassPreferences.isUsingPhysicalProperties()


    }

    override fun onResume() {
        super.onResume()
        register()
        initializePhysicalPropertiesImage()

    }

    override fun onStop() {
        super.onStop()
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
            CompassPreferences.useRotationVector ->{
                isVectorUsed = CompassPreferences.getVectorSensorUsed()
            }

            CompassPreferences.logEnabled ->{
                isLogEnabled = CompassPreferences.getLogEnabled()
                showToastLogStatus(this , isLogEnabled)
            }
        }
    }




    private fun register() {
        if (haveAccelerometerSensor && haveMagnetometerSensor) {
            sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME)
            sensorManager.registerListener(this, sensorMagneticField, SensorManager.SENSOR_DELAY_GAME)
            sensorManager.registerListener(this ,sensorRotationVector , SensorManager.SENSOR_DELAY_GAME )
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
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            if (isVectorUsed) {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                CompassAzimuth.adjustAzimuthForDisplayRotation(
                    ((SensorManager.getOrientation(
                        rotationMatrix,
                        FloatArray(3)
                    )[0] + twoTimesPi) % twoTimesPi * degreesPerRadian).toFloat(),
                    this.windowManager
                )
                val azimuth = ((SensorManager.getOrientation(
                    rotationMatrix,
                    FloatArray(3)
                )[0] + twoTimesPi) % twoTimesPi * degreesPerRadian).toFloat()
                CompassAzimuth.adjustAzimuthForDisplayRotation(azimuth, this.windowManager)
                viewRotation(azimuth.normalizeEulerAngle(false), isAnimated)
            }
        }
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                LowPassFilter.smoothAndSetReadings(
                    accelerometerReadings,
                    event.values,
                    readingsAlpha
                )
                accelerometer = Vector3(
                    accelerometerReadings[0],
                    accelerometerReadings[1],
                    accelerometerReadings[2]
                )
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                LowPassFilter.smoothAndSetReadings(
                    magnetometerReadings,
                    event.values,
                    readingsAlpha
                )
                magnetometer = Vector3(
                    magnetometerReadings[0],
                    magnetometerReadings[1],
                    magnetometerReadings[2]
                )
                val magneticStrength =
                    calculateMagneticStrength(magnetometer.x, magnetometer.y, magnetometer.z)
                updateMagneticStrengthTextView(magneticStrengthValueTextView, magneticStrength)
            }
        }

        if (!isVectorUsed) {
            val angle = if (isGimbalLock) {
                val successfullyCalculatedRotationMatrix = SensorManager.getRotationMatrix(
                    rotation,
                    inclination,
                    accelerometerReadings,
                    magnetometerReadings
                )

                if (successfullyCalculatedRotationMatrix) {
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rotation, orientation)
                    CompassAzimuth.adjustAzimuthForDisplayRotation(
                        ((orientation[0] + twoTimesPi) % twoTimesPi * degreesPerRadian).toFloat(),
                        this.windowManager
                    )
                } else {
                    0F
                }
            } else {
                CompassAzimuth.calculate(
                    gravity = accelerometer,
                    magneticField = magnetometer,
                    this.windowManager
                )
            }
            if (!isUserRotatingDial) {
                rotationAngle = angle.normalizeEulerAngle(false)
                viewRotation(rotationAngle, isAnimated)
                if(isLogEnabled) {
                    writeSensorDataToCSV(event, rotationAngle)
                }
            }
        }

    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        when (sensor?.type) {
            Sensor.TYPE_ACCELEROMETER -> accelerometerAccuracy = accuracy
            Sensor.TYPE_MAGNETIC_FIELD -> magneticAccuracy = accuracy
        }

        updateAccuracyTextView(sensorAccuracyStateTextView, accelerometerAccuracy, magneticAccuracy)
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

    private fun initializeListeners(){
        menu.setOnClickListener {
           showCompassMenu()
        }
    }


    private fun showCompassMenu() {
        val compassMenu = CompassMenu()
        compassMenu.show(supportFragmentManager, compassMenu.tag)
    }


    private fun initializePhysicalPropertiesImage() {
        dial = findViewById(R.id.dial)

        if (dial.drawable == null) {
             loadImage(R.drawable.compass_dial, dial, this, 0)
            setPhysicalProperties()
        }

    }

    private fun calculateMagneticStrength(x: Float, y: Float, z: Float): Float {
        return kotlin.math.sqrt(x * x + y * y + z * z)
    }

    private  fun updateMagneticStrengthTextView(strengthValueTv: TickerView, magneticStrength: Float) {
        // Update the TextView with the magnetic strength
        strengthValueTv.text = String.format("%.0f µT", magneticStrength)

        // Define the ranges for color coding
        val bestRange = Pair(25.0, 65.0)
        val moderateRange = Pair(20.0, 80.0)

        // Set text color based on the magnetic strength range
        val textColor = when (magneticStrength) {
            in bestRange.first..bestRange.second -> this.getColor(R.color.colorAccent)
            in moderateRange.first..moderateRange.second -> this.getColor(R.color.speedometer_needle_end)
            else -> this.getColor(R.color.speedometer_needle_start)
        }

        strengthValueTv.textColor = textColor
    }


    private fun updateAccuracyTextView(accuracyTV: TextView, accelerometerAccuracy: Int, magneticAccuracy: Int) {
        // Choose the lower accuracy level between accelerometer and magnetic sensor
        val overallAccuracy = when {
            accelerometerAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE || magneticAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                // If either sensor is unreliable, set overall accuracy to low
                SensorManager.SENSOR_STATUS_UNRELIABLE
            }
            accelerometerAccuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW || magneticAccuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                // If either sensor has low accuracy, set overall accuracy to low
                SensorManager.SENSOR_STATUS_ACCURACY_LOW
            }
            accelerometerAccuracy == SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM || magneticAccuracy == SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> {
                // If either sensor has medium accuracy, set overall accuracy to medium
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM
            }
            else -> {
                // If both sensors have high accuracy, set overall accuracy to high
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH
            }
        }

        // Update the TextView with accuracy level and color
        accuracyTV.text = when (overallAccuracy) {
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "Low"
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Medium"
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "High"
            else -> "Unknown"
        }

        // Set text color based on the overall accuracy level
        val textColor = when (overallAccuracy) {
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> this.getColor(R.color.speedometer_needle_start)
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> this.getColor(R.color.speedometer_needle_end)
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> this.getColor(R.color.colorAccent)
            else -> this.getColor(R.color.colorAccent)
        }

        accuracyTV.setTextColor(textColor)
    }

    fun writeSensorDataToCSV(event: SensorEvent, rotationAngle: Float) {
        val timestamp = event.timestamp.toString()

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                writeDataToCSV(timestamp, event.values, rotationAngle, "accelerometer.csv")
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                writeDataToCSV(timestamp, event.values, rotationAngle, "magnetometer.csv")
            }
        }
    }

    private fun writeDataToCSV(timestamp: String, values: FloatArray, rotationAngle: Float, fileName: String) {
        val filePath = getCSVFilePath(fileName)

        try {
            val file = File(filePath)

            if (!file.exists()) {
                if (file.createNewFile()) {
                    Log.d("CSVWriter", "File created successfully at $filePath")
                    // Write header if the file is newly created
                    FileWriter(file, true).use { writer ->
                        writer.append("Timestamp,X,Y,Z,RotationAngle\n")
                    }
                } else {
                    Log.e("CSVWriter", "Error creating file at $filePath")
                    return
                }
            }

            FileWriter(file, true).use { writer ->
                // Write data to the file
                writer.append("$timestamp,${values[0]},${values[1]},${values[2]},$rotationAngle\n")
            }

            Log.d("CSVWriter", "Data written to $filePath")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    private fun getCSVFilePath(fileName: String): String {
        // Use context.filesDir to get the app's internal storage directory
        val directory = File(this.filesDir, "SensorData")

        // Create the directory if it doesn't exist
        if (!directory.exists() && !directory.mkdirs()) {
            Log.e("CSVWriter", "Error creating directory at ${directory.absolutePath}")
        }

        return "${directory.absolutePath}/$fileName"
    }


    private fun showToastLogStatus(context: Context, isLogStarted: Boolean) {
        val message = if (isLogStarted) "Log Started" else "Log Ended"
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }



}