package org.mukdongjeil.mjchurch.training;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Toast;

import org.mukdongjeil.mjchurch.MainActivity;
import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.common.ext_fragment.ImageBaseFragment;
import org.mukdongjeil.mjchurch.common.photoview.PhotoViewAttacher;
import org.mukdongjeil.mjchurch.common.util.DisplayUtil;
import org.mukdongjeil.mjchurch.common.util.ImageUtil;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.common.util.SystemHelpers;
import org.mukdongjeil.mjchurch.protocol.RequestBaseTask;
import org.mukdongjeil.mjchurch.protocol.RequestImageTask;
import org.mukdongjeil.mjchurch.slidingmenu.MenuListFragment;

/**
 * Created by Kim SungJoong on 2015-08-24.
 */
public class TrainingFragment extends ImageBaseFragment{
    private static final String TAG = TrainingFragment.class.getSimpleName();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showLoadingDialog();
        }

        String requestUrl;
        boolean needListRequest = false;
        String title;

        Bundle args = getArguments();
        if (args == null) {
            Logger.e(TAG, "arguments is null");
            requestUrl = Const.TRAINING_HOME_URL;
            title = MenuListFragment.TRAINING_MENUS[0];
        } else {
            int selectedMenuIndex = args.getInt(MenuListFragment.SELECTED_MENU_INDEX);
            Logger.i(TAG, "selected sliding menu index : " + selectedMenuIndex);

            switch(selectedMenuIndex) {
                case 13:
                    requestUrl = Const.TRAINING_REARING_CLASS_URL;
                    title = MenuListFragment.TRAINING_MENUS[1];
                    break;
                case 14:
                    requestUrl = Const.TRAINING_MOTHER_WISE_URL;
                    title = MenuListFragment.TRAINING_MENUS[2];
                    break;
                case 15:
                    requestUrl = Const.TRAINING_DISCIPLE_URL;
                    title = MenuListFragment.TRAINING_MENUS[3];
                    break;
                case 12:
                default:
                    requestUrl = Const.TRAINING_BIBLE_STUDY_URL;
                    title = MenuListFragment.TRAINING_MENUS[0];
                    break;
            }
        }

        getActivity().setTitle(title);

        if (needListRequest == true) {
            Toast.makeText(getActivity(), "구현 준비 중입니다.", Toast.LENGTH_LONG).show();

        } else {
            new RequestImageTask(requestUrl, new RequestBaseTask.OnResultListener() {
                @Override
                public void onResult(Object obj, int position) {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).hideLoadingDialog();
                    }

                    if (obj != null && obj instanceof Bitmap) {
                        Bitmap resizedBitmap = ImageUtil.getResizeBitmapImage((Bitmap) obj, DisplayUtil.getDisplaySizeWidth(SystemHelpers.getApplicationContext()));
                        if (getImageView() != null) {
                            getImageView().setImageBitmap(resizedBitmap);
                            new PhotoViewAttacher(getImageView());
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
}
