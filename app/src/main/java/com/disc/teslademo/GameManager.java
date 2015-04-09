package com.disc.teslademo;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GameManager extends FragmentActivity
         implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
         LocationListener, OnMapReadyCallback {

     // Message types sent from the BluetoothReadService Handler
     public static final int MESSAGE_STATE_CHANGE = 1;
     public static final int MESSAGE_READ = 2;
     public static final int MESSAGE_WRITE = 3;
     public static final int MESSAGE_DEVICE_NAME = 4;
     public static final int MESSAGE_TOAST = 5;
     // Key names received from the BluetoothChatService Handler
     public static final String DEVICE_NAME = "device_name";
     public static final String TOAST = "toast";
     protected static final String TAG = "GameManager", tPad = "Tee-Pad", basket = "Basket";
     // Intent request codes
     private static final int REQUEST_CONNECT_DEVICE = 1;
     private static final int REQUEST_ENABLE_BT = 2;
     private static BluetoothSerialService mSerialService = null;
     // Gameplay Variables
     public int currentBasket;
     // Map Variables
     protected GoogleApiClient mGoogleApiClient;
     ArrayList<LatLng> points = new ArrayList<>();
     TrajectoryPlotter trajectory = new TrajectoryPlotter();
     LocationManager manager;
    List<Double> plotPoints = new ArrayList<>();
     MapperPlayedGame currentGame;
     MapperCourse course;
    double lastGPS = 0.0;
    String receivedMessage;
    boolean messageInProgress = false;
    int numberOfMessageTries = 0;
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // The Handler that gets information back from the BluetoothService
    private final Handler mHandlerBT = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothSerialService.STATE_CONNECTED:
                            if (mMenuItemConnect != null) {
                                mMenuItemConnect.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
                                mMenuItemConnect.setTitle(R.string.disconnect);
                            }

                             pairedDeviceText.setText(R.string.title_connected_to);
                             pairedDeviceText.append(" " + mConnectedDeviceName);
                             break;

                         case BluetoothSerialService.STATE_CONNECTING:
                             pairedDeviceText.setText(R.string.title_connecting);
                             break;

                         case BluetoothSerialService.STATE_LISTEN:
                         case BluetoothSerialService.STATE_NONE:
                             if (mMenuItemConnect != null) {
                                 mMenuItemConnect.setIcon(android.R.drawable.ic_menu_search);
                                 mMenuItemConnect.setTitle(R.string.connect);
                             }
                             pairedDeviceText.setText(R.string.title_not_connected);

                             break;
                     }
                     break;
                 case MESSAGE_WRITE:
                     Log.d(TAG, "Message written");
                     break;
                 case MESSAGE_READ:
                     byte[] readBuf = (byte[]) msg.obj;
                     String readMessage = new String(readBuf, 0, msg.arg1); // construct a string from the valid bytes in the buffer
                     textView.setText(readMessage);
                     incomingDataPoints(readMessage);
                     break;
                 case MESSAGE_DEVICE_NAME:
                     // save the connected device's name
                     mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                     Toast.makeText(getApplicationContext(), getString(R.string.toast_connected_to) + " "
                             + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                     break;
                 case MESSAGE_TOAST:
                     Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                     break;
             }
         }
     };
     private BluetoothAdapter mBluetoothAdapter = null;
    private TextView textView, totalGameStrokes, currentBasketNumber, currentBasketPar, currenBasketStrokes;  // Used to view input from bt
     private boolean mEnablingBT;
     private boolean mLocalEcho = false;
     private boolean mAllowInsecureConnections = true;
     private int mOutgoingEoL_0D = 0x0D;
     private int mOutgoingEoL_0A = 0x0A;
     // Menu Variables
     private MenuItem updateBasket, updateTeepad, exitGame, mMenuItemConnect;
     private boolean updated = false;
     private LocationRequest mLocationRequest;
     private Location lastLocation;
    private TextView courseName, pairedDeviceText;
     private Boolean mRequestingLocationUpdates;
     private String mLastUpdateTime;
     private MapFragment gameMap;
     private PolylineOptions polylineOptions;
    private boolean basketConfirmation = false;
    private int currentPar = 0, currentStroke = 0, totalStrokeCount = 0, totalHoles;

     /**
      * *********************************** Call Asynch Task Methods *********************
      */
     public void saveGameData() {
         new DynamoDBManagerTask().execute(DynamoDBManagerType.SAVE_GAME);
     }

    public void loadCourse() {
        new DynamoDBManagerTask().execute(DynamoDBManagerType.GET_COURSE);
     }

    public void loadGameData() {
        new DynamoDBManagerTask().execute(DynamoDBManagerType.LOAD_GAME);
    }

    public void saveCourse() {
        new DynamoDBManagerTask().execute(DynamoDBManagerType.SAVE_COURSE);
    }

    /**
     * *********************************************************************************
     * Called when the activity is first created.
     */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_game_manager);

         // Setup Text Views
         textView = (TextView) findViewById(R.id.textView);
         courseName = (TextView) findViewById(R.id.courseName);
         pairedDeviceText = (TextView) findViewById(R.id.pairedDeviceText);
         totalGameStrokes = (TextView) findViewById(R.id.totalGameStrokesField);
         currentBasketPar = (TextView) findViewById(R.id.basketParField);
         currenBasketStrokes = (TextView) findViewById(R.id.basketStrokeField);
         currentBasketNumber = (TextView) findViewById(R.id.currentBasketField);

         // Setup Bluetooth
         mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
         if (mBluetoothAdapter == null) {
             finishDialogNoBluetooth();
             return;
         }
         mSerialService = new BluetoothSerialService(this, mHandlerBT, textView);

         // Automatically start up connection
         BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(getString(R.string.TeslaDiscAddress));
//         BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(getString(R.string.RNBTAddress));

         // Attempt to connect to the device
         mSerialService.connect(device);

         // Setup map
         mGoogleApiClient = new GoogleApiClient.Builder(this)
                 .addApi(LocationServices.API)
                 .addConnectionCallbacks(this)
                 .addOnConnectionFailedListener(this)
                 .build();
         gameMap = (MapFragment) getFragmentManager()
                 .findFragmentById(R.id.gameMap);
         gameMap.getMapAsync(this);
         setupLocationListener();

         course = new MapperCourse();
         currentGame = new MapperPlayedGame();

         // Setup Game Info
         loadCourse();
         while (course.getCourseName() == null) ;    // Wait for course to load
         currentBasket = 0;
         totalHoles = course.getBasketPars().size();

         courseName.setText(course.getCourseName());

//         MapperPlayedGame to save game data to server
         currentGame.setPlayedBy(MainActivity.currentUser.getUserName());
         currentGame.setGameLocation(course.getCourseName());

         // Time stamp used as game ID
         Date date = new Date();
         Timestamp sdf = new Timestamp(date.getTime());
         currentGame.setGameDate(date.toString());
         currentGame.setgameId(sdf.toString());

         //  Trajectory Setup
         polylineOptions = new PolylineOptions(); // Instantiating the class PolylineOptions to plot polyline in the map
         polylineOptions.color(Color.RED); // Setting the color of the polyline
         polylineOptions.width(7); // Setting the width of the polyline

         // Buttons
         Button exitGameBttn = (Button) findViewById(R.id.endGameBttn);
         exitGameBttn.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 Log.d(TAG, "exitGameBttn pressed");
                 saveAndExitGameDialog();
             }
         });

         Button nextBasketBttn = (Button) findViewById(R.id.nextBasketBttn);
         nextBasketBttn.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 Log.d(TAG, "nextBasketBttn pressed");
                 nextBasket();
             }
         });

         updateDisplay();
     }

    private void saveAndExitGameDialog() {
        Toast.makeText(this, "Exiting Game -> Need to implement save", Toast.LENGTH_LONG).show();

        // Add all compiled plot points
        currentGame.setPlotPoints(plotPoints);
        currentGame.setTotalStrokes(totalStrokeCount);
        currentGame.setLikes(0);
        // All other saved info should be up to date, ready to push to server

        saveGameData();

        // Show exit dialog
        Log.d(TAG, "Showing game results");
        GameSummaryDialog dialog = new GameSummaryDialog(this, currentGame);
        while (dialog.isShowing()) {
            // Wait until dialog is closed
        }

        // After dialog is dismissed, return to NewsFeed
        finish();
    }

    private void nextBasket() {
        Toast.makeText(this, "Moving on to the next basket", Toast.LENGTH_SHORT).show();
        currentGame.addHoleStroke(currentBasket, currentStroke);  // save stroke count for basket
        currentStroke = 0;                                        // reset stroke count for next hole

        currentBasket++;
        if (currentBasket >= MapperCourse.MAX_NUMBER_OF_HOLES) {
            gameFinished();
            Log.d(TAG, "Game successfully completed");
            Toast.makeText(this, "Game successfully completed!", Toast.LENGTH_SHORT).show();
        } else {
            updateDisplay();   // show correct values on display
        }
    }

    private void updateDisplay() {
        currentPar = (int) course.getABasketPar(currentBasket);
        totalGameStrokes.setText(String.valueOf(totalStrokeCount));     // Updated during incomingDataPoints
        currentBasketNumber.setText(String.valueOf(currentBasket + 1)); // updated during nextBasket
        currentBasketPar.setText(String.valueOf(currentPar));           // updated just now
        currenBasketStrokes.setText(String.valueOf(currentStroke));     // updated during incomingDataPoints/nextBasket
    }

    /**
     * ****************************************** App start/stop/resume/pause methods ***
     */

//     private void setupCourse() {
//         for (int i = 0; i < 18; i++) {
//             course.setABasketLongitude(i, 37.66);
//             course.setABasketLatitude(i, -105.77);
//             course.setABasketPar(i, 3);
//             course.setATpadLongitude(i, 36.66);
//             course.setATpadLatitude(i, -105.55);
//         }
//     }
    @Override
    public void onStart() {
        super.onStart();
        mEnablingBT = false;
        mGoogleApiClient.connect(); // Connect the Google Map client.
    }

     @Override
     public synchronized void onResume() {
         super.onResume();
         if (!mEnablingBT) { // If we are turning on the BT we cannot check if it's enable
             if ((mBluetoothAdapter != null) && (!mBluetoothAdapter.isEnabled())) {

                 AlertDialog.Builder builder = new AlertDialog.Builder(this);
                 builder.setMessage(R.string.alert_dialog_turn_on_bt)
                         .setIcon(android.R.drawable.ic_dialog_alert)
                         .setTitle(R.string.alert_dialog_warning_title)
                         .setCancelable(false)
                         .setPositiveButton(R.string.alert_dialog_yes, new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int id) {
                                 mEnablingBT = true;
                                 Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                 startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                             }
                         })
                         .setNegativeButton(R.string.alert_dialog_no, new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int id) {
                                 finishDialogNoBluetooth();
                             }
                         });
                 AlertDialog alert = builder.create();
                 alert.show();
             }

             if (mSerialService != null) {
                 // Only if the state is STATE_NONE, do we know that we haven't started already
                 if (mSerialService.getState() == BluetoothSerialService.STATE_NONE) {
                     // Start the Bluetooth chat services
                     mSerialService.start();
                 }
             }

             if (mBluetoothAdapter != null) {
                 updatePrefs();
             }
         }
     }

     @Override
     public synchronized void onPause() {
         super.onPause();
     }

     @Override
     public void onStop() {
         mGoogleApiClient.disconnect();
         super.onStop();
     }

     @Override
     public void onDestroy() {
         super.onDestroy();
         if (mSerialService != null)
             mSerialService.stop();
     }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000); // Update location every second
        mLocationRequest.setFastestInterval(5000);

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "GoogleApiClient connection has failed");
    }

    private void setupLocationListener() {
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            AlertDialog.Builder locationAlertBuilder = new AlertDialog.Builder(this);
            locationAlertBuilder.setTitle("Location Not Enabled")
                    .setMessage("Location Services are required for this application." +
                            "Please check your settings and try again.")
                    .setPositiveButton("Return to News Feed", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // if this button is clicked, close
                            // current activity
                            //                            numberOfTries = 0;
                            finish();
                        }
                    })
                    .setNegativeButton("Continue w/o Location", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // if this button is clicked, close
                            // current activity
                            dialog.dismiss();
                        }
                    });
            AlertDialog locationAlert = locationAlertBuilder.create();
            locationAlert.show();
        }
     }

    /**
     * ****************************************** Bluetooth methods ***
     */
    private void updatePrefs() {
        mSerialService.setAllowInsecureConnections(mAllowInsecureConnections);
    }

     public int getConnectionState() {
         return mSerialService.getState();
     }

     private byte[] handleEndOfLineChars(int outgoingEoL) {
         byte[] out;
         if (outgoingEoL == 0x0D0A) {
             out = new byte[2];
             out[0] = 0x0D;
             out[1] = 0x0A;
         } else {
             if (outgoingEoL == 0x00) {
                 out = new byte[0];
             } else {
                 out = new byte[1];
                 out[0] = (byte) outgoingEoL;
             }
         }
         return out;
     }

     private void sendMessage(String message) {
         byte[] sendByte = message.getBytes();
         send(sendByte);
     }

     public void send(byte[] out) {
         if (out.length == 1) {
             if (out[0] == 0x0D) {
                 out = handleEndOfLineChars(mOutgoingEoL_0D);
             } else {
                 if (out[0] == 0x0A) {
                     out = handleEndOfLineChars(mOutgoingEoL_0A);
                 }
             }
         }

         if (out.length > 0) {
             mSerialService.write(out);
         }
     }

     public void finishDialogNoBluetooth() {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(R.string.alert_dialog_no_bt)
                 .setIcon(android.R.drawable.ic_dialog_info)
                 .setTitle(R.string.app_name)
                 .setCancelable(false)
                 .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         finish();
                     }
                 });
         AlertDialog alert = builder.create();
         alert.show();
     }

     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch (requestCode) {
             case REQUEST_CONNECT_DEVICE:
                 // When DeviceListActivity returns with a device to connect
                 if (resultCode == Activity.RESULT_OK) {
                     // Get the device MAC address
                     String address = data.getExtras()
                             .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                     // Get the BLuetoothDevice object
                     BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                     // Attempt to connect to the device
                     mSerialService.connect(device);
                 }
                 break;

             case REQUEST_ENABLE_BT:
                 // When the request to enable Bluetooth returns
                 if (resultCode != Activity.RESULT_OK) {
                     Log.d(TAG, "BT not enabled");
                     finishDialogNoBluetooth();
                 }
         }
     }

     /**
      * ********************************** Mapping Methods *************************
      */
     @Override
     public void onMapReady(GoogleMap map) {
         map.setMyLocationEnabled(true);

         // Move camera to current phone location
         if ((lastLocation != null)) {
             LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
             map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
         } else {
             map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(course.getATpadLatitude(0), course.getATpadLongitude(0)), 18f));
         }

         updateMapDrawings();
     }

     private void updateMapDrawings() {
         // clear old markers
         gameMap.getMap().clear();

         gameMap.getMap().setMapType(GoogleMap.MAP_TYPE_SATELLITE);

         //**************** Add basket/tee-pad markers
         for (int i = 0; i < course.getBasketLatitudes().size(); i++) {
             gameMap.getMap().addMarker(new MarkerOptions()
                     .icon(BitmapDescriptorFactory.fromResource(R.drawable.basket_icon)) // basket
                     .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                     .position(new LatLng(course.getABasketLatitude(i), course.getABasketLongitude(i))));

             gameMap.getMap().addMarker(new MarkerOptions()
                     //.icon(BitmapDescriptorFactory.fromResource(R.drawable.)) // basket
                     .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                     .position(new LatLng(course.getATpadLatitude(i), course.getATpadLongitude(i))));
         }
     }

     private void updateCourseData(final String target) {
         //
         AlertDialog.Builder updateAlertBuilder = new AlertDialog.Builder(this);

         // set title
         updateAlertBuilder.setTitle("Update " + target);

         // set dialog message
         updateAlertBuilder.setMessage("You are attempting to update the current "
                 + target + ". Please place your phone as close to the center of the "
                 + target + "as possible, then click UPDATE. Otherwise, click CANCEL");
         updateAlertBuilder.setCancelable(false);
         updateAlertBuilder.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 // if this button is clicked, close
                 // current activity
                 if (target.equals(tPad)) {  // Trying to update current teepad
                     //  Update it with current location

                     if (lastLocation != null) {
                         course.setATpadLatitude(currentBasket, lastLocation.getLatitude());
                         course.setATpadLongitude(currentBasket, lastLocation.getLongitude());
                     }

                     //  Upload to the server
                     saveCourse();

                 } else {  // Trying to update current basket
                     if (lastLocation != null) {
                         course.setABasketLatitude(currentBasket, lastLocation.getLatitude());
                         course.setABasketLongitude(currentBasket, lastLocation.getLongitude());
                     }
                     //  Upload to the server
                     saveCourse();
                 }
                 updateMapDrawings();
             }
         });
         updateAlertBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 // if this button is clicked, just close the dialog box and do nothing
                 dialog.cancel();
             }
         });
         // create alert dialog
         AlertDialog updateAlert = updateAlertBuilder.create();

         // show it
         updateAlert.show();
     }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
    }

    private void updateUI() {
        if (!updated) {
            LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            gameMap.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
            updated = true;
        }
    }

    private void incomingDataPoints(String inputString) {   // method to deal with received data
         switch (inputString) {
             case "R": // Disc is checking if connection is ready
                 sendMessage(inputString);
                 break;
             case "F": //    Disc was in Find My Disc mode
                 break;
             default:       // Receiving GPS points
                 String[] result = inputString.split(",");
                 int mLength = result.length;

                 // Check if a new string has been started
                 if (mLength > 0 && result[0].equals("S")) {
                     Log.d(TAG, "String Started");

                     // Check if entire string has been received
                     if (result[mLength - 1].equals("E")) { // String has correct ending
                         Log.d(TAG, "*************** GOOD INPUT RECEIVED **************");
                         messageInProgress = false;
                         numberOfMessageTries = 0;
                         int doubleSize = mLength - 2;  // Ignore the last 1 of original message (E)

                         // Check if EOH was also received
                         if (result[mLength - 2].equals("EOH")) {
                             Log.d(TAG, "Received EOH signal");
                             nextBasket(); // Update values in TextViews
                             doubleSize = mLength - 3;  // Ignore the last 2 of original message (EOH and E)
                         }

                         // Parse out doubles into GPSinput array
                         double[] GPSinput = new double[doubleSize];
                         for (int z = 0; z < doubleSize; z++) {
                             try {
                                 GPSinput[z] = Double.parseDouble(result[z + 1]);   // Ignore the first one of original message (S)
                                 Log.d(TAG, "GPS Point" + z + ": " + GPSinput[z]);
                             } catch (NumberFormatException nfex) {
                                 Log.d(TAG, "Bad Input: " + result[z + 1]);
                             }
                             if (z == doubleSize)
                                 break;    // For some reason, the for loop wasn't working, needed this extra check
                         }

                         // Doubles parsed, ready to filter and add points to the map
                         Log.d(TAG, "Ready to filter");
                         if (GPSinput[0] != lastGPS) {  // In case message was sent multiple times
                             filterInput(GPSinput);
                             lastGPS = GPSinput[0];
                             currentStroke++;           // Increment stroke counts
                             totalStrokeCount++;
                             Log.d(TAG, "Adding new points");
                         } else {
                             Log.d(TAG, "Received duplicate points.");
                         }
                     }

                     // String wasn't sent in 1 message, will need to append additional points
                     else {
                         Log.d(TAG, "Bad end string, starting to wait for additional messages");
                         receivedMessage = inputString; // Save portion of received points
                         messageInProgress = true;
                         numberOfMessageTries++;        // Only attempt to add 5 messages together
                     }
                 }

                 // Check to see if a string is in progress of being fully received
                 else if (messageInProgress) {  /// Currently putting string together
                     Log.d(TAG, "*************** APPENDING MESSAGES");
                     receivedMessage += inputString;    // Add current
                     String[] appendedResults = receivedMessage.split(",");
                     mLength = appendedResults.length;

                     // Check to see if full string has finally been received
                     if (mLength > 0 && appendedResults[mLength - 1].equals("E")) {
                         // Completed new string
                         Log.d(TAG, "*************** GOOD INPUT RECEIVED **************");
                         messageInProgress = false;
                         numberOfMessageTries = 0;
                         int doubleSize = mLength - 2;

                         // Check to see if EOH signal was received
                         if (appendedResults[mLength - 2].equals("EOH")) {
                             Log.d(TAG, "Received EOH signal");
                             nextBasket();
                             doubleSize = mLength - 3;
                         }

                         // Parse doubles
                         double[] GPSinput = new double[doubleSize];
                         for (int z = 0; z < doubleSize; z++) {
                             try {
                                 GPSinput[z] = Double.parseDouble(appendedResults[z + 1]);
                                 Log.d(TAG, "GPS Point" + z + ": " + GPSinput[z]);
                             } catch (NumberFormatException nfex) {
                                 Log.d(TAG, "Bad Input: " + appendedResults[z + 1]);
                             }
                             if (z == doubleSize) break;
                         }

                         // Ready to add GPS points to the map
                         Log.d(TAG, "Ready to filter");
                         if (GPSinput[0] != lastGPS) {
                             filterInput(GPSinput);
                             lastGPS = GPSinput[0];
                             currentStroke++;
                             totalStrokeCount++;
                             Log.d(TAG, "Adding new points");
                         } else {
                             Log.d(TAG, "Received duplicate points.");
                         }
                     } else {
                         // Still need to complete string
                         receivedMessage += inputString;    // Add new input to currently compiled incomplete string
                         numberOfMessageTries++;
                         messageInProgress = true;          // Set flag to continue waiting for additional input
                         if (numberOfMessageTries > 5) { // Took too many tries to get message, abort
                             messageInProgress = false;
                         }
                         Log.d(TAG, "Appending new message, still need more");
                     }
                 }

                 // If string wasn't started correctly, or isn't in progress of being put together, disregard
                 break;
         }
     }

     private void filterInput(double[] GPSinput) {
         Toast.makeText(this, "Adding new points", Toast.LENGTH_SHORT).show();

         // Initialize constants and flight data
         //        GPSinput = trajectory.filter(GPSinput);
         trajectory.initData(GPSinput);
         points = trajectory.mapTrajectory();

         // Setting points of polyline
         polylineOptions.addAll(points);

         // Adding the polyline to the map
         gameMap.getMap().addPolyline(polylineOptions);

         // Polled GPS location from disc
         for (int i = 0; i < GPSinput.length - 1; i = i + 2) {
             gameMap.getMap().addMarker(new MarkerOptions().position(new LatLng(GPSinput[i], GPSinput[i + 1])));
         }

         // Keep track of plotted points
         for (int i = 0; i < GPSinput.length; i++) {
             Double temp = GPSinput[i];
             plotPoints.add(temp);
         }
     }


     //**************************************************** Game Play Method ******************
     private void gameFinished() {   // Method after completing 18 holes
         currentGame.setTotalStrokes(totalStrokeCount);  // Update final stroke tally
         // TODO: Take screen shot of flight trajectory, add it to currentGame; save to server
         MainActivity.currentUser.addPlayedGame(currentGame.getgameId());    // add to current user's profile
         saveGameData();     // Save to server
     }

    private void exitGameEarlyDialog() {
         AlertDialog.Builder exitDialogBuilder = new AlertDialog.Builder(this);
         exitDialogBuilder.setTitle("Exit Game");
         exitDialogBuilder.setMessage("Are you sure you want to exit? If you leave" +
                 "before a full game is over, your data will not be saved");
         exitDialogBuilder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 // if this button is clicked, close current activity
                 Intent intent = new Intent();
                 setResult(RESULT_CANCELED, intent);
                 finish();
             }
         });
         exitDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 // if this button is clicked, close
                 // current activity
                 dialog.dismiss();
             }
         });

         AlertDialog exitDialog = exitDialogBuilder.create();
         exitDialog.show();
     }

    private void exitGameDialog() {
        AlertDialog.Builder exitDialogBuilder = new AlertDialog.Builder(this);
        exitDialogBuilder.setTitle("Exit Game");
        exitDialogBuilder.setMessage("Are you sure you want to exit?");
        exitDialogBuilder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // if this button is clicked, close current activity
                Intent intent = new Intent();
                intent.putExtra("GameID", currentGame.getgameId());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        exitDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog exitDialog = exitDialogBuilder.create();
        exitDialog.show();
    }

     /**
      * ************************************ Menu Options ***************************
      */
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         // only add the menu when the menu is empty
         if (menu.size() == 0) {
             MenuInflater menuInflater = getMenuInflater();
             menuInflater.inflate(R.menu.option_menu, menu);
             mMenuItemConnect = menu.getItem(0);
             updateBasket = menu.getItem(1);
             updateTeepad = menu.getItem(2);
             exitGame = menu.getItem(3);
         }
         return true;
     }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed Called");
        exitGameEarlyDialog();
    }

     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.connect:
                 if (getConnectionState() == BluetoothSerialService.STATE_NONE) {
                     // Launch the DeviceListActivity to see devices and do scan
                     Intent serverIntent = new Intent(this, DeviceListActivity.class);
                     startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                 } else if (getConnectionState() == BluetoothSerialService.STATE_CONNECTED) {
                     mSerialService.stop();
                     mSerialService.start();
                 }
                 return true;
             case R.id.updateCurrentBasket:
                 updateCourseData(basket);
                 return true;
             case R.id.updateCurrentTeepad:
                 updateCourseData(tPad);
                 return true;
             case R.id.exitGame:
                 exitGameEarlyDialog();
                 return true;
         }
         return false;
     }

     public void doOpenOptionsMenu() {
         openOptionsMenu();
     }

     private enum DynamoDBManagerType {
         SAVE_GAME, GET_COURSE, SAVE_COURSE, LOAD_GAME
     }

     /**
      * *********************************** Inner Classes *****************************
      */
     private class DynamoDBManagerTask extends AsyncTask<DynamoDBManagerType, Void, String> {

         private double distance(double lat1, double lon1, double lat2, double lon2) {
             double theta = lon1 - lon2;
             double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
             dist = Math.acos(dist);
             dist = rad2deg(dist);
             dist = dist * 60 * 1.1515;
             return (dist);
         }

         /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
       /*::  This function converts decimal degrees to radians             :*/
       /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
         private double deg2rad(double deg) {
             return (deg * Math.PI / 180.0);
         }

         /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
       /*::  This function converts radians to decimal degrees             :*/
       /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
         private double rad2deg(double rad) {
             return (rad * 180.0 / Math.PI);
         }

         @Override
         protected String doInBackground(DynamoDBManagerType... types) {
             Log.d("DoINBackGround", "On doInBackground...");

             AmazonDynamoDBClient clientManager = new AmazonDynamoDBClient(MainActivity.credentials);
             DynamoDBMapper mapper = new DynamoDBMapper(clientManager);

             switch (types[0]) {
                 case GET_COURSE:
                     try {
//                         Log.d(TAG, "Loading all saved courses.....");
//                         // Retrieve all courses from saved User Table, returned in undetermined order
//                         DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
//                         PaginatedScanList scanResult = mapper.scan(MapperCourse.class, scanExpression);
//                         ArrayList<MapperCourse> result = new ArrayList<>();
//                         result.addAll(scanResult);        // Change result to ArrayList
//                         Log.d(TAG, "Retrieved all saved courses, searching for nearest course.....");
//
//                         if (result.size() < 2) { // Only one course saved, return that course
//                             course = result.get(0);
//                             Log.d(TAG, "Only one course, loaded " + course.getCourseName());
//                         } else {  // Compare loaded courses to user's current location
//                             double distanceLog = -1;
//                             int useThisOne = 0;
//                             for (int i = 0; i < result.size() - 1; i++) {
//                                 double distanceTemp = distance(lastLocation.getLatitude(), lastLocation.getLongitude(),
//                                         result.get(i).getATpadLatitude(0), result.get(i).getATpadLongitude(0));
//                                 if (distanceLog == -1)
//                                     distanceLog = distanceTemp;  // Set up distance log
//                                 else if (distanceTemp < distanceLog) // found a course that's closer to the user
//                                     useThisOne = i; // keep track of which course is closest
//                             }
//
//                             course = result.get(useThisOne);
//                             Log.d(TAG, "Ran through courses, loaded " + course.getCourseName());
//                         }
                         course = mapper.load(MapperCourse.class, "Test");
                         //TODO: Switch between saved courses
//                         course = mapper.load(MapperCourse.class, "Test2");
                         Log.d(TAG, "Course loaded " + course.getCourseName());
                     } catch (AmazonServiceException ex) {
                         Log.e(TAG, "Error loading course");
                         Log.e(TAG, "Error: " + ex);
                     }
                     break;
                 case SAVE_COURSE:
                     try {
                         Log.d(TAG, "Saving course");
                         mapper.save(course);
                         Log.e(TAG, "Course saved.");
                     } catch (AmazonServiceException ex) {
                         Log.e(TAG, "Error saving course");
                         Log.e(TAG, "Error: " + ex);
                     }
                     break;
                 case SAVE_GAME:
                     try {
                         Log.d(TAG, "Saving game data");
                         mapper.save(currentGame);
                         Log.d(TAG, "Game data saved");
                     } catch (AmazonServiceException ex) {
                         Log.e(TAG, "Error saving game data");
                         Log.e(TAG, "Error: " + ex);
                     }
                     break;
             }
             return null;
         }

         @Override
         protected void onPostExecute(String result) {
         }
     }

 }