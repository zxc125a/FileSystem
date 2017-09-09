package util;

public class StringMethod {
	/**
	 * 判断字符串对象str是否为空对象或者其内容为0
	 * 若是，则返回true, 否则返回false;
	 */
	public static boolean isEmpty(String str) {
		
		if(str == "" || str == null) {
			
			return true;
		}
		return false;
	}
}
