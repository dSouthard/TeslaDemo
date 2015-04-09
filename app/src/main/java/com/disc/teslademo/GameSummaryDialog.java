package com.disc.teslademo;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by diana on 4/9/15.
 */
public class GameSummaryDialog extends DialogFragment implements OnMapReadyCallback {

    protected GoogleApiClient mGoogleApiClient;
    ArrayList<LatLng> points = new ArrayList<>();
    TrajectoryPlotter trajectory = new TrajectoryPlotter();
    LocationManager manager;
    List<Double> plotPoints = new ArrayList<>();
    private MapperPlayedGame playedGame;
    private Context context;
    private MapFragment gameMap;
    private PolylineOptions polylineOptions;

    private String courseName;
    private int totalStrokes, totalHoles;
    private List<Integer> holeStrokes;


    public GameSummaryDialog() {

    }

    public static final GameSummaryDialog newInstance(String courseName, int totalStrokes, double[] plots, int totalHoles, ArrayList<Integer> holeStrokes) {
        GameSummaryDialog dialog = new GameSummaryDialog();
        Bundle bundle = new Bundle(2);
        bundle.putString("courseName", courseName);
        bundle.putInt("totalStrokes", totalStrokes);
        bundle.putIntegerArrayList("holeStrokes", holeStrokes);
        bundle.putDoubleArray("plotPoints", plots);
        bundle.putInt("totalHoles", totalHoles);

        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reviewgame_summary_dialog, container);

        courseName = getArguments().getString("courseName");
        totalStrokes = getArguments().getInt("totalStrokes");
        holeStrokes = getArguments().getIntegerArrayList("holeStrokes");
        totalHoles = getArguments().getInt("totalHoles");

        double[] plotPointsArray = getArguments().getDoubleArray("plotPoints");


        // Dialog textviews that need inputs
        // Overall Summary
        TextView gameSummaryCourseName = (TextView) view.findViewById(R.id.courseNameOverallSummary);
        TextView avgStroke = (TextView) view.findViewById(R.id.avgStrokeperHoleSummary);
        TextView totalStrokesSummary = (TextView) view.findViewById(R.id.totalStrokesSummary);
        // Hole Summary

        // Set text
        gameSummaryCourseName.setText(courseName);
        if (totalHoles != 0) avgStroke.setText(String.valueOf(totalStrokes / totalHoles));
        else avgStroke.setText("Divide by zero");
        totalStrokesSummary.setText(String.valueOf(totalStrokes));

        // Dialog buttons
        Button reviewHolesbttn = (Button) view.findViewById(R.id.reviewEachHoleBttn);
        //you can change the button dimensions here
        reviewHolesbttn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //actions
                final Dialog detailsDialog = new Dialog(context);   // Overall Summary
                detailsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                detailsDialog.setContentView(R.layout.reviewgame_detail_dialog);
                ListView wallFeed = (ListView) detailsDialog.findViewById(R.id.HoleListView);

                ArrayList<String> wallList = new ArrayList<String>();
                ArrayAdapter<String> listAdapter = new ArrayAdapter<>(context, R.layout.friend_name, wallList);
                wallFeed.setAdapter(listAdapter);

                // Add each hole info
                for (int i = 0; i < holeStrokes.size(); i++) {
                    StringBuilder summary = new StringBuilder();
                    summary.append("Hole " + i);
                    summary.append("\t");
                    summary.append(" " + holeStrokes.get(i));
                    if (holeStrokes.get(i) == 1)
                        summary.append(" Stroke");
                    else
                        summary.append(" Strokes");
                    wallList.add(summary.toString());
                }
                listAdapter.notifyDataSetChanged();

                Button detailsDoneBttn = (Button) v.findViewById(R.id.detailsDoneButton);
                detailsDoneBttn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        //actions
                        detailsDialog.dismiss();
                    }
                });

                detailsDialog.show();

            }
        });

        Button doneBttn = (Button) view.findViewById(R.id.reviewEachHoleBttn);
        doneBttn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //actions
                dismiss();
            }
        });

        // Setup map
        polylineOptions = new PolylineOptions(); // Instantiating the class PolylineOptions to plot polyline in the map
        polylineOptions.color(Color.RED); // Setting the color of the polyline
        polylineOptions.width(7); // Setting the width of the polyline

        TrajectoryPlotter trajectoryPlotter = new TrajectoryPlotter();
        if (plotPointsArray.length > 0) {
            trajectoryPlotter.initData(plotPointsArray);
            points = trajectoryPlotter.mapTrajectory();
            polylineOptions.addAll(points);
        }

        gameMap = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.dialogMapFragment);
        gameMap.getMapAsync(this);
        gameMap.getMap().addPolyline(polylineOptions);

//        // Polled GPS location from disc
//        for (int i = 0; i < GPSpoints.length - 1; i = i + 2) {
//            gameMap.getMap().addMarker(new MarkerOptions().position(new LatLng(GPSpoints[i], GPSpoints[i + 1])));
//        }

        return view;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.setMyLocationEnabled(false);
        gameMap.getMap().setMapType(GoogleMap.MAP_TYPE_SATELLITE);
//        imap.moveCamera(CameraUpdateFactory.newLatLngZoom(points.get(0),19f ));
    }
}
