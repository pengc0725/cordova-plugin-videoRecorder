package com.pc.videoRecorder;

import java.io.File;
import java.io.IOException;

import com.synconset.FakeR;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 视频拍摄页面
 * Created by Wood on 2016/4/6.
 */
@SuppressLint({ "HandlerLeak", "ClickableViewAccessibility", "ShowToast" })
@SuppressWarnings("deprecation")
public class RecordVideoActivity extends Activity implements View.OnClickListener {
    private static final int REQ_CODE = 110;
    private static final int RES_CODE = 111;
    /**
     * 录制进度
     */
    private static final int RECORD_PROGRESS = 100;
    /**
     * 录制结束
     */
    private static final int RECORD_FINISH = 101;

    /**
     * 是否有足够的剩余空间
     */
    private boolean haveEnoughSpace = false;
    
    private MovieRecorderView movieRecorderView;
    private Button buttonShoot;
    private RelativeLayout rlBottomRoot;
    private TextView textViewCountDown;
    private TextView textViewUpToCancel;//上移取消
    private TextView textViewReleaseToCancel;//释放取消
    /**
     * 是否结束录制
     */
    private boolean isFinish = true;
    /**
     * 是否触摸在松开取消的状态
     */
    private boolean isTouchOnUpToCancel = false;
    /**
     * 当前进度
     */
    private int currentTime =10;
    
    /**
     * 按下的位置
     */
    private float startY;

    private FakeR fakeR;
    private int maxSecond;
	private boolean isDelete=true;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RECORD_PROGRESS:
                    if (currentTime < 10) {
                        textViewCountDown.setText("00:0" + currentTime);
                    } else {
                        textViewCountDown.setText("00:" + currentTime);
                    }
                    break;
                case RECORD_FINISH:
                    if (isTouchOnUpToCancel) {//录制结束，还在上移删除状态没有松手，就复位录制
                        resetData();
                    } else {//录制结束，在正常位置，录制完成跳转页面
                        isFinish = true;
                        buttonShoot.setEnabled(false);
                        finishActivity();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
         WindowManager.LayoutParams.FLAG_FULLSCREEN);
        fakeR = new FakeR(this);
        setContentView(fakeR.getId("layout", "activity_record_video"));
        maxSecond = getIntent().getIntExtra("MAX_SECOND", 10);
        initView();
    }

    public void initView() {
        ((TextView) findViewById(fakeR.getId("id", "title"))).setText("录制视频");
        movieRecorderView = (MovieRecorderView) findViewById(fakeR.getId("id", "movieRecorderView"));
        buttonShoot = (Button) findViewById(fakeR.getId("id", "button_shoot"));
        rlBottomRoot = (RelativeLayout) findViewById(fakeR.getId("id", "rl_bottom_root"));
        textViewCountDown = (TextView) findViewById(fakeR.getId("id", "textView_count_down"));
        textViewCountDown.setText("10:00");
        textViewUpToCancel = (TextView) findViewById(fakeR.getId("id", "textView_up_to_cancel"));
        textViewReleaseToCancel = (TextView) findViewById(fakeR.getId("id", "textView_release_to_cancel"));

        WindowManager wm = (WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();

		int height = wm.getDefaultDisplay().getHeight();
        movieRecorderView.setMaxSecond(maxSecond);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) movieRecorderView.getLayoutParams();
        layoutParams.width=width;
        layoutParams.height = height;
        movieRecorderView.setLayoutParams(layoutParams);


        FrameLayout.LayoutParams rlBottomRootLayoutParams = (FrameLayout.LayoutParams) rlBottomRoot.getLayoutParams();
        
        rlBottomRootLayoutParams.height = height;
        rlBottomRootLayoutParams.width = width;
        rlBottomRoot.setLayoutParams(rlBottomRootLayoutParams);

        //处理触摸事件
        buttonShoot.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					 textViewUpToCancel.setVisibility(View.VISIBLE);//提示上移取消
					 isFinish = false;//开始录制
					 startY = event.getY();//记录按下的坐标
					 movieRecorderView.record(new MovieRecorderView.OnRecordFinishListener() {
						 @Override
						 public void onRecordFinish() {
							 handler.sendEmptyMessage(RECORD_FINISH);
						 }
					 });
				 } else if (event.getAction() == MotionEvent.ACTION_UP) {
					 textViewUpToCancel.setVisibility(View.GONE);
					 textViewReleaseToCancel.setVisibility(View.GONE);

					 if (startY - event.getY() > 100) {//上移超过一定距离取消录制，删除文件
						 if (!isFinish) {
							 resetData();
						 }
					 } else {
						if (10-movieRecorderView.getTimeCount() > 3) {//录制时间超过三秒，录制完成
							 handler.sendEmptyMessage(RECORD_FINISH);
						 } else {//时间不足取消录制，删除文件
							 Toast.makeText(RecordVideoActivity.this, "视频录制时间太短", Toast.LENGTH_SHORT).show();
							 resetData();
						 }
					 }
				 } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
					 //根据触摸上移状态切换提示
					 if (startY - event.getY() > 100) {
						 isTouchOnUpToCancel = true;//触摸在松开就取消的位置
						 if (textViewUpToCancel.getVisibility() == View.VISIBLE) {
							 textViewUpToCancel.setVisibility(View.GONE);
							 textViewReleaseToCancel.setVisibility(View.VISIBLE);
						 }
					 } else {
						 isTouchOnUpToCancel = false;//触摸在正常录制的位置
						 if (textViewUpToCancel.getVisibility() == View.GONE) {
							 textViewUpToCancel.setVisibility(View.VISIBLE);
							 textViewReleaseToCancel.setVisibility(View.GONE);
						 }
					 }
				 } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
					 resetData();
				 }
                
            	
                return true;
            }
        });

         movieRecorderView.setOnRecordProgressListener(new MovieRecorderView.OnRecordProgressListener() {
            @Override
            public void onProgressChanged(int maxTime, int currentTime) {
                RecordVideoActivity.this.currentTime = currentTime;
                handler.sendEmptyMessage(RECORD_PROGRESS);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        checkCameraPermission();
    }

    /**
     * 检测摄像头和录音权限
     */
    private void checkCameraPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "视频录制和录音没有授权", Toast.LENGTH_LONG);
        } else {
            resetData();
        }
    }

    /**
     * 重置状态
     */
    private void resetData() {
        if (movieRecorderView.getRecordFile() != null)
            movieRecorderView.getRecordFile().delete();
        movieRecorderView.stop();
        isFinish = true;
        currentTime = 0;
        textViewCountDown.setText("00:10");
        buttonShoot.setEnabled(true);
        textViewUpToCancel.setVisibility(View.GONE);
        textViewReleaseToCancel.setVisibility(View.GONE);
        try {
            movieRecorderView.initCamera();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        isFinish = true;
        movieRecorderView.stop();
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     *
     * @param dir 将要删除的文件目录
     * @return
     */
    @SuppressWarnings("unused")
	private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                if (!deleteDir(new File(dir, children[i]))) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    @Override
    public void onDestroy() {
        if (movieRecorderView.getRecordFile() != null) {
            File file = new File(movieRecorderView.getRecordFile().getAbsolutePath());
            if (file != null && file.exists()) {
				if(isDelete){
					 file.delete();
				}
            }
        }
        super.onDestroy();
    }

    /**
     * TODO 录制完成需要做的事情
     */
    private void finishActivity() {
        if (isFinish) {
            movieRecorderView.stop();
            Intent intent = new Intent(this, VideoPreviewActivity.class);
            intent.putExtra("path", movieRecorderView.getRecordFile().getAbsolutePath());
            intent.putExtra("duration", 10-currentTime);
            startActivityForResult(intent, REQ_CODE);
        }
    }

    @Override
    public void onClick(View v) {
    	
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE && resultCode == RES_CODE) {
            setResult(RES_CODE);
            finish();
        }
        if (requestCode == 200 || resultCode == 200) {
			isDelete=false;
        	setResult(200, data); 
        	finish();
        }
    }
    
    @Override  
    public void onBackPressed() {  
        setResult(RESULT_CANCELED);
        finish();
        super.onBackPressed();  
    }
}
