package com.aptina.camera.eventlisteners;

import com.aptina.R;
import com.aptina.miscellaneous.DimensionConverter;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;

/**
 * Panel sliding controller. Listens to the slider touch events.
 * Works with vertical panel only for now.
 */
public class PanelSlideListener implements OnTouchListener, OnClickListener {

    /**
     * Width of the panel buffer zone - the close to the max panel width.
     */
    private static final int PANEL_BUFFER_ZONE = 5;

    /**
     * Difference between min panel width in pixels & dips.
     */
    private final static int MIN_PANEL_WIDTH_GAP = 3;

    /**
     * Panel view.
     */
    private View mPanel = null;

    /**
     * Panel content view.
     */
    private View mPanelContent = null;

    /**
     * Max panel width.
     */
    private final float mMaxPanelWidth;

    /**
     * Min panel width.
     */
    private final float mMinPanelWidth;	

    /**
     * Initial X coordinate of the touch event.
     */
    private float mInitialX;

    /**
     * Current state of panel.
     * 0 - is hidden, 1 - is shown.
     */
    private int mCurrentState = 0;

    /**
     * Creates new instance of the class.
     * 
     * @param panel Panel view.
     * @param context Application context.
     */
    public PanelSlideListener(View panel, Context context)
    {
        if ((panel == null) || (context == null)) {
            throw new IllegalArgumentException();
        }

        this.mPanel = panel;
        this.mPanelContent = panel.findViewById(R.id.panel_content);

        mMaxPanelWidth = DimensionConverter.dipToPixels(context, context.getResources().getDimension(R.dimen.panel_max_width));
        mMinPanelWidth = DimensionConverter.dipToPixels(context, context.getResources().getDimension(R.dimen.panel_min_width)-MIN_PANEL_WIDTH_GAP);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {		
        int action = event.getAction();
        LinearLayout.LayoutParams panelParams = (LinearLayout.LayoutParams) mPanel.getLayoutParams();
        int delta = Math.round(event.getRawX() - mInitialX);
        mInitialX = event.getRawX();

        // Start of touch event.
        if (action == MotionEvent.ACTION_DOWN) {
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (delta > 0) {
                panelParams.width = (int)Math.round(Math.floor(Math.min(panelParams.width + delta, mMaxPanelWidth)));
            } else {
                panelParams.width = (int)Math.round(Math.floor(Math.max(panelParams.width + delta, mMinPanelWidth)));
            }
            mPanel.setLayoutParams(panelParams);
            if (panelParams.width >= (int)mMaxPanelWidth - PANEL_BUFFER_ZONE) {
                mPanelContent.setVisibility(View.VISIBLE);
            } else if (mPanelContent.getVisibility() == View.VISIBLE) {
                mPanelContent.setVisibility(View.GONE);
            }
        }
        // End of touch event.
        else if (action == MotionEvent.ACTION_UP) {
            if (mCurrentState == 0) {
                panelParams.width = (int) mMaxPanelWidth;
                mPanelContent.setVisibility(View.VISIBLE);
                mCurrentState = 1;
            } else {
                panelParams.width = (int) mMinPanelWidth;
                mPanelContent.setVisibility(View.GONE);
                mCurrentState = 0;
            }
            mPanel.setLayoutParams(panelParams);
        }

        return true;
    }

    @Override
    public void onClick(View view) {
        LinearLayout.LayoutParams panelParams = (LinearLayout.LayoutParams) mPanel.getLayoutParams();

        if ((panelParams.width >= (Math.floor(mMaxPanelWidth) - PANEL_BUFFER_ZONE))) {
            panelParams.width = (int) mMinPanelWidth;
            mPanelContent.setVisibility(View.GONE);
            mCurrentState = 0;
        } else if (panelParams.width < (Math.floor(mMaxPanelWidth) - PANEL_BUFFER_ZONE)) {
            panelParams.width = (int) mMaxPanelWidth;
            mPanelContent.setVisibility(View.VISIBLE);
            mCurrentState = 1;
        }

        mPanel.setLayoutParams(panelParams);
    }
}
