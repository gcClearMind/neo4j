package work;

import org.apache.jena.assembler.RuleSet;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.ProfileRegistry;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.InfGraph;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasonerFactory;
import org.apache.jena.reasoner.rulesys.RDFSRuleReasonerFactory;
import org.apache.jena.util.PrintUtil;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import tool.CoreOWLUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static tool.CoreOWLUtil.SetSourceName;
import static tool.CoreOWLUtil.getOntModel;

public class test5 {
    public static void main(String[] args) throws IOException, OWLOntologyCreationException {
//        InputStream inputStream = test.class.getResourceAsStream("/Initialization.properties");
//        Properties properties = new Properties();
//        properties.load(inputStream);

        //rdf文件

        SetSourceName("http://www.neo4j.com/ontologies/data.owl");

        String inputFileName = Paths.get("test5.rdf").toString();
        Model model = ModelFactory.createOntologyModel();
        OntModel ontModel = getOntModel(model, inputFileName);

        String ruleString1 = "a(?a), b(?b), r1(?a, ?b), d(?d), r4(?b, ?d) -> r5(?a, ?d)";
        String ruleString2 = "[Rules: (?a rdf:type http://www.neo4j.com/ontologies/data.owl#a), (?a http://www.neo4j.com/ontologies/data.owl#r1 ?b), (?b rdf:type http://www.neo4j.com/ontologies/data.owl#b), (?b http://www.neo4j.com/ontologies/data.owl#r4 ?d), (?d rdf:type http://www.neo4j.com/ontologies/data.owl#d) -> (?a http://www.neo4j.com/ontologies/data.owl#r5 ?d)]";
        RuleSet ruleSet = RuleSet.create(ruleString2);
//        GenericRuleReasoner reasoner = (GenericRuleReasoner) GenericRuleReasonerFactory.theInstance().create(null);
//        reasoner.setRules(ruleSet.getRules());
//        reasoner.setMode(GenericRuleReasoner.HYBRID);
////        InfGraph infgraph = reasoner.bind(ontModel.getGraph());
//        InfModel infgraph = ModelFactory.createInfModel(reasoner, model);
//
//        model = infgraph.getRawModel();
//
//        OutputStream out = Files.newOutputStream(Paths.get("test6.rdf"));
//        model.write(out,"RDF/XML-ABBREV");

        OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = ontologyManager.loadOntologyFromOntologyDocument((IRI) ontModel.getBaseModel().getGraph());
        OWLReasonerFactory owlReasonerFactory = new ReasonerFactory();
        OWLReasoner owlReasoner = owlReasonerFactory.createReasoner(ontology);





        owlReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);


//        List<OWLClass> owlClasses = ontology.classesInSignature().collect(Collectors.toList());
;
    }
}
