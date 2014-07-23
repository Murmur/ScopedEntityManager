<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@
    page contentType="text/html; charset=UTF-8" pageEncoding="ISO-8859-1"
    import="java.util.*,
		java.util.concurrent.*,
		javax.persistence.*,
		es.claro.persistence.PersistenceManager,
		test.*
		"
%><%
System.out.println("------");
final long ownerId = Thread.currentThread().getId();
System.out.println("mainThread="+ownerId);
ExecutorService executor = Executors.newFixedThreadPool(4);
List<Future<String[]>> workers = new ArrayList<Future<String[]>>();
for(int idx=0; idx < 4; idx++) {
	final String[] retval = new String[] { ""+idx, "" };
	Runnable worker = new Runnable() { public void run() {
		int sleep = 1+Double.valueOf(Math.random()*(3000-1)).intValue();
		try { Thread.sleep(sleep); } catch(Exception ex) { }
		// get new automanaged instance owned by main httprequest thread id, 
		// all instances are closed once http request is completed.
		EntityManager em = PersistenceManager.getInstance().getEntityManager(ownerId, null, null, null); // ownerId, dbName, emName, optionsMap
		System.out.println("thread " + retval[0] + " " + em.hashCode());
		OrderHeader oh = em.find(OrderHeader.class, 1);
		retval[1] = oh.getComment();
		em.close();
	}};
	workers.add( executor.submit(worker, retval) );
}
String[] retval = workers.get(0).get();
executor.shutdown(); // let workers run and then close threadpool

// get default automanaged instance, close() is delayed until http request is completed.
EntityManager em = PersistenceManager.getInstance().getEntityManager();
OrderHeader oh2 = em.find(OrderHeader.class, 2);
em.close();
System.out.println("em2 closed " + em.hashCode());

// this returns same automanaged instance, em.close() is not mandatory its
// automatically called once http request is completed. But it doesn't do harm if was called anyway.
em = PersistenceManager.getInstance().getEntityManager();
OrderHeader oh3 = em.find(OrderHeader.class, 3);
em.close();
System.out.println("em3 closed " + em.hashCode());

// get new non-managed instance, application must explicitly close this instance
OrderHeader oh4;
em = PersistenceManager.getInstance().getEntityManager(-1, null, null, null); // ownerId, dbName, emName, optionsMap
try {
	oh4 = em.find(OrderHeader.class, 4);
} finally {
	em.close();
	System.out.println("em4 closed " + em.hashCode());
}

%><!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
  <title>JPAExample</title>
</head>
<body>

1 bean: <%= retval[1] %><br/>
2 bean: <%= oh2.getComment() %><br/>
3 bean: <%= oh3.getComment() %><br/>
4 bean: <%= oh4.getComment() %><br/>

</body>
</html>