package com.example.bluetooth_seadmete_otsimine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

data class ScannedDevice(val address: String, val name: String?, val rssi: Int)

class DeviceAdapter : ListAdapter<ScannedDevice, DeviceAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameView: TextView = itemView.findViewById(R.id.deviceName)
        val rssiView: TextView = itemView.findViewById(R.id.deviceRssi)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val d = getItem(position)
        holder.nameView.text = d.name ?: "Unknown device"
        holder.rssiView.text = "${d.rssi} dBm"
    }

    class DiffCallback : DiffUtil.ItemCallback<ScannedDevice>() {
        override fun areItemsTheSame(oldItem: ScannedDevice, newItem: ScannedDevice): Boolean {
            return oldItem.address == newItem.address
        }

        override fun areContentsTheSame(oldItem: ScannedDevice, newItem: ScannedDevice): Boolean {
            return oldItem == newItem
        }
    }
}
