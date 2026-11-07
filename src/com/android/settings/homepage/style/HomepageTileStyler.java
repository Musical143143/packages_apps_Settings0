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
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settings.R;
import com.android.settingslib.Utils;

/**
 * Applies the "Page style" tile options (spacing, elevation, title size, summaries) to the rows
 * of the homepage list. Each option is only written when it differs, so this is cheap to run on
 * every frame and keeps rows correct after they are recycled or rebound.
 */
public final class HomepageTileStyler {

    private HomepageTileStyler() {
    }

    /** Applies the current tile options to every row currently laid out in {@code list}. */
    public static void applyToChildren(RecyclerView list) {
        final Context context = list.getContext();
        final float spacing = dp(context, getSpacingDp(HomepageStyle.getTileSpacing(context)));
        final float elevation = dp(context,
                getElevationDp(HomepageStyle.getTileElevation(context)));
        final float titleSp = getTitleSizeSp(HomepageStyle.getTileTitleSize(context));
        final boolean showSummaries = HomepageStyle.isTileShowSummaries(context);
        final boolean cards = HomepageStyle.isCardLayout(context);

        for (int i = 0; i < list.getChildCount(); i++) {
            final View tile = list.getChildAt(i);
            applyToTile(tile, context, spacing, elevation, titleSp, showSummaries);
            applyCardStyle(tile, context, cards);
        }
    }

    private static void applyToTile(View tile, Context context, float spacing, float elevation,
            float titleSp, boolean showSummaries) {
        final int spacingPx = Math.round(spacing);
        final ViewGroup.LayoutParams params = tile.getLayoutParams();
        if (params instanceof ViewGroup.MarginLayoutParams) {
            final ViewGroup.MarginLayoutParams margins = (ViewGroup.MarginLayoutParams) params;
            if (margins.topMargin != spacingPx || margins.bottomMargin != spacingPx) {
                margins.topMargin = spacingPx;
                margins.bottomMargin = spacingPx;
                tile.setLayoutParams(margins);
            }
        }

        if (tile.getElevation() != elevation) {
            tile.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
            tile.setElevation(elevation);
        }

        final TextView title = tile.findViewById(android.R.id.title);
        if (title != null) {
            final float targetPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, titleSp,
                    context.getResources().getDisplayMetrics());
            if (Math.abs(title.getTextSize() - targetPx) > 0.5f) {
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, titleSp);
            }
        }

        final View summary = tile.findViewById(android.R.id.summary);
        if (summary != null) {
            final int visibility = showSummaries ? View.VISIBLE : View.GONE;
            if (summary.getVisibility() != visibility) {
                summary.setVisibility(visibility);
            }
        }
    }

    /**
     * Turns a row into a rounded card when card layout is on, and restores its original
     * background when it is off. Rows that are not preferences (such as untitled categories)
     * are left alone.
     */
    private static void applyCardStyle(View tile, Context context, boolean cards) {
        final Object card = tile.getTag(R.id.homepage_style_card_tag);
        final boolean isRow = tile.findViewById(R.id.icon_frame) != null;

        if (!cards || !isRow) {
            if (card != null && tile.getBackground() == card) {
                tile.setBackground((Drawable) tile.getTag(R.id.homepage_style_original_bg_tag));
            }
            if (card != null) {
                tile.setTag(R.id.homepage_style_card_tag, null);
                tile.setTag(R.id.homepage_style_original_bg_tag, null);
            }
            return;
        }

        // Already styled and not rebound since, so nothing to do.
        if (card != null && tile.getBackground() == card) {
            return;
        }

        final float radius = dp(context, 20);
        final GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(radius);
        // Floating-surface colour follows the system light or dark theme.
        final int surface = Utils.getColorAttrDefaultColor(context,
                android.R.attr.colorBackgroundFloating);
        shape.setColor(ColorUtils.setAlphaComponent(surface, 0xC8));
        final int text = Utils.getColorAttrDefaultColor(context, android.R.attr.textColorPrimary);
        shape.setStroke(Math.round(dp(context, 1)), ColorUtils.setAlphaComponent(text, 0x1F));

        final RippleDrawable ripple = new RippleDrawable(
                ColorStateList.valueOf(ColorUtils.setAlphaComponent(text, 0x1F)), shape, null);
        tile.setTag(R.id.homepage_style_original_bg_tag, tile.getBackground());
        tile.setTag(R.id.homepage_style_card_tag, ripple);
        tile.setBackground(ripple);
    }

    private static int getSpacingDp(String spacing) {
        switch (spacing) {
            case HomepageStyle.SPACING_COMPACT:
                return 0;
            case HomepageStyle.SPACING_COMFORTABLE:
                return 4;
            default:
                return 8;
        }
    }

    private static int getElevationDp(String elevation) {
        switch (elevation) {
            case HomepageStyle.ELEVATION_FLAT:
                return 0;
            case HomepageStyle.ELEVATION_RAISED:
                return 2;
            default:
                return 6;
        }
    }

    private static float getTitleSizeSp(String size) {
        switch (size) {
            case HomepageStyle.TITLE_SIZE_SMALL:
                return 14f;
            case HomepageStyle.TITLE_SIZE_LARGE:
                return 18f;
            default:
                return 16f;
        }
    }

    private static float dp(Context context, int value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                context.getResources().getDisplayMetrics());
    }
}
