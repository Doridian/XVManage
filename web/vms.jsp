<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="loggedInUser" class="de.doridian.xvmanage.models.User" scope="session"/>
<t:masterpage title="VMs">
    <jsp:attribute name="addtitional_head">
        <script type="text/javascript" src="static/vms.js"></script>
    </jsp:attribute>
    <jsp:body>
        <table class="table">
            <thead>
            <tr>
                <th>Name</th>
                <th>Node</th>
                <th width="200px">CPU</th>
                <th width="200px">Memory</th>
                <th>Controls</th>
            </tr>
            </thead>
            <tbody id="vmsList">
            <jsp:include page="vms_list.jsp"/>
            </tbody>
        </table>
        <div id="appletContainer"></div>
    </jsp:body>
</t:masterpage>