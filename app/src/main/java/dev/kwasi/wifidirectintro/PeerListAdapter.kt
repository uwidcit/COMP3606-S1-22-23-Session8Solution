package dev.kwasi.wifidirectintro

import android.annotation.SuppressLint
import android.net.wifi.p2p.WifiP2pDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PeerListAdapter(private val peerInterface: CustomPeerInterface) :
    RecyclerView.Adapter<PeerListAdapter.ViewHolder>() {
    private var peers = mutableListOf<WifiP2pDevice>()


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val addressTextView: TextView = itemView.findViewById(R.id.macTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.peer_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val peer = peers[position]
        holder.nameTextView.text = peer.deviceName
        holder.addressTextView.text = peer.deviceAddress

        holder.itemView.setOnClickListener {
            peerInterface.onPeerClicked(peer)
        }
    }

    override fun getItemCount(): Int {
        return peers.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(deviceList: Collection<WifiP2pDevice>){
        peers.clear()
        peers.addAll(deviceList)
        notifyDataSetChanged()
    }
}
