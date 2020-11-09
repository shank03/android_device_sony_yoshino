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

package com.yoshino.parts;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import com.android.internal.os.DeviceKeyHandler;
import com.android.internal.util.havoc.ActionUtils;

import java.util.List;

import static com.yoshino.parts.Constants.*;

public class KeyHandler implements DeviceKeyHandler {

    private final Context context;
    private PowerManager pm = null;

    private boolean isActionUpFactor = false;
    private boolean handlerStarted = false;

    public KeyHandler(Context context) {
        this.context = context;
    }

    /**
     * Handling key events
     *
     * @param keyEvent event to be handled
     * @return null if handled else #keyEvent itself
     */
    @Override
    public KeyEvent handleKeyEvent(KeyEvent keyEvent) {
        if (!hasSetupCompleted()) {
            return keyEvent;
        }

        if (pm == null) {
            pm = context.getSystemService(PowerManager.class);
        }

        // FOCUS KEY (1st Stage)
        if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_FOCUS) {
            if (!isFocusToggleFlashEnabled()) {
                if (isCameraAppRunning()) {
                    return keyEvent;
                }
                Log.d(TAG, "handleKeyEvent: Focus toggle preference disabled");
                return null;
            }

            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                keyEvent.startTracking();
            }

            if (keyEvent.isLongPress()) {
                if (isCameraAppRunning()) {
                    return keyEvent;
                }
                ActionUtils.toggleCameraFlash();
                return null;
            }
        }

        // CAMERA KEY (2nd stage)
        if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_CAMERA) {
            if (!isCameraLongPressEnabled()) {
                if (isCameraAppRunning()) {
                    return keyEvent;
                }
                Log.d(TAG, "handleKeyEvent: Camera long press preference disabled");
                return null;
            }

            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                if (!pm.isInteractive()) {
                    Log.d(TAG, "ACTION_DOWN: Screen OFF");
                    scheduleHandler();
                    return null;
                } else {
                    keyEvent.startTracking();
                }
            }

            if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                Log.d(TAG, "ACTION_UP: set false");
                isActionUpFactor = false;
            }

            if (keyEvent.isLongPress()) {
                Log.d(TAG, "LONG_PRESS: Camera key long pressed");

                if (isCameraAppRunning()) {
                    Log.d(TAG, "LONG_PRESS: Camera App already running");
                    return keyEvent;
                }

                startCamera();
                return null;
            }
        }

        return keyEvent;
    }

    private void startCamera() {
        Intent intent;
        KeyguardManager km = context.getSystemService(KeyguardManager.class);

        if (km.isKeyguardLocked()) {
            Log.d(TAG, "startCamera: Lockscreen active");
            intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE);
        } else {
            intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        }
        if (!pm.isInteractive()) {
            pm.wakeUp(SystemClock.uptimeMillis(), PowerManager.WAKE_REASON_CAMERA_LAUNCH,
                    "Wake screen to launch camera.");
            Log.d(TAG, "startCamera: Woke screen");
            intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE);
        }

        Log.d(TAG, "startCamera: Starting camera");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivityAsUser(intent, UserHandle.CURRENT_OR_SELF);
    }

    private void scheduleHandler() {
        if (handlerStarted) {
            return;
        }
        Log.d(TAG, "scheduleHandler: Starting ...");
        isActionUpFactor = true;
        handlerStarted = true;
        new Handler(context.getMainLooper()).postDelayed(() -> {
            handlerStarted = false;
            if (isActionUpFactor) {
                isActionUpFactor = false;
                startCamera();
            }
        }, 700);
    }

    /**
     * Get status of {@link Constants#SONY_CAMERA_PKG} app
     *
     * @return if in foreground and interactive
     */
    private boolean isCameraAppRunning() {
        ActivityManager manager = context.getSystemService(ActivityManager.class);
        List<ActivityManager.RunningAppProcessInfo> list = manager.getRunningAppProcesses();

        for (ActivityManager.RunningAppProcessInfo info : list) {
            if (info.processName.equals(SONY_CAMERA_PKG)
                    && info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return if user/device setup is completed
     */
    private boolean hasSetupCompleted() {
        return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.USER_SETUP_COMPLETE, 0) != 0;
    }

    private boolean isCameraLongPressEnabled() {
        return Settings.System.getInt(context.getContentResolver(), CAMERA_LONG_PRESS, 1) == 1;
    }

    private boolean isFocusToggleFlashEnabled() {
        return Settings.System.getInt(context.getContentResolver(), FOCUS_TOGGLE_FLASH, 0) == 1;
    }
}
