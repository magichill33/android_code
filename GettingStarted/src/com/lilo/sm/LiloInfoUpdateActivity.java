package com.lilo.sm;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.leador.mapLayer.GoogleMapLayer;
import com.lilo.model.CommonFacility;
import com.lilo.model.ExtendPart;
import com.lilo.model.HouseLand;
import com.lilo.model.HouseModel;
import com.lilo.model.LandScaping;
import com.lilo.model.MessageEnum;
import com.lilo.model.OtherFacility;
import com.lilo.model.Part;
import com.lilo.model.PartField;
import com.lilo.model.RoadTraffic;
import com.lilo.model.Sanitation;
import com.lilo.service.RunQueryByBuffer;
import com.lilo.service.RunQueryByPoint;
import com.lilo.service.RunQueryDataTask;
import com.lilo.util.DataUtil;
import com.lilo.util.DrawUtil;
import com.lilo.util.TextOverlay;
import com.lilo.widget.TipForm;
import com.supermap.android.data.GetFeaturesResult;
import com.supermap.android.maps.DefaultItemizedOverlay;
import com.supermap.android.maps.LayerView;
import com.supermap.android.maps.LineOverlay;
import com.supermap.android.maps.MapController;
import com.supermap.android.maps.MapView;
import com.supermap.android.maps.Overlay;
import com.supermap.android.maps.OverlayItem;
import com.supermap.android.maps.Point2D;
import com.supermap.android.maps.PointOverlay;
import com.supermap.android.maps.PolygonOverlay;
import com.supermap.services.components.commontypes.Feature;
import com.supermap.services.components.commontypes.Geometry;
import com.supermap.services.components.commontypes.GeometryType;
import com.supermap.services.components.commontypes.Rectangle2D;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.widget.Toast;

public class LiloInfoUpdateActivity extends Activity {

	private MapView mapView;
	private MapController mapController;
	private LayerView layerView;
	private Handler regionHandler; //区域数据回显消息
	private Handler houseHandler;
	private Handler touchHouseHandler;
	private Handler partsHandler;
	private Handler gatherPartHandler;
	
	private Geometry touchGeo = null;//房屋Geo
	private List<Geometry> touchGeoList = new ArrayList<Geometry>();
	/**
	 * 0：表示部件更新
	 * 1：表示房屋更新
	 */
	private String uType;
	private String gridCodes;
	private String pType;
	private List<TextOverlay> textOverlays = new ArrayList<TextOverlay>(); //区域文字
	private List<PolygonOverlay> polygonOverlays = new ArrayList<PolygonOverlay>(); //区域图层
	private List<PolygonOverlay> houseOverlays = new ArrayList<PolygonOverlay>(); //房屋图层集
	private List<Overlay> partsOverlays = new ArrayList<Overlay>(); //部件overlay
	
	private PolygonOverlay buildOverlay = null; //楼栋图层
	private DefaultItemizedOverlay partsOverlay = null;
	private Overlay partOverlay = null;
	private String dataUrl;
	private String partsUrl;
	
	private boolean isAddPoint;
	private int touchDownX;
	private int touchDownY;
	private int touchX;
	private int touchY;
	//private Drawable iconPart;
	private TipForm tipForm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lilo_parts);
		mapView = (MapView) this.findViewById(R.id.mapview);
		mapController = mapView.getController();
		layerView = new LayerView(this);
		dataUrl = getString(R.string.mainUrl) + "/" + getString(R.string.dataUrl);
		partsUrl = getString(R.string.mainUrl) + "/" + getString(R.string.partsUrl);
		String map2d = getString(R.string.mainUrl) + "/" + getString(R.string.map2d);
		layerView.setURL(map2d);
		mapView.setBuiltInZoomControls(true);
		mapView.addLayer(layerView);
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		gridCodes = bundle.getString("gridCodes");
		uType = bundle.getString("uType");
		tipForm = new TipForm();
		regionHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
	            switch (msg.what) {
	            case MessageEnum.QUERY_SUCCESS:
	                GetFeaturesResult queryResult = (GetFeaturesResult) msg.obj;
	                showQueryResult(queryResult);
	              //  progressDialog.dismiss();
	                break;
	            case MessageEnum.QUERY_FAILED:
	               // progressDialog.dismiss();
	                Toast.makeText(LiloInfoUpdateActivity.this,"获取区域信息失败", Toast.LENGTH_LONG).show();
	                break;
	            default:
	               // progressDialog.dismiss();
	                break;
	            }
			}
		};
		
		houseHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MessageEnum.QUERY_SUCCESS:
					GetFeaturesResult houseResult = (GetFeaturesResult) msg.obj;
					drawHouseOnMap(houseResult);
					break;
				case MessageEnum.QUERY_FAILED:
		            Toast.makeText(LiloInfoUpdateActivity.this,"获取房屋信息失败", Toast.LENGTH_LONG).show();
		            break;
				default:
					break;
				}
			}
		};
		
		touchHouseHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
	            case MessageEnum.QUERY_SUCCESS:
	            	tipForm.dismissDialog();
	                GetFeaturesResult queryResult = (GetFeaturesResult) msg.obj;
	                drawSelectedHouse(queryResult);
	                break;
	            case MessageEnum.QUERY_FAILED:
	            	tipForm.dismissDialog();
	                Toast.makeText(LiloInfoUpdateActivity.this,"获取区域信息失败", Toast.LENGTH_LONG).show();
	                break;
	            default:
	            	tipForm.dismissDialog();
	                break;
	            }
			}
		};
		
		partsHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
		            case MessageEnum.QUERY_SUCCESS:
		            	tipForm.dismissDialog();
		                GetFeaturesResult queryResult = (GetFeaturesResult) msg.obj;
		                drawParts(queryResult);
		                break;
		            case MessageEnum.QUERY_FAILED:
		            	tipForm.dismissDialog();
		                Toast.makeText(LiloInfoUpdateActivity.this,"获取区域信息失败", Toast.LENGTH_LONG).show();
		                break;
		            default:
		            	tipForm.dismissDialog();
		                break;
				}
			}
		};
		
		gatherPartHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
		            case MessageEnum.QUERY_SUCCESS:
		            	tipForm.dismissDialog();
		                GetFeaturesResult queryResult = (GetFeaturesResult) msg.obj;
		                postPartInfo(queryResult);
		                break;
		            case MessageEnum.QUERY_FAILED:
		            	tipForm.dismissDialog();
		                Toast.makeText(LiloInfoUpdateActivity.this,"获取区域信息失败", Toast.LENGTH_LONG).show();
		                break;
		            default:
		            	tipForm.dismissDialog();
		                break;
				}
			}
		};
		
		//清空绘制的数据
		clearRegionLay();
		
		String[] codes = gridCodes.split(",");
		StringBuffer paramsBuffer = new StringBuffer();
		for(String code:codes)
		{
			paramsBuffer.append("'");
			paramsBuffer.append(code);
			paramsBuffer.append("',");
		}
		String params = paramsBuffer.substring(0, paramsBuffer.lastIndexOf(","));
		//获取区域数据
		new RunQueryDataTask(dataUrl, 
						getString(R.string.region_grid), "WGBM in (" + params + ")", regionHandler).execute("*");
		if(uType.equals("1"))
		{
			//获取房屋数据
			new RunQueryDataTask(dataUrl,getString(R.string.sm_house), 
					"WGBM in (" + params + ")", houseHandler).execute("*");
		}else if(uType.equals("0"))
		{
			pType = bundle.getString("pType");
			int imgId = LiloInfoUpdateActivity.this
					.getResources().getIdentifier("p"+pType, "drawable", "com.lilo.sm");
	    	Drawable iconPart = LiloInfoUpdateActivity.this.getResources().getDrawable(imgId);
	    	iconPart.setAlpha(200);
			/*String picUrl = getString(R.string.mainUrl) + "/" + getString(R.string.part_legend1) + pType + "@"
					+ getString(R.string.part_legend2);
			Bitmap partmap = returnBitMap(picUrl);
			BitmapDrawable iconPart = new BitmapDrawable(partmap);*/
	    	partsOverlay = new DefaultItemizedOverlay(iconPart);
	    	new RunQueryDataTask(partsUrl, 
	    			getString(R.string.wudu_cmp) + "_" + pType, "BGCODE in (" + params + ")", partsHandler).execute("*");
		}
	}
	
	public Bitmap returnBitMap(String url){ 
        URL myFileUrl = null;   
        Bitmap bitmap = null;  
        try {   
            myFileUrl = new URL(url);   
        } catch (MalformedURLException e) {   
            e.printStackTrace();   
        }   
        try {   
            HttpURLConnection conn = (HttpURLConnection) myFileUrl   
              .openConnection();   
            conn.setDoInput(true);   
            conn.connect();   
            InputStream is = conn.getInputStream();   
            bitmap = BitmapFactory.decodeStream(is);   
            is.close();   
        } catch (IOException e) {   
              e.printStackTrace();   
        }   
              return bitmap;   
    }   
	
	/**
	 * 根据查询返回的结果集绘制区域
	 * @param result
	 */
	protected void showQueryResult(GetFeaturesResult result) {
		if (result == null || result.features == null || result.featureCount == 0) {
            Toast.makeText(this, "查询区域结果为空!", Toast.LENGTH_LONG).show();
            return;
        }
		List<List<Point2D>> pointsLists = new ArrayList<List<Point2D>>();
    	Feature[] queryfeatures = result.features;
   
    	touchGeo = queryfeatures[0].geometry;
      	Rectangle2D bounds = DataUtil.getGraphicsBound(result);
      	DataUtil.zoomMap(bounds, mapView, mapController);
      	
    	for(Feature f:queryfeatures)
 	    {
    		String labelName = null;
    		for(int i = 0;i<f.fieldNames.length;i++)
    		{
    			if(f.fieldNames[i].equals("WGMC"))
    			{
    				labelName = f.fieldValues[i];
    				break;
    			}
    		}
    		Geometry geometry = f.geometry;
    		touchGeoList.add(geometry);
 	    	List<Point2D> points = DataUtil.getPiontsFromGeometry(geometry);
 	    	if (geometry.parts.length > 1) {
                int num = 0;
                for (int j = 0; j < geometry.parts.length; j++) {
                    int count = geometry.parts[j];
                    List<Point2D> partList = points.subList(num, num + count);
                    pointsLists.add(partList);
                    num = num + count;
                }
            } else {
                pointsLists.add(points);
            }
    		//pointsLists.add(points);
    		
    		/**
    		 * 标注文字
    		 */
    		Point2D center = new Point2D(geometry.getCenter().x,geometry.getCenter().y);
    		//OverlayItem overlayItem = new OverlayItem(center, labelName,labelName);
    		//dOverLay.addItem(overlayItem);
    		TextOverlay textOverlay = new TextOverlay(center, labelName);
    		mapView.getOverlays().add(textOverlay);
    		textOverlays.add(textOverlay);
 	    }
    	
    	// 把所有查询的几何对象都高亮显示
        for (int m = 0; m < pointsLists.size(); m++) {
            List<Point2D> geoPointList = pointsLists.get(m);
            TouchGridOverlays polygonOverlay = new TouchGridOverlays(new DrawUtil().getPolygonPaint(Color.BLUE,180,3));
            mapView.getOverlays().add(polygonOverlay);
            polygonOverlays.add(polygonOverlay);
            polygonOverlay.setData(geoPointList);
            polygonOverlay.setShowPoints(false);
           
        }
    
        this.mapView.invalidate(); //刷新地图
	}
	
	/**
	 * 绘制查询房屋数据
	 * @param houseResult
	 */
	protected void drawHouseOnMap(GetFeaturesResult houseResult)
	{
		if (houseResult == null || houseResult.features == null) {
            Toast.makeText(this, "查询结果为空!", Toast.LENGTH_LONG).show();
            return;
        }
		
		List<List<Point2D>> pointsLists = new ArrayList<List<Point2D>>();
    	Feature[] queryfeatures = houseResult.features;
    	
   	 	for(Feature f:queryfeatures)
	    {
	   		Geometry geometry = f.geometry;
	    	List<Point2D> points = DataUtil.getPiontsFromGeometry(geometry);
	    	if (geometry.parts.length > 1) 
	    	{
               int num = 0;
               for (int j = 0; j < geometry.parts.length; j++)
               {
                   int count = geometry.parts[j];
                   List<Point2D> partList = points.subList(num, num + count);
                   pointsLists.add(partList);
                   num = num + count;
               }
	    	} 
	    	else 
	    	{
               pointsLists.add(points);
	    	}
	    }
   	 	
    	// 把所有查询的几何对象都高亮显示
        for (int m = 0; m < pointsLists.size(); m++) {
            List<Point2D> geoPointList = pointsLists.get(m);
            PolygonOverlay polygonOverlay = new PolygonOverlay(new DrawUtil().getPolygonPaint(0x9696fe,50,1,Paint.Style.FILL));
            //PolygonOverlay polygonOverlay = new TouchPolygonOverlays(getPolygonPaint(0x9696fe,50,1,Paint.Style.FILL));
            mapView.getOverlays().add(polygonOverlay);
            houseOverlays.add(polygonOverlay);
            polygonOverlay.setData(geoPointList);
            polygonOverlay.setShowPoints(false);
           
        }

        
        this.mapView.invalidate(); //刷新地图
	}
	
	/**
	 * 绘制选中房屋
	 * @param houseResult
	 * @return
	 */
	protected HouseModel drawSelectedHouse(GetFeaturesResult houseResult)
	{
		if (houseResult == null || houseResult.features == null || houseResult.featureCount == 0) {
            Toast.makeText(this, "查询结果为空!", Toast.LENGTH_LONG).show();
            return null;
        }
		
		Feature feature = houseResult.features[0];
   		Geometry geometry = feature.geometry;
    	List<Point2D> points = DataUtil.getPiontsFromGeometry(geometry);
    	// 把所有查询的几何对象都高亮显示
    	
    	if(buildOverlay!=null && mapView.getOverlays().contains(buildOverlay))
    	{
    		mapView.getOverlays().remove(buildOverlay);
    		buildOverlay.destroy();
    	}
    	
    	PolygonOverlay overlay = new PolygonOverlay(new DrawUtil().getPolygonPaint(0xff0000,150,1,Paint.Style.FILL));
        mapView.getOverlays().add(overlay);
        overlay.setData(points);
        overlay.setShowPoints(false);
        mapView.invalidate();
        buildOverlay = overlay;
        
        String houseCode = "";
        String gridCode = "";
        int k = 0;
        for(int i = 0;i<feature.fieldNames.length&&k<=2;i++)
		{
			if(feature.fieldNames[i].equals(getString(R.string.houseCode)))
			{
				houseCode = feature.fieldValues[i];
				k++;
			}
			
			if(feature.fieldNames[i].equals("WGBM"))
			{
				gridCode = feature.fieldValues[i];
				k++;
			}
		}
		
		TipForm.showToast("房屋编码：" + houseCode + "，网格编码：" + gridCode,getApplicationContext()); 
        
		return null;
	}
	
	/**
	 * 绘制部件信息
	 * @param result
	 */
	protected void drawParts(GetFeaturesResult result)
	{
		if (result == null || result.features == null || result.featureCount == 0) {
            Toast.makeText(this, "查询部件结果为空!", Toast.LENGTH_LONG).show();
            return;
        }
		
        if(mapView.getOverlays().contains(partsOverlay))
        {
        	mapView.getOverlays().remove(partsOverlay);
        	if(partsOverlay.size() > 0)
        	{
        		partsOverlay.clear();
        	}
        }
        if(mapView.getOverlays().contains(partsOverlays))
        {
        	mapView.getOverlays().remove(partsOverlays);
        	if(partsOverlays.size() > 0)
        	{
        		partsOverlays.clear();
        	}
        }
        	
        List<List<Point2D>> pointsLists = new ArrayList<List<Point2D>>();
		Feature[] features = result.features;
        GeometryType type = features[0].geometry.type;
        
        for (int i = 0; i < features.length; i++) {
            Feature feature = features[i];
            Geometry geometry = features[i].geometry;
            List<Point2D> points = DataUtil.getPiontsFromGeometry(geometry);
            if(type == GeometryType.POINT)
            {
            	 //List<Point2D> geoPoints = DataUtil.getPiontsFromGeometry(geometry);
                 if (points != null && points.size() > 0) {
                     OverlayItem overlayItem = new OverlayItem(points.get(0), null, null);
                     partsOverlay.addItem(overlayItem);
                 }
            }else
            {
     	    	
     	    	if (geometry.parts.length > 1) {
                    int num = 0;
                    for (int j = 0; j < geometry.parts.length; j++) {
                        int count = geometry.parts[j];
                        List<Point2D> partList = points.subList(num, num + count);
                        pointsLists.add(partList);
                        num = num + count;
                    }
                } else {
                    pointsLists.add(points);
                }
         	    	
            }
           
        }
        
    	if(type == GeometryType.LINE)
    	{
        	// 把所有查询的几何对象都高亮显示
            for (int m = 0; m < pointsLists.size(); m++) {
                List<Point2D> geoPointList = pointsLists.get(m);
                //PolygonOverlay polygonOverlay = new PolygonOverlay(getPolygonPaint(Color.BLUE,180,3));
                LineOverlay part = new LineOverlay(new DrawUtil().getPolygonPaint(0x045ff5, 150,3));
                mapView.getOverlays().add(part);
                partsOverlays.add(part);
                part.setData(geoPointList);
                part.setShowPoints(false);
               
            }
    	}else 
    	{
            for (int m = 0; m < pointsLists.size(); m++) {
                List<Point2D> geoPointList = pointsLists.get(m);
                PolygonOverlay polygonOverlay = new PolygonOverlay(new DrawUtil().getPolygonPaint(0x0000ff,150,1,Paint.Style.FILL));
                //PolygonOverlay polygonOverlay = new TouchPolygonOverlays(getPolygonPaint(0x9696fe,50,1,Paint.Style.FILL));
                mapView.getOverlays().add(polygonOverlay);
                partsOverlays.add(polygonOverlay);
                polygonOverlay.setData(geoPointList);
                polygonOverlay.setShowPoints(false);
               
            }
    	}
    	
        if(partsOverlay.size()>0)
        {
        	mapView.getOverlays().add(partsOverlay);
        }
        
        mapView.invalidate();
	}
	
	/**
	 * 将采集的部件信息提交出去
	 * @param result
	 */
	protected void postPartInfo(GetFeaturesResult result)
	{
		if (result == null || result.features == null || result.featureCount == 0) {
            Toast.makeText(this, "查询部件信息为空!", Toast.LENGTH_LONG).show();
            return;
        }
		
		Feature feature = result.features[0];
		Geometry geometry = feature.geometry;
		List<Point2D> points = DataUtil.getPiontsFromGeometry(geometry);
		if(partOverlay!=null && mapView.getOverlays().contains(partOverlay))
		{
			mapView.getOverlays().remove(partOverlay);
			if(partOverlay instanceof DefaultItemizedOverlay)
			{
				((DefaultItemizedOverlay) partOverlay).clear();
			}
		}
		//com.supermap.services.components.commontypes.Point2D mp = feature.geometry.points[0];
		if(geometry.type == GeometryType.POINT)
		{
			/*BitmapDrawable bd = (BitmapDrawable) iconPart;
			Bitmap bitmap = bd.getBitmap();*/
			
			//Drawable da = new BitmapDrawable(bitmap);
			
			int imgId = LiloInfoUpdateActivity.this
					.getResources().getIdentifier("p"+pType, "drawable", "com.lilo.sm");
			Bitmap bitmap = BitmapFactory.decodeResource(LiloInfoUpdateActivity.this.getResources(),imgId);
	    	Drawable da1 = LiloInfoUpdateActivity.this.getResources().getDrawable(imgId);
	    	//bitmap.setDensity(da.)
	    	int width = da1.getIntrinsicWidth();
	    	int height = da1.getIntrinsicHeight();
	    	Matrix matrix = new Matrix();
	    	matrix.postScale(2.0f, 2.0f);
	    	Bitmap newmp = Bitmap.createBitmap(bitmap,0,0,
	    			width,height,matrix,true);
	    	//newmp.setPixel(0, 0, 0xff0000);
	    	BitmapDrawable da = new BitmapDrawable(newmp);
			
			//da.setTargetDensity(metrics)
	    	//ColorFilter filter = new LightingColorFilter(0x111111, 0xffff00);
	    	da.setAlpha(244);
	    	//da.setFilterBitmap(true);
			//da.setColorFilter(filter);
			DefaultItemizedOverlay itemOverlay = new DefaultItemizedOverlay(da);
			Point2D mp = points.get(0);
			OverlayItem item = new OverlayItem(mp,"","");
			itemOverlay.addItem(item);
			mapView.getOverlays().add(itemOverlay);
			partOverlay = itemOverlay;
			//mapController.setCenter(mp);
		}else if(geometry.type == GeometryType.LINE)
		{
			 LineOverlay part = new LineOverlay(new DrawUtil().getPolygonPaint(0xff0000, 255,3));
             mapView.getOverlays().add(part);
             partsOverlays.add(part);
             part.setData(points);
             part.setShowPoints(false);
             partOverlay = part;
		}else
		{
			  PolygonOverlay polygonOverlay = new PolygonOverlay(new DrawUtil().getPolygonPaint(0xff0000,255,1,Paint.Style.FILL));
              //PolygonOverlay polygonOverlay = new TouchPolygonOverlays(getPolygonPaint(0x9696fe,50,1,Paint.Style.FILL));
              mapView.getOverlays().add(polygonOverlay);
              partsOverlays.add(polygonOverlay);
              polygonOverlay.setData(points);
              polygonOverlay.setShowPoints(false);
              partOverlay = polygonOverlay;
		}
		
		mapView.invalidate();
		String parentType = pType.substring(0, 2);
		Part part = null;
		switch (Integer.parseInt(parentType)) {
			case 1:
				part = new CommonFacility();
				break;
			case 2:
				part = new RoadTraffic();
				break;
			case 3:
				part = new Sanitation();
				break;
			case 4:
				part = new LandScaping();
				break;
			case 5:
				part = new HouseLand();
				break;
			case 6:
				part = new OtherFacility();
				break;
			default:
				part = new ExtendPart();
				break;
		}
		
		List<String> fields = part.getFieldNames();
		String[] keys = feature.fieldNames;
		String[] values = feature.fieldValues;
		for(int i = 0;i < keys.length;i++)
		{
			try {
				part.setValue(keys[i], values[i]);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		TipForm.showToast(part.toString(), getApplicationContext());
		Log.i("ly", part.toString());
	}
	
	/**
	 * 行政区域overlays
	 * @author Administrator
	 *
	 */
	class TouchGridOverlays extends PolygonOverlay
	{
		public TouchGridOverlays(Paint polygonPaint) {
			super(polygonPaint);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event, MapView map) {

            switch (event.getAction()) {
	            case MotionEvent.ACTION_DOWN: // 捕获手指触摸按下动作
	                isAddPoint = true;
	                touchDownX = Math.round(event.getX());
	                touchDownY = Math.round(event.getY());
	                break;
	            case MotionEvent.ACTION_MOVE: // 捕获手指触摸移动动作
	                int x = Math.round(event.getX());
	                int y = Math.round(event.getY());
	                if (Math.abs(x - touchDownX) > 4 || Math.abs(y - touchDownY) > 4) {
	                    isAddPoint = false;// 平移不加入该点
	                }
	                break;
	            case MotionEvent.ACTION_UP: // 捕获手指触摸离开动作
	                if (isAddPoint) {
	                    touchX = Math.round(event.getX());
	                    touchY = Math.round(event.getY());
	                    // 记录点击位置
		                  Point2D touchPoint = mapView.getProjection().fromPixels(touchX, touchY);
		  
		                  Boolean isIn = false;
		                  for(Geometry geo:touchGeoList)
		                  {
		                	  isIn = Geometry.isPointInPolygon(new com.supermap.services.components.
		  	                  		commontypes.Point2D(touchPoint.x, touchPoint.y), geo);
		                	  if(isIn)
		                		  break;
		                  }
		                  if(isIn)
		                  {
		                  	//showProgressDialog();
		                  	if(uType.equals("0"))
		                  	{
		                  		tipForm.showProgressDialog(LiloInfoUpdateActivity.this);
		                  		new RunQueryByBuffer(partsUrl, getString(R.string.wudu_cmp) + "_" + pType,
		                  				touchPoint, gatherPartHandler).execute("*");
		                  	}else if(uType.equals("1"))
		                  	{
		                  		tipForm.showProgressDialog(LiloInfoUpdateActivity.this);
		                  		new RunQueryByPoint(dataUrl, getString(R.string.sm_house),
		                  				touchPoint, touchHouseHandler).execute("*");
		                  	}
		                  	
		                  }else
		                  {
		                  	TipForm.showToast("请在划定的区域内点击",getApplicationContext());
		                  }
		                  
		                 /**
		                  * 测试代码
		                  */
		              	if(!isIn&&uType.equals("0"))
	                  	{
		              		tipForm.showProgressDialog(LiloInfoUpdateActivity.this);
	                  		new RunQueryByBuffer(partsUrl, getString(R.string.wudu_cmp) + "_" + pType,
	                  				touchPoint, gatherPartHandler).execute("*");
	                  	}
	                    
	                }
	                break;
            }
            return false;
		}
	}
	

    
    /**
     * 清空绘制区域
     */
    protected void clearRegionLay()
    {
    	 //lineOverlay.setData(new ArrayList<Point2D>());
         //polygonOverlay.setData(new ArrayList<Point2D>());
        // geoPoints.clear();
         if (polygonOverlays.size() != 0) {
             mapView.getOverlays().remove(polygonOverlays);
             polygonOverlays.clear();
         }
         if(textOverlays.size()!=0)
         {
        	 mapView.getOverlays().remove(textOverlays);
        	 textOverlays.clear();
         }
         if(houseOverlays.size()!=0)
         {
        	 mapView.getOverlays().remove(houseOverlays);
        	 houseOverlays.clear();
         }
         mapView.invalidate();
    }
    


	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.lilo_parts, menu);
		return true;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.destroy();
	}

	
}
