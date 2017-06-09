package org.mukdongjeil.mjchurch.common;

import android.os.Environment;

import org.mukdongjeil.mjchurch.R;

import java.io.File;

/**
 * Created by Kim SungJoong on 2015-08-11.
 */
public class Const {

    public static boolean DEBUG_MODE = false;

    public static long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;

    public static final File DIR_PUB_DOWNLOAD = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

    public static final int DEFAULT_IMG_RESOURCE = R.drawable.ic_progressring;

    public static final int THANKS_SHARE_LIST_COUNT_PER_PAGE = 20;
    public static final int GALLERY_LIST_COUNT_PER_PAGE = 9;


    public static final int PAGE_TYPE_INTRODUCE = 1;
    public static final int PAGE_TYPE_SERMON = 2;
    public static final int PAGE_TYPE_TRAINING = 3;
    public static final int PAGE_TYPE_BOARD = 4;

    public static final String BASE_URL = "http://mukdongjeil.hompee.org";

    public static final String INTENT_KEY_SELECTED_MENU_INDEX = "selectedMenuIndex";
    public static final String INTENT_KEY_PAGE_TYPE = "pageType";
    public static final String INTENT_KEY_PAGE_TITLES = "pageTitles";
    public static final String INTENT_KEY_PAGE_URLS = "pageUrls";

    // Introduce menu names
    public static final String[] INTRODUCE_MENU_NAMES = { "교회소개", "교회연혁", "찾아오는 길", "예배시간안내", "섬김의 동역자" };

    // Introduce page urls
    public static final String INTRODUCE_HOME_URL = BASE_URL + "/m/html/index.mo?menuId=1749&topMenuId=1&menuType=27&newMenuAt=false&tPage=1";
    public static final String INTRODUCE_HISTORY_URL = BASE_URL + "/m/html/index.mo?menuId=10004213&topMenuId=1&menuType=27&newMenuAt=true&tPage=1";
    public static final String INTRODUCE_FIND_MAP_URL = BASE_URL + "/m/html/index.mo?menuId=1754&topMenuId=1&menuType=27&newMenuAt=false&tPage=1";
    public static final String INTRODUCE_TIME_TABLE_URL = BASE_URL + "/m/html/index.mo?menuId=2273&topMenuId=1&menuType=27&newMenuAt=false&tPage=1";
    public static final String INTRODUCE_WORKER_URL = BASE_URL + "/m/html/index.mo?menuId=1752&topMenuId=1&menuType=27&newMenuAt=false&tPage=1";

    public static final String[] INTRODUCE_MENU_URLS = {
            INTRODUCE_HOME_URL,
            INTRODUCE_HISTORY_URL,
            INTRODUCE_FIND_MAP_URL,
            INTRODUCE_TIME_TABLE_URL,
            INTRODUCE_WORKER_URL
    };

    // Training menu names
    public static final String[] TRAINING_MENU_NAMES = { "양육과 훈련", "성경공부", "양육", "마더와이즈", "일대일 제자양육" };

    // Training page urls
    public static final String TRAINING_HOME_URL = BASE_URL + "/m/html/index.mo?menuId=10005607&topMenuId=3&menuType=27&newMenuAt=true";
    public static final String TRAINING_BIBLE_STUDY_URL = BASE_URL + "/m/html/index.mo?menuId=10004998&topMenuId=3&menuType=27&newMenuAt=true";
    public static final String TRAINING_REARING_CLASS_URL = BASE_URL + "/m/html/index.mo?menuId=10004997&topMenuId=3&menuType=27&newMenuAt=true";
    public static final String TRAINING_MOTHER_WISE_URL = BASE_URL + "/m/html/index.mo?menuId=10004999&topMenuId=3&menuType=27&newMenuAt=true";
    public static final String TRAINING_DISCIPLE_URL = BASE_URL + "/m/html/index.mo?menuId=10005002&topMenuId=3&menuType=27&newMenuAt=true";

    public static final String[] TRAINING_MENU_URLS = {
            TRAINING_HOME_URL,
            TRAINING_BIBLE_STUDY_URL,
            TRAINING_REARING_CLASS_URL,
            TRAINING_MOTHER_WISE_URL,
            TRAINING_DISCIPLE_URL
    };

    // Board page urls
    private static final String BOARD_THANKS_SHARE_URL = BASE_URL + "/m/board/index.mo?menuId=10004076&topMenuId=6&menuType=1&newMenuAt=true&pageNow=";
    private static final String BOARD_GALLERY_LIST_URL = BASE_URL + "/m/photo/index.mo?menuId=7203&topMenuId=6&menuType=9&newMenuAt=false&pageNow=";
    private static final String BOARD_GALLERY_CONTENT_URL = BASE_URL + "/m/photo/view.mo?menuId=7203&topMenuId=6&menuType=9&newMenuAt=false&bbsNo=";
    private static final String BOARD_NEW_PERSON_LIST_URL = BASE_URL + "/m/photo/index.mo?menuId=1770&topMenuId=6&menuType=9&newMenuAt=false&pageNow=";
    private static final String BOARD_NEW_PERSON_CONTENT_URL = BASE_URL + "/m/photo/view.mo?menuId=1770&topMenuId=6&menuType=9&newMenuAt=false&bbsNo=";

    public static final String[] BOARD_MENU_URLS = {
            BOARD_THANKS_SHARE_URL,
            BOARD_GALLERY_LIST_URL,
            BOARD_NEW_PERSON_LIST_URL
    };

    public static String getThanksShareListUrl(int pageNo) {
        return BOARD_THANKS_SHARE_URL + pageNo;
    }

    public static String getGalleryListUrl(int pageNo) {
        return BOARD_GALLERY_LIST_URL + pageNo;
    }

    public static String getGalleryContentUrl(String contentNo) {
        return BOARD_GALLERY_CONTENT_URL + contentNo;
    }

    public static String getNewPersonListUrl(int pageNo) {
        return BOARD_NEW_PERSON_LIST_URL + pageNo;
    }

    public static String getNewPersonContentUrl(String contentNo) {
        return BOARD_NEW_PERSON_CONTENT_URL + contentNo;
    }

    // Sermon page urls
    public static final int WORSHIP_TYPE_SUNDAY_MORNING = 0;
    public static final int WORSHIP_TYPE_SUNDAY_AFTERNOON = 1;
    public static final int WORSHIP_TYPE_WEDNESDAY = 2;
    public static final int WORSHIP_TYPE_FRIDAY = 3;

    public static final int SUNDAY_MORNING_WORSHIP_ID      = 10004043;
    public static final int SUNDAY_AFTERNOON_WORSHIP_ID    = 10004044;
    public static final int WEDNESDAY_WORSHIP_ID           = 10004487;
    public static final int FRIDAY_WORSHIP_ID              = 10006470;

    private static final String WORSHIP_LIST_URL        = BASE_URL + "/m/board/index.mo?menuId=";
    private static final String WORSHIP_LIST_EXT_URL    = "&topMenuId=2&menuType=1&newMenuAt=true&tPage=";

    private static final String WORSHIP_CONTENT_URL     = BASE_URL + "/m/board/view.mo?menuId=";
    private static final String WORSHIP_CONTENT_EXT1_URL = "&topMenuId=2&menuType=1&newMenuAt=true&tPage=";
    private static final String WORSHIP_CONTENT_EXT2_URL = "&sPage=1&ssPage=1&topSubId=null&pageNow=1&bbsNo=";

    public static String getWorshipListUrl(int worshipType, int pageNo) {
        switch (worshipType) {
            case WORSHIP_TYPE_WEDNESDAY:
                return WORSHIP_LIST_URL + WEDNESDAY_WORSHIP_ID + WORSHIP_LIST_EXT_URL + pageNo;

            case WORSHIP_TYPE_SUNDAY_AFTERNOON:
                return WORSHIP_LIST_URL + SUNDAY_AFTERNOON_WORSHIP_ID + WORSHIP_LIST_EXT_URL + pageNo;

            case WORSHIP_TYPE_FRIDAY:
                return WORSHIP_LIST_URL + FRIDAY_WORSHIP_ID + WORSHIP_LIST_EXT_URL + pageNo;

            case WORSHIP_TYPE_SUNDAY_MORNING:
            default:
                return WORSHIP_LIST_URL + SUNDAY_MORNING_WORSHIP_ID + WORSHIP_LIST_EXT_URL + pageNo;
        }
    }

    public static final String[] WORSHIP_PAGE_URLS = {
            getWorshipListUrl(WORSHIP_TYPE_SUNDAY_MORNING, 1),
            getWorshipListUrl(WORSHIP_TYPE_SUNDAY_AFTERNOON, 1),
            getWorshipListUrl(WORSHIP_TYPE_WEDNESDAY, 1),
            getWorshipListUrl(WORSHIP_TYPE_FRIDAY, 1)
    };

    public static String getWorshipContentUrl(int worshipType, int pageNo, String contentNo) {
        switch (worshipType) {
            case WORSHIP_TYPE_WEDNESDAY:
                return WORSHIP_CONTENT_URL + WEDNESDAY_WORSHIP_ID + WORSHIP_CONTENT_EXT1_URL + pageNo + WORSHIP_CONTENT_EXT2_URL + contentNo;

            case WORSHIP_TYPE_SUNDAY_AFTERNOON:
                return WORSHIP_CONTENT_URL + SUNDAY_AFTERNOON_WORSHIP_ID + WORSHIP_CONTENT_EXT1_URL + pageNo + WORSHIP_CONTENT_EXT2_URL + contentNo;

            case WORSHIP_TYPE_FRIDAY:
                return WORSHIP_CONTENT_URL + FRIDAY_WORSHIP_ID + WORSHIP_CONTENT_EXT1_URL + pageNo + WORSHIP_CONTENT_EXT2_URL + contentNo;

            case WORSHIP_TYPE_SUNDAY_MORNING:
            default:
                return WORSHIP_CONTENT_URL + SUNDAY_MORNING_WORSHIP_ID + WORSHIP_CONTENT_EXT1_URL + pageNo + WORSHIP_CONTENT_EXT2_URL + contentNo;
        }
    }
}
