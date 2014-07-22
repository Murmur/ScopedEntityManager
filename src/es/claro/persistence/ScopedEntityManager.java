package es.claro.persistence;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;

/**
 * http://docs.oracle.com/javaee/6/api/javax/persistence/EntityManager.html
 */
public class ScopedEntityManager implements EntityManager {
	private final EntityManager delegate;
	private long ownerId;
	private String emName; // this may be NULL
	private Map<String,Object> attributes;
	private ScopedListener listener;
  
	public ScopedEntityManager(EntityManager delegate, String emName, long ownerId) {
		this.delegate = delegate;
		this.ownerId=ownerId;
		this.emName=emName;
	}
  
	@Override
	public void close() {
		if (ownerId>0) {
			//System.out.println("  is managed, do lazyClose");
		} else {
			//System.out.println("  is not managed, do immediateClose");
			lazyClose();
		}
	}

	protected void lazyClose() {
		try {
			// auto-rollback if TX was started and is still open
			if (getTransaction().isActive())
				getTransaction().rollback();
		} catch (Exception ex) { } // nothing we can do, silent fail
		if (listener != null) {
			try { listener.lazilyClosed(this); } catch(Exception ex) { }
		}
		attributes=null;
		delegate.close();
	}

	@Override public void persist(Object object) { delegate.persist(object); }
	@Override public <T> T merge(T entity) { return delegate.merge(entity); }
	@Override public void remove(Object object) { delegate.remove(object); }
	@Override public <T> T find(Class<T> entityClass, Object primaryKey) { return delegate.find(entityClass, primaryKey); }
	@Override public <T> T getReference(Class<T> entityClass, Object primaryKey) { return delegate.getReference(entityClass, primaryKey); }
	@Override public void flush() { delegate.flush(); }
	@Override public void setFlushMode(FlushModeType flushModeType) { delegate.setFlushMode(flushModeType); }
	@Override public FlushModeType getFlushMode() { return delegate.getFlushMode(); }
	@Override public void lock(Object object, LockModeType lockModeType) { delegate.lock(object, lockModeType); }
	@Override public void refresh(Object object) { delegate.refresh(object); }
	@Override public void clear() { delegate.clear(); }
	@Override public boolean contains(Object object) { return delegate.contains(object); }

	@Override public Query createQuery(String string) { return delegate.createQuery(string); }
	@Override public Query createNamedQuery(String string) { return delegate.createNamedQuery(string); }
	@Override public Query createNativeQuery(String string) { return delegate.createNativeQuery(string); }
	@Override public Query createNativeQuery(String string, String string0) { return delegate.createNativeQuery(string, string0); }

	@Override @SuppressWarnings( "rawtypes" )
	public Query createNativeQuery(String string, Class aClass) { return delegate.createNativeQuery(string, aClass); }

	@Override public void joinTransaction() { delegate.joinTransaction(); }
	@Override public EntityTransaction getTransaction() { return delegate.getTransaction(); } 
	@Override public Object getDelegate() { return delegate.getDelegate(); }
	@Override public boolean isOpen() { return delegate.isOpen(); }
	  
	// JPA 2.0
	@Override public <T>T find(Class<T> entityClass, Object primaryKey, Map<String,Object> props) { return delegate.find(entityClass, primaryKey, props); }
	@Override public <T>T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) { return delegate.find(entityClass, primaryKey, lockMode); }
	@Override public <T>T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String,Object> props) { return delegate.find(entityClass, primaryKey, lockMode, props); }
	@Override public void lock(Object entity, LockModeType lockMode, Map<String,Object> props) { delegate.lock(entity, lockMode, props); }
	@Override public void refresh(Object entity, Map<String,Object> props) { delegate.refresh(entity, props); }
	@Override public void refresh(Object entity, LockModeType lockMode) { delegate.refresh(entity, lockMode); }
	@Override public void refresh(Object entity, LockModeType lockMode, Map<String,Object> props) { delegate.refresh(entity, lockMode, props); }
	@Override public void detach(Object entity) { delegate.detach(entity); }
	@Override public LockModeType getLockMode(Object entity) { return delegate.getLockMode(entity); }
	@Override public void setProperty(String propertyName, Object value) { delegate.setProperty(propertyName, value); }
	@Override public Map<String,Object> getProperties() { return delegate.getProperties(); }
	@Override public <T>TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) { return delegate.createQuery(criteriaQuery); }
	@Override public <T>TypedQuery<T> createQuery(String qlString, Class<T> resultClass) { return delegate.createQuery(qlString, resultClass); }
	@Override public <T>TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) { return delegate.createNamedQuery(name, resultClass); }
	@Override public <T>T unwrap(Class<T> cls) { return delegate.unwrap(cls); }
	@Override public EntityManagerFactory getEntityManagerFactory() { return delegate.getEntityManagerFactory(); }
	@Override public CriteriaBuilder getCriteriaBuilder() { return delegate.getCriteriaBuilder(); }
	@Override public Metamodel getMetamodel() { return delegate.getMetamodel(); }
  
// ***************************************************
// ** Custom methods 
// ***************************************************

	public void setScopedListener(ScopedListener listener) {
		this.listener = listener;
	}
	
	public String getName() { return emName; }
	public long getOwnerId() { return ownerId; }
	
  	public Object getAttribute(String name) {
  	  	// attributes variable is set to NULL in close() function
  		return attributes != null ? attributes.get(name) : null;
	}
  
  	public void setAttribute(String name, Object value) {
  		//if (value==null) {
  		//	removeAttribute(name); // FIXME do not keep null attributes?
  		//} else {
  			if (attributes==null) attributes = new HashMap<String,Object>();
  			attributes.put(name, value);
  		//}
  	}
  
  	public Object removeAttribute(String name) {
  		return attributes!=null ? attributes.remove(name) : null;
  	}
  
  	public Iterator<String> getAttributeNames() {
  		return attributes != null ? 
			attributes.keySet().iterator() :
			Collections.<String>emptySet().iterator();
  	}
	  
	public static interface ScopedListener {
		public void lazilyClosed(ScopedEntityManager em);
	}
  
}
