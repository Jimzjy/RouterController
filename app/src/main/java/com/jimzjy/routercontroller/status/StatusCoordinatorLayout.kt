package com.jimzjy.routercontroller.status

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import com.jimzjy.routercontroller.R

/**
 *
 */
class StatusCoordinatorLayout(ctx: Context, attrs: AttributeSet?) : CoordinatorLayout(ctx, attrs) {
    private val mTopText = ctx.resources.getDimension(R.dimen.status_top_text_height)
    private val mTop = ctx.resources.getDimension(R.dimen.status_top_widget_height)
    private val mTopOffsetY = mTop - mTopText
    private val mAlphaChangeStart = mTopOffsetY - (mTopText / 2)
    private var mShowText: ((alpha: Float) -> Unit)? = null

    override fun scrollTo(x: Int, y: Int) {
        var sY = y
        when {
            sY < 0 -> sY = 0
            sY > mTopOffsetY -> sY = mTopOffsetY.toInt()
        }
        if (sY != scrollY) super.scrollTo(x, sY)
        when {
            sY > mAlphaChangeStart -> mShowText?.invoke((sY - mAlphaChangeStart) / (mTopText / 2))
            sY <= mAlphaChangeStart -> mShowText?.invoke(0f)
        }
    }

    fun setShowText(showText: (alpha: Float) -> Unit) {
        this.mShowText = showText
    }
}