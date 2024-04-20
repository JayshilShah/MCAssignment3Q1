package com.example.sensingapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class HistoryActivity : ComponentActivity() {
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        val viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(MainViewModel::class.java)


        setContent {
            HistoryContent(viewModel)
        }
    }

    @Composable
    fun HistoryContent(viewModel: MainViewModel) {
        val allOrientationData by viewModel.allOrientationData.observeAsState(initial = emptyList())

        Column {
            GraphView(allOrientationData, "Roll") { it.roll }
            GraphView(allOrientationData, "Pitch") { it.pitch }
            GraphView(allOrientationData, "Yaw") { it.yaw }
        }
    }

    @Composable
    fun GraphView(data: List<OrientationData>, label: String, valueSelector: (OrientationData) -> Float) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            factory = { context ->
                LineChart(context)
            },
            update = { chart ->
                val entries = data.mapIndexed { index, orientationData ->
                    Entry(index.toFloat(), valueSelector(orientationData))
                }
                val dataSet = LineDataSet(entries, label).apply {
                }
                chart.data = LineData(dataSet)
                chart.invalidate()
            }
        )
    }
}