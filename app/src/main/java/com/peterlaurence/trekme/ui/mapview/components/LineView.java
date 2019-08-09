package com.peterlaurence.trekme.ui.mapview.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.peterlaurence.mapview.ScaleChangeListener;


/**
 * A custom view that draws a line between two points and represents the distance
 * between them.
 *
 * @author peterLaurence on 21/06/17.
 */
public class LineView extends View implements ScaleChangeListener {
    private static final int DEFAULT_STROKE_COLOR = 0xCC311B92;
    private static final int DEFAULT_STROKE_WIDTH_DP = 4;
    private float mStrokeWidth;
    private Paint mPaint = new Paint();
    private float[] mLine = new float[4];

    private float mScale;

    public LineView(Context context, float scale) {
        this(context, scale, null);
    }

    public LineView(Context context, float scale, Integer color) {
        super(context);
        setWillNotDraw(false);
        mScale = scale;

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_STROKE_WIDTH_DP, metrics);

        if (color == null) {
            mPaint.setColor(DEFAULT_STROKE_COLOR);
        } else {
            mPaint.setColor(color);
        }

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void updateLine(float x1, float y1, float x2, float y2) {
        mLine[0] = x1;
        mLine[1] = y1;
        mLine[2] = x2;
        mLine[3] = y2;

        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.scale(mScale, mScale);
        mPaint.setStrokeWidth(mStrokeWidth / mScale);
        canvas.drawLine(mLine[0], mLine[1], mLine[2], mLine[3], mPaint);
        super.onDraw(canvas);
    }

    @Override
    public void onScaleChanged(float scale) {
        mScale = scale;
        invalidate();
    }
}
