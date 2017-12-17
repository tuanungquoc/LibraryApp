package libraryapp277.tuanung.sjsu.edu.myapplication;

import android.support.v4.app.Fragment;

/**
 * Created by t0u000c on 12/15/17.
 */

public class ConfirmationActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new ConfirmationFragment();
    }
}
