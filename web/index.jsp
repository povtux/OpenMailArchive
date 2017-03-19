<%@ page import="org.openmailarchive.Entities.Group" %>
<%@ page import="org.openmailarchive.Entities.User" %>
<%@ page import="javax.naming.Context" %>
<%@ page import="javax.naming.InitialContext" %>
<%@ page import="javax.naming.NamingException" %>
<%@ page import="javax.sql.DataSource" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.sql.SQLException" %>
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
<a href="logout" title="logout">Logout</a>
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
<br><br>
<%
    String query = "SELECT domain FROM org_domain WHERE orgid IN(SELECT orgid FROM user WHERE username=?)";
    Context initCtx;
    Connection conn;
    try {
        initCtx = new InitialContext();
        Context envCtx = (Context) initCtx.lookup("java:comp/env");
        DataSource ds = (DataSource) envCtx.lookup("jdbc/OpenMailArchDB");

        conn = ds.getConnection();

        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, u.getUsername());
        ResultSet rs = stmt.executeQuery();

        String like = "";
        while (rs.next()) {
            if (like != "") like += " OR ";
            like += "recipient LIKE \"%@" + rs.getString(1) + "\"";
        }
        rs.close();
        stmt.close();
        query = "SELECT * FROM mail WHERE mailid IN(SELECT mailid FROM recipient WHERE " + like + " )";

        stmt = conn.prepareStatement(query);
        rs = stmt.executeQuery();

        while (rs.next()) {
%>
<table>
    <tr>
        <td><%= rs.getString(3) %>
        </td>
        <td><%= rs.getDate(4) %>
        </td>
        <td><%= rs.getString(5) %>
        </td>
    </tr>
    <tr>
        <td colspan="3">
            <% if (rs.getString(7).equals("plain")) { %>
            <pre> <% } %>
        <%= rs.getString(6) %>
        <% if (rs.getString(7).equals("plain")) { %> </pre>
            <% } %>
        </td>
    </tr>
</table>
<%
        }
    } catch (NamingException | SQLException e) {
        e.printStackTrace();
    }

%>
</body>
</html>