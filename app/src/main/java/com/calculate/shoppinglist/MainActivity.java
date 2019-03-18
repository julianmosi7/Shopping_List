package com.calculate.shoppinglist;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
                    Position pos = new Position(txtposition.getText().toString(), numberPicker.getValue());
                    storesList.get(currentStore).addItem(pos);
                    toList(currentStore);
                    positionsAdapter.notifyDataSetChanged();
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
            }
        });
        alert.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alert.show();



    }



}
