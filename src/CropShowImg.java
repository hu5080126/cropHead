package eebochina.com.testtechniques;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class CropShowImg extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_show_img);
        ImageView imageView = (ImageView) findViewById(R.id.crop_show_img);
        String filePath = getIntent().getStringExtra("path");
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        imageView.setImageBitmap(bitmap);
    }

    public static void startThis(Context context, String path) {
        Intent intent = new Intent(context, CropShowImg.class);
        intent.putExtra("path", path);
        context.startActivity(intent);
    }


}
