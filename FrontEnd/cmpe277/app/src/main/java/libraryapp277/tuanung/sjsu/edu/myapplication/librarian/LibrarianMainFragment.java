package libraryapp277.tuanung.sjsu.edu.myapplication.librarian;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import libraryapp277.tuanung.sjsu.edu.myapplication.MainActivity;
import libraryapp277.tuanung.sjsu.edu.myapplication.R;

/**
 * Created by t0u000c on 12/16/17.
 */

public class LibrarianMainFragment extends Fragment {

    private AppCompatButton btnSearchBook;
    private AppCompatButton btnAddBook;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_librarian_main, parent, false);

        btnSearchBook = (AppCompatButton) v.findViewById(R.id.appCompatButtonSearchBook);
        btnAddBook    = (AppCompatButton) v.findViewById(R.id.appCompatButtonAddBook);

        btnAddBook.setOnClickListener(setUpListener(btnAddBook.getText().toString()));
        btnSearchBook.setOnClickListener(setUpListener(btnSearchBook.getText().toString()));

        return v;
    }

    public View.OnClickListener setUpListener(String feature){
        Class myClass = null;
        switch(feature){
            case "Search Book":
                myClass = LibrarianSearchActivity.class;
                break;
            case "Add Book":
                myClass = AddBookActivity.class;
                break;
        }
        final Class finalMyClass = myClass;
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(getActivity(), finalMyClass);
                startActivity(mainIntent);
            }
        };
    }
}
