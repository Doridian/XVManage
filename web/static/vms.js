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
    var param = document.createElement("param");
    param.name = name;
    param.value = value;
    applet.appendChild(param);
}

function vmVNC(name) {
    $.getJSON("/ManageVM.do?action=vnc&vm=" + name, function(data) {
        var applet = document.createElement("applet");
        applet.archive = "static/vnc.jar";
        applet.code = "com.glavsoft.viewer.Viewer";
        applet.width = "1";
        applet.height = "1";

        addAppletParam(applet, "Host", data.host);
        addAppletParam(applet, "Port", data.port);
        addAppletParam(applet, "Password", data.password);
        addAppletParam(applet, "SSL", data.ssl ? "yes": "no");

        addAppletParam(applet, "OpenNewWindow", "yes");
        addAppletParam(applet, "AllowAppletInteractiveConnections", "no");

        var appletContainer = document.getElementById("appletContainer");
        appletContainer.innerHTML = "";
        appletContainer.appendChild(applet);
    });
}

$(document).ready(function() {
    window.setInterval(refreshVMs, 5000);
});