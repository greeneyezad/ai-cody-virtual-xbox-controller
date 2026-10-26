package com.aicody.virtualxbox

import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.io.OutputStream
import java.net.Socket
import java.util.UUID

interface ControllerTransport : Closeable {
    suspend fun connect()
    suspend fun send(state: ControllerState)
    val description: String
}

class AdbTunnelTransport(private val host: String = "127.0.0.1", private val port: Int = 26760) : ControllerTransport {
    private var socket: Socket? = null
    override val description = "USB / ADB tunnel"
    override suspend fun connect() = withContext(Dispatchers.IO) {
        socket = Socket(host, port).apply { soTimeout = 3000 }
    }
    override suspend fun send(state: ControllerState) = withContext(Dispatchers.IO) {
        val s = socket ?: error("Not connected")
        s.getOutputStream().write(state.toJsonLine().toByteArray(Charsets.UTF_8))
        s.getOutputStream().flush()
    }
    override fun close() { runCatching { socket?.close() }; socket = null }
}

class BluetoothSppTransport(
    private val macAddress: String,
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
) : ControllerTransport {
    private var socket: BluetoothSocket? = null
    override val description = "Bluetooth RFCOMM / SPP"
    override suspend fun connect() = withContext(Dispatchers.IO) {
        val adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter() ?: error("Bluetooth unavailable")
        val device = adapter.getRemoteDevice(macAddress)
        socket = device.createRfcommSocketToServiceRecord(uuid).also { it.connect() }
    }
    override suspend fun send(state: ControllerState) = withContext(Dispatchers.IO) {
        val s = socket ?: error("Not connected")
        s.outputStream.write(state.toJsonLine().toByteArray(Charsets.UTF_8))
        s.outputStream.flush()
    }
    override fun close() { runCatching { socket?.close() }; socket = null }
}
