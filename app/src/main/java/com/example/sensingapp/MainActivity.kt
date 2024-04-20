package com.example.sensingapp

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.RoomOpenHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.os.Environment
import java.io.File
import java.io.FileWriter
import java.io.IOException
import kotlinx.coroutines.withContext

@Entity(tableName = "orientation_data")
data class OrientationData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val roll: Float,
    val pitch: Float,
    val yaw: Float
)

@Dao
interface OrientationDataDao {
    @Insert
    fun insert(data: OrientationData)

    @Query("SELECT * FROM orientation_data ORDER BY timestamp DESC")
    fun getAllData(): List<OrientationData>

    @Query("SELECT * FROM orientation_data ORDER BY timestamp DESC LIMIT 500")
    fun getTop500Data(): List<OrientationData>
}

@Database(entities = [OrientationData::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun orientationDataDao(): OrientationDataDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var roll = 0f
    private var pitch = 0f
    private var yaw = 0f
    private var lastTimestamp: Long = 0
    private var angularVelocityX = 0f
    private var angularVelocityY = 0f
    private var angularVelocityZ = 0f
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        database = AppDatabase.getDatabase(this)

        setContent {
            OrientationDisplay(roll, pitch, yaw)
        }
    }

    override fun onResume() {
        super.onResume()
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }
        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Calculate roll and pitch
            pitch = Math.toDegrees(Math.atan2(y.toDouble(), z.toDouble())).toFloat()
            roll = Math.toDegrees(Math.atan2((-x).toDouble(), Math.sqrt((y * y + z * z).toDouble()))).toFloat()
        }
        if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            val dt = (event.timestamp - lastTimestamp) * NS2S
            lastTimestamp = event.timestamp

            // Convert angular velocities from rad/s to degrees/s
            angularVelocityX = Math.toDegrees(event.values[0].toDouble()).toFloat()
            angularVelocityY = Math.toDegrees(event.values[1].toDouble()).toFloat()
            angularVelocityZ = Math.toDegrees(event.values[2].toDouble()).toFloat()

            // Integrate angular velocities to estimate orientation changes
            yaw += angularVelocityZ * dt

            // Normalize yaw angle to [0, 360)
            yaw = (yaw + 360) % 360
        }
        // Update UI
        setContent {
            OrientationDisplay(roll, pitch, yaw)
        }
        val data = OrientationData(
            timestamp = System.currentTimeMillis(),
            roll = event.values[0],
            pitch = event.values[1],
            yaw = event.values[2]
        )
        CoroutineScope(Dispatchers.IO).launch {
            database.orientationDataDao().insert(data)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }

    override fun onDestroy() {
        super.onDestroy()
        accelerometer?.let {
            sensorManager.unregisterListener(this)
        }
        gyroscope?.let{
            sensorManager.unregisterListener(this)
        }
    }

    companion object {
        private const val NS2S = 1.0f / 1000000000.0f
    }

    @Composable
    fun OrientationDisplay(roll: Float, pitch: Float, yaw: Float) {
        Box (
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 0.1f.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "          Roll:",
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Blue
                        )
                        Text(
                            text = "          Pitch:",
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "          Yaw:",
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Red
                        )
                    }
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "$roll          ",
                            fontSize = 24.sp,
                            textAlign = TextAlign.Left,
                            color = Color.Blue
                        )
                        Text(
                            text = "$pitch          ",
                            fontSize = 24.sp,
                            textAlign = TextAlign.Left,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "$yaw          ",
                            fontSize = 24.sp,
                            textAlign = TextAlign.Left,
                            color = Color.Red
                        )
                    }
                }
                Row {
                    Button(
                        onClick = { navigateToHistoryActivity() },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("History Graphs")
                    }
                }
//                Row {
//                    Button(
//                        onClick = {
//                            CoroutineScope(Dispatchers.Main).launch {
//                                exportDataToCsv(this@MainActivity)
//                            }
//                        },
//                        modifier = Modifier.padding(16.dp)
//                    ) {
//                        Text("Export Data")
//                    }
//                }
            }
        }
    }

    fun navigateToHistoryActivity() {
        startActivity(Intent(this@MainActivity, HistoryActivity::class.java))
    }

//    suspend fun exportDataToCsv(context: Context) {
//        withContext(Dispatchers.IO) {
//            val database = AppDatabase.getDatabase(context)
//            val orientationDataList = database.orientationDataDao().getTop500Data()
//
//            val csvFileName = "orientation_data.csv"
//
//            val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "SensorData")
//            directory.mkdirs() // Create the directory if it does not exist
//
//            val csvFile = File(directory, csvFileName)
//
//            try {
//                val writer = FileWriter(csvFile)
//                writer.append("timestamp,roll,pitch,yaw\n")
//                orientationDataList.forEach { data ->
//                    writer.append("${data.timestamp},${data.roll},${data.pitch},${data.yaw}\n")
//                }
//                writer.flush()
//                writer.close()
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }
//    }
}

