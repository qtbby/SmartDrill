package com.example.smartdrill

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import java.nio.charset.StandardCharsets

class NearbyMeshManager(
    private val context: Context,
    private val onAlertReceived: (disasterType: String, senderId: String, locationInfo: String, deviceInfo: String) -> Unit
) {
    private val SERVICE_ID = "com.example.smartdrill.MESH_SOS"
    private val strategy = Strategy.P2P_CLUSTER

    private val connectedEndpoints = mutableSetOf<String>()

    fun startMeshService() {
        if (!hasGooglePlayServices()) {
            Log.w("NearbyMesh", "Google Play Services not available - offline SOS mesh disabled")
            return
        }
        startAdvertising()
        startDiscovery()
    }

    private fun hasGooglePlayServices(): Boolean {
        val result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        return result == ConnectionResult.SUCCESS
    }

    private fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(strategy).build()
        Nearby.getConnectionsClient(context)
            .startAdvertising(
                "SmartDrillUser",
                SERVICE_ID,
                connectionLifecycleCallback,
                advertisingOptions
            )
            .addOnSuccessListener {
                Log.d("NearbyMesh", "Started advertising SOS mesh node")
            }
            .addOnFailureListener { e: Exception ->
                Log.e("NearbyMesh", "Failed to start advertising", e)
            }
    }

    private fun startDiscovery() {
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(strategy).build()
        Nearby.getConnectionsClient(context)
            .startDiscovery(
                SERVICE_ID,
                object : EndpointDiscoveryCallback() {
                    override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                        Log.d("NearbyMesh", "Found peer endpoint: $endpointId")
                        Nearby.getConnectionsClient(context)
                            .requestConnection("SmartDrillUser", endpointId, connectionLifecycleCallback)
                            .addOnSuccessListener {
                                Log.d("NearbyMesh", "Requested connection to $endpointId")
                            }
                            .addOnFailureListener { e: Exception ->
                                Log.e("NearbyMesh", "Failed to request connection", e)
                            }
                    }

                    override fun onEndpointLost(endpointId: String) {
                        Log.d("NearbyMesh", "Lost endpoint: $endpointId")
                    }
                },
                discoveryOptions
            )
            .addOnSuccessListener {
                Log.d("NearbyMesh", "Started discovering peers")
            }
            .addOnFailureListener { e: Exception ->
                Log.e("NearbyMesh", "Failed to start discovery", e)
            }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Nearby.getConnectionsClient(context).acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                connectedEndpoints.add(endpointId)
                Log.d("NearbyMesh", "Connected to peer: $endpointId")
            } else {
                Log.e("NearbyMesh", "Connection failed to $endpointId")
            }
        }

        override fun onDisconnected(endpointId: String) {
            connectedEndpoints.remove(endpointId)
            Log.d("NearbyMesh", "Disconnected from peer: $endpointId")
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                val bytes = payload.asBytes() ?: return
                val message = String(bytes, StandardCharsets.UTF_8)
                Log.d("NearbyMesh", "Received SOS mesh payload: $message")

                // Parse format: DISASTER|senderId|locationInfo|deviceInfo
                val parts = message.split("|")
                if (parts.size >= 4) {
                    val disasterType = parts[0]
                    val senderId = parts[1]
                    val locationInfo = parts[2]
                    val deviceInfo = parts[3]
                    onAlertReceived(disasterType, senderId, locationInfo, deviceInfo)
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    fun broadcastSOS(disasterType: String, senderId: String, locationInfo: String, deviceInfo: String) {
        val message = "$disasterType|$senderId|$locationInfo|$deviceInfo"
        val payload = Payload.fromBytes(message.toByteArray(StandardCharsets.UTF_8))

        for (endpointId in connectedEndpoints) {
            Nearby.getConnectionsClient(context).sendPayload(endpointId, payload)
                .addOnSuccessListener {
                    Log.d("NearbyMesh", "Successfully broadcasted SOS to $endpointId")
                }
                .addOnFailureListener { e: Exception ->
                    Log.e("NearbyMesh", "Failed to broadcast SOS to $endpointId", e)
                }
        }
    }

    fun stopMesh() {
        Nearby.getConnectionsClient(context).stopAdvertising()
        Nearby.getConnectionsClient(context).stopDiscovery()
        Nearby.getConnectionsClient(context).stopAllEndpoints()
        connectedEndpoints.clear()
    }
}
