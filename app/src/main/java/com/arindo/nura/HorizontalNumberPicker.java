package com.arindo.nura;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

/**
 * Created by bmaxard on 21/02/2017.
 */

public class HorizontalNumberPicker extends LinearLayout {
    public HorizontalNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater layoutInflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.horizontal_number_picker, this);
    }
}
