package montacer.elfazazi.ejerc5clasepmdmtema1;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ktx.Firebase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import montacer.elfazazi.ejerc5clasepmdmtema1.adapter.ProductAdapter;
import montacer.elfazazi.ejerc5clasepmdmtema1.configuracion.Constantes;
import montacer.elfazazi.ejerc5clasepmdmtema1.databinding.ActivityMainBinding;
import montacer.elfazazi.ejerc5clasepmdmtema1.modelos.Product;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ArrayList<Product> productList;
    private ProductAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private FirebaseDatabase database;
    private DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        productList = new ArrayList<>(); //importante, no olvidar inicializar lista al crearla
        database = FirebaseDatabase.getInstance("https://ejemplo-fire-base-b-default-rtdb.europe-west1.firebasedatabase.app/");
        reference = database.getReference(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("lista");

        adapter = new ProductAdapter(productList, R.layout.product_view_holder, MainActivity.this, reference); //adapter siempre despues de inicializar el arraylist
            //este es el contructor del ProductAdapter, le pasamos: la lista, que vista queremos mostrar q es el resource y donde queremos mostrarla que es en el main

        layoutManager = new GridLayoutManager(this, 2); //mostrara las cosas en columnas de 2

        binding.contentMain.container.setAdapter(adapter);
        binding.contentMain.container.setLayoutManager(layoutManager);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createProduct().show();
               }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    productList.clear();
                    GenericTypeIndicator<ArrayList<Product>> gti = new GenericTypeIndicator<ArrayList<Product>>() {};
                    productList.addAll(snapshot.getValue(gti));
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private AlertDialog createProduct(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle(R.string.titleCreate);
        builder.setCancelable(false);

        View productViewModel = LayoutInflater.from(this).inflate(R.layout.product_view_model, null);
        EditText txtName = productViewModel.findViewById(R.id.txtNameProductViewModel);
        EditText txtQuantity = productViewModel.findViewById(R.id.txtQuantityProductViewModel);
        EditText txtPrice = productViewModel.findViewById(R.id.txtPriceProductViewModel);
        TextView lbTotal = productViewModel.findViewById(R.id.lbTotalProductViewModel);

        builder.setView(productViewModel);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int quantity = Integer.parseInt(txtQuantity.getText().toString());
                    float price = Float.parseFloat(txtPrice.getText().toString());

                    float total = quantity * price;

                    lbTotal.setText(String.valueOf(total)+"$");
                }catch (Exception e){

                }
            }
        };

        txtQuantity.addTextChangedListener(textWatcher);
        txtPrice.addTextChangedListener(textWatcher);

        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (txtName.getText().toString().isEmpty() || txtQuantity.getText().toString().isEmpty() ||
                txtPrice.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this, R.string.missing, Toast.LENGTH_SHORT).show();
                }else {
                    Product product = new Product(txtName.getText().toString(),
                            Integer.parseInt(txtQuantity.getText().toString()),
                            Float.parseFloat(txtPrice.getText().toString()));

                    productList.add(0, product);
                   // adapter.notifyItemInserted(0);
                    reference.setValue(productList);
                   // Toast.makeText(MainActivity.this, product.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });


        return builder.create();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.menu_logout, menu);
         return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
         super.onOptionsItemSelected(item);
         if (item.getItemId() == R.id.btnSalir){
             FirebaseAuth.getInstance().signOut();
             startActivity(new Intent(MainActivity.this, LoginActivity.class));
             finish();
         }
         return true;
    }
}