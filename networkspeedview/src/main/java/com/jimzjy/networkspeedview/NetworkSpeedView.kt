package com.jimzjy.networkspeedview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import kotlin.math.cos
import kotlin.math.sin

/**
 *
 */
open class NetworkSpeedView : View {
    companion object {
        const val TEXT_TO_SPEED_UNIT_SIZE = 0.6f
    }
    /**
     * 主要文本的大小
     */
    private var _mainTextSize = 0f
    /**
     * 副文本的大小
     */
    private var _subTextSize = 0f

    /**
     * unit: sp
     * 用于外部设置 主要文本的大小, 单位: sp
     */
    open var mainTextSize
        get() = _mainTextSize
        set(value) {
            _mainTextSize = value * resources.displayMetrics.scaledDensity
            mMainSpeedUnitSize = _mainTextSize * TEXT_TO_SPEED_UNIT_SIZE
            textSizeCoefficient = floatArrayOf(_mainTextSize, _subTextSize)
        }
    /**
     * unit: sp
     * 用于外部设置 副文本的大小, 单位: sp
     */
    open var subTextSize
        get() = _subTextSize
        set(value) {
            _subTextSize = value * resources.displayMetrics.scaledDensity
            mSubSpeedUnitSize = _subTextSize * TEXT_TO_SPEED_UNIT_SIZE
            textSizeCoefficient = floatArrayOf(_mainTextSize, _subTextSize)
        }

    /**
     * view 的宽高
     */
    private var mWidth = 0f
    private var mHeight = 0f
    private var mHalfWidth = 0f
    private var mHeight2 = 0f
    private var mHeight4 = 0f

    /**
     * 中间线的 起始,结束,高度
     */
    private var mLineStartX = 0f
    private var mLineStopX = 0f
    private var mLineY = 0f

    /**
     * 速度单位
     * 0: B/s, 1: KB/s, 2: MB/s, 3: GB/s
     */
    private var speedUnit = 1
    /**
     * 速度单位的记录数组
     */
    private val mSpeedUnitArray = arrayOf("B/s", "KB/s", "MB/s", "GB/s")
    /**
     * 上传速度单位, 同 speedUnit
     */
    private var mUploadUnit = 0
    /**
     * 下载速度单位, 同 speedUnit
     */
    private var mDownloadUnit = 0

    /**
     * 主要字的 paint
     */
    private val mPaintMainText = Paint(Paint.ANTI_ALIAS_FLAG)
    /**
     * 副字的 paint
     */
    private val mPaintSubText = Paint(Paint.ANTI_ALIAS_FLAG)
    /**
     * 公共 paint, 用于: 指示速度的三角形 path, 中间线, 速度单位
     */
    private val mPaintCommon = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * 指示速度的三角形的 path
     */
    private val path = Path()

    /**
     * 速度单位绘制, X轴开始的位置
     * 次要和主要都是通过这个属性
     */
    private var mMainSpeedUnitStart = 0f
    /**
     * 主要速度单位大小
     * 当这个属性被修改, mainSpeedUnitStart属性 也会被修改为适合的值
     */
    private var mMainSpeedUnitSize = 0f
        set(value) {
            field = value
            mPaintMainText.textSize = value
            mMainSpeedUnitStart = mLineStopX - mPaintMainText.measureText(mSpeedUnitArray[mDownloadUnit])
            mPaintMainText.textSize = _mainTextSize
        }
    /**
     * 次要速度单位大小
     */
    private var mSubSpeedUnitSize = 0f

    /**
     * X, Y, radius
     * 主三角形的中心 X, Y, 半径
     * (三角形为正三角形, 通过中心点来算出三个顶点的坐标)
     */
    private val mMainTriangleCenter = arrayOf(0f, 0f, 0f)
    /**
     * X, Y, radius
     * 副三角形的中心 X, Y, 半径
     * (三角形为正三角形, 通过中心点来算出三个顶点的坐标)
     */
    private val mSubTriangleCenter = arrayOf(0f, 0f, 0f)
    /**
     * X0,Y0,X1,Y1,...
     * 记录 主,副三角形 的顶点坐标, 先主后副
     */
    private val mTriangleVertex = Array(12) { 0f }

    /**
     * unit: bytes
     * 记录速度的数组, 单位: bytes
     */
    private val mSpeedArray = floatArrayOf(0f, 0f)

    /**
     * 设置显示 下载/上传 速度时是否反转
     */
    var isReverseSpeed = false
    /**
     * 在 onDraw 中绘制的 主要文本
     */
    private val mMainText get() = if (isDownloadMain) String.format("%.1f", mSpeedArray[1]) else String.format("%.1f", mSpeedArray[0])
    /**
     * 在 onDraw 中绘制的 副文本
     */
    private val mSubText get() = if (isDownloadMain) String.format("%.1f", mSpeedArray[0]) else String.format("%.1f", mSpeedArray[1])

    /**
     * 主要文本 绘制起始位置
     */
    private val mainTextStart get() = mHalfWidth - mPaintMainText.measureText(mMainText) / 2
    /**
     * 次要文本 绘制起始位置
     */
    private val subTextStart get() = mHalfWidth - mPaintSubText.measureText(mSubText) / 2

    /**
     * 按下移动过程中的相对一开始按下点的 Y轴 偏移值
     */
    private var offsetY = 0f
    /**
     * 调整 xMainTextHeight, 改变主要文本显示位置
     */
    private val offsetY1 get() = if (offsetY < -mHeight2) -mainTextHeight + mHeight + mHeight2 else 0f
    /**
     * 主要文本默认高度
     */
    private var mainTextHeight = 0f
    /**
     * 次要文本默认高度
     */
    private var subTextHeight = 0f
    /**
     * 在移动中 主要文本高度
     */
    private val xMainTextHeight get() = mainTextHeight + offsetY + offsetY1
    /**
     * 在移动中 次要文本高度
     */
    private val xSubTextHeight get() = subTextHeight + offsetY
    /**
     * 按下位置 Y轴 记录值
     */
    private var touchDownY = 0f

    /**
     * set: 0: mainTextHeight, 1: subTextHeight
     * get: textSizeCoefficient[0] * (currentHeight / mHeight) + textSizeCoefficient[1] = textSize
     * 文本大小系数, 用于计算移动中文本大小
     */
    private var textSizeCoefficient = floatArrayOf(0f, 0f)
        set(value) {
            field[0] = (value[1] - value[0]) * 2.5f
            field[1] = 2 * value[0] - value[1]
        }
    /**
     * 当前主要文本大小, 由高度决定
     */
    private val currentMainTextSize
        get() = if (xMainTextHeight < mainTextHeight) getCurrentTextSize(1, xMainTextHeight)
        else getCurrentTextSize(0, xMainTextHeight)
    /**
     * 当前副文本大小, 由高度决定
     */
    private val currentSubTextSize
        get() = if (xSubTextHeight < mainTextHeight) getCurrentTextSize(1, xSubTextHeight)
        else getCurrentTextSize(0, xSubTextHeight)

    /**
     * 三角旋转动画
     */
    private val mRotateAnimator = ValueAnimator.ofFloat(0f, 60f)
    /**
     * 判断主要文本是否为 下载速度 , 用于决定 三角旋转动画 的正序或倒序
     */
    private var isDownloadMain = true

    /**
     * 是否加锁, 防止在移动中修改速度值
     */
    var isLocked: Boolean = false
        private set(value) {
            field = value
        }

    /**
     * 外部访问 速度
     */
    val speedArray = arrayOf("", "")

    constructor(ctx: Context) : super(ctx)

    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs) {
        val ta = ctx.obtainStyledAttributes(attrs, R.styleable.NetworkSpeedView)

        _mainTextSize = ta.getDimension(R.styleable.NetworkSpeedView_mainText_size, resources.getDimension(R.dimen.mainTextSize))
        _subTextSize = ta.getDimension(R.styleable.NetworkSpeedView_subText_size, resources.getDimension(R.dimen.subTextSize))
        speedUnit = ta.getInt(R.styleable.NetworkSpeedView_speed_unit, 1)

        ta.recycle()
        init()
    }

    private fun init() {
        //设置初始颜色
        mPaintMainText.setARGB(255, 255, 255, 255)
        mPaintSubText.setARGB(200, 255, 255, 255)
        mPaintCommon.setARGB(200, 255, 255, 255)

        //设置初始文本大小
        mPaintMainText.textSize = _mainTextSize
        mPaintSubText.textSize = _subTextSize

        //设置初始速度单位
        if (speedUnit <= 3) {
            mDownloadUnit = speedUnit
            mUploadUnit = mDownloadUnit
        }

        //计算并得到 文本大小系数
        textSizeCoefficient = floatArrayOf(_mainTextSize, _subTextSize)

        //设置 三角动画行为
        mRotateAnimator.addUpdateListener {
            try {
                setTrianglePath(it.animatedValue as Float)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (!isInEditMode) {
            mPaintMainText.textSize = currentMainTextSize
            mPaintSubText.textSize = currentSubTextSize
            //绘制主要文本
            canvas?.drawText(mMainText, mainTextStart, xMainTextHeight, mPaintMainText)
            //绘制次要文本
            canvas?.drawText(mSubText, subTextStart, xSubTextHeight, mPaintSubText)

            mPaintCommon.textSize = mMainSpeedUnitSize
            //绘制主要速度单位
            canvas?.drawText(mSpeedUnitArray[mDownloadUnit], mMainSpeedUnitStart, mainTextHeight, mPaintCommon)
            mPaintCommon.textSize = mSubSpeedUnitSize
            //绘制次要速度单位
            canvas?.drawText(mSpeedUnitArray[mUploadUnit], mMainSpeedUnitStart, subTextHeight, mPaintCommon)

            //绘制分割线
            canvas?.drawLine(mLineStartX, mLineY, mLineStopX, mLineY, mPaintCommon)

            //绘制 两个指示三角
            canvas?.drawPath(path, mPaintCommon)
        } else {
            setSpeedArray(floatArrayOf(51200f, 1024800f))

            mPaintMainText.textSize = _mainTextSize
            mPaintSubText.textSize = _subTextSize
            canvas?.drawText(mMainText, mainTextStart, mainTextHeight, mPaintMainText)
            canvas?.drawText(mSubText, subTextStart, subTextHeight, mPaintSubText)

            mPaintCommon.textSize = mMainSpeedUnitSize
            canvas?.drawText(mSpeedUnitArray[mDownloadUnit], mMainSpeedUnitStart, mainTextHeight, mPaintCommon)
            mPaintCommon.textSize = mSubSpeedUnitSize
            canvas?.drawText(mSpeedUnitArray[mUploadUnit], mMainSpeedUnitStart, subTextHeight, mPaintCommon)

            canvas?.drawLine(mLineStartX, mLineY, mLineStopX, mLineY, mPaintCommon)

            canvas?.drawPath(path, mPaintCommon)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val action = event?.action

        //没有设置 performClick , 所以设置 onClickListener 会没有效果
        //onTouchListener 需要 return false , 来调用 onTouchEvent
        when (action) {
        //移动中, 通过偏移值来改变文本透明度
            MotionEvent.ACTION_MOVE -> {
                if (touchDownY >= 0) {
                    offsetY = event.y - touchDownY
                    if (offsetY in -mHeight4..0f) {
                        setPaintAlpha(offsetY)
                        invalidate()
                    }
                }
            }
        //按下时记录 初始点 Y值
            MotionEvent.ACTION_DOWN -> {
                if (mRotateAnimator.isStarted) endAnimator()
                touchDownY = event.y
                isLocked = true
            }
        //抬起时 开始动画, 旋转 三角指示
            MotionEvent.ACTION_UP -> {
                if (mRotateAnimator.isStarted) endAnimator()
                if (offsetY < -mHeight * 0.25f) {
                    swapSpeed()
                    startRotateAnimator(isDownloadMain)
                }
                resetOnTouch()
            }
        //非人为取消, 结束动画
            MotionEvent.ACTION_CANCEL -> {
                resetOnTouch()
                if (mRotateAnimator.isStarted) endAnimator()
            }
        }
        return true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        //detach 时结束动画
        if (mRotateAnimator.isStarted) endAnimator()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        //warp_content 的默认大小
        val width = resources.getDimensionPixelOffset(R.dimen.widthDefault)
        val height = resources.getDimensionPixelOffset(R.dimen.heightDefault)

        if (layoutParams.width == ViewGroup.LayoutParams.WRAP_CONTENT && layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(width, height)
        } else if (layoutParams.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(width, heightSize)
        } else if (layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(widthSize, height)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        //根据view宽高, 修改属性值
        mWidth = w.toFloat()
        mHeight = h.toFloat()
        mHalfWidth = mWidth * 0.5f
        mHeight2 = mHeight * 0.2f
        mHeight4 = mHeight * 0.4f

        mLineStartX = mWidth * 0.15f
        mLineStopX = mWidth * 0.85f
        mLineY = mHeight * 0.55f

        mMainSpeedUnitSize = _mainTextSize * TEXT_TO_SPEED_UNIT_SIZE
        mSubSpeedUnitSize = _subTextSize * TEXT_TO_SPEED_UNIT_SIZE

        mainTextHeight = mHeight * 0.4f
        subTextHeight = mHeight * 0.8f

        setTriangleCenter()
        setTrianglePath(0f)
    }

    /**
     * 设置三角形的中心
     */
    private fun setTriangleCenter() {
        mMainTriangleCenter[2] = -mPaintMainText.fontMetrics.ascent / 2
        mMainTriangleCenter[0] = mLineStartX + mMainTriangleCenter[2]
        mMainTriangleCenter[1] = mainTextHeight - mMainTriangleCenter[2]
        mSubTriangleCenter[2] = -mPaintSubText.fontMetrics.ascent / 2
        mSubTriangleCenter[0] = mMainTriangleCenter[0]
        mSubTriangleCenter[1] = subTextHeight - mSubTriangleCenter[2]

        mMainTriangleCenter[2] *= 0.8f
        mSubTriangleCenter[2] *= 0.8f
    }

    /**
     * 设置三角形顶点位置
     */
    private fun setTriangleVertex(angle: Float) {
        if (angle in 0..60) {
            var trueAngle = angle + 90.0
            var trueAngleR = Math.toRadians(trueAngle).toFloat()
            var i = 0
            while (i < 6) {
                if (i % 2 == 0) {
                    mTriangleVertex[i] = mMainTriangleCenter[0] + mMainTriangleCenter[2] * cos(trueAngleR)
                } else {
                    mTriangleVertex[i] = mMainTriangleCenter[1] + mMainTriangleCenter[2] * sin(trueAngleR)
                    trueAngle += 120
                    trueAngleR = Math.toRadians(trueAngle).toFloat()
                }
                i++
            }
            trueAngle = angle + 30.0
            trueAngleR = Math.toRadians(trueAngle).toFloat()
            while (i < 12) {
                if (i % 2 == 0) {
                    mTriangleVertex[i] = mSubTriangleCenter[0] + mSubTriangleCenter[2] * cos(trueAngleR)
                } else {
                    mTriangleVertex[i] = mSubTriangleCenter[1] + mSubTriangleCenter[2] * sin(trueAngleR)
                    trueAngle += 120
                    trueAngleR = Math.toRadians(trueAngle).toFloat()
                }
                i++
            }
        }
    }

    /**
     * 设置三角形的绘制 path
     */
    private fun setTrianglePath(angle: Float) {
        setTriangleVertex(angle)

        path.reset()
        path.moveTo(mTriangleVertex[0], mTriangleVertex[1])
        path.lineTo(mTriangleVertex[2], mTriangleVertex[3])
        path.lineTo(mTriangleVertex[4], mTriangleVertex[5])
        path.close()

        path.moveTo(mTriangleVertex[6], mTriangleVertex[7])
        path.lineTo(mTriangleVertex[8], mTriangleVertex[9])
        path.lineTo(mTriangleVertex[10], mTriangleVertex[11])
        path.close()

        invalidate()
    }

    /**
     * SpeedArray 0: upload, 1: download
     * 设置速度大小, 设置后重绘 view (如果 isLocked == false )
     */
    open fun setSpeedArray(speed: FloatArray) {
        if (isLocked) return
        mSpeedArray[0] = if (!isReverseSpeed) speed[0] else speed[1]
        mSpeedArray[1] = if (!isReverseSpeed) speed[1] else speed[0]

        when (speedUnit) {
            1 -> {
                mSpeedArray[0] /= 1000f; mSpeedArray[1] /= 1000f
            }
            2 -> {
                mSpeedArray[0] /= 1000_000f; mSpeedArray[1] /= 1000_000f
            }
            3 -> {
                mSpeedArray[0] /= 1000_000_000f; mSpeedArray[1] /= 1000_000_000f
            }
            4 -> {
                when {
                    mSpeedArray[0] >= 1000_000_000f -> {
                        mSpeedArray[0] /= 1000_000_000f; mUploadUnit = 3
                    }
                    mSpeedArray[0] >= 1000_000f -> {
                        mSpeedArray[0] /= 1000_000f; mUploadUnit = 2
                    }
                    mSpeedArray[0] >= 1000f -> {
                        mSpeedArray[0] /= 1000f; mUploadUnit = 1
                    }
                    else -> mUploadUnit = 0
                }
                when {
                    mSpeedArray[1] >= 1000_000_000f -> {
                        mSpeedArray[1] /= 1000_000_000f; mDownloadUnit = 3
                    }
                    mSpeedArray[1] >= 1000_000f -> {
                        mSpeedArray[1] /= 1000_000f; mDownloadUnit = 2
                    }
                    mSpeedArray[1] >= 1000f -> {
                        mSpeedArray[1] /= 1000f; mDownloadUnit = 1
                    }
                    else -> mDownloadUnit = 0
                }
            }
            5 -> {
                when {
                    mSpeedArray[1] >= 1000_000_000f -> {
                        mSpeedArray[0] /= 1000_000_000f; mSpeedArray[1] /= 1000_000_000f; mDownloadUnit = 3
                    }
                    mSpeedArray[1] >= 1000_000f -> {
                        mSpeedArray[0] /= 1000_000f; mSpeedArray[1] /= 1000_000f; mDownloadUnit = 2
                    }
                    mSpeedArray[1] >= 1000f -> {
                        mSpeedArray[0] /= 1000f; mSpeedArray[1] /= 1000f; mDownloadUnit = 1
                    }
                    else -> mDownloadUnit = 0
                }
                mUploadUnit = mDownloadUnit
            }
        }

        setOutSpeedArray()
        invalidate()
    }

    /**
     * 将移动后修改的值 改回默认值
     */
    open fun resetOnTouch() {
        touchDownY = -1f
        offsetY = 0f
        isLocked = false
        setPaintAlpha(0f)
        invalidate()
    }

    /**
     * i: 0: textSizeCoefficientDown, 1: textSizeCoefficientUp
     * 通过 textSizeCoefficient 获取目前文本大小
     */
    private fun getCurrentTextSize(i: Int, height: Float): Float {
        var size = 0f
        when (i) {
            0 -> size = textSizeCoefficient[0] * (height / mHeight) + textSizeCoefficient[1]
            1 -> size = textSizeCoefficient[0] * (0.8f - height / mHeight) + textSizeCoefficient[1]
        }
        return size
    }

    /**
     * 在移动中, 设置主要文本, 次要文本的透明度
     */
    private fun setPaintAlpha(offsetY: Float) {
        when {
            offsetY == 0f -> {
                mPaintMainText.alpha = 255; mPaintSubText.alpha = 200
            }
            offsetY > -mHeight2 -> {
                mPaintMainText.alpha = (255 * (1 + offsetY / mHeight2)).toInt()
                mPaintSubText.alpha = 200 + (55 * -offsetY / mHeight4).toInt()
            }
            offsetY in -mHeight4..-mHeight2 -> {
                mPaintMainText.alpha = (200 * (-offsetY - mHeight2) / mHeight2).toInt()
                mPaintSubText.alpha = 200 + (55 * -offsetY / mHeight4).toInt()
            }
            else -> mPaintMainText.alpha = 0
        }
    }

    /**
     * 交换下载和上传值
     */
    private fun swapSpeed() {
        val s = mSpeedArray[0]
        val u = mDownloadUnit
        mSpeedArray[0] = mSpeedArray[1]
        mSpeedArray[1] = s
        mDownloadUnit = mUploadUnit
        mUploadUnit = u
        isDownloadMain = !isDownloadMain
    }

    /**
     * 开始 三角旋转动画
     */
    private fun startRotateAnimator(notReverse: Boolean) {
        if (notReverse) {
            mRotateAnimator.reverse()
        } else {
            mRotateAnimator.start()
        }
    }

    /**
     * 结束 三角旋转动画
     */
    private fun endAnimator() {
        mRotateAnimator.end()
    }

    /**
     * 设置外部访问的速度数组
     */
    private fun setOutSpeedArray() {
        speedArray[0] = "▲ ${String.format("%.1f", mSpeedArray[0])} ${mSpeedUnitArray[mUploadUnit]}"
        speedArray[1] = "▼ ${String.format("%.1f", mSpeedArray[1])} ${mSpeedUnitArray[mDownloadUnit]}"
    }
}