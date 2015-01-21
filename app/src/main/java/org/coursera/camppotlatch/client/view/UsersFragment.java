package org.coursera.camppotlatch.client.view;

import android.app.Activity;

import com.google.common.collect.Lists;

import org.coursera.camppotlatch.client.model.User;
import org.coursera.camppotlatch.client.serviceproxy.PotlatchServiceConn;
import org.coursera.camppotlatch.client.serviceproxy.UserServiceApi;

import java.util.Collection;
import java.util.List;

public class UsersFragment extends AbstractUsersFragment {
    private Activity mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mActivity = activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mActivity = null;
    }

    @Override
    protected List<User> loadUsers(String mSearchText, int offset, int limit) {
        final UserServiceApi svc = PotlatchServiceConn.getUserServiceOrShowLogin(mActivity);

        List<User> retrievedUsersList = null;
        if (svc != null) {
            Collection<User> retrievedUsers = svc.findAll(mSearchText, offset, limit);

            retrievedUsersList = Lists.newArrayList(retrievedUsers);
        }

        return retrievedUsersList;
    }
}
