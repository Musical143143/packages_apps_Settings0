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

import android.app.settings.SettingsEnums;
import android.os.Bundle;

import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.InstrumentedPreferenceFragment;

/** "Page style" screen: customizes the look of the Settings homepage. */
public class HomepageStyleFragment extends InstrumentedPreferenceFragment {

    private static final String KEY_RESET = "homepage_style_reset";

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.homepage_style;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setPreferenceDataStore(
                new HomepageStyle.DataStore(getContext()));
        super.onCreatePreferences(savedInstanceState, rootKey);

        final Preference reset = findPreference(KEY_RESET);
        if (reset != null) {
            reset.setOnPreferenceClickListener(preference -> {
                resetStyle();
                return true;
            });
        }
    }

    private void resetStyle() {
        HomepageStyle.reset(getContext());
        // Rebuild the screen so every control shows its default value again.
        setPreferenceScreen(null);
        onCreatePreferences(null, null);
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SETTINGS_HOMEPAGE;
    }
}
