package com.example.kkoser.gbk

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.kkoser.emulatorcore.gpu.Color
import com.kkoser.emulatorcore.gpu.Renderer

class RendererImpl : View, Renderer {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setWillNotDraw(false)
        setWillNotCacheDrawing(false)
    }

    private val pendingEvents = mutableListOf<PendingPixel>()

    override fun render(x: Int, y: Int, color: Color) {
        val paint = Paint()
        paint.setARGB(255, color.red, color.blue, color.green)
        pendingEvents.add(PendingPixel(paint, x, y))
    }

    override fun refresh() {
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {

        if (canvas == null) return

        canvas.save()
        val bgPaint = Paint()
        bgPaint.setARGB(100, 255, 0, 0)
        canvas.drawPaint(bgPaint)

        val eventsToProcess = ArrayList(this.pendingEvents)
        for (event in eventsToProcess) {
            canvas.drawRect(event.x.toFloat(), (event.x + 1).toFloat(), (event.y).toFloat(), (event.y + 1).toFloat(), event.paint)
        }
        canvas.restore()
    }

    private data class PendingPixel(
            val paint: Paint,
            val x: Int,
            val y: Int
    )
}