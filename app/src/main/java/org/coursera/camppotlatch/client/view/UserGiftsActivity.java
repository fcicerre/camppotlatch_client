package org.coursera.camppotlatch.client.view;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import org.coursera.camppotlatch.R;

public class UserGiftsActivity extends Activity {
    public static final String USER_LOGIN_EXTRA = "userLogin";

    private FragmentManager mFragmentManager;

    private Fragment mUserGiftsFragment;

    protected String mUserLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_gifts);

        mUserLogin = getIntent().getStringExtra(USER_LOGIN_EXTRA);

        // Get a reference to the FragmentManager
        mFragmentManager = getFragmentManager();

        mUserGiftsFragment = new UserGiftsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(UserGiftsFragment.USER_LOGIN_EXTRA, mUserLogin);
        mUserGiftsFragment.setArguments(bundle);

        // Start a new FragmentTransaction
        FragmentTransaction fragmentTransaction = mFragmentManager
                .beginTransaction();

        // Add the new gifts fragment to the layout
        fragmentTransaction.add(R.id.fragment_container, mUserGiftsFragment);

        // Commit the FragmentTransaction
        fragmentTransaction.commit();
    }
}
