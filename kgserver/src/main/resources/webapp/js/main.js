/** 
* Controler code for the Corese/KGRAM web application
*
* author : alban.gaignard@cnrs.fr
*/

// The root URL for the RESTful services
var rootURL = "http://localhost:8080/kgram";

var statVOID = ["SELECT (COUNT(*) AS ?no) { ?s ?p ?o  }", 
"SELECT (COUNT(distinct ?s) AS ?no) { ?s a []  }",
"SELECT (COUNT(DISTINCT ?s ) AS ?no) { { ?s ?p ?o  } UNION { ?o ?p ?s } FILTER(!isBlank(?s) && !isLiteral(?s)) }",
"SELECT (COUNT(distinct ?o) AS ?no) { ?s rdf:type ?o }",
"SELECT (count(distinct ?p) AS ?no) { ?s ?p ?o }",
"SELECT (COUNT(DISTINCT ?s ) AS ?no) {  ?s ?p ?o }",
"SELECT (COUNT(DISTINCT ?o ) AS ?no) {  ?s ?p ?o  filter(!isLiteral(?o)) }",
"SELECT DISTINCT ?type { ?s a ?type }",
"SELECT DISTINCT ?p { ?s ?p ?o }",
"SELECT  ?class (COUNT(?s) AS ?count ) { ?s a ?class } GROUP BY ?class ORDER BY ?count",
"SELECT  ?p (COUNT(?s) AS ?count ) { ?s ?p ?o } GROUP BY ?p ORDER BY ?count",
"SELECT  ?p (COUNT(DISTINCT ?s ) AS ?count ) { ?s ?p ?o } GROUP BY ?p ORDER BY ?count",
"SELECT  ?p (COUNT(DISTINCT ?o ) AS ?count ) { ?s ?p ?o } GROUP BY ?p ORDER BY ?count" ];

$('#btnReset').click(function() {
	reset();
	//return false;
});

$('#btnLoad').click(function() {
	//reset();
	load($('#txtLoad').val());
	//return false;
});

$('#btnQuery').click(function() {
	//reset();
	sparql($('#sparqlTextArea').val());
});

//todo validate URL
$('#btnDataSource').click(function() {
	addDataSource($('#txtDataSource').val());
});

$('#sparqlTextArea').val(statVOID[0]);
$('#VOIDSparql_Select').on('change', function (e) {
    var query = statVOID[$(this).val()];
    //console.log(query);
    $('#sparqlTextArea').val(query);
});

$('#tbDataSources').on("click", "#testBtn", function(e) {
	var row = $(this).closest("tr");
	var endpoint = row.children(":first").html(); // table row ID 
	testEndpoint(endpoint, row.index());
});

$('#tbDataSources').on("click", "#delBtn", function(e) {
	var endpoint = $(this).closest("tr").children(":first").html(); // table row ID 
	deleteEndpoint(endpoint);
	$(this).parent().parent().remove();
});

//
function reset() {
	console.log('reset');
	$.ajax({
		type: 'POST',
		url: rootURL + '/sparql/reset',
		data: {'entailments': $('#checkEntailments').prop('checked')},
		dataType: "text",
		success: function(data, textStatus, jqXHR){
			infoSuccess('Corese/KGRAM graph reset done (entailments: '+$('#checkEntailments').prop('checked')+').');
			console.log(data);
			$('#checkEntailments').prop('checked', false);
		},
		error: function(jqXHR, textStatus, errorThrown){
			infoError('Corese/KGRAM error: ' + textStatus);
			console.log(errorThrown);
		}
	});
}

function load() {
	console.log('loading '+$('#txtLoad').val());
	$.ajax({
		type: 'POST',
		url: rootURL + '/sparql/load',
		//data: JSON.stringify({'remote_path': $('#txtLoad').val()}),
		data: {'remote_path': $('#txtLoad').val()},
		dataType: "text",
		success: function(data, textStatus, jqXHR){
			console.log(data);
			infoSuccess("Loading done.");
		},
		error: function(jqXHR, textStatus, errorThrown){
			infoError('Corese/KGRAM error: ' + textStatus);
			console.log(errorThrown);
			console.log(jqXHR);
		}
	});
}

function sparql(sparqlQuery) {
	console.log('sparql '+sparqlQuery);
	$.ajax({
		type: 'GET',
		headers: { 
        	Accept : "application/sparql-results+json"
    	},
		url: rootURL + '/sparql',
		data: {'query':sparqlQuery},
		//dataType: "application/sparql-results+json",
		dataType: "json",
		success: function(data, textStatus, jqXHR){
			//console.log(data);
			//console.log(data.results.bindings);
			renderList(data);
		},
		error: function(jqXHR, textStatus, errorThrown){
			infoError("SPARQL querying failure: "+textStatus);
			console.log(errorThrown);
			console.log(jqXHR);
		}
	});
}

function addDataSource(endpointURL) {
	console.log(endpointURL);
	console.log();
	$('#tbDataSources tbody').append("<tr> <td>"+endpointURL+"</td><td><button id=\"testBtn\" class=\"btn btn-mini btn-success\" type=button>Test</button></td> <td><button id=\"delBtn\" class=\"btn btn-mini btn-danger\" type=button>Delete</button></td> </tr>");
	testEndpoint(endpointURL,$('#tbDataSources tbody tr:last').index());
}

function testEndpoint(endpointURL, rowIndex){
	console.log("Testing "+endpointURL+" endpoint !");
	var testQuery = "SELECT * where {?x ?p ?y} LIMIT 10"
	$.ajax({
		type: 'GET',
		headers: { 
        	Accept : "application/sparql-results+json"
    	},
		url: endpointURL,
		data: {'query':testQuery},
		dataType: "json",
		success: function(data, textStatus, jqXHR){
			infoSuccess(endpointURL+" responds to SPARQL queries");
			//update the icon of the data source
			$('#tbDataSources tbody tr:eq('+rowIndex+')').append('<td><i class=\"icon-ok\"></i></td>');
		},
		error: function(jqXHR, textStatus, errorThrown){
			infoError(endpointURL+" does not responds to SPARQL queries");
			//update the icon of the data source
			$('#tbDataSources tbody tr:eq('+rowIndex+')').append('<td><i class=\"icon-warning-sign\"></i></td>');
		}
	});
}

function deleteEndpoint(endpointURL){
	console.log("Deleting "+endpointURL+" endpoint !");
}

function renderList(data) {
	// JAX-RS serializes an empty list as null, and a 'collection of one' as an object (not an 'array of one')
	var listVal = data.results.bindings == null ? [] : (data.results.bindings instanceof Array ? data.results.bindings : [data.results.bindings]);
	var listVar = data.head.vars == null ? [] : (data.head.vars instanceof Array ? data.head.vars : [data.head.vars]);


	$('#tbRes thead tr').remove();
	$('#tbRes tbody tr').remove();

	//Rendering the headers
	var tableHeader = '<tr>';
	$.each(listVar, function(index, item) {
		tableHeader = tableHeader + '<th>?'+item+'</th>';
	});
	tableHeader = tableHeader +'</tr>';
	$('#tbRes thead').html(tableHeader);

	//Rendering the values
	$.each(listVal, function(index, item) {
		var row = "<tr>";
		$.each(item, function(name, v) {
    		/// do stuff
    		row = row + "<td>"+v.value+"</td>";
    		//console.log(name + '=' + v.value);
  		});
		row = row + "</tr>";
		$('#tbRes tbody').append(row); 
	});
}

function infoWarning(message){
	var html = "<div class=\"alert alert\"><button type=\"button\" class=\"close\" data-dismiss=\"alert\">&times;</button><strong>Warning!</strong> "+message+"</div>";
	$('#footer').prepend(html);
}
function infoSuccess(message){
	var html = "<div class=\"alert alert-success\"><button type=\"button\" class=\"close\" data-dismiss=\"alert\">&times;</button><strong>Success!</strong> "+message+"</div>";
	$('#footer').prepend(html);
}
function infoError(message){
	var html = "<div class=\"alert alert-error\"><button type=\"button\" class=\"close\" data-dismiss=\"alert\">&times;</button><strong>Error!</strong> "+message+"</div>";
	$('#footer').prepend(html);
}