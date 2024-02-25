package work;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.checkerframework.checker.oigj.qual.O;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import tool.CoreOWLUtil;
import tool.CoreOWLUtil.*;

import static tool.CoreOWLUtil.*;

public class test6 {
    public static void main(String[] args) throws IOException {
        InputStream inputStream = test.class.getResourceAsStream("/Initialization.properties");
        Properties properties=new Properties();
        properties.load(inputStream);
        // 连接数据库
        String uri = properties.getProperty("neo4j.uri");
        String user = properties.getProperty("neo4j.user");
        String password = properties.getProperty("neo4j.password");
        Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));

        Model model = ModelFactory.createDefaultModel();
        SetSourceName("http://www.neo4j.com/ontologies/data.owl");

        String inputFileName = Paths.get("data/test6.rdf").toString();
        OntModel ontModel = getOntModel(model, inputFileName);
        OntClass ontClass_A = ontModel.getOntClass(getNameSpace() + "a");
        Individual individual_A = ontClass_A.createIndividual(getNameSpace() + "a_test");
        OntClass ontClass_E = ontModel.getOntClass(getNameSpace() + "e");
        individual_A.addOntClass(ontClass_E);



        OutputStream out = Files.newOutputStream(Paths.get("data/test6_res.rdf"));
        model.write(out,"RDF/XML-ABBREV");
    }
}
