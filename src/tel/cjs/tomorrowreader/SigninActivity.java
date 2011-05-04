package tel.cjs.tomorrowreader;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.util.Log;
import android.widget.EditText;   
import android.app.ProgressDialog;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.Intent;
import android.widget.Toast;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author CJ Slade
 * Авторизация
 */
public class SigninActivity extends Activity {
    
    public static final String TAG = "zvtra";
    
    private BookmarksDbAdapter mDbAdapter;

    ProgressDialog mDialog;    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);   
        Log.d(TAG, "Sign_in layout");       
        
        setContentView(R.layout.signin);
             
    }
    
    @Override
    public void onResume(){
        super.onResume();
                    
        Button loginButton = (Button) findViewById(R.id.login_submit_button);
        
        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doLogin();
            }
        });     
    }
    
    protected void doLogin(){
        Log.d(TAG, "loginButton clicked");
        
        mDialog = ProgressDialog.show(this, "", "Идет подключение", true);
        
        String email = ((EditText)findViewById(R.id.field_email)).getText().toString();  
        String password = ((EditText)findViewById(R.id.field_password)).getText().toString();  
        Log.d(TAG, "try login "+email+":" + password);       
        
 
        String requestUrl = "http://" + ZvtraConst.DOMAIN + "/api/get_key";
        
        List<NameValuePair> postParams = new ArrayList<NameValuePair>(2);
        postParams.add( new BasicNameValuePair( "email", email) );
        postParams.add( new BasicNameValuePair( "password", password));

        boolean success = false;
        String apiKey = "";
        
        
        try {
            JSONObject jsonObjRecv = HttpClient.SendHttpRequest(HttpClient.POST, requestUrl, postParams);        
            success = jsonObjRecv.getBoolean("success");
            if (success) {
                apiKey = jsonObjRecv.getJSONObject("data").getString("key");
                Log.d(TAG, apiKey);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        mDialog.dismiss();
        
        if (!success) {        
            CharSequence text = getString(R.string.auth_error);
            Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
            toast.show();
        } else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor prefsEditor = prefs.edit();
            prefsEditor.putString("apiKey", apiKey);
            prefsEditor.putString("email", email);
            prefsEditor.commit();
            clearDb();
            launchBookmarksManager();
        }
      
    }
    
    protected void clearDb(){
        mDbAdapter = new BookmarksDbAdapter(this);
        mDbAdapter.open();
        mDbAdapter.deleteAll();
    }
    
    protected void launchBookmarksManager() {
        Intent i = new Intent(this, BookmarksActivity.class);
        startActivity(i);
    }
}
