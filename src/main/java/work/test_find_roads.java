package work;

import org.apache.jena.base.Sys;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import tool.CoreOWLUtil;
import tool.Path;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;

import static tool.CoreOWLUtil.*;

public class test_find_roads {
    public static void main(String[] args) throws IOException {

        InputStream inputStream = test.class.getResourceAsStream("/Initialization.properties");
        Properties properties=new Properties();
        properties.load(inputStream);
        // 连接数据库
        String uri = properties.getProperty("neo4j.uri");
        String user = properties.getProperty("neo4j.user");
        String password = properties.getProperty("neo4j.password");
        Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));

        Model model = ModelFactory.createDefaultModel();
        SetSourceName("http://www.neo4j.com/ontologies/data.owl");

        String inputFileName = Paths.get("data/output_individual.rdf").toString();
        OntModel ontModel = getOntModel(model, inputFileName);

        String start = "Block";
        String end = "Block";
        int pathLen = 4;
        Set<String> SWRL_list = new HashSet<>();
        try (Session session = driver.session()) {
            Result result = session.run("match p = (n:`" + start + "`)-[*2.."+ pathLen +"]->(m:`" + end + "`) " +
                    "return p as list");
            while (result.hasNext()) {
                Record record = result.next();
                org.neo4j.driver.types.Path path = record.get("list").asPath();
                String swrl = CoreOWLUtil.getSWRL(ontModel, path);
                System.out.println("-------------------------------------------------------------------------------------------");
                System.out.println(swrl);
                System.out.println(showPath(path));

                System.out.println("-------------------------------------------------------------------------------------------");
                System.out.println();
                SWRL_list.add(swrl);
            }
            for(String swrl : SWRL_list) {
                System.out.println(swrl);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
