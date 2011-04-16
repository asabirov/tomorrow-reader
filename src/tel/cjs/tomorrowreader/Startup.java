package tel.cjs.tomorrowreader;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.preference.PreferenceManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
/**
 * @author CJ Slade
 * Стартовое окно
 */
public class Startup extends Activity {
    
    public static final String TAG = "zvtra";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);               
        setContentView(R.layout.main);
        
        Log.d(TAG, "Startup activity");
        
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);        
                
    }
    @Override
    public void onResume(){
        super.onResume();
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        Button registrationButton = (Button) findViewById(R.id.registration_button);        
        registrationButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                launchRegistration();
            }
        });   
        
        Button signinButton = (Button) findViewById(R.id.main_signin_button);        
        signinButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                launchSignin();
            }
        });   
        
        Button signinAsButton = (Button) findViewById(R.id.signin_as_button);        
        signinAsButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                launchBookmarksManager();
            }
        });
        
        if (prefs.getString("apiKey", "").length() == 0) {
            signinAsButton.setVisibility(4);
        } else {
            signinAsButton.setText(prefs.getString("email", ""));
        }
        
    }
    /**
     * Запуск авторизации
     */
    protected void launchSignin() {
        Intent i = new Intent(this, Signin.class);
        startActivity(i);
    }    
    /**
     * Регистрация
     */
    protected void launchRegistration(){
        String url = getString(R.string.registration_url);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
    /**
     * Запуск списка закладок
     */
    protected void launchBookmarksManager() {
        Intent i = new Intent(this, BookmarksManager.class);
        startActivity(i);
    }
}
