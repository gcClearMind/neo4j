package work;

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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import static tool.CoreOWLUtil.*;

public class test3 {
    public static void main(String[] args) throws IOException {
        InputStream inputStream = test.class.getResourceAsStream("/Initialization.properties");
        Properties properties=new Properties();
        properties.load(inputStream);

        //rdf文件
        Model model = ModelFactory.createDefaultModel();
        SetSourceName("http://www.neo4j.com/ontologies/data.owl");

        String inputFileName = Paths.get("output.rdf").toString();
        OntModel ontModel = getOntModel(model, inputFileName);
        OntClass start = CoreOWLUtil.getClass(ontModel,"Blocks:Block");
        System.out.println(start.getURI());
        OntClass end = CoreOWLUtil.getClass(ontModel,"Blocks:Block");

        List<Pair<OntProperty, OntClass>> res = getRelations(ontModel, start);

        for(Pair<OntProperty, OntClass> o : res) {
            System.out.println("---------------------------------");
            System.out.println(o.toString());
            System.out.println("---------------------------------");
        }
//        System.out.println(getAllPath(ontModel, start, end));
    }
}
