package com.disc.teslademo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;


public class GameMapDisplay extends FragmentActivity
        implements OnMapReadyCallback {

    private static String TAG = "Map Fragment Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_fragment);

        MapFragment gameMap = (MapFragment) getFragmentManager().findFragmentById(R.id.gameMap);
        gameMap.getMapAsync(this);

        String summary;
        if (NewsFeedFragment.mapThisGame != null)
            summary = makeDetailedSummary(NewsFeedFragment.mapThisGame);
        else summary = "Nothing to show";

        TextView summaryText = (TextView) findViewById(R.id.detailedSummary);
        summaryText.setText(summary);

        Button doneButton = (Button) findViewById(R.id.doneMapButton);
        doneButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Log.i(TAG, "Map doneButton clicked.");
                finish();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String makeDetailedSummary(MapperPlayedGame game) {

        StringBuffer value = new StringBuffer();

        value.append("Basket Hole Count: \n");
        for (int i = 0; i < game.getHoleStrokes().size(); i++) {
            value.append("Hole #" + i + 1 + ": " + game.getHoleStroke(i));
            value.append(System.getProperty("line.separator"));
        }

        value.append("Total Holes: " + game.getTotalHoles());
        value.append(System.getProperty("line.separator"));
        int avgStroke = 0;
        if (game.getTotalHoles() != 0)
            avgStroke = game.getTotalStrokes() / game.getTotalHoles();
        value.append("Average Stroke per Hole: " + avgStroke);
        value.append(System.getProperty("line.separator"));
        value.append("Total Game Time: " + game.getTotalGameTime());
        value.append(System.getProperty("line.separator"));

        // Need to add individual hole strokes, map fragment
        return value.toString();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.setMyLocationEnabled(true);

        // Move camera to current phone location
        if (NewsFeedFragment.mapThisGame != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(NewsFeedFragment.mapThisGame.getPlotPoint(0), NewsFeedFragment.mapThisGame.getPlotPoint(1)), 18f));

            TrajectoryPlotter trajectoryPlotter = new TrajectoryPlotter();

            // Set up polylines
            PolylineOptions polylineOptions = new PolylineOptions(); // Instantiating the class PolylineOptions to plot polyline in the map
            polylineOptions.color(Color.RED); // Setting the color of the polyline
            polylineOptions.width(7); // Setting the width of the polyline

            double[] points = new double[NewsFeedFragment.mapThisGame.getPlotPoints().size()];
            for (int i = 0; i < points.length; i++)
                points[i] = NewsFeedFragment.mapThisGame.getPlotPoint(i);

            if (points.length > 0) {
                trajectoryPlotter.initData(points);
                ArrayList<LatLng> latPoints = trajectoryPlotter.mapTrajectory();
                polylineOptions.addAll(latPoints);

            }
            map.addPolyline(polylineOptions);
        }

    }
}
