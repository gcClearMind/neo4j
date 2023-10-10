package work;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import tool.CoreOWLUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.file.Files;
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
        List<String> subClasses = Arrays.asList(properties.getProperty("Sysml.subClass").split(" "));
//        System.out.println(subClass);
        Model model = ModelFactory.createDefaultModel();
        SetSourceName("http://www.neo4j.com/ontologies/data.owl");
        String inputFileName = Paths.get("1.rdf").toString();
        OntModel ontModel = getOntModel(model,inputFileName);
        for(String rel : subClasses) {
            List<String> o = Arrays.asList(rel.split(","));
            OntClass father = CoreOWLUtil.getClass(ontModel, o.get(0));
            OntClass son = CoreOWLUtil.getClass(ontModel, o.get(1));
            addSubClass(ontModel, father, son);
        }
        model.write(System.out, "N-TRIPLES");
        OutputStream out = Files.newOutputStream(Paths.get("Initialization.rdf"));
        model.write(out,"RDF/XML-ABBREV");
        // 先写入一些粗略的 uml 与sysml的对应， 在每次录入过程中， 通过同类项的推理， 将标签归类到相应的class子类下

    }
}
