package dev.kwasi.wifidirectintro

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetAddress

class UdpHandler(private val activity: MainActivity, private val logInterface: LogInterface, private val shouldEcho: Boolean) {
    private val port = 8682
    private val bufferSize = 2048
    private var socket:DatagramSocket? = null

    suspend fun initializeAndRun(){
        socket = withContext(Dispatchers.IO) {
            DatagramSocket(port)
        }
        while (true){
            val receiveData = ByteArray(bufferSize)
            val receivePacket = DatagramPacket(receiveData, receiveData.size)
            withContext(Dispatchers.IO) {
                socket!!.receive(receivePacket)
            }

            val message = String(receivePacket.data, 0, receivePacket.length)

            activity.runOnUiThread {
                logInterface.logString(
                    "${receivePacket.address}: $message")
            }

            if (shouldEcho){
                send("Hwllo from the GO", receivePacket.address)

            }
        }
    }

    private suspend fun send (message: String, address: InetAddress){
        val s = message.toByteArray()
        withContext(Dispatchers.IO) {
            socket!!.send(DatagramPacket(s, s.size, address, port))
        }
    }

    suspend fun sendToGo(message: String){
        val goAddress = withContext(Dispatchers.IO) {
            Inet4Address.getByName("192.168.49.1")
        }
        send(message, goAddress)
    }

    fun close(){
        socket?.close()
    }
}