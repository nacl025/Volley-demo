/**
 * Copyright 2013 Mani Selvaraj
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mani.volleydemo;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.mani.volleydemo.toolbox.BitmapLruCache;
import com.mani.volleydemo.toolbox.DiskBitmapCache;
import com.mani.volleydemo.toolbox.FadeInImageListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Demonstrates how to execute ImageRequest and NetworkImageView to download a image from a URL using Volley library.
 * @author Mani Selvaraj
 *
 */

public class NetworkImageActivity extends Activity implements View.OnClickListener {

	private Button mTrigger;
	private RequestQueue mVolleyQueue;
	private ListView mListView;
	private ImageView mImageView1;
	private ImageView mImageView2;
	private ImageView mImageView3;
	private NetworkImageView mNetworkImageView;
	private EfficientAdapter mAdapter;
	private ProgressDialog mProgress;
	private List<DataModel> mDataList;
	
	private ImageLoader mImageLoader;
	private ImageLoader mImageLoader2;
	
	private final String TAG_REQUEST = "MY_TAG";
	String testUrlToDownloadImage1 = "http://img.netbian.com/file/2018/1105/d0b56b378c98bf2de7c8457b82ea0259.jpg";
	String testUrlToDownloadImage2 = "http://img.netbian.com/file/2018/1017/e9ecf76ba0d6b38de16d309808698c83.jpg";
	String testUrlToDownloadImage23 = "https://gss0.baidu.com/-fo3dSag_xI4khGko9WTAnF6hhy/zhidao/wh%3D600%2C800/sign=4345736da586c91708565a3ff90d5cf7/2fdda3cc7cd98d10d7669ba4233fb80e7aec90f1.jpg";

	// 获取到可用内存的最大值，使用内存超出这个值会引起OutOfMemory异常。
	// LruCache通过构造函数传入缓存值，以KB为单位。
	int maxMemory = (int) (Runtime.getRuntime().maxMemory());
	// 使用最大可用内存值的1/8作为缓存的大小。
	int cacheSize = maxMemory / 8;

	int max_cache_size = 10000000;

	private class DataModel {
		private String mImageUrl;
		private String mTitle;
		
		public String getImageUrl() {
			return mImageUrl;
		}
		public void setImageUrl(String mImageUrl) {
			this.mImageUrl = mImageUrl;
		}
		public String getTitle() {
			return mTitle;
		}
		public void setTitle(String mTitle) {
			this.mTitle = mTitle;
		}
		
	}

	JsonObjectRequest jsonObjRequest;
	BitmapLruCache mBitmapLruCache;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.networkimage_layout);
		
		actionBarSetup();
		
		// Initialise Volley Request Queue. 
		mVolleyQueue = Volley.newRequestQueue(this);

		findViewById(R.id.button).setOnClickListener(this);
		mDataList = new ArrayList<DataModel>();
		mListView = (ListView) findViewById(R.id.image_list);
		mImageView1 = (ImageView) findViewById(R.id.imageview1);
		mImageView2 = (ImageView) findViewById(R.id.imageview2);
		mImageView3 = (ImageView) findViewById(R.id.imageview3);
		mNetworkImageView = (NetworkImageView) findViewById(R.id.networkimageview);
		mTrigger = (Button) findViewById(R.id.send_http);
		mAdapter = new EfficientAdapter(this);
		mListView.setAdapter(mAdapter);
		mTrigger.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showProgress();
				makeSampleHttpRequest();
			}
		});


		mBitmapLruCache = new BitmapLruCache(cacheSize);

		mImageLoader = new ImageLoader(mVolleyQueue, new DiskBitmapCache(getCacheDir(),max_cache_size));
		mImageLoader.get(testUrlToDownloadImage1,
				ImageLoader.getImageListener(mImageView1,
						R.drawable.flickr,
						android.R.drawable.ic_dialog_alert),
				50, 50);


		mImageLoader2 = new ImageLoader(mVolleyQueue, mBitmapLruCache);
		mImageLoader2.get(testUrlToDownloadImage2, new FadeInImageListener(mImageView2,this));

		ImageRequest imgRequest = new ImageRequest(testUrlToDownloadImage2, new Response.Listener<Bitmap>() {
				@Override
				public void onResponse(Bitmap response) {
					mImageView3.setImageBitmap(response);
				}
			}, 0, 0, Bitmap.Config.ARGB_8888, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					mImageView3.setImageResource(R.drawable.ic_launcher);
				}
			});
		mVolleyQueue.add(imgRequest);

		mNetworkImageView.setImageUrl(testUrlToDownloadImage1, mImageLoader);
	}

	@Override
	public void onClick(View v) {
		mImageLoader = new ImageLoader(mVolleyQueue, new DiskBitmapCache(getCacheDir(),max_cache_size));
		mImageLoader.get(testUrlToDownloadImage1,
				ImageLoader.getImageListener(mImageView1,
						R.drawable.flickr,
						android.R.drawable.ic_dialog_alert),
				50, 50);



		mImageLoader2 = new ImageLoader(mVolleyQueue, mBitmapLruCache);
		mImageLoader2.get(testUrlToDownloadImage2, new FadeInImageListener(mImageView2,this));
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void actionBarSetup() {
	  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	    ActionBar ab = getActionBar();
	    ab.setTitle("ImageLoading");
	  }
	}

	public void onDestroy() {
		super.onDestroy();
		System.out.println("######### onDestroy ######### "+mAdapter);
	}
	
	public void onStop() {
		super.onStop();
		if(mProgress != null)
			mProgress.dismiss();
		// Keep the list of requests dispatched in a List<Request<T>> mRequestList;
		/*
		 for( Request<T> req : mRequestList) {
		 	req.cancel();
		 }
		 */
		//jsonObjRequest.cancel();
		//( or )
		//mVolleyQueue.cancelAll(TAG_REQUEST);
	}

	private void makeSampleHttpRequest() {
		
		String url = "https://api.flickr.com/services/rest";
		Uri.Builder builder = Uri.parse(url).buildUpon();
		builder.appendQueryParameter("api_key", "5e045abd4baba4bbcd866e1864ca9d7b");
		builder.appendQueryParameter("method", "flickr.interestingness.getList");
		builder.appendQueryParameter("format", "json");
		builder.appendQueryParameter("nojsoncallback", "1");
		
		
		jsonObjRequest = new JsonObjectRequest(Request.Method.GET, builder.toString(), null, new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				System.out.println("####### Response JsonObjectRequest SUCCESS  ######## "+response.toString());
				try {
					parseFlickrImageResponse(response);
					mAdapter.notifyDataSetChanged();
				} catch (Exception e) {
					e.printStackTrace();
					showToast("JSON parse error");
				}
				stopProgress();
			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				stopProgress();
				System.out.println("####### onErrorResponse ########## "+error.getMessage()); 
				showToast(error.getMessage());
			}
		});

		jsonObjRequest.setTag(TAG_REQUEST);	
		mVolleyQueue.add(jsonObjRequest);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void showProgress() {
		mProgress = ProgressDialog.show(this, "", "Loading...");
	}
	
	private void stopProgress() {
		mProgress.cancel();
	}
	
	private void showToast(String msg) {
		Toast.makeText(NetworkImageActivity.this, msg, Toast.LENGTH_LONG).show();
	}
	
	private void parseFlickrImageResponse(JSONObject response) throws JSONException {
		System.out.println("#######  parseFlickrImageResponse   ######## "+mAdapter);
		if(response.has("photos")) {
			try {
				JSONObject photos = response.getJSONObject("photos");
				JSONArray items = photos.getJSONArray("photo");

				mDataList.clear();
				
				for(int index = 0 ; index < items.length(); index++) {
				
					JSONObject jsonObj = items.getJSONObject(index);
					
					String farm = jsonObj.getString("farm");
					String id = jsonObj.getString("id");
					String secret = jsonObj.getString("secret");
					String server = jsonObj.getString("server");
					
					String imageUrl = "https://farm" + farm + ".static.flickr.com/" + server + "/" + id + "_" + secret + "_t.jpg";
					DataModel model = new DataModel();
					model.setImageUrl(imageUrl);
					model.setTitle(jsonObj.getString("title"));
					mDataList.add(model);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private  class EfficientAdapter extends BaseAdapter {
		
        private LayoutInflater mInflater;
        
        public EfficientAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        public int getCount() {
            return mDataList.size();
        }
        
        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            System.out.println("#######  getView   ########### "+position); 
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.networkimage_list_item, null);
                holder = new ViewHolder();
                holder.image = (NetworkImageView) convertView.findViewById(R.id.image);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            
            holder.title.setText(mDataList.get(position).getTitle());
            // As contrast to 
            holder.image.setImageUrl(mDataList.get(position).getImageUrl(),mImageLoader);
            return convertView;
        }
        
        class ViewHolder {
            TextView title;
            NetworkImageView image;
        }	
        
	}	
}
