package fr.inria.edelweiss.kgraph.core.producer;

import fr.inria.acacia.corese.api.IDatatype;
import fr.inria.acacia.corese.cg.datatype.DatatypeMap;
import fr.inria.acacia.corese.triple.parser.Processor;
import java.util.Iterator;
import java.util.List;

import fr.inria.edelweiss.kgram.api.core.Edge;
import fr.inria.edelweiss.kgram.api.core.Entity;
import fr.inria.edelweiss.kgram.api.core.ExprType;
import fr.inria.edelweiss.kgram.api.core.Node;
import fr.inria.edelweiss.kgram.tool.MetaIterator;
import fr.inria.edelweiss.kgraph.core.EdgeIndexer;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgraph.core.edge.EdgeGeneric;
import java.util.ArrayList;

/**
 * Transient Dataset over graph in order to iterate edges 
 * Use case: Producer getEdges()
 * default or named graphs, from or from named
 * Default graph: eliminate duplicate edges during iteration
 * May take edge level into account for RuleEngine optimization
 * Example: 
 * graph.getDefault().from(g1).iterate(foaf:knows)
 * graph.getNamed().minus(list(g1 g2)).iterate(us:John, 0)
 * graph.getDefault().iterate().filter(ExprType.GE, 100) -- filter (?object >= 100)
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2016
 */
public class DataProducer implements Iterable<Entity>, Iterator<Entity> {

    static final List<Entity> empty     = new ArrayList<Entity>(0);
    
    Iterable<Entity> iter;
    Iterator<Entity> it;
    EdgeGeneric glast;
    Edge last;
    Graph graph;
    private DataFilter filter;
    DataFrom from;
    boolean isNamedGraph;

    public DataProducer(Graph g) {
        graph = g;
        isNamedGraph = false;
    }

    public static DataProducer create(Graph g) {
        DataProducer ei = new DataProducer(g);
        return ei;
    }
      
    public DataProducer iterate() {
        return iterate(graph.getTopProperty());
    }

    public DataProducer iterate(Node predicate) {
        setIterable(graph.getEdges(predicate));
        return this;
    }

    public DataProducer iterate(Node predicate, Node node) {
        return iterate(predicate, node, 0);
    }
    
    public DataProducer iterate(Node node, int n) {
        return iterate(graph.getTopProperty(), node, n);
    }

     public DataProducer iterate(Node predicate, Node node, int n) {
        // optimize special cases
        if (isNamedGraph) {
            if (node == null && from != null && !from.isEmpty()) {
                // exploit IGraph Index to focus on from
                setIterable(getEdgesFromNamed(from.getFrom(), predicate));
                return this;
            } 
        } 
        // default graph
        else if (node == null && from != null && !from.isEmpty()) {
            // no query node has a value, there is a from           
            if (! from.isFromOK(from.getFrom())) {
                // from URIs are unknown in current graph
                setIterable( empty);
                return this;
            }
        } 

        // general case
        setIterable(graph.properGetEdges(predicate, node, n));
        return this;
    }
    
    

    /**
     * Iterate predicate from named
     */
    Iterable<Entity> getEdgesFromNamed(List<Node> from, Node predicate) {
        MetaIterator<Entity> meta = new MetaIterator<Entity>();

        for (Node src : from) {
            Node tfrom = graph.getGraphNode(src.getLabel());
            if (tfrom != null) {
                Iterable<Entity> it = graph.getEdges(predicate, tfrom, Graph.IGRAPH);
                if (it != null) {
                    meta.next(it);
                }
            }
        }

        if (meta.isEmpty()) {
            return empty;
        } else {
            return meta;
        }
    }
   
    
    public void setIterable(Iterable<Entity> it){
        iter = it;
    }
    
    /**
     * RuleEngine require Edge with getIndex() >= n
     * 
     */
    public DataProducer level(int n){
        setFilter(new DataFilter(ExprType.EDGE_LEVEL, n));
        return  this;
    }
    
    public DataProducer named(){
        this.isNamedGraph = true;
        return this;
    }
    
    public DataFrom getCreateDataFrom(){
        if (from == null){
            from = new DataFrom(graph);
            setFilter(from);
        }
        return from;
    }
    
    public DataProducer from(List<Node> list) {  
        if (list != null && ! list.isEmpty()){
            getCreateDataFrom().from(list);
        }
        return this;
    }
    
    public DataProducer from(Node g) {
        if (g != null) {          
            getCreateDataFrom().from(g);
        }
        return this;
    }
    
    
    public DataProducer from(List<Node> list, Node source) {
        if (source == null){
            return from(list);
        }
        return from(source);
    }
    
    /**
     * The from clause is taken as skip from
     * the graphs are skipped to answer query
     */    
    public DataProducer minus(List<Node> list){
        getCreateDataFrom().minus(list);
        return this;
    }
    
    public DataProducer minus(Node node){
        getCreateDataFrom().minus(node);
        return this;
    }
    
    @Override
    public Iterator<Entity> iterator() {
        if (from != null && from.isOneFrom() && from.getFromNode() == null) {
            return empty.iterator();
        }
        it = iter.iterator();
        last = null;
        glast = null;
        return this;
    }

    @Override
    public boolean hasNext() {
        return it.hasNext();
    }

    boolean same(Node n1, Node n2) {
        return n1.getIndex() == n2.getIndex();
    }

    /**
     * Main function iterate Edges.
     */
    @Override
    public Entity next() {

        while (hasNext()) {
            Entity ent = it.next();
            

            if (isNamedGraph) {
                // ok
            } 
            else if (last != null && ! different(last, ent.getEdge())){
                continue;
            }
            
            if (filter != null && ! filter.eval(ent)) {
                // filter process from() clause
                if (filter.fail()) {
                    // RuleEngine edge level may fail
                    it = empty.iterator();
                    return null;
                }
                continue;
            }
                    
            record(ent);
            return ent;
        }
        return null;
    }
      
    // eliminate successive duplicates
    boolean different(Edge last, Edge edge) {       
        if (edge.getEdgeNode() == null || ! same(last.getEdgeNode(), edge.getEdgeNode())) {
            // different properties: ok
            return true;
        } else {
            int size = last.nbNode();
            if (size == edge.nbNode()) {               
                for (int i = 0; i < size; i++) {
                    if (!same(last.getNode(i), edge.getNode(i))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
        
    void record(Entity ent) {
        if (EdgeIndexer.test){
            record2(ent);
        }
        else {
            last = ent.getEdge();
        }
    }

    // record a copy of ent for last
    void record2(Entity ent) {
        if (glast == null) {
            glast = new EdgeGeneric();
            last = glast;
        }
        glast.duplicate(ent);
    }

    @Override
    public void remove() {
    }
   
    
    /********************************************************
     * 
     * API to add filters to iterate()
     * Use case:
     * 
     * g.getDefault().iterate(foaf:age).filter(new DataFilterFactory().object(ExprType.GE, 50))   -- object >= 50
     * g.getNamed().from(g1).iterate().filter(new DataFilterFactory().compare(ExprType.EQ, 0, 1)) -- subject = object
     * g.getDefault().iterate().filter(new DataFilterFactory().and().subject(AA).object(BB)) -- and/or are binary
     * g.getDefault().iterate().filter(new DataFilterFactory().not().or().subject(AA).object(BB)) -- not is unary
     * 
     *******************************************************/
        
    public DataProducer filter(DataFilter f){
        setFilter(f);
        return this;
    }
     
    public DataProducer filter(DataFilterFactory f){
        setFilter(f.getFilter());
        return this;
    }

    /**
     * @return the filter
     */
    public DataFilter getFilter() {
        return filter;
    }

    /**
     * @param filter the filter to set
     */
    public void setFilter(DataFilter f) {
        if (filter == null){
            filter = f;
        }
        else if (filter.isBoolean()){
           filter.setFilter(f);
        }
        else {
            // use case: filter = from(g1)
            filter = new DataFilterAnd(filter, f);
        }
    }
    
}