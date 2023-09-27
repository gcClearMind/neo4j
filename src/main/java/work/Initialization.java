package work;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static tool.CoreOWLUtil.*;

public class Initialization {
    public static void main(String[] args) throws IOException {
        InputStream inputStream = test.class.getResourceAsStream("/Initialization.properties");
        Properties properties=new Properties();
        properties.load(inputStream);
        List<String> o = Arrays.asList(properties.getProperty("Sysml.relation").split(" "));
        System.out.println(o);
//        Model model = ModelFactory.createDefaultModel();
//        SetSourceName("http://www.neo4j.com/ontologies/data.owl");
//        String inputFileName = Paths.get("1.rdf").toString();
//        OntModel ontModel = getOntModel(model,inputFileName);
//        printClasses(ontModel);
//        model.write(System.out, "N-TRIPLES");
        // 先写入一些粗略的 uml 与sysml的对应， 在每次录入过程中， 通过同类项的推理， 将标签归类到相应的class子类下
    }
}
