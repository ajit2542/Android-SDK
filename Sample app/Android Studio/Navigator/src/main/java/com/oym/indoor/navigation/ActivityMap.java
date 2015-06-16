package com.oym.indoor.navigation;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.oym.indoor.Building;
import com.oym.indoor.Floor;
import com.oym.indoor.Indoor;
import com.oym.indoor.Indoor.GetBuildingsCallback;
import com.oym.indoor.JSON;
import com.oym.indoor.location.IndoorLocation;
import com.oym.indoor.location.NotificationResult;
import com.oym.indoor.navigation.views.CustomListView;
import com.oym.indoor.navigation.views.CustomListView.CustomItems;
import com.oym.indoor.navigation.views.CustomListView.CustomSection;
import com.oym.indoor.navigation.views.CustomListView.CustomSingleItem;
import com.oym.indoor.navigation.views.CustomListView.CustomSingleItemImage;
import com.oym.indoor.navigation.views.CustomListView.EntryAdapter;
import com.oym.indoor.routing.Area;
import com.oym.indoor.routing.IndoorRouting.GetAreaCallback;
import com.oym.indoor.routing.Instruction;
import com.oym.indoor.routing.Instruction.InstructionType;
import com.oym.indoor.routing.InstructionAdapter;
import com.oym.indoor.routing.Route;
import com.oym.indoor.routing.RoutePoint;
import com.oym.indoor.routing.RouteProjectedPoint;
import com.oym.indoor.routing.Routing;
import com.oym.indoor.routing.RoutingResult;

import java.util.ArrayList;
import java.util.List;

public class ActivityMap extends ActionBarActivity implements IndoorLocationListener {

	private static final float MAP_CIRCLE_STROKE = 5;
	private static final float MAP_ROUTE_WIDTH = 10;
	private static final float MAP_TILT_NAVIGATION = 30;
	private static final int MAP_TIME_FIRSTUPDATE = 1000;
	private static final int MAP_TIME_MARKER = 500;
	private static final int MAP_TIME_NAVIGATION = 250;
	private static final float MAP_ZINDEX_TILES = 1;
	private static final float MAP_ZINDEX_ROUTE = 2;
	private static final float MAP_ZINDEX_CIRCLE = 3;
	private static final float MAP_ZOOM_DEFAULT = 19;
	private static final String NOTIFICATION_KEY_MESSAGE = "msg";
	private static final double ROUTE_ARRIVAL_THRESHOLD = 2;

	// View	
	private ActionBar actionBar;
	private DrawerLayout drawerLayout;
	private ActionBarDrawerToggle drawerToggle;
	private ListView drawerList;
	private AlertDialog alert;
	private TextView fubDestination;
	private TextView fubLocal;
	private TextView fubNumber;
	private TextView fbbDistance;
	private TextView fbbInstruction;
	private ImageView fbbImage;
	private Fragment fub;
	private Fragment fbb;
	private ProgressDialog progressDialog;
	private ImageView myLocImage;
	private RelativeLayout myLocLayout;
	private Bitmap markerBitmap;

	// Map
	private GoogleMap map;
	private TileOverlay tileOverlay;
	private Marker markerPos;
	private Circle circlePos;
	private ArrayList<Polyline> mapRoute;
	private int lastFloornumber;

	// Flags
	private boolean avoidDestroy = false;
	private boolean isFirstMapUpdate = false;
	private boolean isMapUpdated = false;
	private boolean isFbbShown = false;
	private boolean isNavigation = false;
	private boolean isCameraUpdated = false;
	private boolean isBuildingReady = false;
	private boolean isNavigationReady = false;

	// Stored position
	private static final String KEY_STORED_POSITION = "OYM_KEY_STORED_POSITION";
	private Bitmap storedBitmap;
	private Marker storedMarker;
	private RoutePoint storedPos;
	private CustomItems storeItem;
	private CustomItems restoreItem;
	private CustomItems deleteItem;

	// Demo
	private ArrayList<Marker> markers = new ArrayList<Marker>();
	private ArrayList<Circle> circles = new ArrayList<Circle>();

	// Stats
	private int logCount = 0;

	// Navigation
	private ArrayList<CustomItems> drawerAreas;
	private Instruction instruction;
	private IndoorLocation currentPosition;
	private final Object mutexPosition = new Object();
	private StartNav taskStartNav;
	private RoutePoint destination;

	private GlobalState gs;
	private Context context;
	private Handler handler;
	private Building building;
	private ArrayList<Floor> floors;
	private int currentFloor;

	// Notifications
	private NotificationManager notificationManager;
	private int notCounter = 1;


	private CancelableCallback callbackMapCancelable = new CancelableCallback() {

		@Override
		public void onFinish() {
			handler.removeCallbacks(runnableEnableMap);
			isMapUpdated = true;
		}

		@Override
		public void onCancel() {
			handler.removeCallbacks(runnableEnableMap);
			isMapUpdated = true;
		}
	};

	private GetBuildingsCallback callbackBuildingsInit = new GetBuildingsCallback() {
		@Override
		public void onSucceed(List<Building> list) {

		}

		@Override
		public void onFailure(Exception e) {
			if (e.getMessage().equals(Indoor.EXCEPTION_NO_BUILDINGS)) {
				Toast.makeText(context, R.string.AMTNoBuildings, Toast.LENGTH_LONG).show();
			}
		}
	};

	private GetBuildingsCallback callbackBuildings = new GetBuildingsCallback() {

		@Override
		public void onSucceed(List<Building> buildings) {
			building = buildings.get(0);
			fubLocal.setText(building.getName());

			// Prepare routing
			gs.setRouting(new Routing(gs.getLinks(), building.getId()));
			gs.getRouting().init(callbackRouting);

			isBuildingReady = true;

			// Retrieve areas
			gs.getLinks().getArea(building.getId(), callbackAreas);

			ArrayList<Integer> l = new ArrayList<Integer>();
			for (Floor f : building.getFloorsList()) {
				l.add(f.getFloor());
				floors.add(f);
			}
		}

		@Override
		public void onFailure(Exception ex) {
			building = null;
		}
	};

	private Routing.RoutingCallback callbackRouting = new Routing.RoutingCallback() {
		@Override
		public void onSucceed() {
			isBuildingReady = true;
			isNavigationReady = true;
		}

		@Override
		public void onFailure(Exception exc) {
			isNavigationReady = false;
//            building = null;
			Log.e("JC", "Error Routing: ", exc);

			if (exc.getMessage().equals(Routing.EXCEPTION_NO_EDGES) && building != null) {
				Toast.makeText(context, getString(R.string.AMTNoEdges, building.getName()), Toast.LENGTH_LONG).show();
			}
		}
	};


	private GetAreaCallback callbackAreas = new GetAreaCallback() {

		@Override
		public void onSucceed(List<Area> areas) {
			drawerAreas = new ArrayList<>();
			for (Area a : areas) {
				// FIXME
				CustomSingleItem csi = new CustomSingleItem(a.getName(),
						new RoutePoint(a.getX(), a.getY(), a.getFloornumber(), a.getBuilding()));
				drawerAreas.add(csi);
			}
			populateDrawer();
		}

		@Override
		public void onFailure(Exception exc) {
			if (building != null) {
				gs.getLinks().getArea(building.getId(), callbackAreas);
			}
		}
	};

	private BroadcastReceiver changeConnection = new BroadcastReceiver() {
		private boolean hasInternet = true;
		@Override
		public void onReceive(Context context, Intent intent) {
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
			NetworkInfo ani = cm.getActiveNetworkInfo();

			if ((ani == null || !ani.isConnected()) && hasInternet) {
				Toast.makeText(context, R.string.AMTNoInternet, Toast.LENGTH_LONG).show();
				hasInternet = false;
			} else if (ani != null && ani.isConnected()) {
				hasInternet = true;
			}
		}
	};

	private Runnable runnableEnableMap = new Runnable() {

		@Override
		public void run() {
			isMapUpdated = true;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_map);

		gs = (GlobalState) getApplication();
		context = this;
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		handler = new Handler();
		floors = new ArrayList<Floor>();

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerList = (ListView) findViewById(R.id.DList);
		fub = getSupportFragmentManager().findFragmentById(R.id.MUpperBar);
		fbb = getSupportFragmentManager().findFragmentById(R.id.MBottomBar);
		fubDestination = (TextView) findViewById(R.id.FUBDestination);
		fubLocal = (TextView) findViewById(R.id.FUBLocal);
		fubNumber = (TextView) findViewById(R.id.FUBNumber);
		fbbDistance = (TextView) findViewById(R.id.FBBDistance);
		fbbInstruction = (TextView) findViewById(R.id.FBBInstruction);
		fbbImage = (ImageView) findViewById(R.id.FBBImage);
		myLocImage = (ImageView) findViewById(R.id.MMyLocation);
		myLocLayout = (RelativeLayout) findViewById(R.id.MMyLocationLayout);

		// Check if there are buildings
		gs.getLinks().getBuildings(callbackBuildingsInit);

		// Prepare view
		hideUpperBar();
		hideBottomBar();
		registerForContextMenu(fbb.getView());
		setMarkerBitmap();

		// Prepare Action Bar
		actionBar = getSupportActionBar();
		actionBar.hide();

		if (map == null) {
			map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.MMap)).getMap();
			if (map != null) {
				map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				map.setIndoorEnabled(false);
				map.setBuildingsEnabled(false);
				map.getUiSettings().setZoomControlsEnabled(false);
				map.setOnMapLoadedCallback(new OnMapLoadedCallback() {

					@Override
					public void onMapLoaded() {
						isFirstMapUpdate = true;

						map.setOnMapLongClickListener(new OnMapLongClickListener() {

							@Override
							public void onMapLongClick(LatLng point) {
								// FIXME How destination is set
								if (!isNavigation && isBuildingReady && isNavigationReady) {
									destination = new RoutePoint(point.longitude, point.latitude, currentFloor, building.getId());
									taskStartNav = new StartNav(destination, getResources().getString(R.string.AMCustomDest));
									taskStartNav.execute();
								}
							}
						});
					}
				});
			}
		}

		// Prepare Drawer        
		drawerToggle = new ActionBarDrawerToggle(
				this,                 /* host Activity */
				drawerLayout,        /* DrawerLayout object */
				R.string.AMDestination,  /* "open drawer" description */
				R.string.AMTitle  /* "close drawer" description */
		) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				map.getUiSettings().setScrollGesturesEnabled(true);
				actionBar.setTitle(R.string.AMTitle);
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				map.getUiSettings().setScrollGesturesEnabled(false);
				populateDrawer();
				actionBar.setTitle(R.string.AMDestination);
			}

		};
		drawerLayout.setDrawerListener(drawerToggle);
		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
		drawerList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
									long arg3) {
				toggleDrawer(); // Close drawer
				if (!isNavigation) {
					CustomItems ci = drawerAreas.get(position-1);
					if (ci instanceof CustomSingleItem) {
						destination = ((CustomSingleItem) ci).point;
						taskStartNav = new StartNav((CustomSingleItem) ci);
						taskStartNav.execute();
					}
				} else {
					// Stop navigation
					stopNavigation();
				}
			}
		});
		drawerToggle.syncState();

		myLocLayout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!isNavigation) {
					if (isCameraUpdated) {
						myLocImage.setImageResource(R.drawable.ic_my_location_grey600_48dp);
						isCameraUpdated = false;
					} else {
						Drawable d = getResources().getDrawable(R.drawable.ic_my_location_white_48dp);
						d.setColorFilter(getResources().getColor(R.color.accentColor), Mode.MULTIPLY);
						myLocImage.setImageDrawable(d);
						isCameraUpdated = true;
					}
				}
			}
		});

		fbb.getView().setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				getSupportFragmentManager().beginTransaction().replace(R.id.MMap, new FragmentInstructions(gs.getRoute().instructions)).addToBackStack("map").commit();
				actionBar.show();
				actionBar.setTitle("Instructions");
				actionBar.setDisplayHomeAsUpEnabled(true);
				drawerToggle.setDrawerIndicatorEnabled(false);
				drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
				hideUpperBar();
				hideBottomBar();
			}
		});

		String json = gs.getSharedPrefs().getString(KEY_STORED_POSITION, "");
		if (json.equals("")) {
			storeItem = new CustomSingleItem(getString(R.string.FDStore), storeItemListener);
			restoreItem = null;
			deleteItem = null;
		} else {
			try {
				storedPos = JSON.mapper.readValue(json, RoutePoint.class);
				storedMarker = map.addMarker(new MarkerOptions().position(storedPos.getLatLng())
						.icon(BitmapDescriptorFactory.fromBitmap(storedBitmap))
						.anchor(0.5f, 0.5f).flat(true));
				storedMarker.setVisible(false);
				storeItem = new CustomSingleItem(getString(R.string.FDUpdate), storeItemListener);
				restoreItem = new CustomSingleItem(getString(R.string.FDRestore), restoreItemListener);
				deleteItem = new CustomSingleItem(getString(R.string.FDDelete), deleteItemListener);
			} catch (Exception e) {
				restoreItem = null;
				deleteItem = null;
			}
		}
		populateDrawer();

		// Attach listener
		gs.addLocationCallback(this);
	}

	@Override
	protected void onStart() {
		super.onStart();

		// Register connectivity change receiver
		registerReceiver(changeConnection, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}

	@Override
	protected void onStop() {
		super.onStop();

		// Detach connectivity change receiver
		unregisterReceiver(changeConnection);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// Detach listener
		gs.removeLocationCallback(this);

		if (tileOverlay != null) {
			tileOverlay.remove();
		}

		if (!avoidDestroy) {
			if (gs.getLinks() != null) {
				gs.getLinks().disconnect();
			}
			if (gs.getIndoorLib() != null) {
				gs.getIndoorLib().stopLocate();
			}

			NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancelAll();
		}
	}

	@Override
	public boolean onKeyDown(int keycode, KeyEvent e) {
		if (drawerLayout != null) {
			switch (keycode) {
				case KeyEvent.KEYCODE_MENU:
					if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
						toggleDrawer();
						return true;
					} else {
						break;
					}
			}
		}
		return super.onKeyDown(keycode, e);
	}

	@Override
	public void onBackPressed() {
		FragmentManager fm = getSupportFragmentManager();
		if (drawerLayout != null && drawerLayout.isDrawerOpen(Gravity.START)) {
			drawerLayout.closeDrawer(Gravity.START);
		} else if (fm.getBackStackEntryCount() != 0) {
			fm.popBackStackImmediate();
			if (fm.getBackStackEntryCount() == 0) {
				actionBar.hide();
				actionBar.setTitle(R.string.AMTitle);
				actionBar.setDisplayHomeAsUpEnabled(true);
				drawerToggle.setDrawerIndicatorEnabled(true);
				drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
				drawerToggle.syncState();
				showBottomBar();
				showUpperBar();
			}
		} else {
			int resource = isNavigation ? R.string.AMDNav : R.string.AMDMap;
			alert = new AlertDialog.Builder(this)
					.setTitle(R.string.AMDTitle)
					.setMessage(resource)
					.setPositiveButton(R.string.exit, new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					})
					.setNegativeButton(R.string.cancel, new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							alert.dismiss();
						}
					})
					.show();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
					onBackPressed();
				} else if (drawerLayout != null) {
					toggleDrawer();
					return true;
				}
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
									ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle(R.string.options);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.map_navigation, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.MMNStop:
				stopNavigation();
				return true;
			case R.id.MMNCancel:
				dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
				return true;
			default:
				return super.onContextItemSelected(item);
		}

	}

	@Override
	public void onLocationUpdate(IndoorLocation loc) {
		synchronized (mutexPosition) {
			currentPosition = loc;
		}

		// Change floor
		if (loc.type == IndoorLocation.TYPE_IBEACON && isMapUpdated && building != null) {
			if (currentFloor != loc.floornumber || tileOverlay == null) {
				changeFloor(loc.floornumber);
			}
			// Stored position
			if (storedMarker != null) {
				if (loc.floornumber == storedPos.floornumber) {
					storedMarker.setVisible(true);
				} else {
					storedMarker.setVisible(false);
				}
			}
		}


		// Get building
		if (loc.type == IndoorLocation.TYPE_IBEACON
				&& (building == null || (building != null && !building.getId().equals(loc.buildingId) && isBuildingReady))) {
			ArrayList<String> ids = new ArrayList<String>();
			ids.add(loc.buildingId);
			gs.getLinks().getBuildings(ids, callbackBuildings);
			isBuildingReady = false;
		}

		// Route being created
		if (taskStartNav != null && taskStartNav.getStatus() != AsyncTask.Status.FINISHED) {
			return;
			// Route created, nav mode set
		} else if (taskStartNav != null && taskStartNav.getStatus() == AsyncTask.Status.FINISHED) {
			progressDialog.dismiss();
			progressDialog = null;
			taskStartNav = null;
		}

		// Instructions mode
		if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
			return;
		}

		LatLng point = new LatLng(loc.latitude, loc.longitude);
		if (isFirstMapUpdate) {
			isFirstMapUpdate = false;
			animateCamera(point, MAP_TIME_FIRSTUPDATE);
		}

		// Handle FUB visibility
		if (loc.type == IndoorLocation.TYPE_IBEACON) {
			showUpperBar(loc.floornumber);
		} else {
			hideUpperBar();
		}

		// Handle Navigation
		if (isNavigation) {
			// Handle FBB visibility
			if (!isFbbShown) {
				showBottomBar();
				myLocLayout.setVisibility(View.GONE);
			}

			// Get projected point
			RoutingResult rr = gs.getRoute().getProjection(loc);
			RouteProjectedPoint projPoint = rr.projectedPoint;

			if (!rr.isRecomputeRequired) {
				// Get next instruction to show
				instruction = gs.getRoute().getRouteInstruction(loc);

				// Update FBB
				updateBottomBar(projPoint);

				// Check end route
				if (instruction.instruction == InstructionType.ARRIVAL
						&& instruction.distance - ROUTE_ARRIVAL_THRESHOLD <= projPoint.distanceFromStart) {
					stopNavigation();
					drawUserPosition(point, loc.accuracy);
				} else {
					// Update map position
					drawRoutePosition(projPoint, loc.accuracy);

					// Update route
					if (loc.type == IndoorLocation.TYPE_IBEACON
							&& (lastFloornumber != loc.floornumber) || mapRoute == null) {
						drawRoute(loc.floornumber);
						lastFloornumber = loc.floornumber;
					}
				}
			} else {
				taskStartNav = new StartNav(destination, fubLocal.getText().toString());
				taskStartNav.execute();
				for (Polyline pl : mapRoute) {
					pl.remove();
				}
				mapRoute = null;
			}
		} else {
			// Update map position
			drawUserPosition(point, loc.accuracy);
		}
	}

	@Override
	public void onNotification(NotificationResult nr) {
		String msg = nr.notification.properties.get(NOTIFICATION_KEY_MESSAGE);
		if (msg == null) {
			switch (nr.notification.action) {
				case ENTER:
					msg = gs.getString(R.string.AMNEnter) + " " + nr.place.getName();
					break;
				case STAY:
					msg = gs.getString(R.string.AMNStay) + " " + nr.place.getName();
					break;
				case LEAVE:
					msg = gs.getString(R.string.AMNLeave) + " " + nr.place.getName();
					break;
				case NEARBY:
					msg = gs.getString(R.string.AMNNearby) + " " + nr.place.getName();
					break;
				default:
					return;
			}
		}

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(gs.getString(R.string.AMNTitle))
				.setContentText(msg)
				.setAutoCancel(true)
				.setTicker(msg)
				.setLights(gs.getResources().getColor(R.color.primaryColorDark), 3000, 6000);

		Intent intent = new Intent(gs.getBaseContext(), ActivityMap.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pi = PendingIntent.getActivity(context, 0, intent, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		builder.setContentIntent(pi);

		android.app.Notification noti = builder.build();
		noti.defaults |= android.app.Notification.DEFAULT_SOUND;
		noti.defaults |= android.app.Notification.DEFAULT_VIBRATE;

		notificationManager.notify(notCounter, noti);
		notCounter++;
	}

	void setAvoidDestroy(boolean value) {
		avoidDestroy = value;
	}

	private void toggleDrawer() {
		if (drawerLayout.isDrawerOpen(Gravity.START)) {
			drawerLayout.closeDrawer(Gravity.START);
		} else {
			drawerLayout.openDrawer(Gravity.START);
		}
	}

	private void populateDrawer() {
		EntryAdapter adapter;
		if (isNavigation) {
			ArrayList<CustomItems> di = new ArrayList<CustomItems>();
			di.add(new CustomSection(getString(R.string.options)));
			di.add(new CustomSingleItemImage(getString(R.string.FDStop), R.drawable.ic_stop));
			di.addAll(getBottomDrawer());
			adapter = new EntryAdapter(this, di);
		} else {
			if (drawerAreas != null && drawerAreas.size() != 0) {
				ArrayList<CustomItems> di = new ArrayList<>();
				di.add(new CustomSection(getString(R.string.FDAreas)));
				di.addAll(drawerAreas);
				di.addAll(getBottomDrawer());
				adapter = new EntryAdapter(this, di);
			} else {
				ArrayList<CustomItems> di = new ArrayList<CustomItems>();
				di.add(new CustomSection(getString(R.string.FDAreas)));
				di.add(new CustomSingleItem(getString(R.string.FDNoAreas), false));
				di.addAll(getBottomDrawer());
				adapter = new EntryAdapter(this, di);
			}
		}
		drawerList.setAdapter(adapter);
	}

	private ArrayList<CustomItems> getBottomDrawer() {
		ArrayList<CustomItems> items = new ArrayList<>();
		if (!isNavigation) {
			items.add(new CustomListView.CustomDivider());
			items.add(new CustomSection(getString(R.string.FDStoreTitle)));
			if (storeItem != null) {
				items.add(storeItem);
			}
			if (restoreItem != null) {
				items.add(restoreItem);
			}
			if (deleteItem != null) {
				items.add(deleteItem);
			}
		}
		items.add(new CustomListView.CustomDivider());
		items.add(new CustomSingleItem(getString(R.string.logout), new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onLogout();
			}
		}));
		return items;
	}

	private void updateBottomBar(RouteProjectedPoint rpp) {
		int dist = (int) (instruction.distance - rpp.distanceFromStart);
		if (dist != 0) {
			fbbDistance.setText(getResources().getQuantityString(R.plurals.routing_in_meters,
					dist, dist));
		} else {
			fbbDistance.setText(getString(R.string.routing_now));
		}
		fbbInstruction.setText(InstructionAdapter.getString(this, instruction));
		fbbImage.setImageResource(InstructionAdapter.getImageResource(instruction));
	}

	private void showUpperBar() {
		if (building == null) {
			hideUpperBar();
		} else {
			fub.getView().setVisibility(View.VISIBLE);
			((RelativeLayout) fub.getView().findViewById(R.id.FUBLevelBox)).setVisibility(View.GONE);
		}
	}

	private void showUpperBar(int floor) {
//		if (floor == 2) { // FIXME
//			fubImage.setImageResource(R.drawable.ic_floor_7_alpha);			
//		} else {
//			fubImage.setImageResource(0);
//		}
		if (building == null) {
			hideUpperBar();
		} else {
			((RelativeLayout) fub.getView().findViewById(R.id.FUBLevelBox)).setVisibility(View.VISIBLE);
			fubNumber.setText(Integer.toString(floor));
			fub.getView().setVisibility(View.VISIBLE);
		}
	}

	private void hideUpperBar() {
		fub.getView().setVisibility(View.GONE);
	}

	private void showBottomBar() {
		fbb.getView().setVisibility(View.VISIBLE);
		isFbbShown = true;
	}

	private void hideBottomBar() {
		fbb.getView().setVisibility(View.GONE);
		isFbbShown = false;
	}

	/**
	 * This task starts the navigation mode.
	 */
	private class StartNav extends AsyncTask<Void, Void, Boolean> implements IndoorLocationListener {

		private static final String TAG = "StartNav";

		private RoutePoint point;
		private String title;
		private Object mutex = new Object();

		public StartNav(CustomSingleItem csi) {
			point = csi.point;
			title = csi.getTitle();
		}

		public StartNav(RoutePoint point, String title) {
			this.point = point;
			this.title = title;
		}

		@Override
		protected void onPreExecute() {
			CameraPosition camera = CameraPosition.builder(map.getCameraPosition())
					.tilt(MAP_TILT_NAVIGATION)
					.build();
			map.moveCamera(CameraUpdateFactory.newCameraPosition(camera));

			// Set progress dialog
			progressDialog = new ProgressDialog(context);
			progressDialog.setMessage(getString(R.string.AMGettingRoute));
			progressDialog.setIndeterminate(true);
			progressDialog.setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {
					taskStartNav.cancel(true);
				}
			});
			progressDialog.show();
			fubDestination.setText(R.string.FUBDestination);
			fubLocal.setText(title);

		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				synchronized (mutexPosition) {
					if (currentPosition.type != IndoorLocation.TYPE_IBEACON) {
						synchronized (mutex) {
							gs.addLocationCallback(this);
							mutex.wait();
						}
					}
					RoutePoint rp = new RoutePoint(currentPosition.longitude,
							currentPosition.latitude, currentPosition.floornumber,
							currentPosition.buildingId);
					Route r = gs.getRouting().computeRoute(rp, point);
					if (r != null) {
						gs.setRoute(r);
						return true;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean succeed) {
			if (succeed) {
				// Handle FUB visibility
				showUpperBar();
				actionBar.hide();

				// FBB visibility handled later, since depends on current inst				

				isNavigation = true;
				populateDrawer(); // After change flag
			} else {
				Log.e(TAG, "Not successful");
				stopNavigation();
				Toast.makeText(context, getString(R.string.AMTNoRoute, title), Toast.LENGTH_LONG).show();
			}
		}

		@Override
		public void onLocationUpdate(IndoorLocation location) {
			if (location.type == IndoorLocation.TYPE_IBEACON) {
				gs.removeLocationCallback(this);
				synchronized (mutexPosition) {
					currentPosition = location;
				}
				synchronized (mutex) {
					mutex.notify();
				}
			}
		}

		@Override
		public void onNotification(NotificationResult nr) {
		}

	}

	;

	private void stopNavigation() {
		isNavigation = false;
		CameraPosition camera = CameraPosition.builder(map.getCameraPosition())
				.tilt(0)
				.bearing(0)
				.build();
		map.moveCamera(CameraUpdateFactory.newCameraPosition(camera));

		// Remove polyline
		if (mapRoute != null) {
			for (Polyline pl : mapRoute) {
				pl.remove();
			}
			mapRoute = null;
		}

		fubDestination.setText(R.string.FUBYouAre);
		fubLocal.setText(building.getName());
		// Handle FBB visibility
		hideBottomBar();
		myLocLayout.setVisibility(View.VISIBLE);

		destination = null;
		populateDrawer();
	}

	private void drawAnimatedMarker(final LatLng point, final double accuracy) {
		if (markerPos != null) {
			final long startTime = SystemClock.elapsedRealtime();
			final long duration = MAP_TIME_MARKER;
			final LatLng start = markerPos.getPosition();
			final Double startAcc = (circlePos == null) ? null : circlePos.getRadius();
			float[] result = new float[3];
			Location.distanceBetween(start.latitude, start.longitude, point.latitude, point.longitude, result);
			final boolean isMoving = (result[0] < 0.5) ? false : true;
			final Interpolator itp = new AccelerateDecelerateInterpolator();
			handler.post(new Runnable() {

				@Override
				public void run() {
					long elapsed = SystemClock.elapsedRealtime() - startTime;
					float t = itp.getInterpolation((float) elapsed / duration);
					double acc = (startAcc != null) ? t * accuracy + (1 - t) * startAcc : 0;
					if (isMoving) {
						double lng = t * point.longitude + (1 - t) * start.longitude;
						double lat = t * point.latitude + (1 - t) * start.latitude;
						markerPos.setPosition(new LatLng(lat, lng));
						handleCircle(new LatLng(lat, lng), acc);
					} else {
						markerPos.setPosition(point);
						handleCircle(point, acc);
					}
					if (elapsed > MAP_TIME_MARKER) {
						isMapUpdated = true;
					} else {
						handler.postDelayed(this, 20);
					}
				}
			});

		} else {
			markerPos = map.addMarker(new MarkerOptions().position(point)
					.icon(BitmapDescriptorFactory.fromBitmap(markerBitmap))
					.anchor(0.5f, 0.5f).flat(true));
			setCircle(point, accuracy);
			isMapUpdated = true;
		}
	}

	private void setCircle(LatLng point, double accuracy) {
		circlePos = map.addCircle(new CircleOptions().center(point)
				.radius(accuracy).zIndex(MAP_ZINDEX_CIRCLE)
				.fillColor(Color.TRANSPARENT)
				.strokeColor(getResources().getColor(R.color.accentColorLight))
				.strokeWidth(MAP_CIRCLE_STROKE));
	}

	private void handleCircle(LatLng point, double accuracy) {
		if (!isNavigation) {
			if (circlePos == null) {
				setCircle(point, accuracy);
			} else {
				circlePos.setCenter(point);
				circlePos.setRadius(accuracy);
			}
		} else if (circlePos != null) {
			circlePos.remove();
			circlePos = null;
		}
	}

	private void drawUserPosition(LatLng point, double accuracy) {
		if (isMapUpdated && markerBitmap != null) {
			isMapUpdated = false;
			// Draw user point and circle
			drawAnimatedMarker(point, accuracy);
			// Update camera
			if (isCameraUpdated) {
				animateCamera(point, MAP_TIME_MARKER, map.getCameraPosition().zoom);
			}
		}
	}

	private void drawRoutePosition(RouteProjectedPoint projPoint, double accuracy) {
		if (isMapUpdated && markerBitmap != null) {
//			isMapUpdated = false;
			// Draw projected point
			drawAnimatedMarker(projPoint.getLatLng(), accuracy);
			// Update camera
			CameraPosition cam = new CameraPosition.Builder(map.getCameraPosition())
					.bearing((float) projPoint.bearing)
					.target(projPoint.getLatLng())
					.build();
			animateCamera(cam, MAP_TIME_NAVIGATION);
		} else {
			Log.e("JC", "Not called, isMapUpdated: " + isMapUpdated);
		}
	}

	private void drawRoute(int floornumber) {
		ArrayList<RoutePoint> points = gs.getRoute().route;
		if (mapRoute != null) {
			for (Polyline pl : mapRoute) {
				pl.remove();
			}
		}
		mapRoute = new ArrayList<Polyline>();
		PolylineOptions lineOpt = new PolylineOptions();
		for (RoutePoint rp : points) {
			if (rp.floornumber == floornumber) {
				lineOpt.add(rp.getLatLng());
			} else if (lineOpt.getPoints().size() != 0) {
				lineOpt.color(getResources().getColor(R.color.accentColorDark))
						.width(MAP_ROUTE_WIDTH).zIndex(MAP_ZINDEX_ROUTE);
				mapRoute.add(map.addPolyline(lineOpt));
				lineOpt = new PolylineOptions();
			}
		}
		if (lineOpt.getPoints().size() != 0) {
			lineOpt.color(getResources().getColor(R.color.accentColorDark))
					.width(MAP_ROUTE_WIDTH).zIndex(MAP_ZINDEX_ROUTE);
			mapRoute.add(map.addPolyline(lineOpt));
		}
	}

	private void animateCamera(CameraPosition camera, int time) {
		map.animateCamera(CameraUpdateFactory.newCameraPosition(camera),
				time, callbackMapCancelable);
		handler.postDelayed(runnableEnableMap, (long) (1.2 * time));
	}

	/**
	 * This method animate the camera to the defined point with default zoom.
	 *
	 * @param point Target point
	 * @param time  Animation time
	 */
	private void animateCamera(LatLng point, int time) {
		animateCamera(point, time, MAP_ZOOM_DEFAULT);
	}

	/**
	 * This method animate the camera to the defined point with default zoom.
	 *
	 * @param point Target point
	 * @param time  Animation time
	 * @param zoom  Zoom level
	 */
	private void animateCamera(LatLng point, int time, float zoom) {
		CameraPosition camera = new CameraPosition.Builder()
				.target(point)
				.zoom(zoom)
				.build();
		animateCamera(camera, time);
	}

	private void changeFloor(int floornumber) {
		if (tileOverlay != null) {
			tileOverlay.remove();
		}
		int position = building.getFloors().indexOfKey(floornumber);
		tileOverlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(floors.get(position).getTileProvider()).zIndex(MAP_ZINDEX_TILES));
		currentFloor = floors.get(position).getFloor();
	}

	private void setMarkerBitmap() {
		int px = getResources().getDimensionPixelSize(R.dimen.marker_size);
		markerBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(markerBitmap);
		Drawable shape = getResources().getDrawable(R.drawable.marker_position);
		shape.setBounds(0, 0, markerBitmap.getWidth(), markerBitmap.getHeight());
		shape.draw(canvas);

		storedBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
		Canvas canvas2 = new Canvas(storedBitmap);
		Drawable shape2 = getResources().getDrawable(R.drawable.marker_stored);
		shape2.setBounds(0, 0, storedBitmap.getWidth(), storedBitmap.getHeight());
		shape2.draw(canvas2);
	}

	private void onLogout() {
		SharedPreferences.Editor sharedPrefsEditor = gs.getSharedPrefs().edit();
		sharedPrefsEditor.putBoolean(FragmentSplashscreen.KEY_PREF_AUTOLOGIN, false);
		sharedPrefsEditor.commit();
		finish();
		Intent i = new Intent(context, ActivityStart.class);
		startActivity(i);
	}

	private View.OnClickListener storeItemListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			synchronized (mutexPosition) {
				if (currentPosition != null && currentPosition.type == IndoorLocation.TYPE_IBEACON) {
					storedPos = new RoutePoint(currentPosition.longitude,
							currentPosition.latitude,
							currentPosition.floornumber,
							currentPosition.buildingId);
					SharedPreferences.Editor sharedPrefsEditor = gs.getSharedPrefs().edit();
					sharedPrefsEditor.putString(KEY_STORED_POSITION, storedPos.toLog());
					sharedPrefsEditor.commit();

					if (storedMarker == null) {
						storedMarker = map.addMarker(new MarkerOptions().position(storedPos.getLatLng())
								.icon(BitmapDescriptorFactory.fromBitmap(storedBitmap))
								.anchor(0.5f, 0.5f).flat(true));
						storeItem = new CustomSingleItem(getString(R.string.FDUpdate), this);
						Toast.makeText(context, getString(R.string.FDStoreToast), Toast.LENGTH_LONG).show();
					} else {
						storedMarker.setPosition(storedPos.getLatLng());
						Toast.makeText(context, getString(R.string.FDUpdateToast), Toast.LENGTH_LONG).show();
					}
					restoreItem = new CustomSingleItem(getString(R.string.FDRestore), restoreItemListener);
					deleteItem = new CustomSingleItem(getString(R.string.FDDelete), deleteItemListener);
					toggleDrawer();
					populateDrawer();
				} else {
					Toast.makeText(context, R.string.AMTMarkerNoIndoor, Toast.LENGTH_LONG).show();
				}
			}
		}
	};

	private View.OnClickListener restoreItemListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (!isNavigation) {
				taskStartNav = new StartNav(storedPos, getResources().getString(R.string.FUBSaved));
				taskStartNav.execute();
				toggleDrawer();
				populateDrawer();
			}
		}
	};

	private View.OnClickListener deleteItemListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			SharedPreferences.Editor sharedPrefsEditor = gs.getSharedPrefs().edit();
			sharedPrefsEditor.remove(KEY_STORED_POSITION);
			sharedPrefsEditor.commit();
			storedMarker.remove();
			storedMarker = null;
			storedPos = null;
			storeItem = new CustomSingleItem(getString(R.string.FDStore), storeItemListener);
			restoreItem = null;
			deleteItem = null;
			toggleDrawer();
			populateDrawer();
			Toast.makeText(context, getString(R.string.FDDeleteToast), Toast.LENGTH_LONG).show();
		}
	};

}
