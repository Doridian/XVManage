<%@ tag import="de.doridian.xvmanage.navbar.NavbarLink" %>
<%@ tag description="Master page template" pageEncoding="UTF-8" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="addtitional_head" fragment="true" %>
<jsp:useBean id="loggedInUser" class="de.doridian.xvmanage.models.User" scope="session"/>
<!DOCTYPE html>
<html>
    <head>
        <title>${title} - XVManage</title>
        <script type="text/javascript" src="//cdnjs.cloudflare.com/ajax/libs/jquery/2.0.0/jquery.min.js"></script>
        <script type="text/javascript" src="//cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/2.3.1/js/bootstrap.min.js"></script>
        <script type="text/javascript" src="/static/bootbox.min.js"></script>
        <script type="text/javascript" src="/static/global.js"></script>
        <link rel="stylesheet" type="text/css" href="//cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/2.3.1/css/bootstrap.min.css" />
        <link rel="stylesheet" type="text/css" href="/static/global.css" />
        <jsp:invoke fragment="addtitional_head"/>
    </head>
    <body>
        <div class="navbar">
            <div class="navbar-inner">
                <a class="brand" href="/">XVManage</a>
                <ul class="nav">
                    <%
                        for(NavbarLink link : NavbarLink.NAVBAR_LINKS)
                            if(link.isVisible(loggedInUser))
                                out.write(link.getHTML(request));
                    %>
                </ul>
                <ul class="nav pull-right">
                    <li><a href="#">Welcome, <%=(!loggedInUser.isLoggedInUser()) ? "Guest" : loggedInUser.getName()%></a></li>
                </ul>
            </div>
        </div>

        <div class="container">
            <jsp:doBody/>
        </div>
    </body>
</html>