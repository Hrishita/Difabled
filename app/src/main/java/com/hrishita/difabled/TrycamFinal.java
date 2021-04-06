package com.hrishita.difabled;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.extensions.HdrImageCaptureExtender;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TrycamFinal extends AppCompatActivity {
    PreviewView mPreviewView;
    Timer timer;
    TensorBuffer inputFeature0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trycam);
        RelativeLayout rl = findViewById(R.id.rl_camera_base);
        Canvas canvas = new Canvas();
        Paint p = new Paint();
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.RED);
        p.setStrokeWidth(1f);
        canvas.drawRect(0,0,64,64, p);
        rl.draw(canvas);
        timer = new Timer();
        mPreviewView = findViewById(R.id.previewcamera1);
        startCamera();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Bitmap bmp = mPreviewView.getBitmap();
                bmp = Bitmap.createBitmap(bmp, 0, 0, 64, 64);
                //bmp = Bitmap.createScaledBitmap(bmp, 64, 64, false);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);


                ImageProcessor imageProcessor =
                        new ImageProcessor.Builder()
                                .add(new ResizeOp(64, 64, ResizeOp.ResizeMethod.BILINEAR))
                                .build();

                TensorImage tImage = new TensorImage(DataType.FLOAT32);

                tImage.load(bmp);
                tImage = imageProcessor.process(tImage);

                TensorBuffer probabilityBuffer =
                        TensorBuffer.createFixedSize(new int[]{1, 16}, DataType.FLOAT32);

                // Initialise the model
                try{
                    MappedByteBuffer tfliteModel
                            = FileUtil.loadMappedFile(TrycamFinal.this,
                            "model.tflite");
                    Interpreter tflite = new Interpreter(tfliteModel);

                    if(null != tflite) {
                        tflite.run(tImage.getBuffer(), probabilityBuffer.getBuffer());
                    }
                    int index = 0;
                    System.out.println("starting....");
                    for(int x = 0;x< probabilityBuffer.getFloatArray().length;x++)
                    {
                        System.out.print(probabilityBuffer.getFloatArray()[x] + " ");
                        if(probabilityBuffer.getFloatArray()[x] > probabilityBuffer.getFloatArray()[index])
                        {
                            index= x;
                        }
                    }
                    int finalIndex = index;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(TrycamFinal.this, "" + finalIndex, Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (IOException e){
                    e.printStackTrace();
                    Log.e("tfliteSupport", "Error reading model", e);
                }



                /*inputFeature0.loadBuffer(ByteBuffer.wrap(stream.toByteArray()));
                Model model = null;
                try {
                    model = Model.newInstance(getApplicationContext());
                    // Runs model inference and gets result.
                    Model.Outputs outputs = model.process(inputFeature0);
                    TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
                    outputFeature0=outputs.getOutputFeature0AsTensorBuffer();
                    Toast.makeText(getApplicationContext(), ""+outputFeature0.toString(), Toast.LENGTH_SHORT).show();
                    // Releases model resources if no longer used.
                    model.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }*/

                bmp.recycle();
            }
        }, 1500,1000);
    }
    private Executor executor = Executors.newSingleThreadExecutor();
    private void startCamera() {

        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {

                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);

                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();

        imageAnalysis.setAnalyzer(executor, new TrycamFinal.YourAnalyzer());

        ImageCapture.Builder builder = new ImageCapture.Builder();

        //Vendor-Extensions (The CameraX extensions dependency in build.gradle)
        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);

        // Query if extension is available (optional).
        if (hdrImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
            // Enable the extension if available.
            hdrImageCaptureExtender.enableExtension(cameraSelector);
        }



        final ImageCapture imageCapture = builder
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();
        preview.setSurfaceProvider(mPreviewView.createSurfaceProvider());
     Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview, imageAnalysis, imageCapture);

    }

    private class YourAnalyzer implements ImageAnalysis.Analyzer {


        @SuppressLint("UnsafeExperimentalUsageError")
        @Override
        public void analyze(ImageProxy imageProxy) {
            Image mediaImage = imageProxy.getImage();

            System.out.println("Reached Here");
            if (mediaImage != null) {
                InputImage image =
                    InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                // Creates inputs for reference.
                inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 64, 64, 3}, DataType.FLOAT32);

            }
        }

    }
    @Nullable
    public static Bitmap getBitmap(Image data) {
        ByteBuffer yBuffer = data.getPlanes()[0].getBuffer();
        ByteBuffer vuBuffer = data.getPlanes()[2].getBuffer();

        int ySize = yBuffer.remaining();
        int vuSize = vuBuffer.remaining();

        byte[] nv21 = new byte[(ySize + vuSize)];
        yBuffer.get(nv21, 0, ySize);
        vuBuffer.get(nv21, 0, vuSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, data.getWidth(), data.getHeight(), null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(), 100, baos);
        return BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size());
    }

}