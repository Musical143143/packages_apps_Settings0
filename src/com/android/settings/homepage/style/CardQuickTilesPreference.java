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

import android.Manifest;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.TetheringManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;

/**
 * Quick tiles of the card layout. Page 1 has Wi-Fi, Bluetooth, mobile data and hotspot; page 2
 * has airplane mode, location and do not disturb. Each tile shows its real state. The network
 * tiles switch their radio when tapped, and fall back to opening their Settings page if the
 * change is refused. Pages change from the dots or by swiping.
 */
public class CardQuickTilesPreference extends Preference implements CardLayoutPreference {

    private static final int REFRESH_DELAY_MS = 800;
    private static final float SWIPE_MIN_VELOCITY = 200f;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private int mPage;

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

        final View page0 = holder.findViewById(R.id.card_quick_page_0);
        final View page1 = holder.findViewById(R.id.card_quick_page_1);
        final View dot0 = holder.findViewById(R.id.card_quick_dot_0);
        final View dot1 = holder.findViewById(R.id.card_quick_dot_1);
        final View pager = holder.findViewById(R.id.card_quick_pager);

        bindTile(holder, R.id.card_quick_wifi, R.string.card_quick_wifi,
                R.drawable.ic_homepage_network, R.drawable.card_circle_blue,
                wifiSubtitle(context), () -> toggleWifi(context));
        bindTile(holder, R.id.card_quick_bluetooth, R.string.card_quick_bluetooth,
                R.drawable.ic_homepage_connected_device, R.drawable.card_circle_purple,
                onOff(context, isBluetoothOn()), () -> toggleBluetooth(context));
        bindTile(holder, R.id.card_quick_data, R.string.card_quick_mobile_data,
                R.drawable.ic_homepage_data_usage, R.drawable.card_circle_green,
                onOff(context, isMobileDataOn(context)), () -> toggleMobileData(context));
        bindTile(holder, R.id.card_quick_hotspot, R.string.card_quick_hotspot,
                R.drawable.ic_homepage_wifi_tethering, R.drawable.card_circle_orange,
                onOff(context, isHotspotOn(context)), () -> toggleHotspot(context));

        bindTile(holder, R.id.card_quick_airplane, R.string.card_quick_airplane,
                R.drawable.ic_airplanemode_active, R.drawable.card_circle_blue,
                onOff(context, isAirplaneOn(context)),
                () -> launch(Settings.ACTION_AIRPLANE_MODE_SETTINGS));
        bindTile(holder, R.id.card_quick_location, R.string.card_quick_location,
                R.drawable.ic_homepage_location, R.drawable.card_circle_green,
                onOff(context, isLocationOn(context)),
                () -> launch(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        bindTile(holder, R.id.card_quick_dnd, R.string.card_quick_dnd,
                R.drawable.ic_homepage_modes, R.drawable.card_circle_purple,
                onOff(context, isDndOn(context)), () -> launch(Settings.ACTION_SOUND_SETTINGS));

        showPage(page0, page1, dot0, dot1, mPage);
        dot0.setOnClickListener(v -> showPage(page0, page1, dot0, dot1, 0));
        dot1.setOnClickListener(v -> showPage(page0, page1, dot0, dot1, 1));

        final GestureDetector detector = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2,
                            float velocityX, float velocityY) {
                        if (velocityX <= -SWIPE_MIN_VELOCITY) {
                            showPage(page0, page1, dot0, dot1, 1);
                            return true;
                        }
                        if (velocityX >= SWIPE_MIN_VELOCITY) {
                            showPage(page0, page1, dot0, dot1, 0);
                            return true;
                        }
                        return false;
                    }
                });
        pager.setOnTouchListener((v, event) -> {
            detector.onTouchEvent(event);
            return false;
        });
    }

    private void showPage(View page0, View page1, View dot0, View dot1, int page) {
        mPage = page;
        page0.setVisibility(page == 0 ? View.VISIBLE : View.GONE);
        page1.setVisibility(page == 1 ? View.VISIBLE : View.GONE);
        dot0.setBackgroundResource(page == 0 ? R.drawable.card_dot_active : R.drawable.card_dot);
        dot1.setBackgroundResource(page == 1 ? R.drawable.card_dot_active : R.drawable.card_dot);
    }

    private void bindTile(PreferenceViewHolder holder, int tileId, int titleRes, int iconRes,
            int circleRes, String subtitle, Runnable onClick) {
        final View tile = holder.findViewById(tileId);
        final ImageView icon = tile.findViewById(R.id.card_quick_tile_icon);
        icon.setImageResource(iconRes);
        icon.setBackgroundResource(circleRes);
        ((TextView) tile.findViewById(R.id.card_quick_tile_title)).setText(titleRes);
        ((TextView) tile.findViewById(R.id.card_quick_tile_subtitle)).setText(subtitle);
        tile.setOnClickListener(v -> onClick.run());
    }

    private void refreshSoon() {
        mHandler.postDelayed(this::notifyChanged, REFRESH_DELAY_MS);
    }

    private void launch(String action) {
        try {
            getContext().startActivity(new Intent(action));
        } catch (ActivityNotFoundException e) {
            // The page is not available on this build; nothing to open.
        }
    }

    private void toggleWifi(Context context) {
        final WifiManager wifi = context.getSystemService(WifiManager.class);
        if (wifi == null) {
            launch(Settings.ACTION_WIFI_SETTINGS);
            return;
        }
        try {
            wifi.setWifiEnabled(!wifi.isWifiEnabled());
        } catch (SecurityException e) {
            launch(Settings.ACTION_WIFI_SETTINGS);
            return;
        }
        refreshSoon();
    }

    private void toggleBluetooth(Context context) {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            launch(Settings.ACTION_BLUETOOTH_SETTINGS);
            return;
        }
        try {
            if (adapter.isEnabled()) {
                adapter.disable();
            } else {
                adapter.enable();
            }
        } catch (SecurityException e) {
            launch(Settings.ACTION_BLUETOOTH_SETTINGS);
            return;
        }
        refreshSoon();
    }

    private void toggleMobileData(Context context) {
        final TelephonyManager telephony = context.getSystemService(TelephonyManager.class);
        if (telephony == null) {
            launch(Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
            return;
        }
        try {
            telephony.setDataEnabled(!telephony.isDataEnabled());
        } catch (SecurityException e) {
            launch(Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
            return;
        }
        refreshSoon();
    }

    private void toggleHotspot(Context context) {
        final TetheringManager tethering = context.getSystemService(TetheringManager.class);
        if (tethering == null) {
            launch(Settings.ACTION_WIRELESS_SETTINGS);
            return;
        }
        try {
            if (isHotspotOn(context)) {
                tethering.stopTethering(TetheringManager.TETHERING_WIFI);
            } else {
                tethering.startTethering(TetheringManager.TETHERING_WIFI, r -> r.run(),
                        new TetheringManager.StartTetheringCallback() {
                            @Override
                            public void onTetheringStarted() {
                            }

                            @Override
                            public void onTetheringFailed(int error) {
                            }
                        });
            }
        } catch (SecurityException e) {
            launch(Settings.ACTION_WIRELESS_SETTINGS);
            return;
        }
        refreshSoon();
    }

    private static String onOff(Context context, boolean on) {
        return context.getString(on ? R.string.card_quick_on : R.string.card_quick_off);
    }

    private static String wifiSubtitle(Context context) {
        final WifiManager wifi = context.getSystemService(WifiManager.class);
        if (wifi == null || !wifi.isWifiEnabled()) {
            return onOff(context, false);
        }
        final WifiInfo info = wifi.getConnectionInfo();
        if (info == null || info.getNetworkId() == -1) {
            return onOff(context, true);
        }
        // The network name needs location permission; without it show "Connected".
        if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            final String ssid = info.getSSID();
            if (ssid != null && !ssid.contains("unknown")) {
                return ssid.replace("\"", "");
            }
        }
        return context.getString(R.string.card_quick_connected);
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

    private static boolean isHotspotOn(Context context) {
        final TetheringManager tethering = context.getSystemService(TetheringManager.class);
        if (tethering == null) {
            return false;
        }
        final String[] ifaces = tethering.getTetheredIfaces();
        return ifaces != null && ifaces.length > 0;
    }

    private static boolean isAirplaneOn(Context context) {
        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
    }

    private static boolean isLocationOn(Context context) {
        final LocationManager location = context.getSystemService(LocationManager.class);
        return location != null && location.isLocationEnabled();
    }

    private static boolean isDndOn(Context context) {
        final NotificationManager notifications =
                context.getSystemService(NotificationManager.class);
        return notifications != null && notifications.getCurrentInterruptionFilter()
                != NotificationManager.INTERRUPTION_FILTER_ALL;
    }
}
