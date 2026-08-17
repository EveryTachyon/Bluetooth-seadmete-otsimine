package com.example.bluetooth_seadmete_otsimine

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ScannerActivity : AppCompatActivity() {
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var scanner: BluetoothLeScanner? = null
    private lateinit var adapter: DeviceAdapter
    private val devices = mutableMapOf<String, ScannedDevice>()
    private var scanning = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        var granted = true
        perms.forEach { if (!it.value) granted = false }
        if (granted) startBleScan()
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val d = result.device
            val addr = d.address ?: return
            val name = d.name
            val rssi = result.rssi
            val sd = ScannedDevice(addr, name, rssi)
            devices[addr] = sd
            adapter.submitList(devices.values.toList())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val manager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = manager.adapter
        scanner = bluetoothAdapter?.bluetoothLeScanner

        adapter = DeviceAdapter()
        val recycler = findViewById<RecyclerView>(R.id.devicesRecycler)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        val scanBtn = findViewById<Button>(R.id.scanButton)
        scanBtn.setOnClickListener {
            if (scanning) stopBleScan() else checkPermissionsAndScan()
            scanBtn.text = if (!scanning) "Stop scanning" else "Scan nearby devices"
        }
    }

    private fun checkPermissionsAndScan() {
        val needed = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            needed += Manifest.permission.BLUETOOTH_SCAN
            needed += Manifest.permission.BLUETOOTH_CONNECT
        } else {
            needed += Manifest.permission.ACCESS_FINE_LOCATION
        }
        val toRequest = needed.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (toRequest.isNotEmpty()) {
            permissionLauncher.launch(toRequest.toTypedArray())
        } else {
            startBleScan()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startBleScan() {
        if (scanning) return
        scanner = bluetoothAdapter?.bluetoothLeScanner
        scanner?.startScan(scanCallback)
        scanning = true
    }

    @SuppressLint("MissingPermission")
    private fun stopBleScan() {
        if (!scanning) return
        scanner?.stopScan(scanCallback)
        scanning = false
    }

    override fun onDestroy() {
        stopBleScan()
        super.onDestroy()
    }
}
