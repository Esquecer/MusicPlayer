package com.example.esquecer.myapplication.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.example.esquecer.myapplication.R;

import java.lang.reflect.InvocationTargetException;

public class MusicNewsActivity extends BaseActivity {
    WebView webView;
    private ProgressBar pg1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_news);
        webView = (WebView) findViewById(R.id.newsWebView);
        pg1 = (ProgressBar) findViewById(R.id.progressBar1);
        Intent intent = getIntent();
        String webPath =  intent.getStringExtra("webPath");
        setWebViewSettings();
        setWebClient();
        webView.loadUrl(webPath);
    }
    private void setWebViewSettings() {
        //获取一个webviewsetting对象
        WebSettings setting = webView.getSettings();
        //支持JavaScript
        setting.setJavaScriptEnabled(true);
        //显示缩放控制工具
        setting.setDisplayZoomControls(true);
        //设置webview支持缩放
        setting.setSupportZoom(true);
        setting.setDomStorageEnabled(true);
        setting.setBuiltInZoomControls(true);
        //设置加载进来的页面自适应手机屏幕
        setting.setUseWideViewPort(true);
        setting.setLoadWithOverviewMode(true);
        setting.setCacheMode(WebSettings.LOAD_NO_CACHE); // 不加载缓存内容
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.setWebChromeClient(new WebChromeClient());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setting.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

    }

    private void setWebClient() {
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                // TODO 自动生成的方法存根
                if(newProgress==100){
                    pg1.setVisibility(View.GONE);//加载完网页进度条消失
                }
                else{
                    pg1.setVisibility(View.VISIBLE);//开始加载网页时显示进度条
                    pg1.setProgress(newProgress);//设置进度值
                }

            }
        });
        webView.setWebViewClient(new WebViewClient() {

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                view.loadUrl(url);
                return true;
            }

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            webView.getClass().getMethod("onResume").invoke(webView,(Object[])null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            webView.getClass().getMethod("onPause").invoke(webView,  (Object[])null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
