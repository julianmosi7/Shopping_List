package com.calculate.shoppinglist;

import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
        isJSONavailable("shoppingList");


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
        }
        return super.onOptionsItemSelected(item);
    }

    private void toList(int position){
        positionsList.clear();
        positionsList.addAll(storesList.get(position).getPosition());
    }

    private void dialog_position(){
        final View vDialog = getLayoutInflater().inflate(R.layout.dialog_position, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("add new Position");
        final EditText txtposition = vDialog.findViewById(R.id.txtposition);
        final NumberPicker numberPicker = vDialog.findViewById(R.id.numberPicker);
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
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("add new Store");
        final EditText txtstorename = new EditText(this);
        alert.setView(txtstorename);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Store store = new Store(txtstorename.getText().toString());
                storesList.add(store);
                storeAdapter.notifyDataSetChanged();
                writeToFile("shoppingList");
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
            Toast.makeText(this, sJson, Toast.LENGTH_LONG).show();
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
                Store store = gson.fromJson(sJson, Store.class);
                Toast.makeText(this, store.getPosition().toString(), Toast.LENGTH_LONG).show();

            }catch(Exception e){
                e.printStackTrace();
                Toast.makeText(this, "No Shopping-List available yet", Toast.LENGTH_LONG).show();

            }

        }
    }




