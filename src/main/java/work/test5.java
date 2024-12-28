package work;

import org.apache.jena.assembler.RuleSet;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.swrlapi.core.SWRLAPIRule;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.exceptions.SWRLBuiltInException;
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.parser.SWRLParseException;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import static tool.CoreOWLUtil.SetSourceName;
import static tool.CoreOWLUtil.getOntModel;

public class test5 {
    public static void main(String[] args) throws OWLOntologyCreationException, SWRLParseException, SWRLBuiltInException, IOException, OWLOntologyStorageException {
        InputStream inputStream = test.class.getResourceAsStream("/Initialization.properties");
        Properties properties = new Properties();
        properties.load(inputStream);
        String uri = properties.getProperty("neo4j.uri");
        String user = properties.getProperty("neo4j.user");
        String password = properties.getProperty("neo4j.password");
        Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));

        Model model = ModelFactory.createDefaultModel();
        SetSourceName("http://www.neo4j.com/ontologies/data.owl");

        String inputFileName = Paths.get("data/output_individual.rdf").toString();
        OntModel ontModel = getOntModel(model, inputFileName);

        String filePath = "data/path/output.txt";
        try (Session session = driver.session()) {
            Result result = session.run("match(a)-[r1:client]-(m)-[r2:supplier]-(b) return a as first , m as relation, b as second");
            while (result.hasNext()) {
                Record record = result.next();
                Node first = record.get("first").asNode();
                Node relation = record.get("relation").asNode();
                Node second = record.get("second").asNode();
                Map nodeMap = relation.asMap();
                String xmiType = (String)nodeMap.get("xmi:type");
                List<String> labels = (List<String>) relation.labels();
                String label = null;
                if(labels.size() == 1) {
                    label = labels.get(0);
                }
                else {
                    for (String s : labels) {
                        if (s.equals(xmiType)) {
                            continue;
                        } else {
                            label = s;
                            break;
                        }
                    }
                }
                Long AID = first.id();
                Long BID = second.id();
                session.run("match(n) where id(n) = " + AID + " match(m) where id(m) = " + BID +
                        " merge(n)-[:`" + label + "`{version:'add'}]->(m)");
            }
        }catch (Exception e) {
            e.printStackTrace();

        }
        // 关闭连接
        driver.close();


    }
}
