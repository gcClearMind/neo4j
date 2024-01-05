package work;

import org.apache.jena.assembler.RuleSet;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.InfGraph;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasonerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static tool.CoreOWLUtil.SetSourceName;
import static tool.CoreOWLUtil.getOntModel;

public class test5 {
    public static void main(String[] args) throws IOException {
//        InputStream inputStream = test.class.getResourceAsStream("/Initialization.properties");
//        Properties properties = new Properties();
//        properties.load(inputStream);

        //rdf文件
        Model model = ModelFactory.createDefaultModel();
        SetSourceName("http://www.neo4j.com/ontologies/data.owl");

        String inputFileName = Paths.get("test5.rdf").toString();
        OntModel ontModel = getOntModel(model, inputFileName);
        GenericRuleReasoner reasoner = (GenericRuleReasoner) GenericRuleReasonerFactory.theInstance().create(null);
        String ruleString1 = "a(?a), b(?b), r1(?a, ?b), d(?d), r4(?b, ?d) -> r5(?a, ?d)";
        RuleSet ruleSet = RuleSet.create(ruleString1);
        reasoner.setRules(ruleSet.getRules());
        reasoner.setMode(GenericRuleReasoner.HYBRID);
        InfGraph infgraph = reasoner.bind(model.getGraph());
        model = ModelFactory.createModelForGraph(infgraph.getRawGraph());
        OutputStream out = Files.newOutputStream(Paths.get("test5.rdf"));
        model.write(out,"RDF/XML-ABBREV");
    }
}
