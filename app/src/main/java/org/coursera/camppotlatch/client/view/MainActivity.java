package org.coursera.camppotlatch.client.view;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import org.coursera.camppotlatch.R;
import org.coursera.camppotlatch.client.commons.AppContext;

public class MainActivity extends Activity {

    private FragmentManager mFragmentManager;

    private Fragment mNewGiftsFragment;
    private Fragment mTopGiftsFragment;
    private Fragment mUserGiftsFragment;

    private Fragment mUsersFragment;
    private Fragment mTopGiftGiversFragment;
    private Fragment mViewUserAccountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configureNavigationMenu();

        // Get a reference to the FragmentManager
        mFragmentManager = getFragmentManager();

        mNewGiftsFragment = new NewGiftsFragment();
        mTopGiftsFragment = new TopGiftsFragment();

        mUserGiftsFragment = new UserGiftsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(UserGiftsFragment.USER_LOGIN_EXTRA, AppContext.getUser().getLogin());
        mUserGiftsFragment.setArguments(bundle);

        mUsersFragment = new UsersFragment();
        mTopGiftGiversFragment = new TopGiftsGiversFragment();

        mViewUserAccountFragment = new ViewUserAccountFragment();
        bundle = new Bundle();
        bundle.putString(ViewUserAccountFragment.USER_LOGIN_EXTRA, AppContext.getUser().getLogin());
        mViewUserAccountFragment.setArguments(bundle);

        // Start a new FragmentTransaction
        FragmentTransaction fragmentTransaction = mFragmentManager
                .beginTransaction();

        // Add the new gifts fragment to the layout
        fragmentTransaction.add(R.id.fragment_container, mNewGiftsFragment);

        // Commit the FragmentTransaction
        fragmentTransaction.commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    private void configureNavigationMenu() {
        ActionBar actionBar = getActionBar();

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME);

        SpinnerAdapter navigationSpinner =
                ArrayAdapter.createFromResource(this, R.array.navigation_titles,
                        android.R.layout.simple_spinner_dropdown_item);

        ActionBar.OnNavigationListener navigationListener = new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int i, long l) {
                FragmentTransaction fragmentTransaction = null;
                switch (i) {
                    case 0:
                        fragmentTransaction = mFragmentManager
                                .beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_container, mNewGiftsFragment);
                        fragmentTransaction.commit();
                        return true;
                    case 1:
                        fragmentTransaction = mFragmentManager
                                .beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_container, mTopGiftsFragment);
                        fragmentTransaction.commit();
                        return true;
                    case 2:
                        fragmentTransaction = mFragmentManager
                                .beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_container, mUserGiftsFragment);
                        fragmentTransaction.commit();
                        return true;
                    case 3:
                        fragmentTransaction = mFragmentManager
                                .beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_container, mUsersFragment);
                        fragmentTransaction.commit();
                        return true;
                    case 4:
                        fragmentTransaction = mFragmentManager
                                .beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_container, mTopGiftGiversFragment);
                        fragmentTransaction.commit();
                        return true;
                    case 5:
                        fragmentTransaction = mFragmentManager
                                .beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_container, mViewUserAccountFragment);
                        fragmentTransaction.commit();
                        return true;
                    default:
                        return false;
                }
            }
        };

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(navigationSpinner, navigationListener);
    }
}
