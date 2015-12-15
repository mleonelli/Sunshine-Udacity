package com.example.android.sunshine.app;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ForecastFragment extends Fragment {

    ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_refresh:
                new FetchWeatherTask().execute("18038");
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String zipCode = "18038";
        new FetchWeatherTask().execute(zipCode);



        //Create some dummy data for ListView
        String[] forecastArray = {
            "Today - Sunny - 88/63",
            "Today - Sunny - 88/63",
            "Today - Sunny - 88/63",
            "Today - Sunny - 88/63",
            "Today - Sunny - 88/63",
            "Today - Sunny - 88/63",
            "Today - Sunny - 88/63",
            "Today - Sunny - 88/63",
            "Today - Sunny - 88/63",
            "Today - Sunny - 88/63",
            "Today - Sunny - 88/63"
        };

        List<String> weekForecast = new ArrayList<>(
                Arrays.asList(forecastArray)
        );
        ListView listView = (ListView)rootView.findViewById(
            R.id.listview_forecast
        );

        mForecastAdapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weekForecast
        );
        listView.setAdapter(mForecastAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String forecast = mForecastAdapter.getItem(position);
                //Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();
                Intent detailIntent = new Intent(getActivity(), DetailsActivity.class).putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(detailIntent);
            }
        });

        return rootView;
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected void onPostExecute(String[] result) {
            if(result != null){
                mForecastAdapter.clear();
                for (String dayForecastStr: result) {
                    mForecastAdapter.add(dayForecastStr);
                }
            }
        }

        @Override
        protected String[] doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String forecastJsonStr = null;


            try {
                Uri uri = Uri.parse("http://api.openweathermap.org/data/2.5/forecast/daily")
                    .buildUpon()
                    .appendQueryParameter("q", params[0])
                    .appendQueryParameter("units", "metric")
                    .appendQueryParameter("cnt", "7")
                    .appendQueryParameter("mode", "json")
                    .appendQueryParameter("appid", BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                    .build();

                URL url = new URL(uri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0)
                    return null;

                forecastJsonStr = buffer.toString();
                try {
                    String[] forecastArray = OpenWeatherHelper.getWeatherDataFromJson(forecastJsonStr, 7);
                    return forecastArray;
                }catch(JSONException e){

                }
                    //JSONObject jsonObject = new JSONObject(forecastJsonStr);
                    //JSONObject dt = (JSONObject) jsonObject.getJSONArray("list").getJSONObject(1);
                    //dt.getJSONObject("temp").getDouble("max");
                    //http://stackoverflow.com/questions/24231223/how-can-i-cast-a-jsonobject-to-a-custom-java-class
            }catch(IOException e){
                Log.e("ForecastFragment", "Error", e);
                return null;
            }
            finally {
                if(urlConnection != null)
                    urlConnection.disconnect();
                if(reader != null){
                    try{
                        reader.close();
                    }
                    catch(final IOException e){
                        Log.e("ForecastFragment", "Error closing stream", e);
                    }
                }
            }

            return null;
        }
    }
}

