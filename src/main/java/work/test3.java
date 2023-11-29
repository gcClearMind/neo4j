package work;

import org.apache.jena.base.Sys;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import tool.CoreOWLUtil;
import tool.Pair;
import tool.Path;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import static tool.CoreOWLUtil.*;

public class test3 {
    public static void main(String[] args) throws IOException {
//        InputStream inputStream = test.class.getResourceAsStream("/Initialization.properties");
//        Properties properties = new Properties();
//        properties.load(inputStream);

        //rdf文件
        Model model = ModelFactory.createDefaultModel();
        SetSourceName("http://www.neo4j.com/ontologies/data.owl");

        String inputFileName = Paths.get("output.rdf").toString();
        OntModel ontModel = getOntModel(model, inputFileName);


        OntClass start = CoreOWLUtil.getClass(ontModel, "Blocks:Block");
        OntClass end = CoreOWLUtil.getClass(ontModel, "Blocks:Block");
//        List<Pair<OntProperty, OntClass>> next_nodes = getRelations(ontModel, start);
//        for(Pair<OntProperty, OntClass> a : next_nodes) {
//            System.out.println(a.toString());
//        }
        LinkedList<Path> paths =  getAllPath(ontModel, start, end, 3);
        for(Path path : paths) {
            System.out.println(showPath(path));
            System.out.println(getSWRL(path));
            System.out.println("----------------------------------------------------");
        }

        System.out.println(paths.size());
    }
}
