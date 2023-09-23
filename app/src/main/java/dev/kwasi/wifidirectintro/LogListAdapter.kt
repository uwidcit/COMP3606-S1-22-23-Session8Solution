package dev.kwasi.wifidirectintro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LogListAdapter() :
    RecyclerView.Adapter<LogListAdapter.ViewHolder>() {
    private var logContent = mutableListOf<String>()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val logTextView: TextView = itemView.findViewById(R.id.log_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.log_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return logContent.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val content: String = logContent[position]
        holder.logTextView.text = content
    }

    fun addToLog(content: String){
        logContent.add(0, content)
        notifyItemInserted(0)
    }
}