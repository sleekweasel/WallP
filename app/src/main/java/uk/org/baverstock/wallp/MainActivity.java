package uk.org.baverstock.wallp;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;


public class MainActivity extends Activity {

    private static final int REQUEST_CODE = 1;
    public static final int PTS = 4;
    private Bitmap bitmap;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.paper);
        imageView.setOnTouchListener(new DragMatrixTouchListener(findViewById(R.id.stuntWallpaper)));

        acquireOrRequestBitmap();

        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSystemWallpaper(bitmap, imageView.getImageMatrix());
            }
        });
        findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetMatrix();
            }
        });
        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void hideActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }
        }
    }

    public int getStatusBarHeight() {
        int statusBarHeight = 0;

        if (!hasOnScreenSystemBar()) {
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            }
        }

        return statusBarHeight;
    }

    private boolean hasOnScreenSystemBar() {
        Display display = getWindowManager().getDefaultDisplay();
        int rawDisplayHeight = 0;
        try {
            Method getRawHeight = Display.class.getMethod("getRawHeight");
            rawDisplayHeight = (Integer) getRawHeight.invoke(display);
        } catch (Exception ex) {
        }

        int UIRequestedHeight = display.getHeight();

        return rawDisplayHeight - UIRequestedHeight > 0;
    }

    private void setSystemWallpaper(Bitmap bitmap, Matrix imageMatrix) {
        Point size = new Point();
        getDisplaySize(size);
        WallpaperManager wm = WallpaperManager.getInstance(this);
        int wwidth = wm.getDesiredMinimumWidth();
        int wheight = wm.getDesiredMinimumHeight();
        Bitmap wallpaper = Bitmap.createBitmap(wwidth, wheight, bitmap.getConfig());
        Canvas canvas = new Canvas(wallpaper);
        canvas.drawARGB(128, 128, 0, 0);
        canvas.translate(wwidth / 2 - size.x / 2, wheight / 2 - size.y / 2 + getStatusBarHeight());
        canvas.drawBitmap(bitmap, imageMatrix, null);
        try {
            Toast.makeText(this, "Setting wallpaper", Toast.LENGTH_SHORT).show();
            wm.setBitmap(wallpaper);
        } catch (IOException e) {
            Toast.makeText(this, "Error reverting wallpaper - " + e, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void getDisplaySize(Point size) {
        getWindow().getWindowManager().getDefaultDisplay().getSize(size);
    }

    private void acquireOrRequestBitmap() {
        String action = getIntent().getAction();
        if (action.endsWith("MAIN")) {
            if (getIntent().getData() != null) {
                //  adb shell am start
                //          -a android.intent.action.MAIN
                //          -c android.intent.category.LAUNCHER
                //          -d http://whatever/you/want
                //          -n uk.org.baverstock.wallp/.MainActivity
                setBitmapFromText(getIntent().getDataString().replace("http://", "").replaceAll("/", "\n"));
                setSystemWallpaper(bitmap, imageView.getImageMatrix());
            } else {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_CODE);
            }
        } else if (action.endsWith("ATTACH_DATA")) {
            setBitmapFromUri(getIntent().getData());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            setBitmapFromUri(uri);
        } else if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_CANCELED) {
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setBitmapFromUri(Uri uri) {
        try {
            if (bitmap != null) {
                bitmap.recycle();
            }
            InputStream stream = getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(stream);
            stream.close();
            imageView.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "File not found: " + e, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Problem with file: " + e, Toast.LENGTH_SHORT).show();
        }
    }

    private void setBitmapFromText(String text) {
        if (bitmap != null) {
            bitmap.recycle();
        }
        Point size = new Point();
        getDisplaySize(size);
        bitmap = Bitmap.createBitmap(size.x, size.y, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawARGB(255, 28, 28, 64);
        canvas.translate(size.x / 2, size.y / 2 + getStatusBarHeight());
        TextPaint p = new TextPaint();
        p.setColor(Color.BLUE);
        p.setStrokeWidth(10);
        canvas.drawLine(-size.x, 0, size.x, 0, p);
        canvas.drawLine(-size.x, -size.y, size.x, size.y, p);
        p.setColor(Color.YELLOW);
        p.setTextSize(1);
        p.setFakeBoldText(true);
        StaticLayout layout = new StaticLayout(
                text, p, size.x, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
        float width = layout.getLineWidth(0);
        for (int i = layout.getLineCount()-1; i > 0; --i) {
            width = Math.max(width, layout.getLineWidth(i));
        }
        p.setTextSize(Math.min(size.x / width, size.y / (layout.getLineCount() * p.getFontSpacing())));
        layout = new StaticLayout(
                text, p, canvas.getWidth(), Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
        canvas.save();
        canvas.translate(-layout.getWidth() / 2, -layout.getHeight() / 2);
        layout.draw(canvas);
        canvas.restore();
        imageView.setImageBitmap(bitmap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Title");
            final EditText input = new EditText(this);
            input.setText("Example Text");

            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setBitmapFromText(input.getText().toString());
                    resetMatrix();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void resetMatrix() {
        imageView.setImageMatrix(new Matrix());
    }

    private static class DragMatrixTouchListener implements View.OnTouchListener {

        private final View stuntWallpaper;

        private Matrix matrix;
        private Matrix originalMatrix;
        public float[] src = new float[PTS * 2];
        public float[] dst = new float[PTS * 2];
        public boolean setSrc = false;

        public DragMatrixTouchListener(View stuntWallpaper) {
            this.stuntWallpaper = stuntWallpaper;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageView imageView = (ImageView) v;
            int actionIndex = event.getActionIndex();
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    originalMatrix = matrix = new Matrix(imageView.getImageMatrix());
                case MotionEvent.ACTION_POINTER_DOWN:
                    if (actionIndex < PTS) {
                        setSrc = true;
                    }
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    imageView.setImageMatrix(originalMatrix);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    setSrc = true;
                    matrix = new Matrix(imageView.getImageMatrix());
                    return true;

                case MotionEvent.ACTION_MOVE:
                    if (setSrc) {
                        setSrc = false;
                        setCoords(event, src);
                    }
                    try {
                        setCoords(event, dst);
                        Matrix twist = new Matrix();
                        if (twist.setPolyToPoly(src, 0, dst, 0, event.getPointerCount())) {
                            twist.preConcat(matrix);
                            imageView.setImageMatrix(twist);
                        }
                    } catch (Throwable t) {
                        Log.e(this.getClass().getCanonicalName(), "TMOVE threw", t);
                    }
                    return true;
            }
            return false;
        }

        private void setCoords(MotionEvent event, float[] coords) {
            int pointerCount = Math.min(event.getPointerCount(), PTS);
            for (int i = 0; i < pointerCount; ++i) {
                coords[i * 2 + 0] = event.getX(i);
                coords[i * 2 + 1] = event.getY(i);
            }
        }
    }
}