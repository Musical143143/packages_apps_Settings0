/*
 * Copyright (C) 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.nirwana;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;

/**
 * Connects Nirwana Engine toggles to system settings that already exist and are read by the
 * platform. Toggles without a system key stay in {@link NirwanaPrefs}.
 */
final class NirwanaSystemBridge {

    static final String KEY_DOUBLE_TAP = "gesture_double_tap";
    static final String KEY_HAPTICS = "sound_haptics";

    private NirwanaSystemBridge() {
    }

    static boolean isBound(String key) {
        return KEY_DOUBLE_TAP.equals(key) || KEY_HAPTICS.equals(key);
    }

    static boolean read(Context context, String key, boolean defaultValue) {
        ContentResolver resolver = context.getContentResolver();
        int fallback = defaultValue ? 1 : 0;
        if (KEY_DOUBLE_TAP.equals(key)) {
            return Settings.Secure.getInt(resolver, Settings.Secure.DOUBLE_TAP_TO_WAKE, fallback) != 0;
        }
        if (KEY_HAPTICS.equals(key)) {
            return Settings.System.getInt(resolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, fallback) != 0;
        }
        return defaultValue;
    }

    static void write(Context context, String key, boolean value) {
        ContentResolver resolver = context.getContentResolver();
        int flag = value ? 1 : 0;
        if (KEY_DOUBLE_TAP.equals(key)) {
            Settings.Secure.putInt(resolver, Settings.Secure.DOUBLE_TAP_TO_WAKE, flag);
        } else if (KEY_HAPTICS.equals(key)) {
            Settings.System.putInt(resolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, flag);
        }
    }
}
