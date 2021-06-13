package com.example.myloactiontracker;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final String TAG = "MainActivity";
    EditText et_firstname, et_LastName, et_phNumber, et_LAttitude;
    Button btn_loocation, btn_submit,btn_nextActivity;
    TextView tv_address;
    String firstName, LastName, PhoneNumber, Address;
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Context context;
    String lat;
    String provider;
    public double latitude;
    public double longitude;
    protected boolean gps_enabled, network_enabled;
    Location location;
    int geocoderMaxResults = 1;
    String result = null;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<String> student_list = new ArrayList<>();
    List<String> teacher_List = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkRunTimePermission();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        final Geocoder geocoder = new Geocoder(MainActivity.this, Locale.ENGLISH);


       /* FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);*/

        et_firstname = findViewById(R.id.et_firstName);
        et_LastName = findViewById(R.id.et_lastName);
        et_phNumber = findViewById(R.id.et_phoneNumber);
        btn_loocation = findViewById(R.id.btn_location);
        tv_address = findViewById(R.id.tv_address);
        btn_submit = findViewById(R.id.btn_submit);
        btn_submit.setVisibility(View.GONE);
        btn_loocation.setVisibility(View.VISIBLE);
        btn_nextActivity = findViewById(R.id.btn_nextActivity);


        btn_loocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  PermissionCheck();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isGPSEnable();
                        try {
                            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                            if (addressList != null && addressList.size() > 0) {
                                Address address = addressList.get(0);
                                System.out.println(address);
                                StringBuilder sb = new StringBuilder();
                                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                                    sb.append(address.getAddressLine(i)); //.append("\n");
                                }
                                sb.append(address.getLocality()).append("\n");
                                sb.append(address.getPostalCode()).append("\n");
                                sb.append(address.getCountryName());
                                result = sb.toString();
                                Log.d(TAG, "run: --------");
                                Log.d(TAG, "run: " + result);
                                Log.d(TAG, "run: " + address.getLocality());
                                Log.d(TAG, "run: " + address.getAddressLine(0));

                                tv_address.setText(address.getAddressLine(0));

                                Address = address.getAddressLine(0);

                              /*  if(Address.isEmpty()){
                                    tv_address.setText("Failed !! please try again...");
                                }else {
                                    tv_address.setText("Success !!");
                                    btn_submit.setVisibility(View.VISIBLE);
                                    btn_loocation.setVisibility(View.GONE);
                                }*/

                                btn_submit.setVisibility(View.VISIBLE);
                                btn_loocation.setVisibility(View.GONE);

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });

            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstName = et_firstname.getText().toString();
                LastName = et_LastName.getText().toString();
                PhoneNumber = et_phNumber.getText().toString();
                Log.d(TAG, "onClick: " + firstName);
                if (!firstName.equals("") && !LastName.equals("") && !Address.equals("") && !PhoneNumber.equals("")) {
                    DataPushing();
                } else {
                    Toast.makeText(MainActivity.this, "There is Some Blank Fields, Please Fill properly ...", Toast.LENGTH_LONG).show();
                }

            }
        });

        btn_nextActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FetchStudentCollectionData();
              //  FetchTeacherCollectionData();
                Intent intent = new Intent(MainActivity.this,Refresh.class);
                startActivity(intent);

            }
        });
    }

    private void DataPushing() {
        Map<String, Object> user = new HashMap<>();
        user.put("firstname", firstName);
        user.put("lastname", LastName);
        user.put("PhoneNumber", "+91" + PhoneNumber);
        user.put("Address", Address);
        db.collection("users").document("+91" + PhoneNumber)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + "+91" + PhoneNumber);
                        Toast.makeText(MainActivity.this, "Thank You!!! " + PhoneNumber, Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                        Toast.makeText(MainActivity.this, "Error Please try Again..", Toast.LENGTH_LONG).show();
                    }
                });

        Address = "";
        firstName = "";
        LastName = "";
        PhoneNumber = "";
        btn_submit.setVisibility(View.GONE);
        btn_loocation.setVisibility(View.VISIBLE);

    }

    private void FetchData(){
        DocumentReference docRef = db.collection("Student").document("Bunty");

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                   // String phone = documentSnapshot.getString("PhoneNumber");
                    String phone = documentSnapshot.getString("RegNo");
                    Log.d(TAG, "onComplete: phoneNumber is :"+phone);
                    Map<String, Object> data = documentSnapshot.getData();
                    Log.d(TAG, "onComplete: phoneNumber is :"+data);

                    if (documentSnapshot.exists()){
                        Log.d(TAG, "onComplete: DocumentSnapshort data:"+task.getResult());
                    }else {
                        Log.d(TAG, "onComplete: No such documents");
                    }
                }else {
                    Log.d(TAG, "onComplete: Get failed with"+task.getException());
                }
                }

        });

    }

    private void FetchStudentCollectionData(){
        db.collection("Student").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {


                         student_list.add(document.getId());

                        student_list.add(String.valueOf(document.getData()));
                    }

                    Log.d(TAG, "onComplete:StudentCollectionData: "+student_list.toString());


                } else {


                   // Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

    }

    private void FetchTeacherCollectionData(){
        // CollectionReference docRef = db.collection("Student");

        db.collection("Teacher").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // student_list.add(document.getId());
                        teacher_List.add(String.valueOf(document.getData()));
                    }
                    Log.d(TAG, teacher_List.toString());
                    Log.d(TAG, "onComplete:TeacherCollectionData: "+teacher_List.toString());
                    BuildAndPushUniversityData();
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

    }

    private void BuildAndPushUniversityData(){
        List<List> University = new ArrayList<>();
        University.add(student_list);
        University.add(teacher_List);
        Log.d(TAG, "onComplete:BuildAndPushUniversityData: "+University);
        for (int i = 0; i> databaseList().length;i++){
        }
    }

    public void checkRunTimePermission() {
        GPSTracker gpsTracker;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                gpsTracker = new GPSTracker(this);

            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                        10);
            }
        } else {
            gpsTracker = new GPSTracker(this); //GPSTracker is class that is used for retrieve user current location
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                GPSTracker gpsTracker = new GPSTracker(this);
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale((Activity) this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // If User Checked 'Don't Show Again' checkbox for runtime permission, then navigate user to Settings
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setTitle("Permission Required");
                    dialog.setCancelable(false);
                    dialog.setMessage("You have to Allow permission to access user location");
                    dialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package",
                                    MainActivity.this.getPackageName(), null));
                            //i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivityForResult(i, 1001);
                        }
                    });
                    AlertDialog alertDialog = dialog.create();
                    alertDialog.show();
                }
                //code for deny
                //101499979234
            }
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        switch (requestCode) {
            case 1001:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        GPSTracker gpsTracker = new GPSTracker(this);
                        if (location != null) {
                            latitude = gpsTracker.getLatitude();
                            longitude = gpsTracker.getLongitude();
                            Log.d(TAG, "startActivityForResult: " + latitude);
                        }
                    } else {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 10);
                    }
                }
                break;
            default:
                break;
        }
    }

    public String getAddressLine(Context context) {
        List<Address> addresses = getGeocoderAddress(context);

        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String addressLine = address.getAddressLine(0);
            Log.d(TAG, "getAddressLine: " + addressLine);

            return addressLine;
        } else {
            return null;
        }
    }

    public List<Address> getGeocoderAddress(Context context) {
        if (location != null) {

            Geocoder geocoder = new Geocoder(context, Locale.ENGLISH);

            try {
                /**
                 * Geocoder.getFromLocation - Returns an array of Addresses
                 * that are known to describe the area immediately surrounding the given latitude and longitude.
                 */
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, this.geocoderMaxResults);

                return addresses;
            } catch (IOException e) {
                //e.printStackTrace();
                Log.e(TAG, "Impossible to connect to Geocoder", e);
            }
        }

        return null;
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        Log.d(TAG, "onLocationChanged: " + latitude);
        Log.d(TAG, "onLocationChanged: " + longitude);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public static void getAddressFromLocation(final double latitude, final double longitude, final Context context, final Handler handler) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                String result = null;
                try {
                    List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                    if (addressList != null && addressList.size() > 0) {
                        Address address = addressList.get(0);
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                            sb.append(address.getAddressLine(i)); //.append("\n");
                        }
                        sb.append(address.getLocality()).append("\n");
                        sb.append(address.getPostalCode()).append("\n");
                        sb.append(address.getCountryName());
                        result = sb.toString();

                        Log.d(TAG, "run: " + result);

                    }
                } catch (IOException e) {
                    Log.e("Location Address Loader", "Unable connect to Geocoder", e);
                } finally {
                    Message message = Message.obtain();
                    message.setTarget(handler);
                    if (result != null) {
                        message.what = 1;
                        Bundle bundle = new Bundle();
                        bundle.putString("address", result);
                        message.setData(bundle);
                    } else {
                        message.what = 1;
                        Bundle bundle = new Bundle();
                        result = " Unable to get address for this location.";
                        bundle.putString("address", result);
                        message.setData(bundle);
                    }
                    message.sendToTarget();
                }
            }
        };
        thread.start();
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            Log.e("location Address=", locationAddress);
        }
    }

    public void showSettingAlert() {
        AlertDialog.Builder alerDialog = new AlertDialog.Builder(MainActivity.this);
        alerDialog.setTitle("GPS Settings");
        alerDialog.setMessage("GPS is not enable, Do you want to go to setting menu?");
        alerDialog.setPositiveButton("settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alerDialog.show();
    }

    public void showSettingAlert2() {
        AlertDialog.Builder alerDialog = new AlertDialog.Builder(MainActivity.this);
        alerDialog.setTitle("GPS Settings");
        alerDialog.setMessage("GPS is not enable, Do you want to go to setting menu?");
        alerDialog.setPositiveButton("settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_DATA_USAGE_SETTINGS);
                startActivity(intent);
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alerDialog.show();
    }

    public void isGPSEnable() {
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enable = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enable) {
            showSettingAlert();
        }
    }
}



