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
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceGroupAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settings.R;
import com.android.settingslib.Utils;

/**
 * Applies the "Page style" tile options to the rows of the homepage list: spacing, elevation,
 * title size, summaries, and the optional card layout. Each option is only written when it
 * differs, so this is cheap to run on every frame and keeps rows correct after recycling.
 */
public final class HomepageTileStyler {

    private static final String ACCOUNT_CATEGORY_KEY = "top_level_account_category";
    private static final int CORNER_TOP = 1;
    private static final int CORNER_BOTTOM = 2;
    private static final int CARD_CORNER_DP = 20;

    // Pastel icon circles used by the card layout, cycled by row position.
    private static final int[] ICON_COLOURS = {
        0xFF5BC8F0, 0xFFF58AD6, 0xFF5ED47A, 0xFFF9A96B,
        0xFFF5C518, 0xFFB58CF5, 0xFF3CC8C8, 0xFFF48C8C,
    };
    private static final int ICON_GLYPH_COLOUR = 0xFF1C1B1F;

    private HomepageTileStyler() {
    }

    /** Applies the current tile options to every row currently laid out in {@code list}. */
    public static void applyToChildren(RecyclerView list) {
        final Context context = list.getContext();
        final boolean cards = HomepageStyle.isCardLayout(context);
        // In card layout the margins are set per card, so spacing is skipped (-1).
        final float spacing = cards ? -1f
                : dp(context, getSpacingDp(HomepageStyle.getTileSpacing(context)));
        final float elevation = dp(context,
                getElevationDp(HomepageStyle.getTileElevation(context)));
        final float titleSp = getTitleSizeSp(HomepageStyle.getTileTitleSize(context));
        final boolean showSummaries = HomepageStyle.isTileShowSummaries(context);

        for (int i = 0; i < list.getChildCount(); i++) {
            final View tile = list.getChildAt(i);
            applyToTile(tile, context, spacing, elevation, titleSp, showSummaries);
            applyCardStyle(tile, context, cards, list);
        }
    }

    private static void applyToTile(View tile, Context context, float spacing, float elevation,
            float titleSp, boolean showSummaries) {
        if (spacing >= 0) {
            final int spacingPx = Math.round(spacing);
            final ViewGroup.LayoutParams params = tile.getLayoutParams();
            if (params instanceof ViewGroup.MarginLayoutParams) {
                final ViewGroup.MarginLayoutParams margins =
                        (ViewGroup.MarginLayoutParams) params;
                if (margins.topMargin != spacingPx || margins.bottomMargin != spacingPx) {
                    margins.topMargin = spacingPx;
                    margins.bottomMargin = spacingPx;
                    tile.setLayoutParams(margins);
                }
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
     * In card layout, each preference row becomes a rounded card. Consecutive rows in the same
     * group share one card with thin dividers, and the icon gets a coloured circle. Everything
     * is undone when the card layout is off or the row is not a preference row.
     */
    private static void applyCardStyle(View tile, Context context, boolean cards,
            RecyclerView list) {
        final int position = list.getChildAdapterPosition(tile);
        final Preference pref = getPreference(list, position);
        final boolean isRow = cards && pref != null && !(pref instanceof PreferenceCategory);
        if (!isRow) {
            restoreCard(tile);
            return;
        }

        final boolean isAccount = pref.getParent() != null
                && ACCOUNT_CATEGORY_KEY.equals(pref.getParent().getKey());
        final boolean groupTop = !isRowAt(list, position - 1);
        final boolean groupBottom = !isRowAt(list, position + 1);
        final int colourIndex = Math.abs(position) % ICON_COLOURS.length;
        final int key = (groupTop ? CORNER_TOP : 0) | (groupBottom ? CORNER_BOTTOM : 0)
                | (isAccount ? 4 : 0) | (colourIndex << 3);

        final Object card = tile.getTag(R.id.homepage_style_card_tag);
        final Object cardKey = tile.getTag(R.id.homepage_style_card_key);
        if (card != null && tile.getBackground() == card && Integer.valueOf(key).equals(cardKey)) {
            return;
        }

        if (card == null || tile.getBackground() != card) {
            tile.setTag(R.id.homepage_style_original_bg_tag, tile.getBackground());
        }
        final Drawable shapeDrawable = buildCardShape(context, groupTop, groupBottom);
        final int text = Utils.getColorAttrDefaultColor(context, android.R.attr.textColorPrimary);
        final RippleDrawable ripple = new RippleDrawable(
                ColorStateList.valueOf(ColorUtils.setAlphaComponent(text, 0x1F)),
                shapeDrawable, null);
        tile.setBackground(ripple);
        tile.setTag(R.id.homepage_style_card_tag, ripple);
        tile.setTag(R.id.homepage_style_card_key, key);

        // Rows inside a group touch each other; groups are separated by the spacing.
        final ViewGroup.LayoutParams params = tile.getLayoutParams();
        if (params instanceof ViewGroup.MarginLayoutParams) {
            final ViewGroup.MarginLayoutParams margins = (ViewGroup.MarginLayoutParams) params;
            final int top = Math.round(dp(context, groupTop ? 8 : 1));
            final int bottom = Math.round(dp(context, groupBottom ? 8 : 0));
            if (margins.topMargin != top || margins.bottomMargin != bottom) {
                margins.topMargin = top;
                margins.bottomMargin = bottom;
                tile.setLayoutParams(margins);
            }
        }

        final ImageView icon = tile.findViewById(android.R.id.icon);
        if (icon != null && !isAccount) {
            final GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(ICON_COLOURS[colourIndex]);
            icon.setBackground(circle);
            final int pad = Math.round(dp(context, 8));
            icon.setPadding(pad, pad, pad, pad);
            icon.setColorFilter(ICON_GLYPH_COLOUR, PorterDuff.Mode.SRC_IN);
            icon.setTag(R.id.homepage_style_card_tag, Boolean.TRUE);
        }
    }

    private static GradientDrawable buildCardShape(Context context, boolean groupTop,
            boolean groupBottom) {
        final float r = dp(context, CARD_CORNER_DP);
        final float[] radii = new float[8];
        if (groupTop) {
            radii[0] = r;
            radii[1] = r;
            radii[2] = r;
            radii[3] = r;
        }
        if (groupBottom) {
            radii[4] = r;
            radii[5] = r;
            radii[6] = r;
            radii[7] = r;
        }
        final GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadii(radii);
        // The floating-surface colour follows the system light or dark theme.
        shape.setColor(Utils.getColorAttrDefaultColor(context,
                android.R.attr.colorBackgroundFloating));
        final int text = Utils.getColorAttrDefaultColor(context, android.R.attr.textColorPrimary);
        shape.setStroke(Math.round(dp(context, 1)), ColorUtils.setAlphaComponent(text, 0x14));
        return shape;
    }

    /** Puts a row back to its original look once it is no longer a card. */
    private static void restoreCard(View tile) {
        final Object card = tile.getTag(R.id.homepage_style_card_tag);
        if (card == null) {
            return;
        }
        if (tile.getBackground() == card) {
            tile.setBackground((Drawable) tile.getTag(R.id.homepage_style_original_bg_tag));
        }
        tile.setTag(R.id.homepage_style_card_tag, null);
        tile.setTag(R.id.homepage_style_card_key, null);
        tile.setTag(R.id.homepage_style_original_bg_tag, null);

        final ImageView icon = tile.findViewById(android.R.id.icon);
        if (icon != null && icon.getTag(R.id.homepage_style_card_tag) != null) {
            icon.setBackground(null);
            icon.setPadding(0, 0, 0, 0);
            icon.clearColorFilter();
            icon.setTag(R.id.homepage_style_card_tag, null);
        }
    }

    private static Preference getPreference(RecyclerView list, int position) {
        if (position == RecyclerView.NO_POSITION
                || !(list.getAdapter() instanceof PreferenceGroupAdapter)) {
            return null;
        }
        final PreferenceGroupAdapter adapter = (PreferenceGroupAdapter) list.getAdapter();
        if (position < 0 || position >= adapter.getItemCount()) {
            return null;
        }
        return adapter.getItem(position);
    }

    private static boolean isRowAt(RecyclerView list, int position) {
        final Preference pref = getPreference(list, position);
        return pref != null && !(pref instanceof PreferenceCategory)
                && !(pref instanceof PreferenceGroup);
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

    private static float dp(Context context, float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                context.getResources().getDisplayMetrics());
    }
}
