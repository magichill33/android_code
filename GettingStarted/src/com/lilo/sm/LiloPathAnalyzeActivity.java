package com.lilo.sm;


import java.util.ArrayList;
import java.util.List;

import com.lilo.model.CaseModel;
import com.lilo.util.DrawUtil;
import com.lilo.util.NetWorkAnalystUtil;
import com.lilo.widget.CaseListDialog;
import com.lilo.widget.TipForm;
import com.supermap.android.maps.DefaultItemizedOverlay;
import com.supermap.android.maps.LayerView;
import com.supermap.android.maps.LineOverlay;
import com.supermap.android.maps.MapController;
import com.supermap.android.maps.MapView;
import com.supermap.android.maps.OverlayItem;
import com.supermap.android.maps.MapView.MapViewEventListener;
import com.supermap.android.maps.Point2D;
import com.supermap.android.maps.PointOverlay;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Switch;

public class LiloPathAnalyzeActivity extends Activity {

	private static final int NETWORKANALYST_DIALOG = 0;
	
	private MapView mapView;
	private MapController mapController;
	private LayerView layerView;
	private List<Point2D> geoPoints = new ArrayList<Point2D>();
	private List<PointOverlay> pointOverlays = new ArrayList<PointOverlay>();
	private List<LineOverlay> pathOverlays = new ArrayList<LineOverlay>();
	private List<CaseModel> caseModelList = null;
	private DefaultItemizedOverlay locOverlay = null;
	private PointOverlay caseOverlay = null;
	
	private Boolean isPop = false; //是否弹出列表窗
    private int touchDownX;
    private int touchDownY;
    
	private CaseListDialog caseListDialog;
	
	protected int titleBarHeight;

	private String dataUrl;
	private String analyzeUrl;
	private TipForm tipForm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lilo_path_analyze);
		analyzeUrl = getString(R.string.mainUrl) + "/" +getString(R.string.networkUrl);
		dataUrl = getString(R.string.mainUrl) + "/" + getString(R.string.dataUrl);
		String map2d = getString(R.string.mainUrl) + "/" + getString(R.string.map2d);
		tipForm = new TipForm();
		locOverlay = new DefaultItemizedOverlay(getResources().getDrawable(R.drawable.location));
		Drawable caseDrawable = getResources().getDrawable(R.drawable.light_red);
		
		initMap(map2d);
		caseModelList = getIntent().getExtras().getParcelableArrayList("caselist");
		Point2D locPoint = new Point2D(104.934798, 33.378143);
		//Point2D locPoint = new Point2D(104.944951,33.374131);
		geoPoints.clear();
		geoPoints.add(locPoint);
		drawLocOverlay(locOverlay, locPoint);
		locMap(locPoint, true);
		//mapController.setCenter(locPoint);
		clearOverlay();
		if(caseModelList!=null && caseModelList.size() > 0)
		{
			for(CaseModel caseModel:caseModelList)
			{
				drawPointOverlay(caseModel.getLon(), caseModel.getLat());
			}
		}
		
		caseListDialog = new CaseListDialog(mapView.getContext(), caseModelList,R.style.dialogTheme);
		caseListDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
				WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
	}
	
	protected void drawPointOverlay(double lon,double lat)
	{
		Point2D mp = new Point2D(lon, lat);
		PointOverlay overlay = new PointOverlay(mp, this);
		mapView.getOverlays().add(overlay);
		pointOverlays.add(overlay);
	}
	
	/*
	 * 绘制带有图标的图层
	 */
	public void drawLocOverlay(DefaultItemizedOverlay dOverlay,Point2D point2D)
	{
	     if(mapView.getOverlays().contains(dOverlay))
	     {
        	mapView.getOverlays().remove(dOverlay);
        	if(dOverlay.size() > 0)
        	{
        		dOverlay.clear();
        	}
	     }
	     OverlayItem itme = new OverlayItem(point2D, null, null);
	     dOverlay.addItem(itme);
	     mapView.getOverlays().add(dOverlay);
	     mapView.invalidate();
	}
	
	public void drawCaseOverlay(Point2D point2D)
	{
		if(caseOverlay!= null && mapView.getOverlays().contains(caseOverlay))
	    {
			mapView.getOverlays().remove(caseOverlay);
	    }
		PointOverlay pol = new PointOverlay(new DrawUtil().getPolygonPaint(0xe11100, 200,20));
		pol.setData(point2D);
		mapView.getOverlays().add(pol);
		caseOverlay = pol;
		mapView.invalidate();
	}
	
	/**
	 * 清除案件选中图层
	 * @param map2d
	 */
	public void removeCaseOverlay()
	{
		 if(mapView.getOverlays().contains(caseOverlay))
	     {
        	mapView.getOverlays().remove(caseOverlay);
        	
	     }
	}
	
	protected void initMap(String map2d)
	{
		mapView = (MapView) this.findViewById(R.id.mapview);
		mapView.post(new Runnable() {
			@Override
			public void run() {
				titleBarHeight = initHeight();
			}
		});
		mapView.addMapViewEventListener(new MapViewEventListener() {
			
			@Override
			public void zoomStart(MapView arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void zoomEnd(MapView arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void touch(MapView arg0) {
				showDialog(0);
                Log.i("ly", "touch_up");
			}
			
			@Override
			public void moveStart(MapView arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void moveEnd(MapView arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void move(MapView arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mapLoaded(MapView arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void longTouch(MapView arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		mapController = mapView.getController();
		layerView = new LayerView(this);
		layerView.setURL(map2d);
		mapView.setBuiltInZoomControls(true);
		mapView.addLayer(layerView);
	}
	
    /**
     * 计算标题栏的高度
     * @return
     */
    private int initHeight(){
    	Rect rect =new Rect();
    	Window window =getWindow();
    	mapView.getWindowVisibleDisplayFrame(rect);
    	//状态栏的高度
    	int statusBarHight =rect.top;
    	//标题栏跟状态栏的总体高度
    	int contentViewTop =window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
    	//标题栏的高度
    	int titleBarHeight =contentViewTop -statusBarHight;
    	return titleBarHeight;
    }
    /**
     * 清空绘制区域
     */
    protected void clearOverlay()
    {
         if (pointOverlays.size() != 0) {
             mapView.getOverlays().removeAll(pointOverlays);
             pointOverlays.clear();
         }
         mapView.invalidate();
    }

    public void analyzePath()
    {
    	tipForm.showProgressDialog(LiloPathAnalyzeActivity.this);
		List<List<Point2D>> pointLists = NetWorkAnalystUtil.excutePathService(analyzeUrl, geoPoints);
		 if (pathOverlays.size() != 0) {
             mapView.getOverlays().removeAll(pathOverlays);
             pathOverlays.clear();
         }
		if(pointLists!=null)
        {
        	for (int i = 0; i < pointLists.size(); i++) {
                List<Point2D> geoPointList = pointLists.get(i);
                LineOverlay lineOverlay = new LineOverlay();
                lineOverlay.setLinePaint(new DrawUtil().getPolygonPaint(0xffff00, 200, 5));
                mapView.getOverlays().add(lineOverlay);
                lineOverlay.setData(geoPointList);
                lineOverlay.setShowPoints(false);
                pathOverlays.add(lineOverlay);
            }
            mapView.invalidate();
        }else{
        	TipForm.showToast("没有找到最佳路径", getApplicationContext());
        }
		
		tipForm.dismissDialog();
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		/* menu.add(0, 1, 0, "分析");*/
		return true;
	}
	
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
/*		switch (item.getItemId()) {
		case 1:
			TipForm.showProgressDialog(LiloPathAnalyzeActivity.this);
			List<List<Point2D>> pointLists = NetWorkAnalystUtil.excutePathService(getString(R.string.networkUrl), geoPoints);
	        for (int i = 0; i < pointLists.size(); i++) {
	            List<Point2D> geoPointList = pointLists.get(i);
	            LineOverlay lineOverlay = new LineOverlay();
	            lineOverlay.setLinePaint(new DrawUtil().getPolygonPaint(0xffff00, 200, 5));
	            mapView.getOverlays().add(lineOverlay);
	            lineOverlay.setData(geoPointList);
	            lineOverlay.setShowPoints(false);
	        }
	        mapView.invalidate();
	        TipForm.dismissDialog();
			break;

		default:
			break;
		}*/
		
		return super.onOptionsItemSelected(item);
	}
	
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case NETWORKANALYST_DIALOG:
            if(caseListDialog != null) {
                return caseListDialog;
            }
            break;
        default:
            break;
        }
        return super.onCreateDialog(id);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
        case NETWORKANALYST_DIALOG:
            if (caseListDialog != null) {
                Log.d("iserver", "NetworkAnalystDemo onPrepareDialog!");
            }
            break;
        default:
            break;
        }
        super.onPrepareDialog(id, dialog);
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.destroy();
	}

	public int getTitleBarHeight() {
		return titleBarHeight;
	}
	
	public List<Point2D> getGeoPoints()
	{
		return geoPoints;
	}

	public void locMap(Point2D mp,Boolean islevel)
	{
		mapController.setCenter(mp);
		if(islevel)
		{
			mapController.setZoom(5);
			//TipForm.showToast(mapView.getMaxZoomLevel() + "级", getApplicationContext());
		}
		//mapController.setZoom(mapView.getZoomLevel()-1);
	}
}
