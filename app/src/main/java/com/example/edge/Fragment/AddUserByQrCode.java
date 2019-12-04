package com.example.edge.Fragment;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.media.Image;
import android.os.Bundle;

import com.example.edge.Activity.ScanQrActivity;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.edge.R;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddUserByQrCode extends Fragment {


    ImageView imageView;
    String uid;

    SurfaceView surfaceView;
    CameraSource cameraSource;

    BarcodeDetector barcodeDetector;
    Button btnScan;

    public AddUserByQrCode() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_user_by_qr_code, container, false);

        imageView = view.findViewById(R.id.imageView);
        btnScan = view.findViewById(R.id.btnScan);
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("preference", Context.MODE_PRIVATE);
        uid = sharedPreferences.getString("uid","");
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),ScanQrActivity.class);
                startActivity(intent);
            }
        });
        Toast.makeText(getContext(), ""+uid, Toast.LENGTH_SHORT).show();
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(uid, BarcodeFormat.QR_CODE,200,200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            imageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return view;
    }


}
