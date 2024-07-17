package work;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDFS;
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

        String inputFileName = Paths.get("data/output.rdf").toString();
        OntModel ontModel = getOntModel(model, inputFileName);

        try (Session session = driver.session()) {
            Result result = session.run("MATCH(n) return n.name as first, labels(n) as second");
//            "match p = (n:`Blocks:Block`)-[*2..6]->(m:`Blocks:Block`) with nodes(p) as nodes unwind(nodes) as node return node.name as name , labels(node) as labels"
            int id = 0;
            while (result.hasNext()) {
                Record record = result.next();
                String name = record.get("first").asString();
                List<Object> second = record.get("second").asList();
                Individual individual = null;
                Boolean start = false;
                if(name == null || name.equals("null")) {
                    name = "";
                }

                for(Object o : second) {
                    String label = o.toString();
                    OntClass ontClass = CoreOWLUtil.getClass(ontModel, label);
                    if(!start) {
                       individual = ontClass.createIndividual(getNameSpace() + (++id));
                        individual.addProperty(RDFS.label, model.createLiteral(name, "name"));
                        start = true;
                    }
                    else {
                        individual.addOntClass(ontClass);
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();

        }
        OutputStream out = Files.newOutputStream(Paths.get("data/output_individual.rdf"));
        model.write(out,"RDF/XML-ABBREV");
    }
}
