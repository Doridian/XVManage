/*jslint white: false */
/*global window, $, Util, RFB, */
"use strict";

// Load supporting scripts
Util.load_scripts(["webutil.js", "base64.js", "websock.js", "des.js", "input.js", "display.js", "jsunzip.js", "rfb.js"]);

var rfb;

function passwordRequired(rfb) {
    rfb.sendPassword(VNC_PASSWORD);
    return false;
}
function sendCtrlAltDel() {
    rfb.sendCtrlAltDel();
    return false;
}
function updateState(rfb, state, oldstate, msg) {
    var s, sb, cad, level;
    s = $D('noVNC_status');
    sb = $D('noVNC_status_bar');
    cad = $D('sendCtrlAltDelButton');
    switch (state) {
        case 'failed':       level = "error";  break;
        case 'fatal':        level = "error";  break;
        case 'normal':       level = "normal"; break;
        case 'disconnected': level = "normal"; break;
        case 'loaded':       level = "normal"; break;
        default:             level = "warn";   break;
    }

    if (state === "normal") { cad.disabled = false; }
    else                    { cad.disabled = true; }

    console.log(rfb);

    if (typeof(msg) !== 'undefined') {
        sb.setAttribute("class", "noVNC_status_" + level);
        s.innerHTML = msg;
    }
}

function FBResize(rfb, width, height) {
    resizeViewPort(width, height + 36);
}

var sizeMayChange = false;
var size = [window.width,window.height];

window.onresize = function() {
    if(sizeMayChange) return;
    window.resizeTo(size[0],size[1]);
};

function resizeViewPort(width, height) {
    sizeMayChange = true;
    if (window.outerWidth) {
        size = [width + (window.outerWidth - window.innerWidth), height + (window.outerHeight - window.innerHeight)];
    } else {
        window.resizeTo(500, 500);
        size = [width + (500 - document.body.offsetWidth), height + (500 - document.body.offsetHeight)];
    }
    window.resizeTo(size[0], size[1]);
    sizeMayChange = false;
}

window.onscriptsload = function () {
    $D('sendCtrlAltDelButton').style.display = "inline";
    $D('sendCtrlAltDelButton').onclick = sendCtrlAltDel;

    rfb = new RFB({'target':       $D('noVNC_canvas'),
        'encrypt':      true, //(window.location.protocol === "https:"),
        'repeaterID':   '',
        'true_color':   true,
        'local_cursor': true,
        'shared':       false,
        'view_only':    false,
        'updateState':  updateState,
        'onFBResize' : FBResize,
        'onPasswordRequired':  passwordRequired});
    rfb.connect(VNC_HOST, VNC_PORT, '', 'websockify');
};