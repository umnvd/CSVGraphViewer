package com.umnvd.sensetestapp.views.plot;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.umnvd.sensetestapp.R;
import com.umnvd.sensetestapp.models.DataPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlotView extends View {

    private float textSize;
    private int axesColor;
    private int gridColor;
    private int graphColor;
    private int textColor;

    private final List<PlotPoint> graphPoints = new ArrayList<>();
    private final List<PlotPoint> xAxisPoints = new ArrayList<>();
    private final List<PlotPoint> yAxisPoints = new ArrayList<>();

    private final RectF plotRect = new RectF();
    private float stepX;
    private float stepY;

    private float maxXTextWidth;
    private float maxYTextWidth;
    private float textHeight;
    private float textCenterDeviation;

    private int xGridStepMultiplier = 1;
    private int yGridStepMultiplier = 1;

    private int xAxisShift = 0;
    private int yAxisShift = 0;

    private float translationX = 0f;
    private float translationY = 0f;
    private float scale = 1f;

    private final Paint axesPaint = new Paint();
    private final Paint gridPaint = new Paint();
    private final Paint graphPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final int defaultGridStep =
            getContext().getResources().getDimensionPixelSize(R.dimen.default_grid_step);

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
        setUpPlot(points);
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth, measuredHeight;

        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            int paddings = getPaddingLeft() + getPaddingRight();
            int stepsCount = Math.max(xAxisPoints.size(), 1);
            measuredWidth = (defaultGridStep * stepsCount) + paddings;
        } else {
            measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        }

        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            int paddings = getPaddingTop() + getPaddingBottom();
            int stepsCount = Math.max(yAxisPoints.size(), 1);
            measuredHeight = (defaultGridStep * stepsCount) + paddings;
        } else {
            measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        }

        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        float plotLeftBottomPadding = Math.max((maxYTextWidth * 2f), (textHeight * 2f));
        float plotTopPadding = textHeight / 1.5f;
        float plotRightPadding = maxXTextWidth / 1.5f;

        plotRect.set(
                getPaddingLeft() + plotLeftBottomPadding,
                getPaddingTop() + plotTopPadding,
                w - getPaddingRight() - plotRightPadding,
                h - getPaddingBottom() - plotLeftBottomPadding
        );

        stepX = plotRect.width() / Math.max((xAxisPoints.size() - 1), 1);
        stepY = plotRect.height() / Math.max((yAxisPoints.size() - 1), 1);

        updatePlot();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawGrid(canvas);
        drawGraph(canvas);
        drawAxesAndMarks(canvas);
    }

    private void initAttributes(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = getContext().obtainStyledAttributes(
                attrs, R.styleable.PlotView, defStyleAttr, defStyleRes
        );

        textSize = typedArray.getDimension(R.styleable.PlotView_textSize, 14);
        axesColor = typedArray.getColor(R.styleable.PlotView_axesColor, Color.BLACK);
        gridColor = typedArray.getColor(R.styleable.PlotView_gridColor, Color.LTGRAY);
        graphColor = typedArray.getColor(R.styleable.PlotView_graphColor, Color.BLUE);
        textColor = typedArray.getColor(R.styleable.PlotView_textColor, Color.BLACK);

        typedArray.recycle();
    }

    private void configurePaints() {
        axesPaint.setColor(axesColor);
        axesPaint.setStyle(Paint.Style.STROKE);
        axesPaint.setStrokeWidth(8);

        gridPaint.setColor(gridColor);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(4);

        graphPaint.setColor(graphColor);
        graphPaint.setStyle(Paint.Style.STROKE);
        graphPaint.setStrokeWidth(8);

        textPaint.setColor(textColor);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(textSize);
    }

    private void setUpPlot(List<DataPoint> originalDataPoints) {
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
        textHeight = fontMetrics.descent - fontMetrics.ascent;
        textCenterDeviation = (fontMetrics.descent + fontMetrics.ascent) / 2f;
    }

    private void updatePlot() {
        for (PlotPoint graphPoint : graphPoints) graphPoint.update();
        for (PlotPoint xAxisPoint : xAxisPoints) xAxisPoint.update();
        for (PlotPoint yAxisPoint : yAxisPoints) yAxisPoint.update();
        updateGridStepsMultipliers();
    }

    private void updateGridStepsMultipliers() {
        xGridStepMultiplier = 1;
        while (true) {
            if (stepX * scale * xGridStepMultiplier > maxXTextWidth * 1.25f) break;
            else xGridStepMultiplier++;
        }

        yGridStepMultiplier = 1;
        while (true) {
            if (stepY * scale * yGridStepMultiplier > textHeight * 1.25f) break;
            else yGridStepMultiplier++;
        }
    }

    private void drawGrid(Canvas canvas) {
        for (int i = 0; i < xAxisPoints.size(); i += xGridStepMultiplier) {
            PlotPoint point = xAxisPoints.get(i);
            if (point.isVisible) {
                canvas.drawLine(point.x, plotRect.top, point.x, plotRect.bottom, gridPaint);
            }
        }

        for (int i = 0; i < yAxisPoints.size(); i += yGridStepMultiplier) {
            PlotPoint point = yAxisPoints.get(i);
            if (point.isVisible) {
                canvas.drawLine(plotRect.left, point.y, plotRect.right, point.y, gridPaint);
            }
        }
    }

    private void drawGraph(Canvas canvas) {
        for (int i = 1; i < graphPoints.size(); i++) {
            PlotPoint fromPoint = graphPoints.get(i - 1);
            PlotPoint toPoint = graphPoints.get(i);
            if (fromPoint.isVisible || toPoint.isVisible) {
                canvas.drawLine(fromPoint.x, fromPoint.y, toPoint.x, toPoint.y, graphPaint);
            }
        }
    }

    private void drawAxesAndMarks(Canvas canvas) {
        canvas.drawRect(plotRect, axesPaint);

        for (int i = 0; i < xAxisPoints.size(); i += xGridStepMultiplier) {
            PlotPoint point = xAxisPoints.get(i);
            if (point.isVisible) {
                canvas.drawLine(
                        point.x,
                        plotRect.bottom - textHeight / 2f,
                        point.x,
                        plotRect.bottom + textHeight / 2f,
                        axesPaint
                );

                textPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(
                        String.valueOf(point.originalX),
                        point.x,
                        plotRect.bottom + textHeight * 2,
                        textPaint
                );
            }
        }

        for (int i = 0; i < yAxisPoints.size(); i += yGridStepMultiplier) {
            PlotPoint point = yAxisPoints.get(i);
            if (point.isVisible) {
                canvas.drawLine(
                        plotRect.left - textHeight / 2f,
                        point.y,
                        plotRect.left + textHeight / 2f,
                        point.y,
                        axesPaint
                );

                textPaint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(
                        String.valueOf(point.originalY),
                        plotRect.left - maxYTextWidth,
                        point.y - textCenterDeviation,
                        textPaint
                );
            }
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
            x = plotRect.left + ((originalX - xAxisShift) * stepX * scale) + translationX;
            y = plotRect.bottom - ((originalY - yAxisShift) * stepY * scale) + translationY;
            isVisible = x >= plotRect.left && x <= plotRect.right
                    && y >= plotRect.top && y <= plotRect.bottom;
        }

    }

}
