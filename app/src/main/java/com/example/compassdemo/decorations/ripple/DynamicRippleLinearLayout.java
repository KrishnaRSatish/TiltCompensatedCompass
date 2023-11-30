package com.example.compassdemo.decorations.ripple;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class DynamicRippleLinearLayout extends LinearLayout {

    public DynamicRippleLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            setBackgroundColor(Color.TRANSPARENT);
            setBackground(Utils.getRippleDrawable(getContext(), getBackground()));
        }
    }

    public void setLayoutEnabled(boolean enabled) {
        setEnabled(enabled);
        if (enabled) {
            setAlpha(1.0f);
        } else {
            setAlpha(0.5f);
        }
    }
}
