package com.oym.indoor.navigation;

import com.oym.indoor.location.IndoorLocation;
import com.oym.indoor.location.NotificationResult;

/**
 *  /!\ Beside implementation, needs to be (un)registred to GlobalState using
 * <tt>gs.addLocationCallback(this)</tt> or <tt>gs.removeLocationCallback(this)</tt>
 *  
 * @author jco
 *
 */
public interface IndoorLocationListener {

	void onLocationUpdate(IndoorLocation location);
	void onNotification(NotificationResult notification);
}
