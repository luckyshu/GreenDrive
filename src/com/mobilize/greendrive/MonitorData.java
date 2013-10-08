package com.mobilize.greendrive;

public class MonitorData {
	private long time;
	private double velocity;
	private double longtitude;
	private double latitude;
	
	public long getTime() {return time;}
	public double getVelocity() {return velocity;}
	public double getLatitude() {return latitude;}
	public double getLongtitude() {return longtitude;}

	public MonitorData (long _time, double _lat, double _lng, double _v)
	{
		time = _time;
		longtitude = _lng;
		latitude = _lat;
		velocity = _v;
	}
}
