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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;


import com.quaap.goaltender.storage.Entry;
import com.quaap.goaltender.storage.Goal;

import java.util.List;


class EntryItemArrayAdapter extends ArrayAdapter<Entry> implements View.OnTouchListener {
    private final Context context;
    //private final List<Entry> values;

    private OnViewAllGoalEntriesClick viewGoalClick;
    private OnEditEntryClick editEntryClick;
    private OnAddEntryClick addEntryClick;
    private OnNavEntryClick navEntryClick;


    private boolean goallist;

    public EntryItemArrayAdapter(Context context, List<Entry> values) {
        super(context, -1, values);
        this.context = context;
        //this.values = values;
    }

    public OnEditEntryClick getEditEntryClick() {
        return editEntryClick;
    }

    public void setEditEntryClick(OnEditEntryClick editEntryClick) {
        this.editEntryClick = editEntryClick;
    }

    public OnAddEntryClick getAddEntryClick() {
        return addEntryClick;
    }

    public void setAddEntryClick(OnAddEntryClick addEntryClick) {
        this.addEntryClick = addEntryClick;
    }

    public OnNavEntryClick getNavEntryClick() {
        return navEntryClick;
    }

    public void setNavEntryClick(OnNavEntryClick navEntryClick) {
        this.navEntryClick = navEntryClick;
    }


//    private static String formatDateTime(Date date) {
//        if (date == null) return null;
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
//        return dateFormat.format(date);
//    }

    private static class ViewHolder {
       // ImageView more_goal_click;
        TextView goaltext;
        TextView valuetext;
        TextView unittext;
        TextView goaldiff;
        TextView datetext;
        ImageView show_ctrls;
        ImageView hide_ctrls;
        ImageButton view_all;
        ImageButton add_entry;
        ImageButton edit_entry;
        TextView entry_item_ctrls_name;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        ViewHolder viewHolder;
        if (convertView==null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.itemrowlayout, parent, false);
            viewHolder = new ViewHolder();
           // viewHolder.more_goal_click = (ImageView) convertView.findViewById(R.id.more_goal_click);
            viewHolder.goaltext = (TextView) convertView.findViewById(R.id.goaltext);
            viewHolder.valuetext = (TextView) convertView.findViewById(R.id.valuetext);
            viewHolder.unittext = (TextView) convertView.findViewById(R.id.unittext);
            viewHolder.goaldiff = (TextView) convertView.findViewById(R.id.goaldiff);
            viewHolder.datetext = (TextView) convertView.findViewById(R.id.datetext);

            viewHolder.show_ctrls = (ImageView)convertView.findViewById(R.id.entry_item_show_ctrls);
            viewHolder.hide_ctrls = (ImageView)convertView.findViewById(R.id.entry_item_hide_ctrls);

            viewHolder.view_all = (ImageButton)convertView.findViewById(R.id.entry_button_view_all);
            viewHolder.add_entry = (ImageButton)convertView.findViewById(R.id.entry_button_add_entry);
            viewHolder.edit_entry = (ImageButton)convertView.findViewById(R.id.entry_button_edit_entry);

            viewHolder.entry_item_ctrls_name = (TextView) convertView.findViewById(R.id.entry_item_ctrls_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        LinearLayout listitem_layout = (LinearLayout)convertView.findViewById(R.id.listitem_layout);

        Entry entry = this.getItem(position);

        if (entry.isNav()) {

            viewHolder.show_ctrls.setVisibility(View.INVISIBLE);
            viewHolder.hide_ctrls.setVisibility(View.INVISIBLE);
            viewHolder.goaltext.setText(entry.getComment());
            final int nav = entry.getNav();
            listitem_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    navEntryClick.itemClicked(nav);
                }
            });

            //listitem_layout.setOnClickListener(null);
            //viewHolder.more_goal_click.setVisibility(View.GONE);
            return convertView;
        }
        viewHolder.show_ctrls.setVisibility(View.VISIBLE);
        viewHolder.hide_ctrls.setVisibility(View.VISIBLE);

        final ViewFlipper vs =  (ViewFlipper)convertView;
        viewHolder.show_ctrls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showControls(vs, false);
            }
        });

        viewHolder.hide_ctrls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showControls(vs, true);
            }
        });


        convertView.setOnTouchListener(this);

        Goal goal = entry.getGoal();
        viewHolder.entry_item_ctrls_name.setText(goal.getName());

        String period = "";
        if (entry.isCollapsed() && entry.getCollapsednum() > 0) {
            //convertView.setBackgroundColor(Color.rgb(245, 245, 245));

            if (goal.getType() == Goal.Type.Cumulative) {
                period = " (" + goal.getPeriod().name() + ", " + entry.getCollapsednum() + " " + (entry.getCollapsednum()>1?"entries":"entry") +  ")";
            }

        }

        final int goalid = goal.getId();
        final int entryid = entry.getId();

        if (entryid==-1 || entry.isUnmet()) {
            viewHolder.edit_entry.setVisibility(View.GONE);
        } else {
            viewHolder.edit_entry.setVisibility(View.VISIBLE);
        }

        //System.out.println("entryid: " + entryid + " ");
        if (viewGoalClick!=null) {

            viewHolder.view_all.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showControls(vs, true);
                    viewGoalClick.itemClicked(goalid);
               }
            });

        }
        if (addEntryClick!=null) {
            viewHolder.add_entry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showControls(vs, true);
                    addEntryClick.itemClicked(goalid);
                }
            });

//            convertView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    addEntryClick.itemClicked(goalid);
//                }
//            });

        }
        if (editEntryClick!=null) {
            viewHolder.edit_entry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showControls(vs, true);
                    editEntryClick.itemClicked(entryid);
                }
            });


            listitem_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (entryid==-1) {
                        addEntryClick.itemClicked(goalid);
                    } else {
                        editEntryClick.itemClicked(entryid);
                    }
                }
            });

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

    public OnViewAllGoalEntriesClick getMoreGoalClick() {
        return viewGoalClick;
    }

    public void setOnViewAllGoalEntries(OnViewAllGoalEntriesClick viewGoalClick) {
        this.viewGoalClick = viewGoalClick;
    }

    public boolean isGoallist() {
        return goallist;
    }

    public void setGoallist(boolean goallist) {
        this.goallist = goallist;
    }


    public interface OnViewAllGoalEntriesClick {
        void itemClicked(int goalid);
    }

    public interface OnEditEntryClick {
        void itemClicked(int entryid);
    }

    public interface OnAddEntryClick {
        void itemClicked(int goalid);
    }

    public interface OnNavEntryClick {
        void itemClicked(int navValue);
    }

    //TODO: convert to gesturelistener.onfling
    // http://codetheory.in/android-viewflipper-and-viewswitcher/
    private float x1,x2;
    static final int MIN_DISTANCE = 140;

    public boolean onTouch(View view, MotionEvent motionEvent) {
        //Toast.makeText(this.getContext(), "touched it", Toast.LENGTH_SHORT).show ();
        switch(motionEvent.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                x1 = motionEvent.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = motionEvent.getX();
                float deltaX = x2 - x1;
                ViewFlipper vs = (ViewFlipper)view;
                if (deltaX > MIN_DISTANCE) {
                    showControls(vs, false);
                    //Toast.makeText(this.getContext(), "left2right swipe", Toast.LENGTH_SHORT).show ();
//                    vs.setInAnimation(vs.getContext(), R.anim.right_in);
//                    vs.setOutAnimation(vs.getContext(), R.anim.right_out);
//
//                    vs.showNext();
//                    return true;
                } else if (-deltaX > MIN_DISTANCE) {
                    showControls(vs, true);
                    //Toast.makeText(this.getContext(), "right2left swipe", Toast.LENGTH_SHORT).show ();
//                    vs.setInAnimation(vs.getContext(), R.anim.left_in);
//                    vs.setOutAnimation(vs.getContext(), R.anim.left_out);
//
//                    vs.showPrevious();

                } else {
                    return false;
                }
        }

        return true;

    }

    private void showControls(ViewFlipper vs, boolean backwards) {
        if (!backwards) {
            //Toast.makeText(this.getContext(), "left2right swipe", Toast.LENGTH_SHORT).show ();
            vs.setInAnimation(vs.getContext(), R.anim.right_in);
            vs.setOutAnimation(vs.getContext(), R.anim.right_out);

            vs.showNext();
        } else {
            //Toast.makeText(this.getContext(), "right2left swipe", Toast.LENGTH_SHORT).show ();
            vs.setInAnimation(vs.getContext(), R.anim.left_in);
            vs.setOutAnimation(vs.getContext(), R.anim.left_out);

            vs.showPrevious();

        }
    }
}