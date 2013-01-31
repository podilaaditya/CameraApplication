package com.aptina.camera.components;

/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * A button designed to be used for the on-screen shutter button.
 * It's currently an {@code ImageView} that can call a delegate when the
 * pressed state changes.
 */
public class ShutterButton extends ImageView {
    
    /**
     * A callback to be invoked when a ShutterButton's pressed state changes.
     */
    public interface OnShutterButtonListener {
        
        /**
         * Called when a ShutterButton is in focus.
         *
         * @param shutterButton The ShutterButton that was pressed.
         */
        void onShutterButtonFocus(ShutterButton shutterButton, boolean pressed);
        
        /**
         * Called when a ShutterButton has been pressed.
         * 
         * @param shutterButton
         */
        void onShutterButtonClick(ShutterButton shutterButton);
    }

    /**
     * Event listener.
     */
    private OnShutterButtonListener mListener;

    /**
     * Pressed indicator.
     */
    private boolean mOldPressed;

    /**
     * Constructor for creating from code.
     * 
     * @param context Application context.
     */
    public ShutterButton(Context context) {
        super(context);
    }

    /**
     * Constructor for creating from XML.
     * 
     * @param context Application context.
     * @param attrs Attribute set.
     */
    public ShutterButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Constructor for creating from XML with style resource.
     * 
     * @param context Application context.
     * @param attrs Attribute set.
     * @param defStyle Style resource.
     */
    public ShutterButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Sets click listener.
     * 
     * @param listener {@linkplain OnShutterButtonListener} to respond click events.
     */
    public void setOnShutterButtonListener(OnShutterButtonListener listener) {
        mListener = listener;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        final boolean pressed = isPressed();
        if (pressed != mOldPressed) {
            if (!pressed) {
                post(new Runnable() {
                    public void run() {
                        callShutterButtonFocus(pressed);
                    }
                });
            } else {
                callShutterButtonFocus(pressed);
            }
            mOldPressed = pressed;
        }
    }

    /**
     * Called when shutter button is pressed.
     * 
     * @param pressed Indicates whether button is pressed.
     */
    private void callShutterButtonFocus(boolean pressed) {
        if (mListener != null) {
            mListener.onShutterButtonFocus(this, pressed);
        }
    }

    @Override
    public boolean performClick() {
        boolean result = super.performClick();
        if (mListener != null) {
            mListener.onShutterButtonClick(this);
        }
        return result;
    }
}
