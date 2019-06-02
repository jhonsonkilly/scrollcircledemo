package com.zjw.scrollcircleprogress;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SeekBarView.ClickCallBack {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SeekBarView mSeekBarView = findViewById(R.id.seek_bar);
        mSeekBarView.setOnclickCallBackListener(this);
    }

    @Override
    public void getSelectedItem(int position) {
        Toast.makeText(this, "选中了第" + position + "个", Toast.LENGTH_SHORT).show();
    }
}
