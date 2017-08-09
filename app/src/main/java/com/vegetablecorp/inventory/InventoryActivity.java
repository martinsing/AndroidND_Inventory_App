package com.vegetablecorp.inventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.vegetablecorp.inventory.data.InventoryContract;
import com.vegetablecorp.inventory.data.InventoryContract.InventoryEntry;

public class InventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENTORY_LOADER = 0;
    private InventoryCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Setup FAB to open DetailActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InventoryActivity.this, DetailActivity.class);
                startActivity(intent);
            }
        });
        // Find the ListView which will be populated with the item data
        ListView listView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);

        // Create new LoaderManager to manage our loaders
        LoaderManager loaderManager = getLoaderManager();

        // Initialize the loader
        loaderManager.initLoader(INVENTORY_LOADER, null, this);

        // Find ListView to populate
        ListView lvItems = (ListView) findViewById(R.id.list);
        // Setup cursor adapter using cursor from last step
        mAdapter = new InventoryCursorAdapter(this, null);
        // Attach cursor adapter to the ListView
        lvItems.setAdapter(mAdapter);

        // Setup the item click listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link DetailActivity}
                Intent intent = new Intent(InventoryActivity.this, DetailActivity.class);

                // Form the content URI that represents the specific item that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link InventoryEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.vegetablecorp.inventory/items/2"
                // if the item with ID 2 was clicked on.
                Uri currentItemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentItemUri);

                // Launch the {@link DetailActivity} to display the data for the current item.
                startActivity(intent);
            }
        });
    }

    private void deleteAll() {
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_inventory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertDummyData();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAll();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri contentUri = InventoryEntry.CONTENT_URI;
        // Define Projection specifying the columns needed.
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_NAME,
                InventoryEntry.COLUMN_UNITS,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_IMAGE,
        };
        // Starts the query
        return new CursorLoader(this,
                contentUri,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }


    /**
     * Helper method to insert hardcoded item data into the database. For debugging purposes only.
     */
    private void insertDummyData() {
        // Create a ContentValues object where column names are the keys,
        // and item attributes are the values.

        ContentValues tshirt = new ContentValues();
        tshirt.put(InventoryContract.InventoryEntry.COLUMN_NAME, "T-Shirt");
        tshirt.put(InventoryEntry.COLUMN_EMAIL, "supplier@clothes.com");
        tshirt.put(InventoryEntry.COLUMN_PRICE, 20);
        tshirt.put(InventoryEntry.COLUMN_UNITS, 100);
        tshirt.put(InventoryEntry.COLUMN_IMAGE, "android.resource://com.vegetablecorp.inventory/drawable/a120046");
        getContentResolver().insert(InventoryEntry.CONTENT_URI, tshirt);

        ContentValues shorts = new ContentValues();
        shorts.put(InventoryContract.InventoryEntry.COLUMN_NAME, "Shorts");
        shorts.put(InventoryEntry.COLUMN_EMAIL, "supplier@clothes.com");
        shorts.put(InventoryEntry.COLUMN_PRICE, 20);
        shorts.put(InventoryEntry.COLUMN_UNITS, 100);
        shorts.put(InventoryEntry.COLUMN_IMAGE, "android.resource://com.vegetablecorp.inventory/drawable/a120041");
        getContentResolver().insert(InventoryEntry.CONTENT_URI, shorts);

        ContentValues underwear = new ContentValues();
        underwear.put(InventoryContract.InventoryEntry.COLUMN_NAME, "underwear");
        underwear.put(InventoryEntry.COLUMN_EMAIL, "supplier@clothes.com");
        underwear.put(InventoryEntry.COLUMN_PRICE, 10);
        underwear.put(InventoryEntry.COLUMN_UNITS, 100);
        underwear.put(InventoryEntry.COLUMN_IMAGE, "android.resource://com.vegetablecorp.inventory/drawable/a120050");
        getContentResolver().insert(InventoryEntry.CONTENT_URI, underwear);

        ContentValues socks = new ContentValues();
        socks.put(InventoryContract.InventoryEntry.COLUMN_NAME, "Socks");
        socks.put(InventoryEntry.COLUMN_EMAIL, "supplier@clothes.com");
        socks.put(InventoryEntry.COLUMN_PRICE, 10);
        socks.put(InventoryEntry.COLUMN_UNITS, 100);
        socks.put(InventoryEntry.COLUMN_IMAGE, "android.resource://com.vegetablecorp.inventory/drawable/a120039");
        getContentResolver().insert(InventoryEntry.CONTENT_URI, socks);

        ContentValues pants = new ContentValues();
        pants.put(InventoryContract.InventoryEntry.COLUMN_NAME, "Pants");
        pants.put(InventoryEntry.COLUMN_EMAIL, "supplier@clothes.com");
        pants.put(InventoryEntry.COLUMN_PRICE, 30);
        pants.put(InventoryEntry.COLUMN_UNITS, 100);
        pants.put(InventoryEntry.COLUMN_IMAGE, "android.resource://com.vegetablecorp.inventory/drawable/a120040");
        getContentResolver().insert(InventoryEntry.CONTENT_URI, pants);
    }
}