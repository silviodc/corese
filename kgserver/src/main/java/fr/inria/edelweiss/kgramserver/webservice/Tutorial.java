package fr.inria.edelweiss.kgramserver.webservice;

import java.util.List;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * SPARQL endpoint apply transformation (like Transformer)
 * Each service manage it's own TripleStore
 * TripleStore content is defined by server in profile.ttl
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2015
 *
 */
@Path("tutorial/{serv}")
public class Tutorial {

    static final String SERVICE           = "/tutorial/"; 
    
    static Manager man;

    static {
        man = new Manager();
    }

   
   TripleStore getTripleStore(String serv){
       TripleStore ts = man.getTripleStoreByService(serv);       
       if (ts == null){
           return Transformer.getTripleStore();
       }
       return ts;
   }

    @POST
    @Produces("text/html")
    public Response post(
            @PathParam("serv")      String serv,
            @FormParam("profile")   String profile, // query + transform
            @FormParam("uri")       String resource, // query + transform
            @FormParam("query")     String query, // SPARQL query
            @FormParam("name")      String name, // SPARQL query name (in webapp/query)
            @FormParam("value")     String value, // values clause that may complement query           
            @FormParam("transform") String transform, // Transformation URI to post process result
            @FormParam("default-graph-uri") List<String> defaultGraphUris,
            @FormParam("named-graph-uri")   List<String> namedGraphUris) {

        return get(serv, profile, resource, query, name, value, transform, defaultGraphUris, namedGraphUris);
    }
    

    @GET
    @Produces("text/html")
    public Response get(
            @PathParam("serv")      String serv,
            @QueryParam("profile")  String profile, // query + transform
            @QueryParam("uri")      String resource, // URI of resource focus
            @QueryParam("query")    String query, // SPARQL query
            @QueryParam("name")     String name, // SPARQL query name (in webapp/query or path or URL)
            @QueryParam("value")    String value, // values clause that may complement query           
            @QueryParam("transform") String transform, // Transformation URI to post process result
            @QueryParam("default-graph-uri") List<String> defaultGraphUris,
            @QueryParam("named-graph-uri")  List<String> namedGraphUris) {
        // Dataset URI of the service
        String uri = man.getURI(serv);
        Param par = new Param(SERVICE + serv, getProfile(uri, profile, transform), transform, resource, name, query);
        par.setValue(value);
        par.setDataset(namedGraphUris, namedGraphUris);
        return new Transformer().template(getTripleStore(serv), par);
    }

    /**
     * When no profile neither transform is given
     * the Dataset URI may be a st:Profile with a st:transform
     * in this case, use it as default profile
     */
   String getProfile(String uri, String profile, String transform){
       if (uri == null || profile != null || transform != null){
           return profile;
       }
       Service s = Transformer.getProfile().getService(uri);
       if (s != null){
           return uri;
       }
       return profile;
   }
    
   
}