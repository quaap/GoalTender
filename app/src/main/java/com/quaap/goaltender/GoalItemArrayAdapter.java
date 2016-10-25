package com.quaap.goaltender;

/**
 * Created by tom on 10/24/16.
 */


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.quaap.goaltender.storage.Entry;
import com.quaap.goaltender.storage.Goal;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GoalItemArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final List<Goal> values;

    //private final Map<Long, Integer> idToPos = new HashMap<>();

    public GoalItemArrayAdapter(Context context, List<Goal> values) {
        super(context, -1);
        this.context = context;
        this.values = values;
    }


    private static String formatDateTime(Date date) {
        if (date == null) return null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return dateFormat.format(date);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView goaltext = new TextView(convertView.getContext());
        goaltext.setText(values.get(position).getName());

        return goaltext;
    }

    public int getPosition(long id) {
       for(int i=0; i<values.size(); i++) {
           if (values.get(i).getId()==id) {
               return i;
           }
       }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return values.get(position).getId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

}