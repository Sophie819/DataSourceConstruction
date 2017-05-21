package name.sophy.querylog;

import java.io.UnsupportedEncodingException;

public class URLDecoder {
	 private final static String ENCODE = "UTF-8"; 
	    /**
	     * URL 解码
	     *
	     * @return String
	     * @author lifq
	     * @date 2015-3-17 下午04:09:51
	     */
	    public static String getURLDecoderString(String str) {
	        String result = "";
	        if (null == str) {
	            return "";
	        }
	        try {
	            result = java.net.URLDecoder.decode(str, ENCODE);
	        } catch (UnsupportedEncodingException e) {
	            e.printStackTrace();
	        }
	        return result;
	    }
}
