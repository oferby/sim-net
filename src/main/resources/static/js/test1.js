function addHost(hostId) {
    cy.add([
        { group: "nodes", data: { id: hostId }, position: { x: 300, y: 300 }, classes: 'graphNode computeNode' }
    ])

}


$(document).ready(function(){
        $("#input").keypress(function(event){
            console.log(event.which);
            if (event.which == 13) {
                addHost('n2');
            }
        })

    })
// 32 space
// 13 enter

var cy = cytoscape({
    container: document.getElementById('cy'), // container to render in
      style: [
        {
          selector: '.graphNode',
          style: {
            'background-fit': 'cover cover',
            'background-color': 'white'
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
        }
      ]
});

cy.add({
    group: "nodes",
    data: { id: "j", weight: 55 },
    position: { x: 50, y: 50 }
});

var eles = cy.add([
  { group: "nodes", data: { id: "n0" }, position: { x: 100, y: 100 } },
  { group: "nodes", data: { id: "n1" }, position: { x: 200, y: 200 } },
  { group: "edges", data: { id: "e0", source: "n0", target: "n1" } }
]);

var collection = cy.collection();
cy.on("click","node", function(evt){
    var node = evt.target
    collection = collection.add(this);
    console.log(node.id() + ' clicked')
});

//cy.remove("node[weight = 70]");

//cy.pon('tap').then(function( event ) {
//
//    console.log('promise done.')
//})
//
//cy.removeListener('tap');
//
//setTimeout( function(){
//    cy.pan({ x: 50, y: -100 });
//}, 2000 );
//
//setTimeout( function(){
//    cy.zoom( 2 );
//}, 3000 );
//
//setTimeout( function(){
//    cy.reset();
//}, 4000 );
//
//cy.animate({
//  pan: { x: 100, y: 100 },
//  zoom: 2
//}, {
//  duration: 1000
//});

//console.log(cy.json());

cy.nodes().addClass('graphNode');
cy.$('#n0').addClass('computeNode');
cy.$('#n1').addClass('switch');
