package org.mukdongjeil.mjchurch.introduce;

import android.graphics.Bitmap;
import android.os.Bundle;

import org.mukdongjeil.mjchurch.MainActivity;
import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.common.ext_fragment.ImageBaseFragment;
import org.mukdongjeil.mjchurch.common.util.DisplayUtil;
import org.mukdongjeil.mjchurch.common.util.ImageUtil;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.common.util.SystemHelpers;
import org.mukdongjeil.mjchurch.protocol.RequestBaseTask;
import org.mukdongjeil.mjchurch.protocol.RequestImageTask;
import org.mukdongjeil.mjchurch.slidingmenu.MenuListFragment;

//import org.mukdongjeil.mjchurch.common.photoview.PhotoViewAttacher;

/**
 * Created by Kim SungJoong on 2015-07-31.
 */
public class IntroduceFragment extends ImageBaseFragment {
    private static final String TAG = IntroduceFragment.class.getSimpleName();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showLoadingDialog();
        }

        String title;
        String requestUrl;

        Bundle args = getArguments();
        if (args == null) {
            Logger.e(TAG, "arguments is null");
            requestUrl = Const.INTRODUCE_HOME_URL;
            title = MenuListFragment.INTRODUCE_MENUS[0];
        } else {
            int selectedMenuIndex = args.getInt(MenuListFragment.SELECTED_MENU_INDEX);
            Logger.i(TAG, "selected sliding menu index : " + selectedMenuIndex);

            switch(selectedMenuIndex) {
                case 2:
                    requestUrl = Const.INTRODUCE_HISTORY_URL;
                    title = MenuListFragment.INTRODUCE_MENUS[1];
                    break;
                case 3:
                    requestUrl = Const.INTRODUCE_FIND_MAP_URL;
                    title = MenuListFragment.INTRODUCE_MENUS[2];
                    break;
                case 4:
                    requestUrl = Const.INTRODUCE_TIME_TABLE_URL;
                    title = MenuListFragment.INTRODUCE_MENUS[3];
                    break;
                case 5:
                    requestUrl = Const.INTRODUCE_WORKER_URL;
                    title = MenuListFragment.INTRODUCE_MENUS[4];
                    break;
                case 1:
                default:
                    requestUrl = Const.INTRODUCE_HOME_URL;
                    title = MenuListFragment.INTRODUCE_MENUS[0];
                    break;
            }
        }

        getActivity().setTitle(title);

        new RequestImageTask(requestUrl, new RequestBaseTask.OnResultListener() {
            @Override
            public void onResult(Object obj, int position) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).hideLoadingDialog();
                }

                if (obj != null && obj instanceof Bitmap) {
                    Bitmap resizedBitmap = ImageUtil.getResizeBitmapImage((Bitmap)obj, DisplayUtil.getDisplaySizeWidth(SystemHelpers.getApplicationContext()));
                    if (getImageView() != null) {
                        getImageView().setImageBitmap(resizedBitmap);
                    } else {
                        Logger.e(TAG, "getImageView is null at onResult");
                    }
                } else {
                    Logger.e(TAG, "obj is null or is not Bitmap at onResult");
                }
            }
        });
    }
}
