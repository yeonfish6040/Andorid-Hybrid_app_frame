package com.yeonfish.hybrid_app_frame.common.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.yeonfish.hybrid_app_frame.R;
import com.yeonfish.hybrid_app_frame.common.activity.HomeActivity;
import com.yeonfish.hybrid_app_frame.common.constant.CommonConstant;
import com.yeonfish.hybrid_app_frame.common.constant.URLConstant;

public class RouterUtil {

    public static void gotoHome(Context context) {
        gotoWebViewActivity(context, URLConstant.WEB_URL_HOME);
    }

    public static void gotoHome(Context context, String url) {
        if (url != null) {
            gotoWebViewActivity(context, url);
        }
    }

    public static void gotoWebViewActivity(Context context, String url) {
        if (context != null) {
            Intent intent = new Intent(context, HomeActivity.class);
            intent.putExtra(CommonConstant.INTENT_PARAM_WEBVIEW_URL, url);
            context.startActivity(intent);
            applyActivityTransition(context);
        }
    }

    /**
     * GNB 클릭시 트랜지션 처리
     *
     * @param context
     */
    private static void applyActivityTransition(Context context) {
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(R.anim.hold, R.anim.hold);
        }
    }

    private static void applySubActivityTransition(Context context) {
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_scale_down);
        }
    }

    private static void applyPopupActivityTransition(Context context) {
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(R.anim.activity_slide_in_top, R.anim.hold);
        }
    }
}