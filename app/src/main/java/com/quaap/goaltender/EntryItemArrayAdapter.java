package com.quaap.goaltender;

/**
 * Created by tom on 10/24/16.
 */


    import android.content.Context;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ArrayAdapter;
    import android.widget.ImageView;
    import android.widget.TextView;

    import com.quaap.goaltender.storage.Entry;

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

        TextView goaltext = (TextView) rowView.findViewById(R.id.goaltext);
        goaltext.setText(values.get(position).getGoal().getName());

        TextView valuetext = (TextView) rowView.findViewById(R.id.valuetext);
        valuetext.setText(values.get(position).getValue() + "");

        TextView unittext = (TextView) rowView.findViewById(R.id.unittext);
        unittext.setText(values.get(position).getGoal().getUnits());

        TextView datetext = (TextView) rowView.findViewById(R.id.datetext);
        datetext.setText(formatDateTime(values.get(position).getDate()));




        return rowView;
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