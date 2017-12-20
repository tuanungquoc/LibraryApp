package libraryapp277.tuanung.sjsu.edu.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import libraryapp277.tuanung.sjsu.edu.myapplication.generalutilities.InputUtilities;
import libraryapp277.tuanung.sjsu.edu.myapplication.generalutilities.SessionManagement;
import libraryapp277.tuanung.sjsu.edu.myapplication.networkutilities.NetworkSingleton;


/**
 * Created by t0u000c on 12/15/17.
 */

public class LoginFragment  extends Fragment {
    private TextInputLayout txtInputLayoutEmail;
    private TextInputEditText txtInputEmail;

    private TextInputLayout txtInputLayoutPassword;
    private TextInputEditText txtInputPassword;

    private AppCompatButton btnLogin;

    private AppCompatTextView register;

    private InputUtilities inputUtils;

    private SessionManagement session;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, parent, false);
        //Init all controls on login fragment
        session = new SessionManagement(getActivity());
        inputUtils = new InputUtilities(getActivity());
        txtInputLayoutEmail = (TextInputLayout) v.findViewById(R.id.textInputLayoutEmail);
        txtInputEmail = (TextInputEditText) v.findViewById(R.id.textInputEditTextEmail);

        txtInputLayoutPassword = (TextInputLayout) v.findViewById(R.id.textInputLayoutPassword);
        txtInputPassword = (TextInputEditText) v.findViewById(R.id.textInputEditTextPassword);

        btnLogin = (AppCompatButton) v.findViewById(R.id.appCompatButtonLogin);
        register = (AppCompatTextView) v.findViewById(R.id.textViewLinkRegister);

        //Add listerer for login and register btn
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //verify email and password
                if (inputUtils.isTxtBoxEmpty(txtInputEmail, txtInputLayoutEmail, "Email cannot be empty") ||
                        inputUtils.isTxtBoxEmpty(txtInputPassword, txtInputLayoutPassword, "Password cannot be empty")) {
                    return;
                } else if (!inputUtils.isEmail(txtInputEmail, txtInputLayoutEmail, "Email is not the right format")) {
                    return;
                } else {
                    loginUser(txtInputEmail.getText().toString(), txtInputPassword.getText().toString());
                }

            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to RegisterActivity
                Intent intentRegister = new Intent(getActivity(), RegisterActivity.class);
                startActivity(intentRegister);
            }
        });
        return v;
    }

    private void loginUser(final String email, final String password) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("email", email);
            payload.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                API.logInURL(), payload,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject object = new JSONObject(response.toString());
                            boolean success = object.getBoolean(("success"));
                            if (success) {
                                String email = object.getString("email");
                                String token = object.getString("token");
                                String userID = object.getString("userID");
                                String userRole = object.getString("role");
                                String name = object.getString("name");
                                session.saveLoginSession(email, token, userID, userRole,name);
                                Intent i = new Intent(getActivity(), MainActivity.class);
                                startActivity(i);
                                getActivity().finish();
                            } else {
                                Toast.makeText(getActivity(), object.getString("msg"), Toast.LENGTH_LONG).show();
                            }

                        } catch (Exception ex) {
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        JSONObject jsonObj = null;

                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null) {
                            try {
                                jsonObj = new JSONObject(new String(networkResponse.data));
                                Toast.makeText(getActivity(), jsonObj.getString("msg"), Toast.LENGTH_LONG).show();
                                if (jsonObj.getString("type").equals("not-verified")) {
                                    Intent intentConfirmation = new Intent(getActivity(), ConfirmationActivity.class);
                                    intentConfirmation.putExtra(ConfirmationFragment.EXTRA_CONFIRM_EMAIL, txtInputEmail.getText().toString());
                                    startActivity(intentConfirmation);
                                    getActivity().finish();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(getActivity(), "There is an error. Please contact admin for more info", Toast.LENGTH_LONG).show();

                        }
                    }
                });
        NetworkSingleton.get(getActivity()).addRequest(jsonObjectRequest, "City View Header Current Date");
    }
}
