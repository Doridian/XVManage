<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:useBean id="loggedInUser" class="de.doridian.xvmanage.models.User" scope="session"/>
<jsp:useBean id="accountBean" class="de.doridian.xvmanage.forms.AccountForm" scope="request">
    <jsp:setProperty name="accountBean" property="*"/>
</jsp:useBean>
<%
    if(accountBean.isSubmit(request)) {
        if(accountBean.validate(loggedInUser)) {
            if(accountBean.getPassword() != null && !accountBean.getPassword().isEmpty()) {
                loggedInUser.setPassword(accountBean.getPassword());
                loggedInUser.save();
            }

            request.setAttribute("errorMsg", "<div class=\"alert alert-success\">Account edited successfully.</div>");
        } else {
            request.setAttribute("errorMsg", accountBean.getErrorHTML());
        }
    } else {
        request.setAttribute("errorMsg", "");
    }
%>
<t:masterpage title="Manage account">
    <jsp:body>
        <form class="form-horizontal" action="account.jsp" method="post">
                ${errorMsg}
            <div class="control-group">
                <label class="control-label" for="username">Username</label>
                <div class="controls">
                    <div class="input-prepend">
                        <span class="add-on"><i class="icon-user"></i></span>
                        <input type="text" id="username" value="${fn:escapeXml(loggedInUser.name)}" readonly="readonly" />
                    </div>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="oldPassword">Old password</label>
                <div class="controls">
                    <div class="input-prepend">
                        <span class="add-on"><i class="icon-lock"></i></span>
                        <input type="password" id="oldPassword" name="oldPassword" value="" required="required" />
                    </div>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="password">Password</label>
                <div class="controls">
                    <div class="input-prepend">
                        <span class="add-on"><i class="icon-lock"></i></span>
                        <input type="password" id="password" name="password" />
                    </div>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="passwordConfirm">Confirm password</label>
                <div class="controls">
                    <div class="input-prepend">
                        <span class="add-on"><i class="icon-lock"></i></span>
                        <input type="password" id="passwordConfirm" name="passwordConfirm" />
                    </div>
                </div>
            </div>
            <div class="control-group">
                <div class="controls">
                    <input type="submit" class="btn" value="Confirm changes" />
                </div>
            </div>
        </form>
    </jsp:body>
</t:masterpage>