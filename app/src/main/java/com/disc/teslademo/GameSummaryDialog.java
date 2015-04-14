package com.disc.teslademo;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class GameSummaryDialog extends DialogFragment implements OnMapReadyCallback {

    public static boolean visible = true;
    ArrayAdapter<String> listAdapter;
    private Context context;
    private MapFragment gameMap;
    private PolylineOptions polylineOptions;
    private String courseName;
    private int totalStrokes, totalHoles;
    private ArrayList<String> wallList;
    private LatLng startMap;
    private TextView holeSummary;
    private String textSummary;

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
        StringBuffer stringBuffer = new StringBuffer();
        // Add each hole info
        for (int i = 0; i < holeStrokes.size(); i++) {
            stringBuffer.append("Hole ").append(String.valueOf(i + 1)).append(": ");
            stringBuffer.append("\t");
            stringBuffer.append(" ").append(holeStrokes.get(i));
            if (holeStrokes.get(i) == 1)
                stringBuffer.append(" Stroke");
            else
                stringBuffer.append(" Strokes");
            stringBuffer.append(System.getProperty("line.separator"));
        }

        textSummary = stringBuffer.toString();

        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.reviewgame_summary_dialog);
        dialog = configureDialogView(dialog);
        visible = true;
        return dialog;
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
        TextView holeSummary = (TextView) v.findViewById(R.id.holeSummaryTextView);


        // Set text
        gameSummaryCourseName.setText(courseName);
        int avgStrokeInt = 0;
//        if (totalHoles != 0)
        avgStrokeInt = (totalStrokes / totalHoles);
        avgStroke.setText(String.valueOf(avgStrokeInt));
        totalStrokesSummary.setText(String.valueOf(totalStrokes));
        holeSummary.setText(textSummary);

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
        gameMap = (MapFragment) getFragmentManager().findFragmentById(R.id.dialogMapFragment);
//        gameMap = (MapFragment) mapSupport;
        gameMap.getMapAsync(this);
        gameMap.getMap().addPolyline(polylineOptions);

        return v;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        SupportMapFragment mapFrag = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.dialogMapFragment);
        if (gameMap != null)
            getFragmentManager().beginTransaction().remove(gameMap).commit();
    }

}
