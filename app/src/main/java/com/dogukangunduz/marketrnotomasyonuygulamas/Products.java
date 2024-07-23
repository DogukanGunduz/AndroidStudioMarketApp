package com.dogukangunduz.marketrnotomasyonuygulamas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.dogukangunduz.marketrnotomasyonuygulamas.databinding.ActivityMainBinding;
import com.dogukangunduz.marketrnotomasyonuygulamas.databinding.ActivityProductsBinding;

import java.util.ArrayList;

public class Products extends AppCompatActivity {
    public ActivityProductsBinding binding;

    ArrayList<Integer> ids = new ArrayList<Integer>();
    ArrayList<String> names = new ArrayList<String>();
    ArrayList<String> expressions = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        try {
            SQLiteDatabase db = this.openOrCreateDatabase("market_db", MODE_PRIVATE, null);
            Cursor cursor = db.rawQuery("SELECT * FROM products ", null);
            int idX = cursor.getColumnIndex("id");
            int nameX = cursor.getColumnIndex("productName");
            while (cursor.moveToNext()) {
                ids.add(cursor.getInt(idX));
                names.add(cursor.getString(nameX));
                expressions.add(cursor.getInt(idX) + "-" + cursor.getString(nameX));
            }
            ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,expressions);
            binding.products.setAdapter(adapter);
            cursor.close();

            binding.products.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String choosen = expressions.get(position);
                    String[] parts = choosen.split("-");
                    int showingProduct = Integer.parseInt(parts[0]);
                    Intent goToDetails = new Intent(Products.this,ProductDetail.class);
                    goToDetails.putExtra("id", showingProduct);
                    goToDetails.putExtra("reason","showProduct");
                    startActivity(goToDetails);

                }
            });
        } catch (Exception e) {
            Toast.makeText(Products.this,"Failed to open", Toast.LENGTH_SHORT).show();
        }
    }
    public void addNewProduct(View v){
        Intent intent = new Intent(Products.this, ProductDetail.class);
        intent.putExtra("reason","addProduct");
        startActivity(intent);
    }
}
