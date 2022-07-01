package com.hriday.project;


import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.core.BaseOptions;
import org.tensorflow.lite.task.text.nlclassifier.BertNLClassifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MoodPrediction extends Fragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_mood_prediction, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DBHelper db=((ApplicationClass)getActivity().getApplication()).getDB();
        ArrayList<Cursor> alc2=db.getData("select * from User_Data");
        final Cursor c2=alc2.get(0);
        String myS = "";
        if(c2!=null){
            c2.moveToFirst();

            String tempStr;
            do{
                for (int j = 0; j < c2.getColumnCount(); j++) {
                    tempStr = c2.getString(j);
                    if (j == 3) {
                        myS += tempStr;
                        myS += ". ";
                    }
                }

            }while(c2.moveToNext());
        }

        /*TextView classificationTextView1=getView().findViewById(R.id.classificationTextView1);
        TextView classificationTextView2=getView().findViewById(R.id.classificationTextView2);
        TextView classificationTextView3=getView().findViewById(R.id.classificationTextView3);
        TextView accuracyTextView1=getView().findViewById(R.id.accuracyTextView1);
        TextView accuracyTextView2=getView().findViewById(R.id.accuracyTextView2);
        TextView accuracyTextView3=getView().findViewById(R.id.accuracyTextView3);*/
        ListView listView = getView().findViewById(R.id.apps_list);
        TextView title=getView().findViewById(R.id.usage_tv);
        TextView noDatatv=getView().findViewById(R.id.noDataTextView);
        if (myS != "") {
            listView.setVisibility(View.VISIBLE);
            title.setVisibility(View.VISIBLE);
            noDatatv.setVisibility(View.GONE);
            BertNLClassifier.BertNLClassifierOptions options =
                    BertNLClassifier.BertNLClassifierOptions.builder()
                            .setBaseOptions(BaseOptions.builder().setNumThreads(4).build())
                            .build();
            BertNLClassifier classifier =
                    null;
            try {
                classifier = BertNLClassifier.createFromFileAndOptions(getContext(),"model.tflite", options);
            } catch (IOException e) {
                e.printStackTrace();
            }

// Run inference
            List<Category> results =  classifier.classify(myS);
            Collections.sort(results,(a1, a2)->Float.compare(a1.getScore(), a2.getScore()));
            Collections.reverse(results);
            ArrayList<Category>resultsAl=new ArrayList<>(results);
            CategoryAdapter adapter = new CategoryAdapter(getContext(), resultsAl);
            listView.setAdapter(adapter);
        /*classificationTextView1.setText(results.get(0).getLabel());
        accuracyTextView1.setText(String.valueOf(results.get(0).getScore()));
        classificationTextView2.setText(results.get(1).getLabel());
        accuracyTextView2.setText(String.valueOf(results.get(1).getScore()));
        classificationTextView3.setText(results.get(2).getLabel());
        accuracyTextView3.setText(String.valueOf(results.get(2).getScore()));*/
        }
        else{
            listView.setVisibility(View.GONE);
            title.setVisibility(View.GONE);
            noDatatv.setVisibility(View.VISIBLE);
        }
    }

}