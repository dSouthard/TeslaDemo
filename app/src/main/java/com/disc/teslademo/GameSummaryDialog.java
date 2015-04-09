package com.disc.teslademo;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by diana on 4/9/15.
 */
public class GameSummaryDialog extends Dialog {
    public GameSummaryDialog(final Context context, final MapperPlayedGame playedGame) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.reviewgame_summary_dialog);

        // Dialog textviews that need inputs
        // Overall Summary
        TextView gameSummaryCourseName = (TextView) findViewById(R.id.courseNameOverallSummary);
        TextView avgStroke = (TextView) findViewById(R.id.avgStrokeperHoleSummary);
        TextView totalStrokesSummary = (TextView) findViewById(R.id.totalStrokesSummary);
        // Hole Summary

        // Set text
        gameSummaryCourseName.setText(playedGame.getGameLocation());
        avgStroke.setText(String.valueOf(playedGame.getTotalStrokes() / playedGame.getTotalHoles()));
        totalStrokesSummary.setText(String.valueOf(playedGame.getTotalStrokes()));

        // Dialog buttons
        Button reviewHolesbttn = (Button) findViewById(R.id.reviewEachHoleBttn);
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
                List<Integer> tempList = playedGame.getHoleStrokes();
                for (int i = 0; i < tempList.size(); i++) {
                    StringBuilder summary = new StringBuilder();
                    summary.append("Hole " + i);
                    summary.append("\t");
                    summary.append(" " + tempList.get(i));
                    if (tempList.get(i) == 1)
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

        Button doneBttn = (Button) findViewById(R.id.reviewEachHoleBttn);
        doneBttn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //actions
                dismiss();
            }
        });
        show();
    }
}
