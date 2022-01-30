package com.umnvd.sensetestapp.views.plot;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.umnvd.sensetestapp.R;
import com.umnvd.sensetestapp.models.DataPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlotView extends View {

    private int axesPadding;
    private float textSize;
    private int axesColor;
    private int gridColor;
    private int graphColor;
    private int textColor;

    private int width;
    private int height;

    private int initialPlotWidth;
    private int initialPlotHeight;
    private float plotWidth;
    private float plotHeight;
    
    private float initialXStep;
    private float initialYStep;
    private float stepX;
    private float stepY;
    private int xAxisStepMultiplier = 1;
    private int yAxisStepMultiplier = 1;

    private float maxTextWidth;
    private float maxTextHeight;

    int xSegmentsCount = 1;
    int ySegmentsCount = 1;
    private final int defaultGridStep =
            getContext().getResources().getDimensionPixelSize(R.dimen.default_grid_step);

    private float translationX = 0;
    private float translationY = 0;
    private float scale = 1;
    
    private List<PlotPoint> graphPoints = new ArrayList<>();
    private List<PlotPoint> xAxisPoints = new ArrayList<>();
    private int xAxisShift = 0;
    private List<PlotPoint> yAxisPoints = new ArrayList<>();
    private int yAxisShift = 0;

    private final Paint axesPaint = new Paint();
    private final Paint gridPaint = new Paint();
    private final Paint graphPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public PlotView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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

    public void setPoints(@NonNull List<DataPoint> points) {
        Log.d("PlotView", "setPoints called");
        setUpPlot(points);
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d("PlotView", "onMeasure called");
        int measuredWidth, measuredHeight;

        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            int paddings = getPaddingLeft() + getPaddingRight();
            measuredWidth = (defaultGridStep * xSegmentsCount) + axesPadding + paddings;
        } else {
            measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        }

        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            int paddings = getPaddingTop() + getPaddingBottom();
            measuredHeight = (defaultGridStep * ySegmentsCount) + axesPadding + paddings;
        } else {
            measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        }
        
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        Log.d("PlotView", "onSizeChanged called");
        width = w;
        height = h;
        
        initialPlotWidth = width - axesPadding;
        initialPlotHeight = height - axesPadding;

        xSegmentsCount = Math.max((xAxisPoints.size() - 1), 1);
        initialXStep = ((float) initialPlotWidth) / xSegmentsCount;

        ySegmentsCount = Math.max((yAxisPoints.size() - 1), 1);
        initialYStep = ((float) initialPlotHeight) / ySegmentsCount;

        Log.d("PlotView", "onSizeChanged finished: " +
                "width=" + width +
                ", height=" + height +
                ", axesPadding=" + axesPadding +
                ", initialPlotWidth=" + initialPlotWidth +
                ", initialPlotHeight=" + initialPlotHeight +
                ", xSegmentsCount=" + xSegmentsCount +
                ", initialXStep=" + initialXStep +
                ", ySegmentsCount=" + ySegmentsCount +
                ", initialYStep=" + initialYStep +
                "");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        updatePlot();

        Log.d("PlotView", "onDraw called");
        drawGrid(canvas);
    }

    private void drawGrid(Canvas canvas) {
        Log.d("PlotView", "X size:" + xAxisPoints.size());
        for (PlotPoint point : xAxisPoints) {
            Log.d("PlotView", "X:" + xAxisPoints.toString());
            if (point.isVisible) {
                canvas.drawLine(
                        point.x,
                        0,
                        point.x,
                        initialPlotHeight,
                        gridPaint
                );
            }
        }

        Log.d("PlotView", "Y size:" + xAxisPoints.size());
        for (PlotPoint point : yAxisPoints) {
            Log.d("PlotView", "Y:" + yAxisPoints.toString());
            if (point.isVisible) {
                canvas.drawLine(
                        axesPadding,
                        point.y,
                        width,
                        point.y,
                        gridPaint
                );
            }
        }
    }

    private void initAttributes(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = getContext().obtainStyledAttributes(
                attrs, R.styleable.PlotView, defStyleAttr, defStyleRes
        );

        axesPadding = typedArray.getDimensionPixelSize(R.styleable.PlotView_axesPadding, 50);
        textSize = typedArray.getDimension(R.styleable.PlotView_textSize, 14);
        axesColor = typedArray.getColor(R.styleable.PlotView_axesColor, Color.BLACK);
        gridColor = typedArray.getColor(R.styleable.PlotView_gridColor, Color.GRAY);
        graphColor = typedArray.getColor(R.styleable.PlotView_graphColor, Color.BLUE);
        textColor = typedArray.getColor(R.styleable.PlotView_textColor, Color.BLACK);

        typedArray.recycle();
    }

    private void configurePaints() {
        axesPaint.setColor(axesColor);
        axesPaint.setStyle(Paint.Style.STROKE);
        axesPaint.setStrokeWidth(4);

        gridPaint.setColor(gridColor);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(4);

        graphPaint.setColor(graphColor);
        graphPaint.setStyle(Paint.Style.STROKE);
        graphPaint.setStrokeWidth(4);

        textPaint.setColor(textColor);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(textSize);
    }

    private void setUpPlot(List<DataPoint> originalDataPoints) {
        ArrayList<DataPoint> dataPoints = new ArrayList<>(originalDataPoints);
        Collections.sort(dataPoints);
        Log.d("PlotView", "setUpPlot dataPoints:" + dataPoints);

        DataPoint minAxesPoint = getMinAxesPoint(dataPoints);
        DataPoint maxAxesPoint = getMaxAxesPoint(dataPoints);
        xAxisShift = minAxesPoint.x;
        yAxisShift = minAxesPoint.y;

        Log.d("PlotView", "setUpPlot minPoint:" + minAxesPoint);
        Log.d("PlotView", "setUpPlot maxPoint:" + maxAxesPoint);

        setUpGraphPoints(dataPoints);
        setUpAxesPoints(minAxesPoint, maxAxesPoint);
        calculateTextSize(minAxesPoint, maxAxesPoint);
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

    private void calculateTextSize(DataPoint minAxesPoint, DataPoint maxAxesPoint) {
        String minXText = String.valueOf(minAxesPoint.x);
        String maxXText = String.valueOf(maxAxesPoint.x);
        maxTextWidth = Math.max(textPaint.measureText(minXText), textPaint.measureText(maxXText));

        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        maxTextHeight = fontMetrics.descent - fontMetrics.ascent;
    }

    private void updatePlot() {
        for (PlotPoint graphPoint : graphPoints) graphPoint.update();
        for (PlotPoint xAxisPoint : xAxisPoints) xAxisPoint.update();
        for (PlotPoint yAxisPoint : yAxisPoints) yAxisPoint.update();

        updateAxesStepsMultipliers();
    }

    private void updateAxesStepsMultipliers() {
        xAxisStepMultiplier = 1;
        while (true) {
            if (initialXStep * scale * xAxisStepMultiplier > maxTextWidth) break;
            else xAxisStepMultiplier++;
        }

        yAxisStepMultiplier = 1;
        while (true) {
            if (initialYStep * scale * yAxisStepMultiplier > maxTextHeight) break;
            else yAxisStepMultiplier++;
        }
    }

    public class PlotPoint {
        
        public final int originalX;
        public final int originalY;
        
        public float x;
        public float y;
        public boolean isVisible;

        public PlotPoint(int originalX, int originalY) {
            this.originalX = originalX;
            this.originalY = originalY;
            update();
        }

        public PlotPoint(DataPoint dataPoint) {
            this(dataPoint.x, dataPoint.y);
        }
        
        public void update() {
            x = axesPadding + ((originalX - xAxisShift) * initialXStep * scale) + translationX;
            y = height - ((originalY - yAxisShift) * initialYStep * scale) - axesPadding + translationY;
            isVisible = x >= axesPadding && x < width && y > 0 && y <= height - axesPadding;
        }

        @Override
        public String toString() {
            return "PlotPoint{" +
                    "originalX=" + originalX +
                    ", originalY=" + originalY +
                    ", x=" + x +
                    ", y=" + y +
                    ", isVisible=" + isVisible +
                    '}';
        }
    }

}
