package com.owl93.dpb

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.*
import androidx.core.content.ContextCompat
import kotlin.math.max
import kotlin.math.min




class CircularProgressView: View {
    //default values
    private val DEFAULT_STROKE_COLOR: Int = ContextCompat.getColor(context, R.color.teal)
    private val DEFAULT_TEXT_COLOR: Int = ContextCompat.getColor(context, R.color.teal)
    private val DEFAULT_TEXT_SIZE: Float = 100f
    private val DEFAULT_STARTING_ANGLE: Int = 0
    private val DEFAULT_DIRECTION: Direction = Direction.CW
    private val DEFAULT_STROKE_WIDTH: Float = 20f
    private val DEFAULT_DRAW_TRACK: Boolean = true

    private val DEFAULT_MAX_VALUE: Float = 100f
    private val DEFAULT_DRAW_TEXT: Boolean = true
    private val DEFAULT_TEXT_FORMAT: TextFormat = TextFormat.PERCENT
    private val DEFAULT_INTERPOLATOR_IDX: Int = 0
    private val DEFAULT_ANIMATION_DURATION: Long = 400L

    private val DEFAULT_STK_GRAD_STYLE = Gradient.STYLE_SWEEP
    private val DEFAULT_STK_GRAD_LINEAR_ANGLE = 0
    private val DEFAULT_STK_GRAD_SIZE = StrokeGradient.STROKE_ONLY

    private val DEFAULT_TXT_GRAD_STYLE = Gradient.STYLE_SWEEP
    private val DEFAULT_TXT_LINEAR_ANGLE = 0
    private val DEFAULT_TXT_GRAD_SIZE = TextGradient.TEXT_ONLY

    private val thirdsGradientPositions = floatArrayOf(0f, 1/6f, 1/3f, 1/2f, 2/3f, 5/6f)
    private val halfGradientPositions = floatArrayOf(0f, 1/4f, 3/4f, 1f)


    private var _progress: Float = DEFAULT_MAX_VALUE

    private var minDimen = 0f
    private var textWidth = 0f
    private var textHeight = 0f

    //pre-alloc vars/vals that are updated and used in each frame
    private var strokePaint = Paint().also{
        it.style = Paint.Style.STROKE
        it.strokeCap = Paint.Cap.BUTT
        it.flags = Paint.ANTI_ALIAS_FLAG
    }

    private var trackPaint = Paint().also {
        it.style = Paint.Style.STROKE
        it.strokeCap = Paint.Cap.BUTT
        it.flags = Paint.ANTI_ALIAS_FLAG
        it.alpha = 0
    }

    private var textPaint = Paint().also {
        it.flags = Paint.ANTI_ALIAS_FLAG
        it.textAlign = Paint.Align.CENTER
    }
    private var textBounds = Rect()
    private var strokeShader: Shader? = null
    private var textShader: Shader? = null
    var animationListener: DeterminateProgressViewListener? = null

    //configurable vars
    var maxValue: Float = DEFAULT_MAX_VALUE
        set(value) {
            field = value
            postInvalidate()
            //Log.d(TAG, "set maxvalue: $value")
        }

    var strokeColor: Int = DEFAULT_STROKE_COLOR
        set(value) {
            field = value
            postInvalidate()
            //Log.d(TAG, "set strokeColor: $value")
        }

    private var strokeGradientMode = false
    var gradientStartColor: Int = 0
        set(value) {
            field = value
            regenerateStrokeShader()
            postInvalidate()
            //Log.d(TAG, "set gradientStart: $value")
        }

    var gradientCenterColor: Int = 0
        set(value) {
            field = value
            regenerateStrokeShader()
            postInvalidate()
            //Log.d(TAG, "set gradientCenter: $value")
        }

    var gradientEndColor: Int = 0
        set(value) {
            field = value
            regenerateStrokeShader()
            postInvalidate()
            //Log.d(TAG, "set gradientEnd: $value")
        }

    var strokeGradientStyle: Gradient = DEFAULT_STK_GRAD_STYLE
        set(value) {
            field = value
            regenerateStrokeShader()
            postInvalidate()
            //Log.d(TAG, "set gradientStyle: $value")
        }

    var strokeGradientLinearAngle: Int = DEFAULT_STK_GRAD_LINEAR_ANGLE
        set(value) {
            field = value
            regenerateStrokeShader()
            postInvalidate()
            //Log.d(TAG, "set strokeLinearAngle: $value")
        }

    var strokeGradientSize: StrokeGradient = DEFAULT_STK_GRAD_SIZE
        set(value) {
            field = value
            regenerateStrokeShader()
            postInvalidate()
           //Log.d(TAG, "set strokeRadialSize: $value")
        }

    var drawTrack: Boolean = DEFAULT_DRAW_TRACK
        set(value) {
            field = value
            postInvalidate()
            //Log.d(TAG, "set drawTrack: $value")
        }

    var trackColor: Int = DEFAULT_STROKE_COLOR
        set(value) {
            field = value
            postInvalidate()
            //Log.d(TAG, "set trackColor: $value")

        }

    var trackWidth: Float = DEFAULT_STROKE_WIDTH/2
        set(value) {
            field = value
            postInvalidate()
            Log.d(TAG, "set trackWidth: $value")

        }

    var trackAlpha: Int = DEFAULT_TRACK_ALPHA
        set(value) {
            field = value
            postInvalidate()
            //Log.d(TAG, "set trackAlpha: $value")
        }

    var strokeWidth: Float = DEFAULT_STROKE_WIDTH
        set(value) {
            field = value
            regenerateStrokeShader()
            postInvalidate()
            //Log.d(TAG, "set strokeWidth: $value")
        }

    var progress: Float = DEFAULT_MAX_VALUE
        set(value) {
            if (!isAnimating) {
                field = value
                _progress = value
                postInvalidate()
                //Log.d(TAG, "set progress: $value")
            } else Log.w(TAG, "can't set progress when animating")
        }

    var startingAngle: Int = DEFAULT_STARTING_ANGLE
        set(value) {
            field = value % 360
            postInvalidate()
            //Log.d(TAG, "set startingAngle: $value")
        }

    var direction: Direction = DEFAULT_DIRECTION
        set(value){
            field = value
            postInvalidate()
            //Log.d(TAG, "set direction: $value")
        }

    var textEnabled: Boolean = DEFAULT_DRAW_TEXT
        set(value) {
            field = value
            postInvalidate()
            //Log.d(TAG, "set textEnabled: $value")
        }

    var textSize: Float = DEFAULT_STROKE_WIDTH/2
        set(value) {
            field = value
            postInvalidate()
            //Log.d(TAG, "set textSize: $value")
        }

    var textColor: Int = DEFAULT_STROKE_COLOR
        set(value) {
            field = value
            postInvalidate()
            //Log.d(TAG, "set textColor: $value")
        }

    private var textGradientMode = false
    var textGradientStartColor: Int = 0
        set(value) {
            field = value
            postInvalidate()
           //Log.d(TAG, "set textGradientStart: $value")
        }

    var textGradientCenterColor: Int = 0
        set(value) {
            field = value
            postInvalidate()
            //Log.d(TAG, "set textGradientCenter: $value")
        }

    var textGradientEndColor: Int = 0
        set(value) {
            field = value
            postInvalidate()
            //Log.d(TAG, "set textGradientEnd: $value")
        }

    var textGradientStyle: Gradient = DEFAULT_TXT_GRAD_STYLE
        set(value) {
            field = value
            postInvalidate()
            //Log.d(TAG, "set textGradientStyle: $value")
        }

    var textGradientLinearAngle: Int = DEFAULT_TXT_LINEAR_ANGLE
        set(value) {
            field = value
            postInvalidate()
            //Log.d(TAG, "set textGradientLinearAngle: $value")
        }


    var textGradientSize: TextGradient = DEFAULT_TXT_GRAD_SIZE
        set(value) {
            field = value
            postInvalidate()
            //Log.d(TAG, "set textGradientSize: $value")
        }

    var textFormat: TextFormat = DEFAULT_TEXT_FORMAT
        set(value) {
            field = value
            postInvalidate()
            //Log.d(TAG, "set textFormat: $value")
        }

    var text: String? = null
        set(value) {
            field = value
            postInvalidate()
            //Log.d(TAG, "set text: $value")
        }

    var animationDuration: Long = DEFAULT_ANIMATION_DURATION
    var animationInterpolator: BaseInterpolator = interpolators[DEFAULT_INTERPOLATOR_IDX]


    //Constructors
    constructor(context: Context): super(context) {
        init(null)
    }

    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet) {
        init(attributeSet)
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int): super(context, attributeSet, defStyleAttr){
        init(attributeSet)
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(context, attributeSet, defStyleAttr, defStyleRes){
    }


    //initialization and reading of XML attrs
    private fun init(attributeSet: AttributeSet?) {
        if(attributeSet == null) return
        val attrs = context.theme.obtainStyledAttributes(attributeSet, R.styleable.CircularProgressView, 0, 0)

        try {
            maxValue = attrs.getFloat(R.styleable.CircularProgressView_maxValue, DEFAULT_MAX_VALUE)
            strokeWidth = attrs.getDimension(R.styleable.CircularProgressView_strokeWidth, DEFAULT_STROKE_WIDTH)
            strokeColor = attrs.getColor(R.styleable.CircularProgressView_strokeColor, DEFAULT_STROKE_COLOR)
            drawTrack = attrs.getBoolean(R.styleable.CircularProgressView_drawTrack, DEFAULT_DRAW_TRACK)
            trackWidth = attrs.getFloat(R.styleable.CircularProgressView_trackWidth, DEFAULT_STROKE_WIDTH/2)
            trackColor = attrs.getColor(R.styleable.CircularProgressView_trackColor, DEFAULT_STROKE_COLOR)
            trackAlpha = attrs.getInt(R.styleable.CircularProgressView_trackAlpha, -1)
            gradientStartColor = attrs.getColor(R.styleable.CircularProgressView_gradientStartColor, 0)
            gradientCenterColor = attrs.getColor(R.styleable.CircularProgressView_gradientCenterColor, 0)
            gradientEndColor = attrs.getColor(R.styleable.CircularProgressView_gradientEndColor, 0)
            strokeGradientStyle = when(attrs.getInt(R.styleable.CircularProgressView_strokeGradientStyle, 0)){
                1 -> Gradient.STYLE_LINEAR
                2 -> Gradient.STYLE_RADIAL
                else -> Gradient.STYLE_SWEEP
            }
            strokeGradientLinearAngle = attrs.getInt(R.styleable.CircularProgressView_strokeGradient_linearAngle, 0)
            strokeGradientSize = if(attrs.getInt(R.styleable.CircularProgressView_strokeGradientSize, 1) == 1)
                StrokeGradient.STROKE_ONLY
            else StrokeGradient.VIEW

            progress = attrs.getFloat(R.styleable.CircularProgressView_progress, DEFAULT_MAX_VALUE/2)
            startingAngle = attrs.getInt(R.styleable.CircularProgressView_startingAngle, DEFAULT_STARTING_ANGLE) % 360
            text = attrs.getString(R.styleable.CircularProgressView_text)
            textEnabled = attrs.getBoolean(R.styleable.CircularProgressView_textEnabled, !text.isNullOrEmpty())
            textColor = attrs.getColor(R.styleable.CircularProgressView_textColor, DEFAULT_TEXT_COLOR)

            textGradientStartColor = attrs.getColor(R.styleable.CircularProgressView_textGradientStartColor, 0)
            textGradientCenterColor = attrs.getColor(R.styleable.CircularProgressView_textGradientCenterColor, 0)
            textGradientEndColor = attrs.getColor(R.styleable.CircularProgressView_textGradientEndColor, 0)
            textGradientStyle = when(attrs.getInt(R.styleable.CircularProgressView_textGradientStyle, 0)) {
                1 -> Gradient.STYLE_LINEAR
                2 -> Gradient.STYLE_RADIAL
                else -> Gradient.STYLE_SWEEP
            }
            textGradientLinearAngle = attrs.getInt(R.styleable.CircularProgressView_textGradient_linearAngle, 0)
            textGradientSize = if(attrs.getInt(R.styleable.CircularProgressView_textGradientSize, 1) == 1)
                TextGradient.TEXT_ONLY
            else TextGradient.VIEW

            textSize = attrs.getDimension(R.styleable.CircularProgressView_textSize, DEFAULT_TEXT_SIZE)
            textFormat = when(attrs.getInt(R.styleable.CircularProgressView_textFormat, 0)){
                1 -> TextFormat.DECIMAL_PERCENT
                2 -> TextFormat.FLOAT
                3 -> TextFormat.INT
                else -> TextFormat.PERCENT
            }
            direction = if(attrs.getInt(R.styleable.CircularProgressView_direction, 0) == 0)
                Direction.CW else Direction.CCW
            animationDuration = attrs.getInt(R.styleable.CircularProgressView_progressAnimationDuration, DEFAULT_ANIMATION_DURATION.toInt()).toLong()
            animationInterpolator = interpolators[attrs.getInt(R.styleable.CircularProgressView_animationInterpolator, DEFAULT_INTERPOLATOR_IDX)]
        }finally {
            attrs.recycle()
        }

        // set up gradient data - use gradients if provided with start * end colors

        determineGradientStatus()
        //Stroke gradient configuration
        if(strokeGradientMode) regenerateStrokeShader()

        //Text gradient configuration
        if(textGradientMode) regenerateTextShader()
    }


    private fun determineGradientStatus() {
        strokeGradientMode = gradientStartColor != 0 && gradientEndColor != 0
        textGradientMode = textGradientStartColor != 0 && textGradientEndColor != 0
    }

    private fun determineGradientDetails(start: Int, center: Int, end: Int, style: Gradient): Pair<IntArray, FloatArray> {
        return if(center == 0) {
            val computedCenter = computeMiddleColor(start, end)
            Pair(if(style == Gradient.STYLE_SWEEP) intArrayOf(computedCenter, end, start, computedCenter) else intArrayOf(start, end), halfGradientPositions)
        } else {
            Pair(if(style== Gradient.STYLE_SWEEP) intArrayOf(start, center, center, end, end, start) else intArrayOf(start, center, end),thirdsGradientPositions)
        }
    }


    private fun computeStrokeRadialPositions(strokeEnd: Float, strokeWidth: Float, colors: IntArray): FloatArray? {
        if(strokeGradientSize == StrokeGradient.VIEW) return null
        val colorStart = strokeEnd-strokeWidth
        val strokeCenter = strokeWidth/2
        return if(colors.size == 3) floatArrayOf(colorStart/strokeEnd, (colorStart + strokeCenter)/strokeEnd, 1f)
        else floatArrayOf(colorStart/strokeEnd, 1f)
    }


    private fun computeLinearAngle(angle: Int, bounds: RectF): FloatArray  {
        val xStart = bounds.left
        val xEnd = bounds.right
        val yStart = bounds.top
        val yEnd = bounds.bottom
        val md = max(bounds.width(), bounds.height())/2
        val c = bounds.centerX()

        return when(angle % 360) {
            0 -> floatArrayOf(xStart, yStart, xEnd, yStart)
            45 -> floatArrayOf(c - md, yStart, c + md, yEnd)
            90 -> floatArrayOf(c, yStart, c, yEnd)
            135 -> floatArrayOf(c + md, yStart, c - md, yEnd)
            180 -> floatArrayOf(xEnd, yStart, xStart, yStart)
            225 -> floatArrayOf(c + md, yEnd, c - md, yStart)
            270 -> floatArrayOf(c, yEnd, c, yStart)
            315 -> floatArrayOf(c - md, yEnd, c + md, yStart)
            else -> {
                Log.w(TAG,"Linear Angle $angle not valid")
                floatArrayOf(xStart, yStart, xEnd, yStart)
            }
        }
    }

    private fun regenerateStrokeShader() {
        if (minDimen == 0f || !strokeGradientMode) return
        val c = minDimen/2
        val details = determineGradientDetails(gradientStartColor, gradientCenterColor, gradientEndColor, strokeGradientStyle)
        strokeShader = when (strokeGradientStyle) {
            Gradient.STYLE_SWEEP -> SweepGradient(c, c, details.first, details.second)
            Gradient.STYLE_LINEAR -> {
                val points = computeLinearAngle(strokeGradientLinearAngle, RectF(0f, 0f, minDimen, minDimen))
                LinearGradient(points[0], points[1], points[2], points[3], details.first, null, Shader.TileMode.CLAMP)
            }
            Gradient.STYLE_RADIAL-> {
                val end = if(!drawTrack || strokeWidth >= trackWidth) c else {
                    c - ((trackWidth - strokeWidth)/2)
                }
                val positions = computeStrokeRadialPositions(end, strokeWidth, details.first)
                RadialGradient(c, c, end, details.first, positions, Shader.TileMode.CLAMP)
            }
        }
    }


    private fun getTextGradientBounds(grad: TextGradient): RectF {
        return if(grad == TextGradient.VIEW) {
            RectF(0f, 0f, minDimen, minDimen)
        }else {
            val c = minDimen/2
            RectF(
                c - textBounds.width()/2,
                c - textBounds.height()/2,
                c + textBounds.width()/2,
                c + textBounds.height()/2
            )
        }
    }

    private fun regenerateTextShader() {
        if(minDimen == 0f) return
        val c = minDimen/2
        val details = determineGradientDetails(textGradientStartColor, textGradientCenterColor, textGradientEndColor, textGradientStyle)
        if(textGradientMode) {
            textShader = when(textGradientStyle) {
                Gradient.STYLE_SWEEP -> SweepGradient(c, c, details.first, details.second)
                Gradient.STYLE_LINEAR -> {
                    val tb = getTextGradientBounds(textGradientSize)
                    val points = computeLinearAngle(textGradientLinearAngle, tb)
                    LinearGradient(points[0], points[1], points[2], points[3], details.first, null, Shader.TileMode.CLAMP)
                }
                Gradient.STYLE_RADIAL -> RadialGradient(c, c, if(textGradientSize == TextGradient.TEXT_ONLY)
                    max(textBounds.width()/2, textBounds.height()/2).toFloat() else minDimen, details.first, null, Shader.TileMode.CLAMP)
            }
        }
    }


    override fun onDraw(canvas: Canvas?) {
        if(canvas == null) return
        determineGradientStatus()
        minDimen = min(width, height).toFloat()

        regenerateStrokeShader()
        strokePaint.let {
            it.color = strokeColor
            it.strokeWidth = strokeWidth
            it.shader =  if(strokeGradientMode) strokeShader else null
        }

        var degrees = (_progress/maxValue) * 360f
        if(direction == Direction.CCW) degrees *= -1
        val startingAngle = -90f + startingAngle
        val maxStroke = if(drawTrack) max(strokeWidth, trackWidth) else strokeWidth
        if(drawTrack){
            trackPaint.let {
                it.color = if(trackColor == 0) strokeColor else trackColor
                it.strokeWidth = trackWidth
                it.alpha = if(trackAlpha == -1) DEFAULT_TRACK_ALPHA else trackAlpha
            }
            canvas.drawArc(maxStroke/2, maxStroke/2, minDimen-maxStroke/2, minDimen-maxStroke/2,
                    0f, 360f, false, trackPaint)

        }

        canvas.drawArc(
            maxStroke/2,
            maxStroke/2,
            minDimen-maxStroke/2,
            minDimen-maxStroke/2,
            startingAngle, degrees, false, strokePaint
        )

        if(textEnabled) {
            val text = text ?: formatText()
            textPaint.getTextBounds(text, 0, text.length, textBounds)
            textHeight = textBounds.height().toFloat()
            textWidth = textBounds.width().toFloat()
            regenerateTextShader()
            textPaint.let {
                it.color = textColor
                it.textSize = textSize
                it.shader = if(textGradientMode) textShader else null
            }
            val c = minDimen/2
            canvas.drawText(text, c, c + textBounds.height()/2, textPaint)
        }
    }

    //text formatter
    private fun formatText(): String {
        val percent = _progress/maxValue * 100
        return when (textFormat) {
            TextFormat.PERCENT -> String.format("%3.0f%%", percent)
            TextFormat.DECIMAL_PERCENT -> String.format("%3.2f%%", percent)
            TextFormat.INT -> _progress.toInt().toString()
            else -> String.format("%#.2f", _progress)
        }
    }


    //animate progress chnages
    private var isAnimating = false
    fun animateProgressChange(toValue: Float, duration: Long = animationDuration) {
        if(isAnimating) return
        ValueAnimator.ofFloat(_progress, toValue).apply {
            interpolator = animationInterpolator
            this.duration = duration
            addUpdateListener {
                _progress = it.animatedValue as Float
                invalidate()
                isAnimating = it.animatedFraction != 1f
                if(!isAnimating) {
                    //Log.d(TAG, "isAnimating: $isAnimating")
                    progress = _progress
                    animationListener?.onAnimationEnd()
                }
            }
        }.start()
        isAnimating = true
        //Log.d(TAG, "isAnimating: $isAnimating")
        animationListener?.onAnimationStart(toValue, _progress)
    }


    private fun computeMiddleColor(color1: Int, color2 :Int): Int {
        val alpha = (Color.alpha(color1) + Color.alpha(color2))/2
        val red = (Color.red(color1) + Color.red(color2))/2
        val green = (Color.green(color1) + Color.green(color2))/2
        val blue = (Color.blue(color1) + Color.blue(color2))/2
        return Color.argb(alpha, red, green, blue)
    }


    companion object {
        private const val TAG = "CircularProgressView"
        const val DEFAULT_TRACK_ALPHA: Int = 125
        private val interpolators = arrayOf(DecelerateInterpolator(),
            AccelerateInterpolator(),
            AccelerateDecelerateInterpolator(),
            LinearInterpolator(),
            AnticipateInterpolator(),
            AnticipateOvershootInterpolator(),
            AnticipateOvershootInterpolator()
        )
    }

}


interface DeterminateProgressViewListener {
    fun onAnimationStart(from: Float, to: Float)
    fun onAnimationEnd()
}

enum class TextFormat {
    PERCENT,
    DECIMAL_PERCENT,
    INT,
    FLOAT
}
enum class Direction {
    CCW,
    CW
}

enum class Gradient {
    STYLE_SWEEP,
    STYLE_RADIAL,
    STYLE_LINEAR,
}

enum class StrokeGradient {
    VIEW,
    STROKE_ONLY
}

enum class TextGradient {
    VIEW,
    TEXT_ONLY
}