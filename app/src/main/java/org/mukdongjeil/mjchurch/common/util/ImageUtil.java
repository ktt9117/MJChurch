package org.mukdongjeil.mjchurch.common.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.text.TextUtils;
import android.transition.Transition;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.request.target.SquaringDrawable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class ImageUtil {
	
	private static final String TAG = "ImageUtil";
	
	public static Bitmap getImage(String path, int sampleWidth) throws IOException, FileNotFoundException {
		SizeInfo size = getSizeFixScreenWidth(path, 0);
		return getOptimizeScreennailImage(path, size.width, size.height, sampleWidth);
	}
	
	public static synchronized Bitmap getOptimizeScreennailImage(String path, float width, float height, int sampleWidth) {
		if(path == null) {
			return null;
		}
		File file = new File(path);
		if(!file.exists()) {
			return null;
		}
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		
		int outWidth = options.outWidth;
		int outHeight = options.outHeight;
		int degrees = getExifOrientation(path);
		if(degrees == 90 || degrees == 270) {
			outWidth = options.outHeight;
			outHeight = options.outWidth;
		}
		
		if(sampleWidth > 0) {
			double sampleSize = outWidth / sampleWidth;
			options.inSampleSize = (int)Math.pow(2d, Math.floor(Math.log(sampleSize)/Math.log(2d)));
		} else {
			float widthScale = outWidth / width;
			float heightScale = outHeight / height;
			float scale = 0;
			if(widthScale > heightScale) {
				if(widthScale > 0) {
					scale = widthScale; 
					if((outHeight / scale) < height) {
						scale = heightScale;
					}
				}
			} else {
				if(heightScale > 0) {
					scale = heightScale;
					if((outWidth / scale) < width) {
						scale = widthScale;
					}
				}
			}
			if(scale > 0) { options.inSampleSize = (int)(((scale = Math.round(scale)) %2) == 0 ? scale : scale-1); }
		}
		
		options.inPreferredConfig = Config.RGB_565;
		options.inJustDecodeBounds = false;
		options.inPurgeable = true;
		options.inDither = true;
//		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeFile(path, options);
		} catch (OutOfMemoryError e) {
			Logger.e(TAG, "getOptimizeScreennailImage():OutOfMemoryError=" + e);
			return null;
		}
		if(bitmap != null) {
			if(degrees != 0) {
				Bitmap bitmap1 = getRotatedBitmap(bitmap, degrees);
				if(bitmap != bitmap1) {
					bitmap.recycle();
					bitmap = bitmap1;
				}
			}
			
			float dstWidth = width;
			float dstHeight = height;
			int srcWidth = bitmap.getWidth();
			int srcHeight = bitmap.getHeight();
			float rateWidth = width / (float)srcWidth;
			float rateHeight = height / (float)srcHeight;
			if(rateWidth > rateHeight) {
				dstWidth = (int)(srcWidth * rateWidth);
				dstHeight = (int)(srcHeight * rateWidth);
			} else {
				dstWidth = (int)(srcWidth * rateHeight);
				dstHeight = (int)(srcHeight * rateHeight);
			}
			
			try {
				Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap, Math.round(dstWidth), Math.round(dstHeight), true);
				if(bitmap != bitmap2) {
					bitmap.recycle();
					bitmap = bitmap2;
				}
			} catch (OutOfMemoryError e) {
				// TODO: handle exception
				Logger.e(TAG, "getOptimizeScreennailImage():OutOfMemoryError=" + e);
			}
		}
		
		return bitmap;
	}
	
	/**
	 * 리사이즈 비율이 작은쪽에 맞춰서 리사이즈를 한다.
	 * @param path
	 * @param width
	 * @param height
	 * @param sampleWidth
	 * @return
	 */
	public static synchronized Bitmap getOptimizeScreennailImageForSmall(String path, float width, float height, int sampleWidth) {
		if(path == null) {
			return null;
		}
		File file = new File(path);
		if(!file.exists()) {
			return null;
		}
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		
		int outWidth = options.outWidth;
		int outHeight = options.outHeight;
		int degrees = getExifOrientation(path);
		if(degrees == 90 || degrees == 270) {
			outWidth = options.outHeight;
			outHeight = options.outWidth;
		}
		
		if(sampleWidth > 0) {
			double sampleSize = outWidth / sampleWidth;
			options.inSampleSize = (int)Math.pow(2d, Math.floor(Math.log(sampleSize)/Math.log(2d)));
		} else {
			float widthScale = outWidth / width;
			float heightScale = outHeight / height;
			float scale = 0;
			if(widthScale > heightScale) {
				if(widthScale > 0) {
					scale = widthScale; 
					if((outHeight / scale) > height) {
						scale = heightScale;
					}
				}
			} else {
				if(heightScale > 0) {
					scale = heightScale;
					if((outWidth / scale) > width) {
						scale = widthScale;
					}
				}
			}
			if(scale > 0) { options.inSampleSize = (int)(((scale = Math.round(scale)) %2) == 0 ? scale : scale-1); }
		}
		
		options.inPreferredConfig = Config.RGB_565;
		options.inJustDecodeBounds = false;
		options.inPurgeable = true;
		options.inDither = true;
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeFile(path, options);
		} catch (OutOfMemoryError e) {
			Logger.e(TAG, "getOptimizeScreennailImage():OutOfMemoryError=" + e);
			return null;
		}
		if(bitmap != null) {
			if(degrees != 0) {
				Bitmap bitmap1 = getRotatedBitmap(bitmap, degrees);
				if(bitmap != bitmap1) {
					bitmap.recycle();
					bitmap = bitmap1;
				}
			}
			
			float dstWidth = width;
			float dstHeight = height;
			int srcWidth = bitmap.getWidth();
			int srcHeight = bitmap.getHeight();
			float rateWidth = width / (float)srcWidth;
			float rateHeight = height / (float)srcHeight;
			
			if(rateWidth < rateHeight) {
				dstWidth = (int)(srcWidth * rateWidth);
				dstHeight = (int)(srcHeight * rateWidth);
			} else {
				dstWidth = (int)(srcWidth * rateHeight);
				dstHeight = (int)(srcHeight * rateHeight);
			}
			
			try {
				Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap, Math.round(dstWidth), Math.round(dstHeight), true);
				if(bitmap != bitmap2) {
					bitmap.recycle();
					bitmap = bitmap2;
				}
			} catch (OutOfMemoryError e) {
				// TODO: handle exception
				Logger.e(TAG, "getOptimizeScreennailImage():OutOfMemoryError=" + e);
			}
		}
		
		return bitmap;
	}
	
	public static synchronized Bitmap getOptimizeScreennailImageForSmall(Bitmap source, float width, float height, int sampleWidth) {
		if(source == null) { return null; }
		
		float dstWidth = width;
		float dstHeight = height;
		int srcWidth = source.getWidth();
		int srcHeight = source.getHeight();
		float rateWidth = width / (float)srcWidth;
		float rateHeight = height / (float)srcHeight;
		
		if(rateWidth < rateHeight) {
			dstWidth = (int)(srcWidth * rateWidth);
			dstHeight = (int)(srcHeight * rateWidth);
		} else {
			dstWidth = (int)(srcWidth * rateHeight);
			dstHeight = (int)(srcHeight * rateHeight);
		}
		
		Bitmap bitmap = null;
		try {
			bitmap = Bitmap.createScaledBitmap(source, Math.round(dstWidth), Math.round(dstHeight), true);
		} catch (OutOfMemoryError e) {
			// TODO: handle exception
			Logger.e(TAG, "getOptimizeScreennailImage():OutOfMemoryError=" + e);
		}
		
		return bitmap;
	}
	
    public static final Bitmap getThumbnail(String path, int targetWidth, int targetHeight)
    {
    	BitmapFactory.Options mOptions = new BitmapFactory.Options();
    	
    	mOptions.inDither = false;
    	mOptions.inJustDecodeBounds = true;
    	
    	BitmapFactory.decodeFile(path, mOptions);
    	
    	int degrees = getExifOrientation(path);
    	Boolean mScaleByHeight = Math.abs(mOptions.outHeight - targetHeight) >= Math.abs(mOptions.outWidth - targetWidth);
    	if(mOptions.outHeight * mOptions.outWidth * 2 >= 16384)
    	{
    		double mSampleSize = mScaleByHeight
    				? mOptions.outHeight / targetHeight
    						: mOptions.outWidth / targetWidth;
    		
    		mOptions.inSampleSize = (int) Math.pow(2d, Math.floor(Math.log(mSampleSize) / Math.log(2d)));
    	}

    	mOptions.inJustDecodeBounds = false;
    	mOptions.inTempStorage = new byte[16*1024];

    	Bitmap bitmap = BitmapFactory.decodeFile(path, mOptions);
    	if (degrees != 0 && bitmap != null) {
    		bitmap = getRotatedBitmap(bitmap, degrees);
    	}

    	return bitmap;
    }
    
	public static final Bitmap getThumbnail(Context context, String path, int sampleWidth) throws IOException, FileNotFoundException {
		Logger.v(TAG, "getThumbnail():path=" + path);
		if (TextUtils.isEmpty(path)) {
			return null;
		}
		Uri uri = null;
		if(!StringUtils.isFileUri(path)) {
			path = StringUtils.setPrefixforFileUri(path);
		}
		uri = Uri.parse(path);
			
		InputStream is = null;
		InputStream is2 = null;
        BitmapFactory.Options options = null;
        try {
            // retrieve image's infomation
            is = context.getContentResolver().openInputStream(uri);
            options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
        } finally {
            try {
            	if (is != null)
            		is.close();
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
        try {
            BitmapFactory.Options options2 = new BitmapFactory.Options();
            float outWidth = options.outWidth;
            int degrees = getExifOrientation(path.substring(StringUtils.PREFIX_FILE_URL.length()));
            if(degrees == 90 || degrees == 270) {
            	outWidth = options.outHeight;
            }
            int scale = (int)(outWidth / sampleWidth);
            options2.inSampleSize = scale;
            is2 = context.getContentResolver().openInputStream(uri);
            
            Bitmap bitmap = BitmapFactory.decodeStream(is2, null, options2);
            if(bitmap != null) {
            	Bitmap rotate = getRotatedBitmap(bitmap, degrees);
            
            	if(rotate != null && bitmap != rotate) {
            		bitmap.recycle();
            		bitmap = rotate;
            	}
            }
            
            return bitmap;
        } finally {
            try {
            	if (is2 != null)
            		is2.close();
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
	}

	public static String getLastImagePath(ContentResolver resolver) {
        Uri uri = Images.Media.EXTERNAL_CONTENT_URI;
        Uri query = uri.buildUpon().appendQueryParameter("limit", "1").build();
        String[] projection = new String[] {ImageColumns._ID, ImageColumns.DATA, ImageColumns.DATE_ADDED};
        String selection = ImageColumns.MIME_TYPE + "='image/jpeg'";
        String order = ImageColumns.DATE_ADDED + " DESC," + ImageColumns._ID + " DESC";
        Cursor cursor = null;
        try {
            cursor = resolver.query(query, projection, selection, null, order);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(1);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }
	
    /**
	 * maxWidth 에 맞춰 리사이즈
	 * 원본 bitmap 이 더 클 경우 원본 리턴
	 */
	public static Bitmap getResizeBitmapImage(Bitmap source, int maxWidth) {
		if (source == null) {
			return null;
		}
		
		int width = source.getWidth();
	    int height = source.getHeight();
	    int newWidth = width;
	    int newHeight = height;
	    float rate = 0.0f;
	    
	    if (width < maxWidth) {
	    	rate = maxWidth / (float) width;
	    	newHeight = (int) (height * rate);
	    	newWidth = maxWidth;
	    	Bitmap newBitmap = Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
	    	if (source != null) {
	    		source.recycle();
	    	}
	    	return newBitmap;
	    } else {
	    	return source;
	    }
	}

    public synchronized static Bitmap getRotatedBitmap(Bitmap bitmap, int degrees) {
    	if (degrees != 0 && bitmap != null) {
    		Matrix m = new Matrix();
    		m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2 );
    		try {
    			return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    		} catch (OutOfMemoryError ex) {
    			// We have no memory to rotate. Return the original bitmap.
    			Logger.e(TAG, "getRotatedBitmap():OutOfMemoryError=" + ex);
    		}
    	}
    	return bitmap;
    }
	
	public static SizeInfo getSizeFixScreenWidth(String path, int paddingWidth) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, opts);
		
		return getSizeFixScreenWidth(opts.outWidth, opts.outHeight, paddingWidth);
	}
	
	public static SizeInfo getSizeFixScreenWidth(int width, int height, int paddingWidth) {
		int screenWidth = DisplayUtil.getDisplaySizeWidth(SystemHelpers.getApplicationContext());
		int adjustWidth = screenWidth - paddingWidth;
		float scale = adjustWidth / (float)width;
		
		return new SizeInfo(Math.round(width * scale), Math.round(height * scale));
	}
	
    public synchronized static int getExifOrientation(String filepath) {
    	int degree = 0;
    	ExifInterface exif = null;
    	
    	try {
    		exif = new ExifInterface(filepath);
		} catch (IOException e) {
			Logger.e(TAG, "cannot read exif");
			e.printStackTrace();
		}

    	if (exif != null) {
    		int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
        	if (orientation != -1) {
        		// We only recognize a subset of orientation tag values.
        		switch(orientation) {
        			case ExifInterface.ORIENTATION_ROTATE_90 :
        				degree = 90;
        				break;
        			case ExifInterface.ORIENTATION_ROTATE_180 :
        				degree = 180;
        				break;
        			case ExifInterface.ORIENTATION_ROTATE_270 :
        				degree = 270;
        				break;
        		}
        	}
    	}
    	return degree;
    }

	public static Bitmap convertDrawableToBitmap(Drawable drawable) {
		if (drawable == null) {
			Logger.e(TAG, "convertDrawableToBitmap params error : cannot convert null object to Bitmap");
			return null;
		}
		Bitmap bitmap = null;
		if (drawable instanceof GlideBitmapDrawable) {
			bitmap = ((GlideBitmapDrawable) drawable).getBitmap();
		} else if (drawable instanceof TransitionDrawable) {
			TransitionDrawable transitionDrawable = (TransitionDrawable) drawable;
			int length = transitionDrawable.getNumberOfLayers();
			for (int i = 0; i < length; ++i) {
				Drawable child = transitionDrawable.getDrawable(i);
				if (child instanceof GlideBitmapDrawable) {
					bitmap = ((GlideBitmapDrawable) child).getBitmap();
					break;
				} else if (child instanceof SquaringDrawable
						&& child.getCurrent() instanceof GlideBitmapDrawable) {
					bitmap = ((GlideBitmapDrawable) child.getCurrent()).getBitmap();
					break;
				}
			}
		} else if (drawable instanceof SquaringDrawable) {
			bitmap = ((GlideBitmapDrawable) drawable.getCurrent()).getBitmap();
		}

		return bitmap;
	}

	public static class SizeInfo {
    	public int width;
    	public int height;
    	
		public SizeInfo(int width, int height) {
			this.width = width;
			this.height = height;
		}
    }
}
