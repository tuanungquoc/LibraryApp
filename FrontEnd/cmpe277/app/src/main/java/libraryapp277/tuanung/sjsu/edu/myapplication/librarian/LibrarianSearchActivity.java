package libraryapp277.tuanung.sjsu.edu.myapplication.librarian;

import android.support.v4.app.Fragment;

import libraryapp277.tuanung.sjsu.edu.myapplication.SingleFragmentActivity;

/**
 * Created by t0u000c on 12/16/17.
 */

public class LibrarianSearchActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new LibrarianSearchFragment();
    }
}
