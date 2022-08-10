package com.cs380.blackjackgame.cardflipanimation;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

public class CardFlipRunnable implements Runnable {
	public static final boolean defaultInfiniteSpin = false;
	public static final boolean defaultFlipCounterClockwise = true;

	public static final float flipToBackDegreeAmount = 90;
	public static final float flipToFaceDegreeAmount = 270;

	@NonNull
	public final ImageView imageView;
	@NonNull
	private final Drawable fromCardImage;
	@NonNull
	private final Drawable toCardImage;
	public final boolean infiniteSpin;

	public boolean cancel = false;
	public final boolean flipCounterClockwise;

	public float rotationDegreesPerMilli;

	public CardFlipRunnable(@NonNull ImageView imageView, Drawable fromCardImage, Drawable toCardImage,
			int flipAnimationDurationMillis) {
		this(imageView, fromCardImage, toCardImage, flipAnimationDurationMillis, defaultInfiniteSpin);
	}

	public CardFlipRunnable(@NonNull ImageView imageView, @NonNull Drawable fromCardImage,
			@NonNull Drawable toCardImage, int flipAnimationDurationMillis, boolean flipCounterClockwise) {
		this(imageView, fromCardImage, toCardImage, flipAnimationDurationMillis, flipCounterClockwise,
				defaultInfiniteSpin);
	}

	public CardFlipRunnable(@NonNull ImageView imageView, @NonNull Drawable fromCardImage,
			@NonNull Drawable toCardImage, int flipAnimationDurationMillis, boolean flipCounterClockwise,
			boolean infiniteSpin) {
		this.imageView = imageView;
		this.fromCardImage = fromCardImage;
		this.toCardImage = toCardImage;
		this.rotationDegreesPerMilli = (flipCounterClockwise ? 1 : -1) * (float) 180 / flipAnimationDurationMillis;
		this.flipCounterClockwise = flipCounterClockwise;
		this.infiniteSpin = infiniteSpin;

		if (this.imageView.getScaleX() >= 0) {
			this.imageView.setScaleX(-1);
		}
	}

	@Override
	public void run() {
		if (this.cancel) {
			return;
		}

		imageView.setRotationY(imageView.getRotationY() + rotationDegreesPerMilli);

		float newYRotation = imageView.getRotationY();
		float absoluteNewYRotation = Math.abs(newYRotation);

		if (absoluteNewYRotation >= 360) {
			imageView.setRotationY(0);
		}

		if (!infiniteSpin && absoluteNewYRotation >= 180) {
			imageView.setRotationY(180);
			Log.i(this.toString(), "" + imageView.getRotationY());
			cancel = true;
			return;
		}

		if (absoluteNewYRotation >= flipToBackDegreeAmount && absoluteNewYRotation <= flipToFaceDegreeAmount) {
			imageView.setImageDrawable(toCardImage);
		} else if (absoluteNewYRotation < flipToBackDegreeAmount || absoluteNewYRotation > flipToFaceDegreeAmount) {
			imageView.setImageDrawable(fromCardImage);
		}
	}
}