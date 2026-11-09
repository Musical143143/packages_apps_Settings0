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

import com.android.settings.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Page definitions for Nirwana Engine, mirroring the screens shown in the reference video. */
final class NirwanaPages {

    static final int TYPE_HEADER = 0;
    static final int TYPE_SWITCH = 1;
    static final int TYPE_CHOICE = 2;
    static final int TYPE_NAV = 3;
    static final int TYPE_ACTION = 4;
    static final int TYPE_INFO = 5;

    static final int HERO_NONE = 0;
    static final int HERO_LOCKSCREEN = 1;
    static final int HERO_ABOUT = 2;
    static final int HERO_BATTERY = 3;
    static final int HERO_STORAGE = 4;

    static final String PAGE_LOCKSCREEN = "lockscreen";
    static final String PAGE_STATUS_BAR = "status_bar";
    static final String PAGE_ICONS = "icons";
    static final String PAGE_GESTURES = "gestures";
    static final String PAGE_BUTTONS = "buttons";
    static final String PAGE_QUICK_SETTINGS = "quick_settings";
    static final String PAGE_NOTIFICATIONS = "notifications";
    static final String PAGE_SOUNDS = "sounds";
    static final String PAGE_FONTS = "fonts";
    static final String PAGE_MISC = "miscellaneous";
    static final String PAGE_ABOUT = "about";
    static final String PAGE_BATTERY = "battery";
    static final String PAGE_STORAGE = "storage";

    /** Navigation targets are prefixed so one field can point at a Nirwana page or a system intent. */
    static final String PREFIX_PAGE = "page:";
    static final String PREFIX_INTENT = "intent:";

    /** Tiles shown on the Nirwana Engine hub, in display order. */
    static final List<String> HUB_TILES = Collections.unmodifiableList(Arrays.asList(
            PAGE_STATUS_BAR, PAGE_FONTS, PAGE_QUICK_SETTINGS, PAGE_NOTIFICATIONS,
            PAGE_SOUNDS, PAGE_ICONS, PAGE_GESTURES, PAGE_BUTTONS, PAGE_MISC));

    private NirwanaPages() {
    }

    /** One line on a page: a section header, a toggle, a choice, a navigation row or an info value. */
    static final class Row {
        final int type;
        final String key;
        final int titleRes;
        final int summaryRes;
        final boolean defaultOn;
        final int optionsRes;
        final String target;

        private Row(int type, String key, int titleRes, int summaryRes, boolean defaultOn,
                int optionsRes, String target) {
            this.type = type;
            this.key = key;
            this.titleRes = titleRes;
            this.summaryRes = summaryRes;
            this.defaultOn = defaultOn;
            this.optionsRes = optionsRes;
            this.target = target;
        }
    }

    /** A full Nirwana Engine page. */
    static final class Page {
        final String key;
        final int titleRes;
        final int subtitleRes;
        final int hero;
        final List<Row> rows;

        private Page(String key, int titleRes, int subtitleRes, int hero, List<Row> rows) {
            this.key = key;
            this.titleRes = titleRes;
            this.subtitleRes = subtitleRes;
            this.hero = hero;
            this.rows = rows;
        }
    }

    static Page get(String key) {
        if (key == null) {
            return get(PAGE_LOCKSCREEN);
        }
        switch (key) {
            case PAGE_STATUS_BAR:
                return build(key, R.string.nirwana_tile_status_bar, R.string.nirwana_status_bar_subtitle,
                        HERO_NONE,
                        toggle("sb_net_speed", R.string.nirwana_net_speed,
                                R.string.nirwana_net_speed_summary, true),
                        toggle("auto_hide_traffic", R.string.nirwana_auto_hide,
                                R.string.nirwana_auto_hide_traffic_summary, true),
                        action("sb_icons", R.string.nirwana_status_bar_icons,
                                R.string.nirwana_status_bar_icons_summary, "status_icons"),
                        toggle("sb_colour_notif", R.string.nirwana_colour_notif,
                                R.string.nirwana_colour_notif_summary, false),
                        toggle("sb_notif_count", R.string.nirwana_notif_count,
                                R.string.nirwana_notif_count_summary, false),
                        header(R.string.nirwana_section_status_clock),
                        toggle("sb_extended_clock", R.string.nirwana_extended_clock,
                                R.string.nirwana_extended_clock_summary, true),
                        toggle("sb_clock_seconds", R.string.nirwana_clock_seconds,
                                R.string.nirwana_clock_seconds_summary, false),
                        toggle("auto_hide_clock", R.string.nirwana_auto_hide,
                                R.string.nirwana_auto_hide_clock_summary, true));

            case PAGE_ICONS:
                return build(key, R.string.nirwana_icon_packs_title, R.string.nirwana_icons_subtitle,
                        HERO_NONE,
                        action("icon_pack", R.string.nirwana_icon_pack,
                                R.string.nirwana_icon_pack_summary, "icon_pack"),
                        choice("icon_wifi", R.string.nirwana_wifi_icon, 0, R.array.nirwana_icon_styles),
                        choice("icon_battery", R.string.nirwana_battery_icon, 0, R.array.nirwana_icon_styles),
                        choice("icon_cellular", R.string.nirwana_cellular_icon, 0, R.array.nirwana_icon_styles),
                        choice("icon_signal", R.string.nirwana_signal_icon, 0, R.array.nirwana_icon_styles),
                        header(R.string.nirwana_section_sliders),
                        toggle("slider_volume", R.string.nirwana_volume_slider, 0, true),
                        toggle("slider_nav", R.string.nirwana_nav_bar, 0, true),
                        toggle("slider_brightness", R.string.nirwana_brightness_slider, 0, true),
                        nav(R.string.nirwana_tile_misc, 0, PAGE_MISC));

            case PAGE_GESTURES:
                return build(key, R.string.nirwana_tile_gestures, R.string.nirwana_gestures_subtitle,
                        HERO_NONE,
                        toggle("gesture_double_tap", R.string.nirwana_double_tap,
                                R.string.nirwana_double_tap_summary, true),
                        toggle("gesture_swipe_brightness", R.string.nirwana_swipe_brightness,
                                R.string.nirwana_swipe_brightness_summary, false),
                        toggle("gesture_back", R.string.nirwana_back_gesture,
                                R.string.nirwana_back_gesture_summary, true));

            case PAGE_BUTTONS:
                return build(key, R.string.nirwana_tile_buttons, R.string.nirwana_buttons_subtitle,
                        HERO_NONE,
                        choice("nav_mode", R.string.nirwana_nav_mode,
                                R.string.nirwana_nav_mode_summary, R.array.nirwana_nav_modes),
                        toggle("btn_torch_long", R.string.nirwana_torch_long,
                                R.string.nirwana_torch_long_summary, false),
                        toggle("btn_volume_wake", R.string.nirwana_volume_wake,
                                R.string.nirwana_volume_wake_summary, false));

            case PAGE_QUICK_SETTINGS:
                return build(key, R.string.nirwana_tile_quick, R.string.nirwana_quick_subtitle,
                        HERO_NONE,
                        toggle("qs_battery_saver", R.string.nirwana_qs_battery_saver, 0, true),
                        toggle("qs_scanner", R.string.nirwana_qs_scanner,
                                R.string.nirwana_qs_scanner_summary, true),
                        toggle("qs_caffeine", R.string.nirwana_qs_caffeine,
                                R.string.nirwana_qs_caffeine_summary, true),
                        toggle("qs_dnd", R.string.nirwana_qs_dnd, 0, false),
                        toggle("qs_brightness", R.string.nirwana_qs_brightness,
                                R.string.nirwana_qs_brightness_summary, true),
                        toggle("qs_media", R.string.nirwana_qs_media,
                                R.string.nirwana_qs_media_summary, true));

            case PAGE_NOTIFICATIONS:
                return build(key, R.string.nirwana_tile_notifications,
                        R.string.nirwana_notifications_subtitle, HERO_NONE,
                        toggle("notif_history", R.string.nirwana_notif_history,
                                R.string.nirwana_notif_history_summary, true),
                        toggle("notif_conversations", R.string.nirwana_conversations,
                                R.string.nirwana_conversations_summary, true),
                        toggle("notif_dots", R.string.nirwana_notif_dots,
                                R.string.nirwana_notif_dots_summary, false));

            case PAGE_SOUNDS:
                return build(key, R.string.nirwana_tile_sounds, R.string.nirwana_sounds_subtitle,
                        HERO_NONE,
                        toggle("sound_haptics", R.string.nirwana_haptics,
                                R.string.nirwana_haptics_summary, true),
                        toggle("sound_dolby", R.string.nirwana_dolby,
                                R.string.nirwana_dolby_summary, true),
                        choice("sound_volume_style", R.string.nirwana_volume_style, 0,
                                R.array.nirwana_volume_styles));

            case PAGE_FONTS:
                return build(key, R.string.nirwana_tile_fonts, R.string.nirwana_fonts_subtitle,
                        HERO_NONE,
                        choice("font_family", R.string.nirwana_font_family, 0,
                                R.array.nirwana_font_families),
                        action("font_import", R.string.nirwana_import_font,
                                R.string.nirwana_import_font_summary, "import_font"));

            case PAGE_MISC:
                return build(key, R.string.nirwana_tile_misc, R.string.nirwana_misc_subtitle,
                        HERO_NONE,
                        toggle("misc_smooth_anim", R.string.nirwana_smooth_anim,
                                R.string.nirwana_smooth_anim_summary, true),
                        toggle("misc_dev_extras", R.string.nirwana_dev_extras,
                                R.string.nirwana_dev_extras_summary, false));

            case PAGE_ABOUT:
                return build(key, R.string.nirwana_tile_about, R.string.nirwana_about_subtitle,
                        HERO_ABOUT,
                        header(R.string.nirwana_section_device),
                        info("build_number", R.string.nirwana_build_number),
                        info("android_version", R.string.nirwana_android_version),
                        info("kernel", R.string.nirwana_kernel),
                        info("hardware", R.string.nirwana_processor),
                        info("uptime", R.string.nirwana_uptime),
                        info("storage_total", R.string.nirwana_storage_total));

            case PAGE_BATTERY:
                return build(key, R.string.nirwana_tile_battery, 0, HERO_BATTERY,
                        intent(R.string.nirwana_battery_usage, R.string.nirwana_battery_usage_summary,
                                "android.intent.action.POWER_USAGE_SUMMARY"),
                        action("sleep_mode", R.string.nirwana_sleep_mode,
                                R.string.nirwana_sleep_mode_summary, "sleep_mode"),
                        intent(R.string.nirwana_battery_saver, 0,
                                "android.settings.BATTERY_SAVER_SETTINGS"),
                        intent(R.string.nirwana_battery_optimization,
                                R.string.nirwana_battery_optimization_summary,
                                "android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS"),
                        action("charging_control", R.string.nirwana_charging_control,
                                R.string.nirwana_charging_control_summary, "charging_control"),
                        action("battery_stats", R.string.nirwana_battery_stats,
                                R.string.nirwana_battery_stats_summary, "battery_stats"));

            case PAGE_STORAGE:
                return build(key, R.string.nirwana_tile_storage, 0, HERO_STORAGE,
                        intent(R.string.nirwana_free_up_space, R.string.nirwana_free_up_space_summary,
                                "android.settings.INTERNAL_STORAGE_SETTINGS"),
                        action("storage_details", R.string.nirwana_storage_details,
                                R.string.nirwana_storage_details_summary, "storage_details"));

            case PAGE_LOCKSCREEN:
            default:
                return build(PAGE_LOCKSCREEN, R.string.nirwana_lockscreen_title,
                        R.string.nirwana_lockscreen_subtitle, HERO_LOCKSCREEN,
                        choice("lock_clock_style", R.string.nirwana_lock_clock_style, 0,
                                R.array.nirwana_clock_styles),
                        toggle("custom_lock_clocks", R.string.nirwana_custom_clocks,
                                R.string.nirwana_custom_clocks_summary, true),
                        header(R.string.nirwana_section_behaviour),
                        toggle("max_notification", R.string.nirwana_max_notification,
                                R.string.nirwana_max_notification_summary, true),
                        toggle("lock_weather", R.string.nirwana_weather_updates,
                                R.string.nirwana_weather_updates_summary, true),
                        toggle("lock_media_art", R.string.nirwana_media_art_cover,
                                R.string.nirwana_media_art_cover_summary, true),
                        toggle("lock_charging", R.string.nirwana_charging_info,
                                R.string.nirwana_charging_info_summary, true),
                        toggle("lock_ambient", R.string.nirwana_ambient_display,
                                R.string.nirwana_ambient_display_summary, true),
                        toggle("lock_aod", R.string.nirwana_always_on_display,
                                R.string.nirwana_always_on_display_summary, true));
        }
    }

    private static Page build(String key, int titleRes, int subtitleRes, int hero, Row... rows) {
        return new Page(key, titleRes, subtitleRes, hero,
                Collections.unmodifiableList(Arrays.asList(rows)));
    }

    private static Row header(int titleRes) {
        return new Row(TYPE_HEADER, null, titleRes, 0, false, 0, null);
    }

    private static Row toggle(String key, int titleRes, int summaryRes, boolean defaultOn) {
        return new Row(TYPE_SWITCH, key, titleRes, summaryRes, defaultOn, 0, null);
    }

    private static Row choice(String key, int titleRes, int summaryRes, int optionsRes) {
        return new Row(TYPE_CHOICE, key, titleRes, summaryRes, false, optionsRes, null);
    }

    private static Row nav(int titleRes, int summaryRes, String pageKey) {
        return new Row(TYPE_NAV, null, titleRes, summaryRes, false, 0, PREFIX_PAGE + pageKey);
    }

    private static Row intent(int titleRes, int summaryRes, String action) {
        return new Row(TYPE_NAV, null, titleRes, summaryRes, false, 0, PREFIX_INTENT + action);
    }

    private static Row action(String key, int titleRes, int summaryRes, String actionId) {
        return new Row(TYPE_ACTION, key, titleRes, summaryRes, false, 0, actionId);
    }

    private static Row info(String key, int titleRes) {
        return new Row(TYPE_INFO, key, titleRes, 0, false, 0, key);
    }
}
