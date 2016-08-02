package com.zjxfood.indiana;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.project.util.BitmapUtilSingle;
import com.project.util.Utils;
import com.zjxfood.activity.AppActivity;
import com.zjxfood.activity.R;
import com.zjxfood.common.Constants;
import com.zjxfood.http.XutilsUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 抽奖详情
 * @author zjx
 *
 */
public class MyIndianaInfoActivity extends AppActivity implements OnClickListener{

	private ImageView mBackImage;
	private ListView mListView;
	private MyIndianaInfoAdapter mInfoAdapter;
	private Bundle mBundle;
	private String mId,mName,mPrice,mImageUrl,mStartTime,mEndTime,mLuckyNum;
	private int page=1;
	private HashMap<String, Object> mMap;
	private ArrayList<HashMap<String, Object>> mList;
	private TextView mMallName,mMallPrice;
	private ImageView mImageView;
	private TextView mStateText;
	private BitmapUtils mBitmapUtils;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_indiana_info_layout);
		setImmerseLayout(findViewById(R.id.head_layout));
		mBitmapUtils = BitmapUtilSingle.getBitmapInstance(getApplicationContext());
		init();
		
		mBundle = getIntent().getExtras();
		if(mBundle!=null){
			mId = mBundle.getString("id");
			mName = mBundle.getString("ProductName");
			mImageUrl = mBundle.getString("ImgUrl");
			mStartTime = mBundle.getString("StartTime");
			mEndTime = mBundle.getString("EndTime");
			mPrice = mBundle.getString("Price");
			mLuckyNum = mBundle.getString("LuckyNum");
		}
		if(mId!=null){
			getList();
		}
		if(mName!=null){
			mMallName.setText("【第"+mId+"期】"+mName);
		}
		if(mPrice!=null){
			java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
			Double price = Double.parseDouble(mPrice);
			mMallPrice.setText("￥"+df.format(price)+"食尚币");
		}
		if(mImageUrl!=null){
			mBitmapUtils.display(mImageView, mImageUrl);
		}
		if(mEndTime!=null){
		if(Utils.isEnd(mEndTime)){
			mStateText.setText("进行中，等待开奖");
		}else{
			if(mLuckyNum!=null){
				if(mLuckyNum.equals("0")){
					mStateText.setText("未达到开奖条件，花费的食尚币已退回");
				}else{
					mStateText.setText(mLuckyNum);
				}
			}
		}
		}
	}
	
	private void init(){
		mBackImage = (ImageView) findViewById(R.id.my_indiana_info_back_info_image);
		mListView = (ListView) findViewById(R.id.my_indiana_info_cj_user_list);
		mMallName = (TextView) findViewById(R.id.my_indiana_info_head_content_name);
		mMallPrice = (TextView) findViewById(R.id.my_indiana_info_head_content_price);
		mImageView = (ImageView) findViewById(R.id.my_indiana_info_head_image);
		mStateText = (TextView) findViewById(R.id.my_indiana_info_state_value);
		
		mBackImage.setOnClickListener(this);
	}

	private void getList() {
		StringBuilder sb = new StringBuilder();
		sb.append("gameId=" + mId + "&userId=" + Constants.mId+"&pageSize=5"+"&currentPage="+page);
		XutilsUtils.get(Constants.getMyIndiana, sb, new RequestCallBack<String>() {
			@Override
			public void onFailure(HttpException arg0, String arg1) {}
			@Override
			public void onSuccess(ResponseInfo<String> res) {
				mMap = Constants.getJsonObjectByData(res.result);
				if (mMap != null && mMap.size() > 0 && mMap.get("rows")!=null) {
					mList = Constants.getJsonArray(mMap.get("rows").toString());
					if(mList!=null && mList.size()>0){
						handler.sendEmptyMessageDelayed(1, 0);
					}
				}
			}
		});
	}
	
	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				mInfoAdapter = new MyIndianaInfoAdapter(getApplicationContext(),mList,"");
				mListView.setAdapter(mInfoAdapter);
				break;
			default:
				break;
			}
		};
	};
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.my_indiana_info_back_info_image:
			finish();
			break;

		default:
			break;
		}
	}
}
