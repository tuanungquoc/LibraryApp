package libraryapp277.tuanung.sjsu.edu.myapplication.patron;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import libraryapp277.tuanung.sjsu.edu.myapplication.API;
import libraryapp277.tuanung.sjsu.edu.myapplication.Book;
import libraryapp277.tuanung.sjsu.edu.myapplication.R;
import libraryapp277.tuanung.sjsu.edu.myapplication.generalutilities.SessionManagement;
import libraryapp277.tuanung.sjsu.edu.myapplication.librarian.AddBookActivity;
import libraryapp277.tuanung.sjsu.edu.myapplication.librarian.LibrarianSearchActivity;
import libraryapp277.tuanung.sjsu.edu.myapplication.networkutilities.NetworkSingleton;

/**
 * Created by t0u000c on 12/16/17.
 */

public class PatronMainFragment extends Fragment {

    private AppCompatButton btnSearchBook;
    private AppCompatButton btnViewBorrowedBook;
    private AppCompatButton btnWaitListBook;
    private AppCompatButton btnReservationListBook;
    private AppCompatButton btnBalanceBook;
    private SessionManagement session;
    public static String PATRON_FEATURE_EXTRA = "patronFeatureExtra";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_patron_main, parent, false);
        session = new SessionManagement(getActivity());
        btnSearchBook = (AppCompatButton) v.findViewById(R.id.appCompatButtonSearchBorrowedBook);
        btnViewBorrowedBook = (AppCompatButton) v.findViewById(R.id.appCompatButtonViewBorrowedBook);
        btnWaitListBook = (AppCompatButton) v.findViewById(R.id.appCompatButtonViewBookWaitingList);
        btnReservationListBook = (AppCompatButton) v.findViewById(R.id.appCompatButtonViewBookReservedList);
        btnBalanceBook = (AppCompatButton) v.findViewById(R.id.appCompatButtonViewBalanceList);
        btnViewBorrowedBook.setOnClickListener(setUpListener(R.string.text_patron_view_borrowed_feature));
        btnSearchBook.setOnClickListener(setUpListener(R.string.text_patron_search_feature));
        btnWaitListBook.setOnClickListener(setUpListener(R.string.text_patron_view_waitlist_feature));
        btnReservationListBook.setOnClickListener(setUpListener(R.string.text_patron_view_reservation_feature));
        btnBalanceBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = API.GetBalance + "/" + session.getSessionDetails().get(SessionManagement.KEY_USER_ID);
                volleyNetworkToGetBalance(url);
            }
        });
        return v;
    }

    public View.OnClickListener setUpListener(final int feature){

        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(getActivity(), PatronSearchBookActivity.class);
                mainIntent.putExtra(PATRON_FEATURE_EXTRA, feature+"");
                startActivity(mainIntent);
            }
        };
    }

    public void volleyNetworkToGetBalance(String url){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int balance = response.getInt("msg");
                    new AlertDialog.Builder(getActivity(),R.style.MyDialogTheme)
                            .setTitle("Your total balance: $" + balance )
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            }).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
