package com.disc.teslademo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class GameSummaryDialog extends DialogFragment implements OnMapReadyCallback {

    public static boolean visible = true;
    ArrayAdapter<String> listAdapter;
    private Context context;
    private SupportMapFragment gameMap;
    private PolylineOptions polylineOptions;
    private String courseName;
    private int totalStrokes, totalHoles;
    private ArrayList<String> wallList;
    private LatLng startMap;

    public GameSummaryDialog() {

    }

    public static GameSummaryDialog newInstance(String courseName, int totalStrokes,
                                                double[] plots, int totalHoles, ArrayList<Integer> holeStrokes) {
        GameSummaryDialog dialog = new GameSummaryDialog();
        Bundle bundle = new Bundle(5);
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
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        courseName = getArguments().getString("courseName");
        totalStrokes = getArguments().getInt("totalStrokes");
        List<Integer> holeStrokes = getArguments().getIntegerArrayList("holeStrokes");
        totalHoles = getArguments().getInt("totalHoles");
        double[] plotPointsArray = getArguments().getDoubleArray("plotPoints");

        TrajectoryPlotter trajectoryPlotter = new TrajectoryPlotter();

        // Set up polylines
        polylineOptions = new PolylineOptions(); // Instantiating the class PolylineOptions to plot polyline in the map
        polylineOptions.color(Color.RED); // Setting the color of the polyline
        polylineOptions.width(7); // Setting the width of the polyline

        if (plotPointsArray.length > 0) {
            trajectoryPlotter.initData(plotPointsArray);
            ArrayList<LatLng> points = trajectoryPlotter.mapTrajectory();
            polylineOptions.addAll(points);

            startMap = new LatLng(plotPointsArray[0], plotPointsArray[1]);  // Start map at beginning of throws
        } else {  // Start map at beginning of course
            startMap = new LatLng(0, 0);
        }

        // Set up hole summary
        wallList = new ArrayList<>();
        // Add each hole info
        for (int i = 0; i < holeStrokes.size(); i++) {
            StringBuilder summary = new StringBuilder();
            summary.append("Hole ").append(i).append(": ");
            summary.append("\t");
            summary.append(" ").append(holeStrokes.get(i));
            if (holeStrokes.get(i) == 1)
                summary.append(" Stroke");
            else
                summary.append(" Strokes");
            wallList.add(summary.toString());
        }
        listAdapter = new ArrayAdapter<>(context, R.layout.friend_name, wallList);
        Dialog summary1 = new Dialog(context);
        summary1.setContentView(R.layout.reviewgame_summary_dialog);
        summary1 = configureDialogView(summary1);
        visible = true;
        return summary1;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.setMyLocationEnabled(false);
        gameMap.getMap().setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        gameMap.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(startMap, 17f));

    }

    private Dialog configureDialogView(Dialog v) {
        // Dialog textviews that need inputs
        // Overall Summary
        TextView gameSummaryCourseName = (TextView) v.findViewById(R.id.courseNameOverallSummary);
        TextView avgStroke = (TextView) v.findViewById(R.id.avgStrokeperHoleSummary);
        TextView totalStrokesSummary = (TextView) v.findViewById(R.id.totalStrokeGameSummaryDialog);
        ListView wallFeed = (ListView) v.findViewById(R.id.holeSummaryList);

        listAdapter = new ArrayAdapter<>(context, R.layout.friend_name, wallList);
        wallFeed.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();

        // Set text
        gameSummaryCourseName.setText(courseName);
        if (totalHoles != 0) avgStroke.setText(String.valueOf(totalStrokes / totalHoles));
        else avgStroke.setText("0");
        totalStrokesSummary.setText(String.valueOf(totalStrokes));

        // Button
        Button doneBttn = (Button) v.findViewById(R.id.doneButton);
        doneBttn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //actions
                visible = false;
                dismiss();
            }
        });

        // Setup map
        gameMap = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.dialogMapFragment);
//        gameMap = (MapFragment) mapSupport;
        gameMap.getMapAsync(this);
        gameMap.getMap().addPolyline(polylineOptions);

        return v;
    }
}
