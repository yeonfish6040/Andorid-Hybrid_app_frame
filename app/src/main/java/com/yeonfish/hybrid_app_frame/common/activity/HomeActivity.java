package com.yeonfish.hybrid_app_frame.common.activity;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.yeonfish.hybrid_app_frame.R;
import com.yeonfish.hybrid_app_frame.common.constant.CommonConstant;
import com.yeonfish.hybrid_app_frame.common.util.RouterUtil;
import com.yeonfish.hybrid_app_frame.common.util.StringUtil;
import com.yeonfish.hybrid_app_frame.databinding.ActivityHomeBinding;

import java.net.URISyntaxException;


public class HomeActivity extends BaseActivity implements View.OnClickListener {

    private ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    if (isGranted) {
                        Toast.makeText(HomeActivity.this, "Notifications permission granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(HomeActivity.this, "FCM can't post notifications without POST_NOTIFICATIONS permission",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });

    private static final int FULL_SCREEN_SETTING = View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_IMMERSIVE;

    private String webUrl = null;
    private String currentWebUrl = null;

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (null != getIntent() && getIntent().hasExtra(CommonConstant.INTENT_PARAM_WEBVIEW_URL)) {
            webUrl = getIntent().getStringExtra(CommonConstant.INTENT_PARAM_WEBVIEW_URL);
        }

        binding.btnPrev.setOnClickListener(this);
        binding.btnNext.setOnClickListener(this);
        binding.btnRefresh.setOnClickListener(this);
        binding.btnHome.setOnClickListener(this);

        if (webUrl != null) {
            binding.webviewBrowser.loadUrl(webUrl);
            binding.webviewBrowser.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    showLoading();
                    super.onPageStarted(view, url, favicon);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    currentWebUrl = url;
                    hideLoading();
                }

                @Override
                @Deprecated
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    return checkUrlLoading(url);
                }

                @TargetApi(Build.VERSION_CODES.N)
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    return checkUrlLoading(request.getUrl().toString());
                }
            });

            binding.webviewBrowser.setWebChromeClient(new WebChromeClient() {
                private View chromeCustomView = null;
                private CustomViewCallback customViewCallback = null;
                private int originalSystemUiVisibility = 0;

                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    super.onProgressChanged(view, newProgress);
                    if (newProgress == 100) {
                        binding.btnPrev.setEnabled(binding.webviewBrowser.canGoBack());
                        binding.btnNext.setEnabled(binding.webviewBrowser.canGoForward());
                    }
                }

                @Override
                public Bitmap getDefaultVideoPoster() {
                    return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
                }

                @Override
                public void onShowCustomView(View view, CustomViewCallback callback) {
                    if (chromeCustomView != null) {
                        callback.onCustomViewHidden();
                        return;
                    }
                    chromeCustomView = view;
                    customViewCallback = callback;
                    originalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
                    ((FrameLayout) getWindow().getDecorView()).addView(chromeCustomView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    getWindow().getDecorView().setSystemUiVisibility(FULL_SCREEN_SETTING);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    super.onShowCustomView(view, callback);
                }

                @Override
                public void onHideCustomView() {
                    if (chromeCustomView != null) {
                        ((FrameLayout) getWindow().getDecorView()).removeView(chromeCustomView);
                        getWindow().getDecorView().setSystemUiVisibility(originalSystemUiVisibility);
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        chromeCustomView = null;
                        if (customViewCallback != null) {
                            customViewCallback.onCustomViewHidden();
                            customViewCallback = null;
                        }
                        super.onHideCustomView();
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.webviewBrowser.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.webviewBrowser.onPause();
    }

    private boolean checkUrlLoading(String _url) {
        if (_url != null) {
            String url = _url;
            if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("javascript:")) {
                boolean isIntent = false;
                String pacName = "";
                if (url.startsWith("intent:") && !url.startsWith("intent:kakaolink://send")) {
                    String[] urlInfo = StringUtil.getCustomIntentUrl(url);
                    if (urlInfo.length == 2 && !urlInfo[0].isEmpty() && !urlInfo[1].isEmpty()) {
                        pacName = urlInfo[1];
                        isIntent = true;
                    }
                }
                if (url.startsWith("intent:kakaolink://send") && !appInstalledOrNot("com.kakao.talk")) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.kakao.talk")));
                    return true;
                }

                try {
                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    Uri uri = Uri.parse(intent.getDataString());
                    intent = new Intent(Intent.ACTION_VIEW, uri);
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        if (isIntent) {
                            if (!pacName.isEmpty()) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pacName)));
                                return true;
                            }
                        }
                    }
                } catch (URISyntaxException ex) {
                    return false;
                }
            } else {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPrev:
                if (binding.webviewBrowser.canGoBack()) {
                    binding.webviewBrowser.goBack();
                    Toast.makeText(this, "이전 페이지", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnNext:
                if (binding.webviewBrowser.canGoForward()) {
                    binding.webviewBrowser.goForward();
                    Toast.makeText(this, "다음 페이지", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnHome:
                if (currentWebUrl != null && !currentWebUrl.endsWith("lhb.kr/mobile/")) {
                    RouterUtil.gotoHome(this);
                    Toast.makeText(this, "홈으로 이동합니다.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnRefresh:
                binding.webviewBrowser.reload();
                Toast.makeText(this, "페이지 새로고침", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void showLoading() {
        binding.loadingView.setVisibility(View.VISIBLE);
        ((AnimationDrawable) binding.loadingView.getBackground()).start();
    }

    private void hideLoading() {
        binding.loadingView.setVisibility(View.INVISIBLE);
        ((AnimationDrawable) binding.loadingView.getBackground()).stop();
    }

    private boolean appInstalledOrNot(String uri) {
        try {
            getPackageManager().getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    @Deprecated
    @Override
    public void onBackPressed() {
        if (binding.webviewBrowser.canGoBack()) {
            binding.webviewBrowser.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.hold, R.anim.activity_slide_out_bottom);
    }
}