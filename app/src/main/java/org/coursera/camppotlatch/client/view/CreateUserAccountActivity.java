package org.coursera.camppotlatch.client.view;

import android.app.Activity;
import android.content.ContentResolver;
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

import org.apache.commons.io.IOUtils;
import org.coursera.camppotlatch.R;
import org.coursera.camppotlatch.client.commons.DateUtils;
import org.coursera.camppotlatch.client.commons.ImageUtils;
import org.coursera.camppotlatch.client.model.OperationResult;
import org.coursera.camppotlatch.client.model.User;
import org.coursera.camppotlatch.client.serviceproxy.PotlatchServiceConn;
import org.coursera.camppotlatch.client.serviceproxy.UserServiceApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.mime.TypedFile;

public class CreateUserAccountActivity extends Activity {
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_CHOOSE_IMAGE = 2;

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

    @InjectView(R.id.user_login_edit_text)
    protected EditText mLogin;

    @InjectView(R.id.user_password_edit_text)
    protected EditText mPassword;

    @InjectView(R.id.user_password_rep_edit_text)
    protected EditText mPasswordRep;

    @InjectView(R.id.user_hide_inappropriate_checkbox)
    protected CheckBox mHideInappropriate;

    @InjectView(R.id.user_gifts_update_period_spinner)
    protected Spinner mUpdatePeriodSpinner;

    private String mImageId;
    private String mImagePath;
    private boolean hasImage;

    private Integer mGiftUpdatePeriod = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user_account);

        this.setTitle("New User");

        ButterKnife.inject(this);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gifts_update_periods, android.R.layout.simple_spinner_item);
        mUpdatePeriodSpinner.setAdapter(adapter);

        mUpdatePeriodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectionStr = (String)adapterView.getItemAtPosition(i);

                String[] selectionParts = selectionStr.split(" ");
                mGiftUpdatePeriod = Integer.parseInt(selectionParts[0]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mGiftUpdatePeriod = 5;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create_user_account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.take_photo_button)
    protected void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(CreateUserAccountActivity.this,
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
            Log.e(CreateUserAccountActivity.class.getName(),
                    "Exception on creating image file: " + ex.toString());
            Toast.makeText(CreateUserAccountActivity.this,
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
            Toast.makeText(CreateUserAccountActivity.this,
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
                //Bundle extras = data.getExtras();
                //Bitmap imageBitmap = (Bitmap) extras.get("data");
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
                hasImage = true;
            }
        } else if (requestCode == REQUEST_CHOOSE_IMAGE) {
            if (resultCode == RESULT_OK) {
                //Bundle extras = data.getExtras();
                //Bitmap imageBitmap = (Bitmap) extras.get("data");
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
                        Toast.makeText(CreateUserAccountActivity.this,
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
                    hasImage = true;
                } catch (FileNotFoundException ex) {
                    Toast.makeText(CreateUserAccountActivity.this,
                            "Uri not found: " + fullPhotoUri.toString(), Toast.LENGTH_SHORT)
                            .show();
                    return;
                } catch (IOException ex) {
                    Toast.makeText(CreateUserAccountActivity.this,
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
                        Toast.makeText(CreateUserAccountActivity.this,
                                "Problems obtaining the photo: "
                                        + ex.toString(), Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                }
            }
        }
    }

    @OnClick(R.id.user_create_button)
    protected void createUser() {
        DateUtils dateUtils = new DateUtils();

        String name = mName.getText().toString();
        if (name.equals("")) {
            Toast.makeText(CreateUserAccountActivity.this, "Insert your name", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        String email = mEmail.getText().toString();
        if (email.equals("")) {
            Toast.makeText(CreateUserAccountActivity.this, "Insert your e-mail", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        String city = mCity.getText().toString();
        if (city.equals("")) {
            Toast.makeText(CreateUserAccountActivity.this, "Insert your city", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        String country = mCountry.getText().toString();
        if (country.equals("")) {
            Toast.makeText(CreateUserAccountActivity.this, "Insert your country", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        String login = mLogin.getText().toString();
        if (login.equals("")) {
            Toast.makeText(CreateUserAccountActivity.this, "Insert your login", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        String password = mPassword.getText().toString();
        if (password.equals("")) {
            Toast.makeText(CreateUserAccountActivity.this, "Insert your password", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        String passwordRep = mPasswordRep.getText().toString();
        if (passwordRep.equals("")) {
            Toast.makeText(CreateUserAccountActivity.this,
                    "Insert your password two times", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        if (!passwordRep.equals(password)) {
            Toast.makeText(CreateUserAccountActivity.this,
                    "The retyped password is different from the typed password", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        Boolean disableInappropriate = mHideInappropriate.isChecked();

        Integer giftUpdatePeriod = mGiftUpdatePeriod;

        Date createDate = null;
        try {
            createDate = dateUtils.convertToISO8601Date(new Date());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (!hasImage) {
            Toast.makeText(CreateUserAccountActivity.this,
                    "Take a photo or choose an image", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        User user = new User(login, password, User.DEFAULT_ROLES,
                name, email, mImageId, city, country, createDate,
                disableInappropriate, giftUpdatePeriod);

        user.setImageContentType(ImageUtils.JPEG_MIME_TYPE);
        user.setImagePath(mImagePath);

        PotlatchServiceConn.init("admin", "cicerre70");
        final UserServiceApi userService = PotlatchServiceConn.getUserServiceOrShowLogin(this);
        AsyncTask<User, Void, User> task = new AsyncTask<User, Void, User>() {
            protected Exception error = null;

            @Override
            protected User doInBackground(User... users) {
                User newUser = null;
                try {
                    User user = users[0];
                    newUser = userService.addUser(user);

                    String imagePath = user.getImagePath();
                    File imageFile = new File(imagePath);
                    if (!imageFile.exists())
                        throw new Exception("There is no " + imagePath + " directory");

                    TypedFile typedImageFile = new TypedFile(user.getImageContentType(), imageFile);
                    OperationResult result = userService.postUserImage(newUser.getLogin(), typedImageFile);
                } catch (Exception e) {
                    error = e;
                    return null;
                }

                return newUser;
            }

            @Override
            protected void onPostExecute(User user) {
                super.onPostExecute(user);

                if (error != null) {
                    Toast.makeText(CreateUserAccountActivity.this,
                            "Error creating user: " + error.getMessage(), Toast.LENGTH_SHORT)
                            .show();

                    return;
                }

                Toast.makeText(CreateUserAccountActivity.this, "User created", Toast.LENGTH_SHORT);
                finish();
            }
        };

        task.execute(user);
    }
}
