
package hear.app.widget;

/**
 * @author Vyshakh, Rahul
 *
 */
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

/**
 *
 *
 * Basic customization can be done in "com.thbs.progressbutton.MasterLayout.java".

 Circle related customization:

 stroke_color.setColor(Color.rgb(0, 161, 234));  // Edit this to change the circle color

 fill_color.setColor(Color.rgb(0, 161, 234));   // Edit this to change the circle fill color
 Icon related customization:

 icon_color.setStyle(Paint.Style.FILL_AND_STROKE);  // Edit this to change the icon color

 final_icon_color.setColor(Color.WHITE);   // Edit this to change the final icon color
 Create new icons using path or use the existing icons in method "iconCreate()"

 Progress arc color can be changed from "com.thbs.progressbutton.CusImage.java" using:

 myPaint.setColor(Color.rgb(0, 161, 234));



 */

public class CusImage extends View {

	private Paint myPaint;
	private Paint myFramePaint;
	public TextView value;
	private float startAngle;
	public float temp;
	float sweepAngle;
	private int flag = 0;
	RectF rect;
	private MasterLayout m;
	int pix = 0;

	public CusImage(Context context, AttributeSet attrs, MasterLayout m) {
		super(context, attrs);
		this.m = m;
		init();
	}

	public CusImage(Context context, MasterLayout m,int pix) {
		super(context);
		this.m = m;
        this.pix=pix;
		init();
	}

	private void init() {

		myPaint = new Paint();
		DisplayMetrics metrics = getContext().getResources()
				.getDisplayMetrics();
		int width = metrics.widthPixels;
		int height = metrics.heightPixels;
		float scarea = width * height;
		//pix = (int) Math.sqrt(scarea * 0.0217);

		myPaint.setAntiAlias(true);
		myPaint.setStyle(Paint.Style.STROKE);
//		myPaint.setColor(Color.rgb(0, 161, 234));  //Edit this to change progress arc color.

        myPaint.setColor(        Color.rgb(0xe8,0xba,0x00)        );

		myPaint.setStrokeWidth(7);

		myFramePaint = new Paint();
		myFramePaint.setAntiAlias(true);
		myFramePaint.setColor(Color.TRANSPARENT);

		float startx = (float) (pix * 0.05);
		float endx = (float) (pix * 0.95);
		float starty = (float) (pix * 0.05);
		float endy = (float) (pix * 0.95);
		rect = new RectF(startx, starty, endx, endy);
	}

	public void setupprogress(int progress) {

		//Updating progress arc 
		
		sweepAngle = (float) (progress * 3.6);
		
	}

	public void reset() {

		//Resetting progress arc
		
		sweepAngle = 0;
		startAngle = -90;
		flag = 1;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int desiredWidth = pix;
		int desiredHeight = pix;
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int width;
		int height;

		
		if (widthMode == MeasureSpec.EXACTLY) {
			
			width = widthSize;
		} else if (widthMode == MeasureSpec.AT_MOST) {
			
			width = Math.min(desiredWidth, widthSize);
		} else {
			
			width = desiredWidth;
		}

		
		if (heightMode == MeasureSpec.EXACTLY) {
			
			height = heightSize;
		} else if (heightMode == MeasureSpec.AT_MOST) {
			
			height = Math.min(desiredHeight, heightSize);
		} else {
			
			height = desiredHeight;
		}

		
		setMeasuredDimension(width, height);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		canvas.drawArc(rect, startAngle, sweepAngle, false, myPaint);
		startAngle = -90;

		if (sweepAngle < 360 && flag == 0) {

			invalidate();

		} else if (flag == 1) {

			sweepAngle = 0;
			startAngle = -90;
			flag = 0;
			invalidate();
		} else {

			sweepAngle = 0;
			startAngle = -90;
			m.finalAnimation();

		}
	}
}
