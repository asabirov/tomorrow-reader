/**
 * @author CJ Slade
 * Управление закладками
 */
package tel.cjs.tomorrowreader;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.util.Log;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Menu;
import android.view.MotionEvent;
import android.content.Context;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

public class BookmarksManager extends ListActivity implements Runnable{

    public static final String TAG = "zvtra";

    private BookmarksDbAdapter mDbAdapter;
    
    private SharedPreferences mPrefs;
    
    private ProgressDialog mProgressDialog;
    
    private Cursor mCursor;
    
    private Dialog mDialog;
    
    private static final int ACTIVITY_READER = 1;
    
    private static final int ACTIVITY_BROWSER = 1;
    
    @Override
    public void onCreate(Bundle bundle) {
        Log.d(TAG, "BookmarksManager activity");
               
        super.onCreate(bundle);      
        
        setContentView(R.layout.bookmarks);     
        
        mDbAdapter = new BookmarksDbAdapter(this);
		mDbAdapter.open();
        
        ImageButton refreshButton = (ImageButton) findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "refreshButton clicked");                
                synchronize();                
            }
        });        
        
        ImageButton addButton = (ImageButton) findViewById(R.id.add_button);
        addButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "addButton clicked");                
                openAddWindow();                
            }
        });        
        
        
        if (!mDbAdapter.dataExists()) {
            Log.d(TAG, "sync"); 
            synchronize();
        }
        
        
        String status = null;
		Bundle extras = getIntent().getExtras();
		status = (bundle == null) ? null : (String) bundle.getSerializable("status");
		if (extras != null) {
			status = extras.getString("status");
            if (status != null) {
                CharSequence text = status;
                Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
                toast.show(); 
            }
		}
        
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        if (mDbAdapter != null) {
            mDbAdapter = new BookmarksDbAdapter(this);
            mDbAdapter.open();
        }
        
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        
        
        
        //TextView titleTextView = (TextView) findViewById(R.id.top_bar_email);
        //titleTextView.setText(mPrefs.getString("email", ""));
        
        updateList();             
    }
    
    /**
     * Синхронизация
     */
    private void synchronize(){
        mProgressDialog = ProgressDialog.show(this, "",  getString(R.string.sync_in_progress), true);
        
        Thread thread = new Thread(this);
        thread.start();
    }
    /**
     * Запуск потока
     */
    public void run() {
        BookmarksDbAdapter db = new BookmarksDbAdapter(this);
		db.open();
        db.synchronize();
                
        handler.sendEmptyMessage(0);
    }
    /**
     * Обработчик
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mProgressDialog.dismiss();
            CharSequence text = getString(R.string.updated);
            Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
            toast.show();
            updateList(); 
        }
    };  
    /**
     * Окно добавления закладки
     */
    private void openAddWindow() {
        mDialog = new Dialog(this);        
        mDialog.setContentView(R.layout.add_dialog);
        mDialog.setCancelable(true);        
        mDialog.show();
        
        Button submitButton = (Button) mDialog.findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "submitButton clicked");                
                
                TextView urlTextView = (TextView) mDialog.findViewById(R.id.field_url);
                sendNewUrl(urlTextView.getText().toString());
                mDialog.dismiss();
            }
        });             
    }
    private void sendNewUrl(String url){
        mDbAdapter.sendNewUrl(url);
        synchronize();
    }
    /**
     * Обновление списка закладок
     */
    private void updateList() {
        mCursor = mDbAdapter.fetchAll();
                
		startManagingCursor(mCursor);

		String[] from = new String[] { "title" };
		int[] to = new int[] { R.id.title };

		SimpleCursorAdapter bookmarks = new SimpleCursorAdapter(this,
			R.layout.bookmark_row, mCursor, from, to);
		setListAdapter(bookmarks);        
    
    }   
    
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
        mDbAdapter.close();
        super.onListItemClick(l, v, position, id);
        
		Intent i = new Intent(this, Reader.class);
		i.putExtra("id", id);
        
        startActivity(i);                
	}      
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDbAdapter.close();
    }
}
