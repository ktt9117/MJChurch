package org.mukdongjeil.mjchurch.fragments;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.common.Const;
import org.mukdongjeil.mjchurch.common.util.Logger;
import org.mukdongjeil.mjchurch.models.Sermon;
import org.mukdongjeil.mjchurch.sermon.ListPlayerController;
import org.mukdongjeil.mjchurch.service.MediaService;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

/**
 * Created by Kim SungJoong on 2015-07-31.
 */
public class SermonPagerFragment extends BaseFragment implements ListPlayerController.NetworkAlertListener {
    private static final String TAG = SermonPagerFragment.class.getSimpleName();

    public interface SermonSelectedListener {
        void onItemSelected(Sermon sermon);
    }

    // Sermon menu names
    private static final String[] SERMON_TAP_NAMES = { "주일 오전예배", "주일 오후예배", "수요예배", "금요기도회" };

    private ViewPager pager;
    private TabLayout tabs;

    private ListPlayerController mPlayerController;
    private MediaService mService;

    private Realm realm;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logger.d(TAG, "[MediaService] onServiceConnected");
            mService = ((MediaService.LocalBinder)service).getService();
            ((MediaService.LocalBinder)service).setMediaStatusChangedListener(new MediaService.MediaStatusChangedListener() {
                @Override
                public void onStatusChanged(int status, Sermon item) {
                    if (mPlayerController != null) {
                        mPlayerController.updatePlayerControllerIfNecessary(item);
                    }
                }
            });
            if (mPlayerController != null) {
                mPlayerController.setMediaService(mService);
                mPlayerController.updatePlayerControllerIfNecessary(mService.getCurrentPlayerItem());
            } else {
                Logger.e(TAG, "cannot set ListPlayerController info caused by ListPlayerController is not initialized yet!");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logger.d(TAG, "[MediaService] onServiceDisconnected");
            mService = null;
        }
    };

    public SermonPagerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        realm = Realm.getDefaultInstance();

        View v = inflater.inflate(R.layout.fragment_pager_sermon, container, false);
        pager = (ViewPager) v.findViewById(R.id.viewpager);
        tabs = (TabLayout) v.findViewById(R.id.tabs);

        mPlayerController = new ListPlayerController(getActivity(), v, this);
        mPlayerController.setMediaServiceConnection(mServiceConnection);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle("예배와 말씀");

        SermonPagerAdapter adapter = new SermonPagerAdapter(getChildFragmentManager());

        int tapSize = SERMON_TAP_NAMES.length;
        for (int i = 0; i < tapSize; i++) {
            SermonFragment fragment = new SermonFragment();
            fragment.setSermonSelectedListener(new SermonSelectedListener() {
                @Override
                public void onItemSelected(Sermon sermon) {
                    mPlayerController.setPlayerControllerVisibility(View.VISIBLE);
                    Sermon standaloneItem = realm.copyFromRealm(sermon);
                    mPlayerController.updatePlayerInfo(standaloneItem);
                }
            });
            Bundle args = new Bundle();
            args.putInt(Const.INTENT_KEY_SELECTED_MENU_INDEX, 7 + i);
            fragment.setArguments(args);
            adapter.addFragment(fragment, SERMON_TAP_NAMES[i]);
        }

        mPlayerController.setPlayerControllerVisibility(View.GONE);
        tabs.setupWithViewPager(pager);
        pager.setAdapter(adapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Intent service = new Intent(getActivity(), MediaService.class);
        getActivity().startService(service);
        getActivity().bindService(service, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getActivity().unbindService(mServiceConnection);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        realm.close();
    }

    @Override
    public void onNetworkNotConnected() {
        Toast.makeText(getActivity(),
                "네트워크에 연결되어 있지 않습니다. 와이파이 또는 데이터 네트워크 연결 후 다시 시도해주세요",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDataAlert(final boolean isDownloadAction) {
        AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
        ab.setTitle("경고");
        ab.setCancelable(false);
        ab.setMessage("와이파이에 연결되어 있지 않습니다. 이대로 진행할 경우 가입하신 요금제에 따라 추가 " +
                "요금이 부과될 수도 있습니다.\n(와이파이 환경에서 이용하시길 권장합니다.)\n " +
                "계속 진행 하시겠습니까?");

        ab.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isDownloadAction) {
                    mPlayerController.downloadCurrentItem();

                } else {
                    mPlayerController.playCurrentItem();
                }
            }
        });

        ab.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        ab.create().show();
    }

    static class SermonPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public SermonPagerAdapter(FragmentManager fm) {
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