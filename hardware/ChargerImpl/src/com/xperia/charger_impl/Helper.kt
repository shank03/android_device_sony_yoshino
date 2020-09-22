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

import android.os.Handler
import android.os.Looper
import android.os.SystemProperties

const val SEND_PKG = "com.xperia.battery_care"

const val ENABLE_CHARGING = "ENABLE_CHARGING"
const val DISABLE_CHARGING = "DISABLE_CHARGING"

const val STOP_BATTERY_CARE = "STOP_BATTERY_CARE"

fun enableCharging() {
    // Check last lines in init.yoshino.rc
    SystemProperties.set("persist.sys.yoshino.battery.care", "1")
}

fun disableCharging(mainLooper: Looper) {
    // Disable charging in 30 seconds after call
    Handler(mainLooper).postDelayed({ SystemProperties.set("persist.sys.yoshino.battery.care", "0") }, 30 * 1000L)
}
