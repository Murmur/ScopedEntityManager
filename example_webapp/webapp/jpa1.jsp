<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@
    page contentType="text/html; charset=UTF-8" pageEncoding="ISO-8859-1"
    import="java.util.*,
		javax.persistence.*,
		es.claro.persistence.PersistenceManager,
		test.*
		"
%><%
System.out.println("------");

// Get automanaged EntityManager, update db bean, 
// EM is automatically closed at the end of http request.
String sOldNewVal="";
EntityManager em = PersistenceManager.getInstance().getEntityManager();
OrderHeader oh = em.find(OrderHeader.class, 1);
if (oh!=null) {
	em.getTransaction().begin();
	sOldNewVal = oh.getComment();
	oh.setUpdated(Calendar.getInstance());
	oh.setComment("Bean \u00C5\u00C4\u00D6 " + " " + System.currentTimeMillis());
	sOldNewVal += " -> " + oh.getComment();
	em.getTransaction().commit();
}

Query q = em.createQuery("SELECT bean FROM OrderHeader bean ORDER BY bean.custId DESC");
List<OrderHeader> list = (List<OrderHeader>)q.getResultList();

%><!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
  <title>JPAExample</title>
</head>
<body>

<b>Bean:</b> <%= oh!=null ? oh.getId()+" "+sOldNewVal : "Not found" %><br/>

<b>List Items</b>
<table cellspacing="0" border="1">
  <c:forEach var="item" items="<%= list %>">
  <jsp:useBean id="item" type="test.OrderHeader" />
  <tr>
  	<td>${item.id}</td>
  	<td>${item.custId}</td>
  	<td><%= JPAUtils.HTMLEncode(item.getComment()) %></td>
  	<td><%= JPAUtils.formatDateTime(item.getUpdated()) %></td>
  </tr>
  </c:forEach>
</table>

</body>
</html>