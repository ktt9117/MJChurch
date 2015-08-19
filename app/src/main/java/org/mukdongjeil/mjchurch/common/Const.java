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
    public static final String HISTROY_URL      = BASE_URL + "/m/html/index.mo?menuId=10004213&topMenuId=1&menuType=27&newMenuAt=true&tPage=1";
    public static final String FIND_MAP_URL     = BASE_URL + "/m/html/index.mo?menuId=1754&topMenuId=1&menuType=27&newMenuAt=false&tPage=1";
    public static final String TIME_TABLE_URL   = BASE_URL + "/m/html/index.mo?menuId=2273&topMenuId=1&menuType=27&newMenuAt=false&tPage=1";
    public static final String WORKER_URL       = BASE_URL + "/m/html/index.mo?menuId=1752&topMenuId=1&menuType=27&newMenuAt=false&tPage=1";

    //worship pages url
    private static final String WORSHIP_LIST_URL        = BASE_URL + "/m/board/index.mo?menuId=10004043&topMenuId=2&menuType=1&newMenuAt=true&tPage=";
    private static final String WORSHIP_CONTENT_URL     = BASE_URL + "/m/board/view.mo?menuId=10004043&topMenuId=2&menuType=1&newMenuAt=true&tPage=";
    private static final String WORSHIP_CONTENT_EXT_URL = "&sPage=1&ssPage=1&topSubId=null&pageNow=1&bbsNo=";

    public static final String getWorshipListURL(int pageNo) {
        return WORSHIP_LIST_URL + pageNo;
    }

    public static final String getWorshipContentURL(int pageNo, String contentNo) {
        return WORSHIP_CONTENT_URL + pageNo + WORSHIP_CONTENT_EXT_URL + contentNo;
    }
}
