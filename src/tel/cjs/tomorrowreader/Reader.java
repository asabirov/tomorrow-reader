package tel.cjs.tomorrowreader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.text.Html;
import android.view.MenuInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.net.URLEncoder;


public class Reader extends Activity {
    
    private static final String TAG = "zvtra";
    
    private BookmarksDbAdapter mDbAdapter;
    
    //private TextView mTitleTextView;
    
    private TextView mContentTextView;
        
    private Long mRowId;
    
    private Cursor mBookmark;
    
    @Override
    public void onCreate(Bundle bundle) {        
        Log.d(TAG, "reader layout");       
                                        
        mDbAdapter = new BookmarksDbAdapter(this);        
		mDbAdapter.open();
        
        super.onCreate(bundle);   
        setContentView(R.layout.reader);
		
		//mContentTextView = (TextView) findViewById(R.id.bookmark_content);

		mRowId = null;
		Bundle extras = getIntent().getExtras();
		mRowId = (bundle == null) ? null : (Long) bundle.getSerializable("id");
		if (extras != null) {
			mRowId = extras.getLong("id");
		}
        
        mBookmark = mDbAdapter.fetch(mRowId);
        
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
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.reader_menu, menu);        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
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
        try {
            if (mRowId != null) {
                                
                startManagingCursor(mBookmark);			

                String title = mBookmark.getString(mBookmark.getColumnIndexOrThrow("title"));

                //mTitleTextView.setText(title);

                String content = mBookmark.getString(mBookmark.getColumnIndexOrThrow("content"));
                
                WebView contentText = (WebView) findViewById(R.id.bookmark_content);
                contentText.setBackgroundColor(R.color.reader_background);
                
                String styles = "<style type=\"text/css\">"
                        + "body {background: #333; color: #fff;}"
                        + "h1 {font-size: 1.3em;}"
                        + "h2 {font-size: 1.1em;}"
                        + "h3 {font-size: 1.1em;}"
                        + "a {color: #76AD5D}"
                        + "a:visited{color: #eee}"
                        + "code {padding: 3px 5px;background: #222;color: #fff;}"
                        + "</style>";
                
                String data = "<html>";
                data += "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />";
                data += styles;
                data += "</head>";
                data += "<body>";
                data += content;
                data += "</body></html>";  
                contentText.loadDataWithBaseURL(null, data, "text/html", "UTF-8", null);  
            }
        } catch(Exception e) {
                e.printStackTrace();
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
     * Запуск Activity:BookmarksManager
     */
    protected void launchBookmarksManager(String status) {
        Intent i = new Intent(this, BookmarksManager.class);
        i.putExtra("status", status);
        startActivity(i);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBookmark.close();
        mDbAdapter.close();
    }
}
