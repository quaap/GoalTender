package com.quaap.goaltender;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import com.quaap.goaltender.storage.Entry;
import com.quaap.goaltender.storage.Goal;
import com.quaap.goaltender.storage.GoalDB;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ReportActivity extends Activity implements CanvasView.OnDrawListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        CanvasView canvasview = (CanvasView)findViewById(R.id.canvas_view);
        canvasview.setOnDrawListener(this);

    }


    @Override
    public void onDraw(Canvas canvas) {
        Paint p = new Paint();
        p.setColor(Color.RED);
        p.setTextSize(30);

        canvas.drawRect(10,10,30,30, p);

        GoalDB db = GoalTender.getDatabase();

        Goal goal = db.getGoal("Walking");

        List<Entry> entries = db.getAllEntriesForGoal(goal);

        Goal.Period period = goal.getPeriod();

        Entry latest = entries.get(0);
        Entry first = entries.get(entries.size()-1);


        Map<String,Entry> dateToEntry = new TreeMap<>();
        Date firstdate = new Date();
        Date now = new Date();
        float max = 0;
        float min = Integer.MAX_VALUE;
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
            canvas.drawText((int)y+"", 2, h, p);
            canvas.drawLine(xmin, h, xmax, h, p);

        }



        int xdatas = 0;
        for (Date d=firstdate; d.before(now) || d.equals(now); d = Utils.getNextDate(d, period)) {
            xdatas++;
        }
        float widthfac = (xmax - xmin) / xdatas;

        float xpos = xmin;


        for (Date d=firstdate; d.before(now); d = Utils.getNextDate(d, period)) {
           // System.out.println("Datebucket " + Utils.formatDateForBucket(d, period));
            canvas.drawRect(xpos, ymax-5, xpos+1 , ymax+10, p);

            Entry entry = dateToEntry.get(Utils.formatDateForBucket(d, period));
            if (entry!=null) {
                float val = entry.getValue() * heightfac;

                canvas.drawRect(xpos, ymax - val, xpos+(widthfac/2) , ymax, p);
            }
            xpos += widthfac;
        }
    }
}
