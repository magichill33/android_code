package com.lilo.model;

/**
 * ��·��ͨ����
 * @author Administrator
 *
 */
public class RoadTraffic extends Part{
	private String stopName; //վ�� (0203)

	public String getStopName() {
		return stopName;
	}

	public void setStopName(String stopName) {
		this.stopName = stopName;
	}

	@Override
	public String toString() {
		return super.toString() + "::RoadTraffic [stopName=" + stopName + "]";
	}
	
	
}
