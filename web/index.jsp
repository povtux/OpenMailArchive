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
    <jsp:include page="includes/head.jsp"/>
</head>
<body>
<jsp:include page="includes/nav.jsp"/>
<jsp:include page="includes/search.jsp"/>
<div class="content">
    <div class="maillist">
        <ul id="mailtable" class="nav nav-pills nav-stacked"></ul>
    </div>
    <div id="mailcontent">

    </div>
</div>
<jsp:include page="includes/foot.jsp"/>
</body>
</html>