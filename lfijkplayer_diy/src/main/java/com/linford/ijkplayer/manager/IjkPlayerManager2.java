package com.linford.ijkplayer.manager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.linford.ijkplayer.R;
import com.linford.ijkplayer.utils.StringsUtil;
import com.linford.ijkplayer.view.GlideApp;
import com.linford.ijkplayer.view.LayoutQuery;
import com.linford.ijkplayer.view.PlayStateParams;
import com.linford.ijkplayer.view.VerticalSeekBar;
import com.linford.ijkplayer.widget.IRenderView;
import com.linford.ijkplayer.widget.IjkVideoView;

import tv.danmaku.ijk.media.player.IMediaPlayer;

import static com.linford.ijkplayer.view.PlayStateParams.MESSAGE_HIDE_CENTER_BOX;
import static com.linford.ijkplayer.view.PlayStateParams.MESSAGE_SEEK_NEW_POSITION;
import static com.linford.ijkplayer.view.PlayStateParams.SCALETYPE_16_9;
import static com.linford.ijkplayer.view.PlayStateParams.SCALETYPE_4_3;
import static com.linford.ijkplayer.view.PlayStateParams.SCALETYPE_FILLPARENT;
import static com.linford.ijkplayer.view.PlayStateParams.SCALETYPE_FITPARENT;
import static com.linford.ijkplayer.view.PlayStateParams.SCALETYPE_FITXY;
import static com.linford.ijkplayer.view.PlayStateParams.SCALETYPE_WRAPCONTENT;
import static com.linford.ijkplayer.view.PlayStateParams.STATUS_COMPLETED;
import static com.linford.ijkplayer.view.PlayStateParams.STATUS_ERROR;
import static com.linford.ijkplayer.view.PlayStateParams.STATUS_IDLE;
import static com.linford.ijkplayer.view.PlayStateParams.STATUS_LOADING;
import static com.linford.ijkplayer.view.PlayStateParams.STATUS_PLAYING;

/**
 * Created by LinFord on 2017/12/29 .
 * 用于界面管理
 * IMediaPlayer.OnCompletionListener, IMediaPlayer.OnPreparedListener, IMediaPlayer.OnInfoListener,  IMediaPlayer.OnErrorListener, IMediaPlayer.OnSeekCompleteListener,
 */

public class IjkPlayerManager2 implements View.OnClickListener, IMediaPlayer.OnCompletionListener, IMediaPlayer.OnPreparedListener, IMediaPlayer.OnInfoListener, IMediaPlayer.OnErrorListener, IMediaPlayer.OnSeekCompleteListener, SeekBar.OnSeekBarChangeListener, AudioManager.OnAudioFocusChangeListener {

    public static final String TAG = "IjkPlayerManager";
    // @BindView(R.id.ijkPlayer)
    // LFIjkPlayer mVideoIjkplayer;//播放器控件
    IjkVideoView mVideoIjkplayer;
    //顶部控制栏控件
    //  @BindView(R.id.ijkplayer_top_bar)//顶部控制栏根布局
    LinearLayout mIjkplayerTopBar;
    //  @BindView(R.id.app_video_title)//视频标题
    TextView mAppVideoTitle;

    //中间控件
    //  @BindView(R.id.play_icon)//中间播放/暂停的图标
    ImageView mPlayIcon;
    //   @BindView(R.id.app_video_box)
    RelativeLayout mAppVideoBox;//根布局
    //  @BindView(R.id.iv_trumb)//封面
    ImageView mIvTrumb;
    //  @BindView(R.id.volume_controller_container)//音量控制布局
    LinearLayout volumeControllerContainer;
    //  @BindView(R.id.brightness_controller_container)//亮度控制布局
    LinearLayout brightnessControllerContainer;
    //   @BindView(R.id.volume_controller_seekBar)//音量进度条
    VerticalSeekBar volumeControllerSeekBar;
    //   @BindView(R.id.brightness_controller_seekbar)//亮度进度条
    VerticalSeekBar brightnessControllerSeekbar;
    // @BindView(R.id.video_thumb_cover)
    LinearLayout mVideoThumbCover;//封面显示

    //底部控制栏控件
    //  @BindView(R.id.ijkplayer_bottom_bar)//底部控制栏根布局
    LinearLayout mIjkplayerBottomBar;
    // @BindView(R.id.app_video_play)//顶部暂停/播放按钮
    ImageView mAppVideoPlay;
    //  @BindView(R.id.app_video_seekBar)//视频播放进度条
    SeekBar mAppVideoSeekBar;
    // @BindView(R.id.app_video_currentTime)//当前播放进度显示
    TextView mAppVideoCurrentTime;
    //  @BindView(R.id.app_video_endTime)//总播放总进度
    TextView mAppVideoEndTime;
    // @BindView(R.id.video_back)
    ImageView mVideoBack;
    ImageView videoFullScreen;
    ImageView videoRotationScreen;
    //旋转方向
    private boolean fullScreenOnly;
    private boolean portrait;
    private int screenWidthPixels;
    private int status = STATUS_IDLE;
    /**
     * 视频旋转的角度，默认只有0,90.270分别对应向上、向左、向右三个方向
     */
    private int rotation = 0;
    /**
     * 视频显示比例,默认保持原视频的大小
     */
    private String currentShowType = PlayStateParams.SCALETYPE_FITPARENT;
    //最大音量
    private int mMaxVolume;
    //当前音量
    private int mVolume = -1;
    //当前亮度
    private float mBrightness = -1f;
    /**
     * 滑动进度条得到的新位置，和当前播放位置是有区别的,newPosition =0也会调用设置的，故初始化值为-1
     */
    private long newPosition = -1;
    private LayoutQuery mLayoutQuery;
    private AudioManager mAudioManager;
    private GestureDetector mGestureDetector;

    private Activity mActivity;
    private String videoPath;
    private boolean isLive = false;//是否为直播
    //屏幕旋转监听事件
    private OrientationEventListener orientationEventListener;

    /**
     * 构造初始化
     * @param acitivity
     */
    public IjkPlayerManager2(Activity acitivity) {
        this.mActivity = acitivity;
        mVideoIjkplayer = mActivity.findViewById(R.id.ijkPlayer);
        initViewListener();
        portrait=getScreenOrientation()== ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }

    /**
     * 初始化View和事件
     */
    private void initViewListener() {
        mIjkplayerTopBar = mActivity.findViewById(R.id.ijkplayer_top_bar);
        mAppVideoTitle = mActivity.findViewById(R.id.app_video_title);
        mPlayIcon = mActivity.findViewById(R.id.play_icon);
        mAppVideoBox = mActivity.findViewById(R.id.app_video_box);
        mIvTrumb = mActivity.findViewById(R.id.iv_trumb);
        volumeControllerContainer = mActivity.findViewById(R.id.volume_controller_container);
        brightnessControllerContainer = mActivity.findViewById(R.id.brightness_controller_container);
        volumeControllerSeekBar = mActivity.findViewById(R.id.volume_controller_seekBar);
        brightnessControllerSeekbar = mActivity.findViewById(R.id.brightness_controller_seekbar);
        mVideoThumbCover = mActivity.findViewById(R.id.video_thumb_cover);
        mIjkplayerBottomBar = mActivity.findViewById(R.id.ijkplayer_bottom_bar);
        mAppVideoPlay = mActivity.findViewById(R.id.app_video_play);
        mAppVideoSeekBar = mActivity.findViewById(R.id.app_video_seekBar);
        mAppVideoCurrentTime = mActivity.findViewById(R.id.app_video_currentTime);
        mAppVideoEndTime = mActivity.findViewById(R.id.app_video_endTime);
        mVideoBack = mActivity.findViewById(R.id.video_back);
        videoFullScreen = mActivity.findViewById(R.id.app_video_fullscreen);
        videoRotationScreen = mActivity.findViewById(R.id.ijk_iv_rotation);

        mAppVideoPlay.setOnClickListener(this);
        mPlayIcon.setOnClickListener(this);
        mVideoBack.setOnClickListener(this);
        videoFullScreen.setOnClickListener(this);
        videoRotationScreen.setOnClickListener(this);

        screenWidthPixels = mActivity.getResources().getDisplayMetrics().widthPixels;
        mLayoutQuery = new LayoutQuery(mActivity);
        mAudioManager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
        orientationEventListener = new OrientationEventListener(mActivity) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation >= 0 && orientation <= 30 || orientation >= 330 || (orientation >= 150 && orientation <= 210)) {
                    //竖屏
                    if (portrait) {
                        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                        orientationEventListener.disable();
                    }
                } else if ((orientation >= 90 && orientation <= 120) || (orientation >= 240 && orientation <= 300)) {
                    if (!portrait) {
                        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                        orientationEventListener.disable();
                    }
                }
            }
        };
        // startPlay();
        //处理控制栏的显示
        initControllView();
        //注册监听事件
        mVideoIjkplayer.setOnPreparedListener(this);
        mVideoIjkplayer.setOnInfoListener(this);
        mVideoIjkplayer.setOnCompletionListener(this);
        mVideoIjkplayer.setOnErrorListener(this);
        // mVideoIjkplayer.setVideoPlayerListener(this);
        //音乐进度件监听注册
        mAppVideoSeekBar.setOnSeekBarChangeListener(this);
        mGestureDetector = new GestureDetector(mActivity, new MyGestureListener());

    }

    /**
     * 播放后初始化进度条显示(在onPreare执行)
     */
    private void MediaStart() {
        mHandler.sendEmptyMessage(1);
        //初始化视频进度条和文本显示
        mAppVideoSeekBar.setMax(1000);
        long position = mVideoIjkplayer.getCurrentPosition();
        long duration = mVideoIjkplayer.getDuration();
        if (mAppVideoSeekBar != null) {
            if (duration > 0) {
                long pos = 1000L * position / duration;
                mAppVideoSeekBar.setProgress((int) pos);
            }
            int percent = mVideoIjkplayer.getBufferPercentage();
            mAppVideoSeekBar.setSecondaryProgress(percent * 10);
        }

        mAppVideoEndTime.setText(StringsUtil.millisToText(mVideoIjkplayer.getDuration()));
        mAppVideoCurrentTime.setText(StringsUtil.millisToText(mVideoIjkplayer.getCurrentPosition()));

    }

    ////////////////////////////////显示顶部和底部控制栏//////////////////////////////////////////
    private void initControllView() {
        //为的是点击屏幕后,上下控制栏都会消失,设置底部事件后可以延迟消失
        mIjkplayerBottomBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                delay6Second();
                return true;
            }
        });

        delay6Second();

    }

    /**
     * 控制栏与控件的显示与消失
     *
     * @param state
     */
    private void refreshVideoControlUI(int state) {
        mIjkplayerTopBar.setVisibility(state);
        mIjkplayerBottomBar.setVisibility(state);
        mPlayIcon.setVisibility(state);
        mVideoThumbCover.setVisibility(state);
        if (mIjkplayerTopBar.getVisibility() == View.VISIBLE) {
            mAppVideoPlay.requestFocus();
        }
    }

    /**
     * 根据发送过来的参数显示控制栏
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    refreshVideoControlUI(View.INVISIBLE);
                    break;
                case 1:
                    if (mVideoIjkplayer.isPlaying()) {
                        updateTime();
                        mAppVideoPlay.setImageResource(R.drawable.simple_player_icon_media_pause);
                        mPlayIcon.setImageResource(R.drawable.simple_player_center_pause);
                        mHandler.sendEmptyMessageDelayed(1, 1000);
                    }
                    break;
                case 2:
                    if (mVideoIjkplayer.isPlaying()) {
                        mAppVideoPlay.setImageResource(R.drawable.simple_player_icon_media_pause);
                        mPlayIcon.setImageResource(R.drawable.simple_player_center_pause);
                    } else {
                        mPlayIcon.setImageResource(R.drawable.simple_player_center_play);
                        mAppVideoPlay.setImageResource(R.drawable.simple_player_arrow_white_24dp);
                    }
                    break;


            }
        }

        ;
    };

    /**
     * 控制栏延迟6秒后消失
     */
    private void delay6Second() {
        mHandler.removeMessages(0);
        //六秒后隐藏
        mHandler.sendEmptyMessageDelayed(0, 6000);
    }

    ///////////////////////////////////////////控制栏的显示与消失///////////////////////end////////////////////////////////////////
    ///////////////////////////////音源焦点////////////////////////////////////////////////////////

    /**
     * 处理音频相关的操作:
     */
    private void initAudioFocus() {
        //申请音频焦点
        mAudioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    /**
     * 实现音源焦点接口
     *
     * @param focusChange
     */
    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // 长久的失去音频焦点，释放MediaPlayer
                //mVideoIjkplayer.stop();
                mVideoIjkplayer.stopPlayback();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // 暂时失去音频焦点，暂停播放等待重新获得音频焦点
                mVideoIjkplayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                break;
        }
    }

    ///////////////////////////////音源焦点/////////////////////end///////////////////////////////////

    /**
     * 更新进度条
     */
    private void updateTime() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mVideoIjkplayer.isPlaying()) {
                    long position = mVideoIjkplayer.getCurrentPosition();
                    long duration = mVideoIjkplayer.getDuration();
                    if (mAppVideoSeekBar != null) {
                        if (duration > 0) {
                            long pos = 1000L * position / duration;
                            mAppVideoSeekBar.setProgress((int) pos);
                        }
                        int percent = mVideoIjkplayer.getBufferPercentage();
                        mAppVideoSeekBar.setSecondaryProgress(percent * 10);
                    }
                    // Log.i(TAG, "run: 更新音乐进度条===>"+position);
                    //显示当前视频进度文本
                    mAppVideoCurrentTime.setText(StringsUtil.millisToText(position));
                }
            }
        });
    }

    /**
     * 点击事件
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.app_video_play:
                if (mVideoIjkplayer.isPlaying()) {
                    mVideoIjkplayer.pause();
                } else {
                    mVideoIjkplayer.start();
                }
                mHandler.sendEmptyMessage(2);
                break;
            case R.id.play_icon:
                if (mVideoIjkplayer.isPlaying()) {
                    mVideoIjkplayer.pause();
                } else {
                    mVideoIjkplayer.start();
                }
                mHandler.sendEmptyMessage(2);
                break;
            case R.id.video_back:
                mActivity.finish();
                mVideoIjkplayer.stopPlayback();
                break;
            case R.id.ijk_iv_rotation:
                Log.i(TAG, "onClick: 屏幕旋转切换............................");
                fullChangeScreen();
               // setPlayerRotation();
                break;
            case R.id.app_video_fullscreen:
                Log.i(TAG, "onClick: 屏幕比例切换............................");
                toggleAspectRatio();
                break;
        }
    }


    /////////////////////////////////Ijkplayer播放器回调的监听////////////////////////////////////////////////

    @Override
    public void onPrepared(IMediaPlayer mp) {
        //每隔0.5秒更新视屏界面信息，如进度条，当前播放时间点等等
        // startPlay();
        MediaStart();
    }


    @Override
    public void onCompletion(IMediaPlayer mp) {
        statusChange(STATUS_COMPLETED);

    }


    @Override
    public void onSeekComplete(IMediaPlayer mp) {

    }


    @Override
    public boolean onError(IMediaPlayer mp, int what, int extra) {
        statusChange(STATUS_ERROR);

        return true;
    }

    //    private int MEDIA_INFO_VIDEO_RENDERING_START = 3;//视频准备渲染
//    int MEDIA_INFO_BUFFERING_START = 701;//开始缓冲
//    int MEDIA_INFO_BUFFERING_END = 702;//缓冲结束
//    int MEDIA_INFO_VIDEO_ROTATION_CHANGED = 10001;//视频选择信息
//    int MEDIA_ERROR_SERVER_DIED = 100;//视频中断，一般是视频源异常或者不支持的视频类型。
//    int MEDIA_ERROR_IJK_PLAYER = -10000;//一般是视频源有问题或者数据格式不支持，比如音频不是AAC之类的
//    int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;//数据错误没有有效的回收
    @Override
    public boolean onInfo(IMediaPlayer mp, int what, int extra) {
        switch (what) {
            case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                statusChange(STATUS_LOADING);
                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                statusChange(STATUS_PLAYING);
                break;
            case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH:
                //显示下载速度
//                      Toast.show("download rate:" + extra);
                break;
            case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                statusChange(STATUS_PLAYING);
                break;
        }
        return false;
    }

    /////////////////////////////////Ijkplayer播放器回调的监听//////////////////////end////////////////////////

    ///////////////////////////////进度条监听事件////////////////////////////////////start/////////////////////
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.app_video_seekBar:
                if (!fromUser) {//一定要判断一下是否用户操作,否则进度条会自己拖动,吃了它的大坑!
                    return;
                }
                long duration = mVideoIjkplayer.getDuration();
                int position = (int) ((duration * progress * 1.0) / 1000);
                mVideoIjkplayer.seekTo(position);
                break;
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()) {
            case R.id.app_video_seekBar:
                long duration = mVideoIjkplayer.getDuration();
                mVideoIjkplayer.seekTo((int) ((duration * seekBar.getProgress() * 1.0) / 1000));
                break;
        }
    }

    ///////////////////////////////进度条监听事件//////////////////////////////////end////////////////////////////


    /**
     * 定时隐藏
     */
    @SuppressLint("HandlerLeak")
    private Handler mDismissHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                /**滑动完成，隐藏滑动提示的box*/
                case MESSAGE_HIDE_CENTER_BOX:
                    volumeControllerContainer.setVisibility(View.GONE);
                    brightnessControllerContainer.setVisibility(View.GONE);
                    mLayoutQuery.id(R.id.app_video_fastForward_box).gone();
                    break;
                /**滑动完成，设置播放进度*/
                case MESSAGE_SEEK_NEW_POSITION:
                    if (newPosition >= 0) {
                        mVideoIjkplayer.seekTo((int) newPosition);
                        newPosition = -1;
                    }
                    break;

            }
        }
    };


    /**
     * 定义手势监听类
     */
    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        /**
         * 单击事件
         *
         * @param e
         * @return
         */
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            refreshVideoControlUI(mIjkplayerBottomBar.getVisibility() == View.VISIBLE ? View.INVISIBLE
                    : View.VISIBLE);
            return super.onSingleTapUp(e);
        }

        /**
         * 双击
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mVideoIjkplayer.isPlaying()) {
                mVideoIjkplayer.pause();
            } else {
                mVideoIjkplayer.start();
            }
            mHandler.sendEmptyMessage(2);

            return true;
        }

        /**
         * 滑动
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            float mOldX = e1.getX(), mOldY = e1.getY();
            float deltaY = mOldY - e2.getY();
            float deltaX = mOldX - e2.getX();
            int y = (int) e2.getRawY();

            Display disp = mActivity.getWindowManager().getDefaultDisplay();
            int windowWidth = disp.getWidth();
            int windowHeight = disp.getHeight();

            boolean seekTo = Math.abs(distanceX) >= Math.abs(distanceY);
            boolean volumeControl = mOldX > windowWidth * 4.0 / 5;

            //歌曲进度滑动
            if (seekTo) {
                onProgressSlide(-deltaX / mVideoIjkplayer.getWidth());
            } else {
                float percent = deltaY / mVideoIjkplayer.getHeight();

                //右边音量改变
                if (volumeControl) {// 右边滑动
                    Log.i(TAG, "onScroll: 右边滑动===>" + (mOldY - y) / windowHeight);
                    onVolumeSlide(percent);
                } else {// 左边亮度滑动
                    onBrightnessSlide(percent);
                    Log.i(TAG, "onScroll: 右边滑动===>" + (mOldY - y) / windowHeight);
                }
            }


            return super.onScroll(e1, e2, distanceX, distanceY);
        }

    }

    /**
     * 左右滑动切换进度条
     *
     * @param percent
     */
    private void onProgressSlide(float percent) {
        int position = (int) mVideoIjkplayer.getCurrentPosition();
        long duration = mVideoIjkplayer.getDuration();
        long deltaMax = Math.min(100 * 1000, duration - position);
        long delta = (long) (deltaMax * percent);
        newPosition = delta + position;
        if (newPosition > duration) {
            newPosition = duration;
        } else if (newPosition <= 0) {
            newPosition = 0;
            delta = -position;
        }
        int showDelta = (int) delta / 1000;
        if (showDelta != 0) {//显示中间进度文本显示
            mLayoutQuery.id(R.id.app_video_fastForward_box).visible();
            String text = showDelta > 0 ? ("+" + showDelta) : "" + showDelta;
            mLayoutQuery.id(R.id.app_video_fastForward).text(text + "s");
            mLayoutQuery.id(R.id.app_video_fastForward_target).text(StringsUtil.millisToText(newPosition) + "/");
            mLayoutQuery.id(R.id.app_video_fastForward_all).text(StringsUtil.millisToText(duration));
        }
    }

    /**
     * 滑动改变声音大小
     *
     * @param percent
     */
    private void onVolumeSlide(float percent) {
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        if (mVolume == -1) {
            mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mVolume < 0)
                mVolume = 0;

            // 显示
            volumeControllerContainer.setVisibility(View.VISIBLE);
        }

        int index = (int) (percent * mMaxVolume) + mVolume;
        if (index > mMaxVolume)
            index = mMaxVolume;
        else if (index < 0)
            index = 0;

        // 变更声音
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
        // 变更进度条
       /* ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
        lp.width = findViewById(R.id.operation_full).getLayoutParams().width
                * index / mMaxVolume;
        mOperationPercent.setLayoutParams(lp);*/
        volumeControllerSeekBar.setMax(mMaxVolume);
        volumeControllerSeekBar.setProgress(index);
    }

    /**
     * 滑动改变亮度
     *
     * @param percent
     */
    private void onBrightnessSlide(float percent) {
        if (mBrightness < 0) {
            mBrightness = mActivity.getWindow().getAttributes().screenBrightness;
            if (mBrightness <= 0.00f)
                mBrightness = 0.50f;
            if (mBrightness < 0.01f)
                mBrightness = 0.01f;

            // 显示
            // mOperationBg.setImageResource(R.drawable.video_brightness_bg);
            brightnessControllerContainer.setVisibility(View.VISIBLE);
        }
        WindowManager.LayoutParams lpa = mActivity.getWindow().getAttributes();
        lpa.screenBrightness = mBrightness + percent;
        if (lpa.screenBrightness > 1.0f)
            lpa.screenBrightness = 1.0f;
        else if (lpa.screenBrightness < 0.01f)
            lpa.screenBrightness = 0.01f;
        mActivity.getWindow().setAttributes(lpa);

        brightnessControllerSeekbar.setMax(100);
        brightnessControllerSeekbar.setProgress((int) (lpa.screenBrightness * 100));
        Log.i(TAG, "onTouchEvent: ===>当前亮度" + lpa.screenBrightness);

    }

    ///////////////////////////////手势滑动监听////////////////////////////////////end//////////////////////

    //////////////////////////////////////////屏幕旋转//////////////////////////////start///////////////////
    private int getScreenOrientation() {
        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && width > height) {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_270:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
            }
        }
        // if the device's natural orientation is landscape or if the device
        // is square:
        else {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_180:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Surface.ROTATION_270:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }
        }
        return orientation;
    }

    public void setFullScreenOnly(boolean fullScreenOnly) {
        this.fullScreenOnly = fullScreenOnly;
        tryFullScreen(fullScreenOnly);
        if (fullScreenOnly) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
    }

    private void tryFullScreen(boolean fullScreen) {
        if (mActivity instanceof AppCompatActivity) {
            ActionBar supportActionBar = ((AppCompatActivity) mActivity).getSupportActionBar();
            if (supportActionBar != null) {
                if (fullScreen) {
                    supportActionBar.hide();
                } else {
                    supportActionBar.show();
                }
            }
        }
        setFullScreen(fullScreen);
    }

    private void setFullScreen(boolean fullScreen) {
        if (this != null) {
            WindowManager.LayoutParams attrs = mActivity.getWindow().getAttributes();
            if (fullScreen) {
                attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                mActivity.getWindow().setAttributes(attrs);
                mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            } else {
                attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
                mActivity.getWindow().setAttributes(attrs);
                mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            }
        }
    }

    private void fullChangeScreen() {
        if (mActivity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {// 切换为竖屏
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    /**
     * 旋转角度
     */
    public IjkPlayerManager2 setPlayerRotation() {
        if (rotation == 0) {
            rotation = 90;
        } else if (rotation == 90) {
            rotation = 270;
        } else if (rotation == 270) {
            rotation = 0;
        }
        setPlayerRotation(rotation);
        return this;
    }

    /**
     * 旋转指定角度
     */
    public IjkPlayerManager2 setPlayerRotation(int rotation) {
        if (mVideoIjkplayer != null) {
            mVideoIjkplayer.setPlayerRotation(rotation);
        }
        return this;
    }
    //////////////////////////////////////////屏幕旋转//////////////////////////////end///////////////////

    ////////////////////////////////////提供外部接收数据的方法/////////////////////////////////////////////////

    /**
     * 获取手势
     *
     * @return
     */
    public GestureDetector getGestureDetector() {
        return mGestureDetector;
    }
    /**
     * 开始播放
     */
/*    private void startPlay() {
        final VideoInfo videoInfo = mActivity.getIntent().getParcelableExtra("videoInfo");
        mVideoIjkplayer.setVideoPath(videoInfo.getPath());
        mAppVideoTitle.setText(videoInfo.getTitle());
        mHandler.sendEmptyMessage(1);

        //获取音源焦点
        initAudioFocus();
    }*/

    /**
     * 播放
     */
    public void play() {
        //initViewListener();
        mVideoIjkplayer.setVideoPath(videoPath);
        mVideoIjkplayer.start();
        mHandler.sendEmptyMessage(1);
        //获取音源焦点
        initAudioFocus();
    }

    /**
     * 设置视频播放地址
     *
     * @param videoPath
     */
    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    /**
     * 设置封面
     *
     * @param thumbPath
     */
    public void setThumbPath(String thumbPath) {
        GlideApp.with(mActivity).asBitmap().load(thumbPath).into(mIvTrumb);
        mHandler.sendEmptyMessageDelayed(0, 2000);
    }

    /**
     * 设置标题
     *
     * @param videoTitle
     */
    public void setVideoTitle(String videoTitle) {
        mAppVideoTitle.setText(videoTitle);
    }

    /**
     * 手势结束
     */
    public void endGesture() {
        mVolume = -1;
        mBrightness = -1f;

        if (newPosition >= 0) {
            mDismissHandler.removeMessages(MESSAGE_SEEK_NEW_POSITION);
            mDismissHandler.sendEmptyMessage(MESSAGE_SEEK_NEW_POSITION);
        } else {
            /**什么都不做(do nothing)*/
        }
        // 隐藏
        mDismissHandler.removeMessages(MESSAGE_HIDE_CENTER_BOX);
        mDismissHandler.sendEmptyMessageDelayed(MESSAGE_HIDE_CENTER_BOX, 500);
    }

    /**
     * 当点击设备回退按钮时调用
     * @return
     */
    public boolean onBackPressed() {
        mVideoIjkplayer.pause();
        if (!fullScreenOnly && getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return true;
        }
        return false;
    }

    /**
     * 改变视频播放比例
     *
     * @return
     */
    public IjkPlayerManager2 toggleAspectRatio() {
        if (mVideoIjkplayer != null) {
            mVideoIjkplayer.toggleAspectRatio();
        }
        return this;
    }

    /**
     * 指定视频比例
     * <pre>
     *     fitParent:可能会剪裁,保持原视频的大小，显示在中心,当原视频的大小超过view的大小超过部分裁剪处理
     *     fillParent:可能会剪裁,等比例放大视频，直到填满View为止,超过View的部分作裁剪处理
     *     wrapContent:将视频的内容完整居中显示，如果视频大于view,则按比例缩视频直到完全显示在view中
     *     fitXY:不剪裁,非等比例拉伸画面填满整个View
     *     16:9:不剪裁,非等比例拉伸画面到16:9,并完全显示在View中
     *     4:3:不剪裁,非等比例拉伸画面到4:3,并完全显示在View中
     * </pre>
     *
     * @param scaleType
     */
    public void setScaleType(String scaleType) {
        currentShowType=scaleType;
        if (SCALETYPE_FITPARENT.equals(scaleType)) {
            mVideoIjkplayer.setAspectRatio(IRenderView.AR_ASPECT_FIT_PARENT);
        } else if (SCALETYPE_FILLPARENT.equals(scaleType)) {
            mVideoIjkplayer.setAspectRatio(IRenderView.AR_ASPECT_FILL_PARENT);
        } else if (SCALETYPE_WRAPCONTENT.equals(scaleType)) {
            mVideoIjkplayer.setAspectRatio(IRenderView.AR_ASPECT_WRAP_CONTENT);
        } else if (SCALETYPE_FITXY.equals(scaleType)) {
            mVideoIjkplayer.setAspectRatio(IRenderView.AR_MATCH_PARENT);
        } else if (SCALETYPE_16_9.equals(scaleType)) {
            mVideoIjkplayer.setAspectRatio(IRenderView.AR_16_9_FIT_PARENT);
        } else if (SCALETYPE_4_3.equals(scaleType)) {
            mVideoIjkplayer.setAspectRatio(IRenderView.AR_4_3_FIT_PARENT);
        }
    }


    ////////////////////////////////////供外部调用的播放状态的回调接口,在父类onInfo接口执行/////////////////////////
    private void statusChange(int newStatus) {
        status = newStatus;
        if (!isLive && newStatus == STATUS_COMPLETED) {
            Log.d(TAG, "statusChange STATUS_COMPLETED...");
            if (playerStateListener != null) {
                playerStateListener.onComplete();
            }
        } else if (newStatus == STATUS_ERROR) {
            Log.d(TAG, "statusChange STATUS_ERROR...");
            if (playerStateListener != null) {
                playerStateListener.onError();
            }
        } else if (newStatus == STATUS_LOADING) {
            //加载进度条显示
            mLayoutQuery.id(R.id.app_video_loading).visible();
            if (playerStateListener != null) {
                playerStateListener.onLoading();
            }
            Log.d(TAG, "statusChange STATUS_LOADING...");
        } else if (newStatus == STATUS_PLAYING) {
            //加载进度条消失
            mLayoutQuery.id(R.id.app_video_loading).invisible();
            Log.d(TAG, "statusChange STATUS_PLAYING...");
            if (playerStateListener != null) {
                playerStateListener.onPlay();
            }
        }
    }

    private PlayerStateListener playerStateListener;

    public void setPlayerStateListener(PlayerStateListener playerStateListener) {
        this.playerStateListener = playerStateListener;
    }

    public interface PlayerStateListener {
        void onComplete();

        void onError();

        void onLoading();

        void onPlay();
    }
    ////////////////////////////////////供外部调用的播放状态的回调接口,与Ijkpayer的回调方法是间接关系(onInfo)/////////////////////////


    //////////////////////////////////////IjkPlayer播放器的声明周期控制////////////////////////////////////////////////////
    public void onPause() {
        if (status == STATUS_PLAYING) {
            mVideoIjkplayer.pause();
        }
    }

    public void onResume() {
        long position = mVideoIjkplayer.getCurrentPosition();
        long duration = mVideoIjkplayer.getDuration();
        if (status == STATUS_PLAYING) {
            if (isLive) {
                mVideoIjkplayer.seekTo(0);
            } else {
                if (position > 0) {
                    long pos = 1000L * position / duration;
                    mVideoIjkplayer.seekTo((int) pos);
                }
            }
            mVideoIjkplayer.start();
        }
    }

    public void onDestroy() {
        orientationEventListener.disable();
        mVideoIjkplayer.stopPlayback();
    }
    //////////////////////////////////////IjkPlayer播放器的声明周期控制////////////////////////////////////////////////////

}
