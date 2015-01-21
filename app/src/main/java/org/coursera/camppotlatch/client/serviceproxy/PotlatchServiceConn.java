/* 
 **
 ** Copyright 2014, Jules White
 **
 ** 
 */
package org.coursera.camppotlatch.client.serviceproxy;

import android.content.Context;
import android.content.Intent;

import org.coursera.camppotlatch.client.auth.EasyHttpClient;
import org.coursera.camppotlatch.client.auth.SecuredRestBuilder;
import org.coursera.camppotlatch.client.view.LoginActivity;

import retrofit.RestAdapter.LogLevel;
import retrofit.client.ApacheClient;
//import android.content.Context;
//import android.content.Intent;

public class PotlatchServiceConn {
    public static final String SERVER = "https://camppotlach-env.elasticbeanstalk.com";
    //public static final String SERVER = "http://camppotlach-env.elasticbeanstalk.com";
    //public static final String SERVER = "https://hal3:443";
    //public static final String SERVER = "http://hal3:8080";

    // Authentication path
    public static final String TOKEN_PATH = "/oauth/token";

	public static final String CLIENT_ID = "mobile";

	private static GiftServiceApi giftSvc_;
    private static UserServiceApi userSvc_;

    /*
    public static synchronized GiftServiceApi get() {
        return giftSvc_;
    }
    */

	public static synchronized GiftServiceApi getGiftServiceOrShowLogin(Context ctx) {
		if (giftSvc_ != null) {
			return giftSvc_;
		} else {
			Intent loginIntent = new Intent(ctx, LoginActivity.class);
			ctx.startActivity(loginIntent);
			return null;
		}
	}

    public static synchronized UserServiceApi getUserServiceOrShowLogin(Context ctx) {
        if (userSvc_ != null) {
            return userSvc_;
        } else {
            Intent loginIntent = new Intent(ctx, LoginActivity.class);
            ctx.startActivity(loginIntent);
            return null;
        }
    }

    /*
    public static synchronized UserServiceApi getNonAuthenticatedAccessToUserService() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new DateJsonSerializer())
                .excludeFieldsWithoutExposeAnnotation()
                .create();


        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(SERVER)
                .setClient(new ApacheClient(new EasyHttpClient()))
                .setConverter(new GsonConverter(gson))
                .build();

        return restAdapter.create(UserServiceApi.class);
    }
    */

    public static synchronized void init(String user,
			String pass) {

		giftSvc_ = new SecuredRestBuilder()
				.setLoginEndpoint(SERVER + TOKEN_PATH)
				.setUsername(user)
				.setPassword(pass)
				.setClientId(CLIENT_ID)
				.setClient(
						new ApacheClient(new EasyHttpClient()))
				.setEndpoint(SERVER).setLogLevel(LogLevel.FULL).build()
				.create(GiftServiceApi.class);

        userSvc_ = new SecuredRestBuilder()
                .setLoginEndpoint(SERVER + TOKEN_PATH)
                .setUsername(user)
                .setPassword(pass)
                .setClientId(CLIENT_ID)
                .setClient(
                        new ApacheClient(new EasyHttpClient()))
                .setEndpoint(SERVER).setLogLevel(LogLevel.FULL).build()
                .create(UserServiceApi.class);
    }
}
