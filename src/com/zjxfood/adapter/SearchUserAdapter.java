package com.zjxfood.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zjxfood.activity.R;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchUserAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<HashMap<String, Object>> mList;

	public SearchUserAdapter(Context context,
							 ArrayList<HashMap<String, Object>> list) {
		this.mContext = context;
		this.mList = list;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void notify(ArrayList<HashMap<String, Object>> list){
		this.mList.addAll(list);
		notifyDataSetChanged();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		ViewHolder mHolder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.my_user_search_item, null);
			mHolder = new ViewHolder();
			mHolder.mPhone = (TextView) convertView
					.findViewById(R.id.users_phone_text);
			mHolder.mDate = (TextView) convertView
					.findViewById(R.id.users_date_text);
			mHolder.mStatus = (TextView) convertView
					.findViewById(R.id.my_users_jh_show);
			mHolder.mCallImage = (ImageView) convertView.findViewById(R.id.user_call_image);
			convertView.setTag(mHolder);
		} else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		if(mList.get(position).get("UserName")!=null){
		mHolder.mPhone.setText(mList.get(position).get("UserName").toString());
		}
		if(mList.get(position).get("JhTime")!=null){
		mHolder.mDate.setText((mList.get(position).get("JhTime").toString()).replaceAll("T", " "));
		}else{
			mHolder.mDate.setText(mList.get(position).get("CreateTime").toString());
		}
//		if(mList.get(position).get("IsJh")!=null){
//			if(mList.get(position).get("IsJh").toString().equals("1")){
//				mHolder.mStatus.setText("已激活");
//			}
//		}
		if(mList.get(position).get("Deep")!=null){
			if(mList.get(position).get("Deep").toString().equals("1")){
				mHolder.mCallImage.setVisibility(View.VISIBLE);
				mHolder.mStatus.setText("第"+mList.get(position).get("Deep")+"级");
			}else{
				mHolder.mCallImage.setVisibility(View.GONE);
				mHolder.mStatus.setText("第"+mList.get(position).get("Deep")+"级");
			}
		}
		mHolder.mCallImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent();
				intent.setAction("android.intent.action.CALL");
				intent.setData(Uri.parse("tel:" + mList.get(position).get("UserName")));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(intent);
			}
		});
		return convertView;
	}

	class ViewHolder {
		TextView mPhone;
		TextView mDate;
		TextView mStatus;
		ImageView mCallImage;
	}
}
