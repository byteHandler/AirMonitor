package nik.sociomind.airmonitor;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class pastData_adapter extends FragmentPagerAdapter {
    public pastData_adapter(FragmentManager fragmentManager){
        super(fragmentManager);
    }
    @Override
    public Fragment getItem(int i) {
        Fragment fragment = null;
        switch (i){
            case 0:
                fragment = new PM25_fragment();
                break;
            case 1:
                fragment = new PM10_fragment();
                break;
            case 2:
                fragment = new gas_fragment();
                break;
            case 3:
                fragment = new lpg_fragment();
                break;
            case 4:
                fragment = new temp_fragment();
                break;
            case 5:
                fragment = new humid_fragment();
                break;
        }
        return fragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String title = null;
        switch (position){
            case 0 :
                title = "PM 2.5";
                break;
            case 1:
                title = "PM 10";
                break;
            case 2:
                title = "Gas / MQ135";
                break;
            case 3:
                title = "LPG / MQ02";
                break;
            case 4:
                title = "Temp";
                break;
            case 5:
                title = "Humid";
                break;
        }
        return title;
    }

    @Override
    public int getCount() {
        return 6;
    }
}
