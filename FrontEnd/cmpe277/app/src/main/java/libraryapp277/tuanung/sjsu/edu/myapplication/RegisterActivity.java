package libraryapp277.tuanung.sjsu.edu.myapplication;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

/**
 * Created by t0u000c on 12/15/17.
 */

public class RegisterActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new RegisterFragment();
    }
}
