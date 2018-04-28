package com.pc.videoRecorder;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.synconset.FakeR;

/**
 * 视频认证上传界面
 */
@SuppressLint("HandlerLeak")
public class VideoPreviewActivity extends Activity implements View.OnClickListener  {
    private static final String LOG_TAG = "VideoPreviewActivity";

    /**
     * 播放进度
     */
    private static final int PLAY_PROGRESS = 110;

    private VideoView videoViewShow;
    private ImageView imageViewShow;
    private Button buttonDone;
    private RelativeLayout rlBottomRoot;
    private Button buttonPlay;
    /**
     * 视频路径
     */
    private String path;
    private String previewImg;
    /**
     * 视频时间
     */
    private int time;
    @SuppressWarnings("unused")
	private int currentTime;
    private int duration;
    private Timer timer;
    private FakeR fakeR;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PLAY_PROGRESS:

            	//达到指定时间，停止播放
                if (!videoViewShow.isPlaying() && time > 0) {
                    if (timer != null) {
                        timer.cancel();
                    }
                }
                break;
            }
        }
    };
    /**
     * 要上传的视频文件
     */
    private File file;

    private void assignViews() {
        videoViewShow = (VideoView) findViewById(fakeR.getId("id", "videoView_show"));
        imageViewShow = (ImageView) findViewById(fakeR.getId("id", "imageView_show"));
        buttonDone = (Button) findViewById(fakeR.getId("id", "button_done"));
        rlBottomRoot = (RelativeLayout) findViewById(fakeR.getId("id", "rl_bottom_root"));
        buttonPlay = (Button) findViewById(fakeR.getId("id", "button_play"));
     }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
         WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        fakeR = new FakeR(this);
        setContentView(fakeR.getId("layout", "activity_video_attestation_upload"));
        
        assignViews();
        initView();
        initData();
    }

    public void initView() {
        ((TextView) findViewById(fakeR.getId("id", "title"))).setText("视频预览");
        findViewById(fakeR.getId("id", "title_left")).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				 finish();
			}
		});

        buttonDone.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent data  =new Intent();
				data.putExtra("videoUrl", path);
                data.putExtra("imgUrl", previewImg);
                data.putExtra("duration",duration);
 				setResult(200, data);
	            finish();
			}
		});
        buttonPlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				buttonPlay.setVisibility(View.GONE);
                imageViewShow.setVisibility(View.GONE);
                videoViewShow.setVisibility(View.VISIBLE);
                playVideo();
			}
		});

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) videoViewShow.getLayoutParams();
        layoutParams.height = width * 4 / 3;//根据屏幕宽度设置预览控件的尺寸，为了解决预览拉伸问题
        videoViewShow.setLayoutParams(layoutParams);
        imageViewShow.setLayoutParams(layoutParams);

        FrameLayout.LayoutParams rlBottomRootLayoutParams = (FrameLayout.LayoutParams) rlBottomRoot.getLayoutParams();
        rlBottomRootLayoutParams.height = width / 3 * 2;
        rlBottomRoot.setLayoutParams(rlBottomRootLayoutParams);
    }

    public void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            path = intent.getExtras().getString("path", "");
            duration=intent.getExtras().getInt("duration",0);
            file = new File(path);
        }

        //获取第一帧图片，预览使用
        if (file.length() != 0) {
            MediaMetadataRetriever media = new MediaMetadataRetriever();
            media.setDataSource(path);
            Bitmap bitmap = media.getFrameAtTime();
            saveBitmapFile(bitmap);
            imageViewShow.setImageBitmap(bitmap);
        }
    }

    public void saveBitmapFile(Bitmap bitmap){
        previewImg=getExternalCacheDir()+ File.separator+System.currentTimeMillis() + ".jpg";
        File file=new File(previewImg);//将要保存图片的路径
        try {
            FileOutputStream out= new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 播放视频
     */
    private void playVideo() {
        videoViewShow.setVideoPath(path);
        videoViewShow.start();
        videoViewShow.requestFocus();
        videoViewShow.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
            	Log.e(LOG_TAG, "videoViewShow.isPlaying():"+videoViewShow.isPlaying());
            	initData();
                videoViewShow.setVisibility(View.GONE);
            	imageViewShow.setVisibility(View.VISIBLE);
                buttonPlay.setVisibility(View.VISIBLE);
            }
        });

        currentTime = 0;//时间计数器重新赋值
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(PLAY_PROGRESS);
            }
        }, 0, 100);
    }

    @Override
    public void onClick(View v) {
       
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoViewShow.stopPlayback();
    }
 // 捕获返回键的方法2  
    @Override  
    public void onBackPressed() {  
       super.onBackPressed();
    }
}
