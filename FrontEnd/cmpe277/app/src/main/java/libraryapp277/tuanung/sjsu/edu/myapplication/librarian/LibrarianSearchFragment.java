package libraryapp277.tuanung.sjsu.edu.myapplication.librarian;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import libraryapp277.tuanung.sjsu.edu.myapplication.API;
import libraryapp277.tuanung.sjsu.edu.myapplication.Book;
import libraryapp277.tuanung.sjsu.edu.myapplication.MainActivity;
import libraryapp277.tuanung.sjsu.edu.myapplication.R;
import libraryapp277.tuanung.sjsu.edu.myapplication.generalutilities.SessionManagement;
import libraryapp277.tuanung.sjsu.edu.myapplication.networkutilities.NetworkSingleton;


/**
 * Created by t0u000c on 12/16/17.
 */

public class LibrarianSearchFragment extends Fragment {

    private EditText book;
    private Button submit;
    private ListView listView;
    private CustomLibrarianBookListAdapter adapter;
    private Button cancel ;
    RequestQueue queue;
    private String bookTitleSearched=null;
    SessionManagement session;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_librarian_search, parent, false);
        book = (EditText) v.findViewById(R.id.textview);
        submit = (Button) v.findViewById(R.id.submit);
        listView = (ListView) v.findViewById(R.id.list);
        cancel = (Button) v.findViewById(R.id.librarianSearchCancel);
        registerForContextMenu(listView);
        BookListSingleton.get(getActivity()).clearBookList();
        queue = Volley.newRequestQueue(getActivity());
        adapter= new CustomLibrarianBookListAdapter(BookListSingleton.get(getActivity()).getBooks(),getActivity());
        listView.setAdapter(adapter);
        session = new SessionManagement(getActivity());

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bookTitleSearched = book.getText().toString();
                // TODO GET LIBRARIAN ID FROM SESSION!!!!!!!!
                String url = API.GetBooks+"?"+"title="+bookTitleSearched+"&enteredBy="+session.getSessionDetails().get(SessionManagement.KEY_USER_ID);
                Log.d("URL", url);
                volleyJsonArrayRequest(url);
                // this line adds the data of your EditText and puts in your array
                //arrayList.add();
                // next thing you have to do is check if your adapter has changed
                //adapter.notifyDataSetChanged();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(getActivity(), MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                getActivity().finish();
                startActivity(mainIntent);
            }
        });
        return v;
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.list) {
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.menu_list, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case R.id.edit:
                int pos = ((AdapterView.AdapterContextMenuInfo)info).position;
                Log.d("Position", String.valueOf(pos));
                Book tempedit=BookListSingleton.get(getActivity()).getBooks().get(pos);
                Intent editIntent = new Intent(getActivity(), AddBookActivity.class);
                editIntent.putExtra("Objectpos", pos);
                editIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(editIntent);
                return true;
            case R.id.delete:
                int position = ((AdapterView.AdapterContextMenuInfo)info).position;
                Log.d("Position", String.valueOf(position));
                Book temp=BookListSingleton.get(getActivity()).getBooks().get(position);
                String url = API.DeleteBooks;
                JSONObject payload = new JSONObject();
                try {
                    payload.put("bookId", temp.getId());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                volleyStringRequest(url, payload);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


    public void volleyJsonArrayRequest(String url){
        BookListSingleton.get(getActivity()).clearBookList();
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
                            BookListSingleton.get(getActivity()).addBook(new Book(objects.getString("_id"),
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

    //Volley to Delete Book data
    public void volleyStringRequest(String url,JSONObject payload){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                url, payload , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d("Response", response.toString());
                    if(response.getBoolean("success")){
                        Toast.makeText(getActivity(),response.getString("msg"),Toast.LENGTH_SHORT).show();
                        String URL = API.GetBooks + "?" + "title="+bookTitleSearched+"&enteredBy="+session.getSessionDetails().get(SessionManagement.KEY_USER_ID);
                        volleyJsonArrayRequest(URL);
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
        NetworkSingleton.get(getActivity()).addRequest(jsonObjectRequest, "Delete Book");

    }
}
