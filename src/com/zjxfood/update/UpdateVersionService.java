package com.zjxfood.update;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import com.zjxfood.activity.MySettingActivity;
import com.zjxfood.activity.R;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

/**
 * 检测安装更新文件的助手类
 * 
 * @author Administrator
 * 
 */
public class UpdateVersionService {
	private static final int DOWN = 1;// 用于区分正在下载
	private static final int DOWN_FINISH = 0;// 用于区分下载完成
	private HashMap<String, String> hashMap;// 存储更新版本的xml信息
	private String fileSavePath;// 下载新apk的厨房地点
	private String updateVersionXMLPath;// 检测更新的xml文件
	private int progress;// 获取新apk的下载数据量,更新下载滚动条
	private boolean cancelUpdate = false;// 是否取消下载
	private Context context;
	private ProgressBar progressBar;
	private Dialog downLoadDialog;
	private PopupWindow mUpdatePopup;
	private TextView mNotUpdate, mUpdate;
	private TextView mUpdateContentText;

	private Handler handler = new Handler() {// 更新ui

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch ((Integer) msg.obj) {
			case DOWN:
				progressBar.setProgress(progress);
				break;
			case DOWN_FINISH:
				Toast.makeText(context, "文件下载完成,正在安装更新", Toast.LENGTH_SHORT)
						.show();
				installAPK();
				break;

			default:
				break;
			}
		}

	};

	/**
	 * 构造方法
	 * 
	 * @param updateVersionXMLPath
	 *            比较版本的xml文件地址(服务器上的)
	 * @param context
	 *            上下文
	 */
	public UpdateVersionService(String updateVersionXMLPath, Context context) {
		super();
		this.updateVersionXMLPath = updateVersionXMLPath;
		this.context = context;
	}

	/**
	 * 检测是否可更新
	 * 
	 * @return
	 */
	public void checkUpdate() {
		Log.i("update", "======================");
		if (isUpdate()) {
			showUpdateVersionDialog();// 显示提示对话框
		} else {
			if (MySettingActivity.isAlertFlag.equals("1")) {
				Toast.makeText(context, "已经是最新版本", Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * 更新提示框
	 */
	private void showUpdateVersionDialog() {
		// 构造对话框
		try{
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.update_version_layout, null);
		mNotUpdate = (TextView) view
				.findViewById(R.id.update_version_not_update);
		mUpdate = (TextView) view.findViewById(R.id.update_version_true_update);
		mUpdateContentText = (TextView) view
				.findViewById(R.id.update_version_content_description);

		mUpdatePopup = new PopupWindow(view, LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT, false);
		mUpdatePopup.setBackgroundDrawable(new BitmapDrawable());
		mUpdatePopup.setOutsideTouchable(true);
		mUpdatePopup.setFocusable(true);
		mUpdatePopup.showAtLocation(view, Gravity.CENTER, 0, 0);
		mUpdate.setOnClickListener(mUpdateClick);
		mNotUpdate.setOnClickListener(mNotUpdateClick);
		if (hashMap != null && hashMap.size() > 0) {
			if (hashMap.get("log") != null) {
				mUpdateContentText.setText(hashMap.get("log"));
			}
		}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	// 更新
	android.view.View.OnClickListener mUpdateClick = new android.view.View.OnClickListener() {
		@Override
		public void onClick(View v) {
			try{
			if(mUpdatePopup!=null && mUpdatePopup.isShowing()){
			mUpdatePopup.dismiss();
			}
			showDownloadDialog();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};

	// 不更新
	android.view.View.OnClickListener mNotUpdateClick = new android.view.View.OnClickListener() {
		@Override
		public void onClick(View v) {
			try{
			if(mUpdatePopup!=null && mUpdatePopup.isShowing()){
			mUpdatePopup.dismiss();
			}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	};

	/**
	 * 下载的提示框
	 */
	protected void showDownloadDialog() {
		{
			// 构造软件下载对话框
			AlertDialog.Builder builder = new Builder(context);
			builder.setTitle("正在更新");
			// 给下载对话框增加进度条
			final LayoutInflater inflater = LayoutInflater.from(context);
			View v = inflater.inflate(R.layout.downloaddialog, null);
			progressBar = (ProgressBar) v.findViewById(R.id.updateProgress);
			builder.setView(v);
			// 取消更新
			builder.setNegativeButton("取消", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					// 设置取消状态
					cancelUpdate = true;
				}
			});
			builder.setCancelable(false);
			downLoadDialog = builder.create();
			downLoadDialog.show();
			// 现在文件
			downloadApk();
		}

	}

	/**
	 * 下载apk,不能占用主线程.所以另开的线程
	 */
	private void downloadApk() {
		new downloadApkThread().start();

	}

	/**
	 * 判断是否可更新
	 * 
	 * @return
	 */
	private boolean isUpdate() {
		// int versionCode = getVersionCode(context);
		String versionName = getVersionName(context);
		Log.i("versionName", "=============versionName========" + versionName);
		try {
			// 把version.xml放到网络上，然后获取文件信息
			URL url = new URL(updateVersionXMLPath);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(5 * 1000);
			conn.setRequestMethod("GET");// 必须要大写
			InputStream inputStream = conn.getInputStream();
			// 解析XML文件。
			ParseXmlService service = new ParseXmlService();
			hashMap = service.parseXml(inputStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.i("hashMap", hashMap + "=====================");
		if (null != hashMap) {
			// int serverCode = Integer.valueOf(hashMap.get("version"));
			String serverCodeName = hashMap.get("versionName");
			Log.i("version", serverCodeName + "=====================");
			// 版本判断
			if (versionName != null && serverCodeName!=null) {
				if (!(serverCodeName.equals(versionName))) {
					// Toast.makeText(context, "新版本是: " + serverCode,
					// Toast.LENGTH_SHORT).show();
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 获取当前版本和服务器版本.如果服务器版本高于本地安装的版本.就更新
	 * 
	 * @param context2
	 * @return
	 */
	private int getVersionCode(Context context2) {
		int versionCode = 0;
		try {
			// 获取软件版本号，对应AndroidManifest.xml下android:versionCode
			// PackageManager packageManager = getPackageManager();
			// PackageInfo packInfo;
			// packInfo = packageManager.getPackageInfo(getPackageName(), 0);
			versionCode = context.getPackageManager().getPackageInfo(
					"com.zjxfood.activity", 0).versionCode;
			// Toast.makeText(context, "当前版本是: " + versionCode,
			// Toast.LENGTH_SHORT).show();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;

	}

	/**
	 * 获取当前版本名称和服务器版本名称.如果服务器版本高于本地安装的版本.就更新
	 * 
	 * @param context2
	 * @return
	 */
	private String getVersionName(Context context2) {
		String versionName = "";
		try {
			// 获取软件版本号，对应AndroidManifest.xml下android:versionCode
			// PackageManager packageManager = getPackageManager();
			// PackageInfo packInfo;
			// packInfo = packageManager.getPackageInfo(getPackageName(), 0);
			versionName = context.getPackageManager().getPackageInfo(
					"com.zjxfood.activity", 0).versionName;
			// Toast.makeText(context, "当前版本是: " + versionCode,
			// Toast.LENGTH_SHORT).show();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionName;

	}

	/**
	 * 安装apk文件
	 */
	private void installAPK() {
		File apkfile = new File(fileSavePath, hashMap.get("fileName") + ".apk");
		if (!apkfile.exists()) {
			return;
		}
		// 通过Intent安装APK文件
		Intent i = new Intent(Intent.ACTION_VIEW);
		System.out.println("filepath=" + apkfile.toString() + "  "
				+ apkfile.getPath());
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
				"application/vnd.android.package-archive");
		context.startActivity(i);
		android.os.Process.killProcess(android.os.Process.myPid());// 如果不加上这句的话在apk安装完成之后点击单开会崩溃

	}

	/**
	 * 卸载应用程序(没有用到)
	 */
	public void uninstallAPK() {
		Uri packageURI = Uri.parse("package:com.zjxfood.activity");
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
		context.startActivity(uninstallIntent);

	}

	/**
	 * 下载apk的方法
	 * 
	 * @author rongsheng
	 * 
	 */
	public class downloadApkThread extends Thread {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			try {
				// 判断SD卡是否存在，并且是否具有读写权限
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					// 获得存储卡的路径
					String sdpath = Environment.getExternalStorageDirectory()
							+ "/";
					fileSavePath = sdpath + "download";
					URL url = new URL(hashMap.get("url"));
					// 创建连接
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setReadTimeout(5 * 1000);// 设置超时时间
					conn.setRequestMethod("GET");
					conn.setRequestProperty("Charser",
							"GBK,utf-8;q=0.7,*;q=0.3");
					// 获取文件大小
					int length = conn.getContentLength();
					// 创建输入流
					InputStream is = conn.getInputStream();

					File file = new File(fileSavePath);
					// 判断文件目录是否存在
					if (!file.exists()) {
						file.mkdir();
					}
					File apkFile = new File(fileSavePath,
							hashMap.get("fileName") + ".apk");
					FileOutputStream fos = new FileOutputStream(apkFile);
					int count = 0;
					// 缓存
					byte buf[] = new byte[1024];
					// 写入到文件中
					do {
						int numread = is.read(buf);
						count += numread;
						// 计算进度条位置
						progress = (int) (((float) count / length) * 100);
						// 更新进度
						Message message = new Message();
						message.obj = DOWN;
						handler.sendMessage(message);
						if (numread <= 0) {
							// 下载完成
							// 取消下载对话框显示
							downLoadDialog.dismiss();
							Message message2 = new Message();
							message2.obj = DOWN_FINISH;
							handler.sendMessage(message2);
							break;
						}
						// 写入文件
						fos.write(buf, 0, numread);
					} while (!cancelUpdate);// 点击取消就停止下载.
					fos.close();
					is.close();
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}
}
