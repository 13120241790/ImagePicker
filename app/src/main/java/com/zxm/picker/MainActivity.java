package com.zxm.picker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.github.picker.PictureSelectorActivity;

import static android.R.id.list;
import static android.os.Build.VERSION_CODES.N;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void startPicker(View view){
        startActivityForResult(new Intent(this,PictureSelectorActivity.class),REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && resultCode == RESULT_OK && REQUEST_CODE == requestCode) {
//            data.putExtra("sendOrigin", mSendOrigin);
//            data.putExtra(Intent.EXTRA_RETURN_RESULT, list);
        }
    }
}
