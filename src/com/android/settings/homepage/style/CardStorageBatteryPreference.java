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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Environment;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;

import java.io.File;

/** Storage and battery tiles of the card layout, with live usage and charge level. */
public class CardStorageBatteryPreference extends Preference implements CardLayoutPreference {

    public CardStorageBatteryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.card_storage_battery_preference);
        setSelectable(false);
        setVisible(false);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        final Context context = getContext();

        final View storage = holder.findViewById(R.id.card_storage);
        final long total;
        final long free;
        final File data = Environment.getDataDirectory();
        total = data.getTotalSpace();
        free = data.getUsableSpace();
        final int usedPercent = total > 0 ? (int) ((total - free) * 100 / total) : 0;
        ((TextView) holder.findViewById(R.id.card_storage_subtitle)).setText(
                context.getString(R.string.card_storage_subtitle, usedPercent,
                        Formatter.formatShortFileSize(context, free)));
        ((ProgressBar) holder.findViewById(R.id.card_storage_progress))
                .setProgress(usedPercent);
        storage.setOnClickListener(v -> launch(Settings.ACTION_INTERNAL_STORAGE_SETTINGS));

        final View battery = holder.findViewById(R.id.card_battery);
        final BatteryManager manager = context.getSystemService(BatteryManager.class);
        final int level = manager != null
                ? manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) : 0;
        final boolean charging = manager != null && manager.isCharging();
        ((TextView) holder.findViewById(R.id.card_battery_subtitle)).setText(
                context.getString(charging ? R.string.card_battery_charging
                        : R.string.card_battery_subtitle, level));
        ((ProgressBar) holder.findViewById(R.id.card_battery_progress)).setProgress(level);
        battery.setOnClickListener(v -> launch(Settings.ACTION_BATTERY_SAVER_SETTINGS));
    }

    private void launch(String action) {
        try {
            getContext().startActivity(new Intent(action));
        } catch (ActivityNotFoundException e) {
            // The page is not available on this build; nothing to open.
        }
    }
}
