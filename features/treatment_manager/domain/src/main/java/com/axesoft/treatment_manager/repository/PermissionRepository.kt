package com.axesoft.treatment_manager.repository

interface PermissionRepository {
    var wifiPermissionGranted: Boolean
    fun saveWifiPermission(granted: Boolean)
}