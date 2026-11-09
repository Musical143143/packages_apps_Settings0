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
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.settings.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Shared view helpers for the Nirwana Engine screens. */
final class NirwanaUi {

    private NirwanaUi() {
    }

    static int dp(Context context, int value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                context.getResources().getDisplayMetrics()));
    }

    static GradientDrawable roundedBackground(Context context, int color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(color);
        drawable.setCornerRadius(dp(context, radiusDp));
        return drawable;
    }

    static GradientDrawable heroBackground(Context context, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {
                        context.getColor(R.color.nirwana_gradient_start),
                        context.getColor(R.color.nirwana_gradient_end)});
        drawable.setCornerRadius(dp(context, radiusDp));
        return drawable;
    }

    static TextView text(Context context, CharSequence value, float sp, int color, boolean bold) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        view.setTextColor(color);
        if (bold) {
            view.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        return view;
    }

    static LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    /** Colours the status and navigation bars to match the Nirwana background. */
    static void styleWindow(Activity activity) {
        int background = activity.getColor(R.color.nirwana_background);
        activity.getWindow().setStatusBarColor(background);
        activity.getWindow().setNavigationBarColor(background);
    }

    /** Large clock card shown at the top of the hub and the lockscreen page. */
    static View lockscreenPreview(Context context) {
        FrameLayout card = new FrameLayout(context);
        card.setBackground(heroBackground(context, 24));
        card.setPadding(dp(context, 24), dp(context, 32), dp(context, 24), dp(context, 32));

        LinearLayout column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.CENTER_HORIZONTAL);

        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        String date = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(new Date());
        column.addView(text(context, time, 64, context.getColor(R.color.nirwana_accent), true));
        column.addView(text(context, date, 14,
                context.getColor(R.color.nirwana_text_secondary), false));

        card.addView(column, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER));
        return card;
    }

    /** Circular progress ring used on the battery and storage pages. */
    static final class RingView extends View {
        private final Paint mTrack = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint mFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF mBounds = new RectF();
        private float mProgress;

        RingView(Context context, int trackColor, int fillColor, int strokeDp) {
            super(context);
            float stroke = dp(context, strokeDp);
            mTrack.setStyle(Paint.Style.STROKE);
            mTrack.setStrokeWidth(stroke);
            mTrack.setStrokeCap(Paint.Cap.ROUND);
            mTrack.setColor(trackColor);
            mFill.setStyle(Paint.Style.STROKE);
            mFill.setStrokeWidth(stroke);
            mFill.setStrokeCap(Paint.Cap.ROUND);
            mFill.setColor(fillColor);
        }

        void setProgress(float progress) {
            mProgress = Math.max(0f, Math.min(1f, progress));
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float inset = mTrack.getStrokeWidth() / 2f;
            mBounds.set(inset, inset, getWidth() - inset, getHeight() - inset);
            canvas.drawArc(mBounds, -90f, 360f, false, mTrack);
            canvas.drawArc(mBounds, -90f, 360f * mProgress, false, mFill);
        }
    }
}
