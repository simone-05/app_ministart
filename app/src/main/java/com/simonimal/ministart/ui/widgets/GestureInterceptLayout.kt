package com.simonimal.ministart.ui.widgets

import android.util.Log
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewGroup
import android.view.ViewConfiguration
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.abs
import kotlin.math.ln


class GestureInterceptLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    fun interface OnGestureDownListener {
        fun onDown(x: Float, y: Float)
    }

    fun interface OnGestureMoveListener {
        fun onMove(verticalSwipeDir: Int, dx: Float, dy: Float, deltaX: Float, deltaY: Float)
    }

    fun interface OnGestureEndListener {
        fun onUp(velocityX: Float, velocityY: Float)
    }

    fun interface OnDoubleTapListener {
        fun onDoubleTap()
    }

    fun interface OnLongPressListener {
        fun onLongPress()
    }

    var onGestureDownListener: OnGestureDownListener? = null
    var onGestureMoveListener: OnGestureMoveListener? = null
    var onGestureEndListener: OnGestureEndListener? = null
    var onDoubleTapListener: OnDoubleTapListener? = null
    var onLongPressListener: OnLongPressListener? = null

    private var startX = 0f
    private var startY = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var dy = 0f
    private var dx = 0f
    private var absDy = 0f
    private var absDx = 0f

    private var velocityTracker: VelocityTracker? = null
    private var verticalSwipeDir = 0    // 1 -> down, -1 -> up
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                return true // REQUIRED
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                onDoubleTapListener?.onDoubleTap()
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                onLongPressListener?.onLongPress()
            }
        })

    /**
     * Trigger sequence according to the finger position when swipe starts
     *
     * From this view (parent):
     * intercept:    down
     * touchevent:   down, move, up
     *
     * From the children implementing click listeners:
     * intercept:           down, move (until return true)
     * touchevent:         (then moves on here) move, up
     */
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.rawX
                startY = event.rawY
                lastY = startY
                lastX = startX
                velocityTracker?.recycle()
                velocityTracker = VelocityTracker.obtain()
                velocityTracker?.addMovement(event)
                onGestureDownListener?.onDown(event.rawX, event.rawY)
                return false // false so children get DOWN event
            }

            MotionEvent.ACTION_MOVE -> {
                absDx = abs(event.rawX - startX)
                absDy = abs(event.rawY - startY)

                if (absDy > touchSlop || absDx > touchSlop) {
                    return true // intercept
                }
            }
        }
        return super.onInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        /* For double tap and long press,
        only when starting the swipe on this view, not children
        */
        gestureDetector.onTouchEvent(event)

        // Handle swipe here
        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                val deltaY = event.rawY - lastY
                val deltaX = event.rawX - lastX
                lastX = event.rawX
                lastY = event.rawY

                dx = event.rawX - startX
                dy = event.rawY - startY

                velocityTracker?.addMovement(event)

                if (abs(dy) > touchSlop || abs(dx) > touchSlop) {
                    if (verticalSwipeDir == 0) {
                        if (dy > 0) verticalSwipeDir = 1 else verticalSwipeDir = -1 // 1 -> down, -1 -> up
                    }
                    onGestureMoveListener?.onMove(verticalSwipeDir, dx, dy, deltaX, deltaY)
                }
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                velocityTracker?.addMovement(event)
                velocityTracker?.computeCurrentVelocity(1000)

                val vx = velocityTracker?.xVelocity ?: 0f
                val vy = velocityTracker?.yVelocity ?: 0f

                onGestureEndListener?.onUp(vx, vy)
                // Reset variables
                lastX = 0f
                lastY = 0f
                startX = 0f
                startY = 0f
                dx = 0f
                dy = 0f
                absDx = 0f
                absDy = 0f
                velocityTracker?.recycle()
                velocityTracker = null
                verticalSwipeDir = 0
            }
        }
        return true // consume event
    }
}
