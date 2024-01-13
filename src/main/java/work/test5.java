package work;

import org.apache.jena.assembler.RuleSet;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.ProfileRegistry;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.InfGraph;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasonerFactory;
import org.apache.jena.reasoner.rulesys.RDFSRuleReasonerFactory;
import org.apache.jena.util.PrintUtil;
import tool.CoreOWLUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

import static tool.CoreOWLUtil.SetSourceName;
import static tool.CoreOWLUtil.getOntModel;

public class test5 {
    public static void main(String[] args) throws IOException {
//        InputStream inputStream = test.class.getResourceAsStream("/Initialization.properties");
//        Properties properties = new Properties();
//        properties.load(inputStream);

        //rdf文件

        SetSourceName("http://www.neo4j.com/ontologies/data.owl");

        String inputFileName = Paths.get("test5.rdf").toString();
        Model model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_TRANS_INF);
        OntModel ontModel = getOntModel(model, inputFileName);
        GenericRuleReasoner reasoner = (GenericRuleReasoner) GenericRuleReasonerFactory.theInstance().create(null);
        String ruleString1 = "a(?a), b(?b), r1(?a, ?b), d(?d), r4(?b, ?d) -> r5(?a, ?d)";
        String ruleString2 = "[Rules: (?a rdf:type http://www.neo4j.com/ontologies/data.owl#a), (?a http://www.neo4j.com/ontologies/data.owl#r1 ?b), (?b rdf:type http://www.neo4j.com/ontologies/data.owl#b), (?b http://www.neo4j.com/ontologies/data.owl#r4 ?d), (?d rdf:type http://www.neo4j.com/ontologies/data.owl#d) -> (?a http://www.neo4j.com/ontologies/data.owl#r5 ?d)]";
        RuleSet ruleSet = RuleSet.create(ruleString2);
        reasoner.setRules(ruleSet.getRules());
        reasoner.setMode(GenericRuleReasoner.HYBRID);
        InfGraph infgraph = reasoner.bind(ontModel.getGraph());

        Iterator<Triple> tripleIterator = infgraph.getRawGraph().find(null, null, null);
//        PrintUtil.registerPrefix("", CoreOWLUtil.getNameSpace());
        while (tripleIterator.hasNext()) {
            System.out.println(tripleIterator.next());
        }
        model = ModelFactory.createModelForGraph(infgraph.getRawGraph());

        OutputStream out = Files.newOutputStream(Paths.get("test6.rdf"));
        model.write(out,"RDF/XML-ABBREV");

    }
}
