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
import java.util.stream.Stream;

import static tool.CoreOWLUtil.SetSourceName;
import static tool.CoreOWLUtil.getOntModel;

public class test5 {
    public static void main(String[] args) throws OWLOntologyCreationException, SWRLParseException, SWRLBuiltInException, FileNotFoundException, OWLOntologyStorageException {
//        InputStream inputStream = test.class.getResourceAsStream("/Initialization.properties");
//        Properties properties = new Properties();
//        properties.load(inputStream);

        //rdf文件

        SetSourceName("http://www.neo4j.com/ontologies/data.owl");

        String inputFileName = Paths.get("data/test5.rdf").toString();
        String outputFileName = Paths.get("data/test6.rdf").toString();
        Model model = ModelFactory.createOntologyModel();
        OntModel ontModel = getOntModel(model, inputFileName);

        String base = "http://www.neo4j.com/ontologies/data.owl";
        String ruleString1 = "a(?x)^b(?y)^r1(?x, ?y)^d(?z)^r4(?y, ?z) -> r5(?x, ?z)";
        String ruleString2 = "[Rules: (?a rdf:type http://www.neo4j.com/ontologies/data.owl#a), (?a http://www.neo4j.com/ontologies/data.owl#r1 ?b), (?b rdf:type http://www.neo4j.com/ontologies/data.owl#b), (?b http://www.neo4j.com/ontologies/data.owl#r4 ?d), (?d rdf:type http://www.neo4j.com/ontologies/data.owl#d) -> (?a http://www.neo4j.com/ontologies/data.owl#r5 ?d)]";
        String ruleString3 = "http://www.neo4j.com/ontologies/data.owl#a(?a), http://www.neo4j.com/ontologies/data.owl#b(?b), http://www.neo4j.com/ontologies/data.owl#r1(?a, ?b), http://www.neo4j.com/ontologies/data.owl#d(?d), http://www.neo4j.com/ontologies/data.owl#r4(?b, ?d) -> http://www.neo4j.com/ontologies/data.owl#r5(?a, ?d)";
        String ruleString4 = "Body(" +
                "ClassAtom(<http://www.neo4j.com/ontologies/data.owl#a> Variable(?a)) " +
                "ClassAtom(<http://www.neo4j.com/ontologies/data.owl#b> Variable(?b)) " +
                "ClassAtom(<http://www.neo4j.com/ontologies/data.owl#d> Variable(?d)) " +
                "ObjectPropertyAtom(<http://www.neo4j.com/ontologies/data.owl#r1> Variable(?a) Variable(?b)) " +
                "ObjectPropertyAtom(<http://www.neo4j.com/ontologies/data.owl#r4> Variable(?b) Variable(?d))" +
                ") " +
                "Head(" +
                "ObjectPropertyAtom(<http://www.neo4j.com/ontologies/data.owl#r5> Variable(?a) Variable(?d))" +
                ")";
        String ruleString5=
                "ClassAtom(<a> Variable(?a))^" +
                "ClassAtom(<b> Variable(?b))^" +
                "ClassAtom(<d> Variable(?d))^" +
                "ObjectPropertyAtom(<r1> Variable(?a) Variable(?b))^" +
                "ObjectPropertyAtom(<r4> Variable(?b) Variable(?d))" +
                ")" +
                "->" +
                "ObjectPropertyAtom(<r5> Variable(?a) Variable(?d))";
        String ruleString6 = "a(?a) -> http://www.neo4j.com/ontologies/data.owl#r5(?a, ?a)";

        RuleSet ruleSet = RuleSet.create(ruleString2);
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        File file = new File(inputFileName);

        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
        manager.getOntologyFormat(ontology).asPrefixOWLOntologyFormat().setDefaultPrefix(base + "#");
        SWRLRuleEngine ruleEngine = SWRLAPIFactory.createSWRLRuleEngine(ontology);

        SWRLAPIRule rule =  ruleEngine.createSWRLRule("rule15", ruleString1);
        ruleEngine.infer();

// 保存修改后的OWL本体
        manager.saveOntology(ontology, new RDFXMLDocumentFormat(), new FileOutputStream(outputFileName));

;
    }
}
