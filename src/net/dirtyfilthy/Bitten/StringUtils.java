package net.dirtyfilthy.Bitten;

public class StringUtils {
	
	public static String truncateText(String str, int maxLen, int lenFromEnd) {
	    int len;
	    if (str != null && (len = str.length()) > maxLen) {
	      return str.substring(0, maxLen - lenFromEnd - 3) + "..." +
	          str.substring(len - lenFromEnd, len);
	    }
	    return str;
	  }
	  public static String truncateText(String str, int maxLen) {
	    return truncateText(str, maxLen, 0);
	  }

}
