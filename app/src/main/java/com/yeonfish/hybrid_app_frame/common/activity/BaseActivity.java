package com.yeonfish.hybrid_app_frame.common.activity;

import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yeonfish.hybrid_app_frame.R;

public class BaseActivity extends AppCompatActivity {

    protected final boolean isPopupActivity = false;

    private boolean isFinishBack = false;
    private final boolean isGNBActivity = true;

    @Override
    public void finish() {
        super.finish();

        if (isPopupActivity)
            overridePendingTransition(R.anim.hold, R.anim.activity_slide_out_bottom);
        else
            overridePendingTransition(R.anim.anim_scale_up, R.anim.anim_slide_out_right);
    }

    @Override
    public void onBackPressed() {
        if (isGNBActivity) {
            if (!isFinishBack) {
                Toast.makeText(this, "'뒤로' 버튼을 한 번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
                isFinishBack = true;
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isFinishBack = false;
                    }
                }, 3000);
            }else {
                finishAffinity();
            }
        }else {
            super.onBackPressed();
        }
    }
}
