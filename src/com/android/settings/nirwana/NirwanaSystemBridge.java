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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Connects Nirwana Engine toggles to system settings that already exist and are read by the
 * platform. Toggles without a matching system key stay in {@link NirwanaPrefs}.
 */
final class NirwanaSystemBridge {

    /** Nirwana toggle key to Settings.Secure key (stored as 0 or 1). */
    private static final Map<String, String> SECURE_KEYS;

    /** Nirwana toggle key to Settings.System key (stored as 0 or 1). */
    private static final Map<String, String> SYSTEM_KEYS;

    static {
        Map<String, String> secure = new HashMap<>();
        secure.put("gesture_double_tap", Settings.Secure.DOUBLE_TAP_TO_WAKE);
        secure.put("lock_ambient", Settings.Secure.DOZE_ENABLED);
        secure.put("lock_aod", Settings.Secure.DOZE_ALWAYS_ON);
        secure.put("max_notification", Settings.Secure.LOCK_SCREEN_SHOW_NOTIFICATIONS);
        secure.put("notif_history", Settings.Secure.NOTIFICATION_HISTORY_ENABLED);
        secure.put("notif_dots", Settings.Secure.NOTIFICATION_BADGING);
        SECURE_KEYS = Collections.unmodifiableMap(secure);

        Map<String, String> system = new HashMap<>();
        system.put("sound_haptics", Settings.System.HAPTIC_FEEDBACK_ENABLED);
        SYSTEM_KEYS = Collections.unmodifiableMap(system);
    }

    private NirwanaSystemBridge() {
    }

    static boolean isBound(String key) {
        return SECURE_KEYS.containsKey(key) || SYSTEM_KEYS.containsKey(key);
    }

    static boolean read(Context context, String key, boolean defaultValue) {
        ContentResolver resolver = context.getContentResolver();
        int fallback = defaultValue ? 1 : 0;
        if (SECURE_KEYS.containsKey(key)) {
            return Settings.Secure.getInt(resolver, SECURE_KEYS.get(key), fallback) != 0;
        }
        if (SYSTEM_KEYS.containsKey(key)) {
            return Settings.System.getInt(resolver, SYSTEM_KEYS.get(key), fallback) != 0;
        }
        return defaultValue;
    }

    static void write(Context context, String key, boolean value) {
        ContentResolver resolver = context.getContentResolver();
        int flag = value ? 1 : 0;
        if (SECURE_KEYS.containsKey(key)) {
            Settings.Secure.putInt(resolver, SECURE_KEYS.get(key), flag);
        } else if (SYSTEM_KEYS.containsKey(key)) {
            Settings.System.putInt(resolver, SYSTEM_KEYS.get(key), flag);
        }
    }
}
