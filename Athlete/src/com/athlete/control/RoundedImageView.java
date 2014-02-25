package com.athlete.control;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.athlete.R;
import com.athlete.util.CommonHelper;

/**
 * Created by edBaev. Date: 30.07.12 Time: 14:01
 */
public class RoundedImageView extends ImageView {
	private float mCornerRadius;
	private float mSizeEndW;
	private float mSizeEndH;
	private float mSizeStartW = 0;
	private float mSizeStartH = 0;
	private Context mContext;

	public RoundedImageView(Context context) {
		super(context);
		mContext = context;
	}

	public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

	public RoundedImageView(Context context, AttributeSet attributes) {
		super(context, attributes);
		mContext = context;
		TypedArray array = context.obtainStyledAttributes(attributes,
				R.styleable.RoundedImageView);
		if (array != null) {
			mCornerRadius = array.getDimension(
					R.styleable.RoundedImageView_corner_radius, 0);
			array.recycle();
		}
	}

	public void setCornerRadius(float cornerRadius, float sizeEndW,
			float sizeEndH, float sizeStartW, float sizeStartH) {
		this.mCornerRadius = cornerRadius;
		this.mSizeEndW = sizeEndW;
		this.mSizeEndH = sizeEndH;
		this.mSizeStartW = sizeStartW;
		this.mSizeStartH = sizeStartH;
	}

	public void setCornerRadius(float cornerRadius, float sizeW) {
		this.setCornerRadius(cornerRadius, sizeW, sizeW, 0, 0);

	}

	@Override
	protected void onDraw(Canvas canvas) {
		Drawable drawable = getDrawable();
		if (drawable instanceof BitmapDrawable && mCornerRadius > 0) {
			Paint paint = ((BitmapDrawable) drawable).getPaint();
			final int color = 0xff000000;
			if (mSizeEndW == 0) {
				mSizeEndW = CommonHelper.getPX(97, mContext);
				mSizeEndH = mSizeEndW;
			}
			final RectF rectF = new RectF(mSizeStartW, mSizeStartH, mSizeEndW,
					mSizeEndH);
			int saveCount = canvas.saveLayer(rectF, null,
					Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG
							| Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
							| Canvas.FULL_COLOR_LAYER_SAVE_FLAG
							| Canvas.CLIP_TO_LAYER_SAVE_FLAG);

			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(color);
			canvas.drawRoundRect(rectF, mCornerRadius, mCornerRadius, paint);

			Xfermode oldMode = paint.getXfermode();
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
			super.onDraw(canvas);
			paint.setXfermode(oldMode);
			canvas.restoreToCount(saveCount);
		} else {
			super.onDraw(canvas);
		}
	}

}