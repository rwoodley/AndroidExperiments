package org.woodley.aigrid.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.woodley.aigrid.app.R;

public class AboutPageActivity extends Activity {
    ProgressDialog _dialog ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.getActionBar().setIcon(R.drawable.curiouschimp);
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_page);
        WebView myWebView = (WebView) findViewById(R.id.webview);
        this.setTitle("About this game.");
        final Context ctx = this;
        myWebView.setWebChromeClient(new WebChromeClient() {
        });
        /*
        myWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                _dialog = ProgressDialog.show(ctx, "", "Accessing info page...");
                super.onPageStarted(view, url, favicon);
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                _dialog.dismiss();
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                try{
                    _dialog.dismiss();
                }catch (Exception e) {
                }
            }
        });
        */
        WebSettings w = myWebView.getSettings();
        w.setPluginState(WebSettings.PluginState.ON);
        myWebView.loadUrl("http://rwoodley.org/MyContent/Ayumu/AyumuInfo.htm");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.about_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
