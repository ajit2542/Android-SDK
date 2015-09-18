package com.oym.indoor.navigation.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextViewUltra extends TextView {

//	public TextViewMedium(Context context, AttributeSet attrs,
//			int defStyleAttr, int defStyleRes) {
//		super(context, attrs, defStyleAttr, defStyleRes);
//		init(context);
//	}
	
	public TextViewUltra(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}
	
	public TextViewUltra(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public TextViewUltra(Context context) {
		super(context);
		init(context);
	}

	
	private void init(Context ctx) {
		try {
//			Typeface tf = Typeface.createFromAsset(ctx.getAssets(), "fonts/NeoSansPro-Ultra.ttf");
//			Typeface tf = Typeface.createFromAsset(ctx.getAssets(), "fonts/Roboto-Regular.ttf");
			Typeface tf = Typeface.createFromAsset(ctx.getAssets(), "fonts/HelveticaNeue.ttf");
			setTypeface(tf);
		} catch (Exception ex) {}
	}
}
