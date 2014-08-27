package com.lilo.sm;

import java.util.ArrayList;
import java.util.List;

import com.leador.mapLayer.GoogleMapLayer;
import com.lilo.event.ButtonTouchListener;
import com.lilo.model.CaseModel;
import com.lilo.model.HouseModel;
import com.lilo.model.MessageEnum;
import com.lilo.service.RunQueryByBuffer;
import com.lilo.service.RunQueryByPoint;
import com.lilo.service.RunQueryDataTask;
import com.lilo.util.DataUtil;
import com.lilo.util.TextOverlay;
import com.lilo.widget.TipForm;

import com.supermap.android.data.GetFeaturesResult;
import com.supermap.android.maps.DefaultItemizedOverlay;
import com.supermap.android.maps.LayerView;
import com.supermap.android.maps.MapController;
import com.supermap.android.maps.MapView;
import com.supermap.android.maps.Overlay;
import com.supermap.android.maps.OverlayItem;
import com.supermap.android.maps.Point2D;
import com.supermap.android.maps.PolygonOverlay;
import com.supermap.android.theme.LabelThemeCell;
import com.supermap.services.components.commontypes.Feature;
import com.supermap.services.components.commontypes.Geometry;
import com.supermap.services.components.commontypes.GeometryType;
import com.supermap.services.components.commontypes.ImageOutputOption;
import com.supermap.services.components.commontypes.MapParameter;
import com.supermap.services.components.commontypes.QueryParameter;
import com.supermap.services.components.commontypes.Rectangle2D;
import com.supermap.services.components.commontypes.TextStyle;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.Toast;

public class LiloMapActivity extends Activity
{

	private MapView mapView;
	private LayerView baseLayerView;
	private String map2d;
	//private String map2d = "http://192.168.4.252:8090/iserver/services/map-china400/rest/maps/China";
	private String dataUrl;
	private String region_grid ="ORCL_supermap:wudu_sm_grid";
	//private String region_comm ="ORCL_supermap:wudu_com";
	private String sm_house = "ORCL_supermap:wudu_sm_house";
	private List<PolygonOverlay> polygonOverlays = new ArrayList<PolygonOverlay>(); //区域图层
	private List<TextOverlay> textOverlays = new ArrayList<TextOverlay>(); //区域文字
	private List<PolygonOverlay> houseOverlays = new ArrayList<PolygonOverlay>();
	
	/**
	 * 0:房屋信息采集
	 * 1：表示事件上报
	 * 2:事件回显
	 */
	private String reportType = "1"; //通过intent获得
	
	private Handler regionHandler; //区域数据回显消息
	private Handler houseHandler; //房屋数据回显消息
	private Handler touchHouseHandler; //点击房屋回显消息
	private Handler touchGridHandler; //点击区域回显消息
	private LayerView layerView;
	private MapController mapController;
	private TouchHouseOverlays touchHouseOverlays;
	
	//private ImageButton btnMap2d; //二维图显示按钮
	private ImageButton btnMapVista; //实景图显示按钮
	private ImageButton btnMapSplit; //分屏显示按钮
	
	//private PolygonOverlay regionLayer;
	// 数据集查询结果 
	//public GetFeaturesResult result; 
	private Dialog progressDialog;  //弹出进度框
	
	private DefaultItemizedOverlay labelOverlay; //标注图层
	private PolygonOverlay buildOverlay = null; //楼栋图层
	
	private Geometry touchGeo = null;//房屋Geo
	private List<Geometry> touchGeoList = new ArrayList<Geometry>();
	private String gridCodes = null; //网格编码，通过intent获得
	private Point2D touchPoint = null; //事件采集的坐标
	private List<Point2D> casePoints = new ArrayList<Point2D>();
	/**
	 * 经纬度通过intent获得，在回显时使用
	 */
	//private double lon;
	//private double lat;
	
	private boolean isAddPoint;
	private int touchDownX;
	private int touchDownY;
	private int touchX;
	private int touchY;
	private TipForm tipForm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Bundle bundle = getIntent().getExtras();
		reportType = bundle.getString("reportType");
		if(reportType.equals("1"))
		{
			gridCodes = bundle.getString("gridCodes");
		}else
		{
			List<CaseModel> caselist = bundle.getParcelableArrayList("caselist");
			List<String> codes = new ArrayList<String>();
			for(CaseModel model:caselist)
			{
				Point2D p = new Point2D(model.getLon(),model.getLat());
				casePoints.add(p);
				if(!codes.contains(model.getGridCode()))
				{
					codes.add(model.getGridCode());
				}
			}
			
			StringBuffer codeBuffer = new StringBuffer();
			for(String code:codes)
			{
				codeBuffer.append(code + ",");
			}
			gridCodes = codeBuffer.substring(0,codeBuffer.length()-1);
		}
		
		map2d = getString(R.string.mainUrl) + "/" + getString(R.string.map2d);
		dataUrl = getString(R.string.mainUrl) + "/" + getString(R.string.dataUrl);
		initUI();
        tipForm = new TipForm();
        labelOverlay = new DefaultItemizedOverlay(getResources().getDrawable(R.drawable.light_red));
       // buildOverlay = new PolygonOverlay(getPolygonPaint(0xff0000,150,1,Paint.Style.FILL));
        mapController = mapView.getController();
		layerView = new LayerView(this);
		layerView.setURL(map2d);
		mapView.setBuiltInZoomControls(true);
		mapView.addLayer(layerView);
		//gridCodes = "62120210000601,62120210000602,62120210000603";
		//lon = 104.928003;
		//lat = 33.387849;
		/**
		 * 点击房屋图层
		 */
		touchHouseHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
	            case MessageEnum.QUERY_SUCCESS:
	            	dismissDialog();
	                GetFeaturesResult queryResult = (GetFeaturesResult) msg.obj;
	                drawSelectedHouse(queryResult);
	                break;
	            case MessageEnum.QUERY_FAILED:
	            	dismissDialog();
	                Toast.makeText(LiloMapActivity.this,"获取区域信息失败", Toast.LENGTH_LONG).show();
	                break;
	            default:
	            	dismissDialog();
	                break;
	            }
			}
		};
		
		/**
		 * 绘制区域handler
		 */
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
	                Toast.makeText(LiloMapActivity.this,"获取区域信息失败", Toast.LENGTH_LONG).show();
	                break;
	            default:
	               // progressDialog.dismiss();
	                break;
	            }
			}
		};
		
		/**
		 * 绘制房屋handler
		 */
		houseHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MessageEnum.QUERY_SUCCESS:
					GetFeaturesResult houseResult = (GetFeaturesResult) msg.obj;
					drawHouseOnMap(houseResult);
					break;
				case MessageEnum.QUERY_FAILED:
		            Toast.makeText(LiloMapActivity.this,"获取房屋信息失败", Toast.LENGTH_LONG).show();
		            break;
				default:
					break;
				}
			}
		};
		
		/**
		 * 点击网格handler
		 */
		touchGridHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MessageEnum.QUERY_SUCCESS:
					tipForm.dismissDialog();
					GetFeaturesResult gridResult = (GetFeaturesResult) msg.obj;
					postGridInfo(gridResult);
					break;
				case MessageEnum.QUERY_FAILED:
					tipForm.dismissDialog();
		            Toast.makeText(LiloMapActivity.this,"获取网格信息失败", Toast.LENGTH_LONG).show();
		            break;
				default:
					tipForm.dismissDialog();
					break;
				}
			}
		};
		
		String[] codes = gridCodes.split(",");
		StringBuffer paramsBuffer = new StringBuffer();
		for(String code:codes)
		{
			paramsBuffer.append("'");
			paramsBuffer.append(code);
			paramsBuffer.append("',");
		}
		String params = paramsBuffer.substring(0, paramsBuffer.lastIndexOf(","));
		//清空绘制的数据
		clearRegionLay();
		//获取区域数据
		new RunQueryDataTask(dataUrl, region_grid, "WGBM in (" + params.toString() + ")", regionHandler).execute("*");
		if(reportType.equals("0"))
		{
			//获取房屋数据
			new RunQueryDataTask(dataUrl, sm_house, "WGBM = '" + params.toString() + "'", houseHandler).execute("*");
		}
	}
	
	/**
	 * 初始化界面控件
	 */
	protected void initUI()
	{
		mapView = (MapView) this.findViewById(R.id.mapview);
		//btnMap2d = (ImageButton) findViewById(R.id.map2d);
		btnMapVista = (ImageButton) findViewById(R.id.mapVista);
		btnMapSplit = (ImageButton) findViewById(R.id.mapSplit);
		registerEvent();
	}

	protected void registerEvent() {
		//btnMap2d.setOnClickListener(new ButtonTouchListener(btnMap2d));
		btnMapVista.setOnTouchListener(new ButtonTouchListener(btnMapVista,LiloMapActivity.this));
		btnMapSplit.setOnTouchListener(new ButtonTouchListener(btnMapSplit,LiloMapActivity.this));
		
	}
	
	/**
	 * 根据查询返回的结果集绘制区域
	 * @param result
	 */
	protected void showQueryResult(GetFeaturesResult result) {
		if (result == null || result.features == null) {
            Toast.makeText(this, "查询结果为空!", Toast.LENGTH_LONG).show();
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
    	
    	if(reportType.equals("1"))
    	{
        	// 把所有查询的几何对象都高亮显示
            for (int m = 0; m < pointsLists.size(); m++) {
                List<Point2D> geoPointList = pointsLists.get(m);
                TouchGridOverlays polygonOverlay = new TouchGridOverlays(getPolygonPaint(Color.BLUE,180,3));
                mapView.getOverlays().add(polygonOverlay);
                polygonOverlays.add(polygonOverlay);
                polygonOverlay.setData(geoPointList);
                polygonOverlay.setShowPoints(false);
               
            }
    	}else{
        	// 把所有查询的几何对象都高亮显示
            for (int m = 0; m < pointsLists.size(); m++) {
                List<Point2D> geoPointList = pointsLists.get(m);
                PolygonOverlay polygonOverlay = new PolygonOverlay(getPolygonPaint(Color.BLUE,180,3));
                mapView.getOverlays().add(polygonOverlay);
                polygonOverlays.add(polygonOverlay);
                polygonOverlay.setData(geoPointList);
                polygonOverlay.setShowPoints(false);
               
            }
            if(reportType.equals("2"))
            {
            	//Point2D point = new Point2D(lon, lat);
            	addLabelOverlays(labelOverlay, mapView, casePoints);
            }
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
   	 	
   	 	if(reportType.equals("0"))
   	 	{
   	    	// 把所有查询的几何对象都高亮显示
   	        for (int m = 0; m < pointsLists.size(); m++) {
   	            List<Point2D> geoPointList = pointsLists.get(m);
   	            TouchHouseOverlays housePolygonOverlay = new TouchHouseOverlays(getPolygonPaint(0x9696fe,50,1,Paint.Style.FILL));
   	            //PolygonOverlay polygonOverlay = new TouchPolygonOverlays(getPolygonPaint(0x9696fe,50,1,Paint.Style.FILL));
   	            mapView.getOverlays().add(housePolygonOverlay);
   	            houseOverlays.add(housePolygonOverlay);
   	            housePolygonOverlay.setData(geoPointList);
   	            housePolygonOverlay.setShowPoints(false);
   	           
   	        }
   	 	}else{
   	    	// 把所有查询的几何对象都高亮显示
   	        for (int m = 0; m < pointsLists.size(); m++) {
   	            List<Point2D> geoPointList = pointsLists.get(m);
   	            PolygonOverlay polygonOverlay = new PolygonOverlay(getPolygonPaint(0x9696fe,50,1,Paint.Style.FILL));
   	            //PolygonOverlay polygonOverlay = new TouchPolygonOverlays(getPolygonPaint(0x9696fe,50,1,Paint.Style.FILL));
   	            mapView.getOverlays().add(polygonOverlay);
   	            houseOverlays.add(polygonOverlay);
   	            polygonOverlay.setData(geoPointList);
   	            polygonOverlay.setShowPoints(false);
   	           
   	        }
   	 	}

        
        this.mapView.invalidate(); //刷新地图
	}
	
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
    	
    	PolygonOverlay overlay = new PolygonOverlay(getPolygonPaint(0xff0000,150,1,Paint.Style.FILL));
        mapView.getOverlays().add(overlay);
        overlay.setData(points);
        overlay.setShowPoints(false);
        mapView.invalidate();
        buildOverlay = overlay;
        
        String houseCode = "";
        for(int i = 0;i<feature.fieldNames.length;i++)
		{
			if(feature.fieldNames[i].equals(getString(R.string.houseCode)))
			{
				houseCode = feature.fieldValues[i];
				break;
			}
		}
		
		showToast("房屋编码：" + houseCode); 
        
		return null;
	}
	
	/**
	 * 提交采集的数据
	 */
	protected void postGridInfo(GetFeaturesResult gridResult)
	{
		if (gridResult == null || gridResult.features == null || gridResult.featureCount == 0) {
            Toast.makeText(this, "查询结果为空!", Toast.LENGTH_LONG).show();
            return;
        }
		
		Feature feature = gridResult.features[0];
		String gridCode = "";
		for(int i = 0;i<feature.fieldNames.length;i++)
		{
			if(feature.fieldNames[i].equals(getString(R.string.gridCode)))
			{
				gridCode = feature.fieldValues[i];
				break;
			}
		}
		
		showToast("网格编码：" + gridCode + "坐标：" + touchPoint.x + "," + touchPoint.y); 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.lilo_map, menu);
		return true;
	}
	

	
    // 绘面风格
    private Paint getPolygonPaint(int color,int alpha,float width) {
      return getPolygonPaint(color, alpha, width, Paint.Style.STROKE);
    }
    
    private Paint getPolygonPaint(int color,int alpha,float width,Style style) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setAlpha(alpha);
        
        paint.setStyle(style);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(width);
        return paint;
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
    

	/**
	 * 触屏Overlays
	 * @author Administrator
	 *
	 */
	class TouchHouseOverlays extends PolygonOverlay
	{
		
		public TouchHouseOverlays(Paint polygonPaint) {
			super(polygonPaint);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event, MapView mapView) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				
                int touchX = Math.round(event.getX());
                int touchY = Math.round(event.getY());
                // 记录点击位置
                Point2D touchPoint = mapView.getProjection().fromPixels(touchX, touchY);
                Boolean isIn = Geometry.isPointInPolygon(new com.supermap.services.components.
                		commontypes.Point2D(touchPoint.x, touchPoint.y), touchGeo);
                if(isIn)
                {
                	showProgressDialog();
                	new RunQueryByPoint(dataUrl, sm_house, touchPoint, touchHouseHandler).execute("*");
                }else
                {
                	showToast("请在划定的区域内点击");
                }
               
            }

            return true;
		}
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
                Log.i("ly","down");
                break;
            case MotionEvent.ACTION_MOVE: // 捕获手指触摸移动动作
                int x = Math.round(event.getX());
                int y = Math.round(event.getY());
                if (Math.abs(x - touchDownX) > 4 || Math.abs(y - touchDownY) > 4) {
                    isAddPoint = false;// 平移不加入该点
                }
                Log.i("ly", Math.abs(x - touchDownX) + "");
                Log.i("ly","move");
                break;
            case MotionEvent.ACTION_UP: // 捕获手指触摸离开动作
            	Log.i("ly","up:" + isAddPoint);
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
	                 /* Boolean isIn = Geometry.isPointInPolygon(new com.supermap.services.components.
	                  		commontypes.Point2D(touchPoint.x, touchPoint.y), touchGeo);*/
	                  
	                  if(!isIn)
	                  {
	                  	showToast("请在划定的区域内点击");
	                  	return true;
	                  }
	                  
	                  addLabelOverlay(labelOverlay, mapView, touchPoint);
                    
                }
                
                break;
        }

            return false;
		}
	}
	
    /**
     *  弹出进度条dialog
     */
    void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(LiloMapActivity.this, "", getResources().getString(R.string.querying),
                    true);
            progressDialog.getWindow().setLayout(400, 200);
        } else {
            progressDialog.show();
        }
    }
    
    /**
     * 关闭进度条
     */
    void dismissDialog()
    {
    	if(progressDialog!=null && progressDialog.isShowing())
    	{
    		progressDialog.dismiss();
    	}
    }

	/**
	 * @param message
	 *            弹出的提示信息
	 * 
	 */
	protected void showToast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT)
				.show();
	}
	
	/**
	 * 采集时标记事件
	 */
	protected void addLabelOverlay(DefaultItemizedOverlay overlay,MapView map,Point2D point)
	{
        if(map.getOverlays().contains(overlay))
        {
        	map.getOverlays().remove(overlay);
        	if(overlay.size() > 0)
        	{
        		overlay.clear();
        	}
        }
        touchPoint = point;
        //TipForm.showToast(point.x + "," + point.y, getApplicationContext());
        OverlayItem layItem = new OverlayItem(point,"","");
        overlay.addItem(layItem);
        map.getOverlays().add(overlay);
        map.invalidate();
        tipForm.showProgressDialog(LiloMapActivity.this);
        new RunQueryByPoint(dataUrl, region_grid, point, touchGridHandler).execute("");
	}
	
	/**
	 * 回显时标记事件
	 */
	protected void addLabelOverlays(DefaultItemizedOverlay overlay,MapView map,List<Point2D> points)
	{
        if(map.getOverlays().contains(overlay))
        {
        	map.getOverlays().remove(overlay);
        	if(overlay.size() > 0)
        	{
        		overlay.clear();
        	}
        }
        for(Point2D point:points)
        {
        	OverlayItem layItem = new OverlayItem(point,"","");
            overlay.addItem(layItem);
        }
        
        map.getOverlays().add(overlay);
        map.invalidate();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.destroy();
	}

	public Geometry getTouchGeo() {
		return touchGeo;
	}

	public String getReportType() {
		return reportType;
	}


	public String getGridCodes() {
		return gridCodes;
	}
	
	
}
