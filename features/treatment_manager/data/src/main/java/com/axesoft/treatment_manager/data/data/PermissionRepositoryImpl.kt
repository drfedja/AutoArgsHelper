package com.axesoft.treatment_manager.data.data

import android.content.Context
import com.axesoft.treatment_manager.repository.PermissionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import androidx.core.content.edit

class PermissionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PermissionRepository {

    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    override var wifiPermissionGranted: Boolean =
        prefs.getBoolean("wifi_permission", false)


    override fun saveWifiPermission(granted: Boolean) {
        prefs.edit { putBoolean("wifi_permission", granted) }
    }
}