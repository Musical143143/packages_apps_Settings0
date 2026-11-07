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
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.android.settings.R;
import com.android.settings.accounts.AvatarViewMixin;
import com.android.settings.homepage.SettingsHomepageActivity;
import com.android.settingslib.Utils;
import com.google.android.material.appbar.AppBarLayout;

import java.util.Random;

/**
 * Applies the "Page style" options to the Settings homepage: the greeting header, the search
 * bar position and the translucent bottom bar.
 */
public final class HomepageStyleController {

    private static final float BOTTOM_BAR_GLASS_ALPHA = 0.8f;

    private final Context mContext;
    private final CoordinatorLayout mRoot;
    private final NestedScrollView mScroll;
    private final AppBarLayout mAppBar;
    private final LinearLayout mAppBarContainer;
    private final LinearLayout mHomepageContainer;
    private final LinearLayout mHeader;
    private final TextView mGreeting;
    private final TextView mTitle;
    private final ImageView mAvatar;
    private final boolean mAvatarSupported;

    // Values picked once per homepage creation so the random options stay stable while resumed.
    private final String mRandomGreeting;
    private final int mRandomGreetingColour;
    private final int mRandomTitleColour;

    // Original placement of the search bar, restored whenever it returns to the top.
    private final CoordinatorLayout.LayoutParams mOriginalAppBarParams;
    @SuppressWarnings("rawtypes")
    private final CoordinatorLayout.Behavior mOriginalScrollBehavior;
    private boolean mBottomMode;

    public HomepageStyleController(SettingsHomepageActivity activity) {
        mContext = activity;
        mRoot = activity.findViewById(R.id.settings_homepage_container);
        mScroll = activity.findViewById(R.id.main_content_scrollable_container);
        mAppBar = activity.findViewById(R.id.app_bar);
        mAppBarContainer = activity.findViewById(R.id.app_bar_container);
        mHomepageContainer = activity.findViewById(R.id.homepage_container);
        mAvatarSupported = AvatarViewMixin.isAvatarSupported(activity);

        final Random random = new Random();
        final String[] greetings = mContext.getResources()
                .getStringArray(R.array.homepage_style_greetings);
        mRandomGreeting = greetings[random.nextInt(greetings.length)];
        mRandomGreetingColour = randomColour(random);
        mRandomTitleColour = randomColour(random);

        mOriginalAppBarParams = new CoordinatorLayout.LayoutParams(
                (CoordinatorLayout.LayoutParams) mAppBar.getLayoutParams());
        mOriginalScrollBehavior = ((CoordinatorLayout.LayoutParams)
                mScroll.getLayoutParams()).getBehavior();

        mHeader = new LinearLayout(activity);
        mHeader.setOrientation(LinearLayout.VERTICAL);
        mHeader.setPadding(dp(20), dp(12), dp(20), dp(8));

        mGreeting = new TextView(activity);
        mGreeting.setTextSize(TypedValue.COMPLEX_UNIT_SP, HomepageStyle.DEFAULT_GREETING_SIZE);
        mHeader.addView(mGreeting, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        final LinearLayout titleRow = new LinearLayout(activity);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        mTitle = new TextView(activity);
        mTitle.setTypeface(null, Typeface.BOLD);
        mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        titleRow.addView(mTitle, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        mAvatar = new ImageView(activity);
        mAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mAvatar.setClipToOutline(true);
        mAvatar.setOutlineProvider(ViewOutlineProvider.OVAL);
        titleRow.addView(mAvatar, new LinearLayout.LayoutParams(dp(40), dp(40)));

        mHeader.addView(titleRow, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        mHomepageContainer.addView(mHeader, 0, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Keep the scrolling content above the bottom bar when it is floating at the bottom.
        mAppBar.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> {
            if (mBottomMode) {
                updateScrollPadding(v.getHeight());
            }
        });
        mScroll.setClipToPadding(false);
    }

    /** Returns the avatar view so the activity can attach an {@link AvatarViewMixin} to it. */
    public ImageView getAvatarView() {
        return mAvatar;
    }

    /** Applies the current "Page style" options. Safe to call repeatedly. */
    public void apply() {
        applyHeader();
        applySearchBarPosition();
    }

    private void applyHeader() {
        final boolean showGreeting = HomepageStyle.isShowGreeting(mContext);
        final boolean showTitle = HomepageStyle.isShowTitle(mContext);
        final boolean showAvatar = mAvatarSupported && HomepageStyle.isShowAvatar(mContext);

        final int gravity = getAlignmentGravity(HomepageStyle.getGreetingAlignment(mContext));
        mGreeting.setGravity(gravity);
        mTitle.setGravity(gravity);
        mGreeting.setTextSize(TypedValue.COMPLEX_UNIT_SP,
                HomepageStyle.getGreetingSize(mContext));
        mGreeting.setText(HomepageStyle.isRandomGreetingWords(mContext)
                ? mRandomGreeting : getDefaultGreeting());
        mGreeting.setTextColor(HomepageStyle.isRandomGreetingColour(mContext)
                ? mRandomGreetingColour : defaultTextColour());
        mGreeting.setVisibility(showGreeting ? View.VISIBLE : View.GONE);

        mTitle.setText(HomepageStyle.getHeaderTitle(mContext));
        mTitle.setTextColor(HomepageStyle.isRandomTitleColour(mContext)
                ? mRandomTitleColour : defaultTextColour());
        mTitle.setVisibility(showTitle ? View.VISIBLE : View.GONE);

        mAvatar.setVisibility(showAvatar ? View.VISIBLE : View.GONE);

        final boolean anyVisible = showGreeting || showTitle || showAvatar;
        mHeader.setVisibility(anyVisible ? View.VISIBLE : View.GONE);
    }

    private void applySearchBarPosition() {
        final String position = HomepageStyle.getSearchBarPosition(mContext);
        final boolean inPage = HomepageStyle.POSITION_IN_PAGE.equals(position);
        final boolean bottom = HomepageStyle.POSITION_BOTTOM.equals(position);
        final boolean hidden = HomepageStyle.POSITION_HIDDEN.equals(position);
        // The bar sits above the scrolling content only in the top placement.
        final boolean barAtTop = !inPage && !bottom && !hidden;
        mBottomMode = bottom;

        if (inPage) {
            attachAppBarToHomepage();
        } else if (bottom) {
            attachAppBarToRoot(createBottomLayoutParams());
        } else {
            attachAppBarToRoot(new CoordinatorLayout.LayoutParams(mOriginalAppBarParams));
        }
        mAppBar.setVisibility(hidden ? View.GONE : View.VISIBLE);
        setScrollBehavior(barAtTop ? mOriginalScrollBehavior : null);

        final int topInset = getTopInset();
        final int containerTop = mContext.getResources()
                .getDimensionPixelSize(R.dimen.search_bar_container_top_padding);
        // The status bar inset is only needed by the search bar container when it sits at top.
        mAppBarContainer.setPadding(mAppBarContainer.getPaddingLeft(),
                barAtTop ? containerTop + topInset : containerTop,
                mAppBarContainer.getPaddingRight(), mAppBarContainer.getPaddingBottom());

        // Without a top bar the content must start below the status bar itself.
        mScroll.setPadding(mScroll.getPaddingLeft(), barAtTop ? 0 : topInset,
                mScroll.getPaddingRight(),
                bottom && !hidden ? mAppBar.getHeight() : 0);

        applyGlass(bottom && HomepageStyle.isGlassBottomBar(mContext));
        mRoot.requestLayout();
    }

    private void attachAppBarToRoot(CoordinatorLayout.LayoutParams params) {
        if (mAppBar.getParent() != mRoot) {
            detachAppBar();
            mRoot.addView(mAppBar, params);
        } else {
            mAppBar.setLayoutParams(params);
        }
    }

    private void attachAppBarToHomepage() {
        if (mAppBar.getParent() != mHomepageContainer) {
            detachAppBar();
            // Right below the header, which is the first child of the homepage container.
            mHomepageContainer.addView(mAppBar, 1, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    private void detachAppBar() {
        final ViewGroup parent = (ViewGroup) mAppBar.getParent();
        if (parent != null) {
            parent.removeView(mAppBar);
        }
    }

    private CoordinatorLayout.LayoutParams createBottomLayoutParams() {
        final CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM;
        return params;
    }

    private void setScrollBehavior(CoordinatorLayout.Behavior behavior) {
        final CoordinatorLayout.LayoutParams params =
                (CoordinatorLayout.LayoutParams) mScroll.getLayoutParams();
        if (params.getBehavior() != behavior) {
            params.setBehavior(behavior);
            mScroll.setLayoutParams(params);
        }
    }

    private void updateScrollPadding(int bottom) {
        mScroll.setPadding(mScroll.getPaddingLeft(), mScroll.getPaddingTop(),
                mScroll.getPaddingRight(), bottom);
    }

    private void applyGlass(boolean glass) {
        final int background = Utils.getColorAttrDefaultColor(mContext,
                android.R.attr.colorBackground);
        mAppBarContainer.setBackgroundColor(glass
                ? ColorUtils.setAlphaComponent(background,
                (int) (255 * BOTTOM_BAR_GLASS_ALPHA))
                : background);
    }

    private int getTopInset() {
        final WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(mRoot);
        return insets == null ? 0 : insets.getInsets(WindowInsetsCompat.Type.systemBars()
                | WindowInsetsCompat.Type.displayCutout()).top;
    }

    private String getDefaultGreeting() {
        return mContext.getResources().getStringArray(R.array.homepage_style_greetings)[0];
    }

    private int defaultTextColour() {
        return Utils.getColorAttrDefaultColor(mContext, android.R.attr.textColorPrimary);
    }

    private static int getAlignmentGravity(String alignment) {
        switch (alignment) {
            case HomepageStyle.ALIGN_CENTER:
                return Gravity.CENTER_HORIZONTAL;
            case HomepageStyle.ALIGN_RIGHT:
                return Gravity.END;
            default:
                return Gravity.START;
        }
    }

    private static int randomColour(Random random) {
        final float[] hsv = {random.nextInt(360), 0.75f, 0.9f};
        return Color.HSVToColor(hsv);
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                mContext.getResources().getDisplayMetrics()));
    }
}
