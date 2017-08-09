package com.vegetablecorp.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.vegetablecorp.inventory.data.InventoryContract.InventoryEntry;

/**
 * {@link ContentProvider} for Inventory app.
 */
public class InventoryProvider extends ContentProvider {

    /**
     * URI matcher code for the content URI for the items table
     */
    private static final int ITEMS = 100;

    /**
     * URI matcher code for the content URI for a single item in the items table
     */
    private static final int ITEM_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.vegetablecorp.inventory/items" will map to the
        // integer code {@link #ITEMS}. This URI is used to provide access to MULTIPLE rows
        // of the items table.
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_ITEMS, ITEMS);

        // The content URI of the form "content://com.vegetablecorp.inventory/items/#" will map to the
        // integer code {@link #ITEM_ID}. This URI is used to provide access to ONE single row
        // of the items table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.vegetablecorp.inventory/items/3" matches, but
        // "content://com.vegetablecorp.inventory/items" (without a number at the end) doesn't match.
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_ITEMS + "/#", ITEM_ID);
    }

    /**
     * Database helper object
     */
    private InventoryDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                // For the ITEMS code, query the items table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the items table.
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case ITEM_ID:
                // For the ITEM_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.vegetablecorp.inventory/items/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the items table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a item into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertItem(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Item requires a name");
        }

        // Check that the email is not null
        String email = values.getAsString(InventoryContract.InventoryEntry.COLUMN_EMAIL);
        if (email == null) {
            throw new IllegalArgumentException("Item requires a email");
        }

        // Check that the gender is valid
        Integer price = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRICE);
        if (price == null & price < 0) {
            throw new IllegalArgumentException("Item requires valid price");
        }

        // If the units is provided, check that it's greater than or equal to 0
        Integer units = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_UNITS);
        if (units != null && units < 0) {
            throw new IllegalArgumentException("Item requires valid units");
        }

        // No need to check the image, any value is valid (including null).

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new item with the given values
        long id = database.insert(InventoryContract.InventoryEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            return null;
        }

        // Notify all listners that the data has changed for the item content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return updateItem(uri, contentValues, selection, selectionArgs);
            case ITEM_ID:
                // For the ITEM_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }

    }

    /**
     * Update items in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more items).
     * Return the number of rows that were successfully updated.
     */
    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link InventoryEntry#COLUMN_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_NAME)) {
            String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Item requires a name");
            }
        }
        // If the {@link InventoryEntry#COLUMN_EMAIL} key is present,
        // check that the name value is not null.
        if (values.containsKey(InventoryEntry.COLUMN_EMAIL)) {
            String email = values.getAsString(InventoryContract.InventoryEntry.COLUMN_EMAIL);
            if (email == null) {
                throw new IllegalArgumentException("Item requires a email");
            }
        }

        // If the {@link InventoryEntry#COLUMN_PRICE} key is present,
        // check that the gender value is valid.
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRICE)) {
            Integer price = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRICE);
            if (price == null & price < 0) {
                throw new IllegalArgumentException("Item requires valid price");
            }
        }

        // If the {@link InventoryEntry#COLUMN_UNITS} key is present,
        // check that the units value is valid.
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_UNITS)) {
            // Check that the weight is greater than or equal to 0
            Integer units = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_UNITS);
            if (units != null && units < 0) {
                throw new IllegalArgumentException("Item requires valid units");
            }
        }

        // No need to check the breed, any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            // Notify all listners that the data has changed for the item content URI
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Returns the number of database rows affected by the update statement
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ITEM_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        if (rowsDeleted != 0) {
            // Notify all listners that the data has changed for the item content URI
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Returns the number of database rows affected by the update statement
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return InventoryContract.InventoryEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return InventoryContract.InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
