

package com.duckduckgo.mobile.android.vpn.ui.util

import android.graphics.*
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RectShape
import android.graphics.drawable.shapes.RoundRectShape
import androidx.core.graphics.toColorInt
import java.util.*
import kotlin.math.absoluteValue

/**
 * @author amulya
 * @datetime 14 Oct 2014, 3:53 PM
 */
class TextDrawable private constructor(builder: Builder) : ShapeDrawable(builder.shape) {
    private val textPaint: Paint
    private val borderPaint: Paint
    private val text: String
    private val color: Int
    private val shape: RectShape
    private val height: Int
    private val width: Int
    private val fontSize: Int
    private val radius: Float
    private val borderThickness: Int

    private fun getDarkerShade(color: Int): Int {
        return Color.rgb(
            (SHADE_FACTOR * Color.red(color)).toInt(),
            (SHADE_FACTOR * Color.green(color)).toInt(),
            (SHADE_FACTOR * Color.blue(color)).toInt(),
        )
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        val r = bounds

        // draw border
        if (borderThickness > 0) {
            drawBorder(canvas)
        }
        val count = canvas.save()
        canvas.translate(r.left.toFloat(), r.top.toFloat())

        // draw text
        val width = if (width < 0) r.width() else width
        val height = if (height < 0) r.height() else height
        val fontSize = if (fontSize < 0) Math.min(width, height) / 2 else fontSize
        textPaint.textSize = fontSize.toFloat()
        canvas.drawText(text, (width / 2).toFloat(), height / 2 - (textPaint.descent() + textPaint.ascent()) / 2, textPaint)
        canvas.restoreToCount(count)
    }

    private fun drawBorder(canvas: Canvas) {
        val rect = RectF(bounds)
        rect.inset((borderThickness / 2).toFloat(), (borderThickness / 2).toFloat())
        if (shape is OvalShape) {
            canvas.drawOval(rect, borderPaint)
        } else if (shape is RoundRectShape) {
            canvas.drawRoundRect(rect, radius, radius, borderPaint)
        } else {
            canvas.drawRect(rect, borderPaint)
        }
    }

    override fun setAlpha(alpha: Int) {
        textPaint.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        textPaint.colorFilter = cf
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun getIntrinsicWidth(): Int {
        return width
    }

    override fun getIntrinsicHeight(): Int {
        return height
    }

    class Builder : IConfigBuilder, IShapeBuilder, IBuilder {
        var text = ""
        var color: Int
        var borderThickness: Int
        var width: Int
        var height: Int
        var font: Typeface
        var shape: RectShape
        var textColor: Int
        var fontSize: Int
        var isBold: Boolean
        var toUpperCase: Boolean
        var radius = 0f
        override fun width(width: Int): IConfigBuilder {
            this.width = width
            return this
        }

        override fun height(height: Int): IConfigBuilder {
            this.height = height
            return this
        }

        override fun textColor(color: Int): IConfigBuilder {
            textColor = color
            return this
        }

        override fun withBorder(thickness: Int): IConfigBuilder {
            borderThickness = thickness
            return this
        }

        override fun useFont(font: Typeface): IConfigBuilder {
            this.font = font
            return this
        }

        override fun fontSize(size: Int): IConfigBuilder {
            fontSize = size
            return this
        }

        override fun bold(): IConfigBuilder {
            isBold = true
            return this
        }

        override fun toUpperCase(): IConfigBuilder {
            toUpperCase = true
            return this
        }

        override fun beginConfig(): IConfigBuilder {
            return this
        }

        override fun endConfig(): IShapeBuilder {
            return this
        }

        override fun rect(): IBuilder {
            shape = RectShape()
            return this
        }

        override fun round(): IBuilder {
            shape = OvalShape()
            return this
        }

        override fun roundRect(radius: Int): IBuilder {
            this.radius = radius.toFloat()
            val radii = floatArrayOf(
                radius.toFloat(),
                radius.toFloat(),
                radius.toFloat(),
                radius.toFloat(),
                radius.toFloat(),
                radius.toFloat(),
                radius.toFloat(),
                radius.toFloat(),
            )
            shape = RoundRectShape(radii, null, null)
            return this
        }

        override fun buildRect(
            text: String,
            color: Int?,
        ): TextDrawable {
            rect()
            return build(text, color)
        }

        override fun buildRoundRect(
            text: String,
            color: Int?,
            radius: Int,
        ): TextDrawable {
            roundRect(radius)
            return build(text, color)
        }

        override fun buildRound(
            text: String,
            color: Int?,
        ): TextDrawable {
            round()
            return build(text, color)
        }

        override fun build(
            text: String,
            color: Int?,
        ): TextDrawable {
            this.color = color ?: textToColor(text)
            this.text = text
            return TextDrawable(this)
        }

        private fun textToColor(text: String): Int {
            val palette = listOf(
                "#94B3AF",
                "#727998",
                "#645468",
                "#4D5F7F",
                "#855DB6",
                "#5E5ADB",
                "#678FFF",
                "#6BB4EF",
                "#4A9BAE",
                "#66C4C6",
                "#55D388",
                "#99DB7A",
                "#ECCC7B",
                "#E7A538",
                "#DD6B4C",
                "#D65D62",
            )

            // hasCode() is consistent for the same text
            return text.hashCode().absoluteValue.let {
                val index = (it % palette.size)
                palette[index]
            }.toColorInt()
        }

        init {
            color = Color.GRAY
            textColor = Color.WHITE
            borderThickness = 0
            width = -1
            height = -1
            shape = RectShape()
            font = Typeface.create("sans-serif-light", Typeface.NORMAL)
            fontSize = -1
            isBold = false
            toUpperCase = false
        }
    }

    interface IConfigBuilder {
        fun width(width: Int): IConfigBuilder
        fun height(height: Int): IConfigBuilder
        fun textColor(color: Int): IConfigBuilder
        fun withBorder(thickness: Int): IConfigBuilder
        fun useFont(font: Typeface): IConfigBuilder
        fun fontSize(size: Int): IConfigBuilder
        fun bold(): IConfigBuilder
        fun toUpperCase(): IConfigBuilder
        fun endConfig(): IShapeBuilder
    }

    interface IBuilder {
        fun build(
            text: String,
            color: Int? = null,
        ): TextDrawable
    }

    interface IShapeBuilder {
        fun beginConfig(): IConfigBuilder
        fun rect(): IBuilder?
        fun round(): IBuilder?
        fun roundRect(radius: Int): IBuilder?
        fun buildRect(
            text: String,
            color: Int? = null,
        ): TextDrawable

        fun buildRoundRect(
            text: String,
            color: Int? = null,
            radius: Int,
        ): TextDrawable

        fun buildRound(
            text: String,
            color: Int? = null,
        ): TextDrawable
    }

    companion object {
        private const val SHADE_FACTOR = 0.9f
        fun builder(): IShapeBuilder {
            return Builder()
        }

        /**
         * @return a round [TextDrawable] from a given text. The background color is automatically calculated from the
         * text string
         */
        fun asIconDrawable(text: String): TextDrawable {
            return builder().buildRound(text.take(1))
        }
    }

    init {

        // shape properties
        shape = builder.shape
        height = builder.height
        width = builder.width
        radius = builder.radius

        // text and color
        text = if (builder.toUpperCase) builder.text.uppercase(Locale.getDefault()) else builder.text
        color = builder.color

        // text paint settings
        fontSize = builder.fontSize
        textPaint = Paint()
        textPaint.color = builder.textColor
        textPaint.isAntiAlias = true
        textPaint.isFakeBoldText = builder.isBold
        textPaint.style = Paint.Style.FILL
        textPaint.typeface = builder.font
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.strokeWidth = builder.borderThickness.toFloat()

        // border paint settings
        borderThickness = builder.borderThickness
        borderPaint = Paint()
        borderPaint.color = getDarkerShade(color)
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = borderThickness.toFloat()

        // drawable paint color
        val paint = paint
        paint.color = color
    }
}
