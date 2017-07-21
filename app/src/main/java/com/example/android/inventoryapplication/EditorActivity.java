package com.example.android.inventoryapplication;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapplication.data.ProductContract;

/**
 * Created by MariamNKinene on 16/07/2017.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = EditorActivity.class.getSimpleName();

    private static final int EXISTING_ITEM_LOADER = 0;
    private static final int IMAGE_REQUEST_CODE = 0;
    private static final String STATE_IMAGE_URI = "STATE_IMAGE_URI";

    final Context mContext = this;

    //Content URI for existing product (and if null it's a new product)
    private Uri mCurrentProductUri;


    // Edit TextVIEWS
    private EditText mEditTextProductName;
    private EditText mEditTextProductPrice;
    private EditText mEditQuantityView;
    private EditText mEditTextSupplierName;
    private EditText mEditTextSupplierEmail;

    // Buttons
    private Button mPlusButton;
    private Button mMinusButton;
    private Button mOrderButton;
    private Button mButtonAddImage;

    private ImageView mImageProduct;
    private Uri mImageUri;

    private String productName;
    private int productPrice;
    private int productQuantity;
    private String supplierName;
    private String supplierEmail;
    private String imagePath;

    // Boolean flag that keeps track of whether the product has been edited (true) or not (false)
    private boolean mProductHasChanged = false;

    // OnTouchListener that listens for any user touches on a View, implying that they are modifying view
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);


        // Examine the intent that was used to launch this activity,
        // in order to figure out if adding a new product or editing an existing one.
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // Check if CurrentProductUri is null (for new product) or not (for existing product)
        // Set action bar title accordingly
        // Also, for existing product, initialize a loader to fetch data from the database and
        // display the current values in the editor
        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_product));
            invalidateOptionsMenu();
            hideOrderButton();
            setQuantity();

        } else {
            setTitle(getString(R.string.editor_activity_title_edit_product));
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        // Find the data views
        initializeUIElements();

        //Plus and minus buttons
        mPlusButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int quantity = Integer.parseInt(mEditQuantityView.getText().toString().trim());
                quantity++;
                mEditQuantityView.setText(Integer.toString(quantity));
            }
        });

        mMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.parseInt(mEditQuantityView.getText().toString().trim());
                if (quantity == 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.detail_quantity_negative),
                            Toast.LENGTH_SHORT).show();
                } else {
                    quantity = quantity - 1;
                    mEditQuantityView.setText(Integer.toString(quantity));
                }
            }
        });
        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get text for product name and price to put in email
                String nameEmail = mEditTextProductName.getText().toString().trim();

                //Create email message
                String message = getString(R.string.email_message) +
                        "\n" + nameEmail;

                //Send intent
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_SUBJECT,
                        getString(R.string.email_subject));
                intent.putExtra(Intent.EXTRA_TEXT, message);

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
        //Open camera when you press on image
        mButtonAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                new Intent();

                if (Build.VERSION.SDK_INT < 19) {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                } else {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                }
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, getString(R.string.action_select_picture)), IMAGE_REQUEST_CODE);
            }
        });

    }

    private void setQuantity() {
        mEditQuantityView = (EditText) findViewById(R.id.detail_quantity_view_edit);
        String quantityText = "0";
        mEditQuantityView.setText(quantityText);
    }


    //This method initializes all UI components used in the Activity
    public void initializeUIElements() {
        // Find the data views
        mEditTextProductName = (EditText) findViewById(R.id.text_product_edit);
        mEditTextProductPrice = (EditText) findViewById(R.id.text_price_edit);
        mEditQuantityView = (EditText) findViewById(R.id.detail_quantity_view_edit);
        mEditTextSupplierName = (EditText) findViewById(R.id.text_supplier_email_edit);
        mEditTextSupplierEmail = (EditText) findViewById(R.id.text_supplier_name_edit);
        mImageProduct = (ImageView) findViewById(R.id.image_product);


        //Identify all the button views
        mPlusButton = (Button) findViewById(R.id.detail_plus_button);
        mMinusButton = (Button) findViewById(R.id.detail_minus_button);
        mOrderButton = (Button) findViewById(R.id.button_order_more);
        mButtonAddImage = (Button) findViewById(R.id.add_image_button);


        // Setup OnTouchListeners
        mEditTextProductName.setOnTouchListener(mTouchListener);
        mEditTextProductPrice.setOnTouchListener(mTouchListener);
        mEditQuantityView.setOnTouchListener(mTouchListener);
        mEditTextSupplierName.setOnTouchListener(mTouchListener);
        mEditTextSupplierEmail.setOnTouchListener(mTouchListener);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mImageUri != null) {
            outState.putString(STATE_IMAGE_URI, mImageUri.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(STATE_IMAGE_URI) &&
                !savedInstanceState.getString(STATE_IMAGE_URI).equals("")) {
            mImageUri = Uri.parse(savedInstanceState.getString(STATE_IMAGE_URI));

            ViewTreeObserver viewTreeObserver = mImageProduct.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onGlobalLayout() {
                    mImageProduct.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mImageProduct.setImageBitmap(Utils.getBitmapFromUri(mImageUri, mContext, mImageProduct));
                }
            });
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST_CODE && (resultCode == RESULT_OK)) {
            try {
                mImageUri = data.getData();
                int takeFlags = data.getFlags();
                takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                try {
                    getContentResolver().takePersistableUriPermission(mImageUri, takeFlags);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }

                mImageProduct.setImageBitmap(Utils.getBitmapFromUri(mImageUri, mContext, mImageProduct));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Method to add a new product to database
     */
    private void saveProduct() {
        String nameString = mEditTextProductName.getText().toString().trim();
        String priceString = mEditTextProductPrice.getText().toString().trim();
        String quantityString = mEditQuantityView.getText().toString().trim();
        String suppliernameString = mEditTextSupplierName.getText().toString().trim();
        String supplierEmailString = mEditTextSupplierEmail.getText().toString().trim();


        // Check if this is supposed to be a new product
        // and check if all the fields in the editor are blank
        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(suppliernameString) ||
                TextUtils.isEmpty(supplierEmailString)) {

            Toast.makeText(this, "Please, insert all required information.",
                    Toast.LENGTH_SHORT).show();


            //Since nothing was edited no need to do anything
            return;
        }


        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();

        //if there is no price
        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, price);


        //if there's no name
        String name = "no name";
        if (!TextUtils.isEmpty(nameString)) {
            name = nameString;
        }
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, name);

        //if there is no quantity
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);

        //if there's no supplier name
        String suppliername = "no supplier name";
        if (!TextUtils.isEmpty(suppliernameString)) {
            suppliername = suppliernameString;
        }
        values.put(ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME, suppliername);


        //if there's no supplier email
        String supplieremail = "no supplier email";
        if (!TextUtils.isEmpty(supplierEmailString)) {
            supplieremail = supplierEmailString;
        }
        values.put(ProductContract.ProductEntry.COLUMN_SUPPLIER_EMAIL, supplieremail);

        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE, imagePath);

        mCurrentProductUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);

        // Determine if this is a new or existing product by checking if mCurrentProductUri is null or not
        if (mCurrentProductUri == null) {
            // This is a NEW product, so insert a new product into the provider,
            // returning the content URI for the new product.
            Uri newUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.new_product_fail_message),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.new_product_success_message),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            //Otherwise if an existing lipstick update with content URI
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.update_product_fail_message),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.update_product_success_message),
                        Toast.LENGTH_SHORT).show();
            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save product to database
                saveProduct();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
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
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the product table
        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductContract.ProductEntry.COLUMN_SUPPLIER_EMAIL,
                ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,     // Query the content URI for the current product
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
            // Find the columns of product attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int suppliernameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_SUPPLIER_NAME);
            int supplieremailColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_SUPPLIER_EMAIL);
            int imageColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String suppliername = cursor.getString(suppliernameColumnIndex);
            String supplieremail = cursor.getString(supplieremailColumnIndex);
            String image = cursor.getString(imageColumnIndex);


            // Update the views on the screen with the values from the database
            mEditTextProductName.setText(name);
            mEditTextProductPrice.setText(String.valueOf(price));
            mEditQuantityView.setText(String.valueOf(quantity));
            mEditTextSupplierName.setText(suppliername);
            mEditTextSupplierEmail.setText(supplieremail);
            mImageProduct.setImageBitmap(Utils.getBitmapFromUri(Uri.parse(image), mContext, mImageProduct));
            mImageUri = Uri.parse(image);


        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mEditTextProductName.setText("");
        mEditTextProductPrice.setText("");
        mEditQuantityView.setText("");
        mEditTextSupplierName.setText("");
        mEditTextSupplierEmail.setText("");
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
                // and continue editing the pet.
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
     * Prompt the user to confirm that they want to delete this product.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
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
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the product that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    private void hideOrderButton() {
        Button OrderButton = (Button) findViewById(R.id.button_order_more);
        OrderButton.setVisibility(View.GONE);
    }
}