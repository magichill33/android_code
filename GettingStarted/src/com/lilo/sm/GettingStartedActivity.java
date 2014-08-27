﻿package com.lilo.sm;


import java.util.ArrayList;
import java.util.List;

import com.lilo.model.CaseModel;
import com.supermap.android.maps.LayerView;
import com.supermap.android.maps.MapView;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class GettingStartedActivity extends Activity {
	// SuperMap iServer提供的地图采用固定地址传递
	//private static final String map2d = "http://192.168.4.252:8090/iserver/services/map-china400/rest/maps/China";
	//private String map2d = "http://support.supermap.com.cn:8090/iserver/services/map-china400/rest/maps/China";
	//private String map2d = "http://192.168.4.252:8090/iserver/services/map-wudu/rest/maps/map2d";
	//protected MapView mapView;
	private Button btnParts;
	private Button btnHouse;
	private Button btnAnalyze;
	private Button btnReport;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		/* mapView = (MapView) this.findViewById(R.id.mapview);
		LayerView layerView = new LayerView(this);
		layerView.setURL(map2d);
		mapView.setBuiltInZoomControls(true);
		mapView.addLayer(layerView);*/
		btnParts = (Button) findViewById(R.id.btnParts);
		btnHouse = (Button) findViewById(R.id.btnHouse);
		btnAnalyze = (Button) findViewById(R.id.btnAnalyze);
		btnReport = (Button) findViewById(R.id.btnReport);
		
		final Intent intent = new Intent();
		intent.setClass(GettingStartedActivity.this, LiloInfoUpdateActivity.class);
		btnParts.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				Bundle bundle = new Bundle();
				bundle.putString("uType", "0");
				bundle.putString("pType", "0118");
				bundle.putString("gridCodes", "62120210200002,62120210200003");
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		
		btnHouse.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Bundle bundle = new Bundle();
				bundle.putString("uType", "1");
				bundle.putString("gridCodes", "62120210000601,62120210000602");
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		
		btnAnalyze.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent1 = new Intent();
				Bundle bundle = new Bundle();
				ArrayList<CaseModel> caseList = new ArrayList<CaseModel>();
				CaseModel c1 = new CaseModel("1001",104.939528,33.374305);
				CaseModel c2 = new CaseModel("1002",104.942456,33.375698);
				CaseModel c3 = new CaseModel("1003",104.943611,33.374319);
				CaseModel c4 = new CaseModel("1004",104.944951,33.374131);
				caseList.add(c1);
				caseList.add(c2);
				caseList.add(c3);
				caseList.add(c4);
				
				bundle.putParcelableArrayList("caselist", caseList);
				intent1.putExtras(bundle);
				//bundle.putCharSequenceArrayList("caselist", caseList)
				intent1.setClass(GettingStartedActivity.this, LiloPathAnalyzeActivity.class);
				startActivity(intent1);
			}
		});
		
		btnReport.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent1 = new Intent();
				Bundle bundle = new Bundle();
				/*bundle.putString("gridCodes", "62120210000601,62120210000602,62120210000603");
				bundle.putString("reportType", "1");*/
				bundle.putString("reportType", "2");
				ArrayList<CaseModel> caselist = new ArrayList<CaseModel>();
				CaseModel  model1 = new CaseModel(104.926879,33.388646,"62120210000602");
				CaseModel  model2 = new CaseModel(104.925386,33.390110,"62120210000603");
				CaseModel  model3 = new CaseModel(104.926917,33.388884,"62120210000602");
				caselist.add(model1);
				caselist.add(model2);
				caselist.add(model3);
				bundle.putParcelableArrayList("caselist", caselist);
				intent1.putExtras(bundle);
				//bundle.putCharSequenceArrayList("caselist", caseList)
				intent1.setClass(GettingStartedActivity.this, LiloMapActivity.class);
				startActivity(intent1);
			}
		});
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

}