package com.vegetablecorp.inventory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vegetablecorp.inventory.data.InventoryContract;

/**
 * {@link InventoryCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of pet data as its data source. This adapter knows
 * how to create list items for each row of pet data in the {@link Cursor}.
 */
public class InventoryCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    static class ViewHolder {
        private TextView name;
        private TextView units;
        private TextView price;
        private ImageView image;
        private ImageView sale;
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        // Find fields to populate in inflated template
        ViewHolder holder;
        holder = new ViewHolder();
        holder.name = view.findViewById(R.id.name);
        holder.units = view.findViewById(R.id.units);
        holder.price = view.findViewById(R.id.price);
        holder.image = view.findViewById(R.id.image);
        holder.sale = view.findViewById(R.id.sale);

        // Find the colums of pet attributes we want
        int idColumnIndex = cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry._ID);
        int nameColumnIndex = cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_NAME);
        int unitsColumnIndex = cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_UNITS);
        int priceColumnIndex = cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_PRICE);
        int imageColumnIndex = cursor.getColumnIndexOrThrow(InventoryContract.InventoryEntry.COLUMN_IMAGE);

        // Extract properties from cursor
        final String itemId = cursor.getString(idColumnIndex);
        String itemName = cursor.getString(nameColumnIndex);
        final int itemUnits = cursor.getInt(unitsColumnIndex);
        String itemPrice = cursor.getString(priceColumnIndex);
        String itemImage = cursor.getString(imageColumnIndex);

        // Populate fields with extracted properties
        holder.name.setText(itemName);
        holder.units.setText("x " + itemUnits);
        holder.price.setText("$ " + itemPrice);
        if (itemImage != null) {
            holder.image.setImageURI(Uri.parse(itemImage));
        }

        holder.sale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemUnits > 0){
                    int amt = itemUnits - 1;
                    // update cursor value
                    ContentValues values = new ContentValues();
                    values.put(InventoryContract.InventoryEntry.COLUMN_UNITS, amt);
                    Uri uri = Uri.withAppendedPath(InventoryContract.InventoryEntry.CONTENT_URI, itemId);
                    context.getContentResolver().update(uri, values, null, null);
                }return;
            }
        });
    }
}