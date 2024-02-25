package work;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import tool.CoreOWLUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static tool.CoreOWLUtil.*;

public class Initialization {
    public static void main(String[] args) throws IOException {
        String fileName = Paths.get("infor.txt").toString();
        final String CHARSET_NAME = "UTF-8";

        List<String> subClasses = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), CHARSET_NAME))) {
            String line;
            while ((line = br.readLine()) != null) {
                subClasses.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        subClasses.forEach(System.out::println);
        Model model = ModelFactory.createDefaultModel();
        SetSourceName("http://www.neo4j.com/ontologies/data.owl");
        String inputFileName = Paths.get("1.rdf").toString();
        OntModel ontModel = getOntModel(model,inputFileName);
        for(String rel : subClasses) {
            List<String> o = Arrays.asList(rel.split(" "));
            OntClass father = CoreOWLUtil.createClass(ontModel, o.get(0));
            OntClass son = CoreOWLUtil.createClass(ontModel, o.get(1));
            addSubClass(father, son);
        }
        model.write(System.out, "N-TRIPLES");
        OutputStream out = Files.newOutputStream(Paths.get("data/Initialization.rdf"));
        model.write(out,"RDF/XML-ABBREV");
        // 先写入一些粗略的 uml 与sysml的对应， 在每次录入过程中， 通过同类项的推理， 将标签归类到相应的class子类下

    }
}
