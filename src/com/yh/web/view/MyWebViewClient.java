/**
 * 
 */
package com.yh.web.view;

import com.yh.web.R;
import com.yh.web.cache.CacheControl;

import android.app.Activity;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

/**
 * @author gudh
 * ×Ô¶¨Òåä¯ÀÀÆ÷WebViewClient
 */
public class MyWebViewClient extends WebViewClient {

	private Activity act;

    public MyWebViewClient(Activity act){
        this.act = act;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        System.out.println(url);
        ((EditText)act.findViewById(R.id.uText)).setText(url);
        view.loadUrl(url);
        return true;
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        super.onLoadResource(view, url);
    }
    
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view,
            String url) {
        return CacheControl.getResource(act, view, url);
    }
}
