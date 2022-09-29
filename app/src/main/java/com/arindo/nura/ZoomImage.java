package com.arindo.nura;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class ZoomImage extends AppCompatActivity {
    private String filePath, filename;
    private File file = null;
	private ImageView actcamera, actgallery;
    private Uri mImageCaptureUri;
    private Bitmap bitmap;
    private int mediaFoto;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zoom_image);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        this.setTitle("Picture");
        
        Intent main=getIntent();
        filePath = main.getExtras().getString("filePath");

        actcamera = (ImageView)findViewById(R.id.actcamera);
        actgallery = (ImageView)findViewById(R.id.actgallery);
        
        try {
            ShowImageZoom(filePath);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

        actcamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaFoto = 1;
                takeFoto(mediaFoto);
            }
        });

        actgallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaFoto = 0;
                takeFoto(mediaFoto);
            }
        });
    }

    private void ShowImageZoom(String filePath){
        TouchImageView img = new TouchImageView(this);
        img.setImageURI(Uri.parse(filePath));
        img.setMaxZoom(4f);

        FrameLayout layout = (FrameLayout)findViewById(R.id.layoutimg);
        layout.addView(img);
    }

    private void takeFoto(int param) {
        SetAccount oprAccount = new SetAccount();
        oprAccount.loadAccount(this);

        file = null;
        filename = "";
        String a = oprAccount.setid();
        File subFolder = new File(MyConfig.path() + "/Android/data/com.arindo.bruconnect/file/");
        boolean success = true;
        if (!subFolder.exists()) {
            success = subFolder.mkdir();
        }
        if (success) {
            try {
                filename = a + ".jpg";
                file = new File(subFolder, filename);
                if(param==1) {
                    mImageCaptureUri = Uri.fromFile(file);
                    Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    i.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                    startActivityForResult(i, 1);
                }else{
                    Intent getImageFromGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                    startActivityForResult(getImageFromGalleryIntent, 1);
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        } else {
            //Toast(2, "Gagal Create Sub Folder Foto!");
        }
    }

    private void copyFile(File sourceFile, File destFile) throws IOException {
        if (!sourceFile.exists()) {
            return;
        }

        FileChannel source = null;
        FileChannel destination = null;
        source = new FileInputStream(sourceFile).getChannel();
        destination = new FileOutputStream(destFile).getChannel();
        if (destination != null && source != null) {
            destination.transferFrom(source, 0, source.size());
        }
        if (source != null) {
            source.close();
        }
        if (destination != null) {
            destination.close();
        }
    }

    private String getRealPathFromURI(Uri contentUri) {

        String[] proj = { MediaStore.Video.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {
        //super.onActivityResult(requestCode, resultCode, returnIntent);
        switch (requestCode) {
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = null;
                    if(mediaFoto==1) {
                        selectedImage = mImageCaptureUri;
                    }else {
                        selectedImage = returnIntent.getData();
                    }
                    ContentResolver cr = getContentResolver();

                    String nuFile = filename;
                    String rfiles = MyConfig.path() + "/Android/data/com.arindo.bruconnect/file/" + nuFile;

                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(cr, selectedImage);
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        file = null;

                        Log.e("choose 1",e.toString());
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        file = null;
                        Log.e("choose 2",e.toString());
                    } catch (Exception e) {
                        // TODO: handle exception
                        e.printStackTrace();
                        file = null;
                        Log.e("choose 3",e.toString());
                    }

                    if (bitmap == null) {
                        //Toast(3, "Image error, silahkan coba kembali");
                        file = null;
                        break;
                    } else {
                        if (mediaFoto == 1){
                            rotate(rfiles);
                            showImage();
                            bitmap.recycle();
                        }else{
                            File f = new File(rfiles);
                            //if (!f.exists()){
                                try {
                                    f.createNewFile();
                                    copyFile(new File(getRealPathFromURI(selectedImage)), f);
                                    rotate(rfiles);
                                    showImage();
                                    bitmap.recycle();
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            //}
                        }

                        //Delete File di DCIM path default foto di perangkat
                        File sdCardRoot = Environment.getExternalStorageDirectory();
                        String path = sdCardRoot.toString();
                        File dir = new File(path + "/DCIM/.thumbnails/");
                        if (dir.exists()) {
                            if (dir.isDirectory()) {
                                String[] children = dir.list();
                                for (int i = 0; i < children.length; i++) {
                                    new File(dir, children[i]).delete();
                                }
                            }
                        }

                        //Delete File di DCIM path setting aplikasi
                        //File sdCardRoot = Environment.getExternalStorageDirectory();
                        //String path = sdCardRoot.toString();
                        File dir2 = new File(MyConfig.path() + "/DCIM/.thumbnails/");

                        if (dir2.exists()) {
                            if (dir2.isDirectory()) {
                                String[] children = dir2.list();
                                for (int i = 0; i < children.length; i++) {
                                    new File(dir2, children[i]).delete();
                                }
                            }
                        }

                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    //Toast(2, "Foto tidak diambil");
                    file = null;
                }
            break;
        }
    }

    private void rotate(String rfiles) {
        Integer w = 0, h = 0;
        Bitmap bmp = BitmapFactory.decodeFile(rfiles);
        try {
            ExifInterface exif = new ExifInterface(rfiles);
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotate = 0;
            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
            }

            int newWidth = 0, newHeight = 0;
            w = bmp.getWidth();
            h = bmp.getHeight();
            newWidth = 320;
            newHeight = 380;

            float scaleWidth = ((float) newWidth) / w;
            float scaleHeight = ((float) newHeight) / h;
            Matrix matrix = new Matrix();
            matrix.preRotate(rotate);
            matrix.postScale(scaleWidth, scaleHeight);
            Bitmap bmpnew = Bitmap.createBitmap(bmp, 0, 0, w, h, matrix, true);

            FileOutputStream fBOut;
            try {
                fBOut = new FileOutputStream(rfiles);
                bmpnew.compress(Bitmap.CompressFormat.JPEG, 100, fBOut);
                fBOut.flush();
                fBOut.close();

                UpdateFileName(); // update imgprofile in db
            } catch (FileNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showImage() {
        if (file != null) {
            String pn = file.toString();
            ShowImageZoom(pn);
        }
    }

    private void UpdateFileName() {
        try{
            SqlHelper dbHelper = new SqlHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL("UPDATE tbl_account SET imgprofile='"+filename+"' WHERE idnom=1");
            db.close();
            Log.e("path",file.toString());
            MainActivity.refreshImage(file.toString());
            Setting.refreshImage(file.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
	public boolean onSupportNavigateUp(){
		onBackPressed();
	    return true;
	}
}
