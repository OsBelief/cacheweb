package com.yh.web;

/**
 * @author gudh
 *
 */
public class Test {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String[] urls = {
				"http://manhua.yicha.cn/manhua/idx.do?sor=1&t=1378453699296"};
		for (String url : urls) {
			System.out.println(url.matches(".+t=?\\d{10,13}([^\\d].*|$)") + "\t" + url);
		}
	}

}
