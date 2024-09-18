package work;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;
import org.checkerframework.checker.oigj.qual.O;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import tool.CoreOWLUtil;
import tool.Path;

import static tool.CoreOWLUtil.*;
import static tool.CoreOWLUtil.getNameSpace;

public class test6 {
    public static void main(String[] args) throws IOException {
        //        InputStream inputStream = test.class.getResourceAsStream("/Initialization.properties");
//        Properties properties = new Properties();
//        properties.load(inputStream);

        //rdf文件
        Model model = ModelFactory.createDefaultModel();
        SetSourceName("http://www.neo4j.com/ontologies/data.owl");

        String inputFileName = Paths.get("data/output.rdf").toString();
        OntModel ontModel = getOntModel(model, inputFileName);


        OntClass start = CoreOWLUtil.getClass(ontModel, "Blocks:Block");
        OntClass end = CoreOWLUtil.getClass(ontModel, "Blocks:Block");
//        List<Pair<OntProperty, OntClass>> next_nodes = getRelations(ontModel, start);
//        for(Pair<OntProperty, OntClass> a : next_nodes) {
//            System.out.println(a.toString());
//        }
        LinkedList<tool.Path> paths =  getAllPath(ontModel, start, end, 4);
        int index = 0;
        for(Path path : paths) {
            System.out.print(index++ + ": ");
            System.out.println(showPath(path));
            System.out.println(getSWRL(path,"aa"));
            System.out.println("----------------------------------------------------");
        }

        System.out.println(paths.size());

    }
}
