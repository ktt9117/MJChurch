package org.mukdongjeil.mjchurch.common.dao;

/**
 * Created by Kim SungJoong on 2015-08-13.
 */
public class MenuItem {
    public static final int MENU_TYPE_GROUP = 0;
    public static final int MENU_TYPE_ITEM = 1;

    public static final int MENU_CATEGORY_INTRODUCE = 0;
    public static final int MENU_CATEGORY_WORSHIP = 1;
    public static final int MENU_CATEGORY_TRAINING = 2;
    public static final int MENU_CATEGORY_GROUP = 3;
    public static final int MENU_CATEGORY_BOARD = 4;

    public String title;
    public int iconRes;
    public int menuType;
    public int menuCategory;

    public MenuItem(String title) {
        this.title = title;
        this.menuType = MENU_TYPE_GROUP;
        this.iconRes = -1;
    }

    public MenuItem(String title, int iconRes, int category) {
        this.title = title;
        this.iconRes = iconRes;
        this.menuType = MENU_TYPE_ITEM;
        this.menuCategory = category;
    }
}