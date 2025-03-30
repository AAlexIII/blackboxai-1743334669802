package com.useractionrecorder

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.useractionrecorder.adapter.ViewPagerAdapter
import com.useractionrecorder.service.RecordingAccessibilityService
import com.useractionrecorder.util.PermissionManager

class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionManager = PermissionManager(this)
        setupViews()
        checkPermissions()
    }

    private fun setupViews() {
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tab_recording)
                1 -> getString(R.string.tab_scheduling)
                else -> ""
            }
        }.attach()
    }

    private fun checkPermissions() {
        if (!isAccessibilityServiceEnabled()) {
            showAccessibilityServiceDialog()
        }
        
        if (!permissionManager.checkRequiredPermissions()) {
            permissionManager.requestRequiredPermissions()
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val accessibilityEnabled = Settings.Secure.getInt(
            contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED, 0
        )

        if (accessibilityEnabled == 1) {
            val serviceString = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            serviceString?.let {
                return it.contains("${packageName}/${RecordingAccessibilityService::class.java.name}")
            }
        }
        return false
    }

    private fun showAccessibilityServiceDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Требуется разрешение")
            .setMessage("Для работы приложения необходимо включить службу специальных возможностей")
            .setPositiveButton("Настройки") { _, _ ->
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            .setNegativeButton("Отмена", null)
            .setCancelable(false)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionManager.handlePermissionResult(requestCode, permissions, grantResults)
    }
}