package com.example.ndhfos.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ndhfos.Database.ItemsDatabase;
import com.example.ndhfos.POJO.Item;
import com.example.ndhfos.R;
import com.example.ndhfos.Utility.AppExecutors;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

//import android.widget.ImageView;

public class CheckoutItemAdapter extends ArrayAdapter<Item> {
	
	private static final String LOG_TAG = CheckoutItemAdapter.class.getSimpleName();
	private ItemsDatabase database;
	
	public CheckoutItemAdapter ( Context context, List<Item> objects ) {
		super( context, 0, objects );
	}
	
	@NonNull
	@Override
	public View getView ( int position, @Nullable View convertView, @NonNull ViewGroup parent ) {
		
		if ( convertView == null )
			convertView = ( (Activity) getContext() ).getLayoutInflater().inflate( R.layout.list_item_checkout, parent, false );
		
		TextView itemNameTV = convertView.findViewById( R.id.item_name_cart_tv );
		TextView priceTV = convertView.findViewById( R.id.price_cart_tv );
		TextView quantityTV = convertView.findViewById( R.id.current_quantity_cart_tv );
		
		//TODO : ImageView itemImage = convertView.findViewById(R.id.item_image);
		//TODO: Fix button clicks
		
		ImageButton deleteBT = convertView.findViewById( R.id.remove_from_checkout );
		Button increaseQuantityBT = convertView.findViewById( R.id.increase_quantity_cart );
		Button decreaseQuantityBT = convertView.findViewById( R.id.decrease_quantity_cart );
		
		Item item = getItem( position );
		database = ItemsDatabase.getInstance( getContext() );
		
		if ( item == null )
			return super.getView( position, convertView, parent );
		
		itemNameTV.setText( item.getName() );
		int quantity = item.getQuantity();
		priceTV.setText( getContext().getResources().getString(
				
				R.string.price_holder,
				(double) quantity * item.getPrice()
		
		) );
		quantityTV.setText( String.valueOf( item.getQuantity() ) );
		
		if ( quantity == 1 ) {
			
			decreaseQuantityBT.setVisibility( View.INVISIBLE );
			deleteBT.setVisibility( View.VISIBLE );
			
		} else {
			
			decreaseQuantityBT.setVisibility( View.VISIBLE );
			deleteBT.setVisibility( View.INVISIBLE );
			
		}
		
		Log.i( LOG_TAG, "Current Quantity" + quantity + " " + item.getName() );
		
		increaseQuantityBT.setOnClickListener( ( event ) -> {
			int currentQuantity = updateCart( item, true );
			quantityTV.setText( String.valueOf( currentQuantity ) );
			decreaseQuantityBT.setVisibility( View.VISIBLE );
			deleteBT.setVisibility( View.INVISIBLE );
		} );
		
		decreaseQuantityBT.setOnClickListener( ( event ) -> {
			int currentQuantity = updateCart( item, false );
			if ( currentQuantity == 1 ) {
				
				deleteBT.setVisibility( View.VISIBLE );
				decreaseQuantityBT.setVisibility( View.INVISIBLE );
				quantityTV.setText( "1" );
				
			} else {
				
				quantityTV.setText( String.valueOf( item.getQuantity() ) );
			}
		} );
		
		deleteBT.setOnClickListener( ( event ) ->
				
				new AlertDialog.Builder( getContext() )
						.setTitle( "Delete Item" )
						.setMessage( "Are you sure you want to remove " + item.getName() + " from the cart?" )
						.setPositiveButton( "Yes, please", ( dialog, which ) -> {
							
							deleteItem( item );
							item.setQuantity( 0 );
							Snackbar itemDeleted = Snackbar.make( ( (Activity) getContext() ).findViewById( android.R.id.content )
									, "Removed " + item.getName() + " from cart.",
									Snackbar.LENGTH_SHORT
							);
							
							itemDeleted.getView().setBackgroundColor( Color.RED );
							( (TextView) itemDeleted.getView()
									.findViewById( com.google.android.material.R.id.snackbar_text ) )
									.setTextColor( Color.WHITE );
							itemDeleted.show();
							
							
						} )
						.setNegativeButton( "No, I have changed my mind", null )
						.create()
						.show()
		
		);
		
		return convertView;
		
	}
	
	private int updateCart ( Item item, boolean increase ) {
		
		int currentQuantity = item.getQuantity() + ( increase ? 1 : -1 );
		Log.i( LOG_TAG, currentQuantity + " " + item.getName() + "s in cart" );
		if ( currentQuantity <= 0 ) {
			AppExecutors.getInstance().getDiskIO().execute( () -> database.itemDAO().deleteItem( item ) );
			currentQuantity = 0;
		} else {
			item.setQuantity( currentQuantity );
			AppExecutors.getInstance().getDiskIO().execute( () -> database.itemDAO().updateItem( item ) );
		}
		
		item.setQuantity( currentQuantity );
		
		return currentQuantity;
		
	}
	
	private void deleteItem ( Item item ) {
		AppExecutors.getInstance()
				.getDiskIO()
				.execute( () -> database.itemDAO().deleteItem( item ) );
		remove( item );
		notifyDataSetChanged();
	}
	
}
