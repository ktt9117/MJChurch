package org.mukdongjeil.mjchurch.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.models.ImagePageUrl;
import org.mukdongjeil.mjchurch.protocol.RequestBaseTask;
import org.mukdongjeil.mjchurch.protocol.RequestImageURLsTask;
import org.mukdongjeil.mjchurch.service.DataService;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Kim SungJoong on 2015-07-31.
 */
public class ImagePagerFragment extends BaseFragment {
    private static final String TAG = ImagePagerFragment.class.getSimpleName();

    private ViewPager pager;
    private TabLayout tabs;

    private Realm realm;

    public ImagePagerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pager, container, false);
        pager = (ViewPager) v.findViewById(R.id.viewpager);
        tabs = (TabLayout) v.findViewById(R.id.tabs);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle == null) {
            Logger.e(TAG, "bundle is null");
            return;
        }

        final int pageType = bundle.getInt(Const.INTENT_KEY_PAGE_TYPE);
        final String[] pageTitles = bundle.getStringArray(Const.INTENT_KEY_PAGE_TITLES);
        final String[] pageUrls = bundle.getStringArray(Const.INTENT_KEY_PAGE_URLS);

        getActivity().setTitle(pageType == Const.PAGE_TYPE_INTRODUCE ? "환영합니다" : "양육과 훈련");

        showLoadingDialog();

        // 하루 한 번만 새로 요청하기 위함
        RealmResults<ImagePageUrl> localPageUrls = DataService.getImagePageUrls(realm, pageType);
        Logger.e(TAG, "localPageUrls.size() : " + localPageUrls.size());

        if (localPageUrls.size() != 0) {
            closeLoadingDialog();

            ImagePagerAdapter adapter = new ImagePagerAdapter(getChildFragmentManager());
            int cnt = 0;
            for (ImagePageUrl pageUrl : localPageUrls) {
                ImageFragment fragment = new ImageFragment();
                fragment.setImgUrl(pageUrl.url);
                adapter.addFragment(fragment, pageUrl.title);
                cnt++;
            }

            tabs.setupWithViewPager(pager);
            pager.setAdapter(adapter);

        } else {

            new RequestImageURLsTask(new RequestBaseTask.OnResultListener() {
                @Override
                public void onResult(Object obj, int position) {
                    closeLoadingDialog();

                    List<String> imgUrlLists = (List<String>) obj;
                    if (imgUrlLists != null && imgUrlLists.size() > 0) {
                        Logger.d(TAG, "imgUrlLists size : " + imgUrlLists.size());
                        ImagePagerAdapter adapter = new ImagePagerAdapter(getChildFragmentManager());
                        int urlSize = imgUrlLists.size();
                        for (int i = 0; i < urlSize; i++) {
                            String imgUrl = imgUrlLists.get(i);
                            ImageFragment fragment = new ImageFragment();
                            fragment.setImgUrl(imgUrl);
                            adapter.addFragment(fragment, pageTitles[i]);

                            final ImagePageUrl url = new ImagePageUrl(pageType, pageTitles[i], imgUrl);
                            DataService.insertToRealm(realm, url);
                        }

                        tabs.setupWithViewPager(pager);
                        pager.setAdapter(adapter);

                    } else {
                        Logger.d(TAG, "cannot get imgUrlLists");
                    }

                }
            }).execute(pageUrls);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    static class ImagePagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ImagePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mFragmentTitleList.size();
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }
    }
}