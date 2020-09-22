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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ChargeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        action ?: return
        context ?: return

        Log.d("${Charger.TAG}.ChargeReceiver", "onReceive: Action received: $action")

        when (action) {
            "$SEND_PKG.$ENABLE_CHARGING" -> enableCharging()
            "$SEND_PKG.$DISABLE_CHARGING" -> disableCharging(context.mainLooper)
        }
    }
}