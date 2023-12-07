package work;

import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.InfGraph;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasonerFactory;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.vocabulary.ReasonerVocabulary;
import tool.CoreOWLUtil;
import tool.Path;

import org.apache.jena.util.PrintUtil;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Scanner;

import static tool.CoreOWLUtil.*;

public class test4 {
    public static void main(String[] args) throws IOException {
//        InputStream inputStream = test.class.getResourceAsStream("/Initialization.properties");
//        Properties properties = new Properties();
//        properties.load(inputStream);

        //rdf文件
        Model model = ModelFactory.createDefaultModel();
        SetSourceName("http://www.neo4j.com/ontologies/data.owl");

        String inputFileName = Paths.get("output.rdf").toString();
        OntModel ontModel = getOntModel(model, inputFileName);


        OntClass start = CoreOWLUtil.getClass(ontModel, "Blocks:Block");
        OntClass end = CoreOWLUtil.getClass(ontModel, "Blocks:Block");
//        List<Pair<OntProperty, OntClass>> next_nodes = getRelations(ontModel, start);
//        for(Pair<OntProperty, OntClass> a : next_nodes) {
//            System.out.println(a.toString());
//        }
        LinkedList<Path> paths =  getAllPath(ontModel, start, end, 3);
        int index = 0;
        for(Path path : paths) {
            System.out.print(index++ + ": ");
            System.out.println(showPath(path));
//            System.out.println(getSWRL(path));
            System.out.println("--------------------------------------------------------------------------------------------");
        }
        System.out.print("选择序号： ");
        Scanner scanner = new Scanner(System.in);
        index = scanner.nextInt();

        Path path = paths.get(index);

        System.out.print("输入新的关系并创建： ");
        String relation = scanner.next();
        addProperty(ontModel, relation);
        String rule = getSWRL(path, relation);
        GenericRuleReasoner reasoner = (GenericRuleReasoner) GenericRuleReasonerFactory.theInstance().create(null);
        PrintUtil.registerPrefix("", CoreOWLUtil.getNameSpace());

        reasoner.setRules(Rule.parseRules(rule));
        reasoner.setMode(GenericRuleReasoner.HYBRID);
        InfGraph infgraph = reasoner.bind(model.getGraph());
        infgraph.setDerivationLogging(true);
        Iterator<Triple> tripleIterator = infgraph.find(null, null, null);
        while (tripleIterator.hasNext()) {
            System.out.println(" - " + PrintUtil.print(tripleIterator.next()));
        }
        OutputStream out = Files.newOutputStream(Paths.get("test4.rdf"));
        model.write(out,"RDF/XML-ABBREV");
    }
}
