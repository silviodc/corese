/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.db_memory;

import static fr.inria.corese.coresetimer.utils.VariousUtils.ensureEndWith;
import static fr.inria.corese.coresetimer.utils.VariousUtils.getEnvWithDefault;
import fr.inria.wimmics.coresetimer.CoreseTimer;
import fr.inria.wimmics.coresetimer.Main.TestDescription;
import fr.inria.wimmics.coresetimer.Main.TestSuite;
import static fr.inria.wimmics.coresetimer.Main.compareResults;
import static fr.inria.wimmics.coresetimer.Main.writeResult;
import java.io.IOException;
import org.openrdf.rio.RDFFormat;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 *
 * @author edemairy
 */
public class QualitativeTest {
	String inputRoot;
	String outputRoot;

	public QualitativeTest() {
	}

	@DataProvider(name = "getResults", parallel = false)
	public Object[][] getResults() {
		TestSuite suite = TestSuite.build("base_tests").
			setWarmupCycles(2).
			setMeasuredCycles(5).
			setInput("human_2007_04_17.rdf", RDFFormat.RDFXML).
			setInputDb("/human_db").
			setInputRoot(inputRoot).
			setOutputRoot(outputRoot);
//			createDb();
		TestDescription[][] tests = {
			{suite.buildTest("select ?p( count(?p) as ?c) where {?e ?p ?y} group by ?p order by ?c")},
			{suite.buildTest("SELECT ?x ?t WHERE { ?x rdf:type ?t }")},
			{suite.buildTest("SELECT ?x ?t WHERE { ?x rdf:type rdfs:Class }")},
			{suite.buildTest("SELECT ?x ?t WHERE { ?x rdfs:subClassOf ?y }")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#> \nSELECT * WHERE { ?x humans:hasSpouse ?y}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n SELECT * WHERE { ?x humans:hasSpouse ?y . ?x rdf:type humans:Male}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n SELECT * WHERE { ?x humans:hasSpouse ?y . ?y rdf:type humans:Male}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n SELECT (count(*) as ?count) WHERE { ?y humans:hasFriend ?z }")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n SELECT ?x WHERE { { ?y humans:hasChild ?x } UNION { ?x humans:hasParent ?y }}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT ?person ?age\n" + "WHERE\n" + "{\n" + " ?person rdf:type humans:Person\n" + " OPTIONAL { ?person humans:age ?age }\n" + "}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT ?x\n" + "WHERE\n" + "{\n" + " ?x humans:age ?age\n" + " FILTER ( xsd:integer(?age) >= 18 )\n" + "}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "ASK\n" + "WHERE\n" + "{\n" + " <http://www.inria.fr/2007/04/17/humans.rdfs-instances#Mark> humans:age ?age\n" + " FILTER ( xsd:integer(?age) >= 18 )\n" + "}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT ?x ?t\n" + "WHERE\n" + "{\n" + "  ?x rdf:type humans:Lecturer .\n" + "  ?x rdf:type ?t \n" + "}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT ?x ?t\n" + "WHERE\n" + "{\n" + " ?x rdf:type humans:Male .\n" + " ?x rdf:type humans:Person .\n" + " ?x rdf:type ?t\n" + "}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT distinct ?person\n" + "WHERE\n" + "{\n" + " {\n" + "  ?person rdf:type humans:Lecturer\n" + " }\n" + " UNION\n" + " {\n" + "  ?person rdf:type humans:Researcher\n" + " }\n" + "}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT distinct ?person\n" + "WHERE\n" + "{\n" + " {\n" + "  ?person rdf:type humans:Lecturer\n" + " }\n" + " UNION\n" + " {\n" + "  ?person rdf:type humans:Researcher\n" + " }\n" + "}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT ?x\n" + "WHERE\n" + "{\n" + " ?x rdf:type humans:Person\n" + " OPTIONAL\n" + " {\n" + "   ?x rdf:type ?t\n" + "   FILTER ( ?t = humans:Researcher )\n" + " }\n" + " FILTER ( ! bound( ?t ) )\n" + "}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT ?x ?y \n" + "WHERE\n" + "{\n" + " ?x humans:hasAncestor ?y\n" + "}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT *\n" + "WHERE\n" + "{\n" + "  ?t rdfs:label \"size\"@en .\n" + "  ?t rdfs:label ?le .\n" + "  ?t rdfs:comment ?ce .\n" + "  FILTER ( lang(?le) = 'en' && lang(?ce) = 'en' )\n" + "}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT * \n" + "WHERE\n" + "{\n" + "  ?t rdfs:label \"person\"@en .\n" + "  ?t rdfs:label ?syn .\n" + "  FILTER ( ?syn != \"person\"@en && lang(?syn) = 'en' )\n" + "}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT ?lf \n" + "WHERE\n" + "{\n" + "  ?t rdfs:label \"shoe size\"@en .\n" + "  ?t rdfs:label ?lf .\n" + "  FILTER ( lang(?lf) = 'fr' )\n" + "}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT *\n" + "WHERE\n" + "{\n" + "  ?laura humans:name \"Laura\" .\n" + "  ?type rdfs:label ?l .\n" + "  {\n" + "   {\n" + "    ?laura rdf:type ?type\n" + "   }\n" + "   UNION\n" + "   {\n" + "    {\n" + "     ?laura ?type ?with\n" + "    }\n" + "    UNION\n" + "    {\n" + "     ?from ?type ?laura\n" + "    }\n" + "   }\n" + "  }\n" + "  FILTER ( lang(?l) = 'en' )\n" + "}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "DESCRIBE ?laura\n" + "WHERE\n" + "{\n" + "  ?laura humans:name \"Laura\" .\n" + "}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "CONSTRUCT \n" + "{\n" + " ?x rdf:type humans:Man\n" + "}\n" + "WHERE\n" + "{\n" + " {\n" + "  ?x rdf:type humans:Man\n" + " }\n" + "  UNION\n" + " {\n" + "  ?x rdf:type humans:Male .\n" + "  ?x rdf:type humans:Person\n" + " }\n" + "}")},
			{suite.buildTest("PREFIX humans: <http://www.inria.fr/2007/04/17/humans.rdfs#>\n" + "SELECT * WHERE\n" + "{\n" + " ?x rdf:type humans:Person .\n" + " ?x humans:name ?name .\n" + " FILTER ( regex(?name, '.*ar.*') )\n" + "}")}, 
//		TestDescription.build("1m_select_s_1").setWarmupCycles(2).setMeasuredCycles(5).setInput("btc-2010-chunk-000.nq").setInputDb("/1m_db", DB_INITIALIZED).setRequest("select * where {<http://www.janhaeussler.com/?sioc_type=user&sioc_id=1>  ?p ?y .}")
		};
		return tests;
	}

	@DataProvider(name = "getResultsPerfAnalysis")
	public Object[][] getResultsPerfAnalysis() {
		TestSuite testRoot = TestSuite.build("perf_analysis").
			setWarmupCycles(2).
			setMeasuredCycles(5).
			setInput("human_2007_04_17.rdf").
			setInputDb("/human_db").
			createDb();
		TestDescription[][] tests = {
			{testRoot.buildTest("select (count(*) as ?count) where { graph ?g {?x ?p ?y}}")},
			{testRoot.buildTest("select (count(*) as ?c) where {?x a ?y}")},
			{testRoot.buildTest("select * where { ?x ?p ?y . ?y ?q ?x }")}
		};
		return tests;
	}

	@DataProvider(name = "getResults_1M", indices = {0})
	public Object[][] getResultsLong() {
		TestSuite rootTest = TestSuite.build("rootTest").setWarmupCycles(2).setMeasuredCycles(5).setInput("btc-2010-chunk-000_0001.nq").setInputDb("/1m_db");
		TestDescription[][] tests = {
			{rootTest.buildTest("select ?p( count(?p) as ?c) where {?e ?p ?y} group by ?p order by ?c")},
			{rootTest.buildTest("select * where {<http://prefix.cc/popular/all.file.vann>  ?p ?y .}")}, //			{TestDescription.build("1m_select_s_1").setWarmupCycles(2).setMeasuredCycles(5).setInput("btc-2010-chunk-000_0001.nq").setInputDb("/1m_db", DB_INITIALIZED).setRequest("select * where {<http://www.janhaeussler.com/?sioc_type=user&sioc_id=1>  ?p ?y .}")},
		//			{TestDescription.build("1m_cycle").setWarmupCycles(2).setMeasuredCycles(5).setInput("btc-2010-chunk-000_0001.nq").setInputDb("/1m_db", DB_INITIALIZED).setRequest("select * where { ?x ?p ?y . ?y ?q ?x }")}
		};
		return tests;
	}

	@DataProvider(name = "getResults_10m")
	public Object[][] getResults10m() {
		TestSuite rootTest = TestSuite.build("rootTest").setWarmupCycles(2).setMeasuredCycles(5).setInput("btc-2010-chunk-000_10000.nq").setInputDb("/10m_db").createDb();
		TestDescription[][] tests = {
			{rootTest.buildTest("select ?x ?p ?y ?q where { ?x ?p ?y . ?y ?q ?x}")}
		};
		return tests;
	}

	@BeforeClass
	public void setup() {
		inputRoot = getEnvWithDefault("INPUT_ROOT", "./data/");
		inputRoot = ensureEndWith(inputRoot, "/");
		outputRoot = getEnvWithDefault("OUTPUT_ROOT", "./outputs/");
		outputRoot = ensureEndWith(inputRoot, "/");
	}

	@Test(dataProvider = "getResults_10m", groups = "")
	public static void testBasic(TestDescription test) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
		CoreseTimer timerDb = new CoreseTimer().setMode(CoreseTimer.Profile.DB).init().run(test);
		CoreseTimer timerMemory = new CoreseTimer().setMode(CoreseTimer.Profile.MEMORY).init().run(test);
		System.out.println("running test: " + test.getId());
		boolean result = compareResults(timerDb.getMapping(), timerMemory.getMapping());
		test.setResultsEqual(result);
		writeResult(test, timerDb, timerMemory);
		assertTrue(result, test.getId());
	}

}
