package com.fiona.labs.dailyselfie;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ItemListAdapter extends BaseAdapter{

	private static final String TAG = "ItemListAdapter";
	
	private ArrayList<String> list = new ArrayList<String>();
	private static LayoutInflater inflater = null;
	private Context mContext;
	
	public ItemListAdapter(Context context) {
		mContext = context;
		inflater = LayoutInflater.from(mContext);
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View newView = convertView;
		ViewHolder holder;

		if (null == convertView) {
			holder = new ViewHolder();
			newView = inflater
					.inflate(R.layout.image_list_view, parent, false);
			holder.image = (ImageView) newView.findViewById(R.id.image_bitmap);
			holder.timeStamp = (TextView) newView.findViewById(R.id.image_timeStamp);
			newView.setTag(holder);
		} else {
			holder = (ViewHolder) newView.getTag();
		}

		
		String filePath = list.get(position);
		Log.i(TAG, "position=" + position);
		Log.i(TAG, "filePath=" + filePath);
		holder.image.setImageBitmap(getBitmap(holder.image, filePath));
		
		int first_ = filePath.indexOf("SELFIE_")+7;
		int second_ = filePath.indexOf("_", first_)+1;
		int third_ = filePath.indexOf("_", second_);
		holder.timeStamp.setText(filePath.substring(first_, third_));
		
		return newView;
	}

	static class ViewHolder {
		ImageView image;
		TextView timeStamp;
	}
	
	private Bitmap getBitmap(ImageView mImageView, String mCurrentPhotoPath) {
	    // Get the dimensions of the View
	    int targetW = mImageView.getLayoutParams().width;
	    int targetH = mImageView.getLayoutParams().height;
	    
	    // Get the dimensions of the bitmap
	    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
	    bmOptions.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
	    int photoW = bmOptions.outWidth;
	    int photoH = bmOptions.outHeight;

	    // Determine how much to scale down the image
	    int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

	    // Decode the image file into a Bitmap sized to fill the View
	    bmOptions.inJustDecodeBounds = false;
	    bmOptions.inSampleSize = scaleFactor;
	    bmOptions.inPurgeable = true;

	    return BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
	    
	}
	
	public void add(String item) {
		list.add(item);
		notifyDataSetChanged();
	}
	
	public ArrayList<String> getList() {
		return list;
	}

	public void remove(int position) {
		list.remove(position);
		notifyDataSetChanged();
	}
	
	public void removeAllViews() {
		list.clear();
		this.notifyDataSetChanged();
	}
}
