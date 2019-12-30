package roberta.heartbeepapp.adapters;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

import roberta.heartbeepapp.fragments.WearableDataFragment;

public class WearableViewPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<String> dataList;
    private ArrayList<String> nameList;

    public WearableViewPagerAdapter(FragmentManager fm, ArrayList<String> dataList, ArrayList<String> nameList) {
        super(fm);
        this.dataList = dataList;
        this.nameList = nameList;
    }

    @Override
    public Fragment getItem(int i) {
        return WearableDataFragment.newInstance(dataList.get(i));
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if(position < nameList.size()) return nameList.get(position);
        else return "";
    }
}
