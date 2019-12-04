package com.example.edge.Fragment;


import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.edge.Activity.FinishRegister;
import com.example.edge.R;
import com.example.edge.Utils.Helper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.rpc.Help;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 */
public class PhotoFragment extends Fragment {


    private static final int CONTENT_REQUEST=1337;
    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    private static final int PICK_IMAGE_REQUEST = 1;

    Uri image_uri;

    Button captureBtn,uploadBtn;
    ImageView captureIv;
    FirebaseStorage storage;
    StorageReference storageReference;
    StorageReference imageRef;
    StorageReference userRef;
    FirebaseAuth auth;
    File output;

    public PhotoFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        auth = FirebaseAuth.getInstance();
        Toast.makeText(getContext(), "Masuk Create", Toast.LENGTH_SHORT).show();
//        Toast.makeText(view.getContext(), ""+auth.getUid(), Toast.LENGTH_SHORT).show();
        captureBtn = (Button) view.findViewById(R.id.captureBtn);
        captureIv = (ImageView) view.findViewById(R.id.captureIv);
        uploadBtn = view.findViewById(R.id.uploadBtn);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        imageRef = storageReference.child("profile"+auth.getUid());
        userRef = storageReference.child("images/"+auth.getUid());

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,PICK_IMAGE_REQUEST);
            }
        });

        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(getView().getContext().checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
                    || getView().getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                        String [] permision = {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permision,PERMISSION_CODE);
                        Toast.makeText(getView().getContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        openCamera();
                        Toast.makeText(getView().getContext(), "Permission Already Granted", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    openCamera();
                }
            }
        });
        return view;
    }

    private void openCamera(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION,"From The Camera");
        image_uri = getView().getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        try {
            output = File.createTempFile("test",".jpg",Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
        } catch (IOException e) {
            e.printStackTrace();
        }
        startActivityForResult(intent,IMAGE_CAPTURE_CODE);
    }

    private void rotateImage(Bitmap bitmap) throws Exception{
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(output.getPath().toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("error",e.getMessage());
            Toast.makeText(getView().getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface
        .ORIENTATION_UNDEFINED);
        Matrix matrix = new Matrix();
        Toast.makeText(getView().getContext(), ""+orientation, Toast.LENGTH_SHORT).show();
        switch(orientation){
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                Toast.makeText(getView().getContext(), "Geser 90", Toast.LENGTH_SHORT).show();
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                Toast.makeText(getView().getContext(), "Geser 180", Toast.LENGTH_SHORT).show();
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(270);
                Toast.makeText(getView().getContext(), "Geser 270", Toast.LENGTH_SHORT).show();
                default:
                    matrix.setRotate(90);
        }
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
//        return rotatedBitmap;
        captureIv.setImageBitmap(rotatedBitmap);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case PERMISSION_CODE:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openCamera();
                }
                else{
                    Toast.makeText(getView().getContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK){
            image_uri = data.getData();
            captureIv.setImageURI(image_uri);
            Bitmap upload = ((BitmapDrawable) captureIv.getDrawable()).getBitmap();
            try {
                rotateImage(upload);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getView().getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            upload = ((BitmapDrawable) captureIv.getDrawable()).getBitmap();

            Toast.makeText(getView().getContext(), "" + image_uri.getPath().toString() + " Cuy", Toast.LENGTH_SHORT).show();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            upload.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] byteData = baos.toByteArray();

            UploadTask uploadTask = imageRef.putBytes(byteData);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getContext(), "Upload Success", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), "Upload Failed", Toast.LENGTH_SHORT).show();
                }
            });
            Helper.imageView = captureIv;
        }
        if(requestCode == IMAGE_CAPTURE_CODE) {
            if (resultCode == Activity.RESULT_OK) {
//                rotateImage(image_uri);
//                Uri temp = data.getData();
//                try {
//                    Bitmap bitmapTemp = MediaStore.Images.Media.getBitmap(getView().getContext().getContentResolver(),temp);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                captureIv.setImageURI(image_uri);
                captureIv.setDrawingCacheEnabled(true);
                captureIv.buildDrawingCache();
                Bitmap upload = ((BitmapDrawable) captureIv.getDrawable()).getBitmap();
                try {
                    rotateImage(upload);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getView().getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                upload = ((BitmapDrawable) captureIv.getDrawable()).getBitmap();
//                try {
//                    rotateImage(upload);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    Log.e("Errorss",e.getMessage());
//                    Toast.makeText(getView().getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//                }
                Toast.makeText(getView().getContext(), "" + image_uri.getPath().toString() + " Cuy", Toast.LENGTH_SHORT).show();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                upload.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] byteData = baos.toByteArray();

                UploadTask uploadTask = imageRef.putBytes(byteData);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getContext(), "Upload Success", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Upload Failed", Toast.LENGTH_SHORT).show();
                    }
                });
                Helper.imageView = captureIv;
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setDataAndType(Uri.fromFile(output),"image.jpeg");
//                startActivity(intent);
            }
        }
        makeProgress();
        ((FinishRegister)getActivity()).continueProgress();
//        }
//        Bitmap bitmap = (Bitmap) data.getExtras().get("data");

    }

    public void makeProgress(){
        if(Helper.progressNow < 100 && !Helper.PHOTO_STATE){
            if(Helper.progressNow + 33 < 100){
                Helper.progressNow += 33;
                Helper.PHOTO_STATE = true;
            }
            else{
                Helper.progressNow = 100;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Toast.makeText(getContext(), "Masuk Resume", Toast.LENGTH_SHORT).show();
        if(Helper.imageView != null){
            captureIv.setImageDrawable(Helper.imageView.getDrawable());
        }
    }
}
