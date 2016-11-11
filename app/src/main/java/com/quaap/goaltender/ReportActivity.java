package com.quaap.goaltender;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import com.quaap.goaltender.storage.Entry;
import com.quaap.goaltender.storage.Goal;
import com.quaap.goaltender.storage.GoalDB;

import java.util.Date;
import java.util.List;

public class ReportActivity extends Activity implements CanvasView.OnDrawListener {
    Paint gbars = new Paint();
    Paint glines = new Paint();
    Paint gtext = new Paint();

    int lastXperiods = 5;

    Goal goal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        final CanvasView canvasview = (CanvasView)findViewById(R.id.canvas_view);
        canvasview.setOnDrawListener(this);

        gbars.setColor(Color.RED);

        glines.setColor(Color.BLACK);

        gtext.setColor(Color.BLUE);
        gtext.setTextSize(26);

        List<Goal> goals = GoalTender.getDatabase().getAllGoals(false);
        final Spinner goallist = (Spinner) findViewById(R.id.report_goallist);


        final ArrayAdapter<Goal> adapter = new ArrayAdapter<Goal>(this, android.R.layout.simple_spinner_item, goals);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        goallist.setAdapter(adapter);

        goallist.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                goal = (Goal)goallist.getSelectedItem();

                canvasview.postInvalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final Spinner number_periods = (Spinner) findViewById(R.id.report_number_periods);

        Integer [] num_pers = {5, 7, 10, 20, 31, 50, 100, 500, 1000, 0};
        number_periods.setAdapter(new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, num_pers));
        number_periods.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                lastXperiods = (int)adapterView.getSelectedItem();

                canvasview.postInvalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    float max;
    float min;
    float canvasheight;
    Goal.Type gtype;


    private int getYcanvasMax() {
        return (int)canvasheight - 80;
    }

    private float getYmin() {
        switch (gtype) {
            case Value:
                return min;
        }

        return 0;
    }

    private float valueYToCanvasY(float y) {
        int ycanmax = getYcanvasMax();

        float heightfac = ycanmax / (max - getYmin());

        return ycanmax - heightfac * ((y - getYmin())/2);
    }

    @Override
    public void onDraw(Canvas canvas) {

        if (goal==null) return;

        GoalDB db = GoalTender.getDatabase();

        // Goal goal = db.getGoal("Walking");

        List<Entry> entries = db.getAllEntriesForGoal(goal);

//        if (entries.size()  > lastXperiods) {
//            entries = entries.subList(0, lastXperiods);
//        }

        Goal.Period period = goal.getPeriod();
        gtype = goal.getType();


        Utils.MapOfLists<String,Entry> dateToEntry = new Utils.MapOfLists<>();
        Date firstdate = new Date();
        Date now = new Date();

        max = goal.getGoalnum();
        min = goal.getGoalnum();
        for (Entry e: entries) {
            if (e.getValue()>max) max = e.getValue();
            if (e.getValue()<min) min = e.getValue();
            if (firstdate.after(e.getDate())) firstdate = e.getDate();
            dateToEntry.put(Utils.formatDateForBucket(e.getDate(), period), e);
            //System.out.println("Date" + e.getDate() + " goes in datebucket " + Utils.formatDateForBucket(e.getDate(), period));
        }


        int xmin = 50;
        int xmax = canvas.getWidth() - 50;
        canvasheight = canvas.getHeight();




        System.out.println(" max: " + max);
        System.out.println(" min: " + min);
        System.out.println(" Entries: " + entries.size());
        System.out.println(canvas.getWidth() + " x " + canvas.getHeight());
        ////System.out.println(widthfac + " x " + heightfac);


        if (gtype != Goal.Type.Checkbox) {
            double ystep = ((double)max - getYmin()) / 5;
            for (double y = getYmin(); y <= max; y += ystep) {
                float h = valueYToCanvasY((float)y);
                canvas.drawText((int) y + "", 2, h, gtext);
                canvas.drawLine(xmin, h, xmax, h, glines);
            }
        }



        int xdatas = 0;
        for (Date d=firstdate; d.before(now) || d.equals(now); d = Utils.getNextDate(d, period)) {
            xdatas++;
        }

        int skip = 0;
        if (xdatas > lastXperiods) {
            skip = xdatas - lastXperiods;
            xdatas = lastXperiods;
        }

        double widthfac = ((double)xmax - (double)xmin) / (double)xdatas;

        double xpos = xmin * 1.2;


        double pad = widthfac/6;

        int xaccum = 90;

        int i=0;
        for (Date d=firstdate; d.before(now); d = Utils.getNextDate(d, period)) {
            // System.out.println("Datebucket " + Utils.formatDateForBucket(d, period));

            if (i++<skip) continue;

            if (xaccum>=90) {
                canvas.drawText(Utils.formatDateForShortDisplay(d, period), (float)xpos+4, valueYToCanvasY(getYmin())+29, gtext);
                xaccum=0;
                canvas.drawRect((float)xpos, valueYToCanvasY(getYmin()) - 10,
                        (float)xpos + 2, valueYToCanvasY(getYmin()) + 29, gtext);

            } else {
                canvas.drawRect((float)xpos, valueYToCanvasY(getYmin()) - 10,
                        (float)xpos + 1, valueYToCanvasY(getYmin()) + 5, glines);
            }
            xaccum+=widthfac;

            List<Entry> dateentries = dateToEntry.getAll(Utils.formatDateForBucket(d, period));
            if (dateentries!=null) {
                float val = 0;
                for(Entry entry: dateentries) {
                    if (goal.getType()== Goal.Type.Cumulative) {
                        val += entry.getValue();
                    } else {
                        val += entry.getValue();
                        break;
                    }
                }


                canvas.drawRect((float)(xpos+pad), valueYToCanvasY(val), (float)(xpos + widthfac-pad), valueYToCanvasY(getYmin()), gbars);
            }
            xpos += widthfac;
        }
    }


    public void onDrawOld(Canvas canvas) {

        if (goal==null) return;

        GoalDB db = GoalTender.getDatabase();

       // Goal goal = db.getGoal("Walking");

        List<Entry> entries = db.getAllEntriesForGoal(goal);

        Goal.Period period = goal.getPeriod();


        Utils.MapOfLists<String,Entry> dateToEntry = new Utils.MapOfLists<>();
        Date firstdate = new Date();
        Date now = new Date();
        float max = goal.getGoalnum();
        float min = goal.getGoalnum();
        for (Entry e: entries) {
            if (e.getValue()>max) max = e.getValue();
            if (e.getValue()<min) min = e.getValue();
            if (firstdate.after(e.getDate())) firstdate = e.getDate();
            dateToEntry.put(Utils.formatDateForBucket(e.getDate(), period), e);
            //System.out.println("Date" + e.getDate() + " goes in datebucket " + Utils.formatDateForBucket(e.getDate(), period));
        }

        int ymax = canvas.getHeight() - 50;
        int ymin = 0;
        int xmin = 50;
        int xmax = canvas.getWidth() - 10;

        float heightfac = ymax / max /2;




        System.out.println(" max: " + max);
        System.out.println(" min: " + min);
        System.out.println(" Entries: " + entries.size());
        System.out.println(canvas.getWidth() + " x " + canvas.getHeight());
        ////System.out.println(widthfac + " x " + heightfac);


        float ystep = max/5;
        for (float y=0; y<=max; y+=ystep) {
            float h = ymax - (y * heightfac);
            canvas.drawText((int)y+"", 2, h, glines);
            canvas.drawLine(xmin, h, xmax, h, glines);

        }



        int xdatas = 0;
        for (Date d=firstdate; d.before(now) || d.equals(now); d = Utils.getNextDate(d, period)) {
            xdatas++;
        }
        float widthfac = (xmax - xmin) / xdatas;

        float xpos = xmin;


        for (Date d=firstdate; d.before(now); d = Utils.getNextDate(d, period)) {
           // System.out.println("Datebucket " + Utils.formatDateForBucket(d, period));
            canvas.drawRect(xpos, ymax-5, xpos+1 , ymax+10, glines);

            List<Entry> dateentries = dateToEntry.getAll(Utils.formatDateForBucket(d, period));
            if (dateentries!=null) {
                for(Entry entry: dateentries) {
                    float val = entry.getValue() * heightfac;

                    canvas.drawRect(xpos, ymax - val, xpos + (widthfac / 2), ymax, gbars);
                }
            }
            xpos += widthfac;
        }
    }
}
