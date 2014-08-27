package com.lilo.event;

import com.lilo.sm.LiloMapActivity;
import com.lilo.sm.LiloStreetActivity;
import com.lilo.sm.LiloVistaActivity;
import com.lilo.sm.R;
import com.supermap.android.maps.Point2D;

import android.content.Context;
import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.sax.StartElementListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;

public class ButtonTouchListener implements OnTouchListener {

	private ImageButton button;
	private Context context;
	
	public final  float[] BT_SELECTED=new float[]  
            { 2, 0, 0, 0, 2,  
        0, 2, 0, 0, 2,  
        0, 0, 2, 0, 2,  
        0, 0, 0, 1, 0 };                  

	public final float[] BT_NOT_SELECTED=new float[]  
            { 1, 0, 0, 0, 0,  
        0, 1, 0, 0, 0,  
        0, 0, 1, 0, 0,  
        0, 0, 0, 1, 0 }; 
	
	public ButtonTouchListener(ImageButton btnMap2d,Context context) {
		super();
		this.button = btnMap2d;
		this.context = context;
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_DOWN)
		{
			view.getBackground().setColorFilter(new ColorMatrixColorFilter(BT_SELECTED));
			view.setBackgroundDrawable(view.getBackground());
			button.setAlpha(120);
			//button.setBackgroundColor(0xffffff);
		}else if(event.getAction() == MotionEvent.ACTION_UP)
		{
			view.getBackground().setColorFilter(new ColorMatrixColorFilter(BT_NOT_SELECTED));
			view.setBackgroundDrawable(view.getBackground());
			button.setAlpha(255);
			//button.setBackgroundColor(0x000000);
			LiloMapActivity activity = (LiloMapActivity) context;
			com.supermap.services.components.commontypes.Point2D point = activity.getTouchGeo().getCenter();
			Point2D cp = new Point2D(point.x, point.y);
			String reportType = activity.getReportType();
			String gridCodes = activity.getGridCodes();
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putSerializable("cp", cp);
			bundle.putString("reportType", reportType);
			bundle.putString("gridCodes", gridCodes);
			intent.putExtras(bundle);
			if(button.getId() == R.id.mapSplit)
			{
				intent.setClass(context, LiloStreetActivity.class);
			}else
			{
				intent.setClass(context, LiloVistaActivity.class);
			}
			context.startActivity(intent);
		}
		return false;
	}


}
