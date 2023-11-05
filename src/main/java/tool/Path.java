package tool;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;

import java.util.LinkedList;

public class Path {
    private LinkedList<Pair<OntProperty, OntClass>> pathList;

    public Path() {
        pathList = new LinkedList<>();
    }

    public void add(Pair<OntProperty, OntClass> pair) {
        pathList.add(pair);
    }

    public LinkedList<Pair<OntProperty, OntClass>> getPathList() {
        return pathList;
    }

    @Override
    public String toString() {
        return pathList.toString();
    }
}

