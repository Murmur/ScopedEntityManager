package es.claro.persistence;

import java.util.*;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;

/**
 * Maintain a lifecycle of automanaged EntityManager instances per http request context.
 * Instance is owned by thread id (1..n) and closeEntityManagers() is called by servlet listener.
 */
public class PersistenceManager {
	public static final String DEFAULT_NAME = "default";
	private static final PersistenceManager singleton = new PersistenceManager();
	private String persistenceUnit = null;  
	private volatile EntityManagerFactory emf; // FIXME: hashMap for 2..n dbName PersistenceUnit names?

	//FIXME: use LockReadWrite lock instead of synchronized block? any performance differences?
	private Map<Long, List<ScopedEntityManager>> emList; // OwnerId=List_of_EMs
	private ScopedEntityManager.ScopedListener scopedListener;
	
	public static PersistenceManager getInstance() { return singleton; }
  
	protected PersistenceManager() { 
		emList = new HashMap<Long,List<ScopedEntityManager>>(32);

		scopedListener = new ScopedEntityManager.ScopedListener() {
			@Override public void lazilyClosed(ScopedEntityManager em) {
				//System.out.println(String.format("lazilyClosed %s ownerId=%d em=%d", em.getName(), em.getOwnerId(), em.hashCode() ));
				PersistenceManager.this.lazilyClosed(em);
			}
		};
	}
 
	public EntityManagerFactory getEntityManagerFactory() {
		// http://stackoverflow.com/questions/70689/what-is-an-efficient-way-to-implement-a-singleton-pattern-in-java
		// use volatile-doublecheck-lazy-singleton
		if (emf == null) {
			synchronized(this) {
				if (emf==null)
					emf = Persistence.createEntityManagerFactory(persistenceUnit);
			}
		}
		return emf;
	}
  
	protected synchronized void closeEntityManagerFactory() {
		closeEntityManagers(0);		
		if (emf != null) {
			emf.close();
			emf = null;
		}
	}

	/**
	 * Get default instance of automanaged entity manager. 
	 * Instance is owned by current thread id.
	 * Use "default" instance name. 
	 * @return	JPA entity manager
	 */
	public EntityManager getEntityManager() {
		return getEntityManager(Thread.currentThread().getId(), null, DEFAULT_NAME, null);
	}

	/**
	 * Create or get named instance.
	 * @param ownerId	owner id is 1..n=automanaged thread id, -n..-1=nonmanaged custom owner
	 * @param dbName	Not used, always null
	 * @param emName	Instance name (get existing or create on first call), NULL always creates a new instance
	 * @param map		Optional entitymanager options
	 * @return
	 */
	@SuppressWarnings("rawtypes")	
	public synchronized EntityManager getEntityManager(long ownerId, String dbName, String emName, Map map) {
		Long tid = Long.valueOf(ownerId);
		List<ScopedEntityManager> ems = emList.get(tid);
		ScopedEntityManager em=null;

		if (ems==null) {
			ems = new ArrayList<ScopedEntityManager>(4);
			emList.put(tid, ems);
		} else if (emName!=null) {
			for(int idx=0; idx<ems.size(); idx++) {
				em = ems.get(idx);
				if (emName.equals(em.getName())) return em;
			}
		}
		
		EntityManager delegate = map!=null ? 
				getEntityManagerFactory().createEntityManager(map) : 
				getEntityManagerFactory().createEntityManager();
		em = new ScopedEntityManager(delegate, emName, ownerId); //ownerId 1..n=automanaged			
		em.setScopedListener(scopedListener);
		ems.add(em);
		return em;				
	}

	/*public synchronized boolean hasEntityManager(long ownerId, String dbName, String emName) {
		Long tid = Long.valueOf(ownerId);
		List<ScopedEntityManager> ems = emList.get(tid);
		if (ems!=null) {
			for(int idx=0; idx<ems.size(); idx++) {
				ScopedEntityManager em = ems.get(idx);
				if (emName.equals(em.getName())) return true;
			}			
		}
		return false;
	}*/
	
	/**
	 * Close entity managers
	 * @param ownerId	1..n=owner id or -n..-1=non-managed owner. 
	 *                  Do not use 0 it closes all instances, its used when webapp context is destroyed.
	 */
	public void closeEntityManagers(long ownerId) {
		List<ScopedEntityManager> ems;
		if (ownerId==0) {
			// close all instances, entire app context was destroyed
			ems = new ArrayList<ScopedEntityManager>();
			synchronized(this) {
				for(List<ScopedEntityManager> list : emList.values())
					ems.addAll(list);
				emList.clear();				
			}
		} else {
			// close instances owned by this thread, httprequest context was destroyed 
			Long tid = Long.valueOf(ownerId);
			synchronized(this) {
				ems = emList.remove(tid);
			}
		}
		if (ems!=null) {
			for(int idx=ems.size()-1; idx>=0; idx--) 
				try { ems.get(idx).lazyClose(); } catch(Exception ex){}
		}		
	}

	private synchronized void lazilyClosed(ScopedEntityManager em) {
		Long tid = Long.valueOf(em.getOwnerId());
		List<ScopedEntityManager> ems = emList.get(tid);
		if (ems!=null) {
			for(int idx=ems.size()-1; idx>=0; idx--) {
				if (ems.get(idx)==em) {
					ems.remove(idx);
					break;
				}
			}
			if (ems.isEmpty()) emList.remove(tid);
		}
	}

	/**
	 * Sets the name of the persistence unit that will be used when the
	 * entity manager factory is created.
	 * @param name Name of one persistence unit defined in persistence.xml
	 */
	protected void setPersistenceUnit(String name) {
		persistenceUnit = name;
	}
	
	/**
	 * Get current statistics (debug use only).
	 * @return
	 */
	public synchronized Map<String,String> getStatistics() {
		long now = System.currentTimeMillis();
		Map<String,String> stats = new LinkedHashMap<String, String>();
		stats.put("em.ownerCount", ""+emList.size());
		int idx=-1;
		for(Long key : emList.keySet()) {			
			idx++;
			List<ScopedEntityManager> list =  emList.get(key);			
			stats.put("em"+idx+".ownerId", ""+key);	// owner ThreadId			
			if (list!=null) {
				for(int idxb=0; idxb<list.size(); idxb++) {
					ScopedEntityManager em = list.get(idxb);
					stats.put("em"+idx+"."+idxb+".name", ""+em.getName());
					stats.put("em"+idx+"."+idxb+".createdUTC", ""+em.getCreated());
					stats.put("em"+idx+"."+idxb+".createdSince", ""+(now-em.getCreated()) );
				}
			}
		}
		
		return stats;
	}

}
