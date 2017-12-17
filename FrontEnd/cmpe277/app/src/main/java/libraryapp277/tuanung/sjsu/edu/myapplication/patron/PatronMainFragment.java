package libraryapp277.tuanung.sjsu.edu.myapplication.patron;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import libraryapp277.tuanung.sjsu.edu.myapplication.R;
import libraryapp277.tuanung.sjsu.edu.myapplication.librarian.AddBookActivity;
import libraryapp277.tuanung.sjsu.edu.myapplication.librarian.LibrarianSearchActivity;

/**
 * Created by t0u000c on 12/16/17.
 */

public class PatronMainFragment extends Fragment {

    private AppCompatButton btnSearchBook;
    private AppCompatButton btnViewBorrowedBook;
    private AppCompatButton btnWaitListBook;
    public static String PATRON_FEATURE_EXTRA = "patronFeatureExtra";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_patron_main, parent, false);

        btnSearchBook = (AppCompatButton) v.findViewById(R.id.appCompatButtonSearchBorrowedBook);
        btnViewBorrowedBook = (AppCompatButton) v.findViewById(R.id.appCompatButtonViewBorrowedBook);
        btnWaitListBook = (AppCompatButton) v.findViewById(R.id.appCompatButtonViewBookWaitingList);

        btnViewBorrowedBook.setOnClickListener(setUpListener(btnViewBorrowedBook.getText().toString()));
        btnSearchBook.setOnClickListener(setUpListener(btnSearchBook.getText().toString()));
        btnWaitListBook.setOnClickListener(setUpListener(btnWaitListBook.getText().toString()));

        return v;
    }

    public View.OnClickListener setUpListener(final String feature){

        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(getActivity(), PatronSearchBookActivity.class);
                mainIntent.putExtra(PATRON_FEATURE_EXTRA, feature);
                startActivity(mainIntent);
            }
        };
    }
}
