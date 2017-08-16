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
    <base href="<%= request.getRequestURL() %>">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
    <!-- Latest compiled and minified CSS -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css"
          integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">

    <!-- Optional theme -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css"
          integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp" crossorigin="anonymous">

    <!-- Latest compiled and minified JavaScript -->
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"
            integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa"
            crossorigin="anonymous"></script>
    <link rel="stylesheet" href="style/mails.css">
</head>
<body>
<% User u = (User) session.getAttribute("user"); %>
<nav class="navbar navbar-default">
    <div class="container-fluid">
        <!-- Brand and toggle get grouped for better mobile display -->
        <div class="navbar-header">
            <button type="button" class="navbar-toggle collapsed" data-toggle="collapse"
                    data-target="#bs-example-navbar-collapse-1" aria-expanded="false">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="navbar-brand" href="#">
                <img alt="OpenMailArchive" src="img/logo.png">
            </a>
        </div>

        <!-- Collect the nav links, forms, and other content for toggling -->
        <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
            <ul class="nav navbar-nav navbar-right">
                <p class="navbar-text"><%= u.getFirstname() %> <%= u.getLastname() %>
                </p>
                <p class="navbar-text"><%= u.getOrg().getName() %>
                </p>

                <!-- GROUPS -->
                <%
                    if (u.getGroups() != null) {
                %>
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true"
                       aria-expanded="false">Groups <span class="caret"></span></a>
                    <ul class="dropdown-menu">
                        <%
                            for (Group g : u.getGroups()) {
                        %>
                        <li><a href="#"><%= g.getName() %>
                        </a></li>
                        <%
                            }
                        %>
                    </ul>
                </li>
                <%
                    }
                %>

                <!-- DOMAINS -->
                <%
                    if (u.getOrg() != null && u.getOrg().getDomains() != null) {
                %>
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true"
                       aria-expanded="false">Domains <span class="caret"></span></a>
                    <ul class="dropdown-menu">
                        <%
                            for (String dom : u.getOrg().getDomains()) {
                        %>
                        <li><a href="#"><%= dom %>
                        </a></li>
                        <%
                            }
                        %>
                    </ul>
                </li>
                <%
                    }
                %>
                <a href="./logout" title="logout" class="btn btn-default navbar-btn">Logout</a>
            </ul>
        </div><!-- /.navbar-collapse -->
    </div><!-- /.container-fluid -->
</nav>
<div class="search">
    <form id="searchform" onsubmit="searchMails();return false;">
        <input max="200" name="query" id="query">
        <input type="submit" value="Search">
    </form>
</div>
<div class="content">
    <div class="maillist">
        <ul id="mailtable" class="nav nav-pills nav-stacked"></ul>
    </div>
    <div id="mailcontent">

    </div>
</div>
<script src="js/mails.js"></script>
</body>
</html>