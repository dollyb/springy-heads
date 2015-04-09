package com.flipkart.chatheads.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;
import com.flipkart.chatheads.R;
import com.flipkart.chatheads.reboundextensions.ChatHeadUtils;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ChatHeadCloseButton extends ImageView {

    private static final long DELAY = 500;
    private static final float PERC_PARENT_WIDTH = 0.1f; //perc of parent to be covered during drag
    private static final float PERC_PARENT_HEIGHT = 0.05f; //perc of parent to be covered during drag
    private int mParentWidth;
    private int mParentHeight;
    private Spring scaleSpring;
    private Spring xSpring;
    private Spring ySpring;
    private boolean disappeared;
    private boolean captured = false;
    public ChatHeadCloseButton(Context context) {
        super(context);
        init();
    }

    public ChatHeadCloseButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChatHeadCloseButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public boolean isDisappeared() {
        return disappeared;
    }

    private void init() {
        setImageResource(R.drawable.chat_head_close_ic);

        int myDiameter = (int) (ChatHead.DIAMETER * 1.2);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ChatHeadUtils.dpToPx(getContext(), myDiameter), ChatHeadUtils.dpToPx(getContext(), myDiameter));
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        layoutParams.bottomMargin = ChatHeadUtils.dpToPx(getContext(), 50);
        setLayoutParams(layoutParams);

        SpringSystem springSystem = SpringSystem.create();
        xSpring = springSystem.createSpring();
        xSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                super.onSpringUpdate(spring);
                setTranslationX((float) spring.getCurrentValue());
            }
        });
        ySpring = springSystem.createSpring();
        ySpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                super.onSpringUpdate(spring);
                setTranslationY((float) spring.getCurrentValue());
            }
        });
        scaleSpring = springSystem.createSpring();
        scaleSpring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                double currentValue = spring.getCurrentValue();
                setScaleX((float) currentValue);
                setScaleY((float) currentValue);
            }
        });

    }

    public void appear() {
        if(isEnabled()) {
            ySpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
            xSpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
            bringToFront();
            disappeared = false;
        }
    }

    public void onCapture() {
        captured = true;
        scaleSpring.setEndValue(1);
    }

    public void onRelease() {
        captured = false;
        scaleSpring.setEndValue(0.8);
    }

    public void disappear(boolean immediate, boolean animate) {
        ySpring.setEndValue(mParentHeight);
        ySpring.setSpringConfig(SpringConfigsHolder.NOT_DRAGGING);
        xSpring.setEndValue(0);
        if (!animate) {
            ySpring.setCurrentValue(mParentHeight-getBottom(), true);
            xSpring.setCurrentValue(0, true);
        }
        disappeared = true;

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mParentWidth = ((View) getParent()).getMeasuredWidth();
        mParentHeight = ((View) getParent()).getMeasuredHeight();
        disappear(true, false);
    }

    public void pointTo(float x, float y) {
        if(isEnabled()) {
            double translationX = getTranslationFromSpring(x, PERC_PARENT_WIDTH, mParentWidth);
            double translationY = getTranslationFromSpring(y, PERC_PARENT_HEIGHT, mParentHeight);
            if (!disappeared) {
                xSpring.setEndValue(translationX);
                ySpring.setEndValue(translationY);

            }
        }
    }

    private double getTranslationFromSpring(double springValue, float percent, int fullValue) {
        float widthToCover = percent * fullValue;
        return SpringUtil.mapValueFromRangeToRange(springValue, 0, fullValue, -widthToCover / 2, widthToCover / 2);
    }

    public boolean isAtRest() {
        return xSpring.isAtRest() && ySpring.isAtRest();
    }

    public int getEndValueX() {
        return (int) xSpring.getEndValue();
    }

    public int getEndValueY() {
        return (int) ySpring.getEndValue();
    }
}
