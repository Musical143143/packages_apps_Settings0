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

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.settings.R;

import java.util.concurrent.TimeUnit;

/** Renders any Nirwana Engine page from its {@link NirwanaPages} definition. */
public class NirwanaSubpageActivity extends Activity {

    static final String EXTRA_PAGE = "nirwana_page";

    private NirwanaPages.Page mPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NirwanaUi.styleWindow(this);
        mPage = NirwanaPages.get(getIntent().getStringExtra(EXTRA_PAGE));
        setContentView(buildContent());
    }

    private View buildContent() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(getColor(R.color.nirwana_background));
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int side = NirwanaUi.dp(this, 16);
        root.setPadding(side, NirwanaUi.dp(this, 8), side, NirwanaUi.dp(this, 32));

        TextView back = NirwanaUi.text(this, "‹", 32,
                getColor(R.color.nirwana_text), false);
        back.setPadding(NirwanaUi.dp(this, 8), 0, NirwanaUi.dp(this, 8), 0);
        back.setOnClickListener(v -> finish());
        root.addView(back);

        TextView title = NirwanaUi.text(this, getString(mPage.titleRes), 30,
                getColor(R.color.nirwana_accent), true);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(title, NirwanaUi.matchWrap());

        if (mPage.subtitleRes != 0) {
            TextView subtitle = NirwanaUi.text(this, getString(mPage.subtitleRes), 14,
                    getColor(R.color.nirwana_text_secondary), false);
            subtitle.setGravity(Gravity.CENTER_HORIZONTAL);
            root.addView(subtitle, NirwanaUi.matchWrap());
        }

        if (mPage.hero != NirwanaPages.HERO_NONE) {
            root.addView(buildHero(), block(NirwanaUi.dp(this, 20), 0));
        }

        LinearLayout card = null;
        for (NirwanaPages.Row row : mPage.rows) {
            if (row.type == NirwanaPages.TYPE_HEADER) {
                TextView header = NirwanaUi.text(this, getString(row.titleRes), 14,
                        getColor(R.color.nirwana_accent), true);
                root.addView(header, block(NirwanaUi.dp(this, 24), NirwanaUi.dp(this, 8)));
                card = null;
                continue;
            }
            if (card == null) {
                card = new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setBackground(NirwanaUi.roundedBackground(this,
                        getColor(R.color.nirwana_surface), 20));
                root.addView(card, block(NirwanaUi.dp(this, 12), 0));
            } else {
                card.addView(divider());
            }
            card.addView(rowView(row));
        }

        scroll.addView(root);
        return scroll;
    }

    private LinearLayout.LayoutParams block(int top, int bottom) {
        LinearLayout.LayoutParams params = NirwanaUi.matchWrap();
        params.topMargin = top;
        params.bottomMargin = bottom;
        return params;
    }

    private View divider() {
        View line = new View(this);
        line.setBackgroundColor(getColor(R.color.nirwana_divider));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, NirwanaUi.dp(this, 1));
        params.leftMargin = NirwanaUi.dp(this, 16);
        params.rightMargin = NirwanaUi.dp(this, 16);
        line.setLayoutParams(params);
        return line;
    }

    private View rowView(NirwanaPages.Row row) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setGravity(Gravity.CENTER_VERTICAL);
        int pad = NirwanaUi.dp(this, 16);
        item.setPadding(pad, NirwanaUi.dp(this, 14), pad, NirwanaUi.dp(this, 14));

        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.addView(NirwanaUi.text(this, getString(row.titleRes), 16,
                getColor(R.color.nirwana_text), false));
        if (row.summaryRes != 0) {
            labels.addView(NirwanaUi.text(this, getString(row.summaryRes), 13,
                    getColor(R.color.nirwana_text_secondary), false));
        }
        item.addView(labels, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        switch (row.type) {
            case NirwanaPages.TYPE_SWITCH:
                return attachSwitch(item, row);
            case NirwanaPages.TYPE_CHOICE:
                return attachChoice(item, row);
            case NirwanaPages.TYPE_INFO:
                item.addView(NirwanaUi.text(this, infoValue(row.target), 14,
                        getColor(R.color.nirwana_text_secondary), false));
                return item;
            case NirwanaPages.TYPE_ACTION:
                return attachChevron(item, () -> showUnavailable());
            case NirwanaPages.TYPE_NAV:
            default:
                return attachChevron(item, () -> openTarget(row.target));
        }
    }

    private View attachSwitch(LinearLayout item, NirwanaPages.Row row) {
        int accent = getColor(R.color.nirwana_accent);
        int off = getColor(R.color.nirwana_text_secondary);
        Switch toggle = new Switch(this);
        boolean bound = NirwanaSystemBridge.isBound(row.key);
        toggle.setChecked(bound
                ? NirwanaSystemBridge.read(this, row.key, row.defaultOn)
                : NirwanaPrefs.getBoolean(this, row.key, row.defaultOn));
        toggle.setThumbTintList(new ColorStateList(
                new int[][] {{android.R.attr.state_checked}, {}},
                new int[] {accent, off}));
        toggle.setTrackTintList(new ColorStateList(
                new int[][] {{android.R.attr.state_checked}, {}},
                new int[] {withAlpha(accent, 0x80), getColor(R.color.nirwana_track_off)}));
        toggle.setOnCheckedChangeListener((button, checked) -> {
            NirwanaPrefs.setBoolean(this, row.key, checked);
            if (bound) {
                NirwanaSystemBridge.write(this, row.key, checked);
            }
        });
        item.addView(toggle);
        item.setOnClickListener(v -> toggle.toggle());
        return item;
    }

    private View attachChoice(LinearLayout item, NirwanaPages.Row row) {
        TextView value = NirwanaUi.text(this, choiceLabel(row), 14,
                getColor(R.color.nirwana_accent), false);
        value.setPadding(NirwanaUi.dp(this, 8), 0, 0, 0);
        item.addView(value);
        item.setOnClickListener(v -> {
            String[] options = getResources().getStringArray(row.optionsRes);
            int next = (NirwanaPrefs.getInt(this, row.key, 0) + 1) % options.length;
            NirwanaPrefs.setInt(this, row.key, next);
            value.setText(options[next]);
        });
        return item;
    }

    private String choiceLabel(NirwanaPages.Row row) {
        String[] options = getResources().getStringArray(row.optionsRes);
        int index = NirwanaPrefs.getInt(this, row.key, 0);
        if (index < 0 || index >= options.length) {
            index = 0;
        }
        return options[index];
    }

    private View attachChevron(LinearLayout item, Runnable onClick) {
        TextView chevron = NirwanaUi.text(this, "›", 22,
                getColor(R.color.nirwana_text_secondary), false);
        item.addView(chevron);
        item.setOnClickListener(v -> onClick.run());
        return item;
    }

    private void openTarget(String target) {
        if (target.startsWith(NirwanaPages.PREFIX_PAGE)) {
            Intent intent = new Intent(this, NirwanaSubpageActivity.class);
            intent.putExtra(EXTRA_PAGE, target.substring(NirwanaPages.PREFIX_PAGE.length()));
            startActivity(intent);
            return;
        }
        try {
            startActivity(new Intent(target.substring(NirwanaPages.PREFIX_INTENT.length())));
        } catch (ActivityNotFoundException e) {
            showUnavailable();
        }
    }

    private void showUnavailable() {
        Toast.makeText(this, R.string.nirwana_unavailable, Toast.LENGTH_SHORT).show();
    }

    private String infoValue(String key) {
        switch (key) {
            case "build_number":
                return Build.DISPLAY;
            case "android_version":
                return "Android " + Build.VERSION.RELEASE;
            case "kernel":
                return System.getProperty("os.version", "");
            case "hardware":
                return Build.HARDWARE;
            case "uptime":
                long minutes = TimeUnit.MILLISECONDS.toMinutes(SystemClock.elapsedRealtime());
                return getString(R.string.nirwana_uptime_value, minutes / 60, minutes % 60);
            case "storage_total":
                return getString(R.string.nirwana_gb_value, bytesToGb(dataStat().getTotalBytes()));
            default:
                return "";
        }
    }

    private View buildHero() {
        switch (mPage.hero) {
            case NirwanaPages.HERO_LOCKSCREEN:
                return NirwanaUi.lockscreenPreview(this);
            case NirwanaPages.HERO_ABOUT:
                return aboutHero();
            case NirwanaPages.HERO_BATTERY:
                return batteryHero();
            case NirwanaPages.HERO_STORAGE:
                return storageHero();
            default:
                return new View(this);
        }
    }

    private LinearLayout heroCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setBackground(NirwanaUi.heroBackground(this, 24));
        int pad = NirwanaUi.dp(this, 24);
        card.setPadding(pad, pad, pad, pad);
        return card;
    }

    private View aboutHero() {
        LinearLayout card = heroCard();
        card.addView(NirwanaUi.text(this, Build.MODEL, 24,
                getColor(R.color.nirwana_accent), true));
        card.addView(NirwanaUi.text(this, Build.MANUFACTURER + " · " + Build.DEVICE, 14,
                getColor(R.color.nirwana_text_secondary), false));
        return card;
    }

    private View batteryHero() {
        BatteryManager manager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
        int level = manager == null
                ? 0 : manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        return ringHero(level / 100f, level + "%", getString(R.string.nirwana_tile_battery));
    }

    private View storageHero() {
        StatFs stat = dataStat();
        long total = stat.getTotalBytes();
        long free = stat.getAvailableBytes();
        float used = total == 0 ? 0f : (total - free) / (float) total;
        return ringHero(used, getString(R.string.nirwana_gb_value, bytesToGb(free)),
                getString(R.string.nirwana_available));
    }

    private View ringHero(float progress, String centreValue, String centreLabel) {
        LinearLayout card = heroCard();
        int size = NirwanaUi.dp(this, 150);

        FrameLayout ringWrap = new FrameLayout(this);
        NirwanaUi.RingView ring = new NirwanaUi.RingView(this,
                getColor(R.color.nirwana_track_off), getColor(R.color.nirwana_accent), 10);
        ring.setProgress(progress);
        ringWrap.addView(ring, new FrameLayout.LayoutParams(size, size, Gravity.CENTER));

        LinearLayout centre = new LinearLayout(this);
        centre.setOrientation(LinearLayout.VERTICAL);
        centre.setGravity(Gravity.CENTER);
        centre.addView(NirwanaUi.text(this, centreValue, 26,
                getColor(R.color.nirwana_accent), true));
        centre.addView(NirwanaUi.text(this, centreLabel, 13,
                getColor(R.color.nirwana_text_secondary), false));
        ringWrap.addView(centre, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER));

        card.addView(ringWrap, new LinearLayout.LayoutParams(size, size));
        return card;
    }

    private StatFs dataStat() {
        return new StatFs(Environment.getDataDirectory().getPath());
    }

    private static long bytesToGb(long bytes) {
        return bytes / (1L << 30);
    }

    private static int withAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }
}
