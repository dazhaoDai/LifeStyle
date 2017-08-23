#### Android手机拍照、剪裁，并非那么简单
简书地址：[我的简书--T9的第三个三角](http://www.jianshu.com/u/46cb5df3d852）

- **前言**
项目中，基本都有用户自定义头像或自定义背景的功能，实现方法一般都是调用系统相机--拍照，或者系统相册--选择照片，然后进行剪裁，最终设为头像或背景。
  而在Android6.0之后，需要动态获取权限，而且Android7.0之后，无法直接根据拍照返回的URI拿到图片，这是因为从安卓7.0开始，直接使用本地真实路径被认为是不安全的，会抛出FileUriExposedExCeption异常，本文就是基于这个功能去针对Android7.0进行操作。
废话不多说，先把基本的页面建立，先来布局。

- **布局**

```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/transparent_black_80"
    android:orientation="vertical">

    <RelativeLayout
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="50dp"
        android:background="@color/black"
        android:visibility="visible">

        <TextView
            android:id="@+id/tv_camera"
            android:layout_width="wrap_content"
            android:layout_height="match_parent"
            android:gravity="center"
            android:padding="10dp"
            android:text="@string/camera"
            android:textColor="@color/white"
            android:textSize="18sp" />

        <TextView
            android:id="@+id/tv_picture"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerInParent="true"
            android:gravity="center"
            android:padding="10dp"
            android:text="@string/picture"
            android:textColor="@color/white"
            android:textSize="18sp" />

        <TextView
            android:id="@+id/tv_done"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_alignParentRight="true"
            android:gravity="center"
            android:padding="10dp"
            android:text="@string/done"
            android:textColor="@color/white"
            android:textSize="18sp" />
    </RelativeLayout>

    <RelativeLayout
        android:id="@+id/rl_camera"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:background="@color/black">


        <com.isseiaoki.simplecropview.CropImageView xmlns:custom="http://schemas.android.com/apk/res-auto"
            android:id="@+id/iv_wallpaper"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:layout_weight="1"
            custom:scv_crop_mode="fit_image"
            custom:scv_frame_color="@color/white"
            custom:scv_frame_stroke_weight="3dp"
            custom:scv_guide_color="@color/white"
            custom:scv_guide_show_mode="show_on_touch"
            custom:scv_guide_stroke_weight="1dp"
            custom:scv_handle_color="@color/white"
            custom:scv_handle_show_mode="show_always"
            custom:scv_handle_size="8dp"
            custom:scv_min_frame_size="100dp"
            custom:scv_overlay_color="@color/down_fragment_alpha" />
    </RelativeLayout>

    <LinearLayout
        android:id="@+id/ll_contral"
        android:layout_width="match_parent"
        android:layout_height="90dp"
        android:background="@color/white"
        android:orientation="horizontal"
        android:paddingBottom="30dp"
        android:paddingLeft="30dp"
        android:paddingRight="30dp"
        android:paddingTop="20dp"
        android:visibility="visible">

        <TextView
            android:id="@+id/tv_reset"
            android:layout_width="0dp"
            android:layout_height="40dp"
            android:layout_marginRight="14dp"
            android:layout_weight="2"
            android:background="@drawable/reset_selector"
            android:gravity="center"
            android:text="Reset"
            android:textColor="@color/white" />

        <ImageView
            android:id="@+id/left_rotate"
            android:layout_width="0dp"
            android:layout_height="40dp"
            android:layout_marginRight="14dp"
            android:layout_weight="1"
            android:background="@drawable/wallpaper_selector"
            android:src="@drawable/rotate_left" />

        <ImageView
            android:id="@+id/right_rotate"
            android:layout_width="0dp"
            android:layout_height="40dp"
            android:layout_marginRight="14dp"
            android:layout_weight="1"
            android:background="@drawable/wallpaper_selector"
            android:src="@drawable/rotate_right" />

        <ImageView
            android:id="@+id/up_reversal"
            android:layout_width="0dp"
            android:layout_height="40dp"
            android:layout_marginRight="14dp"
            android:layout_weight="1"
            android:background="@drawable/wallpaper_selector"
            android:src="@drawable/reversal_up" />

        <ImageView
            android:id="@+id/left_reversal"
            android:layout_width="0dp"
            android:layout_height="40dp"
            android:layout_weight="1"
            android:background="@drawable/wallpaper_selector"
            android:src="@drawable/reversal_left" />
    </LinearLayout>

</LinearLayout>```

![布局](http://upload-images.jianshu.io/upload_images/2789715-22589dd6fff685d0?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

布局很简单，点击Camera、Picture、Done，分别调用手机拍照、调用系统相册照片、完成操作。

- **调用相机拍照**

 Android6.0之前，调用系统拍照，只需要在AndroidManifest.xml声明
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
 权限，而在6.0以后，不仅需要声明，更需要运行时申请权限，先来调用相机拍照。 

1.初始化控件，使用ButterKnife，这个简直傻瓜式的完成
```
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
    private Uri localUri = null;```

2.点击拍照：

	```
	tvCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPremission(); //检查权限
            }
        });
        
        
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



	 @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {  //申请权限的返回值
            case CAMERA_REQUEST_CODE:
                int length = grantResults.length;
                final boolean isGranted = length >= 1 && PackageManager.PERMISSION_GRANTED == grantResults[length - 1];
                if (isGranted) {  //如果用户赋予权限，则调用相机
                    openCamera();
	                }else{ //未赋予权限，则做出对应提示
                
	                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


	private void openCamera() {  //调用相机拍照
        Intent intent = new Intent();
        File file = new FileStorage().createIconFile(); /工具类，稍后会给出
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
```

通过FileProvider创建一个content类型的Uri，不仅是通过FileProvider.getUriForFile(SkinActivity.this, "com.ddz.demo", file);而且在AndroidManifest.xml中进行配置，android:authorities要和FileProvider中一样，同时在xml中配置路径

```
    <provider
            android:name="android.support.v4.content.FileProvider"
            android:authorities="com.ddz.demo"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>
```
```
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-path
        name="my_images"
        path="images/" />
</paths>```
- **系统相册选择照片**
	系统相册选择照片比较简单，直接调用对应方法：
	```
	   Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICTURE);```

- **剪裁照片**
思路：拿到返回路径之后，，获取图像，并压缩，返回压缩图像进行剪裁
```
 @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CAPTURE:
                    if (null != imageUri) {
                        localUri = imageUri;
                        setBitmap(localUri);//获取拍照并自定义保存地址的照片
                    }
                    break;
                case REQUEST_PICTURE:
			       if(data.getData()){
                                localUri = data.getData();
                                 setBitmap(localUri);  //获取相册选择的照片
			     
			       }
                    break;
            }
```           

 -   *** 获取图片，并剪裁，使用的是一个强大的第三方剪裁库 **
  [SimpleCropView](https://github.com/IsseiAoki/SimpleCropView)，具体用法可以看READ.ME,
   build.gradle中添加依赖：
```
repositories {
    jcenter()
}
dependencies {
    compile 'com.isseiaoki:simplecropview:1.1.4'
}
```
```AndroidManifest.xml``` 添加必要的权限
```
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```
要剪裁的图片布局，使用类似于ImageView,其中
![参数](http://upload-images.jianshu.io/upload_images/2789715-2b932fefe6cf34f2?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
scv_frame_color---剪裁框外边框的颜色
scv_guide_color---剪裁框内边线的颜色
scv_handle_color---剪裁框四个角的圆点颜色
scv_overlay_color---覆盖图片的颜色
而且，scv_guide和scv_hanle有三种显示模式，SHOW_ALWAYS(default)--默认一直显示, NOT_SHOW--不显示, SHOW_ON_TOUCH--触碰显示。
可以组合使用
![组合使用](http://upload-images.jianshu.io/upload_images/2789715-9e958ebd24a04ebd?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
其他方法
获取剪裁之后的Bitmap
```getCroppedBitmap()(Sync method)```

开始剪裁，
```startCrop(Uri saveUri, CropCallback cropCallback, SaveCallback saveCallback)(Async Method, RECOMMENDED)```

压缩格式
有三种格式可选, PNG(默认),JPEG, 和WEBP.
```setCompressFormat(Bitmap.CompressFormat.JPEG);```

压缩 质量
可以设置压缩质量范围 0~100(默认100)
```setCompressQuality(90);```

固定输出大小
```
setOutputWidth(100); // If cropped image size is 400x200, output size is 100x50
setOutputHeight(100); // If cropped image size is 400x200, output size is 200x100
```

设置剪裁比例
多种比例可选
```
FIT_IMAGE, RATIO_4_3, RATIO_3_4, SQUARE(default), RATIO_16_9, RATIO_9_16, FREE, CUSTOM, CIRCLE, CIRCLE_SQUARE
cropImageView.setCropMode(CropImageView.CropMode.RATIO_16_9);
```
旋转图像
```cropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D); // 左旋转90度
cropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_M90D); // 右旋转90度```

是否开启旋转动画
```
setAnimationEnabled(true);```

设置旋转动画时长
```
setAnimationDuration(200);```

设置旋转动画加速器
```setInterpolator(new AccelerateDecelerateInterpolator());```

布局中使用CropImageView
```
   <com.isseiaoki.simplecropview.CropImageView xmlns:custom="http://schemas.android.com/apk/res-auto"
            android:id="@+id/iv_wallpaper"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:layout_weight="1"
            custom:scv_crop_mode="fit_image"
            custom:scv_frame_color="@color/white"
            custom:scv_frame_stroke_weight="3dp"
            custom:scv_guide_color="@color/white"
            custom:scv_guide_show_mode="show_on_touch"
            custom:scv_guide_stroke_weight="1dp"
            custom:scv_handle_color="@color/white"
            custom:scv_handle_show_mode="show_always"
            custom:scv_handle_size="8dp"
            custom:scv_min_frame_size="100dp"
            custom:scv_overlay_color="@color/down_fragment_alpha" />
```
```	   
 /**
	     * 根据Uri获取图像并压缩图像
	     *
	     * @param uri
	     */
    private void setBitmap(Uri uri) {
        if (uri != null) { //使用Rxjava进行耗时操作并切换线程
            Observable.create((rx.Observable.OnSubscribe<Bitmap>) subscriber -> {
                subscriber.onNext(ImageUtils.getCompressBitmap(LockCameraActivity.this, uri));  //针对拍照和系统相册获取图片遇到的图片过大导致OOM问题，剪裁之前先对图像进行压缩
                subscriber.onCompleted();
            }).subscribeOn(Schedulers.io()) //分线程进行
                    .observeOn(AndroidSchedulers.mainThread()) //主线程设置bitmap
                    .subscribe(bitmap -> {
                        ivWallpaper.setCropMode(CropImageView.CropMode.RATIO_9_16);比例是9：16
                        if (bitmap != null) ivWallpaper.setImageBitmap(bitmap);
                    });
        } else {
            backLock();
        }
    }
```
根据上面操作，![功能](http://upload-images.jianshu.io/upload_images/2789715-77f5b74ac7fe6d78?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
已经完成系统拍照或者获取系统相册图片并压缩步骤，显示了剪裁页面，此时根据需求，有重置、左旋转、右旋转、上下翻转、左右翻转，
设置五个点击事件
```
 @OnClick({R.id.tv_done, R.id.tv_reset, R.id.left_rotate, R.id.right_rotate, R.id.up_reversal, R.id.left_reversal})
    public void onClick(View view) {
        switch (view.getId()) {
                break;
            case R.id.tv_reset:
                setBitmap(localUri); //重置，重新获取图像压缩并显示在ivWallpaper上
                break;
            case R.id.left_rotate:
                ivWallpaper.rotateImage(CropImageView.RotateDegrees.ROTATE_M90D); //左旋转
                break;
            case R.id.right_rotate:
                ivWallpaper.rotateImage(CropImageView.RotateDegrees.ROTATE_90D);//右旋转
                break;
            case R.id.up_reversal:
                takeReversal(REVERSAL_UP);//上下翻转
                break;
            case R.id.left_reversal:
                takeReversal(REVERSAL_LEFT);//左右翻转
                break;
            case R.id.tv_done:

                break;                
        }
    }


private void takeReversal(int reversal) {
        Observable.create((Observable.OnSubscribe<Bitmap>) subscriber -> {
            subscriber.onNext(getRotateBitmap(reversal)); //翻转图像
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
```
最后，如果要得到最后的头像，直接调用```ivWallpaper.getCroppedBitmap()``` 得到剪裁后的图像。

粗略介绍了Android7.0调用相机拍照的坑，已经6.0开始的动态权限申请，已经剪裁、旋转、翻转图像等一系列操作，demo只是个参考，大家可以根据自己需求，进一步封装。
最后附上demo代码：
