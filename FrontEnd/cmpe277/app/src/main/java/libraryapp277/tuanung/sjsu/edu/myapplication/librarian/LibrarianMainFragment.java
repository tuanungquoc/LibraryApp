package libraryapp277.tuanung.sjsu.edu.myapplication.librarian;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import libraryapp277.tuanung.sjsu.edu.myapplication.API;
import libraryapp277.tuanung.sjsu.edu.myapplication.MainActivity;
import libraryapp277.tuanung.sjsu.edu.myapplication.R;
import libraryapp277.tuanung.sjsu.edu.myapplication.generalutilities.SessionManagement;
import libraryapp277.tuanung.sjsu.edu.myapplication.networkutilities.NetworkSingleton;

/**
 * Created by t0u000c on 12/16/17.
 */

public class LibrarianMainFragment extends Fragment {

    private AppCompatButton btnSearchBook;
    private AppCompatButton btnAddBook;
    private AppCompatButton btnTest;
    private SessionManagement session ;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_librarian_main, parent, false);
        session = new SessionManagement(getActivity());

        btnSearchBook = (AppCompatButton) v.findViewById(R.id.appCompatButtonSearchBook);
        btnAddBook    = (AppCompatButton) v.findViewById(R.id.appCompatButtonAddBook);

        btnAddBook.setOnClickListener(setUpListener(btnAddBook.getText().toString()));
        btnSearchBook.setOnClickListener(setUpListener(btnSearchBook.getText().toString()));

        btnTest = (AppCompatButton) v.findViewById(R.id.appCompatButtonTesting);

        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.MyDialogTheme);
                builder.setTitle("Title");

                // Set up the input
                final EditText input = new EditText(getActivity());
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String days = input.getText().toString();
                        String url = API.CronJob + "/" + days;
                        volleyNetworkCronJob(url);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

            }
        });


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


    public void volleyNetworkCronJob(String url){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                JSONObject jsonObj = null;

                NetworkResponse networkResponse = error.networkResponse;
                if(networkResponse != null) {
                    try {
                        jsonObj = new JSONObject(new String(networkResponse.data));
                        Toast.makeText(getActivity(),jsonObj.getString("msg"),Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(getActivity(), "There is an error. Please contact admin for more info", Toast.LENGTH_LONG).show();
                    Log.e("Error",error.getMessage());
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                //headers.put("Content-Type", "application/json");
                String token = session.getSessionDetails().get(SessionManagement.KEY_TOKEN);
                headers.put("x-access-token", session.getSessionDetails().get(SessionManagement.KEY_TOKEN));
                headers.put("Content-Type","application/json");
                return headers;
            }
        };
        NetworkSingleton.get(getActivity()).addRequest(jsonObjectRequest, "Get Books");

    }
}
