package com.yh.web.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.widget.EditText;
import android.widget.Toast;

import com.yh.web.R;
import com.yh.web.cache.CacheControl;
import com.yh.web.cache.CacheObject;
import com.yh.web.cache.CachePolicy;
import com.yh.web.cache.Config;
import com.yh.web.cache.HttpUtil;
import com.yh.web.cache.MIME;

public class MainActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// ����¼������GO��ʱ���Զ�����GoBtn��������ָ��URL
		EditText uText = (EditText) findViewById(R.id.uText);
		// ��װ�س��Զ�����
		uText.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					goBtnClick(null);
					return true;
				}
				return false;
			}
		});

		// ����WebClient
		WebView web = (WebView) findViewById(R.id.webView1);
		web.setWebViewClient(new MyWebViewClient(this));
		web.setWebChromeClient(new MyWebChromeClient(this));
		setWebSetting(web.getSettings());
		// ���������¼�
		web.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				HitTestResult result = ((WebView) view).getHitTestResult();
				if (result.getType() == WebView.HitTestResult.IMAGE_TYPE) {
					// ������ͼƬ�Ĳ˵���
					ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					ClipData textCd = ClipData.newPlainText("ImageUrl",
							result.getExtra());
					clipboard.setPrimaryClip(textCd);
					Toast.makeText(view.getContext(), "ͼƬURL�ѿ���",
							Toast.LENGTH_LONG).show();
					return true;
				}
				return false;
			}
		});
		// web ��ý���
		web.requestFocus();

		// ��ʼ��MIME
		MIME.initMIME(this.getAssets());
		// ��ʼ��������
		Config.initFilter(this.getAssets());
		// ��ʼ���������
		CachePolicy.initPolicy(this.getAssets());
		// ��ʼ��AsyncHttpClient
		HttpUtil.initAsyncHttpClient(web.getSettings().getUserAgentString());
		// ��ʼ������
		CacheControl.initCache(this);
	}

	@SuppressLint("SetJavaScriptEnabled")
	public void setWebSetting(WebSettings set) {
		set.setJavaScriptEnabled(true);// ����JS

		set.setDomStorageEnabled(true);// ����localStorage
		set.setAppCacheEnabled(true);// ���û���
		// set.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); //
		// ���û��棬����û����������

		set.setSupportZoom(true); // �����Ƿ�֧������
		set.setBuiltInZoomControls(true); // �����Ƿ���ʾ�ڽ����Ź���
		// set.setSavePassword(true); //�����Ƿ񱣴�����
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * �˵�ѡ����
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.action_download:
			// ����URL������
			String url = ((EditText) findViewById(R.id.uText)).getText()
					.toString();
			String fileName = CacheObject.getCacheFileName(url);

			HttpUtil.downUrlToFile(this, url, fileName);

			return true;
		case R.id.action_settings:
			Log.d("debug", "setClick");
			return true;
		case R.id.action_eixt:
			exit();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * ��ʾ�˳���ʾ
	 */
	protected void showExitDialog() {
		Builder builder = new Builder(this);
		builder.setMessage("ȷ���˳���");
		builder.setTitle("��ʾ");
		builder.setPositiveButton("ȷ��", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				exit();
			}
		});
		builder.setNegativeButton("ȡ��", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	/**
	 * ����Go��ť
	 * 
	 * @param view
	 * @return
	 */
	public boolean goBtnClick(View view) {
		EditText uText = (EditText) findViewById(R.id.uText);
		String url = uText.getText().toString();
		if (HttpUtil.isUrl(url)) {
			if (!url.startsWith("http://") && !url.startsWith("https://")) {
				url = "http://" + url;
			}
		} else {
			url = "http://www.google.com.hk/search?q=" + url;
		}
		uText.setText(url);
		WebView web = (WebView) findViewById(R.id.webView1);
		web.loadUrl(url);
		web.requestFocus();
		return true;
	}

	/**
	 * ��׽���ؼ�����
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		WebView web = (WebView) findViewById(R.id.webView1);
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if (web.canGoBack()) {
				web.goBack();
				((EditText) findViewById(R.id.uText)).setText(web.getUrl());
				return true;
			} else {
				showExitDialog();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
}
