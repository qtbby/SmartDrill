package com.example.smartdrill.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartdrill.routing.Graph
import com.example.smartdrill.routing.MaxFlow
import kotlin.math.roundToInt

// Human-readable labels for the node IDs used by the small flow-network simulation.
private val demoPlaceNames = mapOf(
    1 to "East Dorm",
    2 to "West Dorm",
    3 to "Central Junction",
    4 to "Campus Court",
    5 to "Community Hall"
)

// Let users vary walkway capacity and a road closure, then inspect simulated flow and bottlenecks.
@Composable
fun MaxFlowDemoScreen(onBack: () -> Unit) {
    // Recompute only when a simulation input changes; the result drives all report cards below.
    var capacity by rememberSaveable { mutableIntStateOf(40) }
    var courtRoadBlocked by rememberSaveable { mutableStateOf(false) }
    val result = remember(capacity, courtRoadBlocked) {
        calculateDemoFlow(capacity, courtRoadBlocked)
    }

    ScreenGradient {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "EVACUATION FLOW DEMO",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Explore how the evacuation network responds to walkway capacity and a blocked road.",
                color = Color.White
            )

            DemoCard {
                Text("Walkway capacity", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("$capacity people per minute per walkway")
                Slider(
                    value = capacity.toFloat(),
                    onValueChange = { capacity = it.roundToInt() },
                    valueRange = 10f..100f,
                    steps = 8
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Block Central Junction → Campus Court", fontWeight = FontWeight.Bold)
                        Text("Try a road closure and compare the result.")
                    }
                    Switch(
                        checked = courtRoadBlocked,
                        onCheckedChange = { courtRoadBlocked = it }
                    )
                }
            }

            DemoCard {
                Text("Maximum evacuation flow", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(
                    text = "${result.maxFlow} people / minute",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF8B1A1A)
                )
                Text("Starts: East Dorm and West Dorm")
                Text("Safe areas: Campus Court and Community Hall")
            }

            DemoCard {
                Text("Walkways carrying flow", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                if (result.usedRoads.isEmpty()) {
                    Text("No walkways are carrying people in this scenario.")
                } else {
                    result.usedRoads.forEach { edge ->
                        Text(
                            "${placeName(edge.from)} → ${placeName(edge.to)}  " +
                                "${edge.flow} / ${edge.capacity} people per minute"
                        )
                    }
                }
            }

            DemoCard {
                Text("Current bottlenecks", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                if (result.bottlenecks.isEmpty()) {
                    Text("No real walkway is limiting this scenario.")
                } else {
                    result.bottlenecks.forEach { edge ->
                        Text("${placeName(edge.from)} → ${placeName(edge.to)}")
                    }
                }
            }

            Text(
                text = "Simulation only: sample locations and capacities. This is not live campus guidance.",
                color = Color.White,
                fontSize = 12.sp
            )
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A0E0E))
            ) {
                Text("Back to Login")
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

// Reusable white card surface for a section of the demo's results and controls.
@Composable
private fun DemoCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = { content() }
        )
    }
}

// Build the sample campus network, apply the selected closure, and calculate its maximum flow.
private fun calculateDemoFlow(capacity: Int, courtRoadBlocked: Boolean): MaxFlow.Result {
    val graph = Graph()
    graph.addNode(Graph.Node(1, "East Dorm", 7.4480, 125.8080, false))
    graph.addNode(Graph.Node(2, "West Dorm", 7.4490, 125.8070, false))
    graph.addNode(Graph.Node(3, "Central Junction", 7.4490, 125.8090, false))
    graph.addNode(Graph.Node(4, "Campus Court", 7.4500, 125.8100, true))
    graph.addNode(Graph.Node(5, "Community Hall", 7.4470, 125.8100, true))

    graph.connect(1, 3)
    graph.connect(2, 3)
    graph.connect(3, 4)
    graph.connect(3, 5)
    graph.connect(1, 4)
    graph.connect(2, 5)
    if (courtRoadBlocked) {
        graph.setHazard(3, 4, 0.0, true)
    }

    return MaxFlow.evacuationCapacity(graph, listOf(1, 2), capacity)
}

// Resolve a known demo node name and use a readable fallback for unlisted identifiers.
private fun placeName(id: Int): String = demoPlaceNames[id] ?: "Place $id"
