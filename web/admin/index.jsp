<%--
  Created by IntelliJ IDEA.
  User: po
  Date: 19/08/2017
  Time: 13:53
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.openmailarchive.admin.graph.Stats" %>
<html>
<head>
    <title>OpenMailArchive - Admin</title>
    <jsp:include page="../includes/head.jsp"/>
</head>
<body>
<jsp:include page="../includes/nav.jsp"/>
<div class="content">
    <%= Stats.getNbMailPerHourSvgGraph() %>
    <%= Stats.getNbMailPerDaySvgGraph() %>
</div>
<jsp:include page="../includes/foot.jsp"/>
</body>
</html>
