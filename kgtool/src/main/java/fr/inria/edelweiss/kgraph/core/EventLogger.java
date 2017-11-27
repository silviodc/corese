package fr.inria.edelweiss.kgraph.core;

import java.util.Date;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class EventLogger {

    private static Logger logger = LogManager.getLogger(EventLogger.class);
    EventManager mgr;
    HashMap<Event, Event> show, hide;

    EventLogger(EventManager ev) {
        mgr = ev;
    }
    
    public void show(Event e) {
        show().put(e, e);
    }
    
    public void hide(Event e) {
        hide().put(e, e);
    }
    
    public void focus() {
       show().clear();
       hide().clear();
    }
    
    HashMap show() {
        if (show == null) {show = new HashMap<>();}
        return show;
    }
    
    HashMap hide() {
        if (hide == null) {hide = new HashMap<>();}
        return hide;
    }
    
    boolean accept(Event e) {
       return (show().isEmpty() || show().containsKey(e)) &&
             (hide().isEmpty() || ! hide().containsKey(e))  ;
    }

    
    void trace(Event type, Event e, Object o) {
        if (accept(e)) {
            message(type, e, o);
        }
    }
    
    void message(Event type, Event e, Object o) {
        switch (type) {
            case Start:
            case Process:
                switch (e) {
                    case LoadStep: logLoadStep(); break;
                    default: logger.info(type + " " + e + pretty(o));
                }
                break;
            case Finish:
                logger.info(type + " " + e);
                break;
        }
        log(type, e);
    }

    void log(Event type, Event e) {
        switch (type) {
            case Start:
                break;
                
            case Finish:
                switch(e) {
                    case Rule: logRule(e);
                    break;
                    
                    case IndexNodeManager: 
                    case IndexNodeManagerReduce: 
                        logNodeManager();
                    break;
                }
                break;
                
            case Process:
                break;
        }
                
        switch (e) {
            case InferenceEngine:
                logInference(type);
                break;
        }
    }

    String pretty(Object o) {
        return (o == null) ? "" : " " + o;
    }
    
    void logLoadStep() {
        logger.info(String.format("Loading: %s : %s", new Date(), mgr.getGraph().size()));
    }

    void logInference(Event e) {
        switch (e) {
            case Start: logger.info("Graph size before: " + mgr.getGraph().size()); break;
            case Finish:logger.info("Graph size after: "  + mgr.getGraph().size()); break;
        }
    }
    
    void logNodeManager() {
        logger.info(String.format("subject: %s ; predicate: %s : ratio: %s ; graph: %s" , 
        getNodeMgr().size() , getNodeMgr().count(), 
        (getNodeMgr().size() > 0) ? ((float) getNodeMgr().count()) /  getNodeMgr().size() : 0, 
        mgr.getGraph().size()));
    }
    
    NodeManager getNodeMgr() {
        return mgr.getGraph().getNodeManager();
    }
    
    void logRule(Event e) {
        logger.info("Graph size after rule: "  + mgr.getGraph().size());
    }

}