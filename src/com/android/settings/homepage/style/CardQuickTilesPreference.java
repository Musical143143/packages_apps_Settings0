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

import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;

/**
 * Quick tiles of the card layout: Wi-Fi, Bluetooth, mobile data and hotspot. Each tile shows its
 * real state and opens the matching Settings page when tapped.
 */
public class CardQuickTilesPreference extends Preference implements CardLayoutPreference {

    public CardQuickTilesPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.card_quick_tiles_preference);
        setSelectable(false);
        setVisible(false);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        final Context context = getContext();

        bindTile(holder, R.id.card_quick_wifi, R.string.card_quick_wifi,
                R.drawable.ic_homepage_network, R.drawable.card_circle_blue,
                onOff(context, isWifiOn(context)), Settings.ACTION_WIFI_SETTINGS);
        bindTile(holder, R.id.card_quick_bluetooth, R.string.card_quick_bluetooth,
                R.drawable.ic_homepage_connected_device, R.drawable.card_circle_purple,
                onOff(context, isBluetoothOn()), Settings.ACTION_BLUETOOTH_SETTINGS);
        bindTile(holder, R.id.card_quick_data, R.string.card_quick_mobile_data,
                R.drawable.ic_network_cell, R.drawable.card_circle_green,
                onOff(context, isMobileDataOn(context)),
                Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
        bindTile(holder, R.id.card_quick_hotspot, R.string.card_quick_hotspot,
                R.drawable.ic_hotspot, R.drawable.card_circle_orange,
                context.getString(R.string.card_quick_tap), Settings.ACTION_WIRELESS_SETTINGS);
    }

    private void bindTile(PreferenceViewHolder holder, int tileId, int titleRes, int iconRes,
            int circleRes, String subtitle, String action) {
        final View tile = holder.findViewById(tileId);
        ((ImageView) tile.findViewById(R.id.card_quick_tile_icon)).setImageResource(iconRes);
        tile.findViewById(R.id.card_quick_tile_icon).setBackgroundResource(circleRes);
        ((TextView) tile.findViewById(R.id.card_quick_tile_title))
                .setText(getContext().getString(titleRes));
        ((TextView) tile.findViewById(R.id.card_quick_tile_subtitle)).setText(subtitle);
        tile.setOnClickListener(v -> launch(action));
    }

    private void launch(String action) {
        try {
            getContext().startActivity(new Intent(action));
        } catch (ActivityNotFoundException e) {
            // The page is not available on this build; nothing to open.
        }
    }

    private static String onOff(Context context, boolean on) {
        return context.getString(on ? R.string.card_quick_on : R.string.card_quick_off);
    }

    private static boolean isWifiOn(Context context) {
        final WifiManager wifi = context.getSystemService(WifiManager.class);
        return wifi != null && wifi.isWifiEnabled();
    }

    private static boolean isBluetoothOn() {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter != null && adapter.isEnabled();
    }

    private static boolean isMobileDataOn(Context context) {
        final TelephonyManager telephony = context.getSystemService(TelephonyManager.class);
        try {
            return telephony != null && telephony.isDataEnabled();
        } catch (SecurityException e) {
            return false;
        }
    }
}
