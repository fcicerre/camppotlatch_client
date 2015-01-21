package org.coursera.camppotlatch.client.commons;

import android.os.Environment;

import org.coursera.camppotlatch.client.model.User;

import java.io.File;
import java.io.IOException;

/**
 * Created by Fabio on 08/11/2014.
 */
public class AppContext {
    private static final String APP_DIR = "camppotlach";

    //private static String userLogin_;
    private static User user_;

    // User login
    /*
    public static String getUserLogin() {
        return userLogin_;
    }
    public static void setUserLogin(String userLogin) {
        userLogin_ = userLogin;
    }
    */

    // User
    public static User getUser() { return user_; }
    public static void setUser(User user) { user_ = user; }

    // Get the application root directory
    public static File getAppDirectory() throws IOException {
        //File storageDir = Environment.getExternalStoragePublicDirectory(
        // Environment.DIRECTORY_PICTURES);
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state))
            throw new IOException("No external storage available");

        File storagePath = Environment.getExternalStorageDirectory();
        //if (!storagePath.exists())
        //    storagePath.mkdirs();
        File appDir = new File(storagePath, APP_DIR);
        if (!appDir.exists())
            appDir.mkdir();

        return appDir;
    }
}
