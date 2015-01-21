package org.coursera.camppotlatch.client.view;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import org.coursera.camppotlatch.R;
import org.coursera.camppotlatch.client.commons.DateUtils;
import org.coursera.camppotlatch.client.model.OperationResult;
import org.coursera.camppotlatch.client.model.User;
import org.coursera.camppotlatch.client.serviceproxy.PotlatchServiceConn;
import org.coursera.camppotlatch.client.serviceproxy.UserServiceApi;

import java.util.ArrayList;
import java.util.Collection;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ChangePasswordUserAccountActivity extends Activity {

    public static final String USER_LOGIN_EXTRA = "userLogin";

    @InjectView(R.id.user_old_password_edit_text)
    protected EditText mOldPassword;

    @InjectView(R.id.user_new_password_edit_text)
    protected EditText mNewPassword;

    @InjectView(R.id.user_new_password_rep_edit_text)
    protected EditText mNewPasswordRep;

    private String mUserLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password_user_account);

        ButterKnife.inject(this);

        mUserLogin = getIntent().getStringExtra(USER_LOGIN_EXTRA);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.change_password_user_account, menu);
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


    @OnClick(R.id.user_password_save_button)
    protected void changePassword() {
        DateUtils dateUtils = new DateUtils();

        final String oldPassword = mOldPassword.getText().toString();
        if (oldPassword.equals("")) {
            Toast.makeText(ChangePasswordUserAccountActivity.this,
                    "Insert your old password", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        final String newPassword = mNewPassword.getText().toString();
        if (newPassword.equals("")) {
            Toast.makeText(ChangePasswordUserAccountActivity.this,
                    "Insert your new password", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        final String newPasswordRep = mNewPasswordRep.getText().toString();
        if (newPasswordRep.equals("")) {
            Toast.makeText(ChangePasswordUserAccountActivity.this,
                    "Insert your new password two times", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        if (!newPasswordRep.equals(newPassword)) {
            Toast.makeText(ChangePasswordUserAccountActivity.this,
                    "The retyped new password is different from the typed new password", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        final UserServiceApi userService = PotlatchServiceConn.getUserServiceOrShowLogin(this);
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            protected Exception error = null;

            @Override
            protected Void doInBackground(Void... voids) {
                User newUser = null;
                try {
                    Collection<String> passwords = new ArrayList<String>();
                    passwords.add(oldPassword);
                    passwords.add(newPassword);

                    OperationResult result = userService.changePassword(mUserLogin, passwords);
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
                    Toast.makeText(ChangePasswordUserAccountActivity.this,
                            "Error changing user password: " + error.getMessage(), Toast.LENGTH_SHORT)
                            .show();

                    return;
                }

                Toast.makeText(ChangePasswordUserAccountActivity.this, "User password changed",
                        Toast.LENGTH_SHORT);
                finish();
            }
        };

        task.execute();
    }
}
