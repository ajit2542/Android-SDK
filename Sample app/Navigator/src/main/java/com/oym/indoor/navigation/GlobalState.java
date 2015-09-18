package com.oym.indoor.navigation;

import android.app.Application;
import android.content.SharedPreferences;

import com.oym.indoor.GoIndoor;
import com.oym.indoor.Route;

import java.util.ArrayList;

public class GlobalState extends Application {

	private GoIndoor go;
	private ArrayList<IndoorLocationListener> callbacksLocation = new ArrayList<IndoorLocationListener>();
	private Route route;
	private SharedPreferences sharedPrefs;
	

	public GoIndoor getGoIndoor() {
		return go;
	}

	public void setGoIndoor(GoIndoor goIndoor) {
		go = goIndoor;
	}

	public ArrayList<IndoorLocationListener> getLocationCallback() {
		synchronized(callbacksLocation) {
			return callbacksLocation;
		}
	}
	
	public void addLocationCallback(IndoorLocationListener listener) {
		synchronized(callbacksLocation) {
			callbacksLocation.add(listener);
		}
	}

	public void removeLocationCallback(IndoorLocationListener listener) {
		synchronized(callbacksLocation) {
			callbacksLocation.remove(listener);
		}
	}

	public Route getRoute() {
		return route;
	}

	public void setRoute(Route route) {
		this.route = route;
	}

	public SharedPreferences getSharedPrefs() {
		return sharedPrefs;
	}

	public void setSharedPrefs(SharedPreferences sharedPrefs) {
		this.sharedPrefs = sharedPrefs;
	}	
	
}
