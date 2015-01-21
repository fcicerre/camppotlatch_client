package org.coursera.camppotlatch.client.view;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import org.coursera.camppotlatch.R;
import org.coursera.camppotlatch.client.commons.AppContext;
import org.coursera.camppotlatch.client.model.User;
import org.coursera.camppotlatch.client.serviceproxy.PotlatchServiceConn;
import org.coursera.camppotlatch.client.serviceproxy.UserServiceApi;


public class LoginActivity extends Activity {
    @InjectView(R.id.login_input)
    protected EditText login_;

    @InjectView(R.id.pass_input)
    protected EditText password_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.inject(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
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

    @OnClick(R.id.sign_in_button)
    protected void login() {
        final String login = login_.getText().toString();
        final String pass = password_.getText().toString();

        PotlatchServiceConn.init(login, pass);
        //final GiftServiceApi giftSvc = PotlachServiceConn.getGiftServiceOrShowLogin(this);
        final UserServiceApi userSvc = PotlatchServiceConn.getUserServiceOrShowLogin(this);

        AsyncTask<Void, Void, User> conTask = new AsyncTask<Void, Void, User>() {
            protected Exception error;

            @Override
            protected User doInBackground(Void... voids) {
                User user = null;

                try {
                    user = userSvc.findByLogin(login);
                } catch (Exception ex) {
                    error = ex;
                }

                return user;
            }

            @Override
            protected void onPostExecute(User user) {
                super.onPostExecute(user);

                if (error != null) {
                    Log.e(LoginActivity.class.getName(), "Error logging in via OAuth.", error);

                    Toast.makeText(
                            LoginActivity.this,
                            "Login failed, check your Internet connection and credentials.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Set the logged user
                //AppContext.setUserLogin(login);
                AppContext.setUser(user);

                // OAuth 2.0 grant was successful and we
                // can talk to the server, open up the video listing
                /*
                startActivity(new Intent(
                        LoginActivity.this,
                        NewGiftsActivity.class));
                 */

                startActivity(new Intent(
                        LoginActivity.this,
                        MainActivity.class));
            }
        };

        conTask.execute();
    }

    @OnClick(R.id.sign_up_button)
    protected void createUserAccount() {
        startActivity(new Intent(
                LoginActivity.this,
                CreateUserAccountActivity.class));
    }
}
