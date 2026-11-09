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

import android.content.Context;
import android.content.SharedPreferences;

/** Persists the toggles and choices made on Nirwana Engine pages. */
final class NirwanaPrefs {

    private static final String FILE = "nirwana_engine";

    private NirwanaPrefs() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    static boolean getBoolean(Context context, String key, boolean defaultValue) {
        return prefs(context).getBoolean(key, defaultValue);
    }

    static void setBoolean(Context context, String key, boolean value) {
        prefs(context).edit().putBoolean(key, value).apply();
    }

    static int getInt(Context context, String key, int defaultValue) {
        return prefs(context).getInt(key, defaultValue);
    }

    static void setInt(Context context, String key, int value) {
        prefs(context).edit().putInt(key, value).apply();
    }
}
