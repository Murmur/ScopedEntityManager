package es.claro.persistence;

import java.io.*;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

/**
 * Handle automanaged EntityManager HTTP request and context lifecycle.
 */
public class ScopedServletListener  implements ServletContextListener, ServletRequestListener {
	
	@Override
	public void contextInitialized(ServletContextEvent evt) {
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

	@Override
	public void contextDestroyed(ServletContextEvent evt) {
		// close factory instance, its one instance per application
		try {
			PersistenceManager.getInstance().closeEntityManagerFactory();
		} catch (Exception ex) {
			evt.getServletContext().log(ex.getMessage(), ex);
		}
	}
	
	@Override
	public void requestInitialized(ServletRequestEvent evt) { }

	@Override
	public void requestDestroyed(ServletRequestEvent evt) {
		PersistenceManager.getInstance().closeEntityManagers(Thread.currentThread().getId());
	}	
	
}
