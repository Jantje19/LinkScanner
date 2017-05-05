package ml.testsite_vic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.SparseArray;
import android.view.View;

import com.google.android.gms.vision.text.TextBlock;

public class DrawBoxes extends View {

    public SparseArray<TextBlock> items;

    public DrawBoxes(Context context, SparseArray<TextBlock> items) {
        super(context);
        this.items = items;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < items.size(); i++) {
            try {
                Paint white = new Paint();
                TextBlock textBlock = items.get(i);
                Rect boundingBox = textBlock.getBoundingBox();

                white.setStrokeWidth(2);
                white.setColor(Color.WHITE);
                white.setStyle(Paint.Style.STROKE);

                try {
                    canvas.drawRect(boundingBox, white);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {}
        }

//        Rect rect = new Rect();
//        rect.set(0, 0, canvas.getWidth(), canvas.getHeight() / 2);
//
//        Paint blue = new Paint();
//        blue.setColor(Color.BLUE);
//        blue.setStyle(Paint.Style.FILL);
//
//        canvas.drawRect(rect, blue);
    }
}
