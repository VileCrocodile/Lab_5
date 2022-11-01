package com.example.androidlabs;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lab3b.R;

import java.sql.SQLDataException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<todoItem> toDoList = new ArrayList<>();
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            loadDataFromDB();
        } catch (SQLDataException e) {
            e.printStackTrace();
        }

        ListView listView = findViewById(R.id.theList);
        ListAdapter adapter = new ListAdapter(getApplicationContext());
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener((parent, view, pos, id)-> {

            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setTitle(R.string.delete_warning)
                    .setMessage(R.string.delete_msg + pos)
                    .setPositiveButton("Yes", (click, arg) -> {
                        deleteFromDB(pos, id);
                        adapter.notifyDataSetChanged();
                    })
                    .setNegativeButton("No", (click,arg) -> {} );
            alertBuilder.show();
            return true;
        });

        //setting up the user interaction
        EditText userText = findViewById(R.id.theEditor);
        Button addButton = findViewById(R.id.theButton);
        SwitchCompat urgentSwitch = findViewById(R.id.theSwitch);

        addButton.setOnClickListener(btn -> {
                    todoItem item = new todoItem();
                    item.setUrgency(urgentSwitch.isChecked());
                    item.setName(userText.getText().toString());
                    userText.setText("");
                    urgentSwitch.setChecked(false);
                    insertIntoDB(item);
                    adapter.notifyDataSetChanged();
                }
        );

    }

    public class ListAdapter extends BaseAdapter {

        Context context;
        LayoutInflater inflater;

        public ListAdapter(Context ctx) {

            this.context = ctx;
            inflater = LayoutInflater.from(ctx);

        }

        @Override
        public int getCount() {
            return toDoList.size();
        }

        @Override
        public todoItem getItem(int position) {
            return toDoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return toDoList.get(position).getID();
        }

        @Override
        public View getView(int position, View old, ViewGroup parent) {

            View newView = old;

            if (newView == null){

                newView = inflater.inflate(R.layout.item_row, parent, false);

            }

            TextView tVu = newView.findViewById(R.id.itemRow);
            tVu.setText(getItem(position).getName());
            if  (getItem(position).isUrgent()) {
                tVu.setBackgroundColor(Color.RED);
                tVu.setTextColor(Color.WHITE);
            }

            return newView;
        }

    }

    private void loadDataFromDB() throws SQLDataException {

        CustomOpenHelper DBOpener = new CustomOpenHelper(this);
        db = DBOpener.getWritableDatabase();

        String[] columns = {CustomOpenHelper.COL_ID, CustomOpenHelper.COL_ITEM, CustomOpenHelper.COL_URGENT};
        Cursor results = db.query(CustomOpenHelper.TABLE_NAME, columns, null,null,null,null,null);

        int itemIndex = results.getColumnIndex(CustomOpenHelper.COL_ITEM);
        int urgency = results.getColumnIndex(CustomOpenHelper.COL_URGENT);
        int idIndex = results.getColumnIndex(CustomOpenHelper.COL_ID);

        while (results.moveToNext()){

            String itemName = results.getString(itemIndex);
            boolean itemUrgency = (results.getString(urgency).equals("1"));
            int id = results.getInt(idIndex);
            todoItem newItem = new todoItem();
            newItem.setID(id);
            newItem.setName(itemName);
            newItem.setUrgency(itemUrgency);
            toDoList.add(newItem);

        }

        results.close();
    }

    private void insertIntoDB(todoItem todo) {

        ContentValues newValues = new ContentValues();
        newValues.put(CustomOpenHelper.COL_ITEM, todo.getName());//adding itemName

        int urgency;
        if (todo.isUrgent()){
            urgency = 1;
        } else urgency = 0;
        newValues.put(CustomOpenHelper.COL_URGENT, urgency);//adding urgency

        long newID = db.insert(CustomOpenHelper.TABLE_NAME, null, newValues);
        toDoList.add(todo);
        Toast.makeText(this, "Inserted item id: "+ newID, Toast.LENGTH_SHORT).show();

    }

    private void deleteFromDB (int pos, long id){

        db.delete(CustomOpenHelper.TABLE_NAME, CustomOpenHelper.COL_ID + "=" + id,null);
        toDoList.remove(pos);
        Toast.makeText(this, "Deleted item at row: " + (pos + 1), Toast.LENGTH_SHORT).show();

    }

    }

