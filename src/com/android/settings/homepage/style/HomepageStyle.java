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

package com.android.settings.homepage.style;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceDataStore;

/**
 * Persisted "Page style" options of the Settings homepage.
 *
 * <p>Values are stored in their own SharedPreferences file. {@link DataStore} lets the
 * "Page style" preference screen read and write them directly, and the homepage reads them
 * through the static getters.</p>
 */
public final class HomepageStyle {

    private static final String PREFS_NAME = "homepage_style";

    public static final String KEY_SEARCH_BAR_POSITION = "search_bar_position";
    public static final String KEY_GLASS_BOTTOM_BAR = "glass_bottom_bar";
    public static final String KEY_HEADER_TITLE = "header_title";
    public static final String KEY_SHOW_TITLE = "show_title";
    public static final String KEY_SHOW_AVATAR = "show_avatar";
    public static final String KEY_SHOW_GREETING = "show_greeting";
    public static final String KEY_RANDOM_GREETING_WORDS = "random_greeting_words";
    public static final String KEY_RANDOM_GREETING_COLOUR = "random_greeting_colour";
    public static final String KEY_RANDOM_TITLE_COLOUR = "random_title_colour";
    public static final String KEY_GREETING_SIZE = "greeting_size";
    public static final String KEY_HUGE_GREETING = "huge_greeting";
    public static final String KEY_TIME_BASED_GREETING = "time_based_greeting";
    public static final String KEY_GREETING_ALIGNMENT = "greeting_alignment";
    public static final String KEY_TILE_SPACING = "tile_spacing";
    public static final String KEY_TILE_ELEVATION = "tile_elevation";
    public static final String KEY_TILE_TITLE_SIZE = "tile_title_size";
    public static final String KEY_TILE_SHOW_SUMMARIES = "tile_show_summaries";

    public static final String POSITION_TOP = "top";
    public static final String POSITION_BOTTOM = "bottom";
    public static final String POSITION_IN_PAGE = "in_page";
    public static final String POSITION_HIDDEN = "hidden";

    public static final String ALIGN_LEFT = "left";
    public static final String ALIGN_CENTER = "center";
    public static final String ALIGN_RIGHT = "right";

    public static final String SPACING_COMPACT = "compact";
    public static final String SPACING_COMFORTABLE = "comfortable";
    public static final String SPACING_SPACIOUS = "spacious";

    public static final String ELEVATION_FLAT = "flat";
    public static final String ELEVATION_RAISED = "raised";
    public static final String ELEVATION_LIFTED = "lifted";

    public static final String TITLE_SIZE_SMALL = "small";
    public static final String TITLE_SIZE_NORMAL = "normal";
    public static final String TITLE_SIZE_LARGE = "large";

    public static final String DEFAULT_HEADER_TITLE = "*THINK POSITIVE ⭐";
    public static final int DEFAULT_GREETING_SIZE = 29;
    public static final int HUGE_GREETING_SIZE = 44;
    public static final int MIN_GREETING_SIZE = 20;
    public static final int MAX_GREETING_SIZE = 40;

    private HomepageStyle() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Clears every stored option, restoring the defaults. */
    public static void reset(Context context) {
        prefs(context).edit().clear().apply();
    }

    public static String getSearchBarPosition(Context context) {
        return prefs(context).getString(KEY_SEARCH_BAR_POSITION, POSITION_BOTTOM);
    }

    public static boolean isGlassBottomBar(Context context) {
        return prefs(context).getBoolean(KEY_GLASS_BOTTOM_BAR, true);
    }

    public static String getHeaderTitle(Context context) {
        return prefs(context).getString(KEY_HEADER_TITLE, DEFAULT_HEADER_TITLE);
    }

    public static boolean isShowTitle(Context context) {
        return prefs(context).getBoolean(KEY_SHOW_TITLE, true);
    }

    public static boolean isShowAvatar(Context context) {
        return prefs(context).getBoolean(KEY_SHOW_AVATAR, true);
    }

    public static boolean isShowGreeting(Context context) {
        return prefs(context).getBoolean(KEY_SHOW_GREETING, true);
    }

    public static boolean isRandomGreetingWords(Context context) {
        return prefs(context).getBoolean(KEY_RANDOM_GREETING_WORDS, true);
    }

    public static boolean isRandomGreetingColour(Context context) {
        return prefs(context).getBoolean(KEY_RANDOM_GREETING_COLOUR, true);
    }

    public static boolean isRandomTitleColour(Context context) {
        return prefs(context).getBoolean(KEY_RANDOM_TITLE_COLOUR, true);
    }

    public static int getGreetingSize(Context context) {
        return prefs(context).getInt(KEY_GREETING_SIZE, DEFAULT_GREETING_SIZE);
    }

    public static boolean isHugeGreeting(Context context) {
        return prefs(context).getBoolean(KEY_HUGE_GREETING, true);
    }

    public static boolean isTimeBasedGreeting(Context context) {
        return prefs(context).getBoolean(KEY_TIME_BASED_GREETING, true);
    }

    public static String getGreetingAlignment(Context context) {
        return prefs(context).getString(KEY_GREETING_ALIGNMENT, ALIGN_LEFT);
    }

    public static String getTileSpacing(Context context) {
        return prefs(context).getString(KEY_TILE_SPACING, SPACING_SPACIOUS);
    }

    public static String getTileElevation(Context context) {
        return prefs(context).getString(KEY_TILE_ELEVATION, ELEVATION_LIFTED);
    }

    public static String getTileTitleSize(Context context) {
        return prefs(context).getString(KEY_TILE_TITLE_SIZE, TITLE_SIZE_NORMAL);
    }

    public static boolean isTileShowSummaries(Context context) {
        return prefs(context).getBoolean(KEY_TILE_SHOW_SUMMARIES, true);
    }

    /** {@link PreferenceDataStore} backed by the homepage style preferences. */
    public static final class DataStore extends PreferenceDataStore {

        private final SharedPreferences mPrefs;

        public DataStore(Context context) {
            mPrefs = prefs(context);
        }

        @Override
        public void putString(String key, String value) {
            mPrefs.edit().putString(key, value).apply();
        }

        @Override
        public String getString(String key, String defValue) {
            return mPrefs.getString(key, defValue);
        }

        @Override
        public void putInt(String key, int value) {
            mPrefs.edit().putInt(key, value).apply();
        }

        @Override
        public int getInt(String key, int defValue) {
            return mPrefs.getInt(key, defValue);
        }

        @Override
        public void putBoolean(String key, boolean value) {
            mPrefs.edit().putBoolean(key, value).apply();
        }

        @Override
        public boolean getBoolean(String key, boolean defValue) {
            return mPrefs.getBoolean(key, defValue);
        }
    }
}
