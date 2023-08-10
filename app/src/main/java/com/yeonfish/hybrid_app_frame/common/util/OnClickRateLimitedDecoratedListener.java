package com.yeonfish.hybrid_app_frame.common.util;

import android.view.View;

public abstract class OnClickRateLimitedDecoratedListener implements View.OnClickListener {
    private static final int CLICK_DELAY_DEFAULT = 300;

    private int clickDelay;

    private long clickTime = -1;

    public OnClickRateLimitedDecoratedListener() {
        this(CLICK_DELAY_DEFAULT);
    }

    public OnClickRateLimitedDecoratedListener(int clickDelay) {
        this.clickDelay = clickDelay;
    }

    @Override
    public void onClick(View v) {
        if (clickTime == -1) {
            onSingleClick(v);
            clickTime = System.currentTimeMillis();
        } else {
            if (System.currentTimeMillis() - clickTime >= clickDelay) {
                onSingleClick(v);
                clickTime = System.currentTimeMillis();
            }
        }
    }

    public abstract void onSingleClick(View v);
}
