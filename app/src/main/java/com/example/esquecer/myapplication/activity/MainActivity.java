package com.example.esquecer.myapplication.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.os.Message;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.esquecer.myapplication.R;
import com.example.esquecer.myapplication.adapter.TestPagerAdapter;
import com.example.esquecer.myapplication.bean.newsMessage;
import com.example.esquecer.myapplication.dao.DataCleanManager;
import com.example.esquecer.myapplication.dao.reptileMusicMessage;
import com.example.esquecer.myapplication.filter.musicFilter;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        ViewPager.OnPageChangeListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ViewPager vpager_four;
    private ImageView img_cursor, tv_one,tv_two, tv_three;
    private ArrayList<View> listViews;
    private int offset = 0;//移动条图片的偏移量
    private int currIndex = 0;//当前页面的编号
    private int bmpWidth;// 移动条图片的长度
    private int one = 0; //移动条滑动一页的距离
    private int two = 0; //滑动条移动两页的距离

    private ListView musicListView;
    private ArrayList<newsMessage> newsList ;
    MusicAdapter myAdapter = new MusicAdapter();

    private static final int INTERNAL_TIME = 1000;
    final MediaPlayer mp = new MediaPlayer();
    String song_path = "";
    private SeekBar seekBar;
    private TextView currentTV;
    private TextView totalTV;
    boolean isStop = true;
    private boolean isSeekBarChanging;//互斥变量，防止进度条与定时器冲突。
    private int currentposition;//当前音乐播放的进度
    private Timer timer;
    private ArrayList<String> list,lists;
    private File[] songFiles;
    ImageButton btnpause;
    View viewOne,viewTwo,viewThree;
    public TextView tv_cashe;
    String chcheFilePath = null;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            // 展示给进度条和当前时间
            int progress = mp.getCurrentPosition();
            seekBar.setProgress(progress);
            currentTV.setText(formatTime(progress));
            // 继续定时发送数据
            updateProgress();
            return true;
        }
    });

    //3、使用formatTime方法对时间格式化：
    private String formatTime(int length) {
        Date date = new Date(length);
        //时间格式化工具
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        String totalTime = sdf.format(date);
        return totalTime;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        new Thread(new Runnable() {
            @Override
            public void run() {
                reptileMusicMessage ref = new reptileMusicMessage();
                newsList = ref.getNewsFromSearch("音乐");
                /*
                Log.i(TAG, "onCreate1: "+newsList.size());
                for(int i = 0 ; i < newsList.size();i++){
                    Log.i(TAG, "onCreate1: "+ newsList.get(i).getNewsTitle()+" "+newsList.get(i).getNewsPath() +" "+newsList.get(i).getNewsTime()+" "+newsList.get(i).getNewsSource());
                }*/
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                      myAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }
    private void initViews() {
        chcheFilePath = this.getCacheDir().getPath();
        Button button = (Button) this.findViewById(R.id.btn_test);
        vpager_four = (ViewPager) findViewById(R.id.vpager_four);
        tv_one = (ImageView) findViewById(R.id.tv_one);
        tv_two = (ImageView) findViewById(R.id.tv_two);
        tv_three = (ImageView) findViewById(R.id.tv_three);
        img_cursor = (ImageView) findViewById(R.id.img_cursor);

        //下划线动画的相关设置：
        bmpWidth = BitmapFactory.decodeResource(getResources(), R.mipmap.line).getWidth();// 获取图片宽度
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;// 获取分辨率宽度
        offset = (screenW / 3 - bmpWidth) / 2;// 计算偏移量
        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);
        img_cursor.setImageMatrix(matrix);// 设置动画初始位置
        //移动的距离
        one = offset * 2 + bmpWidth;// 移动一页的偏移量,比如1->2,或者2->3
        two = one * 2;// 移动两页的偏移量,比如1直接跳3
        //往ViewPager填充View，同时设置点击事件与页面切换事件
        listViews = new ArrayList<View>();
        LayoutInflater mInflater = getLayoutInflater();
        viewOne = mInflater.inflate(R.layout.view_one, null, false);
        viewTwo = mInflater.inflate(R.layout.view_two, null, false);
        viewThree = mInflater.inflate(R.layout.view_three, null, false);

        /*---------------设置按钮事件-----------------*/
        Button btclose = ((Button)viewThree.findViewById(R.id.btClose));
        btclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitDialog();
            }
        });
        tv_cashe=(TextView) viewThree.findViewById(R.id.textChche);
        //获得应用内部缓存(/data/data/com.example.androidclearcache/cache)
        File datafile =new File(chcheFilePath);
        try {
            tv_cashe.setText("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t当前缓存：\t\t"+DataCleanManager.getCacheSize(datafile));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Button btclear = ((Button)viewThree.findViewById(R.id.btClear));
        btclear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataClearDialog();
            }
        });
        tv_three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File datafile =new File(chcheFilePath);
                try {
                    tv_cashe.setText("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t当前缓存：\t\t"+DataCleanManager.getCacheSize(datafile));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        /*-----------------设置按钮事件-----------------*/


        newsList = new ArrayList<>();
        musicListView = (ListView)viewTwo.findViewById(R.id.newsListView);
        musicListView.setAdapter(myAdapter);//添加adapter
        //设置listview点击事件
        musicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                newsMessage news = newsList.get(i);
                Log.i(TAG, "onCreate1: "+news.getNewsTitle()+"   "+news.getNewsPath()+'\n');
                Intent intent = new Intent(MainActivity.this,MusicNewsActivity.class);
                intent.putExtra("webPath",news.getNewsPath());
                startActivity(intent);
            }
        });
        listViews.add(viewOne);
        listViews.add(viewTwo);
        listViews.add(viewThree);
        vpager_four.setAdapter(new TestPagerAdapter(listViews));
        vpager_four.setCurrentItem(0);          //设置ViewPager当前页，从0开始算
        tv_one.setOnClickListener(this);
        tv_two.setOnClickListener(this);
        tv_three.setOnClickListener(this);
        vpager_four.addOnPageChangeListener(this);

        /*--------------------------------------------------*/
        totalTV = viewOne.findViewById(R.id.music_total_time);
        currentTV = viewOne.findViewById(R.id.music_current_time);
        seekBar = ((SeekBar) viewOne.findViewById(R.id.music_seekbar));
        seekBar.setOnSeekBarChangeListener(new MySeekBar());

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
            return;
        }
        //判断是否是AndroidN以及更高的版本 N=24
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
        list = new ArrayList<String>();   //音乐列表
        File sdpath = new File(Environment.getExternalStorageDirectory().getPath(),"Download/");//获得手机SD卡路径
        lists = new ArrayList<String>();   //歌名
        songFiles = sdpath.listFiles(new musicFilter(".mp3"));
        for (File file : songFiles) {
            list.add(file.getAbsolutePath());   //获取文件的绝对路径
            String filestr = file.getAbsolutePath();
            lists.add(filestr.substring(filestr.lastIndexOf('/')+1,filestr.length()));
        }
        Log.i(TAG, "size : "+lists.get(0));
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_single_choice,
                lists);
        ListView li = (ListView)(viewOne.findViewById(R.id.listView1));
        li.setAdapter(adapter);
        li.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        li.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                song_path = ((TextView) view).getText().toString();
                TextView song_name = ((TextView)viewOne.findViewById(R.id.textView));
                song_name.setText( "正在播放："+song_path);
                currentposition = position;
                changeMusic(currentposition);
                try {
                    mp.reset();    //重置
                    mp.setDataSource(song_path);
                    mp.prepare();     //准备
                    mp.start(); //播放
                    seekBar.setMax(mp.getDuration());
                    isStop = false;
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (!isSeekBarChanging) {
                                seekBar.setProgress(mp.getCurrentPosition());
                            }
                        }
                    }, 0, 50);
                } catch (Exception e) {
                }
            }
        });

        //暂停和播放
        btnpause = (ImageButton) viewOne.findViewById(R.id.btn_pause);
        btnpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (song_path.isEmpty())
                    Toast.makeText(getApplicationContext(), "先选收歌曲先听听", Toast.LENGTH_SHORT).show();
                if (mp.isPlaying()) {
                    mp.pause();  //暂停
                    isStop = true;
                    btnpause.setImageResource(android.R.drawable.ic_media_play);
                } else if (!song_path.isEmpty()) {
                    mp.start();   //继续播放
                    isStop = false;
                    btnpause.setImageResource(android.R.drawable.ic_media_pause);
                }
            }
        });
  //上一曲和下一曲
        final ImageButton previous = (ImageButton) viewOne.findViewById(R.id.previous);
        previous.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                changeMusic(--currentposition);
            }
        });
        final ImageButton next = (ImageButton) viewOne.findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeMusic(++currentposition);
            }
        });
           /*    -------------------------------------*/
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_one:
                vpager_four.setCurrentItem(0);
                break;
            case R.id.tv_two:
                vpager_four.setCurrentItem(1);
                break;
            case R.id.tv_three:
                vpager_four.setCurrentItem(2);
                break;
        }
    }

    @Override
    public void onPageSelected(int index) {
        Animation animation = null;
        switch (index) {
            case 0:
                if (currIndex == 1) {
                    animation = new TranslateAnimation(one, 0, 0, 0);
                } else if (currIndex == 2) {
                    animation = new TranslateAnimation(two, 0, 0, 0);
                }
                break;
            case 1:
                if (currIndex == 0) {
                    animation = new TranslateAnimation(offset, one, 0, 0);
                } else if (currIndex == 2) {
                    animation = new TranslateAnimation(two, one, 0, 0);
                }
                break;
            case 2:
                if (currIndex == 0) {
                    animation = new TranslateAnimation(offset, two, 0, 0);
                } else if (currIndex == 1) {
                    animation = new TranslateAnimation(one, two, 0, 0);
                }
                break;
        }
        currIndex = index;
        animation.setFillAfter(true);// true表示图片停在动画结束位置
        animation.setDuration(300); //设置动画时间为300毫秒
        img_cursor.startAnimation(animation);//开始动画
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    private void changeMusic(int position) {
        TextView song_name = ((TextView)viewOne.findViewById(R.id.textView));
        ListView listview = (ListView)(viewOne.findViewById(R.id.listView1));
        isStop = false;
        btnpause.setImageResource(android.R.drawable.ic_media_pause);
        if (position < 0) {
            currentposition = position = list.size() - 1;
        } else if (position > list.size() - 1) {
            currentposition = position = 0;
        }
        song_path = songFiles[position].getAbsolutePath();
        listview.setItemChecked(currentposition,true);
        song_name.setText( "正在播放："+lists.get(currentposition));
        try {
            // 切歌之前先重置，释放掉之前的资源
            mp.reset();
            // 设置播放源
            mp.setDataSource(song_path);
            // 开始播放前的准备工作，加载多媒体资源，获取相关信息
            mp.prepare();

            // 开始播放
            mp.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        seekBar.setProgress(0);//将进度条初始化
        seekBar.setMax(mp.getDuration());//设置进度条最大值为歌曲总时间
        totalTV.setText(formatTime(mp.getDuration()));//显示歌曲总时长

        updateProgress();//更新进度条
    }
    private void updateProgress() {
        // 使用Handler每间隔1s发送一次空消息，通知进度条更新
        Message msg = Message.obtain();// 获取一个现成的消息
        // 使用MediaPlayer获取当前播放时间除以总时间的进度
        int progress = mp.getCurrentPosition();
        msg.arg1 = progress;
        mHandler.sendMessageDelayed(msg, INTERNAL_TIME);
    }


    public class MySeekBar implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
        }

        /*滚动时,应当暂停后台定时器*/
        public void onStartTrackingTouch(SeekBar seekBar) {
            isSeekBarChanging = true;
        }

        /*滑动结束后，重新设置值*/
        public void onStopTrackingTouch(SeekBar seekBar) {
            isSeekBarChanging = false;
            mp.seekTo(seekBar.getProgress());
        }

    }
    protected void onDestroy() {
        super.onDestroy();
        if (mp != null) {
            mp.stop();
            mp.release();
        }
        Toast.makeText(getApplicationContext(), "退出啦", Toast.LENGTH_SHORT).show();
    }

    public class  MusicAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        @Override
        public int getCount() {
            return newsList.size();
        }
        @Override
        public Object getItem(int position) {
            return newsList.get(position);
        }
        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater mInflater = getLayoutInflater();
            // View view= mInflater.inflate(R.layout.news_item, null, false);
            View view = View.inflate(MainActivity.this,R.layout.news_item,null);
            String Message = "\n"+ newsList.get(position).getNewsTitle() +"\n\n"+newsList.get(position).getNewsSource()+"     "+newsList.get(position).getNewsTime()+"\n";
            ((TextView)view.findViewById(R.id.newsTitle)).setText(Message);
            return view;
        }
    }
    private void exitDialog(){   //退出程序的方法
           Dialog dialog = new AlertDialog.Builder(MainActivity.this)
              .setTitle("程序退出？")   // 创建标题
              .setMessage("您确定要退出吗？")       //表示对话框的内容
              .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                     public void onClick(DialogInterface dialog, int which) {
                         MainActivity.this.finish(); //操作结束
                       }

             }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {
                       }
                 }).create();   //创建对话框
     dialog.show();   //显示对话框
   }
    private void dataClearDialog(){   //清除缓存的方法
        Dialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("清除缓存？")   // 创建标题
                .setMessage("您确定要清除缓存吗？")       //表示对话框的内容
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //DataCleanManager.cleanInternalCache(getApplicationContext());
                        File Datafile =new File("/sdcard/"+chcheFilePath);
                        try {
                            if (Datafile != null && Datafile.exists() && Datafile.isDirectory()) {
                                for (File item : Datafile.listFiles()) {
                                   // Log.i(TAG, "zzzz: "+item);
                                    item.delete();
                                }
                            }
                            tv_cashe.setText("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t当前缓存： \t\t"+DataCleanManager.getCacheSize(Datafile));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create();   //创建对话框
        dialog.show();   //显示对话框
    }

}