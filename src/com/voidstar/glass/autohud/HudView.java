/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.voidstar.glass.autohud;

import com.voidstar.glass.autohud.util.MathUtils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Location;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Draws a stylized compass, with text labels at the cardinal and ordinal
 * directions, and tick marks at the half-winds. The red "needles" in the
 * display mark the current heading.
 */
public class HudView extends View {

	/** Various dimensions and other drawing-related constants. */
	private static final float DIRECTION_TEXT_HEIGHT = 120.0f;
	private static final float SUBTITLE_TEXT_HEIGHT = 40.0f;
	private static final float FUEL_STATS_TEXT_HEIGHT = 50.0f;
	//private static final float SMALL_ICONS_HEIGHT = 
	
	private final Paint mPaint;
	private final Paint mSubtitlePaint;
	private final Paint mFuelStatsPaint;
	
	private final Bitmap mFuelIcon;
	private final Bitmap mMpgIcon;

	private String mTachString = "0.0";
	private String mSpeedString = "0";
	private String mFuelString = "0%";
	private String mMpgString = "0.0";

	public HudView(Context context) {
		this(context, null, 0);
	}

	public HudView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HudView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(Color.WHITE);
		mPaint.setAntiAlias(true);
		mPaint.setTextSize(DIRECTION_TEXT_HEIGHT);
		mPaint.setTextAlign(Align.RIGHT);
		mPaint.setTypeface(Typeface.createFromFile(new File(
				"/system/glass_fonts", "Roboto-Thin.ttf")));

		mSubtitlePaint = new Paint();
		mSubtitlePaint.setStyle(Paint.Style.FILL);
		mSubtitlePaint.setAntiAlias(true);
		mSubtitlePaint.setTextSize(SUBTITLE_TEXT_HEIGHT);
		mSubtitlePaint.setTextAlign(Align.RIGHT);
		mSubtitlePaint.setColor(Color.GRAY);
		mSubtitlePaint.setTypeface(Typeface.createFromFile(new File(
				"/system/glass_fonts", "Roboto-Thin.ttf")));

		mFuelStatsPaint = new Paint();
		mFuelStatsPaint.setStyle(Paint.Style.FILL);
		mFuelStatsPaint.setAntiAlias(true);
		mFuelStatsPaint.setTextSize(FUEL_STATS_TEXT_HEIGHT);
		mFuelStatsPaint.setTextAlign(Align.RIGHT);
		mFuelStatsPaint.setColor(Color.LTGRAY);
		mFuelStatsPaint.setTypeface(Typeface.createFromFile(new File(
				"/system/glass_fonts", "Roboto-Thin.ttf")));

		mFuelIcon = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.ic_drop);
		//mFuelIcon = Bitmap.createScaledBitmap(mFuelIcon, dstWidth, dstHeight, filter)
		
		mMpgIcon = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.ic_fire);
		
		//mPlaceBitmap = BitmapFactory.decodeResource(context.getResources(),
		//		R.drawable.place_mark);
	}

	public void setTach(int newTach) {
		mTachString = String.format("%.1f", (newTach / 1000f));
	}

	public void setSpeed(int newSpeed) {
		mSpeedString = Integer.toString(newSpeed);
	}

	public void setFuel(int newFuel) {
		mFuelString = Integer.toString(newFuel) + "%";
	}

	public void setMpg(float newMpg) {
		mMpgString = String.format("%.1f", newMpg);
	}


	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawText(mTachString, 250, 160, mPaint);
		canvas.drawText(mSpeedString, 550, 160, mPaint);

		canvas.drawText("krpm", 250, 200, mSubtitlePaint);
		canvas.drawText("mph", 550, 200, mSubtitlePaint);

		canvas.drawText(mFuelString, 250, 310, mFuelStatsPaint);
		canvas.drawText(mMpgString, 550, 310, mFuelStatsPaint);
		
		canvas.drawBitmap(mFuelIcon, 85, 270, mSubtitlePaint);
		canvas.drawBitmap(mMpgIcon, 385, 270, mSubtitlePaint);
	}
}
