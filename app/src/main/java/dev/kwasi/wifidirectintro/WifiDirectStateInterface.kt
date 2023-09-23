package dev.kwasi.wifidirectintro

interface WifiDirectStateInterface {
    fun onGroupConnect(isGroupOwner: Boolean)
    fun onGroupDisconnect()
}