package com.example.pc.sanjicache;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mIv;
    String url = "http://img0.bdstatic.com/img/image/shouye/xiaoxiao/%E5%AE%A0%E7%89%A983.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        CastielImageLoader.getInstance().loadImage(url,mIv,this);

        //http://img0.bdstatic.com/img/image/shouye/xiaoxiao/%E5%AE%A0%E7%89%A983.jpg
        //加强版三级缓存
        /*ImageHelper imageHelper = new ImageHelper(this);
        imageHelper.display(mIv,url);*/

    }

    private void initView() {
        mIv = (ImageView) findViewById(R.id.iv);
        mIv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv:
                CastielImageLoader.getInstance().loadImage(url,mIv,this);
                break;
        }
    }
}
