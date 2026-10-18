package com.simonimal.ministart.ui.animations

import android.view.MotionEvent
import android.widget.TextView


object ViewAnimations {

    /**
     * Adds shrinking text and/or color fading,
     * when the argument text is pressed
     */
    fun textClickAnimations(
        textView: TextView,
        fade: Boolean = true,
        shrink: Boolean = false
    ) {

        fun animateView(alpha: Float, scale: Float, duration: Long) {
            val anim = textView.animate()

            if (fade) anim.alpha(alpha)

            if (shrink) {
                anim.scaleX(scale)
                anim.scaleY(scale)
            }

            anim.setDuration(duration).start()
        }

        textView.setOnTouchListener { v, event ->
            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    animateView(0.5f, 0.9f, 150)
                }

                MotionEvent.ACTION_UP -> {
                    v.performClick()  // for accessibility services
                    animateView(1f, 1f, 100)
                }

                MotionEvent.ACTION_CANCEL -> {
                    animateView(1f, 1f, 100)
                }

                MotionEvent.ACTION_MOVE -> {
                    val inside =
                        event.x in 0f..v.width.toFloat() &&
                        event.y in 0f..v.height.toFloat()
                    if (!inside) {
                        animateView(1f, 1f, 150)
                    }
                }
            }

            false
        }
    }
}