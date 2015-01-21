package org.coursera.camppotlatch.client.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.coursera.camppotlatch.R;
import org.coursera.camppotlatch.client.commons.AppContext;
import org.coursera.camppotlatch.client.commons.ImageUtils;
import org.coursera.camppotlatch.client.commons.UserUtils;
import org.coursera.camppotlatch.client.model.OperationResult;
import org.coursera.camppotlatch.client.model.User;
import org.coursera.camppotlatch.client.model.UserImageType;
import org.coursera.camppotlatch.client.serviceproxy.PotlatchServiceConn;
import org.coursera.camppotlatch.client.serviceproxy.UserServiceApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.client.Response;
import retrofit.mime.TypedFile;


public class EditUserAccountActivity extends Activity {
    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_CHOOSE_IMAGE = 2;

    public static final String USER_LOGIN_EXTRA = "userLogin";

    @InjectView(R.id.image_view)
    protected ImageView mImageView;

    @InjectView(R.id.user_name_edit_text)
    protected EditText mName;

    @InjectView(R.id.user_email_edit_text)
    protected EditText mEmail;

    @InjectView(R.id.user_city_edit_text)
    protected EditText mCity;

    @InjectView(R.id.user_country_edit_text)
    protected EditText mCountry;

    @InjectView(R.id.user_hide_inappropriate_checkbox)
    protected CheckBox mHideInappropriate;

    @InjectView(R.id.user_gifts_update_period_spinner)
    protected Spinner mUpdatePeriodSpinner;

    private String mUserLogin;
    private User mUser;

    private String mImageId;
    private String mImagePath;
    private boolean wasImageChanged;

    private Integer mGiftUpdatePeriod = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_account);

        this.setTitle("Edit User");

        ButterKnife.inject(this);

        mUserLogin = getIntent().getStringExtra(USER_LOGIN_EXTRA);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gifts_update_periods, android.R.layout.simple_spinner_item);
        mUpdatePeriodSpinner.setAdapter(adapter);

        mUpdatePeriodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectionStr = (String) adapterView.getItemAtPosition(i);

                String[] selectionParts = selectionStr.split(" ");
                mGiftUpdatePeriod = Integer.parseInt(selectionParts[0]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mGiftUpdatePeriod = 5;
            }
        });

        refreshView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void refreshView() {
        final UserServiceApi svc = PotlatchServiceConn.getUserServiceOrShowLogin(this);

        if (svc != null) {
            AsyncTask<Void, Void, Void>
                    refreshTask = new AsyncTask<Void, Void, Void>() {
                protected Exception error = null;

                @Override
                protected Void doInBackground(Void... voids) {
                    InputStream imageInputStream = null;

                    try {
                        // Get the user
                        mUser = svc.findByLogin(mUserLogin);
                        if (mUser == null)
                            throw new Exception("There is no user with login " + mUser);

                        String imageId = mUser.getImageId();
                        if (imageId == null) {
                            return null;
                            //throw new Exception("There is no image id in the user " + mUserLogin);
                        }

                        // If the image isn't already in the file system, download it
                        File imageFile = ImageUtils.getImageFile(imageId);
                        if (!imageFile.exists()) {
                            imageFile = ImageUtils.createImageFile(imageId);

                            Response response =
                                    svc.getUserImage(mUser.getLogin(), UserImageType.NORMAL);
                            imageInputStream = response.getBody().in();
                            FileUtils.copyInputStreamToFile(imageInputStream, imageFile);
                        }

                        mUser.setImagePath(imageFile.getAbsolutePath());
                    } catch (Exception ex) {
                        error = ex;
                        return null;
                    } finally {
                        if (imageInputStream != null)
                            try {
                                imageInputStream.close();
                            } catch (Exception ex) {
                                error = ex;
                                return null;
                            }
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(Void voids) {
                    super.onPostExecute(null);

                    if (error != null) {
                        Toast.makeText(
                                EditUserAccountActivity.this,
                                "Unable to fetch the user image: " + error.toString(),
                                Toast.LENGTH_SHORT).show();

                        return;
                    }

                    Bitmap bitmap = UserUtils.loadImageBitmap(mUser);
                    mImageView.setImageBitmap(bitmap);

                    mName.setText(mUser.getName());
                    mEmail.setText(mUser.getEmail());
                    mCity.setText(mUser.getCity());
                    mCountry.setText(mUser.getCountry());

                    mHideInappropriate.setChecked(mUser.getDisableInappropriate());

                    String[] updateTimes =
                            getResources().getStringArray(R.array.gifts_update_periods);

                    mHideInappropriate.setChecked(mUser.getDisableInappropriate());

                    mGiftUpdatePeriod = mUser.getGiftsReloadPeriod();

                    // Set the update period
                    int pos = 0;
                    int selPos = 0;
                    for (String updateTime : updateTimes) {
                        String[] updateTimePart = updateTime.split(" ");
                        int curPeriod = Integer.parseInt(updateTimePart[0]);
                        if (mGiftUpdatePeriod == curPeriod)
                            selPos = pos;
                        pos++;
                    }
                    mUpdatePeriodSpinner.setSelection(selPos);
                }
            };

            refreshTask.execute();
        }
    }

    @OnClick(R.id.take_photo_button)
    protected void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(EditUserAccountActivity.this,
                    "No camera available", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        // Try to create a file to store image
        File imageFile = null;
        try {
            mImageId = ImageUtils.generateImageId();
            imageFile = ImageUtils.createImageFile(mImageId);
            mImagePath = imageFile.getAbsolutePath();
        } catch (IOException ex) {
            // Error occurred while creating the File
            Log.e(EditUserAccountActivity.class.getName(),
                    "Exception on creating image file: " + ex.toString());
            Toast.makeText(EditUserAccountActivity.this,
                    "Exception on creating image file: " + ex.toString(), Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        // Call the camera activity
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(imageFile));
        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
    }

    @OnClick(R.id.choose_image_button)
    protected void chooseImage() {
        Intent chooseImageIntent = new Intent(Intent.ACTION_GET_CONTENT);
        chooseImageIntent.setType(ImageUtils.JPEG_MIME_TYPE);
        chooseImageIntent.addCategory(Intent.CATEGORY_OPENABLE);

        if (chooseImageIntent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(EditUserAccountActivity.this,
                    "No image chooser available", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        startActivityForResult(chooseImageIntent, REQUEST_CHOOSE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                Bitmap imageBitmap = ImageUtils.getBitmap(
                        mImagePath, mImageView.getWidth(), mImageView.getHeight(), true);

                // Save the new bitmap to the file
                try {
                    ImageUtils.saveBitmap(mImagePath, imageBitmap);
                } catch (FileNotFoundException ex) {
                    Log.e(CreateUserAccountActivity.class.getName(),
                            "Error saving image bitmap: " + ex.toString());
                }

                mImageView.setImageBitmap(imageBitmap);
                wasImageChanged = true;
            }
        } else if (requestCode == REQUEST_CHOOSE_IMAGE) {
            if (resultCode == RESULT_OK) {
                Uri fullPhotoUri = data.getData();
                ContentResolver resolver = getContentResolver();
                ParcelFileDescriptor sourcePhotoFileDesc = null;
                File targetImageFile = null;
                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                    sourcePhotoFileDesc = resolver.openFileDescriptor(fullPhotoUri, "r");
                    inputStream =
                            new FileInputStream(sourcePhotoFileDesc.getFileDescriptor());

                    mImageId = ImageUtils.generateImageId();
                    targetImageFile = ImageUtils.createImageFile(mImageId);
                    outputStream = new FileOutputStream(targetImageFile);
                    if (IOUtils.copy(inputStream, outputStream) <= 0) {
                        Toast.makeText(EditUserAccountActivity.this,
                                "No photo obtained: " + fullPhotoUri.toString(), Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }

                    mImagePath = targetImageFile.getAbsolutePath();
                    Bitmap imageBitmap = ImageUtils.getBitmap(
                            mImagePath, mImageView.getWidth(), mImageView.getHeight(), true);

                    // Save the new bitmap to the file
                    try {
                        ImageUtils.saveBitmap(mImagePath, imageBitmap);
                    } catch (FileNotFoundException ex) {
                        Log.e(CreateUserAccountActivity.class.getName(),
                                "Error saving image bitmap: " + ex.toString());
                    }

                    mImageView.setImageBitmap(imageBitmap);
                    wasImageChanged = true;
                } catch (FileNotFoundException ex) {
                    Toast.makeText(EditUserAccountActivity.this,
                            "Uri not found: " + fullPhotoUri.toString(), Toast.LENGTH_SHORT)
                            .show();
                    return;
                } catch (IOException ex) {
                    Toast.makeText(EditUserAccountActivity.this,
                            "Problems obtaining the photo: " + ex.toString(), Toast.LENGTH_SHORT)
                            .show();
                    return;
                } finally {
                    try {
                        if (sourcePhotoFileDesc != null)
                            sourcePhotoFileDesc.close();
                        if (inputStream != null)
                            inputStream.close();
                        if (outputStream != null)
                            outputStream.close();
                    } catch (IOException ex) {
                        Toast.makeText(EditUserAccountActivity.this,
                                "Problems obtaining the photo: "
                                        + ex.toString(), Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                }
            }
        }
    }

    @OnClick(R.id.user_save_button)
    protected void saveUser() {
        String name = mName.getText().toString();
        if (name.equals("")) {
            Toast.makeText(EditUserAccountActivity.this, "Insert your name", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        String email = mEmail.getText().toString();
        if (email.equals("")) {
            Toast.makeText(EditUserAccountActivity.this, "Insert your e-mail", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        String city = mCity.getText().toString();
        if (city.equals("")) {
            Toast.makeText(EditUserAccountActivity.this, "Insert your city", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        String country = mCountry.getText().toString();
        if (country.equals("")) {
            Toast.makeText(EditUserAccountActivity.this, "Insert your country", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        Boolean disableInappropriate = mHideInappropriate.isChecked();

        Integer giftUpdatePeriod = mGiftUpdatePeriod;

        /*
        if (!hasImage) {
            Toast.makeText(EditUserAccountActivity.this,
                    "Take a photo or choose an image", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        */

        mUser.setName(name);
        mUser.setEmail(email);
        mUser.setCity(city);
        mUser.setCountry(country);
        mUser.setDisableInappropriate(disableInappropriate);
        mUser.setGiftsReloadPeriod(giftUpdatePeriod);

        if (wasImageChanged) {
            mUser.setImageId(mImageId);
            mUser.setImageContentType(ImageUtils.JPEG_MIME_TYPE);
            mUser.setImagePath(mImagePath);
        }

        final UserServiceApi userService = PotlatchServiceConn.getUserServiceOrShowLogin(this);
        AsyncTask<User, Void, User> task = new AsyncTask<User, Void, User>() {
            protected Exception error = null;

            @Override
            protected User doInBackground(User... users) {
                User user = users[0];
                try {
                    OperationResult result = userService.updateUser(user.getLogin(), user);

                    if (wasImageChanged) {
                        String imagePath = user.getImagePath();
                        File imageFile = new File(imagePath);
                        if (!imageFile.exists())
                            throw new Exception("There is no " + imagePath + " directory");

                        TypedFile typedImageFile = new TypedFile(user.getImageContentType(), imageFile);
                        result = userService.postUserImage(user.getLogin(), typedImageFile);
                    }
                } catch (Exception e) {
                    error = e;
                    return null;
                }

                return user;
            }

            @Override
            protected void onPostExecute(User user) {
                super.onPostExecute(user);

                if (error != null) {
                    Toast.makeText(EditUserAccountActivity.this,
                            "Error saving user: " + error.getMessage(), Toast.LENGTH_SHORT)
                            .show();

                    return;
                }

                // Update the user account for the application's current user
                if (AppContext.getUser().getLogin().equals(mUser.getLogin())) {
                    AppContext.setUser(mUser);
                }

                Toast.makeText(EditUserAccountActivity.this, "User saved", Toast.LENGTH_SHORT);
                finish();
            }
        };

        task.execute(mUser);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_user_account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_remove_gift:
                secureRemoveUser();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void secureRemoveUser() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        dialogBuilder.setMessage("Remove this user?");

        dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                removeUser();
            }
        });

        dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        dialogBuilder.create().show();
    }

    private void removeUser() {
        final UserServiceApi userService = PotlatchServiceConn.getUserServiceOrShowLogin(this);
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            protected Exception error = null;

            @Override
            protected Void doInBackground(Void... voids) {

                try {
                    OperationResult result = userService.removeUser(mUserLogin);
                } catch (Exception e) {
                    error = e;
                    return null;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void voids) {
                super.onPostExecute(voids);

                if (error != null) {
                    Toast.makeText(EditUserAccountActivity.this,
                            "Error removing user: " + error.getMessage(), Toast.LENGTH_SHORT)
                            .show();

                    return;
                }

                Toast.makeText(EditUserAccountActivity.this, "User removed", Toast.LENGTH_SHORT);
                finish();
            }
        };

        task.execute();
    }
}
