/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import fr.inria.acacia.corese.api.IEngine;
import fr.inria.acacia.corese.api.EngineFactory;
import fr.inria.acacia.corese.api.IResults;
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgengine.GraphEngine;
import fr.inria.edelweiss.kgramenv.util.QueryExec;
import java.io.File;
import java.net.MalformedURLException;
import org.apache.commons.lang.time.StopWatch;

/**
 *
 * @author gaignard
 */
public class G5KdbpediaStandalone {

    public static void main(String args[]) throws MalformedURLException, EngineException {
//        final RemoteProducer kg1 = RemoteProducerServiceClient.getPort("http://"+args[0]+":8090/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
//        final RemoteProducer kg2 = RemoteProducerServiceClient.getPort("http://"+args[1]+":8090/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
//        final RemoteProducer kg3 = RemoteProducerServiceClient.getPort("http://"+args[2]+":8090/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
//        final RemoteProducer kg4 = RemoteProducerServiceClient.getPort("http://"+args[3]+":8090/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");

//        kg1.initEngine();
//        kg2.initEngine();
//        kg3.initEngine();
//        kg4.initEngine();

        File rep1 = new File("/home/agaignard/data/1.7M/1-stores/persondata.1.rdf");
//        File rep2 = new File("/home/agaignard/data/1.7M-4-prop/dbpediaBirthPlace.rdf");
//        File rep3 = new File("/home/agaignard/data/1.7M-4-prop/dbpediaNames.rdf");
//        File rep4 = new File("/home/agaignard/data/1.7M-4-prop/dbpediaRemaining.rdf");

        System.out.println(rep1.getAbsolutePath());

//        Map<String, Object> reqCtxt1 = ((BindingProvider) kg1).getRequestContext();
//        reqCtxt1.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//        reqCtxt1.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);

//        Map<String, Object> reqCtxt2 = ((BindingProvider) kg2).getRequestContext();
//        reqCtxt2.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//        reqCtxt2.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
//        
//        Map<String, Object> reqCtxt3 = ((BindingProvider) kg3).getRequestContext();
//        reqCtxt3.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//        reqCtxt3.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
//        
//        Map<String, Object> reqCtxt4 = ((BindingProvider) kg4).getRequestContext();
//        reqCtxt4.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 1024);
//        reqCtxt4.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);

//        final DataHandler data1 = new DataHandler(new FileDataSource(rep1));
//        final DataHandler data2 = new DataHandler(new FileDataSource(rep2));
//        final DataHandler data3 = new DataHandler(new FileDataSource(rep3));
//        final DataHandler data4 = new DataHandler(new FileDataSource(rep4));

//        ExecutorService executor = Executors.newCachedThreadPool();
//        executor.submit(new Runnable() {
//            @Override
//            public void run() {
//                kg1.uploadRDF(data1);
//
//            }
//        });
//        executor.submit(new Runnable() {
//            @Override
//            public void run() {
//                kg2.uploadRDF(data2);
//
//            }
//        });
//        executor.submit(new Runnable() {
//            @Override
//            public void run() {
//                kg3.uploadRDF(data3);
//
//            }
//        });
//        executor.submit(new Runnable() {
//            @Override
//            public void run() {
//                kg4.uploadRDF(data4);
//
//            }
//        });
//        executor.shutdown();
//        while (!executor.isTerminated()) {
//        }

        ///////////////////////
        EngineFactory ef = new EngineFactory();
        IEngine engine = ef.newInstance();
        engine.load(rep1.getAbsolutePath());

        QueryExec exec = QueryExec.create(engine);

//        exec.addRemote(new URL("http://"+args[0]+":8090/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
//        exec.addRemote(new URL("http://"+args[1]+":8090/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
//        exec.addRemote(new URL("http://"+args[2]+":8090/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
//        exec.addRemote(new URL("http://"+args[3]+":8090/kgserver-1.0.2-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));

        for (int i = 0; i < 4; i++) {
            StopWatch sw = new StopWatch();
            sw.start();
            IResults res = exec.SPARQLQuery(Queries.QueryBobbyA);
//            IResults res = exec.SPARQLQuery(Queries.QueryBob);
            System.out.println("--------");
            System.out.println("Results in " + sw.getTime() + "ms");
            GraphEngine gEng = (GraphEngine) engine;
            System.out.println("Graph size " + gEng.getGraph().size());
            System.out.println("Results size " + res.size());
            String[] variables = res.getVariables();

//            for (Enumeration<IResult> en = res.getResults(); en.hasMoreElements();) {
//                IResult r = en.nextElement();
//                HashMap<String, String> result = new HashMap<String, String>();
//                for (String var : variables) {
//                    if (r.isBound(var)) {
//                        IResultValue[] values = r.getResultValues(var);
//                        for (int j = 0; j < values.length; j++) {
//                            System.out.println(var + " = " + values[j].getStringValue());
////                            result.put(var, values[j].getStringValue());
//                        }
//                    } else {
//                        //System.out.println(var + " = Not bound");
//                    }
//                }
//            }
//            System.out.println(sw.getTime() + " ms");
        }
    }
}
