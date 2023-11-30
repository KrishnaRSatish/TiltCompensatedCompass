package com.example.compassdemo.decorations;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;

import com.example.compassdemo.R;

import java.util.Arrays;

/**
 * Custom dynamic ripple image button
 *
 * @Warning do not use any background
 * with this view. This view will be
 * transparent when created
 */


public class DynamicRippleImageButton extends androidx.appcompat.widget.AppCompatImageButton {
    public static final int alpha = 100;
    public DynamicRippleImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.TRANSPARENT);
        if (!isInEditMode()) {
            setBackground(getRippleDrawable(getContext(), getBackground(), 2F));
        }
    }


    public static RippleDrawable getRippleDrawable(Context context, Drawable backgroundDrawable, float divisiveFactor) {
        float[] outerRadii = new float[8];
        float[] innerRadii = new float[8];
        Arrays.fill(outerRadii, 30 / divisiveFactor);
        Arrays.fill(innerRadii, 30 / divisiveFactor);

        RoundRectShape shape = new RoundRectShape(outerRadii, null, innerRadii);
        ShapeDrawable mask = new ShapeDrawable(shape);

        ColorStateList stateList = ColorStateList.valueOf(context.getColor(R.color.colorAccent));

        RippleDrawable rippleDrawable = new RippleDrawable(stateList, backgroundDrawable, mask);
        rippleDrawable.setAlpha(alpha);

        return rippleDrawable;
    }
}
