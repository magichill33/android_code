package com.lilo.util;

import android.graphics.Paint;
import android.graphics.Paint.Style;

public class DrawUtil {
    // ªÊ√Ê∑Á∏Ò
    public Paint getPolygonPaint(int color,int alpha,float width) {
      return getPolygonPaint(color, alpha, width, Paint.Style.STROKE);
    }
    
    public Paint getPolygonPaint(int color,int alpha,float width,Style style) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setAlpha(alpha);
        
        paint.setStyle(style);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(width);
        return paint;
    }
}
