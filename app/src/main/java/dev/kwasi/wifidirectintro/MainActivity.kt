package dev.kwasi.wifidirectintro

import android.Manifest
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build.VERSION.SDK_INT
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class MainActivity : AppCompatActivity(), CustomPeerInterface, LogInterface, WifiDirectStateInterface{
    private val requestCode = 1234
    private var hasPermissions = true

    private var wfdManager: WifiDirectManager? = null

    private val peerAdapter: PeerListAdapter = PeerListAdapter(this)
    private val logAdapter: LogListAdapter = LogListAdapter()

    private var handler: UdpHandler? = null

    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val recyclerView = findViewById<RecyclerView>(R.id.rv_peer_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = peerAdapter

        val logView = findViewById<RecyclerView>(R.id.rv_chatbox)
        logView.layoutManager = LinearLayoutManager(this)
        logView.adapter = logAdapter

        hasPermissions = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
        if (SDK_INT >= 33){
            hasPermissions = hasPermissions && checkSelfPermission(Manifest.permission.NEARBY_WIFI_DEVICES) == PackageManager.PERMISSION_GRANTED
        }


        if (!hasPermissions){
            var perm = arrayOf(
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
            // Android 13 (API 33) requires the NEARBY_WIFI_DEVICES permission
            if (SDK_INT >= 33){
                perm +=Manifest.permission.NEARBY_WIFI_DEVICES
            }
            ActivityCompat.requestPermissions(this, perm, requestCode)
        } else {
            setupWfdManager()
        }
        updateUI()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            this.requestCode -> {
                hasPermissions = true
                grantResults.forEach {
                    hasPermissions = hasPermissions && it==PackageManager.PERMISSION_GRANTED
                }
                if (hasPermissions){
                    setupWfdManager()
                }
                updateUI()
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    private fun setupWfdManager() {
        val manager: WifiP2pManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager

        val channel = manager.initialize(this, mainLooper, null)
        wfdManager = WifiDirectManager(manager, channel, this, this, this)
    }

    private fun updateUI() {
        // This function looks at our hasPermissions state variable and hides or shows the relevant views
        val llPermissionOk = findViewById<LinearLayout>(R.id.ll_permission_ok)
        val rlError = findViewById<RelativeLayout>(R.id.rl_error)

        llPermissionOk.visibility = if (hasPermissions) View.VISIBLE else View.GONE
        rlError.visibility = if (!hasPermissions) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        wfdManager?.also {
            registerReceiver(it, intentFilter)
        }
    }

    /* unregister the broadcast receiver */
    override fun onPause() {
        super.onPause()
        wfdManager?.also { receiver ->
            unregisterReceiver(receiver)
        }
    }

    fun onDiscoverClick(view: View) {
        wfdManager?.discover();
    }

    fun onCreateGroupClick(view: View) {
        wfdManager?.createGroup()
    }

    fun onDisconnectClick(view: View) {
        wfdManager?.disconnect()
    }

    fun onInfoClick(view: View) {
        //TODO: Add info to logs
    }
    fun onMessageClick(view: View) {
        val message = "Hello, WOrld!" // Message to send
        if (wfdManager != null){
            if (wfdManager!!.isConnected()){
                if (handler == null){
                    handler = UdpHandler(this, this, wfdManager!!.isGroupOwner())
                }
                runBlocking {
                    handler!!.sendToGo(message)
                }
            }

        }
    }

    override fun onPeerClicked(peer: WifiP2pDevice) {
        wfdManager?.connectToPeer(peer);
    }

    override fun onPeerListUpdated(deviceList: Collection<WifiP2pDevice>) {
        peerAdapter.updateList(deviceList)
    }

    override fun logString(logContent: String) {
       logAdapter.addToLog(logContent)
    }

    override fun onGroupConnect(isGroupOwner: Boolean) {
        handler = UdpHandler(this, this, isGroupOwner)
        runBlocking {
            handler!!.initializeAndRun()
        }
    }

    override fun onGroupDisconnect() {
        handler?.close()
        handler = null

    }

}
