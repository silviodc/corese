/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.wimmics.coresetimer;

import fr.inria.corese.rdftograph.RdfToGraph;
import fr.inria.corese.rdftograph.RdfToGraph.DbDriver;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgtool.load.SPARQLResult;
import fr.inria.wimmics.coresetimer.CoreseTimer.Profile;
import static fr.inria.wimmics.coresetimer.Main.TestDescription.DB_INITIALIZATION.*;
import java.io.IOException;
import java.time.LocalDateTime;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.openrdf.rio.RDFFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import test.w3c.TestW3C11KGraphNew;
import static fr.inria.corese.coresetimer.utils.VariousUtils.*;
import java.io.File;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author edemairy
 */
public class Main {

	private static Logger logger = Logger.getLogger(Main.class.getName());

	public static class TestDescription {

		public final static int DEFAULT_WARMUP_CYCLES = 5;
		public final static int DEFAULT_MEASURED_SAMPLES = 20;
		private static String INPUT_ROOT;
		private static String OUTPUT_ROOT;

		public static void setInputRoot(String path) {
			INPUT_ROOT = ensureEndWith(path, "/");
		}

		public static String getInputRoot() {
			return INPUT_ROOT;
		}

		public static void setOutputRoot(String path) {
			OUTPUT_ROOT = ensureEndWith(path, "/");
		}

		public static String getOutputRoot() {
			return OUTPUT_ROOT;
		}

		private String testId;
		private String inputMem;
		private RDFFormat format = RDFFormat.NQUADS;
		private String request;
		private String inputDb;
		private final String OUTPUT_FILE_FORMAT = "%s/result_%s.xml";
		private final String RDF_OUTPUT_FILE_FORMAT = "%s/rdf_result_%s_%s.xml";
		private boolean resultsEqual; // Wether the request has returned the same results in memory and db version.
		private DbDriver driver = RdfToGraph.DbDriver.TITANDB;
		private DB_INITIALIZATION dbState = DB_INITIALIZATION.DB_UNINITIALIZED;
		private int warmupCycles;
		private int measuredCycles;

		String getResultFileName(Profile mode) {
			return String.format(RDF_OUTPUT_FILE_FORMAT, TestDescription.getOutputRoot(), testId, mode);
		}

		public enum DB_INITIALIZATION {
			DB_INITIALIZED, // The db is provided already filled.
			DB_UNINITIALIZED  // The db has to be filled with the data of the inputMem file.
		}

		public TestDescription setFormat(RDFFormat newFormat) {
			format = newFormat;
			return this;
		}

		public RDFFormat getFormat() {
			return format;
		}

		private TestDescription(String id) {
			testId = id;
			measuredCycles = DEFAULT_MEASURED_SAMPLES;
			warmupCycles = DEFAULT_WARMUP_CYCLES;
		}

		static public TestDescription build(String id) {
			return new TestDescription(id);
		}

		public String getId() {
			return testId;
		}

		public TestDescription init() throws IOException {
			if (dbState == DB_INITIALIZATION.DB_UNINITIALIZED) {
				RdfToGraph converter = new RdfToGraph().setDriver(driver).convertFileToDb(getInput(), format, getInputDb());
			}
			return this;
		}

		public TestDescription setInput(String input) {
			return this.setInput(input, RDFFormat.NQUADS);
		}

		public TestDescription setInput(String input, RDFFormat format) {
			this.inputMem = input;
			this.format = format;
			inputDb = input.replaceFirst("\\..+", "_db");
			return this;
		}

		public String getInput() {
			return INPUT_ROOT + inputMem;
		}

		public TestDescription setDriver(DbDriver driver) {
			this.driver = driver;
			return this;
		}

		public TestDescription setInputDb(String path, DB_INITIALIZATION dbState) {
			this.inputDb = path;
			this.dbState = dbState;
			return this;
		}

		public String getInputDb() {
			return INPUT_ROOT + inputDb;
		}

		public TestDescription setRequest(String request) {
			this.request = request;
			return this;
		}

		public String getRequest() {
			return request;
		}

		public TestDescription setResultsEqual(boolean result) {
			this.resultsEqual = result;
			return this;
		}

		public Boolean getResultsEqual() {
			return resultsEqual;
		}

		public String getOutputPath() {
			String result = String.format(OUTPUT_FILE_FORMAT, TestDescription.getOutputRoot(), testId);
			return result;
		}

		public TestDescription setWarmupCycles(int n) {
			this.warmupCycles = n;
			return this;
		}

		public int getWarmupCycles() {
			return this.warmupCycles;
		}

		public TestDescription setMeasuredCycles(int n) {
			this.measuredCycles = n;
			return this;
		}

		public int getMeasuredCycles() {
			return this.measuredCycles;
		}
	}

	public final static String[] queries = { // @TODO afficher pour chaque requête le nombre de résultats.
	// @TODO jointure
	// @ select distinct ?p  where {?e ?p ?y}  [order by ?p]
	// @TODO tester les littéraux
	//		"select (count(*) as ?count) where { graph ?g {?x ?p ?y}}",
	//		"select * where {<http://prefix.cc/popular/all.file.vann>  ?p ?y .}",// limit 10000",
	//		"select * where { ?x  a ?y }", // biaisé car beaucoup de données sont typées
	//		"select (count(*) as ?c) where {?x a ?y}", // permet de supprimer le coût de construction du résultat.
	//				"select * where { <http://prefix.cc/popular/all.file.vann>  ?p ?y . ?y ?q <http://prefix.cc/popular/all.file.vann> .} limit 10000"
	//				"select * where { ?x ?p ?y . ?y ?q ?x }" // Intractable: if there are 10^6 edges, requests for 10^12 edges. @TODO Traiter la jointure.
	//		"select ?p( count(?p) as ?c) where {?e ?p ?y} group by ?p order by ?c"
	// Campagne de tests
	// 
	// 
	// Famille de tests 1	
	// BGP: on connait une valeur, la bd sait rechercher cette valeur instantanément ? Efficacité 
	/*		s ?p ?o 
	               ?s ?p o 2 cas principaux :URI (1cas), Literal (String, int, double, 
		humans : avec des requêtes 	
		  X ?p ?y . ?z ?q ?y
		  X ?p ?y . ?y ?q ?z
		 URI ?p ?y . 
		 ?x ?p URI. ?p ?z ?t
		 ?x p ?x

		 tester d'abord les propriétés fixées 
		 tester ensuite les propriétés libres

	Famille de tests 2
		?x ?p ?y . filter(contains(?y, " ")) . ?y ?q ?z  (même chose que précédemment, mais avec des filtres)
	Famille de tests 3 
		Cycles de longueur 2, 3, etc.	

	Famille de tests 4
		tester les 16 cas.

	1. Sémantique
	2. Benchmark
	 */};


	public static boolean compareResults(Mappings map_db, Mappings map_memory) {
		TestW3C11KGraphNew tester = new TestW3C11KGraphNew();
		boolean result = tester.validate(map_db, map_memory);
		return result;
	}

	public static void writeResult(TestDescription test, CoreseTimer timerDb, CoreseTimer timerMemory) {
		Document doc = null;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.newDocument();
			Element rootElement = doc.createElement("TestResult");

			Element inputs = doc.createElement("Inputs");

			Element inputFile = doc.createElement("InputFile");
			Text inputFileText = doc.createTextNode(test.getInput());
			inputFile.appendChild(inputFileText);

			Element dbPath = doc.createElement("DbPath");
			Text dbPathText = doc.createTextNode(test.getInputDb());
			dbPath.appendChild(dbPathText);

			Element request = doc.createElement("Request");
			Text requestText = doc.createTextNode(test.getRequest());
			request.appendChild(requestText);

			Element timestamp = doc.createElement("Timestamp");
			Text timestampText = doc.createTextNode(LocalDateTime.now().toString());
			timestamp.appendChild(timestampText);

			Element[] subElements = {inputFile, dbPath, request, timestamp};
			for (Element e : subElements) {
				inputs.appendChild(e);
			}

			Element outputs = doc.createElement("Outputs");

			Element result = doc.createElement("ResultsEqual");
			Text resultText = doc.createTextNode(test.getResultsEqual().toString());
			result.appendChild(resultText);

			Element statsMemory = doc.createElement("StatsMemory");
			Text statsMemoryText = doc.createTextNode(timerMemory.getStats().toString());
			statsMemory.appendChild(statsMemoryText);

			Element statsDb = doc.createElement("StatsDb");
			Text statsDbText = doc.createTextNode(timerDb.getStats().toString());
			statsDb.appendChild(statsDbText);

			Element[] subElements2 = {result, statsMemory, statsDb};
			for (Element e : subElements2) {
				outputs.appendChild(e);
			}

			rootElement.appendChild(inputs);
			rootElement.appendChild(outputs);

			doc.appendChild(rootElement);

		} catch (ParserConfigurationException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.ERROR, null, ex);
		}

		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(test.getOutputPath()));
			transformer.transform(source, result);
		} catch (TransformerConfigurationException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.ERROR, null, ex);
		} catch (TransformerException ex) {
			Logger.getLogger(Main.class.getName()).log(Level.ERROR, null, ex);
		}
	}

}
