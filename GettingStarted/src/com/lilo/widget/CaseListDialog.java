package com.lilo.widget;

import java.util.ArrayList;
import java.util.List;

import com.lilo.model.CaseModel;
import com.lilo.sm.LiloPathAnalyzeActivity;
import com.lilo.sm.R;
import com.supermap.android.maps.Point2D;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.view.View.OnClickListener;

public class CaseListDialog extends Dialog {

	private Button btnAudit;
	private Button btnAnalyze;
	private ListView caseListView;
	
	private Context context;
	private LiloPathAnalyzeActivity analyzeActivity;
	private List<CaseModel> caseModels;
	
	 // 记录按下事件点
    private float mTouchX;
    private float mTouchY;
    // 记录抬起事件点
    private float mTouchUpX;
    private float mTouchUpY;
    private List<Point2D> geoPoints = null;
    private CaseModel cas = null;
    /**
     * <p>
     * 当前窗口的布局，支持拖动动态布局
     * </p>
     */
    private WindowManager.LayoutParams lp;
	
	public CaseListDialog(Context context,List<CaseModel> models,int theme) {
		super(context,theme);
		this.context = context;
		this.caseModels = models;
		analyzeActivity = (LiloPathAnalyzeActivity) context;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.dialog_case);
		DisplayMetrics metric = new DisplayMetrics();
        //caseListView.getWindowManager().getDefaultDisplay().getMetrics(metric);
		analyzeActivity.getWindowManager().getDefaultDisplay().getMetrics(metric);
		  // 获取当前窗口布局
        lp = getWindow().getAttributes();
        lp.y = analyzeActivity.getTitleBarHeight();
        lp.width = (int) (metric.widthPixels*0.6);
        lp.height = (int) (metric.heightPixels*0.3);
        getWindow().setGravity(Gravity.CENTER | Gravity.TOP);
        getWindow().setAttributes(lp);
		
		btnAudit = (Button) findViewById(R.id.btnAudit);
		btnAnalyze = (Button) findViewById(R.id.btnAnalyze);
		caseListView = (ListView) findViewById(R.id.caseListView);
		final String preName = "案件号:";
		final List<String> caseIds = new ArrayList<String>();
		
		for(CaseModel caseModel:caseModels)
		{
			caseIds.add(preName + caseModel.getCaseId());
		}
		
		//caseListView.setAdapter(new ArrayAdapter<String>(this,R.layout.list_item,new String[1]{"1"}));
		caseListView.setAdapter(new ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_1,caseIds));
		caseListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				//TipForm.showToast(caseIds.get(position),analyzeActivity);
				String cid = caseIds.get(position);
				
				for(CaseModel cm:caseModels)
				{
					if(cid.equals(preName + cm.getCaseId()))
					{
						cas = cm;
						break;
					}
				}
				if(cas!=null)
				{
					Point2D mp = new Point2D(cas.getLon(),cas.getLat());
					geoPoints = analyzeActivity.getGeoPoints();
					if(geoPoints.size() > 1)
					{
						geoPoints.remove(1);
						geoPoints.add(mp);
					}else{
						geoPoints.add(mp);
					}
					analyzeActivity.drawCaseOverlay(mp);
					analyzeActivity.locMap(mp,false);
				}
			}
		});
		btnAnalyze.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(geoPoints == null || geoPoints.size() < 2)
				{
					TipForm.showToast("请选择一个案件号，进行分析", analyzeActivity);
				}else{
					dismiss();
					analyzeActivity.analyzePath();
					geoPoints.remove(1);
					
				}
				
			}
		});
		
		btnAudit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(geoPoints == null || geoPoints.size() < 2)
				{
					TipForm.showToast("请选择一个案件号，进行核实", analyzeActivity);
				}else{
					//此处填写跳转到核实的界面代码
					if(cas!=null)
					{
						TipForm.showToast("案件号：" + cas.getCaseId(), analyzeActivity);
					}
					geoPoints.remove(1);
					dismiss();
				}
			}
		});

	}
	
    /**
     * <p>
     * 相应触碰窗口事件，实现窗口的拖动
     * </p>
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN: // 捕获手指触摸按下动作
            // 获取相对View的坐标，即以此View左上角为原点
            mTouchX = event.getX();
            mTouchY = event.getY();
            break;
        case MotionEvent.ACTION_MOVE: // 捕获手指触摸移动动作
            break;
        case MotionEvent.ACTION_UP: // 捕获手指触摸离开动作
            mTouchUpX = event.getX();
            mTouchUpY = event.getY();
            updateViewPosition();
            break;
        }
        return true;
    }

    private void updateViewPosition() {
        // 更新浮动窗口位置参数
        lp.x = (int) (lp.x + mTouchUpX - mTouchX); // 新位置X坐标
        lp.y = (int) (lp.y + mTouchUpY - mTouchY); // 新位置Y坐标
        this.onWindowAttributesChanged(lp); // 刷新显示
        this.show();
    }
    
    

}
