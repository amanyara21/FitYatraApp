package com.aman.fityatraapp.utils

import android.graphics.Canvas
import android.graphics.Paint
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.renderer.BarChartRenderer
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler

class RoundedBarChartRenderer(
    chart: BarDataProvider?,
    animator: ChartAnimator?,
    viewPortHandler: ViewPortHandler?
) : BarChartRenderer(chart, animator, viewPortHandler) {

    private val mShadowBuffers = FloatArray(4)

    private val mBarShadowPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        mBarShadowPaint.style = Paint.Style.FILL
    }

    override fun drawDataSet(c: Canvas, dataSet: IBarDataSet, index: Int) {
        val trans = mChart.getTransformer(dataSet.axisDependency)

        mBarBorderPaint.color = dataSet.barBorderColor
        mBarBorderPaint.strokeWidth = Utils.convertDpToPixel(dataSet.barBorderWidth)

        val roundedBarBuffer = mBarBuffers[index]
        roundedBarBuffer.setPhases(1f, 1f)
        roundedBarBuffer.setDataSet(index)
        roundedBarBuffer.setInverted(mChart.isInverted(dataSet.axisDependency))
        roundedBarBuffer.setBarWidth(mChart.barData.barWidth)
        roundedBarBuffer.feed(dataSet)

        trans.pointValuesToPixel(roundedBarBuffer.buffer)

        // draw the bar shadow before the values
        if (mChart.isDrawBarShadowEnabled) {
            for (j in 0 until roundedBarBuffer.size()) {
                val left = roundedBarBuffer.buffer[j]
                val right = roundedBarBuffer.buffer[j + 2]
                mShadowBuffers[0] = left
                mShadowBuffers[2] = right
                trans.pointValuesToPixel(mShadowBuffers)

                val x = (mShadowBuffers[0] + mShadowBuffers[2]) / 2f

                mBarShadowPaint.color = dataSet.barShadowColor
                c.drawRoundRect(
                    left,
                    mViewPortHandler.contentTop(),
                    right,
                    mViewPortHandler.contentBottom(),
                    15f,
                    15f,
                    mBarShadowPaint
                )
            }
        }

        // if multiple colors
        if (dataSet is BarDataSet && dataSet.colors.size > 1) {
            for (j in 0 until roundedBarBuffer.size() step 4) {
                if (!mViewPortHandler.isInBoundsLeft(roundedBarBuffer.buffer[j + 2])) {
                    continue
                }

                if (!mViewPortHandler.isInBoundsRight(roundedBarBuffer.buffer[j])) {
                    break
                }

                val color = dataSet.getColor(j / 4)
                mRenderPaint.color = color
                c.drawRoundRect(
                    roundedBarBuffer.buffer[j],
                    roundedBarBuffer.buffer[j + 1],
                    roundedBarBuffer.buffer[j + 2],
                    roundedBarBuffer.buffer[j + 3],
                    15f,
                    15f,
                    mRenderPaint
                )
            }
        } else { // single color
            mRenderPaint.color = dataSet.color
            for (j in 0 until roundedBarBuffer.size() step 4) {
                if (!mViewPortHandler.isInBoundsLeft(roundedBarBuffer.buffer[j + 2])) {
                    continue
                }

                if (!mViewPortHandler.isInBoundsRight(roundedBarBuffer.buffer[j])) {
                    break
                }

                c.drawRoundRect(
                    roundedBarBuffer.buffer[j],
                    roundedBarBuffer.buffer[j + 1],
                    roundedBarBuffer.buffer[j + 2],
                    roundedBarBuffer.buffer[j + 3],
                    15f,
                    15f,
                    mRenderPaint
                )
            }
        }
    }
}
