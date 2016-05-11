package com.oym.indoor.navigation;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
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
import com.oym.indoor.GoIndoor;
import com.oym.indoor.Instruction;
import com.oym.indoor.Instruction.InstructionType;
import com.oym.indoor.InstructionAdapter;
import com.oym.indoor.LocationResult;
import com.oym.indoor.NotificationResult;
import com.oym.indoor.Place;
import com.oym.indoor.Route;
import com.oym.indoor.RoutePoint;
import com.oym.indoor.RouteProjectedPoint;
import com.oym.indoor.RoutingResult;
import com.oym.indoor.navigation.views.CustomListView;
import com.oym.indoor.navigation.views.CustomListView.CustomItems;
import com.oym.indoor.navigation.views.CustomListView.CustomSection;
import com.oym.indoor.navigation.views.CustomListView.CustomSingleItem;
import com.oym.indoor.navigation.views.CustomListView.CustomSingleItemImage;
import com.oym.indoor.navigation.views.CustomListView.EntryAdapter;

import org.codehaus.jackson.map.ObjectMapper;

import java.util.ArrayList;

public class ActivityMap extends AppCompatActivity implements IndoorLocationListener {

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
	private FloatingActionButton fab;
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
	private boolean isNavigationReady = true;

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
	private CustomSingleItem itemUserProfile;

	// Navigation
	private ArrayList<CustomItems> drawerAreas;
	private Instruction instruction;
	private LocationResult currentPosition;
	private final Object mutexPosition = new Object();
	private StartNav taskStartNav;
	private RoutePoint destination;

	private GlobalState gs;
	private Context context;
	private Handler handler;
	private Building building;
	private int currentFloor;
	private ObjectMapper mapper = new ObjectMapper();

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

	private BroadcastReceiver btListener = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
				if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_TURNING_OFF) {
					Toast.makeText(context, R.string.AMTNoBt, Toast.LENGTH_LONG).show();

				//} else if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON) {
				//	startLeUnknownScan();
				}
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
		fab = (FloatingActionButton) findViewById(R.id.MFabMyLocation);

		// Check if there are buildings
		if (gs.getGoIndoor().getBuildings().isEmpty()) {
			Toast.makeText(context, R.string.AMTNoBuildings, Toast.LENGTH_LONG).show();
		}

		// Prepare view
		populateDrawer();
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
						showViewAnimated(fab);

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
		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		drawerList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
									long arg3) {
				toggleDrawer(); // Close drawer
				if (!isNavigation) {
					CustomItems ci = drawerAreas.get(position - 1);
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

		itemUserProfile = new CustomSingleItem(getString(R.string.FDUserProfile), new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (gs.getGoIndoor().getLogger() != null && gs.getGoIndoor().getSettings() != null) {
					getSupportFragmentManager().beginTransaction().replace(R.id.MMap, new FragmentUserProfile(), "UserProfile").addToBackStack("map").commit();
					actionBar.show();
					actionBar.setTitle(getString(R.string.FDUserProfile));
					actionBar.setDisplayHomeAsUpEnabled(true);
					drawerToggle.setDrawerIndicatorEnabled(false);
					drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
					hideUpperBar();
					hideBottomBar();
					if (!isNavigation) {
						hideViewAnimated(fab);
					}
				} else {
					Toast.makeText(context, getString(R.string.FUPNoSettings), Toast.LENGTH_LONG).show();
				}
			}
		});

		fab.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// UserProfile mode
				if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
					Fragment f = getSupportFragmentManager().findFragmentByTag("UserProfile");
					if (f != null) {
						((FragmentUserProfile)f).fabClick();
					}
					return;
				} else
				if (!isNavigation) {
					if (isCameraUpdated) {
						fab.setImageResource(R.drawable.ic_my_location_grey600_48dp);
						isCameraUpdated = false;
					} else {
						Drawable d = getResources().getDrawable(R.drawable.ic_my_location_white_48dp);
						d.setColorFilter(getResources().getColor(R.color.accentColor), Mode.MULTIPLY);
						fab.setImageDrawable(d);
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
				storedPos = mapper.readValue(json, RoutePoint.class);
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

		// Read Options Preferences
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				String locAlgoStr = gs.getSharedPrefs().getString(getString(R.string.FPOLocationAlgKey), String.valueOf(GoIndoor.LOCATION_TYPE_PROJECT));
				int locAlgo = Integer.parseInt(locAlgoStr);
				//noinspection ResourceType
				gs.getGoIndoor().setLocationType(locAlgo);
				if (gs.getSharedPrefs().getBoolean(getString(R.string.FPOForceScanKey), false)) {
					gs.getGoIndoor().setScanForced(true);
				}
			}
		}, 600);

		// Attach listener
		gs.addLocationCallback(this);
	}

	@Override
	protected void onStart() {
		super.onStart();

		// Register connectivity change receiver
		registerReceiver(changeConnection, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		registerReceiver(btListener, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
	}

	@Override
	protected void onStop() {
		super.onStop();

		// Detach connectivity change receiver
		unregisterReceiver(changeConnection);
		unregisterReceiver(btListener);
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
			if (gs.getGoIndoor() != null) {
				gs.getGoIndoor().disconnect();
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
		if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
			drawerLayout.closeDrawer(GravityCompat.START);
		} else if (fm.getBackStackEntryCount() != 0) {
			fm.popBackStackImmediate();
			if (fm.getBackStackEntryCount() == 0) {
				actionBar.hide();
				actionBar.setTitle(R.string.AMTitle);
				actionBar.setDisplayHomeAsUpEnabled(true);
				drawerToggle.setDrawerIndicatorEnabled(true);
				drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
				drawerToggle.syncState();
				if (isNavigation) {
					showBottomBar();
				} else {
					showViewAnimated(fab);
				}
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
	public void onLocationUpdate(LocationResult loc) {
		synchronized (mutexPosition) {
			currentPosition = loc;
		}

		// Change floor
		if (loc.type == LocationResult.TYPE_BEACON && isMapUpdated && building != null) {
			if (currentFloor != loc.floorNumber || tileOverlay == null) {
				changeFloor(loc.floorNumber);
			}
			// Stored position
			if (storedMarker != null) {
				if (loc.floorNumber == storedPos.floorNumber) {
					storedMarker.setVisible(true);
				} else {
					storedMarker.setVisible(false);
				}
			}
		}


		// FIXME Log Position -> Amb menys frequencia?
		if (logCount == 5) {
			gs.getGoIndoor().getLogger().logPosition(loc);
			logCount = 0;
		} else {
			logCount++;
		}

		// Get building
		if (loc.type == LocationResult.TYPE_BEACON
				&& (building == null || (building != null && !building.getId().equals(loc.building) && isBuildingReady))) {
			building = gs.getGoIndoor().getBuildings(loc.building);
			if (building != null) {
				isBuildingReady = true;
				fubLocal.setText(building.getName());

				// Check areas
				drawerAreas = new ArrayList<>();
				for (Place place : gs.getGoIndoor().getPlaces(building.getId())) {
					// FIXME
					CustomSingleItem csi = new CustomSingleItem(place.getName(),
							new RoutePoint(place.getX(), place.getY(), place.getFloorNumber(), place.getBuilding()));
					drawerAreas.add(csi);
				}
				populateDrawer();
			}
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
		if (loc.type == LocationResult.TYPE_BEACON) {
			showUpperBar(loc.floorNumber);
		} else {
			hideUpperBar();
		}

		// Handle Navigation
		if (isNavigation) {
			// Handle FBB visibility
			if (!isFbbShown) {
				showBottomBar();
				hideViewAnimated(fab);
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
					if (loc.type == LocationResult.TYPE_BEACON
							&& (lastFloornumber != loc.floorNumber) || mapRoute == null) {
						drawRoute(loc.floorNumber);
						lastFloornumber = loc.floorNumber;
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
		if (msg == null || msg.isEmpty()) {
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
		if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
			drawerLayout.closeDrawer(GravityCompat.START);
		} else {
			drawerLayout.openDrawer(GravityCompat.START);
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
		items.add(new CustomSection(getString(R.string.settings)));
		items.add(itemUserProfile);
		items.add(new CustomSingleItem(getString(R.string.FDOptions), new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getSupportFragmentManager().beginTransaction().replace(R.id.MMap, new FragmentOptions(), "Options").addToBackStack("map").commit();
				actionBar.show();
				actionBar.setTitle(getString(R.string.FDOptions));
				actionBar.setDisplayHomeAsUpEnabled(true);
				drawerToggle.setDrawerIndicatorEnabled(false);
				drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
				hideUpperBar();
				hideBottomBar();
				if (!isNavigation) {
					hideViewAnimated(fab);
				}
			}
		}));
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

	private void showViewAnimated(View view) {
		ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0, 1);
		ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0, 1);
		AnimatorSet animSetXY = new AnimatorSet();
		animSetXY.playTogether(scaleX, scaleY);
		animSetXY.setInterpolator(new FastOutLinearInInterpolator());
		animSetXY.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
		animSetXY.start();
		view.setVisibility(View.VISIBLE);
	}

	private void hideViewAnimated(View view) {
		ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1, 0);
		ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1, 0);
		AnimatorSet animSetXY = new AnimatorSet();
		animSetXY.playTogether(scaleX, scaleY);
		animSetXY.setInterpolator(new FastOutLinearInInterpolator());
		animSetXY.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
		animSetXY.start();
	}

	/**
	 * This task starts the navigation mode.
	 */
	private class StartNav extends AsyncTask<Void, Void, Boolean> implements IndoorLocationListener {

		private static final String TAG = "StartNav";

		private RoutePoint point;
		private String title;
		private final Object mutex = new Object();

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
					if (currentPosition.type != LocationResult.TYPE_BEACON) {
						synchronized (mutex) {
							gs.addLocationCallback(this);
							mutex.wait();
						}
					}
					RoutePoint rp = new RoutePoint(currentPosition.longitude,
							currentPosition.latitude, currentPosition.floorNumber,
							currentPosition.building);
					Route r = gs.getGoIndoor().computeRoute(rp, point);
					if (r != null && !r.getProjection(currentPosition).isRecomputeRequired) {
						gs.setRoute(r);
						gs.getGoIndoor().getLogger().logRoute(r);
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
		public void onLocationUpdate(LocationResult location) {
			if (location.type == LocationResult.TYPE_BEACON) {
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
		showViewAnimated(fab);

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
			if (rp.floorNumber == floornumber) {
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
		currentFloor = building.getFloors().get(floornumber).getFloorNumber();
		tileOverlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(building.getFloors().get(floornumber).getTileProvider()).zIndex(MAP_ZINDEX_TILES));
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
				if (currentPosition != null && currentPosition.type == LocationResult.TYPE_BEACON) {
					storedPos = new RoutePoint(currentPosition.longitude,
							currentPosition.latitude,
							currentPosition.floorNumber,
							currentPosition.building);
					SharedPreferences.Editor sharedPrefsEditor = gs.getSharedPrefs().edit();
					sharedPrefsEditor.putString(KEY_STORED_POSITION, storedPos.toJson());
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
