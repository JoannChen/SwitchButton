package com.joann.chen.switchbutton

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.Checkable

/**
 * Desc：线形开关
 * Author：JoannChen
 * Mail：yongzuo_chen@dingyuegroup.cn
 * Date：2018/11/2 0002 11:00
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class BaseSwitchButton : View, Checkable {

    private val circleSwitchButton: Int = 1
    private val squareSwitchButton: Int = 2
    private val lineSwitchButton: Int = 3
    private var switchButtonStyle = 1

    /**
     * 动画状态：
     * 1.静止
     * 2.进入拖动
     * 3.处于拖动
     * 4.拖动-复位
     * 5.拖动-切换
     * 6.点击切换
     */
    private val animateStateNone = 0
    private val ANIMATE_STATE_PENDING_DRAG = 1
    private val ANIMATE_STATE_DRAGING = 2
    private val ANIMATE_STATE_PENDING_RESET = 3
    private val ANIMATE_STATE_PENDING_SETTLE = 4
    private val ANIMATE_STATE_SWITCH = 5
    private val argbEvaluator = android.animation.ArgbEvaluator()
    /**
     * 阴影半径
     */
    private var shadowRadius: Int = 0
    /**
     * 阴影Y偏移px
     */
    private var shadowOffset: Int = 0
    /**
     * 阴影颜色
     */
    private var shadowColor: Int = 0

    /**
     * 背景半径
     */
    private var viewRadius: Float = 0.toFloat()

    /**
     * 背景矩形圆角半径
     */
    private var viewRoundRadius: Float = 0.toFloat()
    /**
     * 按钮半径
     */
    private var buttonRadius: Float = 0.toFloat()
    /**
     * 背景高
     */
    private var height: Float = 0.toFloat()
    /**
     * 背景宽
     */
    private var width: Float = 0.toFloat()
    /**
     * 背景位置
     */
    private var left: Float = 0.toFloat()
    private var top: Float = 0.toFloat()
    private var right: Float = 0.toFloat()
    private var bottom: Float = 0.toFloat()
    private var centerX: Float = 0.toFloat()
    private var centerY: Float = 0.toFloat()
    /**
     * 背景底色
     */
    private var background: Int = 0
    /**
     * 背景关闭颜色
     */
    private var uncheckedColor: Int = 0
    /**
     * 背景打开颜色
     */
    private var checkedColor: Int = 0
    /**
     * 按钮打开的颜色
     */
    private var checkedBtnColor: Int = 0
    /**
     * 按钮关闭的颜色
     */
    private var uncheckedBtnColor: Int = 0
    /**
     * 边框宽度px
     */
    private var borderWidth: Int = 0
    /**
     * 打开指示线颜色
     */
    private var checkLineColor: Int = 0
    /**
     * 打开指示线宽
     */
    private var checkLineWidth: Int = 0
    /**
     * 打开指示线长
     */
    private var checkLineLength: Float = 0.toFloat()
    /**
     * 关闭圆圈颜色
     */
    private var uncheckCircleColor: Int = 0
    /**
     * 关闭圆圈线宽
     */
    private var uncheckCircleWidth: Int = 0
    /**
     * 关闭圆圈位移X
     */
    private var uncheckCircleOffsetX: Float = 0.toFloat()
    /**
     * 关闭圆圈半径
     */
    private var uncheckCircleRadius: Float = 0.toFloat()
    /**
     * 打开指示线位移X
     */
    private var checkedLineOffsetX: Float = 0.toFloat()
    /**
     * 打开指示线位移Y
     */
    private var checkedLineOffsetY: Float = 0.toFloat()
    /**
     * 按钮最左边
     */
    private var buttonMinX: Float = 0.toFloat()
    /**
     * 按钮最右边
     */
    private var buttonMaxX: Float = 0.toFloat()
    /**
     * 按钮画笔
     */
    private var buttonPaint: Paint? = null
    /**
     * 背景画笔
     */
    private var paint: Paint? = null
    /**
     * 当前状态
     */
    private var viewState: ViewState? = null
    private var beforeState: ViewState? = null
    private var afterState: ViewState? = null
    private val rect = RectF()
    /**
     * 动画状态
     */
    private var animateState = animateStateNone
    /**
     *
     */
    private var valueAnimator: ValueAnimator? = null
    /**
     * 是否选中
     */
    private var isChecked: Boolean = false
    /**
     * 是否启用动画
     */
    private var enableEffect: Boolean = false
    /**
     * 是否启用阴影效果
     */
    private var shadowEffect: Boolean = false
    /**
     * 是否显示指示器
     */
    private var showIndicator: Boolean = false
    /** */
    /**
     * 收拾是否按下
     */
    private var isTouchingDown = false
    /**
     *
     */
    private var isUiInited = false
    /**
     *
     */
    private var isEventBroadcast = false
    private var onCheckedChangeListener: OnCheckedChangeListener? = null


    //Joann
    private var lineHeight = 0

    private var buttonOffset = 0

    //Joann

    /**
     * 手势按下的时刻
     */
    private var touchDownTime: Long = 0
    private val postPendingDrag = Runnable {
        if (!isInAnimating) {
            pendingDragState()
        }
    }
    private val animatorUpdateListener = object : ValueAnimator.AnimatorUpdateListener {
        override fun onAnimationUpdate(animation: ValueAnimator) {
            val value = animation.animatedValue as Float
            when (animateState) {
                ANIMATE_STATE_PENDING_SETTLE -> {
                    run { }
                    run { }
                    run {
                        viewState!!.checkedLineColor = argbEvaluator.evaluate(
                                value,
                                beforeState!!.checkedLineColor,
                                afterState!!.checkedLineColor
                        ) as Int

                        viewState!!.radius = beforeState!!.radius + (afterState!!.radius - beforeState!!.radius) * value

                        if (animateState != ANIMATE_STATE_PENDING_DRAG) {
                            viewState!!.buttonX = beforeState!!.buttonX + (afterState!!.buttonX - beforeState!!.buttonX) * value
                        }

                        viewState!!.checkStateColor = argbEvaluator.evaluate(
                                value,
                                beforeState!!.checkStateColor,
                                afterState!!.checkStateColor
                        ) as Int

                    }
                }
                ANIMATE_STATE_PENDING_RESET -> {
                    run { }
                    run {
                        viewState!!.checkedLineColor = argbEvaluator.evaluate(value, beforeState!!.checkedLineColor, afterState!!.checkedLineColor) as Int
                        viewState!!.radius = beforeState!!.radius + (afterState!!.radius - beforeState!!.radius) * value
                        if (animateState != ANIMATE_STATE_PENDING_DRAG) {
                            viewState!!.buttonX = beforeState!!.buttonX + (afterState!!.buttonX - beforeState!!.buttonX) * value
                        }
                        viewState!!.checkStateColor = argbEvaluator.evaluate(value, beforeState!!.checkStateColor, afterState!!.checkStateColor) as Int
                    }
                }
                ANIMATE_STATE_PENDING_DRAG -> {
                    viewState!!.checkedLineColor = argbEvaluator.evaluate(value, beforeState!!.checkedLineColor, afterState!!.checkedLineColor) as Int
                    viewState!!.radius = beforeState!!.radius + (afterState!!.radius - beforeState!!.radius) * value
                    if (animateState != ANIMATE_STATE_PENDING_DRAG) {
                        viewState!!.buttonX = beforeState!!.buttonX + (afterState!!.buttonX - beforeState!!.buttonX) * value
                    }
                    viewState!!.checkStateColor = argbEvaluator.evaluate(value, beforeState!!.checkStateColor, afterState!!.checkStateColor) as Int
                }
                ANIMATE_STATE_SWITCH -> {
                    viewState!!.buttonX = beforeState!!.buttonX + (afterState!!.buttonX - beforeState!!.buttonX) * value

                    val fraction = (viewState!!.buttonX - buttonMinX) / (buttonMaxX - buttonMinX)

                    viewState!!.checkStateColor = argbEvaluator.evaluate(
                            fraction,
                            uncheckedColor,
                            checkedColor
                    ) as Int

                    viewState!!.radius = fraction * viewRadius
                    viewState!!.checkedLineColor = argbEvaluator.evaluate(
                            fraction,
                            Color.TRANSPARENT,
                            checkLineColor
                    ) as Int
                }
                ANIMATE_STATE_DRAGING -> {
                }
                animateStateNone -> {
                }
                else -> {
                }
            }
            postInvalidate()
        }
    }
    private val animatorListener = object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {}

        override fun onAnimationEnd(animation: Animator) {
            when (animateState) {
                ANIMATE_STATE_DRAGING -> {
                }
                ANIMATE_STATE_PENDING_DRAG -> {
                    animateState = ANIMATE_STATE_DRAGING
                    viewState!!.checkedLineColor = Color.TRANSPARENT
                    viewState!!.radius = viewRadius

                    postInvalidate()
                }
                ANIMATE_STATE_PENDING_RESET -> {
                    animateState = animateStateNone
                    postInvalidate()
                }
                ANIMATE_STATE_PENDING_SETTLE -> {
                    animateState = animateStateNone
                    postInvalidate()
                    broadcastEvent()
                }
                ANIMATE_STATE_SWITCH -> {
                    isChecked = !isChecked
                    animateState = animateStateNone
                    postInvalidate()
                    broadcastEvent()
                }
                animateStateNone -> {
                }
                else -> {
                }
            }
        }

        override fun onAnimationCancel(animation: Animator) {}

        override fun onAnimationRepeat(animation: Animator) {}
    }


    /**
     * 是否在动画状态
     */
    private val isInAnimating: Boolean
        get() = animateState != animateStateNone

    /**
     * 是否在进入拖动或离开拖动状态
     */
    private val isPendingDragState: Boolean
        get() = animateState == ANIMATE_STATE_PENDING_DRAG || animateState == ANIMATE_STATE_PENDING_RESET

    /**
     * 是否在手指拖动状态
     */
    private val isDragState: Boolean
        get() = animateState == ANIMATE_STATE_DRAGING

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    /**
     * 初始化参数
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private fun init(context: Context, attrs: AttributeSet?) {


        when (switchButtonStyle) {
            circleSwitchButton -> {

            }
            squareSwitchButton -> {
                val density = resources.displayMetrics.density

                lineHeight = (LINE_HEIGHT * density + 0.5f).toInt()

                //        buttonOffset = (int) (BUTTON_OFFSET * density + 0.5f);
            }
            lineSwitchButton -> {
                val density = resources.displayMetrics.density

                lineHeight = (LINE_HEIGHT * density + 0.5f).toInt()
                buttonOffset = (BUTTON_OFFSET * density + 0.5f).toInt()
            }
        }

        var typedArray: TypedArray? = null
        if (attrs != null) {
            typedArray = context.obtainStyledAttributes(attrs, R.styleable.SwitchButton)
        }

        shadowEffect = optBoolean(typedArray,
                R.styleable.SwitchButton_sb_shadow_effect,
                true)

        uncheckCircleColor = optColor(typedArray,
                R.styleable.SwitchButton_sb_uncheckcircle_color,
                -0x555556)//0XffAAAAAA;

        uncheckCircleWidth = optPixelSize(typedArray,
                R.styleable.SwitchButton_sb_uncheckcircle_width,
                dp2pxInt(1.5f))//dp2pxInt(1.5f);

        uncheckCircleOffsetX = dp2px(10f)

        uncheckCircleRadius = optPixelSize(typedArray,
                R.styleable.SwitchButton_sb_uncheckcircle_radius,
                dp2px(4f))//dp2px(4);

        checkedLineOffsetX = dp2px(4f)
        checkedLineOffsetY = dp2px(4f)

        shadowRadius = optPixelSize(typedArray,
                R.styleable.SwitchButton_sb_shadow_radius,
                dp2pxInt(2.5f))//dp2pxInt(2.5f);

        shadowOffset = optPixelSize(typedArray,
                R.styleable.SwitchButton_sb_shadow_offset,
                dp2pxInt(1.5f))//dp2pxInt(1.5f);

        shadowColor = optColor(typedArray,
                R.styleable.SwitchButton_sb_shadow_color,
                0X33000000)//0X33000000;

        uncheckedColor = optColor(typedArray,
                R.styleable.SwitchButton_sb_uncheck_color,
                -0x222223)//0XffDDDDDD;

        checkedColor = optColor(typedArray,
                R.styleable.SwitchButton_sb_checked_color,
                -0xae2c99)//0Xff51d367;

        uncheckedBtnColor = optColor(typedArray,
                R.styleable.SwitchButton_sb_uncheckedbtn_color,
                -0xae2c99)//0Xff51d367;

        checkedBtnColor = optColor(typedArray,
                R.styleable.SwitchButton_sb_checkedbtn_color,
                -0xae2c99)//0Xff51d367;

        borderWidth = optPixelSize(typedArray,
                R.styleable.SwitchButton_sb_border_width,
                dp2pxInt(1f))//dp2pxInt(1);

        checkLineColor = optColor(typedArray,
                R.styleable.SwitchButton_sb_checkline_color,
                Color.WHITE)//Color.WHITE;

        checkLineWidth = optPixelSize(typedArray,
                R.styleable.SwitchButton_sb_checkline_width,
                dp2pxInt(1f))//dp2pxInt(1.0f);

        checkLineLength = dp2px(6f)

        val buttonColor = optColor(typedArray,
                R.styleable.SwitchButton_sb_button_color,
                Color.WHITE)//Color.WHITE;

        val effectDuration = optInt(typedArray,
                R.styleable.SwitchButton_sb_effect_duration,
                300)//300;

        isChecked = optBoolean(typedArray,
                R.styleable.SwitchButton_sb_checked,
                false)

        showIndicator = optBoolean(typedArray,
                R.styleable.SwitchButton_sb_show_indicator,
                true)

        background = optColor(typedArray,
                R.styleable.SwitchButton_sb_background,
                Color.TRANSPARENT)

        enableEffect = optBoolean(typedArray,
                R.styleable.SwitchButton_sb_enable_effect,
                true)

        if (typedArray != null) {
            typedArray.recycle()
        }


        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        buttonPaint!!.color = buttonColor

        if (shadowEffect) {
            buttonPaint!!.setShadowLayer(
                    shadowRadius.toFloat(),
                    0f, shadowOffset.toFloat(),
                    shadowColor)
        }


        viewState = ViewState()
        beforeState = ViewState()
        afterState = ViewState()

        valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator!!.duration = effectDuration.toLong()
        valueAnimator!!.repeatCount = 0

        valueAnimator!!.addUpdateListener(animatorUpdateListener)
        valueAnimator!!.addListener(animatorListener)

        super.setClickable(true)
        super.setPadding(0, 0, 0, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)

        if (widthMode == View.MeasureSpec.UNSPECIFIED || widthMode == View.MeasureSpec.AT_MOST) {
            widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(DEFAULT_WIDTH, View.MeasureSpec.EXACTLY)
        }
        if (heightMode == View.MeasureSpec.UNSPECIFIED || heightMode == View.MeasureSpec.AT_MOST) {
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(DEFAULT_HEIGHT, View.MeasureSpec.EXACTLY)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)


        val viewPadding = Math.max(shadowRadius + shadowOffset, borderWidth).toFloat()

        height = h.toFloat() - viewPadding - viewPadding
        width = w.toFloat() - viewPadding - viewPadding

        when (switchButtonStyle) {
            circleSwitchButton -> {
                viewRadius = height * .6f
            }
            squareSwitchButton -> {
                viewRoundRadius = mdp2px(0.6f)
                viewRadius = height * 0.35f
            }
            lineSwitchButton -> {
                viewRadius = height * 0.35f

            }
        }


        buttonRadius = viewRadius - borderWidth

        when (switchButtonStyle) {
            circleSwitchButton -> {
                left = viewPadding
                top = viewPadding + 10
                right = w - viewPadding
                bottom = h.toFloat() - viewPadding - 10f
            }
            squareSwitchButton -> {
                left = (w / 2 - lineHeight).toFloat()
                top = viewPadding
                right = (w / 2 + lineHeight).toFloat()
                bottom = h - viewPadding
            }
            lineSwitchButton -> {
                left = viewPadding
                top = viewPadding + 10
                right = w - viewPadding
                bottom = h.toFloat() - viewPadding - 10f
            }
        }



        centerX = (left + right) * .5f
        centerY = (top + bottom) * .5f

        when (switchButtonStyle) {
            circleSwitchButton -> {
                buttonMinX = left + viewRadius - 10
                buttonMaxX = right - viewRadius + 10
            }
            squareSwitchButton -> {
                buttonMinX = left + (lineHeight / 2).toFloat() + buttonOffset.toFloat()
                buttonMaxX = right - (lineHeight / 2).toFloat() - buttonOffset.toFloat()

            }
            lineSwitchButton -> {
                buttonMinX = left + viewRadius - 10 + buttonOffset
                buttonMaxX = right - viewRadius + 10 - buttonOffset
            }
        }


        if (isChecked()) {
            buttonPaint!!.color = checkedBtnColor
            setCheckedViewState(viewState)
        } else {
            buttonPaint!!.color = uncheckedBtnColor
            setUncheckViewState(viewState)
        }

        isUiInited = true

        postInvalidate()

    }

    /**
     * @param viewState
     */
    private fun setUncheckViewState(viewState: ViewState?) {
        viewState!!.radius = 0f
        viewState.checkStateColor = uncheckedColor
        viewState.checkedLineColor = Color.TRANSPARENT
        viewState.buttonX = buttonMinX
    }

    /**
     * @param viewState
     */
    private fun setCheckedViewState(viewState: ViewState?) {
        viewState!!.radius = viewRadius
        viewState.checkStateColor = checkedColor
        viewState.checkedLineColor = checkLineColor
        viewState.buttonX = buttonMaxX
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint!!.strokeWidth = borderWidth.toFloat()
        paint!!.style = Paint.Style.FILL
        //绘制白色背景
        paint!!.color = background
        drawRoundRect(canvas,
                left, top, right, bottom,
                viewRadius, paint!!)

        when (switchButtonStyle) {
            circleSwitchButton -> {
                //绘制关闭状态的边框
                //        paint.setStyle(Paint.Style.STROKE);
                paint!!.color = uncheckedColor
                drawRoundRect(canvas,
                        left, top, right, bottom,
                        viewRadius, paint!!)

                //绘制小圆圈
                if (showIndicator) {
                    drawUncheckIndicator(canvas)
                }

                //绘制开启背景色
                val des = viewState!!.radius * .5f//[0-backgroundRadius*0.5f]
                paint!!.style = Paint.Style.STROKE
                paint!!.color = viewState!!.checkStateColor
                paint!!.strokeWidth = borderWidth + des * 2f
                drawRoundRect(canvas,
                        left + des, top + des, right - des, bottom - des,
                        viewRadius, paint!!)


            }
            squareSwitchButton -> {
                //        //绘制关闭状态的边框
                //        paint.setStyle(Paint.Style.STROKE);
                //        paint.setColor(uncheckedColor);
                //        drawRoundRect(canvas,
                //                left, top, right, bottom,
                //                viewRadius, paint);

                //绘制小圆圈
                if (showIndicator) {
                    drawUncheckIndicator(canvas)
                }

                //绘制开启背景色
                val des = 0f//[0-backgroundRadius*0.5f]
                paint!!.style = Paint.Style.FILL
                paint!!.color = viewState!!.checkStateColor
                //        paint.setStrokeWidth(borderWidth + des * 2f);
                val btnHalfHeightY = ((bottom - top) / 2 + top).toInt()
                drawRoundRect(canvas,
                        left, (btnHalfHeightY - lineHeight / 2).toFloat(),
                        right, (btnHalfHeightY + lineHeight / 2).toFloat(),
                        viewRoundRadius, paint!!)
            }

            lineSwitchButton -> {
                //绘制小圆圈
                if (showIndicator) {
                    drawUncheckIndicator(canvas)
                }

                //绘制开启背景色
                paint!!.style = Paint.Style.FILL
                paint!!.color = viewState!!.checkStateColor
                val btnHalfHeightY = ((bottom - top) / 2 + top).toInt()
                drawRoundRect(canvas,
                        left, (btnHalfHeightY - lineHeight / 2).toFloat(),
                        right, (btnHalfHeightY + lineHeight / 2).toFloat(),
                        viewRadius, paint!!)
            }
        }


        //绘制按钮左边绿色长条遮挡
        paint!!.style = Paint.Style.FILL
        paint!!.strokeWidth = 1f

        //绘制小线条
        if (showIndicator) {
            drawCheckedIndicator(canvas)
        }

        //绘制按钮
        when (switchButtonStyle) {
            circleSwitchButton -> {
                drawButton(canvas, viewState!!.buttonX, centerY)

            }
            squareSwitchButton -> {
                //绘制按钮
//                drawRoundRect(canvas,
//                        viewState!!.buttonX - lineHeight / 2, (btnHalfHeightY - lineHeight / 2).toFloat(),
//                        viewState!!.buttonX + lineHeight / 2, (btnHalfHeightY + lineHeight / 2).toFloat(),
//                        viewRoundRadius, buttonPaint)

                //        drawButton(canvas, viewState.buttonX, centerY);
            }
            lineSwitchButton -> {
                drawButton(canvas, viewState!!.buttonX, centerY)
            }
        }
    }


    /**
     * 绘制选中状态指示器
     */
    protected fun drawCheckedIndicator(canvas: Canvas) {
        drawCheckedIndicator(canvas,
                viewState!!.checkedLineColor,
                checkLineWidth.toFloat(),
                left + viewRadius - checkedLineOffsetX, centerY - checkLineLength,
                left + viewRadius - checkedLineOffsetY, centerY + checkLineLength,
                paint)
    }

    /**
     * 绘制选中状态指示器
     */
    @JvmOverloads protected fun drawCheckedIndicator(canvas: Canvas,
                                                     color: Int = viewState!!.checkedLineColor,
                                                     lineWidth: Float = checkLineWidth.toFloat(),
                                                     sx: Float = left + viewRadius - checkedLineOffsetX, sy: Float = centerY - checkLineLength, ex: Float = left + viewRadius - checkedLineOffsetY, ey: Float = centerY + checkLineLength,
                                                     paint: Paint?) {
        paint!!.style = Paint.Style.STROKE
        paint.color = color
        paint.strokeWidth = lineWidth
        canvas.drawLine(
                sx, sy, ex, ey,
                paint)
    }

    /**
     * 绘制关闭状态指示器
     */
    private fun drawUncheckIndicator(canvas: Canvas) {
        drawUncheckIndicator(canvas,
                uncheckCircleColor,
                uncheckCircleWidth.toFloat(),
                right - uncheckCircleOffsetX, centerY,
                uncheckCircleRadius,
                paint)
    }

    /**
     * 绘制关闭状态指示器
     */
    protected fun drawUncheckIndicator(canvas: Canvas,
                                       color: Int,
                                       lineWidth: Float,
                                       centerX: Float, centerY: Float,
                                       radius: Float,
                                       paint: Paint?) {
        paint!!.style = Paint.Style.STROKE
        paint.color = color
        paint.strokeWidth = lineWidth
        canvas.drawCircle(centerX, centerY, radius, paint)
    }

    /**
     * @param canvas
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @param startAngle
     * @param sweepAngle
     * @param paint
     */
    private fun drawArc(canvas: Canvas,
                        left: Float, top: Float,
                        right: Float, bottom: Float,
                        startAngle: Float, sweepAngle: Float,
                        paint: Paint) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawArc(left, top, right, bottom,
                    startAngle, sweepAngle, true, paint)
        } else {
            rect.set(left, top, right, bottom - 30)
            canvas.drawArc(rect,
                    startAngle, sweepAngle, true, paint)
        }
    }

    /**
     * @param canvas
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @param backgroundRadius
     * @param paint
     */
    private fun drawRoundRect(canvas: Canvas,
                              left: Float, top: Float,
                              right: Float, bottom: Float,
                              backgroundRadius: Float,
                              paint: Paint) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawRoundRect(left, top, right, bottom,
                    backgroundRadius, backgroundRadius, paint)
        } else {
            rect.set(left, top, right, bottom)
            canvas.drawRoundRect(rect,
                    backgroundRadius, backgroundRadius, paint)
        }
    }

    /**
     * @param x px
     * @param y px
     */
    private fun drawButton(canvas: Canvas, x: Float, y: Float) {
        canvas.drawCircle(x, y, buttonRadius, buttonPaint!!)
        //绘制圆边框
        paint!!.style = Paint.Style.STROKE
        //        canvas.drawCircle(x, y, buttonRadius, paint);
        paint!!.strokeWidth = 0f
        paint!!.color = -0x222223

    }

    override fun isChecked(): Boolean {
        return isChecked
    }

    override fun setChecked(checked: Boolean) {
        if (checked == isChecked()) {
            postInvalidate()
            return
        }
        toggle(enableEffect, false)
    }

    override fun toggle() {
        toggle(true)
    }

    /**
     * 切换状态
     */
    fun toggle(animate: Boolean) {
        toggle(animate, true)
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private fun toggle(animate: Boolean, broadcast: Boolean) {
        if (!isEnabled) {
            return
        }

        if (isEventBroadcast) {
            throw RuntimeException("should NOT switch the state in method: [onCheckedChanged]!")
        }
        if (!isUiInited) {
            isChecked = !isChecked
            if (broadcast) {
                broadcastEvent()
            }
            return
        }

        if (valueAnimator!!.isRunning) {
            valueAnimator!!.cancel()
        }

        if (!enableEffect || !animate) {
            isChecked = !isChecked
            if (isChecked()) {
                setCheckedViewState(viewState)
            } else {
                setUncheckViewState(viewState)
            }
            postInvalidate()
            if (broadcast) {
                broadcastEvent()
            }
            return
        }

        animateState = ANIMATE_STATE_SWITCH
        beforeState!!.copy(viewState!!)

        if (isChecked()) {
            //切换到unchecked
            buttonPaint!!.color = uncheckedBtnColor
            setUncheckViewState(afterState)
        } else {
            buttonPaint!!.color = checkedBtnColor
            setCheckedViewState(afterState)
        }
        valueAnimator!!.start()
    }

    /**
     *
     */
    private fun broadcastEvent() {
        if (onCheckedChangeListener != null) {
            isEventBroadcast = true
            onCheckedChangeListener!!.onCheckedChanged(this, isChecked())
        }
        isEventBroadcast = false
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        val actionMasked = event.actionMasked

        when (actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isTouchingDown = true
                touchDownTime = System.currentTimeMillis()
                //取消准备进入拖动状态
                removeCallbacks(postPendingDrag)
                //预设100ms进入拖动状态
                postDelayed(postPendingDrag, 100)
            }
            MotionEvent.ACTION_MOVE -> {
                val eventX = event.x
                if (isPendingDragState) {
                    //在准备进入拖动状态过程中，可以拖动按钮位置
                    var fraction = eventX / getWidth()
                    fraction = Math.max(0f, Math.min(1f, fraction))

                    viewState!!.buttonX = buttonMinX + (buttonMaxX - buttonMinX) * fraction

                } else if (isDragState) {
                    //拖动按钮位置，同时改变对应的背景颜色
                    var fraction = eventX / getWidth()
                    fraction = Math.max(0f, Math.min(1f, fraction))

                    viewState!!.buttonX = buttonMinX + (buttonMaxX - buttonMinX) * fraction

                    viewState!!.checkStateColor = argbEvaluator.evaluate(
                            fraction,
                            uncheckedColor,
                            checkedColor
                    ) as Int
                    postInvalidate()

                }
            }
            MotionEvent.ACTION_UP -> {
                isTouchingDown = false
                //取消准备进入拖动状态
                removeCallbacks(postPendingDrag)

                if (System.currentTimeMillis() - touchDownTime <= 300) {
                    //点击时间小于300ms，认为是点击操作
                    toggle()
                } else if (isDragState) {
                    //在拖动状态，计算按钮位置，设置是否切换状态
                    val eventX = event.x
                    var fraction = eventX / getWidth()
                    fraction = Math.max(0f, Math.min(1f, fraction))
                    val newCheck = fraction > .5f
                    if (newCheck == isChecked()) {
                        pendingCancelDragState()
                    } else {
                        isChecked = newCheck
                        pendingSettleState()
                    }
                } else if (isPendingDragState) {
                    //在准备进入拖动状态过程中，取消之，复位
                    pendingCancelDragState()
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                isTouchingDown = false
                removeCallbacks(postPendingDrag)

                if (isPendingDragState || isDragState) {
                    //复位
                    pendingCancelDragState()
                }
            }
        }
        return true
    }

    /**
     * 设置是否启用阴影效果
     *
     * @param shadowEffect true.启用
     */
    fun setShadowEffect(shadowEffect: Boolean) {
        if (this.shadowEffect == shadowEffect) {
            return
        }
        this.shadowEffect = shadowEffect

        if (this.shadowEffect) {
            buttonPaint!!.setShadowLayer(
                    shadowRadius.toFloat(),
                    0f, shadowOffset.toFloat(),
                    shadowColor)
        } else {
            buttonPaint!!.setShadowLayer(
                    0f,
                    0f, 0f,
                    0)
        }
    }

    fun setEnableEffect(enable: Boolean) {
        this.enableEffect = enable
    }

    /**
     * 开始进入拖动状态
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private fun pendingDragState() {
        if (isInAnimating) {
            return
        }
        if (!isTouchingDown) {
            return
        }

        if (valueAnimator!!.isRunning) {
            valueAnimator!!.cancel()
        }

        animateState = ANIMATE_STATE_PENDING_DRAG

        beforeState!!.copy(viewState!!)
        afterState!!.copy(viewState!!)

        if (isChecked()) {
            afterState!!.checkStateColor = checkedColor
            afterState!!.buttonX = buttonMaxX
            afterState!!.checkedLineColor = checkedColor
        } else {
            afterState!!.checkStateColor = uncheckedColor
            afterState!!.buttonX = buttonMinX
            afterState!!.radius = viewRadius
        }

        valueAnimator!!.start()
    }

    /**
     * 取消拖动状态
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private fun pendingCancelDragState() {
        if (isDragState || isPendingDragState) {
            if (valueAnimator!!.isRunning) {
                valueAnimator!!.cancel()
            }

            animateState = ANIMATE_STATE_PENDING_RESET
            beforeState!!.copy(viewState!!)

            if (isChecked()) {
                setCheckedViewState(afterState)
            } else {
                setUncheckViewState(afterState)
            }
            valueAnimator!!.start()
        }
    }

    /**
     * 动画-设置新的状态
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private fun pendingSettleState() {
        if (valueAnimator!!.isRunning) {
            valueAnimator!!.cancel()
        }

        animateState = ANIMATE_STATE_PENDING_SETTLE
        beforeState!!.copy(viewState!!)

        if (isChecked()) {
            setCheckedViewState(afterState)
        } else {
            setUncheckViewState(afterState)
        }
        valueAnimator!!.start()
    }

    override fun setOnClickListener(l: View.OnClickListener?) {}

    override fun setOnLongClickListener(l: View.OnLongClickListener?) {}

    fun setOnCheckedChangeListener(l: OnCheckedChangeListener) {
        onCheckedChangeListener = l
    }

    interface OnCheckedChangeListener {
        fun onCheckedChanged(view: BaseSwitchButton, isChecked: Boolean)
    }


    /** */

    /**
     * 保存动画状态
     */
    private class ViewState internal constructor() {
        /**
         * 按钮x位置[buttonMinX-buttonMaxX]
         */
        internal var buttonX: Float = 0.toFloat()
        /**
         * 状态背景颜色
         */
        internal var checkStateColor: Int = 0
        /**
         * 选中线的颜色
         */
        internal var checkedLineColor: Int = 0
        /**
         * 状态背景的半径
         */
        internal var radius: Float = 0.toFloat()

        fun copy(source: ViewState) {
            this.buttonX = source.buttonX
            this.checkStateColor = source.checkStateColor
            this.checkedLineColor = source.checkedLineColor
            this.radius = source.radius
        }
    }


    companion object {
        private val DEFAULT_WIDTH = dp2pxInt(58f)
        private val DEFAULT_HEIGHT = dp2pxInt(36f)
        //Joann

        /**
         * 按钮线的高度
         */
        private val LINE_HEIGHT = 2
//        private val LINE_HEIGHT = 16f
        /**
         * 按钮左右偏移量
         */
        private val BUTTON_OFFSET = 8
//        private val BUTTON_OFFSET = 0
        //Joann

        /** */
        private fun dp2px(dp: Float): Float {
            val r = Resources.getSystem()
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.displayMetrics)
        }

        private fun mdp2px(dp: Float): Float {
            val density = Resources.getSystem().displayMetrics.density
            return dp * density + 0.5f
        }

        private fun dp2pxInt(dp: Float): Int {
            return dp2px(dp).toInt()
        }

        private fun optInt(typedArray: TypedArray?,
                           index: Int,
                           def: Int): Int {
            return typedArray?.getInt(index, def) ?: def
        }

        private fun optPixelSize(typedArray: TypedArray?,
                                 index: Int,
                                 def: Float): Float {
            return typedArray?.getDimension(index, def) ?: def
        }

        private fun optPixelSize(typedArray: TypedArray?,
                                 index: Int,
                                 def: Int): Int {
            return typedArray?.getDimensionPixelOffset(index, def) ?: def
        }

        private fun optColor(typedArray: TypedArray?,
                             index: Int,
                             def: Int): Int {
            return typedArray?.getColor(index, def) ?: def
        }

        private fun optBoolean(typedArray: TypedArray?,
                               index: Int,
                               def: Boolean): Boolean {
            return typedArray?.getBoolean(index, def) ?: def
        }
    }

}