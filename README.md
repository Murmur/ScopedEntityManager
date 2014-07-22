ScopedEntityManager
===================
Java Persistence API (JPA) wrapper for servlet container such as Apache Tomcat.
Automanage Entity Manager lifetime within http request context.

Inspiration
-----------
This wrapper is forked from [codegoogle scoped-entitymanager](https://code.google.com/p/scoped-entitymanager) project.
It's no longer maintained and last changes were 2007 and JPA1.x interface. I took the code, studied,
implementation changes to simplify things, added JPA2.x interface, support for two or more automanaged
entity manager instances within a single thread.

Features
--------
- **automanage** - close entity managers at the end of http request context.
- **named instance** - named instances for custom use-case, may have 2..n instances per request context.
- **non-managed owner** - custom owner id if instance should be kept available and open across http requests lifetime.
- **custom attributes** - custom attributes within entity manager to keep some application-level housekeeping values.
- **rollback** - automanaged instance is rollbacked if active transaction was not committed.

ScopedEntityManager wrapper uses servlet listener for lifetime management,
entity managers are automatically closed at the end of http request. Each request may
have one or more automanaged instances depending on how application retrieves instances.

Instances are owned by calling threadID and normal single threaded http handler code
creates just one entity manager instance, getter returns same instance within a thread.
This provides application a transparent single transaction context per http request.

Application may create two or more instances if that is required, all instances are
released at the end of http request lifetime.

Public interface
-----------------
Public methods retrieving existing or new entity managers.

**PersistenceManager PersistenceManager.getInstance()**
Returns singleton of PersistenceManager class.

**EntityManager getEntityManager()**
Get default entity manager which is owned by calling threadID, each 
concecutive call within a thread receives same instance.

**EntityManager getEntityManager(long ownerId, String dbName, String emName, Map map)**
Get or create named instance owned by given ownerID.
@ownerId  owner id which usually is threadID 1...n, this may be -n..-1 custom id but
          then application must close those non-automanaged instances. 
@dbName   PersistenceUnit name in persistence.xml file, always NULL current implentation uses first PU name
@emName   instance name to be get or created on first call, NULL always creates new instance
@map      JPA entity manager constructor options or NULL

**void closeEntityManagers(long ownerId)**
Close entity managers by owner id, this is automatically called for http request context.
Application must call this if -n..-1 custom ownerID was used. All open instances are
automatically closed if web application is undeployed.

Example
-------
Servlet or .jsp script example using automanaged EM instance.
```
// no need to call em.close() for automanaged EM instances
EntityManager em = PersistenceManager.getInstance().getEntityManager();
Query qry = em.createQuery("SELECT bean FROM OrderHeader bean ORDER BY bean.custId DESC, bean.updated ASC");
List<OrderHeader> beans = (List<OrderHeader>)qry.getResultList();
- - - 
// EM is automatically rollbacked if servlet exception was thrown or commit() was not invoked
EntityManager em = PersistenceManager.getInstance().getEntityManager();
OrderHeader bean = em.find(OrderHeader.class, 2);
em.getTransaction().begin();
bean.setComment("this is new comment text " + System.currentTimeMillis() );
em.getTransaction().commit();
```

Dependencies
------------
Servlet container libraries.
JPA implementation such as [Apache OpenJPA](http://openjpa.apache.org/) libraries.

Compatibility tested on
-----------------------
Compatibility is tested on the following environments, but wrapper should probably
work on other JPA 2.x implementations as well.
Tomcat7, JDK7, OpenJPA2.2_release
Tomcat7, JDK7, OpenJPA2.4.0_nightly
