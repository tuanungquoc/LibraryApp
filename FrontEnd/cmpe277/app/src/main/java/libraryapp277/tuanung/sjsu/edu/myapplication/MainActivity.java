package libraryapp277.tuanung.sjsu.edu.myapplication;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import libraryapp277.tuanung.sjsu.edu.myapplication.generalutilities.SessionManagement;
import libraryapp277.tuanung.sjsu.edu.myapplication.librarian.LibrarianMainFragment;
import libraryapp277.tuanung.sjsu.edu.myapplication.librarian.LibrarianSearchFragment;
import libraryapp277.tuanung.sjsu.edu.myapplication.patron.PatronBookSearchFragment;
import libraryapp277.tuanung.sjsu.edu.myapplication.patron.PatronMainFragment;

public class MainActivity extends AppCompatActivity {

    private SessionManagement session;
    TextView welcomeText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        session = new SessionManagement(getApplicationContext());
        session.loginValidation();
        if(session.getSessionDetails().get(SessionManagement.KEY_USER_ID)!=null) {
            welcomeText = (TextView) findViewById(R.id.textViewWelcome);
            welcomeText.setText("Welcome:" + session.getSessionDetails().get(SessionManagement.KEY_USER_NAME));
            FragmentManager fm = getSupportFragmentManager();
            Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);
            if (fragment == null) {
                if (session.getSessionDetails().get(SessionManagement.KEY_USER_ROLE).equals("patron"))
                    fragment = new PatronMainFragment();
                else
                    fragment = new LibrarianMainFragment();
                fm.beginTransaction().add(R.id.fragmentContainer, fragment).commit();

            }
        }
        //Check to see if it is a lib or pat, then direct to the right activity

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                session.logoutSession();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
