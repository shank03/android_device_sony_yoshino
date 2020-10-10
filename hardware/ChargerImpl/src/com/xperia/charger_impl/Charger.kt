/*
 * Copyright (c) 2020, Shashank Verma (shank03) <shashank.verma2002@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 */

package com.xperia.charger_impl

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.IBinder
import android.util.Log

class Charger : Service() {

    companion object {
        const val TAG = "Charger"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * A simple receiver that
     * - [enableCharging] when battery is less than 100
     * - [enableCharging] when reboot/shutdown
     * - [disableCharging] when fully charged.
     */
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            action ?: return

            when (action) {
                Intent.ACTION_BATTERY_CHANGED -> {
                    val batPct = intent.let {
                        val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                        val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                        level * 100 / scale.toFloat()
                    }

                    if (batPct == 100F) {
                        Log.d(TAG, "onReceive: Battery full")

                        // Send broadcast to stop battery care in-case it's running
                        //sendBroadcast(Intent().setAction("$packageName.$STOP_BATTERY_CARE")
                        //        .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                        //        .setComponent(ComponentName(SEND_PKG, "$SEND_PKG.receiver.StopBatteryCare")))

                        disableCharging(mainLooper)
                    } else enableCharging()
                }
                Intent.ACTION_SHUTDOWN, Intent.ACTION_REBOOT -> enableCharging()
            }
        }
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate: Service started")
        registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED).apply {
            addAction(Intent.ACTION_REBOOT)
            addAction(Intent.ACTION_SHUTDOWN)
        })
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }
}