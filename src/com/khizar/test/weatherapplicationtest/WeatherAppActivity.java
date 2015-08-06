package com.khizar.test.weatherapplicationtest;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.koushikdutta.ion.Ion;

/**
 * @author Khizar
 *
 */
public class WeatherAppActivity extends ActionBarActivity implements OnRefreshListener {
    
    private final static String PROPERTIES_FILE_NAME = "WeatherTest.properties";
    
    private static String IMG_URL; 
    private String url;
    
    private RequestQueue queue;
    
    private SwipeRefreshLayout swipeLayout;
    
    private TextView firstLabel ;
    private TextView secondLabel ;
    private TextView thirdLabel ;
    	
    private TextView firstTemperature ;
    private TextView secondTemperature ;
    private TextView thirdTemperature ;
    
    private ImageView firstImageView;
    private ImageView secondImageView;
    private ImageView thirdImageView;
    	
    private TextView updateTimeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_weather_app);
	
	firstLabel = (TextView) findViewById(R.id.first_label);
	secondLabel = (TextView) findViewById(R.id.second_label);
	thirdLabel = (TextView) findViewById(R.id.third_label);
	
	firstTemperature = (TextView) findViewById(R.id.first_temp);
	secondTemperature = (TextView) findViewById(R.id.second_temp);
	thirdTemperature = (TextView) findViewById(R.id.third_temp);
	
	firstImageView = (ImageView) findViewById(R.id.firstImageView);
	secondImageView = (ImageView) findViewById(R.id.secondImageView);
	thirdImageView = (ImageView) findViewById(R.id.thirdImageView);
	
	updateTimeText = (TextView) findViewById(R.id.updateTextview);
	
	swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
	swipeLayout.setOnRefreshListener(this);
	swipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
		android.R.color.holo_green_light, android.R.color.holo_orange_light,
		android.R.color.holo_red_light);

	// read the properties file.
	Resources resources = this.getResources();
	AssetManager assetManager = resources.getAssets();

	Properties properties = loadProperties(assetManager);
	String baseUrl = properties.getProperty("baseUrl");
	String appId = properties.getProperty("appId");
	String cityIds = properties.getProperty("cityIds");
	IMG_URL = properties.getProperty("imageUrl");

	url = baseUrl + "?units=metric&id=" + cityIds + "&APPID=" + appId;

	//initialize the RequestQueue
	queue = Volley.newRequestQueue(this);
	
	populateView();

    }

    /**
     * This method simply creates a request for the API call and adds it to the RequestQueue
     */
    private void populateView() {
	JsonObjectRequest jsObjRequest = createRequestForTempFromApi();
	queue.add(jsObjRequest);
    }

    /**
     * This method reads a properties file using the AssetManager whose object is passed as argument and returns a 
     * Properties object which can then be used to retrieve properties from that file
     * 
     * @param assetManager
     * @return
     */
    private Properties loadProperties(AssetManager assetManager) {
	Properties properties = new Properties();
	try {
	    InputStream inputStream = assetManager.open(PROPERTIES_FILE_NAME);
	    properties.load(inputStream);
	} catch (IOException e) {
	    System.err.println(getResources().getString(R.string.error_prop_file));
	    e.printStackTrace();
	}
	return properties;
    }
    
    /**
     * The method called when the activity is refreshed using the swipe to refresh gesture. it simply calls
     * the populateView() method.
     */
    @Override
    public void onRefresh() {
	populateView();
    }

    /**
     * This method creates a JsonObjectRequest to get the weather data from the API. It handles the response,
     * both in case of success and error via the listeners defined in it.
     * 
     * @return jsObjRequest
     */
    private JsonObjectRequest createRequestForTempFromApi() {
	JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null,
		new Response.Listener<JSONObject>() {

		    @Override
		    public void onResponse(JSONObject response) {
			try {
			    // number of weather objects returned. we are already expecting three as we have
			    // passed three city codes, but still better to only iterate only the number of 
			    // objects that have been returned in the json.
			    int objectCount = response.getInt("cnt");
			    
			    if (objectCount > 0) {
				JSONArray weatherObjectsList = response.getJSONArray("list");
				
				//iterate over the list, extract the data that is needed from the json and populate it.
				for (int i = 0; i < objectCount; i++) {
				    Long temperature = weatherObjectsList.getJSONObject(i).getJSONObject("main").getLong("temp");
				    String iconName = weatherObjectsList.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("icon");
				    String cityName = weatherObjectsList.getJSONObject(i).getString("name");
				    
				    switch (i) {
				    case 0:
					firstLabel.setText(cityName);
					firstTemperature.setText(" " + temperature + "\u00b0");
					Ion.with(firstImageView).load(IMG_URL + iconName +".png");
					break;
				    case 1:
					secondLabel.setText(cityName);
					secondTemperature.setText(" " + temperature + "\u00b0");
					Ion.with(secondImageView).load(IMG_URL + iconName +".png");
					break;
				    case 2:
					thirdLabel.setText(cityName);
					thirdTemperature.setText(" " + temperature + "\u00b0");
					Ion.with(thirdImageView).load(IMG_URL + iconName +".png");
					break;

				    default:
					break;
				    }
				}

			    }
			    
			    // set current time as update time.
			    String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
			    updateTimeText.setText("Updated on: " + currentDateTimeString);
			    
			    // check to see if this was a call from the swipe to refresh, and set refreshing to false
			    // to stop the refreshing indicator.
			    if(swipeLayout.isRefreshing()){
				swipeLayout.setRefreshing(false);
			    }

			} catch (JSONException e) {
			    System.err.println(getResources().getString(R.string.error_api_call));
			    e.printStackTrace();
			} 
			
		    }

		}, new Response.ErrorListener() {

		    @Override
		    public void onErrorResponse(VolleyError error) {
			if (swipeLayout.isRefreshing()) {
			    swipeLayout.setRefreshing(false);
			}
			System.err.println(getResources().getString(R.string.error_api_call));
			error.printStackTrace();
		    }
		});
	return jsObjRequest;
    }
}
