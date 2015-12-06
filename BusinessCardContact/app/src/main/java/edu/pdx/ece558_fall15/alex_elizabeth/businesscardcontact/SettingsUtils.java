package edu.pdx.ece558_fall15.alex_elizabeth.businesscardcontact;

import android.app.Activity;
import android.content.Intent;

import java.util.ArrayList;

/**
 * This class is used to handle the dynamic setting of the activity themes.
 * Modified from sample code by Matt Quigley
 * http://www.androidengineer.com/2010/06/using-themes-in-android-applications.html
 */
public class SettingsUtils {
    private static int sTheme;  // the integer of the index of the currently selected theme

    // assign the default theme
    public final static int DEFAULT_COLOR_THEME = 0;

    //public final static int THEME_DEFAULT = 0;
    public final static int THEME_TEAL = 0;
    public final static int THEME_SAGE = 1;
    public final static int THEME_OCEAN = 2;
    public final static int THEME_EGGPLANT = 3;
    public final static int THEME_BUBBLEGUM = 4;
    public final static int THEME_CRANBERRY = 5;
    public final static int THEME_MONOCHROME = 6;
    public final static int THEME_STANDARD = 7;

    // list of names of the available themes
    private static String[] mThemeListStr = {"Teal", "Sage", "Ocean", "Eggplant", "Bubblegum", "Cranberry", "Monochrome", "Standard"};

    /**
     * Return the currently selected theme
     */
    public static int getActiveTheme() {
        return sTheme;
    }

    /**
     * Sets the active theme.
     * Note: This is used only in onCreate() of the first activity to retrieve savedPreferences and load the saved theme here.
     */
    public static void setActiveTheme(int selectedTheme) {
        sTheme = selectedTheme;
    }

    /**
     * Return the list of available themes
     */
    public static String[] getThemeListStr() {
        return mThemeListStr;
    }

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

    /**
     * Set the theme of the activity, according to the configuration.
     */
    public static void onActivityCreateSetTheme(Activity activity)
    {
        // set the activities theme, depending on the currently selected theme
        switch (sTheme)
        {
            case THEME_TEAL:
                activity.setTheme(R.style.AppThemeTeal);
                break;
            case THEME_SAGE:
                activity.setTheme(R.style.AppThemeSage);
                break;
            case THEME_OCEAN:
                activity.setTheme(R.style.AppThemeOcean);
                break;
            case THEME_EGGPLANT:
                activity.setTheme(R.style.AppThemeEggplant);
                break;
            case THEME_BUBBLEGUM:
                activity.setTheme(R.style.AppThemeBubblegum);
                break;
            case THEME_CRANBERRY:
                activity.setTheme(R.style.AppThemeCranberry);
                break;
            case THEME_MONOCHROME:
                activity.setTheme(R.style.AppThemeMonochrome);
                break;
            case THEME_STANDARD:
            default:
                activity.setTheme(R.style.AppThemeStandard);
                break;
        }
    }
}
