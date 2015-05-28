# OYM Indoor SDK for Android

## Introduction

The indoor module is split in three different packages:
- Indoor SDK (com.oym.indoor)
- Indoor Location SDK (com.oym.indoor.location)
- Indoor Routing SDK (com.oym.indoor.routing)

## Preparing the environment
### AndroidManifest.xmlThe minimum SDK version that the app should be API 18.

```xml
<uses-sdk        android:minSdkVersion="18"… ```
We need to give the app the following permission to be able to use Bluetooth and use the Google Location Services API```xml<!-- Bluetooth --><uses-permission android:name="android.permission.BLUETOOTH" /><uses-permission android:name="android.permission.BLUETOOTH_ADMIN" /><!-- GPS --><uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/><uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />```
We have to ensure that the device is capable to use Bluetooth Low Energy (Bluetooth 4.0), hence the following feature shall be included```xml<uses-feature        android:name="android.hardware.bluetooth_le"        android:required="true" />```
Finally we need to define the Indoor Service to be used```xml<application…        <service            android:name="com.oym.indoor.location.IndoorLocationService"            android:exported="false" />```

### Importing libraries
In order to work properly, the app shall include the following libraries:
-	indoor-1.2.0.jar-	oym-links-sdk.1.4.1.jar-	httpclientandroidlib-1.1.2.jar-	jackson-core-asl-1.9.12.jar-	jackson-jaxrs-1.9.12.jar-	jackson-mapper-asl-1.9.12.jar-	jackson-xc-1.9.12.jarThe app shall include the Google Play Services in order to be able to use the Google Location Services API.## Preparing a sample appIn order to use the indoor library, a basic Android app shall be created. After making all the modifications mentioned in the previous section, it is necessary to bear in mind that the application is using the Google Services API, Bluetooth and WiFi/Network connection, hence it is necessary to check that all this features are available.
The next step is to define an `IndoorLocationBroadcast` to handle the computed position. This `IndoorLocationBroadcast` will be called each time a position is computed.```javaprivate IndoorLocationBroadcast broadcast = new IndoorLocationBroadcast() {		@Override		public void onReceive(IndoorLocation location) {			// TODO Auto-generated method stub		}	};```
The `IndoorLocation` object includes the following fields:```java/** WGS84 Longitude */public final double longitude;/** WGS84 Latitude */public final double latitude;/** Number of iBeacons used */public final int used;/** Position accuracy (meters) */public final double accuracy;/** List including the longitude, latitude and accuracy of each iBeacon in sight */public final ArrayList<Double> found;
/** Floor number */public final int floor;/** Positioning type: {@link TYPE_IBEACON}, {@link TYPE_FUSED} */public final int type;/** Building name */public final String bName;/** Number of geofences crossed */public final int geofences;```

The following step is to create a new instance of the indoor location library:```java
lib = new IndoorLocationLib(context, oymWebservicesUrl, oymUser, oymPassword);```The final step to start the library is provide the `IndoorLocationBroadcast` to the library. We also provide a `StartLocateCallback` that will handle the start of the service and provide feedback whether the action was successful or not to de developer. To do that we use the `startLocate()` method, where the input is the `IndoorLocationBroadcast`, the `StartLocateCallback` and the third optional parameter is the update rate in milliseconds. If the update rate is not specified, `DEFAULT_REFRESH` value from `IndoorLocationService` will be assumed.```javalib.startLocate(br, startCallback);```After starting the library, the `IndoorBroadcastReceiver` will be used when a new position is computed.### Exiting the app
In order to stop properly the library, it is necessary to call the `stopLocate()` method when the library is no longer needed and the location service should be stopped```javalib.stopLocate();```

## Creating a routeIn order to create a route, an instance of `Routing` needs to be created and initialized. The constructor requires an `IndoorRouting` object that has been initialized and the building ID where the routing needs to be computed. Then the object needs to be initialized, providing to the function a `RoutingCallback` that will inform whether the init process was successful.

```javaRouting routing = new Routing(indoor, buildingId);routing.init(callback);```
Once the routing object has been successfully initialized, a route between two points can be computed. For that, the position of the start and end points need to be encoded inside a `RoutePoint` object.

```javapublic RoutePoint(double x, double y, int floornumber, String buildingId)```
To get the `Route` object, it just required to use the `computeRoute()` method:

```javaRoute route = routing.computeRoute(startRoutePoint, destinationRoutePoint);```
### Projecting position to the routeOnce a route is computed, it is possible to project the user position to the computed route by using the `getProjection()` method inside the Routing object. This method will provide a `RoutingResult` that will provide the user position on top of the route, a flag telling whether the user is too far away from the route and further useful information.## Showing indoor maps
In order to show the indoor maps overlap in Google Map, this guide assumes that the app already have an initialized GoogleMap object called map. Further information in how to include a map in your app can be found [here](https://developers.google.com/maps/documentation/android/).The first step is to retrieve the buildings from the server. In order to do that we use the `Indoor` class, inside com.oym.indoor package. The following constructor shall be used. In there the webservices Url, username and password shall be provided. The method already handles the connection to the server, hence a `ConnectCallback` shall be used as input parameter.```java	Indoor indoor = new Indoor(oymWebservicesUrl, user, password, callback);	public interface ConnectCallback {		/**		 *  This method is called when the connection to the server has 		 * been correctly established. 		 */		public void onSucceed();		/**		 *  This method is called when an exception is thrown when 		 * trying to connect to the server.		 * 		 * @param exc {@link Exception} thrown		 */		public void onFailure(Exception exc);	}```
When the connection is established successfully, the next step is to retrieve the buildings from the server. For that, with the Indoor object we created, we use the `getBuildings()` methods. There are simplified versions reducing input parameters of this method, which might apply for certain cases.```javapublic void getBuildings(List<String> buildingIdList, String boundingBox, int offset, int bucketSize, GetBuildingsCallback callback)
```In there we specify a list of buildingId to be retrieved, the bounding box in which the search shall be performed, an offset for the results and the max size of the result. As well, we need to provide a `GetBuildingsCallback` to check whether the operation has been successful. ```javapublic interface GetBuildingsCallback {	/**	 *  This method is called when the buildings are correctly 	 * retrieved from the server.	 * 	 * @param buildings List of {@link Building} objects retrieved 	 *  from the server	 */	public void onSucceed(List<Building> buildings);	/**	 *  This method is called when an exception is thrown when 	 * trying to retrieve the {@link Building} objects from the 	 * server.	 * 	 * @param ex {@link Exception} thrown	 */	public void onFailure(Exception ex);}
```When the building list has been successfully retrieved, all the information needed is already retrieved. In each Building object, it includes all the floors available for that building, including their tile providers. In order to access an `UrlTileProvider` we just need to call the `getTileProvider()` method in a Floor object:```javaArrayList<Floor> floors = buildings.get(iBuilding).getFloorsList();UrlTileProvider tiles = floors.get(iFloor).getTileProvider();```
Finally, this `UrlTileProvider` can be directly added to the map. ```javamap.addTileOverlay(new TileOverlayOptions().tileProvider(tiles));```

### Exiting the appBefore exiting the app it is advisable to disconnect from the server. For that, the `disconnect()` method shall be called.```javaindoor.disconnect();```