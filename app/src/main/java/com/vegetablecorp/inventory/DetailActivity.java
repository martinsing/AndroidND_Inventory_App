package com.vegetablecorp.inventory;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.vegetablecorp.inventory.data.InventoryContract.InventoryEntry;

/**
 * Allows user to create a new item or edit an existing one.
 */
public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    /**
     * Identifier for the item data loader
     */
    private static final int EXISTING_PET_LOADER = 0;
    private static final int PICK_IMAGE_REQUEST = 0;

    /**
     * Content URI for the existing item (null if it's a new item)
     */
    private Uri mCurrentItemUri;
    private EditText mNameEditText;
    private EditText mEmailEditText;
    private EditText mUnitsEditText;
    private EditText mPriceEditText;
    private ImageView mImage;
    private Uri mImageUri;
    private FloatingActionButton fab;

    /**
     * Boolean flag that keeps track of whether the item has been edited (true) or not (false)
     */
    private boolean mItemHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mItemHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Final all relevant buttons that require actions
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        ImageView increase = (ImageView) findViewById(R.id.increase);
        increase.setOnClickListener(this);
        ImageView decrease = (ImageView) findViewById(R.id.decrease);
        decrease.setOnClickListener(this);
        ImageView delete = (ImageView) findViewById(R.id.delete);
        delete.setOnClickListener(this);
        Button reorder = (Button) findViewById(R.id.reorder);
        reorder.setOnClickListener(this);
        ImageView save = (ImageView) findViewById(R.id.save);
        save.setOnClickListener(this);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new item or editing an existing one.
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        // If the intent DOES NOT contain an item content URI, then we know that we are
        // creating a new item.
        if (mCurrentItemUri == null) {
            // This is a new item, so change the app bar to say "Add a Item"
            setTitle(getString(R.string.editor_activity_title_new_item));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete an item that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing item, so change app bar to say "Edit Item"
            setTitle(getString(R.string.editor_activity_title_edit_item));

            // Initialize a loader to read the item data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mEmailEditText = (EditText) findViewById(R.id.edit_item_email);
        mUnitsEditText = (EditText) findViewById(R.id.edit_item_units);
        mPriceEditText = (EditText) findViewById(R.id.edit_item_price);
        mImage = (ImageView) findViewById(R.id.image);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mEmailEditText.setOnTouchListener(mTouchListener);
        mUnitsEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        increase.setOnTouchListener(mTouchListener);
        decrease.setOnTouchListener(mTouchListener);
        fab.setOnTouchListener(mTouchListener);
    }

    /**
     * Check to make sure that non of the edittext are empty
     */
    public int check() {
        boolean allAttributesPresent = true;
        String nameString = mNameEditText.getText().toString().trim();
        String emailString = mEmailEditText.getText().toString().trim();
        String unitsString = mUnitsEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();

        if (TextUtils.isEmpty(nameString)) {
            mNameEditText.setError(getString(R.string.missing));
            allAttributesPresent = false;
        }
        if (TextUtils.isEmpty(emailString)) {
            mEmailEditText.setError(getString(R.string.missing));
            allAttributesPresent = false;
        }
        if (TextUtils.isEmpty(unitsString)) {
            mUnitsEditText.setError(getString(R.string.missing));
            allAttributesPresent = false;
        }
        if (TextUtils.isEmpty(priceString)) {
            mPriceEditText.setError(getString(R.string.missing));
            allAttributesPresent = false;
        }

        if (mImageUri == null){
            Snackbar.make(findViewById(R.id.layout), R.string.missing_image, Snackbar.LENGTH_LONG).show();
            allAttributesPresent = false;
        }
        if (allAttributesPresent == false){
            return 0;
        }
        return 1;

    }

    public int isnull(){
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String emailString = mEmailEditText.getText().toString().trim();
        String unitsString = mUnitsEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();

        // Check if this is supposed to be a new item
        // and check if all the fields in the editor are blank
        if (mCurrentItemUri == null &&
                TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(emailString) &&
                TextUtils.isEmpty(unitsString) &&
                TextUtils.isEmpty(priceString)&&
                TextUtils.isEmpty(String.valueOf(mImageUri))) {
            // Since no fields were modified, we can return early without creating a new item.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return 0;
        }
        return 1;
    }

    /**
     * Get user input from editor and save item into database.
     */
    private void save() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String emailString = mEmailEditText.getText().toString().trim();
        String unitsString = mUnitsEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();

        // Create a ContentValues object where column names are the keys,
        // and item attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_NAME, nameString);
        values.put(InventoryEntry.COLUMN_EMAIL, emailString);
        values.put(InventoryEntry.COLUMN_UNITS, unitsString);
        values.put(InventoryEntry.COLUMN_PRICE, priceString);
        values.put(InventoryEntry.COLUMN_IMAGE, String.valueOf(mImageUri));

        // Determine if this is a new or existing item by checking if mCurrentItemUri is null or not
        if (mCurrentItemUri == null) {
            // This is a NEW item, so insert a new item into the provider,
            // returning the content URI for the new item.
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            // Show a snackbar message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Snackbar.make(findViewById(R.id.layout), getString(R.string.editor_insert_item_failed),
                        Snackbar.LENGTH_LONG).show();
            } else {
                // Otherwise, the insertion was successful and we can display a snackbar.
                Snackbar.make(findViewById(R.id.layout), getString(R.string.editor_insert_item_successful),
                        Snackbar.LENGTH_LONG).show();
            }
        } else {
            // Otherwise this is an EXISTING item, so update the item with content URI: mCurrentItemUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentItemUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

            // Show a snackbar message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Snackbar.make(findViewById(R.id.layout), getString(R.string.editor_update_item_failed),
                        Snackbar.LENGTH_LONG).show();
            } else {
                // Otherwise, the update was successful and we can display a snackbar.
                Snackbar.make(findViewById(R.id.layout), getString(R.string.editor_update_item_successful),
                        Snackbar.LENGTH_LONG).show();
            }
        }
        finish();
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all item attributes, define a projection that contains
        // all columns from the item table
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_NAME,
                InventoryEntry.COLUMN_EMAIL,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_UNITS,
                InventoryEntry.COLUMN_IMAGE
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentItemUri,         // Query the content URI for the current item
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of item attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_NAME);
            int emailColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_EMAIL);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
            int unitsColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_UNITS);
            int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String email = cursor.getString(emailColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int units = cursor.getInt(unitsColumnIndex);
            String image = cursor.getString(imageColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mEmailEditText.setText(email);
            mPriceEditText.setText(Integer.toString(price));
            mUnitsEditText.setText(Integer.toString(units));
            if (image != null) {
                mImageUri = Uri.parse(image);
                mImage.setImageURI(mImageUri);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mEmailEditText.setText("");
        mUnitsEditText.setText("");
        mPriceEditText.setText("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                delete();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the item in the database.
     */
    private void delete() {
        if (mCurrentItemUri != null) {
            // Otherwise this is an EXISTING item, so update the item with content URI: mCurrentItemUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentItemUri will already identify the correct row in the database that
            // we want to delete.
            int rowsAffected = getContentResolver().delete(mCurrentItemUri, null, null);

            // Show a snackbar message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Snackbar.make(findViewById(R.id.layout), getString(R.string.editor_delete_item_failed),
                        Snackbar.LENGTH_LONG).show();
            } else {
                // Otherwise, the update was successful and we can display a snackbar.
                Snackbar.make(findViewById(R.id.layout), getString(R.string.editor_delete_item_successful),
                        Snackbar.LENGTH_LONG).show();
            }
        }
        finish();
    }

    /**
     * Select an image
     */
    public void imageSelect() {
        permissionsCheck();
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    public void permissionsCheck() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PICK_IMAGE_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which item was selected.
                if (data != null) {
                    mImageUri = data.getData();
                    mImage.setImageURI(mImageUri);
                    mImage.invalidate();
                }
            }
        }
    }

    /**
     * Email Reorder
     */
    public void reorder() {
        Intent reorder = new Intent(Intent.ACTION_SENDTO);
        reorder.setType("text/html");
        reorder.setData(Uri.parse("mailto:" + mEmailEditText.getText().toString()));
        reorder.putExtra(Intent.EXTRA_SUBJECT, "Reordering: " + mNameEditText.getText().toString().trim());
        reorder.putExtra(Intent.EXTRA_TEXT, "Requesting reorder of " + mNameEditText.getText().toString().trim());
        startActivity(Intent.createChooser(reorder, "Send Email"));
    }

    public void decrease(){
        int quantity = Integer.parseInt(mUnitsEditText.getText().toString());
        if (quantity > 0){
            quantity -- ;
            mUnitsEditText.setText(String.valueOf(quantity));
        }return;
    }
    public void increase(){
        int quantity = Integer.parseInt(mUnitsEditText.getText().toString());
            quantity ++ ;
            mUnitsEditText.setText(String.valueOf(quantity));
}

    // to handle the buttons
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                // get image
                imageSelect();
                break;
            case R.id.increase:
                // increase by 1
                increase();
                break;
            case R.id.decrease:
                // decrease by 1
                decrease();
                break;
            case R.id.delete:
                // delete current item
                showDeleteConfirmationDialog();
                break;
            case R.id.reorder:
                // reorder from supplier
                reorder();
                break;
            case R.id.save:
                // save current item
                if (!mItemHasChanged) {
                    onBackPressed();
                } else {
                    if(isnull() != 1){
                        return;
                    }
                    if(check() != 1){
                        return;
                    }
                    save();
                }
                break;
            default:
                break;
        }
    }
}


