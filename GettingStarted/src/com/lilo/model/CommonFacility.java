package com.lilo.model;

/**
 * ������ʩ��
 * 
 * @author Administrator
 *
 */
public class CommonFacility extends Part {
	private String material; //����,���ʺ��ڲ������Ϊ(0120~0121��0124~0126,0131~0137)
	private String tradeScope; //��Ӫ��Χ �ʺ���(0125)
	private int num; //������ʩ��Ŀ �ʺ���(0126)
	public String getMaterial() {
		return material;
	}
	public void setMaterial(String material) {
		this.material = material;
	}
	public String getTradeScope() {
		return tradeScope;
	}
	public void setTradeScope(String tradeScope) {
		this.tradeScope = tradeScope;
	}
	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	@Override
	public String toString() {
		return super.toString() + "::CommonFacility [material=" + material + ", tradeScope="
				+ tradeScope + ", num=" + num + "]";
	}
	
	
}
