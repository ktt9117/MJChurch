package org.mukdongjeil.mjchurch;

import android.os.Environment;

import java.io.File;

/**
 * Created by Kim SungJoong on 2015-08-11.
 */
public class Const {

    public static final boolean DEBUG_MODE = false;

    public static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    public static final File DIR_PUB_DOWNLOAD = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    public static final int DEFAULT_IMG_RESOURCE = R.drawable.ic_progressring;

    public static final String FIRE_DATA_MESSAGE_CHILD = "messages";
    public static final String FIRE_DATA_USER_CHILD = "users";

    public static final String MIME_TYPE_IMAGES = "image/*";
    public static final String FILE_EXT_JPG = ".jpg";
    public static final String SAVE_IMAGE_PREFIX = "/img_";
    public static final String SIMPLE_DATETIME_FORMAT = "yyyyMMdd-hhmmss";
    public static final String ALBUM_NAME = "/묵동제일앨범";
    public static final String TEMP_FILE_NAME = "/tmp_share_image.jpg";

    public static final String CHATROOM_TOPIC = "chat_room_topic";
    public static final String INTENT_ACTION_OPEN_CHAT = "open_chat";
    public static final int NOTIFICATION_ID_CHAT = 9999;

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
    public static final String INTENT_KEY_WORSHIP_TYPE = "worshipType";
    public static final String INTENT_KEY_TITLE = "title";
    public static final String INTENT_KEY_MESSAGE = "message";
    public static final String INTENT_KEY_USERNAME = "username";

    // Introduce menu names
    public static final String[] INTRODUCE_MENU_NAMES = { "교회소개", "교회연혁", "찾아오는 길", "예배시간안내", "섬김의 동역자" };

    // Introduce page urls
    private static final String INTRODUCE_HOME_URL = BASE_URL + "/m/html/index.mo?menuId=1749&topMenuId=1&menuType=27&newMenuAt=false&tPage=1";
    private static final String INTRODUCE_HISTORY_URL = BASE_URL + "/m/html/index.mo?menuId=10004213&topMenuId=1&menuType=27&newMenuAt=true&tPage=1";
    private static final String INTRODUCE_FIND_MAP_URL = BASE_URL + "/m/html/index.mo?menuId=1754&topMenuId=1&menuType=27&newMenuAt=false&tPage=1";
    private static final String INTRODUCE_TIME_TABLE_URL = BASE_URL + "/m/html/index.mo?menuId=2273&topMenuId=1&menuType=27&newMenuAt=false&tPage=1";
    private static final String INTRODUCE_WORKER_URL = BASE_URL + "/m/html/index.mo?menuId=1752&topMenuId=1&menuType=27&newMenuAt=false&tPage=1";

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
    private static final String TRAINING_HOME_URL = BASE_URL + "/m/html/index.mo?menuId=10005607&topMenuId=3&menuType=27&newMenuAt=true";
    private static final String TRAINING_BIBLE_STUDY_URL = BASE_URL + "/m/html/index.mo?menuId=10004998&topMenuId=3&menuType=27&newMenuAt=true";
    private static final String TRAINING_REARING_CLASS_URL = BASE_URL + "/m/html/index.mo?menuId=10004997&topMenuId=3&menuType=27&newMenuAt=true";
    private static final String TRAINING_MOTHER_WISE_URL = BASE_URL + "/m/html/index.mo?menuId=10004999&topMenuId=3&menuType=27&newMenuAt=true";
    private static final String TRAINING_DISCIPLE_URL = BASE_URL + "/m/html/index.mo?menuId=10005002&topMenuId=3&menuType=27&newMenuAt=true";

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

    // Sermon page urls
    public static final int WORSHIP_TYPE_SUNDAY_MORNING = 0;
    public static final int WORSHIP_TYPE_SUNDAY_AFTERNOON = 1;
    public static final int WORSHIP_TYPE_WEDNESDAY = 2;
    public static final int WORSHIP_TYPE_FRIDAY = 3;

    private static final int SUNDAY_MORNING_WORSHIP_ID = 10004043;
    private static final int SUNDAY_AFTERNOON_WORSHIP_ID = 10004044;
    private static final int WEDNESDAY_WORSHIP_ID = 10004487;
    private static final int FRIDAY_WORSHIP_ID = 10006470;

    private static final String WORSHIP_LIST_URL = BASE_URL + "/m/board/index.mo?menuId=";
    private static final String WORSHIP_LIST_EXT_URL = "&topMenuId=2&menuType=1&newMenuAt=true&tPage=";

    private static final String WORSHIP_CONTENT_URL = BASE_URL + "/m/board/view.mo?menuId=";
    private static final String WORSHIP_CONTENT_EXT1_URL = "&topMenuId=2&menuType=1&newMenuAt=true&tPage=";
    private static final String WORSHIP_CONTENT_EXT2_URL = "&sPage=1&ssPage=1&topSubId=null&pageNow=1&bbsNo=";

    public static final String getWorshipListUrl(int worshipType, int pageNo) {
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

    public static final String getWorshipContentUrl(int worshipType, int pageNo, String contentNo) {
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
