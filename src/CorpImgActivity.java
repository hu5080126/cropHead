package eebochina.com.testtechniques;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CorpImgActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mCropBtn;
    private View mCropView;
    private ImageView mCorpImg;
    private RelativeLayout mCropLayout;
    private Bitmap mCropBitmap;
    private int top, left;
    private float downX, downY;
    private final int ZOOM = 1, DRAG = 0;
    private float oldSpacing = 1f;
    private int state;


    /**
     * 记录缩放时两指中间点坐标
     */
    private PointF mid = new PointF();
    private boolean isMain, isPointer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corp_img);

        mCropBtn = (TextView) findViewById(R.id.corp_handle_btn);
        mCropView = findViewById(R.id.crop_handler_select);
        mCorpImg = (ImageView) findViewById(R.id.crop_handler_img);
        mCropLayout = (RelativeLayout) findViewById(R.id.crop_handler_layout);

        //要裁剪的图片
        mCropBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.crop);
        mCorpImg.setImageBitmap(mCropBitmap);

        mCropBtn.setOnClickListener(this);

        mCorpImg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction() & motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_POINTER_DOWN:
                        //次要点被点击
                        oldSpacing = spacing(motionEvent);
                        if (oldSpacing > 10f) {
                            isPointer = false;
                            isMain = false;
                            state = ZOOM;
                            midPoint(mid, motionEvent);
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        //次要点松开
                        Log.d("CropHandlerActivity", "ACTION_POINTER_UP");
                        isPointer = true;
                        if (isPointer && isMain) {
                            isPointer = false;
                            isMain = false;
                            state = DRAG;
                            oldSpacing = 1f;
                        }
                        break;
                    case MotionEvent.ACTION_DOWN:
                        downX = motionEvent.getX();
                        downY = motionEvent.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Matrix matrix = mCorpImg.getImageMatrix();
                        if (state == DRAG) {
                            //拖拽
                            matrix.postTranslate(motionEvent.getX() - downX, motionEvent.getY() - downY);
                            downX = motionEvent.getX();
                            downY = motionEvent.getY();
                        } else if (state == ZOOM && !isPointer && !isMain) {
                            //放大缩小
                            float newSpacing = spacing(motionEvent);
                            float scale = newSpacing / oldSpacing;
                            matrix.postScale(scale, scale, mid.x, mid.y);
                            oldSpacing = newSpacing;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        isMain = true;
                        if (isPointer && isMain) {
                            isPointer = false;
                            isMain = false;
                            state = DRAG;
                            oldSpacing = 1f;
                        }
                        break;
                }
                mCorpImg.invalidate();
                return true;
            }
        });
    }


    /**
     * 多点触控时，计算最先放下的两指距离
     *
     * @param event
     * @return
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 多点触控时，计算最先放下的两指中心坐标
     *
     * @param point
     * @param event
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            int layoutWidth = mCropLayout.getWidth();
            int layoutHeight = mCropLayout.getHeight();
            int imgWidth = mCropBitmap.getWidth();
            int imgHeight = mCropBitmap.getHeight();
            int selectWidth = mCropView.getWidth();
            int selectHeight = mCropView.getHeight();

            //缩放比例
            float scaleNum;

            //将要裁剪的图片长宽高做对比， 将较小的一方做等比缩放成裁剪框大小
            if (imgWidth < imgHeight) {
                scaleNum = (selectWidth * 1.0f) / (imgWidth * 1.0f);
                imgHeight = (int) (scaleNum * imgHeight);
                imgWidth = selectWidth;
            } else {
                scaleNum = (selectHeight * 1.0f) / (imgHeight * 1.0f);
                imgWidth = (int) (scaleNum * imgWidth);
                imgHeight = selectHeight;
            }


            Matrix matrix = new Matrix();
            matrix.postScale(scaleNum, scaleNum);
            //平移距离
            matrix.postTranslate((layoutWidth - imgWidth) / 2, (layoutHeight - imgHeight) / 2);

            top = (layoutHeight - selectHeight) / 2;
            left = (layoutWidth - selectWidth) / 2;

            //设置缩放类型为 矩阵
            mCorpImg.setScaleType(ImageView.ScaleType.MATRIX);
            mCorpImg.setImageMatrix(matrix);
            mCorpImg.setImageBitmap(mCropBitmap);
        }
    }

    /**
     * 获取裁剪框内截图
     *
     * @return
     */
    private Bitmap getBitmap() {
        // 获取截屏
        mCropLayout.setDrawingCacheEnabled(true);
        mCropLayout.buildDrawingCache();
        int borderWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
        Bitmap finalBitmap = Bitmap.createBitmap(mCropLayout.getDrawingCache(),
                left + borderWidth, top + borderWidth, mCropView.getWidth() - 2 * borderWidth,
                mCropView.getHeight() - 2 * borderWidth);

        // 释放资源
        mCropLayout.destroyDrawingCache();
        return finalBitmap;
    }

    /**
     * 获取sdcard路径
     * @return
     */
    private String getSDCardPath() {
        File sdcardDir = null;
        // 判断SDCard是否存在
        boolean sdcardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
        if (sdcardExist) {
            sdcardDir = Environment.getExternalStorageDirectory();
        }
        return sdcardDir.toString();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.corp_handle_btn:
                Bitmap bitmap = getBitmap();
                FileOutputStream fos = null;
                String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(new Date());
                String fileName = "cropTemp_" + timeStamp + ".jpeg";
                try {
                    //获取sdcard的根目录
                    String sdPath = getSDCardPath();

                    //创建程序自己创建的文件夹
                    File tempFile = new File(sdPath);
                    if (!tempFile.exists()) {
                        tempFile.mkdirs();
                    }
                    //创建图片文件
                    File file = new File(sdPath + File.separator + fileName);
                    if (!file.exists()) {
                        file.createNewFile();
                    }

                    fos = new FileOutputStream(file);
                    if (fos != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                        fos.close();
                    }
                    CropShowImg.startThis(this, file.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
