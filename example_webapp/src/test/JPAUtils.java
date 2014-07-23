package test;

import java.util.*;
import java.text.*;
import javax.persistence.*;
import org.apache.openjpa.kernel.StoreContext;

public class JPAUtils {

    /**
     * Find all entities
     * @param em    entity manager
     * @param entityClass   entity class
     * @return  list of entities or empty list
     */
    public static<T> List<T> findAll(EntityManager em, Class<T> entityClass) {
        TypedQuery<T> q = em.createQuery("Select bean from " + entityClass.getSimpleName() + " bean", entityClass);
        return q.getResultList();
    }

    public static String cal2db(Calendar val, StoreContext ctx) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		return df.format(val.getTime()); // convert to UTC string
	}
    
    public static Calendar db2cal(String val, StoreContext ctx) {
		// convert from UTC string to default timezone calendar
        int y = Integer.parseInt(val.substring(0, 4));
        int m = Integer.parseInt(val.substring(5, 7));
        int d = Integer.parseInt(val.substring(8, 10));
		int h = Integer.parseInt(val.substring(11, 13));
		int mi= Integer.parseInt(val.substring(14, 16));
		int s = Integer.parseInt(val.substring(17, 19));
		int millis = (val.length() > 20 ? Integer.parseInt(val.substring(20)) : 0 );
        Calendar cal = Calendar.getInstance( TimeZone.getTimeZone("UTC") );
        cal.set(Calendar.YEAR, y);
        cal.set(Calendar.MONTH, m-1);
        cal.set(Calendar.DAY_OF_MONTH, d);
        cal.set(Calendar.HOUR_OF_DAY, h);
        cal.set(Calendar.MINUTE, mi);
        cal.set(Calendar.SECOND, s);
        cal.set(Calendar.MILLISECOND, millis);
        long ts = cal.getTimeInMillis();
        cal.setTimeZone(TimeZone.getDefault());
        cal.setTimeInMillis(ts);
        return cal;
    }

	public static String formatDateTime(Calendar val) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(val.getTime()); // convert to default timezone string
	}

	public static String HTMLEncode(String val) {
		if (val == null) return "";
		return XMLEncode(val, false, false);	
	}
	
	public static String XMLEncode(String val, boolean useApos, boolean keepNewlines) {
		int len = val.length();
		StringBuilder sbuf = new StringBuilder(len+10);
		for (int idx=0; idx<len; idx++) {
			char ch = val.charAt(idx);
			switch (ch) {
			case '<': {    sbuf.append("&lt;");     break; }
			case '>': {    sbuf.append("&gt;");     break; }
			case '&': {    sbuf.append("&amp;");    break; }
			case '"': {    sbuf.append("&quot;");   break; }
			case '\'': {   
				if (useApos) sbuf.append("&apos;");
				else sbuf.append("&#39;");
				break;  }
            //case '€': {    sbuf.append("&#8364;"); break; }
			case '\r':
			case '\n':
			case '\t':
			case '\f': {
				if (keepNewlines) {
					sbuf.append(ch);
				} else {
					sbuf.append("&#");
					sbuf.append(Integer.toString(ch));
					sbuf.append(';');
				}
				break; }
			default: {
				sbuf.append(ch);
				}
			}
		}
		return sbuf.toString();
	}
	
}
