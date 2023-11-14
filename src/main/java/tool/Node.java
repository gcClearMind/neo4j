package tool;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private OntClass data;
    private List<Pair<OntProperty, OntClass>> edge;

    public Node(OntClass data) {
        this.data = data;
        this.edge = new ArrayList<>();
    }

    public OntClass getData() {
        return data;
    }

    public void setData(OntClass data) {
        this.data = data;
    }

    public List<Pair<OntProperty, OntClass>> getEdge() {
        return edge;
    }

    public void addEdge(Pair<OntProperty, OntClass> next) {
        this.edge.add(next);
    }

    public Integer getSize() {
        return this.edge.size();
    }
}
