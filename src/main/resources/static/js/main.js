function createGraph(){
    var g = cytoscape({
                container: document.getElementById('cy'), // container to render in
                  style: [
                    {
                      selector: '.graphNode',
                      style: {
                        'background-fit': 'cover cover',
                        'background-color': 'white',
                        'label': 'data(id)',
                        'text-valign': 'bottom'
                      }
                    },
                    {
                      selector: '.computeNode',
                      style: {
                        'background-image': 'images/pc.png'
                      }
                    },
                    {
                      selector: '.loadBalancer',
                      style: {
                        'background-image': 'images/lb.png'
                      }
                    },
                    {
                      selector: '.router',
                      style: {
                        'background-image': 'images/router.png'
                      }
                    },
                    {
                        selector: '.switch',
                        style: {
                            'background-image': 'images/switch.png'
                        }
                    },
                    {
                        selector: '.acl',
                        style: {
                            'background-image': 'images/acl.png'
                        }
                    },
                    {
                      selector: '.internet',
                      style: {
                        'background-image': 'images/world.png'
                        }
                    },
                    {
                      selector: '.subnet',
                      style: {
                        'background-image': 'images/cloud.png'
                      }
                    },
                    {
                      selector: '.gateway',
                      style: {
                        'background-image': 'images/gateway.png'
                      }
                    },
                    {
                      selector: '.application',
                      style: {
                        'background-image': 'images/application.png'
                      }
                    },
                    {
                      selector: '.firewall',
                      style: {
                        'background-image': 'images/firewall.png'
                      }
                    },
                     {
                       selector: '.policyAllow',
                       style: {
                         'background-image': 'images/policyAllow.png'
                       }
                     },
                     {
                          selector: '.policyDeny',
                          style: {
                            'background-image': 'images/policyDeny.png'
                          }
                        },{
                         selector: '.policy',
                         style: {
                           'background-image': 'images/policy.png'
                         }
                       },

                     {
                       selector: '.group',
                       style: {
                         'background-image': 'images/group.png',
                         'shape': 'rectangle'
                       }
                     }

                  ]
            });

    return g;

}

var cy = createGraph();

var lastCommand = '';

var stompClient = null;

var hasAlert = false;

var hasUnknown = false;

function connect() {
    var socket = new SockJS('/intent-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/hint', function (hint) {
            var res = JSON.parse(hint.body);
            if (res.status == 'DONE') {
                sendIntent(res);
            } else if (res.status == 'LOCAL') {
                doLocal(res);
            } else if (res.status == 'INFO') {
                doInfo(res);
            } else if (res.status == 'ERROR') {
                showAlert(res.hint);
            } else {
                inputTextHint(res.hint);
            }

        });
        stompClient.subscribe('/topic/graph', function (intent) {
            var res = JSON.parse(intent.body);
            addToGraph(res);
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function doLocal(res) {

    switch (res.intent) {

        case "clear":
            cy.destroy()
            console.log("graph destroyed");
            cy = createGraph();
            break;
         case "disconnectNodes":
            var edgeId = "#" + res.params.id;
            cy.$(edgeId).remove();
            break;
         case "deleteNode":
            var nodeId = "#" + res.params.id;
            cy.$(nodeId).remove();
            break;

        default:
            console.log('unknown local intent');
            break;
    }

}

function sendinfo(info) {
    var $info = $('#info');
    $info.css('z-index', '-1');
    $info.css('opacity', '0');
    $info.css('bottom', '0%');
    stompClient.send("/app/getHint", {}, JSON.stringify(info));
}


function doInfo(res) {

    if (res.params['type'] == 'option') {

        var $info = $('#info');
        $info.text(res.params['question'] + "  ");
        var $btn = null;
        var ss = res.params['options'].split(",");
        for (var i in ss) {
             var param = ss[i];
             $btn = $('<button/>', {
                text: param,
                id: 'btn_'+i,
                click: function () {
                    res.params[res.params['param']] = this.textContent;
                    sendinfo(res);
                }
            });

            $info.append($btn);
        }

        $info.css('opacity', '1');
        $info.css('bottom', '125%');
        $info.css('z-index', '2');

    } else if (res.params['type'] == 'yesno') {

        var $info = $('#info');
        $info.text(res.params['question'] + "  ");

        var $btn = null;

        $btn = $('<button/>', {
           text: "yes",
           id: 'btn_yes',
           click: function () {
               res.params[res.params['param']] = "yes";
               sendinfo(res);
           }
        });

        $info.append($btn);

        $btn = $('<button/>', {
           text: "no",
           id: 'btn_no',
           click: function () {
               res.params[res.params['param']] = "no";
               sendinfo(res);
           }
        });

        $info.append($btn);

        $info.css('opacity', '1');
        $info.css('bottom', '125%');
        $info.css('z-index', '2');

    } else if (res.params['type'] == 'unknownRequest') {

        var $info = $('#info');
        $info.text(res.params['question']);

        $info.css('opacity', '1');
        $info.css('bottom', '125%');
        $info.css('z-index', '2');

        hasUnknown = true;

    }

}


function addToGraph(graphNode) {
    cy.add(graphNode);
    cy.layout({name:'breadthfirst'}).run()
}

function getHintFromWit(text) {

    $.ajax({
      url: 'https://api.wit.ai/message',
      data: {
        'q': text,
        'access_token' : 'ZXSURWM4YGEVFFV6QENTBUK6PXUV2TBR'
      },
      dataType: 'jsonp',
      method: 'GET',
      success: function(response) {
          console.log("success!", response);
      }
    });

}

function getHint(isDone) {

    var msg = null;
    if (isDone) {
        msg = JSON.stringify({
                          'hint': $("#input").val(),
                          'status': 'ENTERED'
                      });
    } else {
        msg  = JSON.stringify({
                           'hint': $("#input").val(),
                           'status': 'HINT'
                       });
    }

    stompClient.send("/app/getHint", {}, msg);
}

function sendIntent(intent){
    console.log('Sending final intent')
    stompClient.send("/app/intent", {}, JSON.stringify(intent));
}

function inputTextHint(inputText){
    $('#hint').text(inputText);
}


function showAlert(alertText) {
    var $warning = $('#warning');
    $warning.text(alertText);
    $warning.css('opacity', '1');
    $warning.css('bottom', '125%');

    hasAlert = true;
}

function clearAlert() {

    var $warning = $('#warning');
    $warning.text("");
    $warning.css('opacity', '0');
    $warning.css('bottom', '0%');
    hasAlert = false;

}

function clearUnknown() {

    var $info = $('#info');
    $info.text('');
    $info.css('opacity', '0');
    $info.css('bottom', '0%');
    $info.css('z-index', '-1');
    hasUnknown = false;

}

$(document).ready(function(){
    connect();
    $("#input").keyup(function(event){

        if (event.which == 13) {
            if (hasAlert)
                clearAlert();

            if (hasUnknown)
                clearUnknown();

            lastCommand = $("#input").val();
            $('#hint').text('') ;
            $('#hint').css('opacity', '0');
//            getHintFromWit($("#input").val());
            getHint(true);
            $("#input").val('');
            return;
        }

        if (event.which == 38 || event.which == 40) {
            $("#input").val(lastCommand);
            $('#hint').text(lastCommand);
            return;
        }
    })

})
// 32 space
// 13 enter
