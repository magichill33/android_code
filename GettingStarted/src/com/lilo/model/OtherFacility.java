package com.lilo.model;

/**
 * ������ʩ
 * @author Administrator
 *
 */
public class OtherFacility extends Part{
	private String siteName; //��������(0602)

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	@Override
	public String toString() {
		return super.toString() + "::OtherFacility [siteName=" + siteName + "]";
	}
	
	
}
