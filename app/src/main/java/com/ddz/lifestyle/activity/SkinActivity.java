package com.ddz.lifestyle.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ddz.lifestyle.R;
import com.ddz.lifestyle.utils.FileStorage;
import com.ddz.lifestyle.utils.FileUtils;
import com.ddz.lifestyle.utils.ImageUtils;
import com.ddz.lifestyle.utils.LifeStyle;
import com.isseiaoki.simplecropview.CropImageView;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * Author : ddz
 * Creation time   : 2016/11/25 10:06
 * Fix time   :  2016/11/25 10:06
 */

public class SkinActivity extends BaseActivity {


    @BindView(R.id.tv_camera)
    TextView tvCamera;
    @BindView(R.id.tv_picture)
    TextView tvPicture;
    @BindView(R.id.tv_done)
    TextView tvDone;
    @BindView(R.id.toolbar)
    RelativeLayout toolbar;
    @BindView(R.id.iv_wallpaper)
    CropImageView ivWallpaper;
    @BindView(R.id.rl_camera)
    RelativeLayout rlCamera;
    @BindView(R.id.tv_reset)
    TextView tvReset;
    @BindView(R.id.left_rotate)
    ImageView leftRotate;
    @BindView(R.id.right_rotate)
    ImageView rightRotate;
    @BindView(R.id.up_reversal)
    ImageView upReversal;
    @BindView(R.id.left_reversal)
    ImageView leftReversal;
    @BindView(R.id.ll_contral)
    LinearLayout llContral;

    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int REQUEST_CAPTURE = 2;
    private static final int REQUEST_PICTURE = 5;
    private static final int REVERSAL_LEFT = 3;
    private static final int REVERSAL_UP = 4;
    private Uri imageUri;
    private Uri localUri = null;

    @Override
    public void initData(Bundle SavedInstanceState) {
        tvCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPremission(); //检查权限
            }
        });
        tvPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPicture();
            }
        });
    }

    private void checkPremission() {
        final String permission = Manifest.permission.CAMERA;  //相机权限
        final String permission1 = Manifest.permission.WRITE_EXTERNAL_STORAGE; //写入数据权限
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, permission1) != PackageManager.PERMISSION_GRANTED) {  //先判断是否被赋予权限，没有则申请权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {  //给出权限申请说明
                ActivityCompat.requestPermissions(SkinActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, CAMERA_REQUEST_CODE);
            } else { //直接申请权限
                ActivityCompat.requestPermissions(SkinActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE); //申请权限，可同时申请多个权限，并根据用户是否赋予权限进行判断
            }
        } else {  //赋予过权限，则直接调用相机拍照
            openCamera();
        }
    }

    private void openCamera() {  //调用相机拍照
        Intent intent = new Intent();
        File file = new FileStorage().createIconFile(); //工具类，稍后会给出
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {  //针对Android7.0，需要通过FileProvider封装过的路径，提供给外部调用
            imageUri = FileProvider.getUriForFile(SkinActivity.this, "com.ddz.demo", file);//通过FileProvider创建一个content类型的Uri，进行封装
        } else { //7.0以下，如果直接拿到相机返回的intent值，拿到的则是拍照的原图大小，很容易发生OOM，所以我们同样将返回的地址，保存到指定路径，返回到Activity时，去指定路径获取，压缩图片
            try {
                imageUri = Uri.fromFile(ImageUtils.createFile(FileUtils.getInst().getPhotoPathForLockWallPaper(), true));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);//设置Action为拍照
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);//将拍取的照片保存到指定URI
        startActivityForResult(intent, REQUEST_CAPTURE);//启动拍照
    }


    @Override
    public void setListener() {

    }

    private void getPicture() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICTURE);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_skin;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {  //申请权限的返回值
            case CAMERA_REQUEST_CODE:
                int length = grantResults.length;
                final boolean isGranted = length >= 1 && PackageManager.PERMISSION_GRANTED == grantResults[length - 1];
                if (isGranted) {  //如果用户赋予权限，则调用相机
                    openCamera();
                } else { //未赋予权限，则做出对应提示

                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CAPTURE:
                    if (null != imageUri) {
                        localUri = imageUri;
                        setBitmap(localUri);
                    }
                    break;
                case REQUEST_PICTURE:
                    localUri = data.getData();
                    setBitmap(localUri);
                    break;
            }
        }
    }


    /**
     * 根据Uri拿到当前图像
     *
     * @param uri
     */
    private void setBitmap(Uri uri) {
        if (uri != null) {
            Observable.create((rx.Observable.OnSubscribe<Bitmap>) subscriber -> {
                subscriber.onNext(ImageUtils.getCompressBitmap(LifeStyle.getContext(), uri));
                subscriber.onCompleted();
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bitmap -> {
                        ivWallpaper.setCropMode(CropImageView.CropMode.FREE);
                        if (bitmap != null) ivWallpaper.setImageBitmap(bitmap);
                    });
        }
    }


    @OnClick({R.id.tv_done, R.id.tv_reset, R.id.left_rotate, R.id.right_rotate, R.id.up_reversal, R.id.left_reversal})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_reset:
                setBitmap(localUri);
                break;
            case R.id.left_rotate:
                ivWallpaper.rotateImage(CropImageView.RotateDegrees.ROTATE_M90D);
                break;
            case R.id.right_rotate:
                ivWallpaper.rotateImage(CropImageView.RotateDegrees.ROTATE_90D);
                break;
            case R.id.up_reversal:
                takeReversal(REVERSAL_UP);
                break;
            case R.id.left_reversal:
                takeReversal(REVERSAL_LEFT);
                break;
        }
    }

    private void takeReversal(int reversal) {
        Observable.create((Observable.OnSubscribe<Bitmap>) subscriber -> {
            subscriber.onNext(getRotateBitmap(reversal));
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(bitmap -> {
            ivWallpaper.setImageBitmap(bitmap);
        });
    }

    /**
     * 翻转图像
     *
     * @param reversal
     * @return
     */
    private Bitmap getRotateBitmap(int reversal) {
        Bitmap bitmap = ivWallpaper.getImageBitmap();
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        if (reversal == REVERSAL_UP) { //上下翻转
            matrix.postScale(1, -1);  //X轴不变，Y轴翻转
        } else if (reversal == REVERSAL_LEFT) { //左右翻转
            matrix.postScale(-1, 1); //Y轴不变，X轴翻转
        }
        Bitmap bitmap1 = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        if (bitmap1 != null) return bitmap1;
        return null;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ivWallpaper != null) {
            ivWallpaper.getImageBitmap().recycle();
        }
    }
}
