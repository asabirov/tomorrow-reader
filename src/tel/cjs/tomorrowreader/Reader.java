
/**
 * Читалка
 * @author CJ Slade
 */

package tel.cjs.tomorrowreader;


public class Reader {
    
    private String _mode;
    
    private String _title;
    
    private String _text;
    
    public Reader setMode(String mode) {
         _mode = mode;
         return this;
    }
    
    public Reader setTitle(String title) {
        _title = title;    
        return this;
    }
    
    public Reader setText(String text) {
        _text = text;
        return this;
    }
    
    public int getBackground() {
        if (_mode == "dark") {
            return R.color.reader_background;
        } else {
            return R.color.reader_background_light;
        }
    }
    
    public String getStyles() {
       String styles = "<style type=\"text/css\">";
       
       if (_mode == "dark") {
          styles += "body {background: #333; color: #fff;}"
             + "a:visited{color: #eee}"
             + "a {color: #76AD5D}";
       } else {
           styles += "body {background: #FFFAE9; color: #222;}"
             + "a {color: #26672D;}"
             + "a:visited {color: #CCCCCC}";
       }
       
       styles += "code, pre {padding: 1em; color: #FFFFFF !important; background: #CF7641; }"
               + "blockquote {margin:1.5em;color:#666;font-style:italic;}"
               + "h1 {font-size: 1.3em;}"
               + "h2 {font-size: 1.1em;}"
               + "h3 {font-size: 1.1em;}"                    
               + "</style>";
       return styles;
    }
    
    public String getContent() {
        String data = "<html>";
            data += "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />";
            data += getStyles();
            data += "</head>";
            data += "<body>";
            data += _text;
            data += "</body></html>"; 
        return data;
    }
}
