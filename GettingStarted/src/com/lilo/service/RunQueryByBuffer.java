package com.lilo.service;

import com.lilo.model.MessageEnum;
import com.lilo.util.DataUtil;
import com.supermap.android.data.GetFeaturesResult;
import com.supermap.android.maps.Point2D;
import com.supermap.services.components.commontypes.Geometry;
import com.supermap.services.components.commontypes.GeometryType;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

/**
 * 根据点来查询空间数据
 * @author ly
 *
 */
public class RunQueryByBuffer extends AsyncTask<String, Void, GetFeaturesResult>
{
	private String url;
	private String dataSet;
	private Point2D point;
	private Handler handler;
	
	public RunQueryByBuffer(String url, String dataSet, Point2D point, Handler handler) {
		super();
		this.url = url;
		this.dataSet = dataSet;
		this.point = point;
		this.handler = handler;
	}

	@Override
	protected GetFeaturesResult doInBackground(String... params) {
		Geometry geometry = new Geometry();
        com.supermap.services.components.commontypes.Point2D[] points = new com.supermap.services.components.commontypes.Point2D[] { new com.supermap.services.components.commontypes.Point2D(
                    point.x, point.y) };
        geometry.points = points;
        geometry.type = GeometryType.POINT;
		return DataUtil.excute_bufferQuery(url, dataSet, geometry,0.00008);
	}
	
	@Override
	protected void onPostExecute(GetFeaturesResult result) {
		Message msg = new Message();
        if (result != null) {
            msg.obj = result;
            msg.what = MessageEnum.QUERY_SUCCESS;
        } else {
            msg.what = MessageEnum.QUERY_FAILED;
        }
        handler.sendMessage(msg);
	}
	
}