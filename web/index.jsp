<%@ page import="org.openmailarchive.Entities.Group" %>
<%@ page import="org.openmailarchive.Entities.User" %>
<%--
  Created by IntelliJ IDEA.
  User: pov
  Date: 14/12/16
  Time: 18:19
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>OpenMailArchive</title>
</head>
<body>
<% User u = (User) session.getAttribute("user"); %>
<%= u.getFirstname() %>
<%= u.getLastname() %>
<%= u.getOrg().getName() %>

<%
    if (u.getGroups() != null) {
        for (Group g : u.getGroups()) {
%>
<%= g.getName() %>
<%
        }
    }
%>

<%
    if (u.getOrg() != null && u.getOrg().getDomains() != null) {
        for (String dom : u.getOrg().getDomains()) {
%>
<%= dom %>
<%
        }
    }
%>
</body>
</html>