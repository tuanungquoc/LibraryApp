package libraryapp277.tuanung.sjsu.edu.myapplication;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.UUID;

/**
 * Created by t0u000c on 12/15/17.
 */

public class LoginActivity extends SingleFragmentActivity {


    @Override
    protected Fragment createFragment() {
        return new LoginFragment();
    }
}
