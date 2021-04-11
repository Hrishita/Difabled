/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hrishita.difabled.org.tensorflow.lite.examples.detection;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Build;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.widget.Toast;

import com.hrishita.difabled.BlindHomeActivity;
import com.hrishita.difabled.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.tensorflow.lite.examples.detection.customview.OverlayView;
import org.tensorflow.lite.examples.detection.customview.OverlayView.DrawCallback;
import org.tensorflow.lite.examples.detection.env.BorderedText;
import org.tensorflow.lite.examples.detection.env.ImageUtils;
import org.tensorflow.lite.examples.detection.env.Logger;
import org.tensorflow.lite.examples.detection.tflite.Detector;
import org.tensorflow.lite.examples.detection.tflite.TFLiteObjectDetectionAPIModel;
import org.tensorflow.lite.examples.detection.tracking.MultiBoxTracker;

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {
  private static final Logger LOGGER = new Logger();
  private TextToSpeech tts;
  // Configuration values for the prepackaged SSD model.
  private static final int TF_OD_API_INPUT_SIZE = 320;
  private static final boolean TF_OD_API_IS_QUANTIZED = true;
  private static final String TF_OD_API_MODEL_FILE = "detect.tflite";
  private static final String TF_OD_API_LABELS_FILE = "labelmap.txt";
  private static final DetectorMode MODE = DetectorMode.TF_OD_API;
  // Minimum detection confidence to track a detection.
  private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
  private static final boolean MAINTAIN_ASPECT = false;
  private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
  private static final boolean SAVE_PREVIEW_BITMAP = false;
  private static final float TEXT_SIZE_DIP = 10;
  OverlayView trackingOverlay;
  private Integer sensorOrientation;

  private Detector detector;

  private long lastProcessingTimeMs;
  private Bitmap rgbFrameBitmap = null;
  private Bitmap croppedBitmap = null;
  private Bitmap cropCopyBitmap = null;

  private boolean computingDetection = false;

  private long timestamp = 0;

  private Matrix frameToCropTransform;
  private Matrix cropToFrameTransform;

  private MultiBoxTracker tracker;

  private BorderedText borderedText;

  @Override
  public void onPreviewSizeChosen(final Size size, final int rotation) {
    final float textSizePx =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
    borderedText.setTypeface(Typeface.MONOSPACE);

    tracker = new MultiBoxTracker(this);

    int cropSize = TF_OD_API_INPUT_SIZE;

    try {
      detector =
          TFLiteObjectDetectionAPIModel.create(
              this,
              TF_OD_API_MODEL_FILE,
              TF_OD_API_LABELS_FILE,
              TF_OD_API_INPUT_SIZE,
              TF_OD_API_IS_QUANTIZED);
      cropSize = TF_OD_API_INPUT_SIZE;
    } catch (final IOException e) {
      e.printStackTrace();
      LOGGER.e(e, "Exception initializing Detector!");
      Toast toast =
          Toast.makeText(
              getApplicationContext(), "Detector could not be initialized", Toast.LENGTH_SHORT);
      toast.show();
      finish();
    }

    previewWidth = size.getWidth();
    previewHeight = size.getHeight();

    sensorOrientation = rotation - getScreenOrientation();
    LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

    LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
    croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);

    frameToCropTransform =
        ImageUtils.getTransformationMatrix(
            previewWidth, previewHeight,
            cropSize, cropSize,
            sensorOrientation, MAINTAIN_ASPECT);

    cropToFrameTransform = new Matrix();
    frameToCropTransform.invert(cropToFrameTransform);

    trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
    trackingOverlay.addCallback(
        new DrawCallback() {
          @Override
          public void drawCallback(final Canvas canvas) {
            tracker.draw(canvas);
            if (isDebug()) {
              tracker.drawDebug(canvas);
            }
          }
        });

    tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
  }

  @Override
  protected void processImage() {
    ++timestamp;
    final long currTimestamp = timestamp;
    trackingOverlay.postInvalidate();

    // No mutex needed as this method is not reentrant.
    if (computingDetection) {
      readyForNextImage();
      return;
    }
    computingDetection = true;
    LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

    rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

    readyForNextImage();

    final Canvas canvas = new Canvas(croppedBitmap);
    canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
    // For examining the actual TF input.
    if (SAVE_PREVIEW_BITMAP) {
      ImageUtils.saveBitmap(croppedBitmap);
    }

    runInBackground(
        new Runnable() {
          @Override
          public void run() {
            LOGGER.i("Running detection on image " + currTimestamp);
            final long startTime = SystemClock.uptimeMillis();
            final List<Detector.Recognition> results = detector.recognizeImage(croppedBitmap);
            lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

            cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
            final Canvas canvas = new Canvas(cropCopyBitmap);
            final Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Style.STROKE);
            paint.setStrokeWidth(2.0f);

            float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
            switch (MODE) {
              case TF_OD_API:
                minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                break;
            }

            final List<Detector.Recognition> mappedRecognitions =
                new ArrayList<Detector.Recognition>();
            tts = new TextToSpeech(DetectorActivity.this, new TextToSpeech.OnInitListener() {
              @Override
              public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                  int result = tts.setLanguage(new Locale("Eng", "IND"));

                  tts.setSpeechRate(1.0f);
                  if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "This Language is not supported");
                  }
                  tts.setPitch(1.0f);
                } else {

                  Log.e("TTS", "Initialization Failed!");
                }
              }
            });
            for (final Detector.Recognition result : results) {
              final RectF location = result.getLocation();
              if (location != null && result.getConfidence() >= minimumConfidence) {
                if(result.getTitle().equals("A")){
                  result.setTitle("Hello");

                  canvas.drawRect(location, paint);
                  String text = result.getTitle();
                  Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
                  speak(text);
                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
                if (result.getTitle().equals("B")){
                  result.setTitle("Good bye");

                  canvas.drawRect(location, paint);
                  String text = result.getTitle();
                  Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
                  speak(text);
                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
                if (result.getTitle().equals("C")){
                  result.setTitle("Help");

                  canvas.drawRect(location, paint);
                  String text = result.getTitle();
                  Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
                  speak(text);
                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
                if(result.getTitle().equals("A")){
                  result.setTitle("Hello");

                  canvas.drawRect(location, paint);
                  String text = result.getTitle();
                  Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
                  speak(text);
                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
                if (result.getTitle().equals("B")){
                  result.setTitle("Good bye");

                  canvas.drawRect(location, paint);
                  String text = result.getTitle();
                  Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
                  speak(text);
                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
                if (result.getTitle().equals("C")){
                  result.setTitle("Help");

                  canvas.drawRect(location, paint);
                  String text = result.getTitle();
                  Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
                  speak(text);
                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                } if(result.getTitle().equals("D")){
                  result.setTitle("Nice to meet you ");

                  canvas.drawRect(location, paint);
                  String text = result.getTitle();
                  Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
                  speak(text);
                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
                if (result.getTitle().equals("E")){
                  result.setTitle("I am fine");

                  canvas.drawRect(location, paint);
                  String text = result.getTitle();
                  Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
                  speak(text);
                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
                if (result.getTitle().equals("F")){
                  result.setTitle("how are you?");

                  canvas.drawRect(location, paint);
                  String text = result.getTitle();
                  Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
                  speak(text);
                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                } if(result.getTitle().equals("G")){
                  result.setTitle("Sorry");

                  canvas.drawRect(location, paint);
                  String text = result.getTitle();
                  Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
                  speak(text);
                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
                if (result.getTitle().equals("H")){
                  result.setTitle(" bye");

                  canvas.drawRect(location, paint);
                  String text = result.getTitle();
                  Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
                  speak(text);
                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
                if (result.getTitle().equals("I")){
                  result.setTitle("Excuse me");

                  canvas.drawRect(location, paint);
                  String text = result.getTitle();
                  Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
                  speak(text);
                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
                if(result.getTitle().equals("J")){
                  result.setTitle("Happy");

                  canvas.drawRect(location, paint);
                  String text = result.getTitle();
                  Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
                  speak(text);
                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
                if (result.getTitle().equals("K")){
                  result.setTitle("Sad");

                  canvas.drawRect(location, paint);
                  String text = result.getTitle();
                  Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
                  speak(text);
                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
                if (result.getTitle().equals("L")){
                  result.setTitle("I am ILL");

                  canvas.drawRect(location, paint);
                  String text = result.getTitle();
                  Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
                  speak(text);
                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
                if(result.getTitle().equals("M")){
                  result.setTitle("Contact me later");

                  canvas.drawRect(location, paint);
                  String text = result.getTitle();
                  Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
                  speak(text);
                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
                if (result.getTitle().equals("N")){
                  result.setTitle("I am busy");

                  canvas.drawRect(location, paint);
                  String text = result.getTitle();
                  Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
                  speak(text);
                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
                if (result.getTitle().equals("O")){
                  result.setTitle("Today");

                  canvas.drawRect(location, paint);
                  String text = result.getTitle();
                  Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
                  speak(text);
                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
                if(result.getTitle().equals("P")){
                  result.setTitle("Nice to meet you ");

                  canvas.drawRect(location, paint);
                  String text = result.getTitle();
                  Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
                  speak(text);
                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
                if(result.getTitle().equals("Q")){
                  result.setTitle("Nice to meet you ");

                  canvas.drawRect(location, paint);
                  String text = result.getTitle();
                  Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
                  speak(text);
                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
                if(result.getTitle().equals("R")){
                  result.setTitle("Nice to meet you ");

                  canvas.drawRect(location, paint);
                  String text = result.getTitle();
                  Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
                  speak(text);
                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
                if(result.getTitle().equals("S")){
                  result.setTitle("Nice to meet you ");

                  canvas.drawRect(location, paint);
                  String text = result.getTitle();
                  Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
                  speak(text);
                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
                if(result.getTitle().equals("T")){
                  result.setTitle("Nice to meet you ");

                  canvas.drawRect(location, paint);
                  String text = result.getTitle();
                  Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
                  speak(text);
                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
                if(result.getTitle().equals("U")){
                  result.setTitle("Nice to meet you ");

                  canvas.drawRect(location, paint);
                  String text = result.getTitle();
                  Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
                  speak(text);
                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
                if(result.getTitle().equals("V")){
                  result.setTitle("Nice to meet you ");

                  canvas.drawRect(location, paint);
                  String text = result.getTitle();
                  Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
                  speak(text);
                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
                if(result.getTitle().equals("W")){
                  result.setTitle("Nice to meet you ");

                  canvas.drawRect(location, paint);
                  String text = result.getTitle();
                  Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
                  speak(text);
                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
                if(result.getTitle().equals("X")){
                  result.setTitle("Nice to meet you ");

                  canvas.drawRect(location, paint);
                  String text = result.getTitle();
                  Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
                  speak(text);
                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
                if(result.getTitle().equals("Y")){
                  result.setTitle("Nice to meet you ");

                  canvas.drawRect(location, paint);
                  String text = result.getTitle();
                  Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
                  speak(text);
                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
                if(result.getTitle().equals("Z")){
                  result.setTitle("Nice to meet you ");

                  canvas.drawRect(location, paint);
                  String text = result.getTitle();
                  Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT).show();
                  speak(text);
                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
              }
            }

            tracker.trackResults(mappedRecognitions, currTimestamp);
            trackingOverlay.postInvalidate();

            computingDetection = false;

            runOnUiThread(
                new Runnable() {
                  @Override
                  public void run() {
                    showFrameInfo(previewWidth + "x" + previewHeight);
                    showCropInfo(cropCopyBitmap.getWidth() + "x" + cropCopyBitmap.getHeight());
                    showInference(lastProcessingTimeMs + "ms");
                  }
                });
          }
        });
  }

  @Override
  public void onDestroy() {
    if (tts != null) {
      tts.stop();
      tts.shutdown();

    }
    super.onDestroy();
  }

  private void speak(String text) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    } else {
      tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
  }

  @Override
  protected int getLayoutId() {
    return R.layout.tfe_od_camera_connection_fragment_tracking;
  }

  @Override
  protected Size getDesiredPreviewFrameSize() {
    return DESIRED_PREVIEW_SIZE;
  }

  // Which detection model to use: by default uses Tensorflow Object Detection API frozen
  // checkpoints.
  private enum DetectorMode {
    TF_OD_API;
  }

  @Override
  protected void setUseNNAPI(final boolean isChecked) {
    runInBackground(
        () -> {
          try {
            detector.setUseNNAPI(isChecked);
          } catch (UnsupportedOperationException e) {
            LOGGER.e(e, "Failed to set \"Use NNAPI\".");
            runOnUiThread(
                () -> {
                  Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
          }
        });
  }

  @Override
  protected void setNumThreads(final int numThreads) {
    runInBackground(() -> detector.setNumThreads(numThreads));
  }
}
