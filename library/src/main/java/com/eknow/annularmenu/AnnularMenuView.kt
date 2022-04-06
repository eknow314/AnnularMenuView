package com.eknow.annularmenu

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.eknow.annularmenu.listener.OnMenuClickListener
import com.eknow.annularmenu.listener.OnMenuLongClickListener
import com.eknow.annularmenu.listener.OnMenuTouchListener
import kotlin.math.*

/**
 * @Description: 环状扇形菜单，手机遥控按钮布局
 * @author: Eknow
 * @date: 2022/4/1 18:05
 */
class AnnularMenuView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mPath: Path
    private var mPaintFill: Paint
    private var mPaintStroke: Paint

    // 圆心
    private var centerX = 0f
    private var centerY = 0f

    // 内圆半径
    private var radiusInner = 0f

    // 外圆半径
    private var radiusOuter = 0f

    // 点击状态，-1未点击，>=0对应按钮下标
    private var onClickState = -1

    // 记录按下时间，超过预设时间算长按按钮
    private var touchTime = 0L


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val maxWidth = MeasureSpec.getSize(widthMeasureSpec)
        val maxHeight = MeasureSpec.getSize(heightMeasureSpec)
        // 取大值，然后设置一个正方形区域
        val size = maxWidth.coerceAtMost(maxHeight)
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY)
        )
        // math 运算导致的精度丢失，网传裁剪掉2，留给算术裁剪即可
        setMeasuredDimension(size - 2, size - 2)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val netWidth = w - paddingLeft - paddingRight
        val netHeight = h - paddingTop - paddingBottom
        // 圆心坐标
        centerX = netWidth * 0.5f
        centerY = netHeight * 0.5f
        // 外圆半径等于控件宽减去边框大小
        radiusOuter = (width / 2 - mMenuStrokeSize).toFloat()
        // 暂时内圆半径用六分之一宽
        radiusInner = radiusOuter / mRadiusRatio
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val rectInner = RectF(
            centerX - radiusInner,
            centerY - radiusInner,
            centerX + radiusInner,
            centerY + radiusInner
        )
        val rectOuter = RectF(
            centerX - radiusOuter,
            centerY - radiusOuter,
            centerX + radiusOuter,
            centerY + radiusOuter
        )
        val sweepAngle = 360.0f / mMenuNum

        // 先计算内圆周长
        val innerPerimeter = (radiusInner * 2 * PI).toFloat()
        // 内弧角度偏移量，偏移量/内圆周长*360，取一半使用
        val innerAngleOffset = (mMenuMargin / innerPerimeter * 360.0f)

        // 计算外圆周长
        val outerPerimeter = (radiusOuter * 2 * PI).toFloat()
        // 外弧角度偏移量
        val outerAngleOffset = (mMenuMargin / outerPerimeter * 360.0f)

        for (i in 0 until mMenuNum) {
            mPath.reset()
            // 内圈
            val startAngle1 = i * sweepAngle + innerAngleOffset / 2 + mMenuDeviationAngle
            mPath.addArc(rectInner, startAngle1, sweepAngle - innerAngleOffset)
            // 外圈
            val startAngle2 = ((i + 1) * sweepAngle) - outerAngleOffset / 2 + mMenuDeviationAngle
            mPath.arcTo(rectOuter, startAngle2, -(sweepAngle - outerAngleOffset))
            mPath.close()

            // 画内部
            mPaintFill = Paint()
            mPaintFill.isAntiAlias = true
            mPaintFill.style = Paint.Style.FILL
            mPaintFill.strokeWidth = mMenuStrokeSize.toFloat()
            if (onClickState == i) {
                // 按压了
                mPaintFill.color = mMenuPressedBgColor
            } else {
                // 普通状态，判断是否使用渐变色
                if (mMenuNormalBgGradientColor0 != Color.TRANSPARENT
                    && mMenuNormalBgGradientColor1 != Color.TRANSPARENT
                ) {
                    // 先用角度来区分水平渐变和垂直渐变
                    // 上半圆是垂直渐变，下半圆是水平渐变
                    if (startAngle2 > 180) {
                        mPaintFill.shader = LinearGradient(
                            0f,
                            0f,
                            0f,
                            radiusOuter,
                            mMenuNormalBgGradientColor0,
                            mMenuNormalBgGradientColor1,
                            Shader.TileMode.CLAMP
                        )
                    } else {
                        mPaintFill.shader = LinearGradient(
                            0f,
                            0f,
                            radiusOuter,
                            0f,
                            mMenuNormalBgGradientColor0,
                            mMenuNormalBgGradientColor1,
                            Shader.TileMode.CLAMP
                        )
                    }
                } else {
                    mPaintFill.color = mMenuNormalBgColor
                }
            }
            canvas.drawPath(mPath, mPaintFill)

            // 画边框
            mPaintStroke = Paint()
            mPaintStroke.isAntiAlias = true
            mPaintStroke.style = Paint.Style.STROKE
            mPaintStroke.strokeWidth = mMenuStrokeSize.toFloat()
            mPaintStroke.color = mMenuStrokeColor
            canvas.drawPath(mPath, mPaintStroke)

            // 画图标
            if (mMenuDrawableList.size > i) {
                val menuDrawable = mMenuDrawableList[i]
                if (menuDrawable != null) {
                    // 图片中心距离圆心位置
                    val k1 = (radiusOuter - radiusInner) / 2 + radiusInner
                    // 把角度转为弧度，并顺时针加上偏移量
                    val a1 = Math.toRadians((((i + 0.5f) * sweepAngle + 360 + mMenuDeviationAngle) % 360).toDouble())
                    // 计算图片圆心
                    val x1: Float = (centerX + k1 * cos(a1)).toFloat()
                    val y1: Float = (centerY + k1 * sin(a1)).toFloat()

                    val rectTemp = RectF(
                        x1 - mIconSize / 2,
                        y1 - mIconSize / 2,
                        x1 + mIconSize / 2,
                        y1 + mIconSize / 2
                    )
                    canvas.drawBitmap(menuDrawable, null, rectTemp, Paint(Paint.ANTI_ALIAS_FLAG))
                }
            }
        }

    }

    @SuppressLint("HandlerLeak")
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                1 -> {
                    mMenuLongClickListener?.OnMenuLongClick(onClickState)
                }
            }
        }
    }

    /**
     * 点触事件
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val textX: Float
        val textY: Float
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 记录按下的时间
                touchTime = System.currentTimeMillis()
                // 记录按下的位置
                textX = event.x
                textY = event.y
                // 距离中心点之间的直线距离
                val distance = calDistanceTwoSpot(centerX, centerY, textX, textY)
                if (distance <= radiusInner) {
                    // 点击的是内圆，暂无效果

                } else if (distance <= radiusOuter) {
                    // 点击的是某个环状扇形
                    val sweepAngle = 360 / mMenuNum
                    // 计算这根线和水平正X轴夹角
                    var angle = calAngle(centerX, centerY, textX, textY)
                    // 该计算得到的角度是连接线顺时针到正X轴夹角，换算下
                    angle = 360 - angle
                    // 再加上偏移量，绘制形状时候顺时针添加了，这时候需要逆时针减去
                    angle = (angle + 360 - mMenuDeviationAngle) % 360
                    // 根据角度得出点击的是那个扇形
                    onClickState = (angle / sweepAngle).toInt()
                    if (onClickState >= mMenuNum) {
                        onClickState = 0
                    }
                } else {
                    //点击了外面，也没有效果

                }
                mHandler.sendEmptyMessageDelayed(1, DL_DEFAULT_LONG_CLICK_TIME.toLong())
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                mHandler.removeMessages(1)
                if (System.currentTimeMillis() - touchTime < DL_DEFAULT_LONG_CLICK_TIME) {
                    //点击小于400毫秒算点击
                    mMenuClickListener?.OnMenuClick(onClickState)
                }
                onClickState = -1
                invalidate()
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_OUTSIDE -> {
                mHandler.removeMessages(1)
                onClickState = -1
                invalidate()
            }
        }
        mTouchListener?.OnTouch(event, onClickState)
        return true
    }

    /**
     * 计算触摸点和圆心之间的距离
     */
    fun calDistanceTwoSpot(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float
    ): Double {
        val width: Float = x1 - x2
        val height: Float = y1 - y2
        return sqrt(width * width + height * height).toDouble()
    }

    /**
     * 计算触摸点和圆心的连线，和水平正X轴夹角
     */
    fun calAngle(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float
    ): Double {
        val x = abs(x1 - x2)
        val y = abs(y1 - y2)
        val z = sqrt((x * x + y * y).toDouble())
        val angle = (asin(y / z) / Math.PI * 180).roundToInt().toDouble()
        return if (x2 > x1 && y2 < y1) {
            // 第一象限
            angle
        } else if (x2 < x1 && y2 < y1) {
            // 第二象限
            180 - angle
        } else if (x2 < x1 && y2 > y1) {
            // 第三象限
            180 + angle
        } else if (x2 > x1 && y2 > y1) {
            // 第四象限
            360 - angle
        } else if (x2 == x1 && y2 < y1) {
            // 正Y轴
            90.0
        } else if (x2 == x1 && y2 > y1) {
            // 负Y轴
            270.0
        } else {
            0.0
        }
    }

    /**
     * drawable 转换为 bitmap
     */
    fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null) return null
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        if (width <= 0 || height <= 0) return null
        // 取 drawable 的颜色格式
        val config = if (drawable.opacity != PixelFormat.OPAQUE) {
            Bitmap.Config.ARGB_8888
        } else {
            Bitmap.Config.RGB_565
        }
        return try {
            Bitmap.createBitmap(mIconSize, mIconSize, config).apply {
                drawable.setBounds(0, 0, width, height)
                drawable.draw(Canvas(this))
            }
        } catch (e: Exception) {
            null
        }
    }

    private val mMenuDrawableList = ArrayList<Bitmap?>()

    /**
     * 设置对应下标菜单的图标
     * @param index 菜单下标
     * @param drawable 图标
     */
    fun setMenuDrawable(index: Int, drawable: Drawable?) {
        if (index < 0 || index > mMenuNum) return
        if (mMenuDrawableList.size > index) {
            mMenuDrawableList[index] = drawableToBitmap(drawable)
        } else {
            mMenuDrawableList.add(index, drawableToBitmap(drawable))
        }
        invalidate()
    }

    private var mMenuClickListener: OnMenuClickListener? = null
    private var mMenuLongClickListener: OnMenuLongClickListener? = null
    private var mTouchListener: OnMenuTouchListener? = null

    /**
     * 设置点击监听
     * @param onMenuClickListener
     */
    fun setOnMenuClickListener(onMenuClickListener: OnMenuClickListener?) {
        mMenuClickListener = onMenuClickListener
    }

    /**
     * 设置长按监听
     * @param onMenuLongClickListener
     */
    fun setOnMenuLongClickListener(onMenuLongClickListener: OnMenuLongClickListener?) {
        mMenuLongClickListener = onMenuLongClickListener
    }

    /**
     * 设置触摸监听
     * @param onMenuTouchListener
     */
    fun setOnMenuTouchListener(onMenuTouchListener: OnMenuTouchListener?) {
        mTouchListener = onMenuTouchListener
    }

    // 按钮数量
    private var mMenuNum: Int

    // 菜单偏移角度，从正X轴顺时针旋转
    private var mMenuDeviationAngle: Float

    // 按钮边框大小
    private var mMenuStrokeSize: Int

    // 按钮边框颜色
    private var mMenuStrokeColor: Int

    // 按钮边距
    private var mMenuMargin: Int

    // 外圆半径和内圆半径比例
    private var mRadiusRatio: Float

    // 菜单点击状态背景色
    private var mMenuPressedBgColor: Int

    // 菜单普通情况下背景色
    private var mMenuNormalBgColor: Int

    // 菜单普通情况下的渐变色0和1
    private var mMenuNormalBgGradientColor0: Int
    private var mMenuNormalBgGradientColor1: Int

    private var mIconSize = dp2px(24f)


    private fun dp2px(dp: Float): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        setWillNotDraw(false)
        mPath = Path()
        mPaintFill = Paint()
        mPaintStroke = Paint()
        // 初始化控件
        context.obtainStyledAttributes(attrs, R.styleable.AnnularMenuView).apply {
            mMenuNum = getInt(R.styleable.AnnularMenuView_amv_menuNum, 3)
            mMenuDeviationAngle = getFloat(R.styleable.AnnularMenuView_amv_menuDeviationAngle, 0f)
            mMenuStrokeSize =
                getDimensionPixelOffset(R.styleable.AnnularMenuView_amv_menuStrokeSize, dp2px(1f))
            mMenuStrokeColor = getColor(
                R.styleable.AnnularMenuView_amv_menuStrokeColor,
                Color.parseColor("#73C4FF")
            )
            mMenuMargin =
                getDimensionPixelOffset(R.styleable.AnnularMenuView_amv_menuMargin, dp2px(10f))
            mRadiusRatio = getFloat(R.styleable.AnnularMenuView_amv_radiusRatio, 3.0f)
            mMenuPressedBgColor = getColor(
                R.styleable.AnnularMenuView_amv_menuPressedBgColor,
                Color.parseColor("#003F77")
            )
            mMenuNormalBgColor = getColor(
                R.styleable.AnnularMenuView_amv_menuNormalBgColor,
                Color.parseColor("#138DAF")
            )
            mMenuNormalBgGradientColor0 = getColor(
                R.styleable.AnnularMenuView_amv_menuNormalBgGradientColor0,
                Color.TRANSPARENT
            )
            mMenuNormalBgGradientColor1 = getColor(
                R.styleable.AnnularMenuView_amv_menuNormalBgGradientColor1,
                Color.TRANSPARENT
            )
            recycle()
        }
    }

    companion object {
        // 长按时长
        private var DL_DEFAULT_LONG_CLICK_TIME = 400

    }

}