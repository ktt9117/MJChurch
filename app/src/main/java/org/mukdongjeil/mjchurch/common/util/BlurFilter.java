package org.mukdongjeil.mjchurch.common.util;

import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

public class BlurFilter {

	private static final String TAG = "BlurFilter";

	public static Bitmap toBlur(Bitmap bmpOriginal, int radius) {
		int width, height;
		height = bmpOriginal.getHeight();
		width = bmpOriginal.getWidth();

		Log.v(TAG, "toBlur():w=" + width + ", h=" + height);

		Paint pnt = new Paint();
		pnt.setAntiAlias(true);

		Bitmap bmpEdit = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bmpEdit);
		// canvas.drawColor(Color.WHITE);

		BlurMaskFilter blur = new BlurMaskFilter(radius,
				BlurMaskFilter.Blur.NORMAL);
		pnt.setMaskFilter(blur);
		canvas.drawBitmap(bmpOriginal, 0, 0, pnt);
		return bmpEdit;

		// BlurMaskFilter blurFilter = new BlurMaskFilter(radius,
		// BlurMaskFilter.Blur.NORMAL);
		// Paint shadowPaint = new Paint();
		// shadowPaint.setMaskFilter(blurFilter);
		//
		// int[] offsetXY = new int[2];
		// Bitmap shadowImage = bmpOriginal.extractAlpha(shadowPaint, offsetXY);
		// Bitmap shadowImage32 = shadowImage.copy(Bitmap.Config.ARGB_8888,
		// true);

		// Canvas c = new Canvas(shadowImage32);
		// c.drawBitmap(bmpOriginal, -offsetXY[0], -offsetXY[1], null);

		// return shadowImage; //shadowImage32;
	}

	public static Bitmap fastblur(Bitmap sentBitmap, int radius) {

		// Stack Blur v1.0 from
		// http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
		//
		// Java Author: Mario Klingemann <mario at quasimondo.com>
		// http://incubator.quasimondo.com
		// created Feburary 29, 2004
		// Android port : Yahel Bouaziz <yahel at kayenko.com>
		// http://www.kayenko.com
		// ported april 5th, 2012

		// This is a compromise between Gaussian Blur and Box blur
		// It creates much better looking blurs than Box Blur, but is
		// 7x faster than my Gaussian Blur implementation.
		//
		// I called it Stack Blur because this describes best how this
		// filter works internally: it creates a kind of moving stack
		// of colors whilst scanning through the image. Thereby it
		// just has to add one new block of color to the right side
		// of the stack and remove the leftmost color. The remaining
		// colors on the topmost layer of the stack are either added on
		// or reduced by one, depending on if they are on the right or
		// on the left side of the stack.
		//
		// If you are using this algorithm in your code please add
		// the following line:
		//
		// Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

		Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

		if (radius < 1) {
			return (null);
		}

		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		int[] pix = new int[w * h];
		Log.e("pix", w + " " + h + " " + pix.length);
		bitmap.getPixels(pix, 0, w, 0, 0, w, h);

		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = radius + radius + 1;

		int r[] = new int[wh];
		int g[] = new int[wh];
		int b[] = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		int vmin[] = new int[Math.max(w, h)];

		int divsum = (div + 1) >> 1;
		divsum *= divsum;
		int dv[] = new int[256 * divsum];
		for (i = 0; i < 256 * divsum; i++) {
			dv[i] = (i / divsum);
		}

		yw = yi = 0;

		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;

		for (y = 0; y < h; y++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + Math.min(wm, Math.max(i, 0))];
				sir = stack[i + radius];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rbs = r1 - Math.abs(i);
				rsum += sir[0] * rbs;
				gsum += sir[1] * rbs;
				bsum += sir[2] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}
			}
			stackpointer = radius;

			for (x = 0; x < w; x++) {

				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (y == 0) {
					vmin[x] = Math.min(x + radius + 1, wm);
				}
				p = pix[yw + vmin[x]];

				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[(stackpointer) % div];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi++;
			}
			yw += w;
		}
		for (x = 0; x < w; x++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = Math.max(0, yp) + x;

				sir = stack[i + radius];

				sir[0] = r[yi];
				sir[1] = g[yi];
				sir[2] = b[yi];

				rbs = r1 - Math.abs(i);

				rsum += r[yi] * rbs;
				gsum += g[yi] * rbs;
				bsum += b[yi] * rbs;

				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}

				if (i < hm) {
					yp += w;
				}
			}
			yi = x;
			stackpointer = radius;
			for (y = 0; y < h; y++) {
				// Preserve alpha channel: ( 0xff000000 & pix[yi] )
				pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16)
						| (dv[gsum] << 8) | dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (x == 0) {
					vmin[y] = Math.min(y + r1, hm) * w;
				}
				p = x + vmin[y];

				sir[0] = r[p];
				sir[1] = g[p];
				sir[2] = b[p];

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi += w;
			}
		}

		Log.e("pix", w + " " + h + " " + pix.length);
		bitmap.setPixels(pix, 0, w, 0, 0, w, h);

		return (bitmap);
	}

	/**
	 * generate a blurred bitmap from given one
	 * 
	 * referenced: http://incubator.quasimondo.com/processing/superfastblur.pde
	 * 
	 * @param original
	 * @param radius
	 * @return
	 */
	public static Bitmap getBlurredBitmap(Bitmap original, int radius) {
		if (radius < 1)
			return null;

		int width = original.getWidth();
		int height = original.getHeight();
		int wm = width - 1;
		int hm = height - 1;
		int wh = width * height;
		int div = radius + radius + 1;
		int r[] = new int[wh];
		int g[] = new int[wh];
		int b[] = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, p1, p2, yp, yi, yw;
		int vmin[] = new int[Math.max(width, height)];
		int vmax[] = new int[Math.max(width, height)];
		int dv[] = new int[256 * div];
		for (i = 0; i < 256 * div; i++)
			dv[i] = i / div;

		int[] blurredBitmap = new int[wh];
		original.getPixels(blurredBitmap, 0, width, 0, 0, width, height);

		yw = 0;
		yi = 0;

		for (y = 0; y < height; y++) {
			rsum = 0;
			gsum = 0;
			bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = blurredBitmap[yi + Math.min(wm, Math.max(i, 0))];
				rsum += (p & 0xff0000) >> 16;
				gsum += (p & 0x00ff00) >> 8;
				bsum += p & 0x0000ff;
			}
			for (x = 0; x < width; x++) {
				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];

				if (y == 0) {
					vmin[x] = Math.min(x + radius + 1, wm);
					vmax[x] = Math.max(x - radius, 0);
				}
				p1 = blurredBitmap[yw + vmin[x]];
				p2 = blurredBitmap[yw + vmax[x]];

				rsum += ((p1 & 0xff0000) - (p2 & 0xff0000)) >> 16;
				gsum += ((p1 & 0x00ff00) - (p2 & 0x00ff00)) >> 8;
				bsum += (p1 & 0x0000ff) - (p2 & 0x0000ff);
				yi++;
			}
			yw += width;
		}

		for (x = 0; x < width; x++) {
			rsum = gsum = bsum = 0;
			yp = -radius * width;
			for (i = -radius; i <= radius; i++) {
				yi = Math.max(0, yp) + x;
				rsum += r[yi];
				gsum += g[yi];
				bsum += b[yi];
				yp += width;
			}
			yi = x;
			for (y = 0; y < height; y++) {
				blurredBitmap[yi] = 0xff000000 | (dv[rsum] << 16)
						| (dv[gsum] << 8) | dv[bsum];
				if (x == 0) {
					vmin[y] = Math.min(y + radius + 1, hm) * width;
					vmax[y] = Math.max(y - radius, 0) * width;
				}
				p1 = x + vmin[y];
				p2 = x + vmax[y];

				rsum += r[p1] - r[p2];
				gsum += g[p1] - g[p2];
				bsum += b[p1] - b[p2];

				yi += width;
			}
		}

		return Bitmap.createBitmap(blurredBitmap, width, height,
				Bitmap.Config.ARGB_8888);
	}

	final static int KERNAL_WIDTH = 7;
	final static int KERNAL_HEIGHT = 7;

	public static int[][] kernal_GaussianMatrix = {
			{ 67, 2292, 19117, 38771, 19117, 2292, 67 },
			{ 2292, 78633, 655965, 1330373, 655965, 78633, 2292 },
			{ 19117, 655965, 5472157, 11098164, 5472157, 655965, 19117 },
			{ 38771, 1330373, 11098164, 22508352, 11098164, 1330373, 38771 },
			{ 19117, 655965, 5472157, 11098164, 5472157, 655965, 19117 },
			{ 2292, 78633, 655965, 1330373, 655965, 78633, 2292 },
			{ 67, 2292, 19117, 38771, 19117, 2292, 67 } };

	public static Bitmap processingBitmap(Bitmap src, int[][] knl) {
		Bitmap dest = Bitmap.createBitmap(src.getWidth(), src.getHeight(),
				src.getConfig());

		int bmWidth = src.getWidth();
		int bmHeight = src.getHeight();
		int bmWidth_MINUS_6 = bmWidth - 6;
		int bmHeight_MINUS_6 = bmHeight - 6;
		int bmWidth_OFFSET_3 = 3;
		int bmHeight_OFFSET_3 = 3;

		for (int i = bmWidth_OFFSET_3; i <= bmWidth_MINUS_6; i++) {
			for (int j = bmHeight_OFFSET_3; j <= bmHeight_MINUS_6; j++) {

				// get the surround 7*7 pixel of current src[i][j] into a matrix
				// subSrc[][]
				int[][] subSrc = new int[KERNAL_WIDTH][KERNAL_HEIGHT];
				for (int k = 0; k < KERNAL_WIDTH; k++) {
					for (int l = 0; l < KERNAL_HEIGHT; l++) {
						subSrc[k][l] = src.getPixel(i - bmWidth_OFFSET_3 + k, j
								- bmHeight_OFFSET_3 + l);
					}
				}

				// subSum = subSrc[][] * knl[][]
				long subSumR = 0;
				long subSumG = 0;
				long subSumB = 0;

				for (int k = 0; k < KERNAL_WIDTH; k++) {
					for (int l = 0; l < KERNAL_HEIGHT; l++) {
						subSumR += (long) (Color.red(subSrc[k][l]))
								* (long) (knl[k][l]);
						subSumG += (long) (Color.green(subSrc[k][l]))
								* (long) (knl[k][l]);
						subSumB += (long) (Color.blue(subSrc[k][l]))
								* (long) (knl[k][l]);
					}
				}

				subSumR = subSumR / 100000000;
				subSumG = subSumG / 100000000;
				subSumB = subSumB / 100000000;

				if (subSumR < 0) {
					subSumR = 0;
				} else if (subSumR > 255) {
					subSumR = 255;
				}

				if (subSumG < 0) {
					subSumG = 0;
				} else if (subSumG > 255) {
					subSumG = 255;
				}

				if (subSumB < 0) {
					subSumB = 0;
				} else if (subSumB > 255) {
					subSumB = 255;
				}

				dest.setPixel(i, j, Color.argb(Color.alpha(src.getPixel(i, j)),
						(int) subSumR, (int) subSumG, (int) subSumB));
			}
		}

		return dest;
	}

}