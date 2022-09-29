package com.arindo.nura;

/**
 * Created by bmaxard on 14/01/2017.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                HistoryBooking tab1 = new HistoryBooking();
                return tab1;
            case 1:
                HistoryProgress tab2 = new HistoryProgress();
                return tab2;
            case 2:
                HistoryCompleted tab3 = new HistoryCompleted();
                return tab3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
