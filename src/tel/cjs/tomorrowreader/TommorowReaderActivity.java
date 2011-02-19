/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tel.cjs.tomorrowreader;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
/**
 *
 * @author CJ Slade
 */
public class TommorowReaderActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);       
        
        setContentView(R.layout.login);
       /* 
        Button loginButton = (Button)findViewById(R.id.button_login);
        loginButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doLogin();
            }
        });    */    
    }
}
