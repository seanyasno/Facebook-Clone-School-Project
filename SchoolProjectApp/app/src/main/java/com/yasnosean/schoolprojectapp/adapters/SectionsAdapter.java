package com.yasnosean.schoolprojectapp.adapters;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.yasnosean.schoolprojectapp.fragments.MoreOptionsPageFragment;
import com.yasnosean.schoolprojectapp.fragments.NotificationsPageFragment;
import com.yasnosean.schoolprojectapp.fragments.PostsPageFragment;

public class SectionsAdapter extends FragmentStatePagerAdapter {

    public SectionsAdapter(FragmentManager fm) {
        super(fm);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }

    @Override
    public Fragment getItem(int position) {
//        switch (position) {
//            case 0:
//                return new PostsPageFragment();
//            case 1:
//                return new NotificationsPageFragment();
//            case 2:
//                return new MoreOptionsPageFragment();
//        }
        switch (position) {
            case 0:
                return new PostsPageFragment();
            case 1:
                return new MoreOptionsPageFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 1;
    }

}
