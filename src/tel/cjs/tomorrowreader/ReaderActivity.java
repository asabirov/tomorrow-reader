package tel.cjs.tomorrowreader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.MenuInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;




public class ReaderActivity extends Activity {
    
    private static final String TAG = "zvtra";
    
    private BookmarksDbAdapter mDbAdapter;
    
    private Long mRowId;
    
    private Cursor mBookmark;
    
    private SharedPreferences mPrefs;
    
    @Override
    public void onCreate(Bundle bundle) {        
        Log.d(TAG, "reader layout");       
                                        
        mDbAdapter = new BookmarksDbAdapter(this);        
		mDbAdapter.open();
        
        super.onCreate(bundle);   
        setContentView(R.layout.reader);
		
		mRowId = null;
		Bundle extras = getIntent().getExtras();
		mRowId = (bundle == null) ? null : (Long) bundle.getSerializable("id");
		if (extras != null) {
			mRowId = extras.getLong("id");
		}
        
        mBookmark = mDbAdapter.fetch(mRowId);
        
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        
		populateFields();
      
        ImageButton homeButton = (ImageButton) findViewById(R.id.home_button);
        homeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "backButton clicked");                
                launchBookmarksManager(null);
            }
        });        
       
        ImageButton deleteButton = (ImageButton) findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "deleteButton clicked");                
                delete();
            }
        });  
        
        ImageButton gotoButton = (ImageButton) findViewById(R.id.original_button);
        gotoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "gotoButton clicked");                
                gotoOriginal();
            }
        });  
        
        ImageButton changeModeButton = (ImageButton) findViewById(R.id.mode_button);
        changeModeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "gotoButton clicked");                
                changeMode();
            }
        });  
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.reader_menu, menu);        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_goto) {
            gotoOriginal();
            return true;
        } else if (item.getItemId() == R.id.menu_delete) {
            deleteCurrent();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
    
    /**
     * Заполнение данными
     */
    protected void populateFields() {
        Log.d(TAG, "populateFields");
       
        if (mRowId != null) {

            startManagingCursor(mBookmark);			

            String title = mBookmark.getString(mBookmark.getColumnIndexOrThrow("title"));

            String text = mBookmark.getString(mBookmark.getColumnIndexOrThrow("content"));
            
            WebView contentText = (WebView) findViewById(R.id.bookmark_content);
            
            contentText.clearView();
            
            Reader reader = new Reader();
            
            reader.setMode(mPrefs.getString("mode", "dark"))
                  .setTitle(title)
                  .setText(text);
            
            contentText.setBackgroundColor(reader.getBackground());

            contentText.loadDataWithBaseURL(null, reader.getContent(), "text/html", "UTF-8", null);  
        }
       
    }
    
    /**
     * Удаление текущей записи
     */
    private void delete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_confirmation)
               .setCancelable(false)
               .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        mDbAdapter.setDeleted(mRowId);            
                        launchBookmarksManager(getString(R.string.deleted));
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                   }
               });
        AlertDialog alert = builder.create();        
        alert.show();
    }
    
    /**
     * Переход на оригинал
     */
    private void  gotoOriginal() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        
        String url = mBookmark.getString(mBookmark.getColumnIndexOrThrow("url"));
        
        i.setData(Uri.parse(url));
        startActivity(i);
    }
    
    /**
     * Удалить текуший текст
     */
    private void deleteCurrent(){
        mDbAdapter.setDeleted(mRowId);
        launchBookmarksManager(getString(R.string.deleted));
    }
    
    /**
     * Запуск Activity:BookmarksActivity
     */
    protected void launchBookmarksManager(String status) {
        Intent i = new Intent(this, BookmarksActivity.class);
        i.putExtra("status", status);
        startActivity(i);
    }
    
    /**
     * Изменение режима просмотра
     */
    
    protected void changeMode(){
        String mode = mPrefs.getString("mode", "dark");
        if (mode == "dark") {
            mode = "light";
        } else {
            mode = "dark";
        }
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        prefsEditor.putString("mode", mode);
        prefsEditor.commit();
        
        Log.d(TAG, "setMode "+ mode);
        Log.d(TAG, mPrefs.getString("mode", "dark"));
        populateFields();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBookmark.close();
        mDbAdapter.close();
    }
}
