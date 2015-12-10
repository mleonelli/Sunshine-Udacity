package com.example.android.sunshine.app;

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
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;


public class ForecastFragment extends Fragment {

    ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastFragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_refresh:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        new FetchWeatherTask().execute();



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
        ArrayList<String> weekForecast = new ArrayList<>(
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
        return rootView;
    }

    private class FetchWeatherTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String forecastJsonStr = null;


            try {

                String baseUrl = "http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&units=metric&cnt=7&mode=json";
                String apiKey = "&APPID=" + BuildConfig.OPEN_WEATHER_MAP_API_KEY;
                URL url = new URL(baseUrl.concat(apiKey));

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

