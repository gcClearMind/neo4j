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
import java.io.OutputStream;
import java.nio.file.Files;
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
//        SetSourceName("http://www.ycao.org/MTSDesign");
        SetSourceName("http://www.neo4j.com/ontologies/data.owl");

        String inputFileName = Paths.get("test3.rdf").toString();
        OntModel ontModel = getOntModel(model, inputFileName);
        OntClass b = CoreOWLUtil.getClass(ontModel, "b");
        OntClass c = CoreOWLUtil.getClass(ontModel, "c");
        OntClass a = CoreOWLUtil.getClass(ontModel, "a");
        OntClass cc = CoreOWLUtil.getClass(ontModel, "cc");
//        addRelation(ontModel,a, a, "a2");
//        addRelation(ontModel,a, b, "ac");
//
//        addRelation(ontModel,b, c, "ac");
//        addRelation(ontModel,a, cc, "ac");
        addRelation(ontModel,b, b, "a2");

        OntClass start = CoreOWLUtil.getClass(ontModel,"a");

        List<Pair<OntProperty, OntClass>> res = getRelations(ontModel, start);
        for(Pair<OntProperty, OntClass> o : res) {
            System.out.println("---------------------------------");
            System.out.println(o.toString());
            System.out.println("---------------------------------");
        }
//        model.write(System.out, "N-TRIPLES");
        OutputStream out = Files.newOutputStream(Paths.get("test3.rdf"));
        model.write(out,"RDF/XML-ABBREV");
    }
}
