package db.dao.service;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		 String foldPath = dir+"//"+cid+"//";	
    	String s = "file:/D:/pcloud/Mallet/db/7/1098_0_1408723853.txt";
    	
    	int a = s.lastIndexOf(".");
    	int b = s.lastIndexOf("/");
    	String sub = s.substring(b+1, a);
    	System.out.println("sub:"+sub);
    	
		String[] sA = sub.split("_");
		String id = sA[0];
		String PR = sA[1];
		String editDate = sA[2];
		System.out.println("id:"+id);
		System.out.println("PR:"+PR);
		System.out.println("editDate:"+editDate);
		
		System.out.println("b:"+b);
		int c = s.lastIndexOf("/", b-1);
		String cid = s.substring(c+1, b);
		System.out.println("cid:"+cid);
	}

	public static void test1() {
		try {
    		String editDate = "2014-08-22 23:26:57";
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    	Date d = sdf.parse(editDate);
	    	System.out.println("long:"+d.getTime());
//	    	Calendar c = Calendar.getInstance();
//	    	c.setTime(d);
			System.out.println(sdf.format(d));
	    	} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

}
