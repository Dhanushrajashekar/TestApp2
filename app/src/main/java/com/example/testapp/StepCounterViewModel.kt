package com.example.testapp
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.ui.theme.HealthDetails
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.sqrt


class StepCounterViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {
    private var sensorManager: SensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var stepSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private var accelerometerSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastAccelerationMagnitude = 0.0
    private var stepThreshold = 12.0 // Example threshold, may need adjustment
    private var lastStepTime = System.currentTimeMillis()

    private val TIME_THRESHOLD_IN_MILLIS = 2 * 60 * 1000 // Change to 2 minutes
    private val INACTIVITY_CHECK_INTERVAL = 10 * 60 * 1000 // Check for inactivity every 10 minutes
    private val INACTIVITY_NOTIFICATION_ID = 12345 // Unique notification ID

    private val _steps = MutableStateFlow(0)
    val steps: StateFlow<Int> = _steps

    private val _calories = MutableStateFlow(0.0)
    val calories: StateFlow<Double> = _calories

    private val _moveGoal = MutableStateFlow(1000)
    val moveGoal: StateFlow<Int> = _moveGoal


    private var healthDetails: HealthDetails? = null


    init {
        val sensorToRegister = stepSensor ?: accelerometerSensor
        sensorManager.registerListener(this, sensorToRegister, SensorManager.SENSOR_DELAY_UI)
        createNotificationChannel()

        // Start checking for inactivity periodically
        startInactivityCheck()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val stepCount = event.values[0].toInt()
                viewModelScope.launch {
                    _steps.value = stepCount
                    _calories.value = calculateCalories(stepCount)
                    sendStepMilestoneNotification(stepCount)
                }
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val accelerationMagnitude = sqrt(x * x + y * y + z * z)
                detectStep(accelerationMagnitude)
            }
        }
    }

    private fun startInactivityCheck() {
        viewModelScope.launch {
            while (true) {
                delay(INACTIVITY_CHECK_INTERVAL.toLong()) // Delay for a specific interval
                checkInactivity()
            }
        }
    }

    private fun checkInactivity() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastStepTime > TIME_THRESHOLD_IN_MILLIS) {
            sendInactivityNotification()
        }
    }

    private fun sendInactivityNotification() {
        val notificationBuilder = NotificationCompat.Builder(getApplication(), "stepCounterChannel")
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle("Get Moving!")
            .setContentText("It seems like you haven't walked for a while. Take a break and walk around!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = getApplication<Application>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(INACTIVITY_NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun detectStep(accelerationMagnitude: Float) {
        val currentTime = System.currentTimeMillis()
        if (accelerationMagnitude > stepThreshold && currentTime - lastStepTime > 500) { // Debounce time of 500ms
            viewModelScope.launch {
                _steps.value += 1
                _calories.value = calculateCalories(_steps.value)
                sendStepMilestoneNotification(_steps.value)
            }
            lastStepTime = currentTime
        }
        lastAccelerationMagnitude = accelerationMagnitude.toDouble()
    }

    private fun sendStepMilestoneNotification(stepCount: Int) {
        if (stepCount % 1000 == 0) {
            val notificationBuilder = NotificationCompat.Builder(getApplication(), "stepCounterChannel")
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle("Congratulations!")
                .setContentText("You've reached $stepCount steps!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            val notificationManager = getApplication<Application>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(stepCount, notificationBuilder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Step Counter Channel"
            val descriptionText = "Notifications for step milestones"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("stepCounterChannel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getApplication<Application>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun setHealthDetails(dateOfBirth: Int, age: Int, weight: Int, height: String, sex: String) {
        healthDetails = HealthDetails( age, weight, height, sex)
    }

    fun resetSteps() {
        _steps.value = 0
        _calories.value = 0.0
    }

    fun increaseMoveGoal() {
        _moveGoal.value += 10
    }

    fun decreaseMoveGoal() {
        if (_moveGoal.value > 10) { // Prevent negative goals
            _moveGoal.value -= 10
        }
    }

    fun setHealthDetails(age: String, height: String, weight: String, sex: String) {
        // Assuming you have a StepCounterViewModel class where you define this function
        // You can perform whatever action you want with the health details here
        // For example, you can save them to a database or update some state in the view model
        // Replace the println statements with your actual implementation
        println("Age: $age")
        println("Height: $height")
        println("Weight: $weight")
        println("Sex: $sex")
    }



    private fun calculateCalories(steps: Int): Double {
        return steps * 0.04 // Simplified calorie calculation. Adjust based on actual use case.
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
    }
}
