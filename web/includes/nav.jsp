<%@ page import="org.openmailarchive.Entities.Group" %>
<%@ page import="org.openmailarchive.Entities.User" %>
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
                            String dom;
                            for (Object o : u.getOrg().getDomains()) {
                                dom = (String) o;
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
                <a href="./admin/index.jsp" title="admin" class="btn btn-default navbar-btn">Admin</a>
                <a href="./logout" title="logout" class="btn btn-default navbar-btn">Logout</a>
            </ul>
        </div><!-- /.navbar-collapse -->
    </div><!-- /.container-fluid -->
</nav>