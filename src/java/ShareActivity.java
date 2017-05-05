package ml.testsite_vic;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.util.regex.Matcher;

import ml.testsite_vic.linkscanner.R;

import static ml.testsite_vic.MainActivity.openWebPage;

public class ShareActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 010701;

    // TODO: Loading animation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.share_layout);
        getSupportActionBar().hide();

        // Get the intent that started this activity
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendImage(intent);
            } else {
                noImageFound();
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendMultipleImages(intent); // Handle multiple images being sent
            } else {
                noImageFound();
            }
        } else {
            noImageFound();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE) {
                handleSendImage(data);
            } else {
                Log.d("ShareActivity", Integer.toString(requestCode));
                cancel("RequestCode is not correct");
            }
        } else {
            Log.d("ShareActivity", Integer.toString(resultCode));
//            cancel("No image selected");
            cancel(null);
        }
    }

    private void handleSendImage(Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);

        if (imageUri != null) {
            recognizeImage(imageUri);
        } else {
            imageUri = intent.getData();

            if (imageUri != null) {
                recognizeImage(imageUri);
            } else {
                cancel(getString(R.string.nai));
            }
        }
    }

    private void handleSendMultipleImages(Intent intent) {
        cancel(getString(R.string.WIP));

        /*ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            // Update UI to reflect multiple images being shared
        }*/
    }

    private void recognizeImage(Uri imageUri) {
        // Update UI to reflect image being shared
        RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.relative_layout);
        mainLayout.removeAllViews();

        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setImageURI(imageUri);

        mainLayout.addView(imageView);

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

            if (!textRecognizer.isOperational()) {
                Toast.makeText(getApplicationContext(), "Error with TextRecognizer", Toast.LENGTH_SHORT).show();
            } else {
                Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                SparseArray<TextBlock> items = textRecognizer.detect(frame);
                StringBuilder stringBuilder = new StringBuilder();

                for (int i = 0; i < items.size(); i++) {
                    TextBlock item = items.valueAt(i);

                    // Item also has boundingBox!!!!
                    stringBuilder.append(item.getValue() + "\n");

                    // Check for links
                    String value = item.getValue().toLowerCase();
                    Matcher matches = MainActivity.regEx.matcher(value);

                    if (matches.find()) {
                        String url = matches.group(2).toString() + matches.group(3).toString();

                        Log.i("Match", url);
                        openWebPage(url, ShareActivity.this);
                        finish();
                    }
                }

                if (stringBuilder.toString() != "") {
                    final TextView textView = new TextView(this);
                    textView.setElevation(10);
                    textView.setTextColor(getResources().getColor(R.color.colorPrimaryText));
                    textView.setText(stringBuilder.toString());
                    mainLayout.addView(textView);

                    // Draw Boxes
                    DrawBoxes drawBoxes = new DrawBoxes(getApplicationContext(), items);
                    ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
                    addContentView(drawBoxes, layoutParams);
                    drawBoxes.setDrawingCacheEnabled(true);

                    mainLayout.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (event.getAction() == MotionEvent.ACTION_UP) {
                                if (textView.getVisibility() == View.VISIBLE) {
                                    textView.setVisibility(View.INVISIBLE);
                                } else if (textView.getVisibility() == View.INVISIBLE) {
                                    textView.setVisibility(View.VISIBLE);
                                } else {
                                    Log.e("ShareActivity", "TextView visibility is weird");
                                }

                                return true;
                            }

                            return true;
                        }
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            cancel("Bitmap creation failed");
        }
    }

    private void noImageFound() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, PICK_IMAGE);
    }

    void cancel(String text) {
        if (text != "" && text != null) Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        setResult(Activity.RESULT_CANCELED);
        finish();
    }
}
