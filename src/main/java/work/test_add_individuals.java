package work;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
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

public class test_add_individuals {
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

        String inputFileName = Paths.get("output.rdf").toString();
        OntModel ontModel = getOntModel(model, inputFileName);

        try (Session session = driver.session()) {
            Result result = session.run("MATCH(n) return n.name as first, labels(n) as second");
            while (result.hasNext()) {
                Record record = result.next();
                String name = record.get("first").asString();
                List<Object> second = record.get("second").asList();
                if(name == null) {
                    System.out.println("1");

                }
                if(name == "null") {
                    System.out.println("----------------");
                }
                Individual individual = null;
                for(Object o : second) {
                    String label = o.toString();
                    OntClass ontClass = CoreOWLUtil.getClass(ontModel, label);
                    if(individual != null) {
                        individual.addOntClass(ontClass);
                    }
                    else {
//                        System.out.print("--");
                        if(ontModel.getIndividual(getNameSpace()+name) == null){
                            individual = ontModel.createIndividual(getNameSpace()+name, ontClass);
                        }
                        else {
//                            System.out.println(ontModel.getIndividual(getNameSpace()+name));
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();

        }
        OutputStream out = Files.newOutputStream(Paths.get("output_individual.rdf"));
        model.write(out,"RDF/XML-ABBREV");
    }
}
