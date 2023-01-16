package com.wallpaper.gallery.gallery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

public class MenuActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "MenuActivity";
    private Button mSetWallpaper;
    private Button mSetThumbnail;
    private EditText mName;
    private RadioButton mLocalPublic;
    private RadioButton mGuest;
    private RadioButton mUser;

    private final int CROP_WALLPAPER = 0; //叫起裁剪wallpaper的requestcode
    private final int CROP_THUMBNAIL = 1; //叫起裁剪thumbnail的requestcode

    private int userType = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        initView();
    }

    private void initView() {
        mSetWallpaper = findViewById(R.id.set_wallpaper);
        mSetWallpaper.setOnClickListener(this);
        mSetThumbnail = findViewById(R.id.set_thumbnail);
        mSetThumbnail.setOnClickListener(this);
        mName = findViewById(R.id.name);
        mLocalPublic = findViewById(R.id.local_public);
        mGuest = findViewById(R.id.guest);
        mUser = findViewById(R.id.user);
        mLocalPublic = findViewById(R.id.local_public);
        mLocalPublic.setOnClickListener(this);
        mGuest = findViewById(R.id.guest);
        mGuest.setOnClickListener(this);
        mUser = findViewById(R.id.user);
        mUser.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.set_wallpaper:
                Intent wallpaperIntent = new Intent(MenuActivity.this, MainActivity.class);
                startActivityForResult(wallpaperIntent,CROP_WALLPAPER);
                break;
            case R.id.set_thumbnail:
                Intent thumbnailIntent = new Intent(MenuActivity.this, ThumbnailActivity.class);
                thumbnailIntent.putExtra("userType", userType);//0 local_public / 1 guest / 2 user
                thumbnailIntent.putExtra("userName", mName.getText()+"");
                startActivityForResult(thumbnailIntent,CROP_THUMBNAIL);
                break;
            case R.id.local_public:
                userType = 0;
                break;
            case R.id.guest:
                userType = 1;
                break;
            case R.id.user:
                userType = 2;
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "requestCode= " + requestCode + ", resultCode= " + resultCode);
        if (requestCode == CROP_WALLPAPER){
            if (resultCode == RESULT_OK){
                Log.d(TAG, "onActivityResult: crop wallpaper ok");
            }else if (resultCode == RESULT_CANCELED){
                Log.d(TAG, "onActivityResult: crop wallpaper cancel");
            }
        }
        if (requestCode == CROP_THUMBNAIL){
            if (resultCode == RESULT_OK){
                Log.d(TAG, "onActivityResult: crop thumbnail ok");
            }else if (resultCode == RESULT_CANCELED){
                Log.d(TAG, "onActivityResult: crop thumbnail cancel");
            }
        }
    }
}
