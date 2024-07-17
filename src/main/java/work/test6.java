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

        String inputFileName = Paths.get("data/output.rdf").toString();
        OntModel ontModel = getOntModel(model, inputFileName);

        try (Session session = driver.session()) {
            Result result = session.run("match p = (n:`Blocks:Block`)-[*2..6]->(m:`Blocks:Block`) " +
                    "with  nodes(p) as nodes unwind(nodes) as node1 " +
                    "with DISTINCT node1 as node  " +
                    "return node.name as name , labels(node) as labels , id(node) as id " +
                    "order by id");
            while (result.hasNext()) {
                Record record = result.next();
                String name = record.get("name").asString();
                List<Object> labels = record.get("labels").asList();
                Long id = record.get("id").asLong();
                Individual individual = null;
                Boolean start = false;
                if(name == null || name.equals("null")) {
                    name = id.toString();
                }

                System.out.println(name);
                for(Object o : labels) {
                    String label = o.toString();
                    OntClass ontClass = CoreOWLUtil.getClass(ontModel, label);
                    if(!start) {
                        individual = ontClass.createIndividual(getNameSpace() + id);
                        individual.addProperty(RDFS.label, model.createLiteral(name));
//                        individual.addProperty(RDFS.label,name);
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

        try (Session session = driver.session()) {
            Result result = session.run("match p = (n:`Blocks:Block`)-[*2..6]->(m:`Blocks:Block`) " +
                    "with  relationships(p) as rsps unwind(rsps) as rsp  " +
                    "return  type(rsp) as type,id(startNode(rsp)) as start ,id(endNode(rsp)) as end");
            while (result.hasNext()) {
                Record record = result.next();
                String type = record.get("type").asString();
                Long start = record.get("start").asLong();
                Long end = record.get("end").asLong();
                Property relationProperty = model.createProperty(getNameSpace() + type + "-0");
                Resource individual_start =  model.getResource(getNameSpace() + start);
                Resource individual_end =  model.getResource(getNameSpace() + end);
                individual_start.addProperty(relationProperty, individual_end);
            }

        }catch (Exception e) {
            e.printStackTrace();
        }


        OutputStream out = Files.newOutputStream(Paths.get("data/output_individual.rdf"));
        model.write(out,"RDF/XML-ABBREV");
    }
}
