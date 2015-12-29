package es.claro.persistence;

import java.io.*;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Initialize global JPA PersistenceManager at the start of webapp context lifecycle.
 * Destroy global JPA PersistenceManager  at the end of webapp context lifecycle. 
 * https://bz.apache.org/bugzilla/show_bug.cgi?id=57314´
 * 
 * Define listener and filter in web.xml file
  <listener>
    <description>Initialize global JPA resources</description>
    <listener-class>es.claro.persistence.ScopedContextListener</listener-class>
  </listener>
  <filter>
    <filter-name>ScopedRequestFilter</filter-name>
    <filter-class>es.claro.persistence.ScopedRequestFilter</filter-class>
  </filter>
  <filter-mapping>
    <filter-name>ScopedRequestFilter</filter-name>
	<url-pattern>/*</url-pattern>
  </filter-mapping>
  
 * @see 	ScopedRequestFilter.java  
 */
public class ScopedContextListener implements ServletContextListener {
	
	@Override public void contextInitialized(ServletContextEvent evt) {
		// Read JPA persistence unit name from xml file,
		// value is <persistence-unit name="myjpaunit" ...> attribute.
		InputStream is=null;
		try {
			is = evt.getServletContext().getResourceAsStream("/WEB-INF/classes/META-INF/persistence.xml");
			if (is==null)
				is = evt.getServletContext().getResourceAsStream("/META-INF/persistence.xml");
			String name = null;
			if (is != null) {
				// hack: no love for XML parser for this one use-case only
				byte[] bytes = new byte[is.available()];
				is.read(bytes);
				String data = new String(bytes);
				int idx = data.indexOf("<persistence-unit");
				idx = idx>0 ? data.indexOf(" name", idx+1) : -1;
				if (idx>0) {
					idx = data.indexOf('"', idx);
					int idxEnd = data.indexOf('"', idx+1);
					name = data.substring(idx+1, idxEnd);
				}
			}
			if (name==null)
				evt.getServletContext().log("persistence-unit name attribute not found, use default name");
			PersistenceManager.getInstance().setPersistenceUnit(name!=null ? name : "default");
		} catch (Exception ex) {
			evt.getServletContext().log(ex.getMessage(), ex);
		} finally {
			try { if (is!=null) is.close(); } catch (Exception ex) { }
		}
	}

	@Override public void contextDestroyed(ServletContextEvent evt) {
		// close factory instance, its one instance per application
		try {
			PersistenceManager.getInstance().closeEntityManagerFactory();
		} catch (Exception ex) {
			evt.getServletContext().log(ex.getMessage(), ex);
		}
	}

}
