package work;

import org.apache.jena.assembler.RuleSet;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Stream;

import static tool.CoreOWLUtil.SetSourceName;
import static tool.CoreOWLUtil.getOntModel;

public class test4 {
    public static void main(String[] args) throws OWLOntologyCreationException, SWRLParseException, SWRLBuiltInException, FileNotFoundException, OWLOntologyStorageException {
//        InputStream inputStream = test.class.getResourceAsStream("/Initialization.properties");
//        Properties properties = new Properties();
//        properties.load(inputStream);

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
        String ruleString = "Block(?individualA) ^ ownedAttribute(?individualA, ?individualB) ^ PartProperty(?individualB) ^ type(?individualB, ?individualC) ^ Block(?individualC) ^ ownedAttribute(?individualC, ?individualD) ^ PartProperty(?individualD) ^ type(?individualD, ?individualE) ^ Block(?individualE) -> relation(?individualA, ?individualE)";
//        String ruleString = "[rule1: (?individual-a rdf:type Block), (?individual-a ownedAttribute ?individual-b), (?individual-b rdf:type RequirementRelated), (?individual-b type ?individual-c), (?individual-c rdf:type System), (?individual-c ownedAttribute ?individual-d), (?individual-d type PartProperty), (?individual-d type ?individual-e), (?individual-e type Block) -> (?individual-a relationship ?individual-e)]";
//        String ruleString = " Block(?individual-a), ownedAttribute(?individual-a, ?individual-b), RequirementRelated(?individual-b), type(?individual-b, ?individual-c), System(?individual-c), ownedAttribute(?individual-c, ?individual-d), PartProperty(?individual-d), type(?individual-d, ?individual-e), Block(?individual-e) -> relationship(?individual-a, ?individual-e)";
//        RuleSet ruleSet = RuleSet.create(ruleString);
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        File file = new File(inputFileName);

        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
        manager.getOntologyFormat(ontology).asPrefixOWLOntologyFormat().setDefaultPrefix(base + "#");
        PrefixManager pm = manager.getOntologyFormat(ontology).asPrefixOWLOntologyFormat();
//        pm.setPrefix("uml:", "http://www.example.org/uml#");
        SWRLRuleEngine ruleEngine = SWRLAPIFactory.createSWRLRuleEngine(ontology);

        SWRLAPIRule rule =  ruleEngine.createSWRLRule("rule15", ruleString);

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

                    System.out.println("Relation found: " + individualAID + " -> " + individualEID);
                }
            }
        }
// 保存修改后的OWL本体
        manager.saveOntology(ontology, new RDFXMLDocumentFormat(), new FileOutputStream(outputFileName));

        ;
    }
}
