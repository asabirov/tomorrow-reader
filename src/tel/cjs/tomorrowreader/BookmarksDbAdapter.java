/**
 * Адаптер для списка закладок
 */
package tel.cjs.tomorrowreader;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.preference.PreferenceManager;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BookmarksDbAdapter {
    
    private Context context;
    
    private BookmarksDbHelper dh;   
    
    private SharedPreferences mPrefs;
    
    private static String TAG = "zvtra";
    
    BookmarksDbAdapter(Context context) {        
        this.context = context;
    }
    /**
     * Подключение к БД
     * @return BookmarksDbAdapter
     */
    public BookmarksDbAdapter open() throws SQLException  {
        dh = new BookmarksDbHelper(context);
        
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        
        return this;
    }    
    /**
     * Добавление записи
     * @param category
     * @param summary
     * @param description
     * @return boolean
     */
    public boolean createBookmark(String externalId, String url, String title, String domain, Integer contentExists, String content, Integer createdAt) {
		if (!dh.existsByExternalId(externalId)) {
            dh.insertRow(externalId, url, title, domain, contentExists, content, createdAt);
            return true;
        } else {
            return false;
        }        
	}
    /**
     * Обновление текста
     * @param content 
     * @param id
     */
    public void updateContent(String content, Long id) {
        dh.updateContent(content, id);
    }
    /**
     * Удаление всех записей
     * @return void
     */
    public void deleteAll() {
		dh.deleteAll();
	} 
    /**
     * Удаление закладки
     * @param id Integer
     * @return boolean
     */
    public boolean delete(Long id) {
		return dh.delete(id);
	}
    /**
     * Список всех закладок
     * @return Cursor
     */
    public Cursor fetchAll() {
        Cursor cursor = dh.fetchAll();
        return cursor;
    }
    /**
     * Возвращает запись
     * @param id Integer
     * @return Cursor
     */
    public Cursor fetch(Long id) {
        Cursor row = dh.fetchRow(id);
        return row;
    }
    /**
     * Закрытие базы
     * @return void
     */
    public void close(){
        dh.close();
    }
   
    /**
     * Наличие данных в базе
     * @return Cursor
     */
    public Boolean dataExists() {
        return dh.dataExists();
    }
    /**
     * Помечаем удаленным
     * @param id Long
     */
    public void setDeleted(Long id) {
        dh.setDeleted(id);
    }
    /**
     * Синхронизация закладок с сервером
     * @return void
     */    
    public void synchronize() {
        syncDeleted();
        syncBookmarks();
        syncContents();       
    }
    /**
     * Удаление закладок на сервере
     */
    private void syncDeleted() {
        Boolean success;
        String externalId;
        Long id ;
        JSONObject jsonObjRecv;
    
        Cursor cursor = dh.fetchDeleted();
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    externalId = cursor.getString(cursor.getColumnIndex("external_id"));
                    id = cursor.getLong(cursor.getColumnIndex("_id"));
                    Log.d(TAG, "try to delete " + externalId);
                    
                    String requestUrl = "http://" + ZvtraConst.DOMAIN + "/bookmarks/" + externalId;

                    List<NameValuePair> getParams = new ArrayList<NameValuePair>(1);
                    getParams.add(new BasicNameValuePair("api_key", mPrefs.getString("apiKey", "")));                     

                    jsonObjRecv = HttpClient.SendHttpRequest(HttpClient.DELETE, requestUrl, getParams);        

                    success = jsonObjRecv.getBoolean("success");

                    if (success) {
                        JSONObject data =  jsonObjRecv.getJSONObject("data");
                        dh.delete(id);
                    }                 
                } while (cursor.moveToNext());
                
            }
       } catch (JSONException e) {
           e.printStackTrace();
       } 
    }
    /**
     * Загружаем список закладок
     */
    private void syncBookmarks() {
        String requestUrl = "http://" + ZvtraConst.DOMAIN + "/bookmarks";
        
        List<NameValuePair> postParams = new ArrayList <NameValuePair>(2);
        postParams.add(new BasicNameValuePair("api_key", mPrefs.getString("apiKey", "")));
        postParams.add(new BasicNameValuePair("limit", "500"));
        
        
        boolean success = false;
        
        try {
            JSONObject jsonObjRecv = HttpClient.SendHttpRequest(HttpClient.GET, requestUrl, postParams);        
            success = jsonObjRecv.getBoolean("success");
            if (success) {
                
                JSONArray data =  jsonObjRecv.getJSONArray("data");
                ArrayList<String> exists = new ArrayList<String>();
                JSONObject row;
                
                if (data != null) {                     
                    for (int i=0;i<data.length();i++){ 
                        row = data.getJSONObject(i);     
                        exists.add(row.getString("id"));
                        createBookmark(row.getString("id"), row.getString("url"), row.getString("title"), 
                                row.getString("domain"), (row.getBoolean("content_exists") ? 1 : 0), "", row.getInt("created_at"));                        
                    }
                    dh.deleteList(exists);
                } 
            }
            
        } catch (JSONException e) {
            e.printStackTrace();
        }                               
    }
    /**
     * Загрузка текстов
     * @return void
     */
    private void syncContents() {
        Boolean success;
        String externalId;
        Long id ;
        JSONObject jsonObjRecv;
    
        Cursor cursor = dh.fetchWithoutContent();
        try {
            if (cursor != null && cursor.moveToFirst()) {
                
                do {
                    externalId = cursor.getString(cursor.getColumnIndex("external_id"));
                    id = cursor.getLong(cursor.getColumnIndex("_id"));

                    String requestUrl = "http://" + ZvtraConst.DOMAIN + "/bookmarks/" + externalId;

                    List<NameValuePair> getParams = new ArrayList<NameValuePair>(1);
                    getParams.add(new BasicNameValuePair("api_key", mPrefs.getString("apiKey", "")));                     

                    jsonObjRecv = HttpClient.SendHttpRequest(HttpClient.GET, requestUrl, getParams);        

                    success = jsonObjRecv.getBoolean("success");

                    if (success) {
                        JSONObject data =  jsonObjRecv.getJSONObject("data");
                        if (data != null) {   
                            updateContent(data.getString("content"), id);                                
                        }
                    }                 
                } while (cursor.moveToNext());
                
            }
       } catch (JSONException e) {
           e.printStackTrace();
       } 
    }
    /**
     * Добавление закладки
     * @return void
     */
    public void sendNewUrl(String url) {
        Boolean success;
        String externalId;
        Long id ;
        JSONObject jsonObjRecv;
    
        try {
            String requestUrl = "http://" + ZvtraConst.DOMAIN + "/bookmarks";

            List<NameValuePair> getParams = new ArrayList<NameValuePair>(4);
            getParams.add(new BasicNameValuePair("api_key", mPrefs.getString("apiKey", "")));
            getParams.add(new BasicNameValuePair("url", url));
            getParams.add(new BasicNameValuePair("grab_title", "yes"));
            getParams.add(new BasicNameValuePair("grab_content", "yes"));            

            jsonObjRecv = HttpClient.SendHttpRequest(HttpClient.POST, requestUrl, getParams);        

            success = jsonObjRecv.getBoolean("success");
                    
       } catch (JSONException e) {
           e.printStackTrace();
       } 
    }
}