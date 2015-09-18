package com.oym.indoor.navigation;

import com.oym.indoor.LocationResult;
import com.oym.indoor.NotificationResult;

/**
 *  /!\ Beside implementation, needs to be (un)registred to GlobalState using
 * <tt>gs.addLocationCallback(this)</tt> or <tt>gs.removeLocationCallback(this)</tt>
 *  
 * @author jco
 *
 */
public interface IndoorLocationListener {

	void onLocationUpdate(LocationResult location);
	void onNotification(NotificationResult notification);
}
