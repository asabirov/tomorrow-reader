/**
 * @author CJ Slade
 * Хелпер для запросов к базе
 */
package tel.cjs.tomorrowreader;

import android.content.ContentValues;

import android.content.Context;
import android.database.Cursor;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.util.Log;
import java.util.ArrayList;

public class BookmarksDbHelper {

    public static final String TAG = "zvtra";
    
    private static final String DATABASE_NAME = "zvtra.db";
    
    private static final String DATABASE_TABLE = "bookmarks";
    
    private static final int DATABASE_VERSION = 2;
        
    private SQLiteDatabase db;
    
    public static final Integer ROW_ID = 0;
    public static final Integer ROW_EXTERNAL_ID = 1;
    public static final Integer ROW_TITLE = 2;
    public static final Integer ROW_URL = 3;
    public static final Integer ROW_DOMAIN = 4;
    public static final Integer ROW_CONTENT_EXISTS = 5;
    public static final Integer ROW_CONTENT = 6;
    public static final Integer ROW_CREATED_AT = 7;
    
    private static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS bookmarks"
                + "(_id INTEGER PRIMARY KEY, external_id VARCHAR, title VARCHAR, url VARCHAR, domain VARCHAR,"
                + "content_exists INT(1), content TEXT, created_at UNSIGNED INTEGER, deleted INT(1), new INT(1));"
                + "CREATE  INDEX bookmarks_external_id ON bookmarks ('external_id' ASC);"
                + "CREATE  INDEX bookmarks_deleted ON bookmarks ('deleted');";

   
    public BookmarksDbHelper(Context context)  {
      OpenHelper openHelper = new OpenHelper(context);
      this.db = openHelper.getWritableDatabase();      
    }
    /**
     * Очистка базы
     * @return void
     */
    public void deleteAll() {
       this.db.execSQL("DROP TABLE " + DATABASE_TABLE);
       this.db.execSQL(DATABASE_CREATE);
    }
    /**
     * Удаление по списку
     * @param ids 
     */
    public void deleteList(ArrayList<String> ids) {
        if (ids.size() > 0) {
            final String whereClause = "external_id NOT IN (" + convertToCommaDelimitedString(ids) + ")"; 
            Log.d(TAG, whereClause);
            this.db.delete(DATABASE_TABLE, whereClause, null);
        }
    }
    /**
     * Implode
     * @param ids
     * @return 
     */
    public static String convertToCommaDelimitedString(ArrayList<String> ids) {
        String out = "";
        for(int i=0; i<ids.size(); i++) {
            if(i!=0) { out += "','"; }
            out += ids.get(i).toString();
        }
        return "'"+out+"'";
    }
    /**
     * Закрытие базы
     * @return void
     */
    public void close() {
        Log.w(TAG, "db close");
        this.db.close();
    }
    /**
     * Добавление закладки
     * @param externalId String
     * @param url String
     * @param title String
     * @param domain String
     * @param contentExists Boolean
     * @param content String
     * @param createdAt Integer
     */
    public void insertRow(String externalId, String url, String title, String domain, Integer contentExists, String content, Integer createdAt) {
        ContentValues initialValues = new ContentValues();
        initialValues.put("external_id", externalId);
        initialValues.put("url", url);
        initialValues.put("title", title);
        initialValues.put("domain", domain);
        initialValues.put("content_exists", contentExists);
        initialValues.put("content", content);
        initialValues.put("created_at", createdAt);
        initialValues.put("deleted", 0);
        db.insert(DATABASE_TABLE, null, initialValues);
    }
    /**
     * Удаление записи
     * @param id Integer
     * @param boolean
     */
    public boolean delete(Long id) {
        return db.delete(DATABASE_TABLE, "_id='" + id + "'", null) > 0;
    }
    /**
     * Возвращает все записи
     * @return Cursor
     */
    public Cursor fetchAll() {
       Cursor cursor = db.query(DATABASE_TABLE, new String[] {
                    "_id", "external_id", "title", "url", "domain", "content_exists", "content", "created_at"}, "deleted = 0", null, null, null, "created_at desc"); 
       
       return cursor;
    }
    /**
     * Возвращает все записи
     * @return Cursor
     */
    public Cursor fetchWithoutContent() {        
       Cursor cursor = db.rawQuery("SELECT _id, external_id FROM " + DATABASE_TABLE + " WHERE content_exists = 1 AND content = ''" , null);
       return cursor;
    }
    /**
     * Возвращает все записи помеченные на удаление
     * @return Cursor
     */
    public Cursor fetchDeleted() {        
       Cursor cursor = db.rawQuery("SELECT _id, external_id FROM " + DATABASE_TABLE + " WHERE deleted = 1" , null);
       return cursor;
    }
    /**
     * Возвращает запись
     * @param id String
     * @return Cursor
     */
    public Cursor fetchRow(Long id) {
        Cursor cursor = db.query(DATABASE_TABLE, new String[] {
                "external_id", "title", "url", "domain", "content_exists", "content", "created_at"}, "_id=" + id, null, null, null, null);        
        if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
    }
    /**
     * Наличие записи в базе по externalId
     * @param externalId String
     * @return boolean
     */
    public boolean existsByExternalId(String externalId) {
        Cursor dataCount = db.rawQuery("SELECT COUNT(*) FROM " + DATABASE_TABLE + " WHERE external_id = '" + externalId + "'", null);
        dataCount.moveToFirst();
        int count = dataCount.getInt(0);
        dataCount.close();
        return (count > 0);
    }
    /**
     * Наличие записей в базе
     * @return boolean
     */
    public boolean dataExists() {
        Cursor dataCount = db.rawQuery("SELECT COUNT(*) FROM " + DATABASE_TABLE, null);
        dataCount.moveToFirst();
        int count = dataCount.getInt(0);
        Log.d(TAG, Integer.toString(count));
        dataCount.close();
        return (count > 0);
    }
    /**
     * Обновление текста
     * @param content 
     * @param id
     */
    public void updateContent(String content, Long id) {
        ContentValues args = new ContentValues();
        args.put("content", content);
        db.update(DATABASE_TABLE, args, "_id=" + id, null);
    }
    /**
     * Помечаем уданным
     * @param id Long
     */
    public void setDeleted(Long id) {
        ContentValues args = new ContentValues();
        args.put("deleted", 1);
        db.update(DATABASE_TABLE, args, "_id=" + id, null);
    }
    private static class OpenHelper extends SQLiteOpenHelper {

      OpenHelper(Context context) {         
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
      }

      @Override
      public void onCreate(SQLiteDatabase db) {
         db.execSQL(DATABASE_CREATE);
      }

      @Override
      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         Log.w(TAG, "Upgrading database, this will drop tables and recreate.");
         db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
         onCreate(db);
      }
   }
}

