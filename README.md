# OYM Indoor SDK for Android

## Introduction

The indoor module is split in three different packages:
- Indoor SDK (com.oym.indoor)
- Indoor Location SDK (com.oym.indoor.location)
- Indoor Routing SDK (com.oym.indoor.routing)

## Indoor SDK

This package is the base of the indoor library, where the connection and interaction with the server is performed. In this package, the main objects to handle buildings, floors and iBeacons are introduced as well as the Indoor object that will handle all the communication and requests to the server.

### Floor class

This class includes all the information regarding the floor items stored in the server. As well, it includes the tile provider to be used to overlap the indoor maps in the Google Map.

Parameter | Type | Description
--------- | ---- | -----------
id | String | Unique ID
building | String | Building ID
floor | int | Floor number
type | String | String defining the floor type
tileProvider | UrlTileProvider | Tile provider for indoor tiles

### Building class

This class includes all the information regarding the Building shapes stored in the server. As well, it includes the floors related with the building.

Parameter | Type | Description
--------- | ---- | -----------
id | String | Unique ID
name | String | Building name
geometry | String | Building bounding box
type | String | String identifying building type
floors | SparseArray&lt;Floor&gt; | Array including the floors available in the building

### Ibeacon class

This class includes all the information regarding the iBeacon items stored in the server.

Parameter | Type | Description
--------- | ---- | -----------
id | String | Unique ID
x | double | WGS84 Longitude
y | double | WGS84 Latitude
major | int | iBeacon major
minor | int | iBeacon minor
txPower | int | Transmission power at 1m
uuidIbeacon | String | iBeacon UUID
building | String | Building ID
floor | String | Floor ID
floornumber | int | Floor number
type | String | String defining the OYMIbeacon type
scanDate | String | Scan date in ISO8601 format
status | int | Variable defining the iBeacon status

### Indoor class

The Indoor class is the basic object to perform any communication with the server and retrieve all the required information. It is provided with three constructors according to the user needs. The object is able to handle the reconnection to the server when it is lost due to timeout (30 min without activity).

Parameter | Type | Description
--------- | ---- | -----------
oymUrl | String | URL to the server
user | String | Username (user@indoor.test)
password | String | Password
callback | ConnectCallback | Callback to handle the login process
*reconnectMaxAttempts* | *int* | *Number of reconnect attempts*
*acCallback* | *AutoconnectCallback* | *Callback to handle the reconnect process*

The ConnectCallback includes an *onSucceed()* method that informs the user that the connection to the server has been established properly. As well, the callback provides a *onFailure(Exception exc)* method to alert that the connection was not successful and provides the exception to identify the problem.

The AutoconnectCallback provides feedback with respect the autoconnect feature. When the session is expired the *onDisconnect()* callback is called while the library tries to reconnect to the server. When the reconnection is successful, the *onSucceess()* callback is called. If the reconnection is not possible, the *onFailure(Exception exc)* is called an the library will no longer be useful.

To initialize an Indoor object we have three different constructors to be used according to the purpose:
- Constructor without autoconnect
```java
public Indoor(String oymUrl, String user, String password, ConnectCallback callback)
```
- Constructor with a fixed number (3) of reconnections
```java
public Indoor(String oymUrl, String user, String password, ConnectCallback callback, AutoconnectCallback acCallback)
```
- Constructor with an user-defined number of reconnections
```java
public Indoor(String oymUrl, String user, String password, ConnectCallback callback, int reconnectMaxAttempts, AutoconnectCallback acCallback)
```
Once the Indoor object is initialized, the ConnectCallback will be called after attempting to connect to the server directly.

##### Example
If the user `user@indoor.test` with password `password` wants to connect to the server located at `https://indoor.onyourmap.com`, with an already ConnectCallback defined called `callback` and without autoconnection, the user shall use:
```java
new Indoor(“https//indoor.onyourmap.com”, “user@indoor.test”, “password”, callback);
```

#### Get Buildings

Once the user is correctly logged in to the server, the user can start retrieving information from the server. In order to retrieve the buildings available, there are several functions to match the user requirements for each case. It can be specified a list of IDs to be retrieved, a bounding box to limit the search and pagination.

In order to handle the process a GetBuildingsCallback needs to be defined. This callback includes the method *onSucceed(List&lt;Building&gt; buildings)* that includes the list of buildings retrieved from the server. In case that the process fails, the *onFailure(Exception exc)* method will be called, with further information of the error.

Below the different methods to retrieve the buildings:
```java
getBuildings(GetBuildingsCallback callback)
```
```java
getBuildings(List&lt;String&gt; buildingIdList, GetBuildingsCallback callback)
```
```java
getBuildings(String boundingBox, GetBuildingsCallback callback)
```
```java
getBuildings(List&lt;String&gt; buildingIdList, String boundingBox, GetBuildingsCallback callback)
￼￼```
```java
getBuildings(List&lt;String&gt; buildingIdList, String boundingBox, int offset, int bucketSize, GetBuildingsCallback callback)
```	

Parameter | Type | Description
--------- | ---- | -----------
callback | GetBuildingsCallback | Callback to handle the process
*buildingIdList* | *List&lt;String&gt;* | *List of the building ID that needs to be retrieved*
*boundingBox* | *String* | *Bounding box to limit the search*
*offset* | *int* | *List offset to be retrieved*
*bucketSize* | *int* | *Number of elements to be retrieved*
	
##### Example
All the buildings available in the server need to be retrieved. The GetBuildingsCallback is already defined and stored as `callbackBuildings`. The user shall use:
```java
indoor.getBuildings(callbackBuildings);
```

#### Get iBeacons

In order to retrieve the iBeacons, it can be done by specifying the id(s) of the building or floor in which the iBeacons are located.

In order to handle the process a GetIbeaconsForBuildingOrFloorCallback needs to be defined. This callback includes the method *onSucceed(ArrayList&lt;Ibeacon&gt; iBeacons)* that includes the list of iBeacons retrieved from the server. In case that the process fails, the *onFailure(Exception exc)* method will be called, with further information of the error.

Below the different methods to retrieve the buildings:
```java
getIbeaconsForBuildingOrFloor(String id, GetIbeaconsForBuildingOrFloorCallback callback)
```
```java
getIbeaconsForBuildingOrFloor(ArrayList&lt;String&gt; ids, GetIbeaconsForBuildingOrFloorCallback callback)
```

Parameter | Type | Description
--------- | ---- | -----------
callback | GetIbeaconsForBuildingOrFloorCallback | Callback to handle the process
*id* | *String* | *Building or floor ID whose iBeacons are requested*
*ids* | *ArrayList&lt;String&gt;* | *Building or floor ID list whose iBeacons are requested*


## Indoor Location SDK

This package is the base of the location library, where the indoor position using iBeacons is computed. This package provides a location provider that provides an indoor position based on iBeacons when the user is inside a building, and a GPS position when the user is outside.

The indoor location provider is able to define geofences per each building. A geofence can be seen as a circular When the user is entering in one of these, the provider will start retrieving the list of iBeacon for the building the user have entered and listening for iBeacons nearby to compute the position.

In order to compute the indoor position it is required a Bluetooth low energy smartphone with Android 4.3 (API 18) or higher. It is also required to have the permission to access to the Bluetooth and user location. The Android manifest shall include the following statements:

```xml
<uses-sdk android:minSdkVersion="18"...
<!-- Bluetooth -->
<uses-permission android:name="android.permission.BLUETOOTH" /> <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<!-- GPS -->
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/> <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-feature android:name="android.hardware.bluetooth_le" android:required="true" />
```

In order to allow the indoor provider work properly, the indoor service needs to be defined in the Android manifest
```xml
<service android:name="com.oym.indoor.location.IndoorLocationService" android:exported="false" />
```

In order to work properly, the app shall include the following libraries:
- indoor-1.0.0.jar
- oym-links-sdk.1.0.jar
- httpclientandroidlib-1.1.2.jar
- jackson -core-asl-1.9.12.jar
- jackson -jaxrs-1.9.12.jar
- jackson -mapper-asl-1.9.12.jar
- jackson-xc-1.9.12.jar

The app shall include the Google Play Services in order to be able to use the Google Location Services API.

The next step is to define an IndoorLocationBroadcast to handle the computed position. This IndoorLocationBroadcast will be called each time a position is computed.
```java
private IndoorLocationBroadcast broadcast = new IndoorLocationBroadcast() {
	@Override
	public void onReceive(IndoorLocation location) { 
		// TODO Auto-generated method stub
	} };
```

### IndoorLocation class

This class includes all the fields necessary to get the user position, the location provider used and further useful information.

Parameter | Type | Description
--------- | ---- | -----------
latitude | double | WGS84 Latitude
longitude | double | WGS84 Longitude
used | int | Number of iBeacons used
accuracy | double | Position accuracy in meters
found | ArrayList&lt;Double&gt; | List including the longitude, latitude and accuracy for each iBeacon in sight
floornumber | int | Floor number
type | int | Positioning type: `TYPE_IBEACON`, `TYPE_FUSED`
bName | String | Building name
buildingId | String | Building ID
geofences | int | Number of geofences crossed

### IndoorLocationLib class

This class is the enter point for the indoor location provider. It offers the capability to start and stop the indoor location provider. It offers two types of indoor location:
- Based on the weighted average of the iBeacons in range (`TYPE_AVERAGE`)
- Based on the more powerful iBeacon in range (`TYPE_CLOSEST`)

Where if not defined, the weighted average type will be selected by default. It comes with different constructors according to the user needs:
- Constructor using the average type by default
```java
public IndoorLocationLib(Context context, String serverUrl, String username, String password)
```
- Constructor using the user-defined type
```java
public IndoorLocationLib(Context context, String serverUrl, String username, String password , int type)
```

Parameter | Type | Description
--------- | ---- | -----------
context | Context | Android context
serverUrl | String | Server URL
username | NSString* | Username (user@foo.bar)
password | NSString* | Password
*type* | *int* | *Indoor location type*

#### startLocate

Once the object is created the indoor location provider can be started. In order to work we need to provide an instance of the abovementioned IndoorLocationBroadcast. The user can also define a desired update rate for the position.

In order to handle the process a StartLocateCallback needs to be defined. This callback includes the method *onSucceed()* that informs the user that the indoor provider has been started properly. In case that the process fails, the *onFailure(Exception exc)* method will be called, with further information of the error.

Parameter | Type | Description
--------- | ---- | -----------
br | IndoorLocationBroadcast | Broadcast to handle the output from the library
callback | StartLocationCallback | Callback to handle the process
*refresh* | *long* | *Update rate in msec*

##### Example
If the user `user@indoor.test`, with password `pass` wants to connect to the server located at `https://indoor.onyourmap.com`, with an already StartLocationCallback defined called `callback` and IndoorLocationBroadcast called `br`, the user shall use:
```java
IndoorLocationLib lib = new IndoorLocationLib(context, "https://indoor.onyourmap.com", "user@indoor.test", "pass"); 
lib.startLocate(br, callback);
```

#### stopLocate

Once the indoor location provider is no longer used, the user shall stop it properly using the *stopLocate()* method.

## Indoor Routing SDK

This package is the base of the routing library, which is an extension of the com.oym.indoor package that includes routing features. This package also handles the interaction with the server, and includes the IndoorRouting object that will provide advanced routing methods.

### Area class

This class includes all the information regarding POI and places stored in the server.

Parameter | Type | Description
--------- | ---- | -----------
id | String | Unique ID
x | Number* | WGS84 Longitude
y | Number* | WGS84 Latitude
name | String | Area name
building | String | Buinding ID
floor | String | Floor ID
floornumber | Number | Floor number
geometry | String | Area boundix box
type | String | String defining the Area type

### InstructionType enumeration

This enumeration includes all the possible instructions supported for the routing algorithm.

Value | Description
----- | -----------
TURNLEFT | Turn left
TURNRIGHT | Turn right
DOWNSTAIRS | Walk downstairs
UPSTAIRS | Walk upstairs
DOWNELEVATOR | Go downstairs with a lift
UPELEVATOR | Go upstairs with a lift
ARRIVAL | Arrive to your destination
STRAIGHT | Go straight ahead

### Instruction class

This class defines an instruction being used in the routing algorithm.

Parameter | Type | Description
--------- | ---- | -----------
distance | double | Distance from the starting point
instruction | InstructionType | Instruction to apply at that distance
props | HashMap&lt;String, String&gt; | Hashmap including extra values to complete the instruction

### RoutePoint class

This class helps to define a point with information of the floor and building to which is targeting.

Parameter | Type | Description
--------- | ---- | -----------
x | double | WGS84 Longitude
y | double | WGS84 Latitude
floornumber | int | Floor number
buildingId | String | Building ID

### RouteProjectedPoint class

This class extends the RoutePoint class by adding further useful information to describe points projected in the user route. The projected point will always lay on the route.

Parameter | Type | Description
--------- | ---- | -----------
x | double | WGS84 Longitude
y | double | WGS84 Latitude
floornumber | int | Floor number
buildingId | String | Building ID
distanceFromStart | double | Distance in meters from the starting point
distanceToShape | double | Distance between the user and the shape
bearing | double | Orientation of the route segment in which the user is, counted clockwise from the north

### RoutingResult class

This class helps the developer to project the current position into a route. As well provides information whether the user is too far away from the route and the route should be recomputed.

Parameter | Type | Description
--------- | ---- | -----------
projectedPoint | RouteProjectedPoint | User position to the route
isRecomputeRequired | boolean | Flag indication whether the user should recompute its route

### IndoorRouting class

The IndoorRouting class inheritates from the Indoor class, hence it will handle all the communication with the server. In addition of all the features Indoor class provides, this class allows the user to retrieve the list of places/POI from the server. It has the same three constructors described for the Indoor class.

#### Get Area

Once the user is correctly logged in to the server as explained in the Indoor class, the user can start retrieving information from the server. In order to retrieve the places/POI available, there are several functions to match the user requirements for each case. It can be specified a single ID or a list of IDs to be retrieved.

In order to handle the process a GetAreaCallback needs to be defined. This callback includes the method *onSucceed(List&lt;Area&gt; areas)* that includes the list of places/POI retrieved from the server. In case that the process fails, the *onFailure(Exception exc)* method will be called, with further information of the error.

### Routing class

This class handles the Routing algorithm interface to the developer. It is required when the user wants to create a route from A to B. in order to initialize the object, the building ID where the routing is required and an initialized and logged in IndoorRouting object shall be provided.

Parameter | Type | Description
--------- | ---- | -----------
indoor | IndoorRouting | Initialized and logged in IndoorRouting object
bId | String | Building ID whose routing needs to be started

#### init

Once the object has been properly created it needs to be initialized. If the object is not correctly initialized the user will not be able to compute routes.

In order to handle the process a RoutingCallback needs to be defined. This callback includes the method *onSucceed()* that indicates that the object has been initialized properly. In case that the process fails, the *onFailure(Exception exc)* method will be called, with further information of the error.

##### Example
The Routing object needs to be created and initialized for the building with ID defined in `buildingId` and the IndoorRouting object `indoor` is initialized and logged in. The RoutingCallback is already defined under  `callback`.
```java
Routing routing = new Routing(indoor, buildingId); routing.init(callback);
```

#### Compute Route

This method is used to compute a route from one point to another. In order to work it requires a RoutePoint to identify the start point and another RoutePoint to identify the destination.

##### Example
It is required to compute a route from the defined RoutePoint `start` to the defined destination RoutePoint `destination` using the Routing object `routing` already defined and initialized.
```java
Route route = routing.computeRoute(start, destination);
```

### Route class

This object defines the route to be followed. It includes information regarding the points, instructions to be shown, distance in meters and an estimation of the route time.

Parameter | Type | Description
--------- | ---- | -----------
route | ArrayList&lt;RoutePoint&gt; | List of OYMRoutePoint containing the route
instructions | List&lt;Instruction&gt; | List of OYMInstruction to follow
distance | double | Route distance in meters
time | int | Route time in seconds
