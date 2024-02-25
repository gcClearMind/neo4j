package work;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import tool.CoreOWLUtil;
import tool.Path;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;

import static tool.CoreOWLUtil.*;

public class test_find_roads {
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
