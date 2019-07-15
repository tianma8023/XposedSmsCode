package com.tianma.xsmscode.ui.theme;

import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;

import com.github.tianma8023.xposed.smscode.R;
import com.tianma.xsmscode.common.constant.PrefConst;

import java.util.ArrayList;
import java.util.List;

public class ThemeItemContainer {

    private List<ThemeItem> mThemeItemList;

    private static ThemeItemContainer sInstance;

    private ThemeItemContainer() {
        mThemeItemList = loadThemeItems();
    }

    public static ThemeItemContainer get() {
        if (sInstance == null) {
            synchronized (ThemeItemContainer.class) {
                if (sInstance == null) {
                    sInstance = new ThemeItemContainer();
                }
            }
        }
        return sInstance;
    }

    private List<ThemeItem> loadThemeItems() {
        List<ThemeItem> themeItems = new ArrayList<>();
        @StringRes int[] colorNameResArray = {
//                R.string.color_default,
                R.string.blue,
                R.string.red,
                R.string.pink,
                R.string.yellow,
                R.string.teal,
                R.string.green,
                R.string.violet,
                R.string.black,
        };
        @ColorRes int[] colorValueResArray = {
//                R.color.colorPrimaryDark,
                R.color.colorPrimaryDark_blue,
                R.color.colorPrimaryDark_red,
                R.color.colorPrimaryDark_pink,
                R.color.colorPrimaryDark_yellow,
                R.color.colorPrimary_teal,
                R.color.colorPrimaryDark_green,
                R.color.colorPrimary_violet,
                R.color.colorPrimary_black,
        };
        @StyleRes int[] themeResArray = {
//                R.style.AppTheme,
                R.style.AppTheme_Blue,
                R.style.AppTheme_Red,
                R.style.AppTheme_Pink,
                R.style.AppTheme_Yellow,
                R.style.AppTheme_Teal,
                R.style.AppTheme_Green,
                R.style.AppTheme_Violet,
                R.style.AppTheme_Black,
        };

        for (int i = 0; i < colorNameResArray.length; i++) {
            themeItems.add(new ThemeItem(
                    colorNameResArray[i],
                    colorValueResArray[i],
                    themeResArray[i]
            ));
        }
        return themeItems;
    }

    public ThemeItem getItemAt(int index) {
        // check current theme index in case of exception.
        if(index < 0 || index >= mThemeItemList.size()) {
            index = PrefConst.CURRENT_THEME_INDEX_DEFAULT;
        }
        return mThemeItemList.get(index);
    }

    public List<ThemeItem> getThemeItemList() {
        return mThemeItemList;
    }

}
