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
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.settings.R;

/** Nirwana Engine hub: a clock preview above a grid of settings tiles. */
public class NirwanaEngineActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NirwanaUi.styleWindow(this);
        setContentView(buildContent());
    }

    private View buildContent() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(getColor(R.color.nirwana_background));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int side = NirwanaUi.dp(this, 16);
        root.setPadding(side, NirwanaUi.dp(this, 24), side, NirwanaUi.dp(this, 32));

        TextView title = NirwanaUi.text(this, getString(R.string.nirwana_engine_title), 32,
                getColor(R.color.nirwana_accent), true);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(title, NirwanaUi.matchWrap());

        TextView subtitle = NirwanaUi.text(this, getString(R.string.nirwana_engine_subtitle), 14,
                getColor(R.color.nirwana_text_secondary), false);
        subtitle.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(subtitle, NirwanaUi.matchWrap());

        View preview = NirwanaUi.lockscreenPreview(this);
        preview.setOnClickListener(v -> openPage(NirwanaPages.PAGE_LOCKSCREEN));
        root.addView(preview, block(NirwanaUi.dp(this, 24)));

        java.util.List<String> tiles = NirwanaPages.HUB_TILES;
        for (int i = 0; i < tiles.size(); i += 2) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.addView(tile(tiles.get(i)), tileParams());
            if (i + 1 < tiles.size()) {
                row.addView(tile(tiles.get(i + 1)), tileParams());
            } else {
                row.addView(new View(this), tileParams());
            }
            root.addView(row, NirwanaUi.matchWrap());
        }

        root.addView(tile(NirwanaPages.PAGE_ABOUT), block(NirwanaUi.dp(this, 6)));

        LinearLayout pair = new LinearLayout(this);
        pair.setOrientation(LinearLayout.HORIZONTAL);
        pair.addView(tile(NirwanaPages.PAGE_BATTERY), tileParams());
        pair.addView(tile(NirwanaPages.PAGE_STORAGE), tileParams());
        root.addView(pair, NirwanaUi.matchWrap());

        scroll.addView(root);
        return scroll;
    }

    private View tile(String pageKey) {
        NirwanaPages.Page page = NirwanaPages.get(pageKey);
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER_VERTICAL);
        int pad = NirwanaUi.dp(this, 18);
        tile.setPadding(pad, pad, pad, pad);
        tile.setMinimumHeight(NirwanaUi.dp(this, 96));
        tile.setBackground(NirwanaUi.roundedBackground(this,
                getColor(R.color.nirwana_surface), 22));
        tile.addView(NirwanaUi.text(this, getString(page.titleRes), 17,
                getColor(R.color.nirwana_text), true));
        tile.setOnClickListener(v -> openPage(pageKey));
        return tile;
    }

    private LinearLayout.LayoutParams tileParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        int margin = NirwanaUi.dp(this, 6);
        params.setMargins(margin, margin, margin, margin);
        return params;
    }

    private LinearLayout.LayoutParams block(int top) {
        LinearLayout.LayoutParams params = NirwanaUi.matchWrap();
        params.topMargin = top;
        params.bottomMargin = NirwanaUi.dp(this, 6);
        return params;
    }

    private void openPage(String pageKey) {
        Intent intent = new Intent(this, NirwanaSubpageActivity.class);
        intent.putExtra(NirwanaSubpageActivity.EXTRA_PAGE, pageKey);
        startActivity(intent);
    }
}
