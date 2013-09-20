<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:masterpage title="Admin Console">
    <jsp:body>
        <textarea rows="20" class="btn-block" id="codeEntry"></textarea><br />
        <button class="btn btn-large btn-block btn-primary" id="codeRun">Run</button>
        <script type="text/javascript">
            $(document).ready(function() {
                $("#codeRun").click(function() {
                    $.post('/AdminRun.do', "code=" + encodeURIComponent($('#codeEntry').val()), function(data) {
                        bootbox.alert("Result:<br />" + data.replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/\n/g, "<br />\n"));
                    });
                });
            });
        </script>
    </jsp:body>
</t:masterpage>