package com.android.menu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity1 extends AppCompatActivity {

    private static final int SAMPLE_SIZE = 1024 * 200;
    private static final int MSG_SUCC = 1;
    private static final int MSG_ERR = -1;
    private static final String OUT_PATH = "/sdcard/Music/out.mp3";
    private WorkHandler mHandler = new WorkHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置布局文件
        setContentView(R.layout.activity_main);
        /**
         * 请求权限
         * Manifest.permission.READ_EXTERNAL_STORAGE  读取外部储存权限
         * Manifest.permission.WRITE_EXTERNAL_STORAGE   写入外部储存权限
         * 0： 请求码  与onactivityresult里面的对应 用作判断
         */
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        //通过名为button的id  获取控件
        Button button = findViewById(R.id.button);
        /**
         * 设置button的点击事件
         * setOnClickListener 设置点击事件监听
         * new View.OnClickListener()为监听事件，在里面相应到view的点击
         */
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //通过id editTextTextPersonName 获取到edit
                EditText text1 = findViewById(R.id.editTextTextPersonName);
                //通过id editTextTextPersonName2  获取到edit
                EditText text2 = findViewById(R.id.editTextTextPersonName2);
                //TextUtils.isEmpty 方法判断字符串是否为null或者是空字符串""
                //如果text1 或者text2沒有输入问题，就Toast提示  必须填入路径才能使用
                if ((TextUtils.isEmpty(text1.getText()) || TextUtils.isEmpty(text2.getText()))
                        && TextUtils.isEmpty(text1.getHint()) || TextUtils.isEmpty(text2.getHint())) {
                    Toast.makeText(MainActivity.this, "必须填入路径才能使用", Toast.LENGTH_LONG).show();
                    return;
                }
                //根据输入框中的输入创建新的文件
                File f1_default = new File(text1.getHint().toString());
                //根据输入框中的输入创建新的文件
                File f2_default = new File(text2.getHint().toString());
                //判断两个创建的文件是否存在

                if (f1_default.exists() && f2_default.exists()) {//如果两个文件都存在
                    //创建名为str的字符串集合
                    List<String> str = new ArrayList<>();
                    //把两个文件路径添加到集合中
                    str.add(f1_default.getAbsolutePath());
                    str.add(f2_default.getAbsolutePath());
                    /**
                     * try catch 监听异常
                     */
                    try {
                        //执行 mixMP3方法
                        mixMP3(str, OUT_PATH, false);
                    } catch (IOException e) {
                        e.printStackTrace();
                        //监听到异常 文字显示写入失败
                        reportErr("写入失败");
                    }
                    return;
                }
                //根据输入框中的输入创建新的文件
                File f1 = new File(text1.getText().toString());
                File f2 = new File(text2.getText().toString());
                /**
                 * f1.exists（）方法 判断文件是否存在
                 */
                if (!f1.exists()) {//如果文件f1不存在
                    Toast.makeText(MainActivity.this, f1.getAbsolutePath() + "不存在", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!f2.exists()) {//如果文件f2不存在
                    Toast.makeText(MainActivity.this, f2.getAbsolutePath() + "不存在", Toast.LENGTH_LONG).show();
                    return;
                }

                List<String> str = new ArrayList<>();
                str.add(f1.getAbsolutePath());
                str.add(f2.getAbsolutePath());
                try {
                    mixMP3(str, OUT_PATH, true);
                } catch (IOException e) {
                    e.printStackTrace();
                    //通过handler异步处理写入失败消息
                    //创建handler消息体 message
                    Message msg = mHandler.obtainMessage(MSG_ERR);
                    //为message添加内容
                    msg.obj = "写入失败";
                    //handler发送消息
                    mHandler.sendMessage(msg);
                }
            }
        });
    }

    /**
     * @param paths   需要合并的音频地址
     * @param outPath 输出合并后的音频地址
     * @param isAdd   是否追加
     * @throws IOException
     */
    public void mixMP3(List<String> paths, String outPath, boolean isAdd) throws IOException {
        File file = new File(outPath);
        //如果文件存在 刪除该文件
        if (file.exists()) {
            file.delete();
        }
        //如果文件夹不存在 创建文件夹
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        //获取读写io流，将输入写入到文件中 outpath为输出文件路径
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outPath, isAdd), SAMPLE_SIZE);
        for (int i = 0; i < paths.size(); i++) {
            MediaExtractor extractor = new MediaExtractor();//创建对象
            extractor.setDataSource(paths.get(i));  //设置需播放的视频文件路径

            int track = getAudioTrack(extractor);
            if (track < 0) {
                return;
            }
            //选择音频轨道 (这样在之后调用readSampleData()/getSampleTrackIndex()方法时候，返回的就只是视轨的数据了，其他轨的数据不会被返回)
            extractor.selectTrack(track);
            //循环写入文件
            while (true) {
             //   分配一个新的字节缓冲区。
                ByteBuffer buffer = ByteBuffer.allocate(SAMPLE_SIZE);
                //把MediaExtractor中的数据写入到这个可用的ByteBuffer对象中去，返回值为-1表示MediaExtractor中数据已全部读完
                int sampleSize = extractor.readSampleData(buffer, 0);
                if (sampleSize <= 0) {
                    break;
                }
                byte[] buf = new byte[sampleSize];
                //获取字节数据
                buffer.get(buf, 0, sampleSize);
                //写入文件
                outputStream.write(buf);
                //音轨数据往前读
                extractor.advance();
            }
            //释放资源
            extractor.release();
        }
        //关闭输入流
        outputStream.close();
        //发送写入成功的异步消息
        mHandler.sendEmptyMessage(MSG_SUCC);
    }

    /**
     * 获取音频数据轨道
     *
     * @param extractor
     * @return  如果不是audio根式返回-1
     */
    //TargetApi 标识该方法只能在api为JELLY_BEAN （16）的手机上才能使用
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    //MediaExtractor便于解复用，通常被编码，媒体数据的提取从数据源。
    private static int getAudioTrack(MediaExtractor extractor) {
       //获取轨道数量 进行循环
        for (int i = 0; i < extractor.getTrackCount(); i++) {
            //获取到通道格式
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            //判断是不是audio根式的
            if (Objects.requireNonNull(mime).startsWith("audio")) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 获取申请权限的结果
     * @param requestCode  获取权限请求码
     * @param permissions  申请的权限列表 为上面申请的 读写权限
     * @param grantResults  已经授权的列表
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //根据请求码进行判断  上面获取的时候输入的为0 此处用0判断
        if (requestCode == 0) {
            //循环 已经同意授权的列表 查看是否全部权限都已授权
            for (int i : grantResults) {
                //权限检查结果 如果没有授权就提示需要允许权限才能使用
                if (i != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "需要允许权限才能使用", Toast.LENGTH_LONG).show();
                    //结束当前页面
                    finish();
                    return;
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * handler 处理异步发送过来的消息
     */
    class WorkHandler extends Handler {
        @Override
        //处理消息
        public void handleMessage(@NonNull Message msg) {
            //根据msg.what区分具体是哪条消息
            switch (msg.what) {
                case MSG_SUCC://接收到成功消息进行处理
                    reportSucc();
                    break;
                case MSG_ERR://接收到失败消息处理
                    //获取message中存的内容
                    String err = (String) msg.obj;
                    reportErr(err);
                    break;
            }
            super.handleMessage(msg);
        }
    }

    /**
     * 获取textview  用来显示合并成功信息
     */
    private void reportSucc() {
        TextView view = findViewById(R.id.result);
        view.setText("合并成功,输出路径为:" + OUT_PATH);
        //设置控件显示   View.VISIBLE显示  View.GONE隐藏
        view.setVisibility(View.VISIBLE);
    }

    /**
     * 获取textview  用来显示合并失败信息
     */
    private void reportErr(String msg) {
        TextView view = findViewById(R.id.result);
        view.setText("合并失败," + msg);
        view.setVisibility(View.VISIBLE);
    }
}
