package dev.kwasi.wifidirectintro

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pGroup
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.ActionListener
import android.net.wifi.p2p.WifiP2pManager.ChannelListener
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener
import android.net.wifi.p2p.WifiP2pManager.PeerListListener
import android.util.Log

class WifiDirectManager(
    private val manager: WifiP2pManager,
    private val channel: WifiP2pManager.Channel,
    private val customPeerInterface: CustomPeerInterface,
    private val logInterface: LogInterface,
    private val wifiDirectStateInterface: WifiDirectStateInterface
): BroadcastReceiver(), ActionListener, ConnectionInfoListener, GroupInfoListener{
    private var groupInfo : WifiP2pGroup? = null

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                // Check to see if Wi-Fi is enabled and notify appropriate activity
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                manager.requestPeers(channel) { peers: WifiP2pDeviceList? ->
                    peers?.deviceList?.let { customPeerInterface.onPeerListUpdated(it) };
                    Log.e("WFD Manager","I got the peer list: $peers")
                }
            }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                // Respond to new connection or disconnections
                manager.requestConnectionInfo(channel, this)
            }
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                // Respond to this device's wifi state changing
            }
        }
    }

    override fun onSuccess() {
        Log.e("WFD Manager", "The call was successful")
        logInterface.logString("The call was a success");
    }

    override fun onFailure(p0: Int) {
        Log.e("WFD Manager", "The call was NOT successful. Error code $p0")
        logInterface.logString("The call was NOT a success");
    }

    @SuppressLint("MissingPermission")
    override fun onConnectionInfoAvailable(info: WifiP2pInfo){
        manager.requestGroupInfo(channel, this)
    }


    override fun onGroupInfoAvailable(p0: WifiP2pGroup?) {
        // Save the group info
        groupInfo = p0
        logInterface.logString("Group Information Updated");
       if (groupInfo == null){
           wifiDirectStateInterface.onGroupDisconnect()
       } else {
           groupInfo?.isGroupOwner?.let { wifiDirectStateInterface.onGroupConnect(it) }
       }
    }

    @SuppressLint("MissingPermission")
    fun discover(){
        manager.discoverPeers(channel, this)
        logInterface.logString("Discover peers called");

    }

    @SuppressLint("MissingPermission")
    fun createGroup(){
        manager.createGroup(channel, this)
        logInterface.logString("Create Group called");

    }

    @SuppressLint("MissingPermission")
    fun disconnect(){
        manager.removeGroup(channel, this)
        logInterface.logString("Disconnect called");

    }

    @SuppressLint("MissingPermission")
    fun connectToPeer(peer: WifiP2pDevice) {
        val config = WifiP2pConfig()
        config.deviceAddress = peer.deviceAddress

        manager.connect(channel, config, this)
        logInterface.logString("Connect to peer: ${peer.deviceName}");
    }

    fun isConnected(): Boolean {
        return groupInfo != null
    }

    fun isGroupOwner(): Boolean {
         return groupInfo != null && groupInfo!!.isGroupOwner
    }
}