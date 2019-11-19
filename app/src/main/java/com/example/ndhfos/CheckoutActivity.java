package com.example.ndhfos;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
//import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.ndhfos.Adapters.CheckoutItemAdapter;
import com.example.ndhfos.Database.ItemsDatabase;
import com.example.ndhfos.POJO.Item;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CheckoutActivity extends AppCompatActivity {

    private ItemsDatabase database;

    private CheckoutItemAdapter checkoutItemAdapter;

    private LinearLayout totalCostHolder, confirmBlock;

    private ListView checkoutList;

    private TextView emptyView, orderNumberTV, itemCountTV, totalCostTV;

    //private Spinner blockSpinner;

    private RadioGroup paymentOptions;

    private Button placeOrderBT;

    private View separator;

    private List<Item> items;

    private String key;

    private int orderNumber;

    private boolean loggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        Bundle extras = getIntent().getExtras();

        if(extras == null)
            finish();
        else if(extras.containsKey("restaurant"))
            key = extras.getString("restaurant");
        else
            finish();

        loggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;

        Toolbar toolbar = findViewById(R.id.toolbar_cart);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle(R.string.order_details);

        checkoutList = findViewById(R.id.list_checkout);

        totalCostHolder = findViewById(R.id.total_cost_holder);
        confirmBlock = findViewById(R.id.confirm_block);

        orderNumberTV = findViewById(R.id.order_number_tv);
        orderNumberTV.setVisibility(View.INVISIBLE);
        itemCountTV = findViewById(R.id.item_count_tv);
        totalCostTV = findViewById(R.id.total_cost_tv);

        //blockSpinner = findViewById(R.id.block_spinner);
        //TODO : Implement block spinner

        paymentOptions = findViewById(R.id.payment_options);

        placeOrderBT = findViewById(R.id.order_button);

        if(loggedIn)
            placeOrderBT.setText(R.string.checkout);
        else
            placeOrderBT.setText(R.string.sign_in);

        emptyView = findViewById(R.id.empty_view);
        separator = findViewById(R.id.separator);

        database = ItemsDatabase.getInstance(CheckoutActivity.this);

        items = database.itemDAO().viewItems();

        checkoutItemAdapter = new CheckoutItemAdapter(CheckoutActivity.this, items);
        checkoutList.setAdapter(checkoutItemAdapter);

        itemCountTV.setText(getString(items.size()==1?
                R.string.number_of_item:R.string.number_of_items,items.size()));
        getOrderNumber();
        getData();

    }

    private void getData(){

        database.itemDAO().viewCart().observe(CheckoutActivity.this, items -> {

                if(items.isEmpty()){

                    emptyView.setVisibility(View.VISIBLE);
                    separator.setVisibility(View.INVISIBLE);
                    totalCostHolder.setVisibility(View.INVISIBLE);
                    confirmBlock.setVisibility(View.INVISIBLE);
                    checkoutList.setVisibility(View.INVISIBLE);
                    orderNumberTV.setVisibility(View.INVISIBLE);
                    itemCountTV.setVisibility(View.INVISIBLE);
                    paymentOptions.setVisibility(View.INVISIBLE);
                    placeOrderBT.setVisibility(View.INVISIBLE);

                } else {

                    emptyView.setVisibility(View.INVISIBLE);

                    checkoutItemAdapter = new CheckoutItemAdapter(CheckoutActivity.this, items);
                    if(items.size() != this.items.size()) {
                        checkoutList.setAdapter(checkoutItemAdapter);
                        this.items = items;
                        itemCountTV.setText(getString(items.size()==1?
                                R.string.number_of_item:R.string.number_of_items,items.size()));
                    }

                    double sum = 0;

                    for(Item item : items)
                        sum += (double)item.getQuantity() * item.getPrice();


                    totalCostTV.setText(getString(R.string.price_holder, sum));

                }

        });

    }

    private void getOrderNumber(){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("restaurants")
                .document(key)
                .collection("orders")
                .get()
                .addOnCompleteListener((task)->{

                    if(task.getResult()!=null){

                        List<DocumentSnapshot> documents = task.getResult().getDocuments();

                        orderNumber = documents.size()+1;

                        orderNumberTV.setText(getString(R.string.order_number, orderNumber));
                        orderNumberTV.setVisibility(View.VISIBLE);

                    }

                });

    }

}
