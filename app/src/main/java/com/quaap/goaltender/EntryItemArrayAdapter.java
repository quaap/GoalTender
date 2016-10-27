package com.quaap.goaltender;

/**
 * Created by tom on 10/24/16.
 */


    import android.content.Context;
    import android.graphics.Color;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ArrayAdapter;
    import android.widget.ImageView;
    import android.widget.TextView;

    import com.quaap.goaltender.storage.Entry;
    import com.quaap.goaltender.storage.Goal;
    import com.quaap.goaltender.storage.GoalDB;

    import java.text.SimpleDateFormat;
    import java.util.Date;
    import java.util.List;
    import java.util.Locale;

public class EntryItemArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final List<Entry> values;

    public EntryItemArrayAdapter(Context context, String[] ids, List<Entry> values) {
        super(context, -1, ids);
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
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.itemrowlayout, parent, false);

        Entry entry = values.get(position);
        Goal goal = entry.getGoal();

        String period = "";
        if (entry.isCollapsed() && entry.getCollapsednum()>0) {
            rowView.setBackgroundColor(Color.rgb(245,245,245));

            if (goal.getType()!=Goal.Type.Single) {
                period = " (" + goal.getType().name() + " of " + entry.getCollapsednum() + ")";
            }
        }


        TextView goaltext = (TextView) rowView.findViewById(R.id.goaltext);
        goaltext.setText(goal.getName());

        TextView valuetext = (TextView) rowView.findViewById(R.id.valuetext);
        valuetext.setText(entry.getValue() + "");

        TextView unittext = (TextView) rowView.findViewById(R.id.unittext);
        unittext.setText(goal.getUnits());

        TextView goaldiff = (TextView) rowView.findViewById(R.id.goaldiff);

        float diff = entry.getValue() - goal.getGoalnum();

        if (goal.getType()!=Goal.Type.Single && !entry.isCollapsed()) {

        } else {
            boolean max = goal.getMinmax() == Goal.MinMax.Maximum;
            int c;
            if (diff > 0) {
                goaldiff.setText(diff + " over");
                c = max ? Color.RED : Color.GREEN;

            } else if (diff < 0) {
                goaldiff.setText(Math.abs(diff) + " under");
                c = max ? Color.GREEN : Color.RED;
            } else { //==0
                goaldiff.setText(" ");
                c = Color.GREEN;
            }

            goaldiff.setTextColor(c);
        }

        TextView datetext = (TextView) rowView.findViewById(R.id.datetext);
        datetext.setText(GoalDB.formatDateTime(entry.getDate(), goal.getType()) + period);

        return rowView;
    }

    public Entry getEntry(int position) {
        return values.get(position);
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