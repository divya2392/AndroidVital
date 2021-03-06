package com.pawan.androidvital.fragment.ChooseImage;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.pawan.androidvital.R;
import com.pawan.androidvital.app.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChooseImageFragment extends Fragment implements View.OnClickListener {
    private Button buttonFromCamera, buttonFromGallery;
    private String cameraFileName;
    public static final String DOWNLOAD_IMAGE_PATH = "";
    public static final int CHOICE_AVATAR_FROM_CAMERA = 0;
    public static final int CHOICE_AVATAR_FROM_GALLERY = 1;
    public static final int CHOICE_AVATAR_FROM_CAMERA_CROP = 3;
    public static final int MY_PERMISSIONS_REQUEST_READ_PHOTO = 4;
    private boolean accessPermissionGranted = true;



    public ChooseImageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_image, container, false);
        buttonFromCamera = (Button) view.findViewById(R.id.button_from_camera);
        buttonFromGallery = (Button) view.findViewById(R.id.button_from_gallery);
        buttonFromCamera.setOnClickListener(this);
        buttonFromGallery.setOnClickListener(this);
        return view;
    }

   /* @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.button_from_gallery) {
            choiceAvatarFromGallery();
        } else if(id == R.id.button_from_camera) {
            choiceAvatarFromCamera();
        }
    }*/

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.button_from_gallery) {
            if (Build.VERSION.SDK_INT >= 23) {
                accessPermissionGranted = false;
                requestPermissions();
            }
            choiceAvatarFromGallery();
        } else if(id == R.id.button_from_camera) {
            choiceAvatarFromCamera();
        }
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_PHOTO);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHOTO: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    accessPermissionGranted = true;
                } else {
                    accessPermissionGranted = false;
                }
            }
        }
    }

    public void choiceAvatarFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraFileName = "file" + System.currentTimeMillis() + ".png";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), cameraFileName);
        if(!file.exists()){
            file.mkdirs();
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CHOICE_AVATAR_FROM_CAMERA_CROP);
    }

    public void choiceAvatarFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(getCropIntent(intent), CHOICE_AVATAR_FROM_GALLERY);
    }

    private Intent getCropIntent(Intent intent) {
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 320);
        intent.putExtra("outputY", 320);
        intent.putExtra("return-data", true);
        return intent;
    }

    public static Bitmap getBitmapFromData(Intent data) {
        Bitmap photo = null;
        Uri photoUri = data.getData();
        if (photoUri != null) {
            photo = BitmapFactory.decodeFile(photoUri.getPath());
        }
        if (photo == null) {
            Bundle extra = data.getExtras();
            if (extra != null) {
                photo = (Bitmap) extra.get("data");
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            }
        }
        return photo;
    }

    public Bitmap getBitmapFromDataV20(Intent intent) {
        Uri uri = intent.getData();
        if( uri == null ) {
            return null;
        }
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor;
        if(Build.VERSION.SDK_INT > 19) {
            String wholeID = DocumentsContract.getDocumentId(uri);
            String id = wholeID.split(":")[1];
            String sel = MediaStore.Images.Media._ID + "=?";
            cursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,projection, sel, new String[]{ id }, null);
        } else {
            cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
        }
        String path = null;
        try {
            int column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            path = cursor.getString(column_index).toString();
            cursor.close();
        } catch(NullPointerException e) {}
        return BitmapFactory.decodeFile(path);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CHOICE_AVATAR_FROM_CAMERA ) {
                Utils.generateToast(getContext(), "CHOICE_AVATAR_FROM_CAMERA", true);
                //Bitmap avatar = getBitmapFromData(data);
                Bitmap avatar = getBitmapFromDataV20(data);
                storeImage(avatar);
            }else if(requestCode == CHOICE_AVATAR_FROM_GALLERY)
            {
                Utils.generateToast(getContext(), "CHOICE_AVATAR_FROM_GALLARY", true);
                //Bitmap avatar = getBitmapFromData(data);
                Bitmap avatar = getBitmapFromDataV20(data);
                storeImage(avatar);
            }
            else if (requestCode == CHOICE_AVATAR_FROM_CAMERA_CROP) {
                Intent intent = new Intent("com.android.camera.action.CROP");
                Uri uri = Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), cameraFileName));
                intent.setDataAndType(uri, "image/*");
                startActivityForResult(getCropIntent(intent), CHOICE_AVATAR_FROM_CAMERA);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void storeImage(Bitmap image) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "crop_avatar.png");
        try {
            if(!file.exists())
                file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.e("FileNotFoundException", e.getMessage());
        } catch (IOException e) {
            Log.e("IOException", e.getMessage());
        }
    }
}
