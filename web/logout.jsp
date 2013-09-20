<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    request.getSession().removeAttribute("loggedInUser");
%>
<t:masterpage title="Logout">
    <jsp:body>
        <div class="alert alert-success">You are now logged out.</div>
    </jsp:body>
</t:masterpage>