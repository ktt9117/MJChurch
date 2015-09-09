package org.mukdongjeil.mjchurch.common;

import org.mukdongjeil.mjchurch.R;

/**
 * Created by Kim SungJoong on 2015-08-11.
 */
public class Const {

    public static boolean DEBUG_MODE = true;

    public static final int DEFAULT_IMG_RESOURCE = R.mipmap.ic_launcher;

    public static final int THANKS_SHARE_LIST_COUNT_PER_PAGE = 20;
    public static final int GALLERY_LIST_COUNT_PER_PAGE = 9;

    public static final String BASE_URL         = "http://mukdongjeil.hompee.org";

    //introduce pages url
    public static final String INTRODUCE_HOME_URL = BASE_URL + "/m/html/index.mo?menuId=1749&topMenuId=1&menuType=27&newMenuAt=false&tPage=1";
    public static final String INTRODUCE_HISTORY_URL = BASE_URL + "/m/html/index.mo?menuId=10004213&topMenuId=1&menuType=27&newMenuAt=true&tPage=1";
    public static final String INTRODUCE_FIND_MAP_URL = BASE_URL + "/m/html/index.mo?menuId=1754&topMenuId=1&menuType=27&newMenuAt=false&tPage=1";
    public static final String INTRODUCE_TIME_TABLE_URL = BASE_URL + "/m/html/index.mo?menuId=2273&topMenuId=1&menuType=27&newMenuAt=false&tPage=1";
    public static final String INTRODUCE_WORKER_URL = BASE_URL + "/m/html/index.mo?menuId=1752&topMenuId=1&menuType=27&newMenuAt=false&tPage=1";

    //worship pages url
    public static final int WORSHIP_TYPE_SUNDAY_MORNING = 0;
    public static final int WORSHIP_TYPE_SUNDAY_AFTERNOON = 1;
    public static final int WORSHIP_TYPE_WEDNESDAY = 2;

    private static final int SUNDAY_MORNING_WORSHIP_ID      = 10004043;
    private static final int SUNDAY_AFTERNOON_WORSHIP_ID    = 10004044;
    private static final int WEDNESDAY_WORSHIP_ID           = 10004487;

    private static final String WORSHIP_LIST_URL        = BASE_URL + "/m/board/index.mo?menuId=";
    private static final String WORSHIP_LIST_EXT_URL    = "&topMenuId=2&menuType=1&newMenuAt=true&tPage=";

    private static final String WORSHIP_CONTENT_URL     = BASE_URL + "/m/board/view.mo?menuId=";
    private static final String WORSHIP_CONTENT_EXT1_URL = "&topMenuId=2&menuType=1&newMenuAt=true&tPage=";
    private static final String WORSHIP_CONTENT_EXT2_URL = "&sPage=1&ssPage=1&topSubId=null&pageNow=1&bbsNo=";

    //training pages url
    public static final String TRAINING_HOME_URL = BASE_URL + "/m/html/index.mo?menuId=10005607&topMenuId=3&menuType=27&newMenuAt=true";
    public static final String TRAINING_BIBLE_STUDY_URL = BASE_URL + "/m/html/index.mo?menuId=10004998&topMenuId=3&menuType=27&newMenuAt=true";
    public static final String TRAINING_REARING_CLASS_URL = BASE_URL + "/m/html/index.mo?menuId=10004997&topMenuId=3&menuType=27&newMenuAt=true";
    public static final String TRAINING_MOTHER_WISE_URL = BASE_URL + "/m/html/index.mo?menuId=10004999&topMenuId=3&menuType=27&newMenuAt=true";
    public static final String TRAINING_DISCIPLE_URL = BASE_URL + "/m/html/index.mo?menuId=10005002&topMenuId=3&menuType=27&newMenuAt=true";
    public static final String TRAINING_BOARD_URL = BASE_URL + "/m/html/index.mo?menuId=10005628&topMenuId=3&menuType=27&newMenuAt=true";

    //board pages url
    private static final String BOARD_THANKS_SHARE_URL = BASE_URL + "/m/board/index.mo?menuId=10004076&topMenuId=6&menuType=1&newMenuAt=true&pageNow=";
    private static final String BOARD_GALLERY_LIST_URL = BASE_URL + "/m/photo/index.mo?menuId=7203&topMenuId=6&menuType=9&newMenuAt=false&pageNow=";
    private static final String BOARD_GALLERY_CONTENT_URL = BASE_URL + "/m/photo/view.mo?menuId=7203&topMenuId=6&menuType=9&newMenuAt=false&bbsNo=";
    private static final String BOARD_NEW_PERSON_LIST_URL = BASE_URL + "/m/photo/index.mo?menuId=1770&topMenuId=6&menuType=9&newMenuAt=false&pageNow=";
    private static final String BOARD_NEW_PERSON_CONTENT_URL = BASE_URL + "/m/photo/view.mo?menuId=1770&topMenuId=6&menuType=9&newMenuAt=false&bbsNo=";

    public static final String getWorshipListUrl(int worshipType, int pageNo) {
        switch (worshipType) {
            case WORSHIP_TYPE_WEDNESDAY:
                return WORSHIP_LIST_URL + WEDNESDAY_WORSHIP_ID + WORSHIP_LIST_EXT_URL + pageNo;

            case WORSHIP_TYPE_SUNDAY_AFTERNOON:
                return WORSHIP_LIST_URL + SUNDAY_AFTERNOON_WORSHIP_ID + WORSHIP_LIST_EXT_URL + pageNo;

            case WORSHIP_TYPE_SUNDAY_MORNING:
            default:
                return WORSHIP_LIST_URL + SUNDAY_MORNING_WORSHIP_ID + WORSHIP_LIST_EXT_URL + pageNo;
        }
    }

    public static final String getWorshipContentUrl(int worshipType, int pageNo, String contentNo) {
        switch (worshipType) {
            case WORSHIP_TYPE_WEDNESDAY:
                return WORSHIP_CONTENT_URL + WEDNESDAY_WORSHIP_ID + WORSHIP_CONTENT_EXT1_URL + pageNo + WORSHIP_CONTENT_EXT2_URL + contentNo;

            case WORSHIP_TYPE_SUNDAY_AFTERNOON:
                return WORSHIP_CONTENT_URL + SUNDAY_AFTERNOON_WORSHIP_ID + WORSHIP_CONTENT_EXT1_URL + pageNo + WORSHIP_CONTENT_EXT2_URL + contentNo;

            case WORSHIP_TYPE_SUNDAY_MORNING:
            default:
                return WORSHIP_CONTENT_URL + SUNDAY_MORNING_WORSHIP_ID + WORSHIP_CONTENT_EXT1_URL + pageNo + WORSHIP_CONTENT_EXT2_URL + contentNo;
        }
    }

    public static final String getThanksShareListUrl(int pageNo) {
        return BOARD_THANKS_SHARE_URL + pageNo;
    }

    public static final String getGalleryListUrl(int pageNo) {
        return BOARD_GALLERY_LIST_URL + pageNo;
    }

    public static final String getGalleryContentUrl(String contentNo) {
        return BOARD_GALLERY_CONTENT_URL + contentNo;
    }

    public static final String getNewPersonListUrl(int pageNo) {
        return BOARD_NEW_PERSON_LIST_URL + pageNo;
    }

    public static final String getNewPersonContentUrl(String contentNo) {
        return BOARD_NEW_PERSON_CONTENT_URL + contentNo;
    }

}
