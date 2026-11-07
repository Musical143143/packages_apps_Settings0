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
import android.graphics.drawable.LayerDrawable;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.util.TypedValue;
import android.view.Gravity;
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

import java.util.HashMap;
import java.util.Map;

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

    // Icon circle colour for each row, as in the mockup. Rows not listed use the default.
    private static final Map<String, Integer> ROW_COLOURS = new HashMap<>();
    // Subtitle text for each row in the card layout, as in the mockup.
    private static final Map<String, Integer> ROW_SUBTITLES = new HashMap<>();
    static {
        ROW_COLOURS.put("top_level_page_style", 0xFF8B5CF6);
        ROW_COLOURS.put("top_level_evolver", 0xFF8B5CF6);
        ROW_COLOURS.put("top_level_network", 0xFFF97316);
        ROW_COLOURS.put("top_level_connected_devices", 0xFFEC4899);
        ROW_COLOURS.put("top_level_apps", 0xFFEF4444);
        ROW_COLOURS.put("top_level_notifications", 0xFF6366F1);
        ROW_COLOURS.put("top_level_sound", 0xFF3B82F6);
        ROW_COLOURS.put("top_level_priority_modes", 0xFF8B5CF6);
        ROW_COLOURS.put("top_level_display", 0xFF22C55E);
        ROW_COLOURS.put("top_level_wallpaper", 0xFFF97316);
        ROW_COLOURS.put("top_level_system", 0xFF3B82F6);
        ROW_COLOURS.put("top_level_about_device", 0xFF8B5CF6);
        ROW_COLOURS.put("top_level_security", 0xFFF97316);
        ROW_COLOURS.put("top_level_location", 0xFFEC4899);
        ROW_COLOURS.put("top_level_accounts_and_backup", 0xFFEF4444);
        ROW_COLOURS.put("top_level_storage", 0xFF8B5CF6);
        ROW_COLOURS.put("top_level_battery", 0xFF22C55E);
        ROW_COLOURS.put("top_level_safety_center", 0xFF14B8A6);

        ROW_SUBTITLES.put("top_level_page_style", R.string.homepage_mock_page_style);
        ROW_SUBTITLES.put("top_level_evolver", R.string.homepage_mock_evolver);
        ROW_SUBTITLES.put("top_level_network", R.string.homepage_mock_network);
        ROW_SUBTITLES.put("top_level_connected_devices", R.string.homepage_mock_connected_devices);
        ROW_SUBTITLES.put("top_level_apps", R.string.homepage_mock_apps);
        ROW_SUBTITLES.put("top_level_notifications", R.string.homepage_mock_notifications);
        ROW_SUBTITLES.put("top_level_sound", R.string.homepage_mock_sound);
        ROW_SUBTITLES.put("top_level_priority_modes", R.string.homepage_mock_modes);
        ROW_SUBTITLES.put("top_level_display", R.string.homepage_mock_display);
        ROW_SUBTITLES.put("top_level_wallpaper", R.string.homepage_mock_wallpaper);
        ROW_SUBTITLES.put("top_level_system", R.string.homepage_mock_system);
        ROW_SUBTITLES.put("top_level_about_device", R.string.homepage_mock_about);
        ROW_SUBTITLES.put("top_level_security", R.string.homepage_mock_security);
        ROW_SUBTITLES.put("top_level_location", R.string.homepage_mock_location);
        ROW_SUBTITLES.put("top_level_accounts_and_backup", R.string.homepage_mock_accounts_backup);
    }
    private static final int DEFAULT_ROW_COLOUR = 0xFF6366F1;
    private static final int ICON_GLYPH_COLOUR = 0xFFFFFFFF;
    private static final int LIGHT_CARD_FILL = 0xF0F5F2FF;
    private static final int ACCOUNT_BANNER_START = 0xFFEDE7FF;
    private static final int ACCOUNT_BANNER_END = 0xFFD6C8FF;

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
        final boolean isRow = cards && pref != null && !(pref instanceof PreferenceCategory)
                && !(pref instanceof CardLayoutPreference);
        if (!isRow) {
            restoreCard(tile);
            return;
        }

        final boolean isAccount = pref.getParent() != null
                && ACCOUNT_CATEGORY_KEY.equals(pref.getParent().getKey());
        final boolean groupTop = !isRowAt(list, position - 1);
        final boolean groupBottom = !isRowAt(list, position + 1);
        final String rowKey = pref.getKey() == null ? "" : pref.getKey();
        final int colour = ROW_COLOURS.containsKey(rowKey)
                ? ROW_COLOURS.get(rowKey) : DEFAULT_ROW_COLOUR;
        final String key = groupTop + ":" + groupBottom + ":" + isAccount + ":" + colour
                + ":" + rowKey;

        final TextView subtitle = tile.findViewById(android.R.id.summary);
        final Integer subtitleRes = ROW_SUBTITLES.get(rowKey);
        if (subtitle != null && subtitleRes != null) {
            final String text = context.getString(subtitleRes);
            if (!text.contentEquals(subtitle.getText())) {
                subtitle.setText(text);
            }
        }

        final Object card = tile.getTag(R.id.homepage_style_card_tag);
        final Object cardKey = tile.getTag(R.id.homepage_style_card_key);
        if (card != null && tile.getBackground() == card && key.equals(cardKey)) {
            return;
        }

        if (card == null || tile.getBackground() != card) {
            tile.setTag(R.id.homepage_style_original_bg_tag, tile.getBackground());
        }
        final Drawable shapeDrawable = buildCardShape(context, groupTop, groupBottom, isAccount);
        final int text = Utils.getColorAttrDefaultColor(context, android.R.attr.textColorPrimary);
        final RippleDrawable ripple = new RippleDrawable(
                ColorStateList.valueOf(ColorUtils.setAlphaComponent(text, 0x1F)),
                withArtwork(context, pref, isAccount, shapeDrawable), null);
        tile.setBackground(ripple);
        tile.setTag(R.id.homepage_style_card_tag, ripple);
        final TextView title = tile.findViewById(android.R.id.title);
        if (title != null) {
            title.setTypeface(null, Typeface.BOLD);
        }
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
            circle.setColor(colour);
            icon.setBackground(circle);
            final int pad = Math.round(dp(context, 8));
            icon.setPadding(pad, pad, pad, pad);
            icon.setColorFilter(ICON_GLYPH_COLOUR, PorterDuff.Mode.SRC_IN);
            icon.setTag(R.id.homepage_style_card_tag, Boolean.TRUE);
        }
    }

    /**
     * Adds the artwork from the mockup to the rows that have it: the account banner, the Evolver
     * wave and the wallpaper thumbnail. Other rows keep the plain card.
     */
    private static Drawable withArtwork(Context context, Preference pref, boolean isAccount,
            Drawable shape) {
        final int artRes;
        final int width;
        final int height;
        final int gravity;
        if (isAccount) {
            artRes = R.drawable.card_mountain;
            width = 240;
            height = 96;
            gravity = Gravity.BOTTOM | Gravity.END;
        } else if ("top_level_evolver".equals(pref.getKey())) {
            artRes = R.drawable.card_wave;
            width = 160;
            height = 60;
            gravity = Gravity.BOTTOM | Gravity.END;
        } else if ("top_level_wallpaper".equals(pref.getKey())) {
            artRes = R.drawable.card_wallpaper_thumb;
            width = 64;
            height = 44;
            gravity = Gravity.CENTER_VERTICAL | Gravity.END;
        } else {
            return shape;
        }
        final LayerDrawable layers = new LayerDrawable(
                new Drawable[] {shape, context.getDrawable(artRes)});
        layers.setLayerWidth(1, Math.round(dp(context, width)));
        layers.setLayerHeight(1, Math.round(dp(context, height)));
        layers.setLayerGravity(1, gravity);
        if (artRes == R.drawable.card_wallpaper_thumb) {
            // Keep the thumbnail clear of the chevron on the right.
            layers.setLayerInsetEnd(1, Math.round(dp(context, 48)));
        }
        return layers;
    }

    private static GradientDrawable buildCardShape(Context context, boolean groupTop,
            boolean groupBottom, boolean isAccount) {
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
        final int surface = Utils.getColorAttrDefaultColor(context,
                android.R.attr.colorBackgroundFloating);
        // Light theme gets the lavender-white card from the mockup; dark keeps the surface.
        final boolean light = ColorUtils.calculateLuminance(surface) > 0.5;
        if (light && isAccount) {
            // The account row is the banner at the top of the mockup.
            shape.setColors(new int[] {ACCOUNT_BANNER_START, ACCOUNT_BANNER_END});
            shape.setOrientation(GradientDrawable.Orientation.TL_BR);
        } else {
            shape.setColor(light ? LIGHT_CARD_FILL : surface);
        }
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

        final TextView title = tile.findViewById(android.R.id.title);
        if (title != null) {
            title.setTypeface(null, Typeface.NORMAL);
        }

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
                && !(pref instanceof PreferenceGroup)
                && !(pref instanceof CardLayoutPreference);
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
