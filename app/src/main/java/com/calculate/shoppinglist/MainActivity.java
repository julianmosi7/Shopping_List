package com.calculate.shoppinglist;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public Spinner spinner;
    public ListView listView;
    public List<Store> storesList = new ArrayList();
    public List<Position> positionsList = new ArrayList();
    public int currentStore;
    private static final int RQ_WRITE_STORAGE = 12345;
    LocationManager locationManager;
    private static final int RQ_ACCESS_FINE_LOCATION = 123;
    private boolean isGpsAllowed = false;

    public ArrayAdapter<Position> positionsAdapter;
    public ArrayAdapter<Store> storeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner = findViewById(R.id.store);
        listView = findViewById(R.id.postitions);

        initCombo(spinner);
        bindAdapterToListView(listView);

        registerForContextMenu(listView);

        registerSystemService();

        checkpermissionGPS();
        getNotification();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                positionsList.clear();
                positionsAdapter.notifyDataSetChanged();
                if(storesList.get(position).getPosition() != null){
                    toList(position);
                }
                currentStore = position;
                for (Position pos :
                     positionsList) {
                    System.out.println(pos.toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        isJSONavailable("shoppingList");
        getNotification();
    }


    private void initCombo(Spinner spinner){
        storeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, storesList);
        spinner.setAdapter(storeAdapter);
    }

    private void bindAdapterToListView(ListView lv){
        positionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, positionsList);
        lv.setAdapter(positionsAdapter);
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menue, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        switch (id){
            case R.id.menu_addPosition:
                dialog_position();
                break;
            case R.id.menu_addStore:
                dialog_store();
                break;
            case R.id.menu_preferences:
                Intent intent = new Intent(this, MySettingsActivity.class);
                startActivityForResult(intent, 0);
        }
        return super.onOptionsItemSelected(item);
    }

    private void toList(int position){
        positionsList.clear();
        positionsList.addAll(storesList.get(position).getPosition());
    }

    private void dialog_position(){
        if(isGpsAllowed){
            try {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                for (Store store:
                     storesList) {
                    if(((location.getLongitude()-store.getLongitude() < 100)&&(location.getLongitude()-store.getLongitude() > -100))&&((location.getLatitude()-store.getLatitude() < 100)&&location.getLatitude()-store.getLatitude() > -100)&&(!store.getPosition().isEmpty())){
                        dialog_note(store.getName());
                    }
                }

            }catch(SecurityException ex){
                ex.printStackTrace();
            }

        }

        getNotification();
        final View vDialog = getLayoutInflater().inflate(R.layout.dialog_position, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("add new Position");
        final EditText txtposition = vDialog.findViewById(R.id.storename);
        final NumberPicker numberPicker = vDialog.findViewById(R.id.numberPicker);
        numberPicker.setMaxValue(100);
        numberPicker.setMinValue(1);
        alert.setView(vDialog);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try{
                    Position pos = new Position(storesList.get(currentStore).getPosition().size()+1, txtposition.getText().toString(), numberPicker.getValue());
                    storesList.get(currentStore).addItem(pos);
                    toList(currentStore);
                    positionsAdapter.notifyDataSetChanged();
                    writeToFile("shoppingList");

                    if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            !=PackageManager.PERMISSION_GRANTED){
                        requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, RQ_WRITE_STORAGE);
                    }else{
                        writeToFile("shoppingList");
                    }

                    Toast.makeText(getApplicationContext(), "Position added", Toast.LENGTH_LONG).show();
                }catch(IndexOutOfBoundsException ex){
                    ex.printStackTrace();
                    Toast.makeText(getApplicationContext(), "No Store selected", Toast.LENGTH_LONG).show();
                }

            }
        });
        alert.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alert.show();

    }

    private void dialog_store(){
        final View vDialog = getLayoutInflater().inflate(R.layout.dialog_store, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("add new Store");
        final EditText txtstorename = vDialog.findViewById(R.id.storename);
        final EditText txtlatitude = vDialog.findViewById(R.id.latitude);
        final EditText txtlongitude = vDialog.findViewById(R.id.longitude);
        final Button btn = vDialog.findViewById(R.id.coordinates_button);
        alert.setView(vDialog);


        btn.setOnClickListener(new View.OnClickListener() {
                                   @Override
                                   public void onClick(View v) {
                                       if(isGpsAllowed){
                                           try {
                                               Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                               txtlatitude.setText(String.valueOf(location.getLatitude()));
                                               txtlongitude.setText(String.valueOf(location.getLongitude()));


                                           }catch(SecurityException ex){
                                               ex.printStackTrace();
                                           }

                                       }
                                   }
                               });


                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Store store = new Store(txtstorename.getText().toString(), Double.parseDouble(txtlatitude.getText().toString()), Double.parseDouble(txtlongitude.getText().toString()));
                        storesList.add(store);
                        storeAdapter.notifyDataSetChanged();


                        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RQ_WRITE_STORAGE);
                        } else {
                            writeToFile("shoppingList");
                        }

                    }
                });
        alert.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alert.show();



    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        getMenuInflater().inflate(R.menu.contextmenue, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    public boolean onContextItemSelected(MenuItem item){
        if(item.getItemId() == R.id.context_delete){
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            String name = "";
            if(info != null){
                long id = info.id;
                int pos = info.position;
                storesList.get(currentStore).getPosition().remove(pos);
                positionsList.remove(pos);
                positionsAdapter.notifyDataSetChanged();
            }
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void writeToFile(String filename) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) return;
        File outFile = Environment.getExternalStorageDirectory();
        String path = outFile.getAbsolutePath();
        String fullPath = path + File.separator + filename;
        System.out.println(fullPath);
        System.out.println("-----");
        try {
            PrintWriter out = new PrintWriter((new OutputStreamWriter(new FileOutputStream(fullPath))));
            String sJson = gson.toJson(storesList);
            out.write(sJson);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


        private void isJSONavailable(String filename){
            String sJson = "";
            String state = Environment.getExternalStorageState();
            if(!state.equals(Environment.MEDIA_MOUNTED)) return;
            File inFile = Environment.getExternalStorageDirectory();
            String path = inFile.getAbsolutePath();
            String fullPath = path + File.separator + filename;
            System.out.println("-----");
            System.out.println(fullPath);

            try{
                BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fullPath)));
                sJson = in.readLine();
                in.close();


                Gson gson = new Gson();
                TypeToken<List<Store>> storeinput = new TypeToken<List<Store>>(){};
                //storesList = gson.fromJson(sJson, storeinput.getType());
                storeAdapter.notifyDataSetChanged();
                positionsAdapter.notifyDataSetChanged();


            }catch(Exception e){
                e.printStackTrace();
                Toast.makeText(this, "No Shopping-List available yet", Toast.LENGTH_LONG).show();

            }

        }

        private void registerSystemService(){
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        }

       private void checkpermissionGPS(){
           String permission = Manifest.permission.ACCESS_FINE_LOCATION;
           if(ActivityCompat.checkSelfPermission(this, permission)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {permission}, RQ_ACCESS_FINE_LOCATION);
         }else{
             gpsGranted();
          }
        }


        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if(requestCode==RQ_WRITE_STORAGE){
                if(grantResults.length>0 && grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "SD Card Permission denied", Toast.LENGTH_SHORT).show();
                }else{
                    writeToFile("shoppingList");
                }
            }else if(requestCode==RQ_ACCESS_FINE_LOCATION){
                if(grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_LONG);
                }else{
                    gpsGranted();
                }
            }

        }

        private void gpsGranted(){
        isGpsAllowed = true;
        }

        private void getNotification(){
            //final Intent intent = new Intent(this, MainActivity.class);
            //PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationChannel channel = new NotificationChannel("1", "channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("description");
            System.out.println("build");
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(android.R.drawable.ic_input_add)
                    .setContentTitle("Store!")
                    .setContentText("You are near a store!");


            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            notificationManager.notify(1, mBuilder.build());

        }

    private void dialog_note(String name){
        getNotification();
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("You are near " + name + "!");

        alert.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alert.show();
    }

    }




