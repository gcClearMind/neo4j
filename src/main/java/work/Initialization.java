package work;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import java.io.IOException;
import java.nio.file.Paths;

import static tool.CoreOWLUtil.*;

public class Initialization {
    public static void main(String[] args) throws IOException {
        Model model = ModelFactory.createDefaultModel();
        String inputFileName = Paths.get("1.rdf").toString();
        OntModel ontModel = getOntModel(model,inputFileName);
        printClasses(ontModel);
        model.write(System.out, "N-TRIPLES");
    }
}
