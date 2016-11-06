package com.quaap.goaltender;

/**
 *   Copyright 2016 Tom Kliethermes
 *
 *   This file is part of GoalTender.
 *
 *   GoalTender is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   GoalTender is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with GoalTender.  If not, see <http://www.gnu.org/licenses/>.
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

import java.util.List;


class EntryItemArrayAdapter extends ArrayAdapter<Entry>  {
    private final Context context;
    //private final List<Entry> values;

    private OnMoreGoalClick moreGoalClick;

    private boolean goallist;

    public EntryItemArrayAdapter(Context context, List<Entry> values) {
        super(context, -1, values);
        this.context = context;
        //this.values = values;
    }


//    private static String formatDateTime(Date date) {
//        if (date == null) return null;
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
//        return dateFormat.format(date);
//    }

    private static class ViewHolder {
        ImageView more_goal_click;
        TextView goaltext;
        TextView valuetext;
        TextView unittext;
        TextView goaldiff;
        TextView datetext;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        ViewHolder viewHolder;
        if (convertView==null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.itemrowlayout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.more_goal_click = (ImageView) convertView.findViewById(R.id.more_goal_click);
            viewHolder.goaltext = (TextView) convertView.findViewById(R.id.goaltext);
            viewHolder.valuetext = (TextView) convertView.findViewById(R.id.valuetext);
            viewHolder.unittext = (TextView) convertView.findViewById(R.id.unittext);
            viewHolder.goaldiff = (TextView) convertView.findViewById(R.id.goaldiff);
            viewHolder.datetext = (TextView) convertView.findViewById(R.id.datetext);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }


        Entry entry = this.getItem(position);

        if (entry.isNav()) {

            viewHolder.goaltext.setText(entry.getComment());

            viewHolder.more_goal_click.setVisibility(View.GONE);
            return convertView;

        }

        Goal goal = entry.getGoal();

        String period = "";
        if (entry.isCollapsed() && entry.getCollapsednum() > 0) {
            //convertView.setBackgroundColor(Color.rgb(245, 245, 245));

            if (goal.getType() == Goal.Type.Cumulative) {
                period = " (" + goal.getPeriod().name() + ", " + entry.getCollapsednum() + " " + (entry.getCollapsednum()>1?"entries":"entry") +  ")";
            }

        }

        if (moreGoalClick!=null && !goallist) {
            final int goalid = goal.getId();
            viewHolder.more_goal_click.setVisibility(View.VISIBLE);
            viewHolder.more_goal_click.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    moreGoalClick.itemClicked(goalid);
                }
            });
        } else {
            viewHolder.more_goal_click.setVisibility(View.GONE);
        }

        viewHolder.goaltext.setText(goal.getName());

        if (entry.isUnmet()) {
            viewHolder.valuetext.setText(R.string.unmet_goal_label);
        } else {
            float value = entry.getValue();
            if (goal.getType() == Goal.Type.Checkbox) {
                if (value>0) {
                    viewHolder.valuetext.setText("\u2713");
                } else {
                    viewHolder.valuetext.setText("\u2718");
                }
            } else if (value == (int) value) {
                viewHolder.valuetext.setText((int) value + "");
            } else {
                viewHolder.valuetext.setText(value + "");
            }
            viewHolder.unittext.setText(goal.getUnits());
        }



        float diff = entry.getValue() - goal.getGoalnum();
        String difftext = diff + "";
        if (diff == (int) diff) {
            difftext = (int) Math.abs(diff) + "";
        }

        if (goal.getType() == Goal.Type.Cumulative && !entry.isCollapsed() || goal.getType() == Goal.Type.Checkbox || entry.isUnmet()) {
            viewHolder.goaldiff.setText(" ");
        } else {
            boolean max = goal.getMinmax() == Goal.MinMax.Maximum;
            int c;
            if (diff > 0) {
                viewHolder.goaldiff.setText(difftext + context.getString(R.string.over_label));
                c = max ? Color.rgb(180,64,64) : Color.GREEN;

            } else if (diff < 0) {
                viewHolder.goaldiff.setText(difftext + context.getString(R.string.under_label));
                c = max ? Color.GREEN : Color.rgb(180,64,64);
            } else { //==0
                viewHolder.goaldiff.setText(" ");
                c = Color.GREEN;
            }

            viewHolder.goaldiff.setTextColor(c);
        }

        viewHolder.datetext.setText(Utils.formatDateForDisplay(entry.getDate(), goal.getPeriod()) + period);

        return convertView;
    }

//    public Entry getEntry(int position) {
//        return values.get(position);
//    }
//
    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }
//
//    @Override
//    public boolean hasStableIds() {
//        return true;
//    }

    public OnMoreGoalClick getMoreGoalClick() {
        return moreGoalClick;
    }

    public void setMoreGoalClick(OnMoreGoalClick moreGoalClick) {
        this.moreGoalClick = moreGoalClick;
    }

    public boolean isGoallist() {
        return goallist;
    }

    public void setGoallist(boolean goallist) {
        this.goallist = goallist;
    }


    public interface OnMoreGoalClick {

        void itemClicked(int goalid);
    }

}