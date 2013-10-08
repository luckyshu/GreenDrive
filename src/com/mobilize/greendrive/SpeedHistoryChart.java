package com.mobilize.greendrive;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;

import android.text.format.Time;

public class SpeedHistoryChart extends AbstractDemoChart {

	static final private int DAY_RECORD = 1;
	static final private int WEEK_RECORD = 2;
	static final private int MONTH_RECORD = 3;

	public String getName() {
		return "Driving Time History";
	}

	public String getDesc() {
		return "Randomly generated values for the scatter chart";
	}

	public Intent execute(Context context, ArrayList<MonitorData> datas,
			int type) {
		String[] titles = new String[] { "Speed" };
		List<double[]> x = new ArrayList<double[]>();
		List<double[]> values = new ArrayList<double[]>();
		int length = titles.length;
		int[] colors = new int[] { Color.rgb(164, 198, 57)};
		PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE };
		XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
		renderer.setLabelsTextSize(12);
		renderer.setAxisTitleTextSize(14);
		renderer.setXLabelsAlign(Align.CENTER);
		renderer.setYLabelsAlign(Align.RIGHT);
		renderer.setZoomEnabled(false, false);
		renderer.setPanEnabled(false, false);
		renderer.setApplyBackgroundColor(true); 
	    renderer.setBackgroundColor(Color.rgb(255, 250, 250)); 
	    renderer.setMarginsColor(Color.rgb(255, 250, 250));
	    renderer.setMargins(new int[] {50, 40, 15, 20});
		length = renderer.getSeriesRendererCount();
		for (int i = 0; i < length; i++) {
			((XYSeriesRenderer) renderer.getSeriesRendererAt(i))
					.setFillPoints(true);
		}

		int count = datas.size();
		Time time = new Time();
    	
    	long current = System.currentTimeMillis();
    	Date now = new Date(current);
 
		switch (type) {
		case (DAY_RECORD):
			for (int i = 0; i < length; i++) {
				double[] xValues = new double[count];
				double[] yValues = new double[count];
				for (int k = 0; k < count; k++) {
					time.set(datas.get(k).getTime());
					xValues[k] = time.hour + time.minute / 60.0 + time.second / 3600.0;
					yValues[k] = datas.get(k).getVelocity();
				}
				x.add(xValues);
				values.add(yValues);
			}
			setChartSettings(renderer, "Driving Time History of Today", "Time(h)",
					"Speed(MPH)", 0, 25, 0, 80, Color.DKGRAY, Color.DKGRAY);
			renderer.setXLabels(14);
			renderer.setYLabels(10);
			break;

		case (WEEK_RECORD):
			for (int i = 0; i < length; i++) {
				double[] xValues = new double[count];
				double[] yValues = new double[count];
				for (int k = 0; k < count; k++) {
					time.set(datas.get(k).getTime());
					xValues[k] = time.weekDay + (time.hour + time.minute / 60.0 + time.second / 3600.0)/24;
					yValues[k] = datas.get(k).getVelocity();
				}
				x.add(xValues);
				values.add(yValues);
			}
			setChartSettings(renderer, "Driving Time History in this Week", "Day",
					"Speed(MPH)", 0, 7, 0, 80, Color.DKGRAY, Color.DKGRAY);
			renderer.setXLabels(8);
			renderer.setYLabels(10);
			break;

		case (MONTH_RECORD):
			for (int i = 0; i < length; i++) {
				double[] xValues = new double[count];
				double[] yValues = new double[count];
				for (int k = 0; k < count; k++) {
					time.set(datas.get(k).getTime());
					xValues[k] = time.monthDay + (time.hour + time.minute / 60.0 + time.second / 3600.0) / 24 / 30;
					yValues[k] = datas.get(k).getVelocity();
				}
				x.add(xValues);
				values.add(yValues);
			}
			setChartSettings(renderer, "Driving Time History in this Month", "Day",
					"Speed(MPH)", 0, 32, 0, 80, Color.DKGRAY, Color.DKGRAY);
			renderer.setXLabels(15);
			renderer.setYLabels(10);
			break;
		}

		return ChartFactory.getScatterChartIntent(context, buildDataset(titles,
				x, values), renderer);
	}

	public Intent execute(Context context) {
		// TODO Auto-generated method stub
		return null;
	}

}
