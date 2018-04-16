package com.jimzjy.routercontroller.common

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.jimzjy.routercontroller.R
import com.jimzjy.routercontroller.status.ScrimView

/**
 *
 */
class DeviceRecyclerScrollerBehavior(ctx: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<ScrimView>(ctx, attrs) {
    private val mTopText = ctx.resources.getDimension(R.dimen.status_top_text_height)
    private val mTotalOffsetY = ctx.resources.getDimension(R.dimen.status_top_widget_height) - mTopText

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: ScrimView, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        return (axes and ViewCompat.SCROLL_AXIS_VERTICAL) != 0
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: ScrimView, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        val toCost = (dy > 0 && coordinatorLayout.scrollY < mTotalOffsetY)
                || (dy < 0 && coordinatorLayout.scrollY > 0 && !target.canScrollVertically(-1))

        if (toCost) {
            val offsetY = dy + coordinatorLayout.scrollY
            when {
                offsetY > mTotalOffsetY -> coordinatorLayout.scrollTo(0, mTotalOffsetY.toInt())
                offsetY < 0 -> coordinatorLayout.scrollTo(0, 0)
                else -> coordinatorLayout.scrollBy(0, dy)
            }
            child.alpha = coordinatorLayout.scrollY / (mTotalOffsetY - mTopText / 2)
            consumed[1] = dy
        }
    }

    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: ScrimView, target: View, type: Int) {
        val scrollY = coordinatorLayout.scrollY
        if (scrollY > mTotalOffsetY / 2) {
//            scrollStopAnimation(coordinatorLayout, child, scrollY, mTotalOffsetY.toInt())
        } else {
//            scrollStopAnimation(coordinatorLayout, child, scrollY, 0)
        }
    }

    private fun scrollStopAnimation(coordinatorLayout: CoordinatorLayout, child: ScrimView, scrollY: Int, scrollTo: Int) {
        val animator = ObjectAnimator.ofInt(coordinatorLayout, "scrollY", scrollY, scrollTo)
        animator.addUpdateListener {
            child.alpha = (it.animatedValue as Int) / (mTotalOffsetY - mTopText / 2)
        }
        animator.start()
    }
}

class MeasureRecyclerBehavior(ctx: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<RecyclerView>(ctx, attrs) {
    private val mTopText = ctx.resources.getDimension(R.dimen.status_top_text_height)

    override fun onMeasureChild(parent: CoordinatorLayout?, child: RecyclerView?, parentWidthMeasureSpec: Int, widthUsed: Int, parentHeightMeasureSpec: Int, heightUsed: Int): Boolean {
        child?.measure(parentWidthMeasureSpec, View.MeasureSpec.makeMeasureSpec(
                View.MeasureSpec.getSize(parentHeightMeasureSpec) - mTopText.toInt(),View.MeasureSpec.EXACTLY))
        return true
    }
}