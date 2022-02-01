package com.umnvd.sensetestapp.views.plot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.umnvd.sensetestapp.R;
import com.umnvd.sensetestapp.models.DataPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlotView extends View {

    private static final String SUPER_STATE_KEY = "superState";
    private static final String SCALE_KEY = "scale";
    private static final String TRANSLATION_X_KEY = "translationX";
    private static final String TRANSLATION_Y_KEY = "translationY";
    private static final float SCALE_STEP = 0.25f;

    private float textSize;
    private int axesColor;
    private int gridColor;
    private int graphColor;
    private int textColor;
    private float axesWidth;
    private float gridWidth;
    private float graphWidth;

    private final List<PlotPoint> graphPoints = new ArrayList<>();
    private final List<PlotPoint> xAxisPoints = new ArrayList<>();
    private final List<PlotPoint> yAxisPoints = new ArrayList<>();

    private final RectF plotRect = new RectF();
    private float stepX;
    private float stepY;

    private float maxXTextWidth;
    private float maxYTextWidth;
    private float textCenterDeviation;

    private int xGridStepMultiplier = 1;
    private int yGridStepMultiplier = 1;

    private int xAxisShift = 0;
    private int yAxisShift = 0;

    private final ScaleGestureDetector scaleGestureDetector =
            new ScaleGestureDetector(getContext(), new ScaleListener());
    private final PointF lastEventPoint = new PointF();
    private int lastPointerId;

    private float scale = 1f;
    private float maxScale = 1f;
    private float translationX = 0f;
    private float translationY = 0f;

    private final Paint axesPaint = new Paint();
    private final Paint gridPaint = new Paint();
    private final Paint graphPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final float maxGridStep;

    public PlotView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        maxGridStep = context.getResources().getDisplayMetrics().density * 100f;
        initAttributes(attrs, defStyleAttr, defStyleRes);
        configurePaints();
    }

    public PlotView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.PlotViewDefaultStyle);
    }

    public PlotView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.plotViewStyle);
    }

    public PlotView(Context context) {
        this(context, null);
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(SUPER_STATE_KEY, super.onSaveInstanceState());
        bundle.putFloat(SCALE_KEY, scale);
        bundle.putFloat(TRANSLATION_X_KEY, translationX);
        bundle.putFloat(TRANSLATION_Y_KEY, translationY);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            state = bundle.getParcelable(SUPER_STATE_KEY);
            scale = bundle.getFloat(SCALE_KEY);
            translationX = bundle.getFloat(TRANSLATION_X_KEY);
            translationY = bundle.getFloat(TRANSLATION_Y_KEY);
            Log.d("PlotView", "tx " + translationX + ", ty " + translationY + ", scale " + scale);
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth, measuredHeight;

        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            int paddings = getPaddingLeft() + getPaddingRight();
            int stepsCount = Math.max(xAxisPoints.size(), 1);
            measuredWidth = ((int) (maxGridStep * stepsCount)) + paddings;
        } else {
            measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        }

        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            int paddings = getPaddingTop() + getPaddingBottom();
            int stepsCount = Math.max(yAxisPoints.size(), 1);
            measuredHeight = ((int) (maxGridStep * stepsCount)) + paddings;
        } else {
            measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        }

        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        float plotLeftPadding = (textSize / 2f) + maxYTextWidth;
        float plotTopPadding = textSize / 1.5f;
        float plotRightPadding = maxXTextWidth / 1.5f;
        float plotBottomPadding = (textSize / 2f) + textSize;

        plotRect.set(
                getPaddingLeft() + plotLeftPadding,
                getPaddingTop() + plotTopPadding,
                w - getPaddingRight() - plotRightPadding,
                h - getPaddingBottom() - plotBottomPadding
        );

        stepX = plotRect.width() / Math.max((xAxisPoints.size() - 1), 1);
        stepY = plotRect.height() / Math.max((yAxisPoints.size() - 1), 1);
        maxScale = maxGridStep / Math.min(stepX, stepY);

        restrictTranslations();
        recalculatePlot();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawGrid(canvas);
        drawGraph(canvas);
        drawAxesAndMarks(canvas);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event == null) return false;
        if (event.getPointerCount() > 1) return scaleGestureDetector.onTouchEvent(event);
        return processTranslation(event);
    }

    public void setPoints(@NonNull List<DataPoint> points) {
        setUpPlotPoints(points);
        requestLayout();
        invalidate();
    }

    private void initAttributes(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = getContext().obtainStyledAttributes(
                attrs, R.styleable.PlotView, defStyleAttr, defStyleRes
        );

        textSize = typedArray.getDimension(R.styleable.PlotView_textSize, 48f);
        axesColor = typedArray.getColor(R.styleable.PlotView_axesColor, Color.BLACK);
        gridColor = typedArray.getColor(R.styleable.PlotView_gridColor, Color.LTGRAY);
        graphColor = typedArray.getColor(R.styleable.PlotView_graphColor, Color.BLUE);
        textColor = typedArray.getColor(R.styleable.PlotView_textColor, Color.BLACK);
        axesWidth = typedArray.getDimension(R.styleable.PlotView_axesWidth, 8f);
        gridWidth = typedArray.getDimension(R.styleable.PlotView_gridWidth, 4f);
        graphWidth = typedArray.getDimension(R.styleable.PlotView_graphWidth, 8f);

        typedArray.recycle();
    }

    private void configurePaints() {
        axesPaint.setColor(axesColor);
        axesPaint.setStyle(Paint.Style.STROKE);
        axesPaint.setStrokeWidth(axesWidth);

        gridPaint.setColor(gridColor);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(gridWidth);

        graphPaint.setColor(graphColor);
        graphPaint.setStyle(Paint.Style.STROKE);
        graphPaint.setStrokeWidth(graphWidth);

        textPaint.setColor(textColor);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(textSize);
    }

    private void setUpPlotPoints(List<DataPoint> originalDataPoints) {
        ArrayList<DataPoint> dataPoints = new ArrayList<>(originalDataPoints);
        Collections.sort(dataPoints);

        DataPoint minAxesPoint = getMinAxesPoint(dataPoints);
        DataPoint maxAxesPoint = getMaxAxesPoint(dataPoints);
        xAxisShift = minAxesPoint.x;
        yAxisShift = minAxesPoint.y;

        setUpGraphPoints(dataPoints);
        setUpAxesPoints(minAxesPoint, maxAxesPoint);
        calculateTextSizes(minAxesPoint, maxAxesPoint);
    }

    private DataPoint getMinAxesPoint(List<DataPoint> dataPoints) {
        int minX = dataPoints.get(0).x;
        int minY = dataPoints.get(0).y;
        for (DataPoint point : dataPoints) {
            if (point.y < minY) minY = point.y;
        }
        return new DataPoint(minX, minY);
    }

    private DataPoint getMaxAxesPoint(List<DataPoint> dataPoints) {
        int maxX = dataPoints.get(dataPoints.size() - 1).x;
        int maxY = dataPoints.get(0).y;
        for (DataPoint point : dataPoints) {
            if (point.y > maxY) maxY = point.y;
        }
        return new DataPoint(maxX, maxY);
    }

    private void setUpAxesPoints(DataPoint minAxesPoint, DataPoint maxAxesPoint) {
        xAxisPoints.clear();
        for (int x = minAxesPoint.x; x <= maxAxesPoint.x; x++) {
            xAxisPoints.add(new PlotPoint(x, yAxisShift));
        }

        yAxisPoints.clear();
        for (int y = minAxesPoint.y; y <= maxAxesPoint.y; y++) {
            yAxisPoints.add(new PlotPoint(xAxisShift, y));
        }
    }

    private void setUpGraphPoints(List<DataPoint> dataPoints) {
        graphPoints.clear();
        for (DataPoint dataPoint : dataPoints) graphPoints.add(new PlotPoint(dataPoint));
    }

    private void calculateTextSizes(DataPoint minAxesPoint, DataPoint maxAxesPoint) {
        String minXText = String.valueOf(minAxesPoint.x);
        String maxXText = String.valueOf(maxAxesPoint.x);
        maxXTextWidth = Math.max(textPaint.measureText(minXText), textPaint.measureText(maxXText));

        String minYText = String.valueOf(minAxesPoint.y);
        String maxYText = String.valueOf(maxAxesPoint.y);
        maxYTextWidth = Math.max(textPaint.measureText(minYText), textPaint.measureText(maxYText));

        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        textCenterDeviation = (fontMetrics.descent + fontMetrics.ascent) / 2f;
    }

    private void updatePlot() {
        recalculatePlot();
        invalidate();
    }

    private void recalculatePlot() {
        for (PlotPoint graphPoint : graphPoints) graphPoint.recalculate();
        for (PlotPoint xAxisPoint : xAxisPoints) xAxisPoint.recalculate();
        for (PlotPoint yAxisPoint : yAxisPoints) yAxisPoint.recalculate();
        recalculateGridStepsMultipliers();
    }

    private void recalculateGridStepsMultipliers() {
        xGridStepMultiplier = 1;
        while (true) {
            if (stepX * scale * xGridStepMultiplier > maxXTextWidth * 1.25f) break;
            else xGridStepMultiplier++;
        }

        yGridStepMultiplier = 1;
        while (true) {
            if (stepY * scale * yGridStepMultiplier > textSize * 1.25f) break;
            else yGridStepMultiplier++;
        }
    }

    private void drawGrid(Canvas canvas) {
        for (int i = 0; i < xAxisPoints.size(); i += xGridStepMultiplier) {
            PlotPoint point = xAxisPoints.get(i);
            if (isLineOnPlot(point.x, plotRect.top, point.x, plotRect.bottom)) {
                canvas.drawLine(point.x, plotRect.top, point.x, plotRect.bottom, gridPaint);
            }
        }

        for (int i = 0; i < yAxisPoints.size(); i += yGridStepMultiplier) {
            PlotPoint point = yAxisPoints.get(i);
            if (isLineOnPlot(plotRect.left, point.y, plotRect.right, point.y)) {
                canvas.drawLine(plotRect.left, point.y, plotRect.right, point.y, gridPaint);
            }
        }
    }

    private void drawGraph(Canvas canvas) {
        int clipRestoreCount = canvas.save();
        canvas.clipRect(plotRect);

        for (int i = 1; i < graphPoints.size(); i++) {
            PlotPoint fromPoint = graphPoints.get(i - 1);
            PlotPoint toPoint = graphPoints.get(i);
            if (isLineOnPlot(fromPoint.x, fromPoint.y, toPoint.x, toPoint.y)) {
                canvas.drawLine(fromPoint.x, fromPoint.y, toPoint.x, toPoint.y, graphPaint);
            }
        }

        canvas.restoreToCount(clipRestoreCount);
    }

    private void drawAxesAndMarks(Canvas canvas) {
        canvas.drawRect(plotRect, axesPaint);

        for (int i = 0; i < xAxisPoints.size(); i += xGridStepMultiplier) {
            PlotPoint point = xAxisPoints.get(i);
            float markStartY = plotRect.bottom - textSize / 2f;
            float markStopY = plotRect.bottom + textSize / 2f;
            if (isLineOnPlot(point.x, markStartY, point.x, markStopY)) {
                canvas.drawLine(point.x, markStartY, point.x, markStopY, axesPaint);
                textPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(
                        String.valueOf(point.originalX),
                        point.x,
                        plotRect.bottom + textSize - textCenterDeviation,
                        textPaint
                );
            }
        }

        for (int i = 0; i < yAxisPoints.size(); i += yGridStepMultiplier) {
            PlotPoint point = yAxisPoints.get(i);
            float markStartX = plotRect.left - textSize / 2f;
            float markStopX = plotRect.left + textSize / 2f;
            if (isLineOnPlot(markStartX, point.y, markStopX, point.y)) {
                canvas.drawLine(markStartX, point.y, markStopX, point.y, axesPaint);
                textPaint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(
                        String.valueOf(point.originalY),
                        plotRect.left - textSize / 2f,
                        point.y - textCenterDeviation,
                        textPaint
                );
            }
        }
    }

    private boolean isLineOnPlot(float x1, float y1, float x2, float y2) {
        return plotRect.left <= Math.max(x1, x2)
                && Math.min(x1, x2) <= plotRect.right
                && plotRect.top < Math.max(y1, y2)
                && Math.min(y1, y2) < plotRect.bottom;
    }

    private boolean processTranslation(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            lastEventPoint.set(event.getX(), event.getY());
            lastPointerId = event.getPointerId(0);
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (scale > 1f) {
                int pointerId = event.getPointerId(0);

                if (pointerId == lastPointerId) {
                    translationX += event.getX() - lastEventPoint.x;
                    translationY += event.getY() - lastEventPoint.y;
                    restrictTranslations();
                    updatePlot();
                }

                lastEventPoint.set(event.getX(), event.getY());
                lastPointerId = event.getPointerId(0);
                return true;
            }
            return false;
        }

        return false;
    }

    private void restrictTranslations() {
        translationX = coerceIn(
                plotRect.right - plotRect.right * scale,
                translationX,
                plotRect.left * scale - plotRect.left
        );
        translationY = coerceIn(
                plotRect.top - plotRect.top * scale,
                translationY,
                plotRect.bottom * scale - plotRect.bottom
        );
    }

    private float coerceIn(float minValue, float value,float maxValue) {
        return Math.max(Math.min(maxValue, value), minValue);
    }

    private class PlotPoint {

        public final int originalX;
        public final int originalY;

        public float x;
        public float y;

        public PlotPoint(int originalX, int originalY) {
            this.originalX = originalX;
            this.originalY = originalY;
            recalculate();
        }

        public PlotPoint(DataPoint dataPoint) {
            this(dataPoint.x, dataPoint.y);
        }

        public void recalculate() {
            x = plotRect.left + ((originalX - xAxisShift) * stepX * scale) + translationX;
            y = plotRect.bottom - ((originalY - yAxisShift) * stepY * scale) + translationY;
        }

    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        private final PointF focus = new PointF();

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            focus.set(detector.getFocusX(), detector.getFocusY());
            return plotRect.contains(focus.x, focus.y);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (detector == null) return false;
            if (detector.getScaleFactor() < SCALE_STEP) return true;

            float prevScale = scale;
            float prevTranslationX = translationX;
            float prevTranslationY = translationY;

            scale = coerceIn(1f, scale * detector.getScaleFactor(), maxScale);

            translationX = (focus.x - plotRect.left)
                    + (prevTranslationX + plotRect.left - focus.x) * scale / prevScale;

            translationY = (focus.y - plotRect.bottom)
                    + (prevTranslationY + plotRect.bottom - focus.y) * scale / prevScale;

            restrictTranslations();
            updatePlot();
            return true;
        }

    }

}
