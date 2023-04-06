/*
 * Copyright 2017 LINE Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.linecorp.clay.view.state

import android.view.MotionEvent

/**
 * The interface of touch events
 */
interface TouchEventHandler {
    /**
     * onTouchBegin, it is invoked when touch begins
     *
     * @param event MotionEvent
     */
    fun onTouchBegin(event: MotionEvent)

    /**
     * onTouchMove, it is invoked when touch is moving
     *
     * @param event MotionEvent
     */
    fun onTouchMove(event: MotionEvent)

    /**
     * onTouchEnd, it is invoked when touch ends
     *
     * @param event MotionEvent
     */
    fun onTouchEnd(event: MotionEvent)
}

/**
 * Base class for edit state
 */
open class EditState<out T>(val editingObject: T) : TouchEventHandler {

    private var touchCount = 0L

    internal var isSteady: Boolean = false
        get() = touchCount > TOLERANCE_TOUCH_COUNT
        private set

    /**
     * invoked after onTouchMove event
     */
    var afterTouchMove: () -> Unit = {}
    /**
     * invoked after onTouchEnd event
     */
    var afterTouchEnd: () -> Unit = {}

    /**
     * The event handler of the onTouchMove
     */
    protected open fun doOnTouchMove(event: MotionEvent) {

    }

    /**
     * The event handler of the onTouchEnd
     */
    protected open fun doOnTouchEnd(event: MotionEvent) {

    }

    override fun onTouchBegin(event: MotionEvent) {
        //not used yet
    }

    final override fun onTouchMove(event: MotionEvent) {
        doOnTouchMove(event)
        ++touchCount
        afterTouchMove()
    }

    final override fun onTouchEnd(event: MotionEvent) {
        doOnTouchEnd(event)
        afterTouchEnd()
    }

    companion object {
        /**
         * Tolerance touch count: use for single or double touch boundary.
         *                        if touch count exceeds this, the state should not be changed until touch end
         */
        private const val TOLERANCE_TOUCH_COUNT = 2
    }
}
