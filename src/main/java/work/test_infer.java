package work;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.neo4j.driver.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.swrlapi.core.SWRLAPIRule;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.exceptions.SWRLBuiltInException;
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.parser.SWRLParseException;

import java.io.*;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Set;

import static tool.CoreOWLUtil.SetSourceName;
import static tool.CoreOWLUtil.getOntModel;

public class test_infer {
    public static void main(String[] args) throws OWLOntologyCreationException, SWRLParseException, SWRLBuiltInException, IOException, OWLOntologyStorageException {
        InputStream inputStream = test.class.getResourceAsStream("/Initialization.properties");
        Properties properties = new Properties();
        properties.load(inputStream);
        String uri = properties.getProperty("neo4j.uri");
        String user = properties.getProperty("neo4j.user");
        String password = properties.getProperty("neo4j.password");
        Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        //rdf文件

        SetSourceName("http://www.neo4j.com/ontologies/data.owl");

        String inputFileName = Paths.get("data/output_individual.rdf").toString();
//        String inputFileName = Paths.get("data/test5.rdf").toString();
        String outputFileName = Paths.get("data/output_individual_infer.rdf").toString();
        Model model = ModelFactory.createOntologyModel();
        OntModel ontModel = getOntModel(model, inputFileName);

        String base = "http://www.neo4j.com/ontologies/data.owl";
//        String ruleString = "a(?x) ^ b(?y) ^ r1(?x, ?y) ^ c(?z) ^ r2(?y, ?z) -> r3(?x, ?z)";
//        String ruleString = "a(?x) , b(?y) , r1(?x, ?y) , c(?z) , r2(?y, ?z) -> r3(?x, ?z)";
//        String ruleString = "[r1: (?x rdf:type a) , (?y rdf:type b), (?x r1 ?y),  (?z rdf:type c) , (?y, r2 ?z) -> (?x r3 ?z)]";
//        String ruleString = "(?x rdf:type a) , (?y rdf:type b), (?x r1 ?y),  (?z rdf:type c) , (?y, r2 ?z) -> (?x r3 ?z)";
//        String ruleString = "Block(?individualA) ^ ownedAttribute(?individualA, ?individualB) ^ uml:Property(?individualB)" +
//                " ^ type(?individualB, ?individualC) ^ uml:Activity(?individualC) ^ ownedAttribute(?individualC, ?individualD)" +
//                " ^ uml:Property(?individualD) ^ type(?individualD, ?individualE) ^ uml:Activity(?individualE) ^ group(?individualE, ?individualF)" +
//                " ^ AllocateActivityPartition(?individualF)" +
//                " ^ represents(?individualF, ?individualG) ^ Block(?individualG) -> relation(?individualA, ?individualG)";
        String ruleString = "Requirement(?individualA) ^ supplier(?individualB, ?individualA) ^ Verify(?individualB) ^ client(?individualB, ?individualC) ^ TestCase(?individualC) -> " +
                "relation(?individualA, ?individualC)";
//        String ruleString = "[rule1: (?individual-a rdf:type Block), (?individual-a ownedAttribute ?individual-b), (?individual-b rdf:type RequirementRelated), (?individual-b type ?individual-c), (?individual-c rdf:type System), (?individual-c ownedAttribute ?individual-d), (?individual-d type PartProperty), (?individual-d type ?individual-e), (?individual-e type Block) -> (?individual-a relationship ?individual-e)]";
//        String ruleString = " Block(?individual-a), ownedAttribute(?individual-a, ?individual-b), RequirementRelated(?individual-b), type(?individual-b, ?individual-c), System(?individual-c), ownedAttribute(?individual-c, ?individual-d), PartProperty(?individual-d), type(?individual-d, ?individual-e), Block(?individual-e) -> relationship(?individual-a, ?individual-e)";
//        RuleSet ruleSet = RuleSet.create(ruleString);
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        File file = new File(inputFileName);

        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
        manager.getOntologyFormat(ontology).asPrefixOWLOntologyFormat().setDefaultPrefix(base + "#");
        PrefixManager pm = manager.getOntologyFormat(ontology).asPrefixOWLOntologyFormat();
        pm.setPrefix("uml:", "http://www.example.org/uml#");
        SWRLRuleEngine ruleEngine = SWRLAPIFactory.createSWRLRuleEngine(ontology);

        SWRLAPIRule rule =  ruleEngine.createSWRLRule("rule1", ruleString);

        System.out.println(rule);
        ruleEngine.infer();
        Set<OWLAxiom> inferredAxioms = ruleEngine.getInferredOWLAxioms();
        for (OWLAxiom axiom : inferredAxioms) {
            // 只处理涉及到relation关系的三元组
            if (axiom instanceof OWLObjectPropertyAssertionAxiom) {
                OWLObjectPropertyAssertionAxiom propertyAssertion = (OWLObjectPropertyAssertionAxiom) axiom;

                // 检查是否为 "relation" 属性
                if (propertyAssertion.getProperty().toString().contains("relation")) {
                    OWLIndividual subject = propertyAssertion.getSubject();
                    OWLIndividual object = propertyAssertion.getObject();

                    // 提取两者的ID
                    String individualAID = subject.toStringID();
                    String individualEID = object.toStringID();
                    String AID = individualAID.substring(individualAID.indexOf('#') + 1);
                    String EID = individualEID.substring(individualEID.indexOf('#') + 1);
                    System.out.println("Relation found: " + individualAID + " -> " + individualEID);
                    //返回结果到知识图谱
                    try (Session session = driver.session()) {
                        Result result = session.run("match(n) where id(n) = " + AID + " match(m) where id(m) = " + EID +
                                " merge(n)-[:relation]->(m)");
                    }catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            }
        }
// 保存修改后的OWL本体
        manager.saveOntology(ontology, new RDFXMLDocumentFormat(), new FileOutputStream(outputFileName));

        ;
    }
}
