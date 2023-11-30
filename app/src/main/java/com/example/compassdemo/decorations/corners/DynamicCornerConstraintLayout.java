package com.example.compassdemo.decorations.corners;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.compassdemo.decorations.LayoutBackground;


public class DynamicCornerConstraintLayout extends ConstraintLayout {
    
    public DynamicCornerConstraintLayout(@NonNull Context context) {
        super(context);
        LayoutBackground.setBackground(context, this, null);
    }
    
    public DynamicCornerConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutBackground.setBackground(context, this, attrs);
    }
    
    public DynamicCornerConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutBackground.setBackground(context, this, attrs);
    }
    
    public DynamicCornerConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        LayoutBackground.setBackground(context, this, attrs);
    }
}
