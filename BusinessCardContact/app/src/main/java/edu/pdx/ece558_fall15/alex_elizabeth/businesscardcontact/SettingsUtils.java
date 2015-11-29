package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.app.Activity;
import android.content.Intent;

/**
 * This class is used to handle the dynamic setting of the activity themes.
 * Modified from sample code by Matt Quigley
 * http://www.androidengineer.com/2010/06/using-themes-in-android-applications.html
 */
public class SettingsUtils {
    private static int sTheme;

    public final static int THEME_DEFAULT = 0;
    public final static int THEME_TEAL = 1;
    //public final static int THEME_EGGPLANT = 2;

    /**
     * Set the theme of the Activity, and restart it by creating a new Activity
     * of the same type.
     */
    public static void changeToTheme(Activity activity, int theme)
    {
        // When the theme is changed, need to restart the activity.
        sTheme = theme;
        activity.finish();

        activity.startActivity(new Intent(activity, activity.getClass()));
    }

    /** Set the theme of the activity, according to the configuration. */
    public static void onActivityCreateSetTheme(Activity activity)
    {
        switch (sTheme)
        {
            default:
            case THEME_DEFAULT:
               // activity.setTheme(R.style.AppDefaultTheme);
                break;
            case THEME_TEAL:
                activity.setTheme(R.style.AppTheme1);
                break;
            /*
            case THEME_EGGPLANT:
                activity.setTheme(R.style.Theme_Blue);
                break;
                */
        }
    }
}
