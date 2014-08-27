package com.lilo.sm;

import java.util.ArrayList;

import com.supermap.android.maps.CloudLayerView;
import com.supermap.android.maps.CoordinateReferenceSystem;
import com.supermap.android.maps.LayerView;
import com.supermap.android.maps.MBTilesLayerView;
import com.supermap.android.maps.MapView;
import com.supermap.android.maps.Point2D;

import com.supermap.services.components.commontypes.LayerCollection;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ZoomControls;

public class MainActivity extends Activity {
	
	//private MapControl mapControl = null;
	//private Workspace workspace;
	private MapView mapView;
	private ZoomControls zoomControls;
	private LayerView baseLayerView;
	//private String map2d = "http://192.168.4.252:8090/iserver/services/map-wudu/rest/maps/map2d";
	//private String map2d = "http://192.168.4.252:8090/iserver/services/map-china400/rest/maps/China";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        mapView = (MapView) this.findViewById(R.id.mapview);

		//LayerView layerView = new LayerView(this);
		//layerView.setURL(map2d);
        MBTilesLayerView layerView = new MBTilesLayerView(this, "SuperMap/mbtiles/mbtile.mbtiles");
		mapView.setBuiltInZoomControls(true);
		mapView.addLayer(layerView);
		
	}

	
	
	@Override
	protected void onDestroy() {
		 if (mapView != null) {
	            mapView.destroy();
	        }
		super.onDestroy();
	}

}
