<%@ page import="de.doridian.xvmanage.models.LibvirtVM" %>
<%@ page import="java.util.Collection" %>
<%@ page import="de.doridian.xvmanage.XVMUtils" %>
<jsp:useBean id="loggedInUser" class="de.doridian.xvmanage.models.User" scope="session"/>
<%
    Collection<LibvirtVM> vms = loggedInUser.getMachines();
    for(LibvirtVM vm : vms) {
%>
<tr>
    <td><%=vm.getName()%></td>
    <td><%=vm.getNode().getName()%></td>
    <% if(vm.isPoweredOn()) { %>
        <td><%=XVMUtils.formatProgressBar(vm.getCpuUsage() / (float)vm.getVcpus())%></td>
        <td><%=XVMUtils.formatProgressBar(vm.getRamUsage())%></td>
    <% } else { %>
        <td>Offline</td>
        <td>Offline</td>
    <% } %>
    <td>
        <div id="managevm_<%=vm.getName()%>">
            <button onclick="vmAction('<%=vm.getName()%>', 'start'); return false;" class="btn btn-success" <%if(vm.isPoweredOn())out.print("disabled=\"disabled\"");%>>Boot</button>
            <button onclick="vmAction('<%=vm.getName()%>', 'shutdown'); return false;" class="btn btn-danger" <%if(!vm.isPoweredOn())out.print("disabled=\"disabled\"");%>>Shutdown</button>
            <button onclick="vmAction('<%=vm.getName()%>', 'destroy'); return false;" class="btn btn-danger" <%if(!vm.isPoweredOn())out.print("disabled=\"disabled\"");%>>Power off</button>
            <button onclick="vmAction('<%=vm.getName()%>', 'reboot'); return false;" class="btn btn-warning" <%if(!vm.isPoweredOn())out.print("disabled=\"disabled\"");%>>Reboot</button>
            <button onclick="vmVNC('<%=vm.getName()%>'); return false;" class="btn btn-primary" <%if(!vm.isPoweredOn())out.print("disabled=\"disabled\"");%>>VNC</button>
        </div>
    </td>
</tr>
<%
    }
%>