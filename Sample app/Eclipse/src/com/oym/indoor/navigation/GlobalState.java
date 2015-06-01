package com.oym.indoor.navigation;

import java.util.ArrayList;

import android.app.Application;
import android.content.SharedPreferences;

import com.oym.indoor.location.IndoorLocationLib;
import com.oym.indoor.routing.IndoorRouting;
import com.oym.indoor.routing.Route;
import com.oym.indoor.routing.Routing;

public class GlobalState extends Application {

	private IndoorRouting links;
	private IndoorLocationLib indoorLib;
	private ArrayList<IndoorLocationListener> callbacksLocation = new ArrayList<IndoorLocationListener>();
	private Route route;
	private Routing routing;
	private SharedPreferences sharedPrefs;
	

	public IndoorRouting getLinks() {
		return links;
	}

	public void setLinks(IndoorRouting links) {
		this.links = links;
	}

	public IndoorLocationLib getIndoorLib() {
		return indoorLib;
	}

	public void setIndoorLib(IndoorLocationLib indoorLib) {
		this.indoorLib = indoorLib;
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

	public Routing getRouting() {
		return routing;
	}

	public void setRouting(Routing routing) {
		this.routing = routing;
	}

	public SharedPreferences getSharedPrefs() {
		return sharedPrefs;
	}

	public void setSharedPrefs(SharedPreferences sharedPrefs) {
		this.sharedPrefs = sharedPrefs;
	}	
	
}
