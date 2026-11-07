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
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.android.settings.R;

/** Device card of the card layout: model, Android version and security patch. */
public class CardDevicePreference extends Preference implements CardLayoutPreference {

    public CardDevicePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.card_device_preference);
        setSelectable(false);
        setVisible(false);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        final Context context = getContext();
        ((TextView) holder.findViewById(R.id.card_device_name)).setText(Build.MODEL);
        ((TextView) holder.findViewById(R.id.card_device_subtitle)).setText(
                context.getString(R.string.card_device_subtitle, Build.VERSION.RELEASE));

        final TextView pill = (TextView) holder.findViewById(R.id.card_device_pill);
        final String patch = Build.VERSION.SECURITY_PATCH;
        if (TextUtils.isEmpty(patch)) {
            pill.setVisibility(View.GONE);
        } else {
            pill.setVisibility(View.VISIBLE);
            pill.setText(context.getString(R.string.card_device_pill, patch));
        }
    }
}
