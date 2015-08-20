package org.mukdongjeil.mjchurch.common;

/**
 * Created by Kim SungJoong on 2015-08-11.
 */
public class Const {

    public static boolean DEBUG_MODE = true;

//    public static final String[] PAGER_MENUS = new String[] {"Introduce", "Worship", "Training", "Groups", "Board"};
//    public static final int INTRODUCE_PAGE_INDEX = 0;
//    public static final int WORSHIP_PAGE_INDEX = 1;

    public static final String BASE_URL         = "http://mukdongjeil.hompee.org";

    //introduce pages url
    public static final String INTRODUCE_URL    = BASE_URL + "/m/html/index.mo?menuId=1749&topMenuId=1&menuType=27&newMenuAt=false&tPage=1";
    public static final String HISTORY_URL = BASE_URL + "/m/html/index.mo?menuId=10004213&topMenuId=1&menuType=27&newMenuAt=true&tPage=1";
    public static final String FIND_MAP_URL     = BASE_URL + "/m/html/index.mo?menuId=1754&topMenuId=1&menuType=27&newMenuAt=false&tPage=1";
    public static final String TIME_TABLE_URL   = BASE_URL + "/m/html/index.mo?menuId=2273&topMenuId=1&menuType=27&newMenuAt=false&tPage=1";
    public static final String WORKER_URL       = BASE_URL + "/m/html/index.mo?menuId=1752&topMenuId=1&menuType=27&newMenuAt=false&tPage=1";

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

    public static final String getWorshipListURL(int worshipType, int pageNo) {
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

    public static final String getWorshipContentURL(int worshipType, int pageNo, String contentNo) {
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
}
