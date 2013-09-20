<%@ page import="de.doridian.xvmanage.navbar.NavbarLink" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:useBean id="loggedInUser" class="de.doridian.xvmanage.models.User" scope="session"/>
<jsp:useBean id="loginBean" class="de.doridian.xvmanage.forms.LoginForm" scope="request">
    <jsp:setProperty name="loginBean" property="*"/>
</jsp:useBean>
<%
    if(loginBean.isSubmit(request)) {
        if(loginBean.validate()) {
            loggedInUser = loginBean.getCurrentUser();
            session.setAttribute("loggedInUser", loggedInUser);

            if(NavbarLink.getForURL(loginBean.getReturnURL()) == null)
                response.sendRedirect("/index.jsp");
            else
                response.sendRedirect(loginBean.getReturnURL());

            return;
        } else {
            request.setAttribute("errorMsg", loginBean.getErrorHTML());
        }
    } else {
        request.setAttribute("errorMsg", "");
    }
%>
<t:masterpage title="Log in">
    <jsp:body>
        <form class="form-horizontal" action="login.jsp" method="post">
            ${errorMsg}
            <div class="control-group">
                <label class="control-label" for="username">Username</label>
                <div class="controls">
                    <div class="input-prepend">
                        <span class="add-on"><i class="icon-user"></i></span>
                        <input type="text" id="username" name="username" placeholder="Username" value="${fn:escapeXml(loginBean.username)}" required="required" />
                    </div>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="password">Password</label>
                <div class="controls">
                    <div class="input-prepend">
                        <span class="add-on"><i class="icon-lock"></i></span>
                        <input type="password" id="password" name="password" required="required" />
                    </div>
                </div>
            </div>
            <div class="control-group">
                <div class="controls">
                    <input type="hidden" name="returnURL" value="${fn:escapeXml(loginBean.returnURL)}" />
                    <input type="submit" class="btn" value="Log in" />
                </div>
            </div>
        </form>
    </jsp:body>
</t:masterpage>