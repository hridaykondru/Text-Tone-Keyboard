package com.hriday.project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.tensorflow.lite.support.label.Category;

import java.util.ArrayList;

public class CategoryAdapter extends ArrayAdapter<Category> {
    public CategoryAdapter(Context context, ArrayList<Category> usageStatDTOArrayList) {
        super(context, 0, usageStatDTOArrayList);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        Category usageStats = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_prediction, parent, false);
        }

        // Lookup view for data population
        TextView app_name_tv = convertView.findViewById(R.id.app_name_tv);
        TextView usage_duration_tv =  convertView.findViewById(R.id.usage_duration_tv);
        TextView usage_perc_tv = convertView.findViewById(R.id.usage_perc_tv);
        ImageView icon_img =  convertView.findViewById(R.id.icon_img);
        ProgressBar progressBar = convertView.findViewById(R.id.progressBar);


        // Populate the data into the template view using the data object
        app_name_tv.setText(usageStats.getLabel());
        usage_duration_tv.setText((usageStats.getScore()*100) + "%");
        usage_perc_tv.setText("");
        icon_img.setImageDrawable(null);
        progressBar.setProgress((int) (usageStats.getScore()*100));

        // Return the completed view to render on screen
        return convertView;
    }
}