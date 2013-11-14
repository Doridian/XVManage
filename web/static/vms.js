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

function vmVNC(name) {
    window.open("/ManageVM.do?action=vnc&vm=" + name, "_blank", "height=480,width=640,directories=no,toolbar=no,status=no,scrollbars=no,resizable=no,menubar=no,location=no");
}

$(document).ready(function() {
    window.setInterval(refreshVMs, 5000);
});