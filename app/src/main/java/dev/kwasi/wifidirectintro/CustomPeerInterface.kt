package dev.kwasi.wifidirectintro

import android.net.wifi.p2p.WifiP2pDevice

interface CustomPeerInterface {
    fun onPeerClicked(peer: WifiP2pDevice)
    fun onPeerListUpdated(deviceList: Collection<WifiP2pDevice>)
}