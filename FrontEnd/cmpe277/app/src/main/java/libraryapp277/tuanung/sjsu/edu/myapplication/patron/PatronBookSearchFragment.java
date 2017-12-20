package libraryapp277.tuanung.sjsu.edu.myapplication.patron;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import libraryapp277.tuanung.sjsu.edu.myapplication.API;
import libraryapp277.tuanung.sjsu.edu.myapplication.Book;
import libraryapp277.tuanung.sjsu.edu.myapplication.R;
import libraryapp277.tuanung.sjsu.edu.myapplication.generalutilities.SessionManagement;
import libraryapp277.tuanung.sjsu.edu.myapplication.networkutilities.NetworkSingleton;

/**
 * Created by t0u000c on 12/16/17.
 */

public class PatronBookSearchFragment extends Fragment {
    private EditText searchPatronTextView;
    private Button searchSubmitBtn;
    private ListView searchPatronBookList;
    private Button cancelPatronBtn;
    //private ArrayAdapter<String> adapter;
    //private ArrayList<JSONObject> arrayList;
    private CustomPatronBorrowbaleBookListAdapter adapter;
    private String bookTitleSearched=null;
    private SessionManagement session;
    private Button patronCheckOutBtn;
    private String feature;
    private LinearLayout searchingLayout;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if(feature.equals(R.string.text_patron_search_feature+""))
            getActivity().getMenuInflater().inflate(R.menu.menu_patron_book_list_search,menu);
        if(feature.equals(R.string.text_patron_view_borrowed_feature +""))
            getActivity().getMenuInflater().inflate(R.menu.menu_patron_book_list_renew,menu);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case R.id.waiting:
                int position = ((AdapterView.AdapterContextMenuInfo)info).position;
                Log.d("Position", String.valueOf(position));
                Book temp=PatronBookListSingleton.get(getActivity()).getBooks().get(position);
                String url = API.BookWaitingList +
                             "/onBook/" + temp.getId() +
                             "/byPatron/" + session.getSessionDetails().get(SessionManagement.KEY_USER_ID) +
                             "/copies/" + temp.getCopies();
                JSONObject payload = new JSONObject();
                try {
                    payload.put("bookId", temp.getId());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                volleyNetworkBookWaitingList(url);
                return true;
            case R.id.renew:
                int position1 = ((AdapterView.AdapterContextMenuInfo)info).position;
                Log.d("Position", String.valueOf(position1));
                Book temp1=PatronBookListSingleton.get(getActivity()).getBooks().get(position1);
                String url1 = API.RenewBook +
                        "/" + temp1.getId() +
                        "/" + session.getSessionDetails().get(SessionManagement.KEY_USER_ID) ;
                JSONObject payload1 = new JSONObject();
                volleyNetworkBookRenew(url1,temp1);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_patron_borrowed_book_list, parent, false);
        Intent intent = getActivity().getIntent();
        feature = intent.getStringExtra(PatronMainFragment.PATRON_FEATURE_EXTRA);
        session = new SessionManagement(getActivity());
        searchPatronTextView = (EditText) v.findViewById(R.id.patronSearchTextView);
        searchSubmitBtn = (Button) v.findViewById(R.id.patronSearchBtn);
        searchPatronBookList = (ListView) v.findViewById(R.id.patronSearchBookList);
        cancelPatronBtn = (Button) v.findViewById(R.id.patronCancelBtn);
        searchingLayout = (LinearLayout) v.findViewById(R.id.patron_layout1);
        PatronBookListSingleton.get(getActivity()).clearBookList();
        adapter= new CustomPatronBorrowbaleBookListAdapter(PatronBookListSingleton.get(getActivity()).getBooks(),getActivity());
        searchPatronBookList.setAdapter(adapter);
        patronCheckOutBtn = (Button) v.findViewById(R.id.patronCheckOutBtn);
        if(!feature.equals(R.string.text_patron_view_waitlist_feature +""))
            registerForContextMenu(searchPatronBookList);

        if(!feature.equals(R.string.text_patron_search_feature +"")){

            searchingLayout.setVisibility(View.GONE);
            if(feature.equals(R.string.text_patron_view_borrowed_feature + "")) {
                patronCheckOutBtn.setText("Return");
                volleyNetworkToGetBookList(API.GetBorrowedBooks + "/" + session.getSessionDetails().get(SessionManagement.KEY_USER_ID));
            }else{
                patronCheckOutBtn.setVisibility(View.GONE);
                if(feature.equals(R.string.text_patron_view_waitlist_feature+""))
                    volleyNetworkToGetBookList(API.GetMyWaitList + "/" + session.getSessionDetails().get(SessionManagement.KEY_USER_ID));
                if(feature.equals(R.string.text_patron_view_reservation_feature +""))
                    volleyNetworkToGetBookList(API.ReserveBooks + "/" + session.getSessionDetails().get(SessionManagement.KEY_USER_ID));
            }
        }else{
            searchSubmitBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PatronBookListSingleton.get(getActivity()).clearBookList();
                    bookTitleSearched = searchPatronTextView.getText().toString();

                    // TODO GET LIBRARIAN ID FROM SESSION!!!!!!!!
                    String url = API.GetBooks+"?"+"title="+bookTitleSearched;
                    if(bookTitleSearched.equals(""))
                        url = API.GetBooks;
                    Log.d("URL", url);
                    volleyNetworkToGetBookList(url);
                    // this line adds the data of your EditText and puts in your array
                    //arrayList.add();
                    // next thing you have to do is check if your adapter has changed
                    //adapter.notifyDataSetChanged();
                }
            });
        }
        //registerForContextMenu(searchPatronBookList);

        patronCheckOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(feature.equals(R.string.text_patron_search_feature +"")) {
                    processBookForCheckingOut();
                }else{
                    processBookForReturn();
                }

            }
        });

        cancelPatronBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
        return v;
    }

    public void processBookForCheckingOut(){
        ArrayList<Book> bookSelectedList = new ArrayList<Book>();
        for (int i = 0; i < adapter.getCount(); i++) {
            Book book = adapter.getItem(i);
            if (book.getIsSelected()) {
                bookSelectedList.add(book);
            }
        }
        if (bookSelectedList.size() > 3) {
            Toast.makeText(getActivity(),
                    "You select more than 3 books",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (bookSelectedList.size() == 0) {
            Toast.makeText(getActivity(),
                    "Please select at least one book",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject payload = new JSONObject();
        try {
            payload.put("patronId", session.getSessionDetails().get(SessionManagement.KEY_USER_ID));
            for (int i = 0; i < bookSelectedList.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("bookId", bookSelectedList.get(i).getId());
                obj.put("title",bookSelectedList.get(i).getTitle());
                payload.put("book", obj);
                //call a network
                volleyNetworkToCheckoutBooks(API.CheckoutBooks, payload);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void processBookForReturn(){
        ArrayList<Book> bookSelectedList = new ArrayList<Book>();
        for (int i = 0; i < adapter.getCount(); i++) {
            Book book = adapter.getItem(i);
            if (book.getIsSelected()) {
                bookSelectedList.add(book);
            }
        }
        if (bookSelectedList.size() > 9) {
            Toast.makeText(getActivity(),
                    "You select more than 9 books",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (bookSelectedList.size() == 0) {
            Toast.makeText(getActivity(),
                    "Please select at least one book",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject payload = new JSONObject();
        try {
            payload.put("patronId", session.getSessionDetails().get(SessionManagement.KEY_USER_ID));
            JSONArray bookList = new JSONArray();
            for (int i = 0; i < bookSelectedList.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("bookId", bookSelectedList.get(i).getId());
                bookList.put(obj);
            }
            payload.put("books", bookList);
            //call a network
            volleyNetworkReturnBooks(API.ReturnBooks,payload);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    //Volley to Get Books data
    public void volleyNetworkToGetBookList(String url){
        PatronBookListSingleton.get(getActivity()).getBooks().clear();
        adapter.notifyDataSetChanged();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d("Response", response.toString());
                    JSONArray bookList = response.getJSONArray("books");
                    try {
                        for(int i = 0; i < bookList.length(); i++) {
                            JSONObject objects = bookList.getJSONObject(i);
                            if(feature.equals(R.string.text_patron_search_feature + "") ||
                                    feature.equals(R.string.text_patron_view_waitlist_feature + "") ||
                                        feature.equals(R.string.text_patron_view_reservation_feature + "") ) {
                                PatronBookListSingleton.get(getActivity()).addBook(new Book(objects.getString("_id"),
                                        objects.getString("author"),
                                        objects.getString("title"),
                                        objects.getString("callNumber"),
                                        objects.getString("publisher"),
                                        objects.getString("year"),
                                        objects.getString("location"),
                                        objects.getString("copies"),
                                        objects.getString("status"),
                                        objects.getString("keywords"),
                                        objects.getString("image")));
                            }
                            else{
                                PatronBookListSingleton.get(getActivity()).addBook(new BorrowedBook(objects.getString("_id"),
                                        objects.getString("author"),
                                        objects.getString("title"),
                                        objects.getString("callNumber"),
                                        objects.getString("publisher"),
                                        objects.getString("year"),
                                        objects.getString("location"),
                                        objects.getString("copies"),
                                        objects.getString("status"),
                                        objects.getString("keywords"),
                                        objects.getString("image"),
                                        objects.getString("dueDate"),
                                        session.getSessionDetails().get(SessionManagement.KEY_USER_ID)));
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (Exception ex) {
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

    //    Volley to Check out data
    public void volleyNetworkToCheckoutBooks(String url, JSONObject payload){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                url, payload , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d("Response", response.toString());
                    if(response.getBoolean("success")){
                        Toast.makeText(getActivity(),response.getString("msg"),Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ex) {
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
                        Toast.makeText(getActivity(), jsonObj.getString("msg"), Toast.LENGTH_LONG).show();
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
                headers.put("x-access-token", session.getSessionDetails().get(SessionManagement.KEY_TOKEN));
                headers.put("Content-Type","application/json");
                return headers;
            }
        };
        NetworkSingleton.get(getActivity()).addRequest(jsonObjectRequest, "Checkout Books");

    }

    // Volly to return books
    public void volleyNetworkReturnBooks(String url, JSONObject payload){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT,
                url, payload , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d("Response", response.toString());
                    if(response.getBoolean("success")){
                        Toast.makeText(getActivity(),response.getString("msg"),Toast.LENGTH_SHORT).show();
                        ArrayList<Book> bookSelectedList = new ArrayList<Book>();
                        for (int i = 0; i < adapter.getCount(); i++) {
                            Book book = adapter.getItem(i);
                            if (book.getIsSelected()) {
                                bookSelectedList.add(book);
                            }
                        }
                        PatronBookListSingleton.get(getActivity()).getBooks().removeAll(bookSelectedList);
                        adapter.notifyDataSetChanged();
                        //volleyNetworkToGetBookList(API.GetBorrowedBooks + "/" + session.getSessionDetails().get(SessionManagement.KEY_USER_ID));
                    }
                } catch (Exception ex) {
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
                        Toast.makeText(getActivity(), jsonObj.getString("msg"), Toast.LENGTH_LONG).show();
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
                headers.put("x-access-token", session.getSessionDetails().get(SessionManagement.KEY_TOKEN));
                headers.put("Content-Type","application/json");
                return headers;
            }
        };
        NetworkSingleton.get(getActivity()).addRequest(jsonObjectRequest, "Return Books");

    }

    public void volleyNetworkBookWaitingList(String url){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                url, new JSONObject() , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d("Response", response.toString());
                    if(response.getBoolean("success")){
                        Toast.makeText(getActivity(),response.getString("msg"),Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ex) {
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
                        Toast.makeText(getActivity(), jsonObj.getString("msg"), Toast.LENGTH_LONG).show();
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
                headers.put("x-access-token", session.getSessionDetails().get(SessionManagement.KEY_TOKEN));
                headers.put("Content-Type","application/json");
                return headers;
            }
        };
        NetworkSingleton.get(getActivity()).addRequest(jsonObjectRequest, "WaitingList Books");

    }

    public void volleyNetworkBookRenew(String url,final Book book){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT,
                url, new JSONObject() , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d("Response", response.toString());
                    if(response.getBoolean("success")){
                        if(book instanceof BorrowedBook){
                            ((BorrowedBook)book).setDueDate(response.getString("msg"));
                        }
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getActivity(),"Renew successfully!",Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ex) {
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

                        Toast.makeText(getActivity(), jsonObj.getString("msg"), Toast.LENGTH_LONG).show();
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
                headers.put("x-access-token", session.getSessionDetails().get(SessionManagement.KEY_TOKEN));
                headers.put("Content-Type","application/json");
                return headers;
            }
        };
        NetworkSingleton.get(getActivity()).addRequest(jsonObjectRequest, "Renew Books");

    }


}
