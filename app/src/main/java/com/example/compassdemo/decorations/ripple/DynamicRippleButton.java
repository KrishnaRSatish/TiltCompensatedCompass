package com.example.compassdemo.decorations.ripple;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.core.content.res.ResourcesCompat;

import com.example.compassdemo.R;
import com.example.compassdemo.utils.ColorUtils;


public class DynamicRippleButton extends androidx.appcompat.widget.AppCompatButton {
    public DynamicRippleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.TRANSPARENT);
        setAllCaps(false);
        setTypeface(ResourcesCompat.getFont(context, R.font.bold));
        setTextColor(context.getColor(R.color.colorAccent));

        if (!isInEditMode()) {
            setBackground(Utils.getRippleDrawable(getContext(), getBackground(), 2F));
        }
    }
}
