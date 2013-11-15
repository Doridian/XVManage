function refreshVMs() {
    $.get("vms_list.jsp?ajax=1&date=" + (new Date().getTime()), function(data) {
        $('#vmsList').html(data);
    });
}

function vmAction(name, action) {
    $('#managevm_'+name+' *').prop('disabled', true);
    $.post('/ManageVM.do', 'ajax=1&action='+action+'&vm=' + name, function(data) {
        $('#managevm_'+name+' *').prop('disabled', false);
        refreshVMs();
    }).fail(function() {
        bootbox.alert("Error "+action+"ing node :(");
        $('#managevm_'+name+' *').prop('disabled', false);
        refreshVMs();
    });
}

function addAppletParam(applet, name, value) {
    var param = document.createElement("argument");
    param.appendChild(document.createTextNode("-" + name + "=" + value));
    //param.setAttribute("name", name);
    //param.setAttribute("value", value);
    applet.appendChild(param);
}

function vmVNC(name) {
    $.getJSON("/ManageVM.do?action=vnc&vm=" + name, function(data) {
        var information = document.createElement("information");
        var title = document.createElement("title");
        title.appendChild(document.createTextNode("TightVNC viewer"));
        var vendor = document.createElement("vendor");
        vendor.appendChild(document.createTextNode("Mark Dietzer"));
        information.appendChild(title);
        information.appendChild(vendor);

        var resource = document.createElement("resources");
        var java = document.createElement("java");
        java.setAttribute("version", "1.6+");
        var jar = document.createElement("jar");
        jar.setAttribute("href", "vnc.jar");
        jar.setAttribute("main", "true");
        resource.appendChild(java);
        resource.appendChild(jar);

        var security = document.createElement("security");
        var allPermissions = document.createElement("all-permissions");
        security.appendChild(allPermissions);

        var applet = document.createElement("application-desc");

        addAppletParam(applet, "host", data.host);
        addAppletParam(applet, "port", data.port);
        addAppletParam(applet, "Password", data.password);
        addAppletParam(applet, "SSL", data.ssl ? "yes": "no");

        addAppletParam(applet, "OpenNewWindow", "no");
        addAppletParam(applet, "AllowAppletInteractiveConnections", "no");

        var appletContainer = document.implementation.createDocument("", "jnlp");
        appletContainer.documentElement.setAttribute("codebase", document.location.protocol + "//" + document.location.host + "/static/");
        appletContainer.documentElement.setAttribute("spec", "1.0+");
        appletContainer.documentElement.appendChild(information);
        appletContainer.documentElement.appendChild(security);
        appletContainer.documentElement.appendChild(resource);
        appletContainer.documentElement.appendChild(applet);
        var blob = new Blob([new XMLSerializer().serializeToString(appletContainer)], {type: "application/x-java-jnlp-file;charset=utf-8"});
        saveAs(blob, "vnc.jnlp");
    });
}

$(document).ready(function() {
    window.setInterval(refreshVMs, 5000);
});