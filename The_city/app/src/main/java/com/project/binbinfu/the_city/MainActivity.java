/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.project.binbinfu.the_city;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.http.Header;
import org.w3c.dom.Text;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;


public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    static HotPagerAdapter mHotPagerAdapter;
    private static FragmentManager fragmentmanager;
    /**
     * The {@link android.support.v4.view.ViewPager} that will display the object collection.
     */
    static ViewPager mViewPager;

    private static Hot_class hot_activities = new Hot_class();


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.

        //mHotPagerAdapter = new HotPagerAdapter(getSupportFragmentManager());
        fragmentmanager = getSupportFragmentManager();
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();

        // Specify that the Home/Up button should not be enabled, since there is no hierarchical
        // parent.
        actionBar.setHomeButtonEnabled(false);

        // Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // For each of the sections in the app, add a tab to the action bar.

        actionBar.addTab(actionBar.newTab().setText(R.string.title_section1)
                .setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.title_section2)
                .setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.title_section3)
                .setTabListener(this));
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab isselected, show the tabcontents in the
        // //container view.
        Fragment fragment3 = null;
        Fragment fragment1 = null;
        Fragment fragment2 = null;
        switch (tab.getPosition()) {
            case 0:
                // The first is Selected
                if(fragment1 == null) {
                    fragment1 = new SelectedFragment();
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment1).commit();
                break;
            case 1:
                // The second is Search
                if(fragment2 == null){
                    fragment2 = new SearchFragment();
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment2).commit();
                Intent intent = new Intent(MainActivity.this, Search.class);

                MainActivity.this.startActivity(intent);
                break;
            case 2:
                // The third is My City
                if(fragment3 == null){
                    fragment3 = new MyCityFragment();
                    Bundle args = new Bundle();
                    args.putInt(MyCityFragment.ARG_SECTION_NUMBER, 3);
                    fragment3.setArguments(args);
                }
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment3).commit();
                Intent nearby = new Intent(MainActivity.this, Nearby.class);

                MainActivity.this.startActivity(nearby);
                break;
            default:
                break;
        }
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }


    /**
     * A fragment that launches other parts of the demo application.
     */
    public static class SelectedFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_selected, container, false);
            mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
            //Fetch data
            AsyncHttpClient client = new AsyncHttpClient();
            JSONObject jsonObject = new JSONObject();
            StringEntity entity = null;
            try {
                jsonObject.put("test", "test");
                entity = new StringEntity(jsonObject.toString());
                entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            client.post(rootView.getContext(),"http://the-city.appspot.com/api/hot_activity",entity,"application/json",new JsonHttpResponseHandler(){
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    try {
                        GsonBuilder gsonBuilder = new GsonBuilder();
                        Gson gson3 = gsonBuilder.create();
                        String str = response.toString();
                        Log.v("test","success");
                        hot_activities = gson3.fromJson(str,Hot_class.class);
                        mHotPagerAdapter = new HotPagerAdapter(fragmentmanager);
                        mViewPager.setAdapter(mHotPagerAdapter);

                    } catch (Exception ex) {
                        Log.e("Hello", "Failed to parse JSON due to: " + ex);
                    }
                }

                public void onFailure(int statusCode, Header[] headers, Throwable throwable,JSONObject errorResponse) {
                    Log.i("error", "fail to request");
                    Log.v("geo","fail");
                }
            });
            // Set up the ViewPager, attaching the adapter.
            //mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
            //mViewPager.setAdapter(mHotPagerAdapter);

            return rootView;
        }
    }

    public static class SearchFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_search, container, false);
            return rootView;
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     */
    public static class MyCityFragment extends Fragment {

        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_mycity, container, false);
            Bundle args = getArguments();
            ((TextView) rootView.findViewById(android.R.id.text1)).setText(
                    getString(R.string.dummy_section_text, args.getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a fragment
     * representing an object in the collection.
     */
    public static class HotPagerAdapter extends FragmentStatePagerAdapter {

        public HotPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new HotObjectFragment();
            Bundle args = new Bundle();
            Log.v("test","fragment");
            Log.v("test",hot_activities.hot_id.get(0));
            if (hot_activities.hot_id.size()!=0) {
                args.putString("hot_id", hot_activities.hot_id.get(i));
                args.putString("hot_title", hot_activities.hot_title.get(i));
                args.putString("hot_start", hot_activities.hot_start.get(i));
                args.putString("hot_end", hot_activities.hot_end.get(i));
                args.putString("hot_take", hot_activities.hot_take.get(i));
                args.putString("hot_like", hot_activities.hot_like.get(i));
                args.putString("hot_address", hot_activities.hot_address.get(i));
                args.putString("hot_type", hot_activities.hot_type.get(i));
                args.putString("hot_cover",hot_activities.hot_cover.get(i));
            }
            else{
                args.putString("hot_id", "");
                args.putString("hot_title", "");
                args.putString("hot_start", "");
                args.putString("hot_end", "");
                args.putString("hot_take", "");
                args.putString("hot_like", "");
                args.putString("hot_address", "");
                args.putString("hot_type", "");
                args.putString("hot_cover","");
            }
                //args.putString("hot_title",hot_activities.hot_id.get(0));
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            // For this contrived example, we have a 100-object collection.
            return hot_activities.hot_id.size();
        }

    }

    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     */
    public static class HotObjectFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_collection_object, container, false);
            final TextView title = (TextView) rootView.findViewById(R.id.title);
            final TextView tag = (TextView) rootView.findViewById(R.id.tag);
            final TextView time = (TextView) rootView.findViewById(R.id.time);
            final TextView address = (TextView) rootView.findViewById(R.id.address);
            final TextView like = (TextView) rootView.findViewById(R.id.like);
            final TextView take = (TextView) rootView.findViewById(R.id.take);
            final ImageView cover = (ImageView) rootView.findViewById(R.id.cover);

            Bundle args = getArguments();
            title.setText(args.getString("hot_title"));
            tag.setText(args.getString("hot_type"));
            time.setText(args.getString("hot_start")+" -- " +args.getString("hot_end"));
            address.setText(args.getString("hot_address"));
            like.setText("Like: "+args.getString("hot_like"));
            take.setText("Take: "+args.getString("hot_take"));
            if (!args.getString("hot_cover").equals("")) {
                Picasso.with(rootView.getContext())
                        .load("http://the-city.appspot.com/img?key="+args.getString("hot_cover"))
                        .placeholder(R.drawable.ico_loading)
                        .resize(250, 200)
                        .into(cover);
            }
            else{
                Log.v("binbn","haha2");
                Picasso.with(rootView.getContext()) //
                        .load("http://upload.wikimedia.org/wikipedia/commons/b/b9/No_Cover.jpg") //
                        .placeholder(R.drawable.ico_loading) //
                        .resize(250, 200)
                        .into(cover);
            }
            return rootView;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This is called when the Home (Up) button is pressed in the action bar.
                // Create a simple intent that starts the hierarchical parent activity and
                // use NavUtils in the Support Package to ensure proper handling of Up.
                Intent upIntent = new Intent(this, MainActivity.class);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is not part of the application's task, so create a new task
                    // with a synthesized back stack.
                    TaskStackBuilder.from(this)
                            // If there are ancestor activities, they should be added here.
                            .addNextIntent(upIntent)
                            .startActivities();
                    finish();
                } else {
                    // This activity is part of the application's task, so simply
                    // navigate up to the hierarchical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
