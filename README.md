# Goindoor SDK for Android [ ![Download](https://api.bintray.com/packages/goindoor/maven/goindoor/images/download.svg) ](https://bintray.com/goindoor/maven/goindoor/_latestVersion)

## Introduction

The Goindoor library handles the communication to the server and provides easy access to the developer to the data, as well as several addition features, such as location provider, routing, statistics and asset management.
An extensive developer documentation can be found [here](http://indoor-onyourmap.github.io/Android-SDK/).


## Preparing the environment
### AndroidManifest.xml
The minimum SDK version that the app should be API 18.

```xml
<uses-sdk
        android:minSdkVersion="18"…
```

We need to give the app the following permission to be able to use Bluetooth and use the Google Location Services API

```xml
<!-- Internet -->
<uses-permission android:name="android.permission.INTERNET" />
<!-- Bluetooth -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<!-- GPS -->
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

We have to ensure that the device is capable to use Bluetooth Low Energy (Bluetooth 4.0), hence the following feature shall be included

```xml
<uses-feature
        android:name="android.hardware.bluetooth_le"
        android:required="true" />
```

Finally we need to define the Indoor Location Service to be used

```xml
<application…
        <service
            android:name="com.oym.indoor.LocationService"
            android:exported="false" />
```

### Importing libraries
The Goindoor library can be downloaded from the jcenter and maven central repositories using the following statement:

```groovy
compile 'com.oym.indoor:goindoor:2.3.3'
```

It might be required to add the following lines inside the android closure:

```groovy
packagingOptions {
    exclude 'META-INF/ASL2.0'
    exclude 'META-INF/LICENSE'
    exclude 'META-INF/NOTICE'
}
```


## Preparing a sample app
In order to use the indoor library, a basic Android app shall be created. After making all the modifications mentioned in the previous section, it is necessary to bear in mind that the application is using the Google Services API, Bluetooth and WiFi/Network connection, hence it is necessary to check that all this features are available.

The next step is to define an `LocationBroadcast` to handle the computed position. This `LocationBroadcast` will be called each time a position is computed.

```java
private LocationBroadcast broadcast = new LocationBroadcast() {
		@Override
		public void onLocation(LocationResult location) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onNotification(NotificationResult notification) {
			// TODO Auto-generated method stub
		}
	};
```

The `LocationResult` object includes the following fields:

```java
/** WGS84 Longitude */
public final double longitude;
/** WGS84 Latitude */
public final double latitude;
/** Number of beacons used */
public final int used;
/** Position accuracy (meters) */
public final double accuracy;
/** List including the longitude, latitude and accuracy of each beacon in sight */
public final ArrayList<Double> found;
/** Floor ID */
public final String floor;
/** Floor number */
public final int floorNumber;
/** Positioning type: {@link #TYPE_BEACON}, {@link #TYPE_FUSED} */
public final int type;
/** Building name */
public final String buildingName;
/** Building ID */
public final String building;
/** Number of geofences */
public final int geofences;
```

To create a GoIndoor object, the inner Builder class needs to be used:

```java
go = new GoIndoor.Builder()
                .setContext(context)
                .setAccount(account)
                .setPassword(password)
                .setConnectCallback(callback)
                .build();
```

This constructor will connect to the server. Once the process is complete the `onConnected()` method from the callback will be called.

In order to start the location service, it is required to provide an `LocationBroadcast` to the library. Once it is defined, the `startLocate()` method can be used, where the input is the `LocationBroadcast`

```java
go.startLocate(br);
```

After starting the library, the `LocationBroadcast` will be used when a new position is computed and a notification is triggered.

### Exiting the app
In order to stop properly the library, it is necessary to call the `stopLocate()` method when the library is no longer needed and the location service should be stopped

```java
go.stopLocate();
```

## Creating a route
In order to create a route between two points, these points need to be encoded inside a `RoutePoint` object.

```java
public RoutePoint(double x, double y, int floorNumber, String buildingId)
```

To get the `Route` object, it just required to use the `computeRoute()` method:

```java
Route route = go.computeRoute(startRoutePoint, destinationRoutePoint);
```

### Projecting position to the route
Once a route is computed, it is possible to project the user position to the computed route by using the `getProjection()` method inside the Route object. This method will provide a `RoutingResult` that will provide the user position on top of the route, a flag telling whether the user is too far away from the route and further useful information.


## Showing indoor maps
In order to show the indoor maps overlap in Google Map, this guide assumes that the app already have an initialized GoogleMap object called map. Further information in how to include a map in your app can be found [here](https://developers.google.com/maps/documentation/android/).

The Goindoor library includes a list of the buildings available for the account. In each Building object, it includes all the floors available for that building, including their tile providers. In order to access an `UrlTileProvider` we just need to call the `getTileProvider()` method in a Floor object:

```java
ArrayList<Floor> floors = building.getFloorsList();
UrlTileProvider tiles = floors.get(iFloor).getTileProvider();
```

Finally, this `UrlTileProvider` can be directly added to the map.

```java
map.addTileOverlay(new TileOverlayOptions().tileProvider(tiles));
```


### Exiting the app
Before exiting the app it is advisable to disconnect from the server. For that, the `disconnect()` method shall be called.

```java
go.disconnect();
```
