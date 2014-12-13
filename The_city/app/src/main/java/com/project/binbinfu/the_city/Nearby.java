package com.project.binbinfu.the_city;

import com.google.android.gms.maps.model.LatLng;

        import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
        import java.net.URL;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.List;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
        import android.support.v4.app.FragmentActivity;
        import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
        import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
        import com.google.android.gms.maps.SupportMapFragment;
        import com.google.android.gms.maps.model.BitmapDescriptorFactory;
        import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
        import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.FIFOLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;


public class Nearby extends FragmentActivity {

    GoogleMap map;
    ArrayList<Marker_List> markerPoints= new ArrayList<Marker_List>();;
    public static double my_latitude;
    public static double my_longitude;
    public int flag = 0;
    public static Nearby_Streams streams;
    private Marker marker;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby);
        initImageLoader();
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.ic_launcher)
                .cacheInMemory()
                .cacheOnDisc().bitmapConfig(Bitmap.Config.RGB_565).build();


        // Getting reference to SupportMapFragment of the activity_main
        SupportMapFragment fm = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);

        // Getting Map for the SupportMapFragment
        map = fm.getMap();
        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location arg0) {
                my_latitude = arg0.getLatitude();
                my_longitude = arg0.getLongitude();
                flag =1;
                //Log.v("ann", "" + my_latitude);
                //Log.v("ann", "" + my_longitude);
            }
        });
        map.setInfoWindowAdapter(new CustomInfoWindowAdapter());
       /* map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                for(Marker_List mark:markerPoints){
                    if(marker.getTitle().equals(mark.title)){
                        Toast.makeText(Nearby.this, mark.title,Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }
                return false;
            }
        });*/
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                for(Marker_List mark:markerPoints){
                    if(marker.getTitle().equals(mark.title)){
                        Toast.makeText(Nearby.this, mark.title,Toast.LENGTH_SHORT).show();
                        /////////////////////add intent////////////////////////
                    }
                }
            }
        });

        Log.v("ann","map");

        if(map!=null) {
            Log.v("ann", "not empty map");
            // Enable MyLocation Button in the Map
            map.setMyLocationEnabled(true);
            //this.my_latitude = map.getMyLocation().getLatitude();
            //this.my_longitude = map.getMyLocation().getLongitude();
        }
        PostFetcher post = new PostFetcher();
        post.execute();



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nearby, menu);
        return true;
    }
    private void handlePostsList(){
        runOnUiThread(new Runnable(){
            public void run(){
                markerPoints.clear();
                Log.v("ann","size = "+streams.markers.size());
                Random rand = new Random();
                for(int i=0;i<streams.markers.size();i++) {
                    LatLng location = new LatLng(Double.parseDouble(streams.markers.get(i).latitude)+(-1+2*rand.nextDouble())/2000, Double.parseDouble(streams.markers.get(i).longitude)+(-1+2*rand.nextDouble())/2000);
                    markerPoints.add(new Marker_List(location,streams.markers.get(i).title,streams.markers.get(i).activity,streams.markers.get(i).cover));
                    map.addMarker(new MarkerOptions()
                            .position(location)
                            .title(streams.markers.get(i).title)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                }
            }
        });
    }

    private void failedLoadingPosts() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Toast.makeText(Nearby.this, "Failed to load Images. Have a look at LogCat.", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private class PostFetcher extends AsyncTask<Void, Void, String>{
        public static final String NEARBY_URL = "http://helloworld0923.appspot.com/api/nearby";
        @Override
        protected String doInBackground(Void... params){
            try{
                //Create an HTTP client
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(NEARBY_URL);
                try{
                    Gson gson = new Gson();
                    Nearby_Request request = new Nearby_Request();
                    request.radius = Double.toString(20);
                    while(flag == 0);
                    request.latitude = ""+my_latitude;
                    request.longitude = ""+my_longitude;
                    String json_request = gson.toJson(request);
                    Log.v("ann","send resquest :"+json_request);
                    post.setHeader("content-type", "application/json");
                    post.setEntity(new StringEntity(json_request));
                    HttpResponse response = client.execute(post);
                    StatusLine statusLine = response.getStatusLine();
                    if(statusLine.getStatusCode() == 200){
                        HttpEntity entity = response.getEntity();
                        InputStream content = entity.getContent();
                        try{
                            Reader reader = new InputStreamReader(content);

                            GsonBuilder gsonBuilder = new GsonBuilder();
                            Gson gson2 = gsonBuilder.create();
                            Log.v("ann",reader.toString());
                            streams = gson2.fromJson(reader,Nearby_Streams.class);
                            handlePostsList();

                        }catch (Exception ex){
                            Log.e("ann", "Failed to parse JSON due to: " + ex);
                            failedLoadingPosts();
                        }
                    }else{
                        Log.e("ann", "Server responded with status code: " + statusLine.getStatusCode());
                        failedLoadingPosts();
                    }
                }catch(Exception ex){
                    Log.v("ann",ex.getMessage());
                    failedLoadingPosts();
                }
            }catch(Exception ex){
                Log.v("ann","Failed to send HTTP POST request due to: " + ex);
                failedLoadingPosts();
            }
            return null;
        }
    }
    protected class Nearby_Request{
        private String radius;
        private String latitude;
        private String longitude;
    }
    protected class Marker_List{
        public LatLng location;
        public String title;
        public String activity;
        public String cover;
        Marker_List(LatLng loc, String title, String activity,String cover){
            this.location = loc;
            this.title = title;
            this.activity = activity;
            this.cover = cover;
            this.cover = cover;

        }

    }
    private class InfoWindowRefresher implements Callback{
        private Marker markerToRefresh;

        private InfoWindowRefresher(Marker markerToRefresh) {
            this.markerToRefresh = markerToRefresh;
        }

        @Override
        public void onSuccess() {
            markerToRefresh.showInfoWindow();
        }

        @Override
        public void onError() {}
    }
    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private View view;
        private boolean not_first = false;

        public CustomInfoWindowAdapter() {
            view = getLayoutInflater().inflate(R.layout.info_window, null);
        }

        @Override
        public View getInfoContents(Marker marker) {
            if (Nearby.this.marker != null && Nearby.this.marker.isInfoWindowShown()) {
                Nearby.this.marker.hideInfoWindow();
                Nearby.this.marker.showInfoWindow();
            }
            return null;
        }

        @Override
        public View getInfoWindow(final Marker marker) {
            Nearby.this.marker = marker;
            String url = null;
            for (Marker_List mark : Nearby.this.markerPoints) {
                if (mark.title.equals(marker.getTitle())) {
                    url = "http://helloworld0923.appspot.com/img?key=" + mark.cover;
                    Log.v("ann", url);
                }
            }
            final ImageView cover = ((ImageView) view.findViewById(R.id.cover));
            /*if (not_first) {
                Picasso.with(Nearby.this)
                        .load(url)
                        .placeholder(R.drawable.ico_loading)
                        .resize(120, 120)
                        .into(cover);
            } else {
                not_first = true;
                Picasso.with(Nearby.this)
                        .load(url)
                        .placeholder(R.drawable.ico_loading)
                        .resize(120, 120)
                        .into(cover, new InfoWindowRefresher(marker));
            }*/

            imageLoader.displayImage(url,cover,options, new SimpleImageLoadingListener(){
                @Override
                public void onLoadingComplete(String imageurl, View view, Bitmap loaded){
                    super.onLoadingComplete(imageurl,view,loaded);
                    getInfoContents(marker);
                }
            });


            final TextView title = ((TextView) view.findViewById(R.id.title));
            title.setText(marker.getTitle());
            return view;
        }
    }
    private void initImageLoader() {
        int memoryCacheSize;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            int memClass = ((ActivityManager)
                    getSystemService(Context.ACTIVITY_SERVICE))
                    .getMemoryClass();
            memoryCacheSize = (memClass / 8) * 1024 * 1024;
        } else {
            memoryCacheSize = 2 * 1024 * 1024;
        }

        final ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                this).threadPoolSize(5)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .memoryCacheSize(memoryCacheSize)
                .memoryCache(new FIFOLimitedMemoryCache(memoryCacheSize-1000000))
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO).enableLogging()
                .build();

        ImageLoader.getInstance().init(config);


    }

}
