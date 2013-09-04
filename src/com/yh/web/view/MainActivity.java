package com.yh.web.view;

import com.yh.web.R;
import com.yh.web.cache.CacheObject;
import com.yh.web.cache.HttpUtil;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends BaseActivity {

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// ����¼������GO��ʱ���Զ�����GoBtn��������ָ��URL
		EditText uText = (EditText) findViewById(R.id.uText);
		uText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView view, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_NULL
						&& event.getAction() == KeyEvent.ACTION_UP) {
					goBtnClick(null);
				}
				return true;
			}
		});

		// ����WebClient
		WebView web = (WebView) findViewById(R.id.webView1);
		web.setWebViewClient(new MyWebViewClient(this));
		web.setWebChromeClient(new MyWebChromeClient(this));
		web.getSettings().setJavaScriptEnabled(true);
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
			String url = ((EditText)findViewById(R.id.uText)).getText().toString();
			String fileName = CacheObject.getFileName(url);
			System.out.println("start save: " + url + " to " + fileName);
			HttpUtil.downUrlToFile(url, fileName);
			System.out.println("end save: " + url + " to " + fileName);
			return true;
		case R.id.action_settings:
			System.out.println("setClick");
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
		String url = ((EditText) findViewById(R.id.uText)).getText().toString();
		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			url = "http://" + url;
		}
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
