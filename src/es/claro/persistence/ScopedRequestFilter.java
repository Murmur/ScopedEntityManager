package es.claro.persistence;

import java.io.*;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Handle automanaged EntityManager HTTP request lifecycle.
 * This closes EntityManager instance at the end of request, 
 * if instance has an active transaction it is rollbacked.
 * @see ScopedContextListener.java
 */
public class ScopedRequestFilter implements Filter {
		
	@Override public void init(FilterConfig fc) throws ServletException { }
	@Override public void destroy() { }

	@Override public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain fc) throws IOException, ServletException {
		//System.out.println("reqInit="+Thread.currentThread().getId());
		try {
			fc.doFilter(req, res);			
		} finally {
			PersistenceManager.getInstance().closeEntityManagers(Thread.currentThread().getId());
		}
		//System.out.println("reqDest="+Thread.currentThread().getId());
	}
	
}
