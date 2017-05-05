package ml.testsite_vic;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ml.testsite_vic.PopUp.ViewDialog;
import ml.testsite_vic.linkscanner.R;

public class MainActivity extends AppCompatActivity {

    /*
     * TODO: Multiple Links
    */

    SurfaceView cameraView;
    TextView textView;
    CameraSource cameraSource;
    public static TinyDB tinydb;

    final int requestCameraPermissionID = 20147;
    public static final Pattern regEx = Pattern.compile("(https?:\\/\\/|www\\.)([-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,4})\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)");

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case requestCameraPermissionID: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        try {
                            cameraSource.start(cameraView.getHolder());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        tinydb = new TinyDB(getApplicationContext());

        cameraView = (SurfaceView) findViewById(R.id.surface_view);
        textView = (TextView) findViewById(R.id.text_view);

        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        ArrayList strings = tinydb.getListString(getString(R.string.tinyDB_cache_name));

        for (int i = 0; i < strings.size(); i++) {
            Log.d("MainActivity", strings.get(i).toString());
        }

        if (!textRecognizer.isOperational()) {
            Log.w("MainActivity", getString(R.string.TRNA));
        } else {
            cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setRequestedFps(2.0f)
                    .setAutoFocusEnabled(true)
                    .build();

            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, requestCameraPermissionID);
                            return;
                        }
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                         e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    cameraSource.stop();
                }
            });

            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();

                    if (items.size() != 0) {
                        textView.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder stringBuilder = new StringBuilder();

                                for (int i = 0; i < items.size(); i++) {
                                    TextBlock item = items.valueAt(i);
                                    String value = item.getValue();
                                    stringBuilder.append(value);
                                    stringBuilder.append("\n");
                                    value = value.toLowerCase();

                                    Matcher matches = regEx.matcher(value);

                                    if (matches.find()) {
                                        String url = matches.group(2).toString() + matches.group(3).toString();

                                        Log.i("Match", url);
                                        openWebPage(url, MainActivity.this);
                                        finish();
                                    }
                                }

                                textView.setText(stringBuilder.toString());
                            }
                        });
                    }
                }
            });
        }

        findViewById(R.id.floatingActionButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String[] itemName = {
                        "Upload image",
                        "History",
                        "Settings"
                };

                Integer[] imgId = {
                        R.drawable.ic_file_upload_black_24dp,
                        R.drawable.ic_history_black_24dp,
                        R.drawable.ic_settings_black_24dp
                };

                ViewDialog alert = new ViewDialog();
                alert.showDialog(MainActivity.this, itemName, imgId);
            }
        });
    }

    static void addUrlToCache(String url, Activity activity) {
        ArrayList<String> history = tinydb.getListString(activity.getString(R.string.tinyDB_cache_name));
        history.add(url);
        tinydb.putListString(activity.getString(R.string.tinyDB_cache_name), history);
    }

    public static String arrayListToString(ArrayList<String> arrayList, Activity activity) {
        String str = "";

        for (int i = 0; i < arrayList.size(); i++) {
            str += arrayList.get(i) + activity.getString(R.string.arrayListToStringSplitChar);
        }

        return str;
    }

    public static void openWebPage(String url, Activity activity) {
        Log.d("Web", url);

        Toast.makeText(activity.getApplicationContext(), activity.getText(R.string.found) + ": " + /*matches.group(3).toString()*/ url + "\nRedirecting to browser...", Toast.LENGTH_SHORT).show();

        if (!url.startsWith("http://") && !url.startsWith("https://"))
            url = "http://" + url;

        addUrlToCache(url, activity);

        Intent intent = new Intent(activity.getApplicationContext(), BuiltInBrowser.class);
        intent.putExtra("url", url);
        activity.startActivity(intent);

//        Uri webpage = Uri.parse(url);
//        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
//        if (intent.resolveActivity(activity.getPackageManager()) != null) {
//            activity.startActivity(intent);
//        }
    }

    public static void deleteHistory(Context context) {
        MainActivity.tinydb.remove(context.getString(R.string.tinyDB_cache_name));
    }
}
