package work;

import org.apache.jena.base.Sys;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Path;
import tool.CoreOWLUtil;

import java.io.BufferedWriter;
import java.io.FileWriter;
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

        String filePath = "data/path/output.txt";

        String start = "Requirement";
        String end = "Block";
        int pathLen = 6;
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
        Set<String> SWRL_list = new HashSet<>();
        Map<String, Vector<Path>> swrl_map = new HashMap<>();
        try (Session session = driver.session()) {
            Result result = session.run("match p = (n:`" + start + "`)-[*2.."+ pathLen +"]-(m:`" + end + "`) " +
                    " WHERE NOT ANY(r IN relationships(p) WHERE type(r) in ['packagedElement','client','supplier'])" +
                    " return p as list");
            int id = 0;
            while (result.hasNext()) {
                Record record = result.next();
                org.neo4j.driver.types.Path path = record.get("list").asPath();
                String swrl = CoreOWLUtil.getSWRL(ontModel, path);

//                writer.write(showPath(path));
//                writer.newLine();
//                writer.write(swrl);
//                writer.newLine();
//                writer.newLine();
//                writer.flush();

                if(SWRL_list.contains(swrl)) {
                    swrl_map.get(swrl).add(path);
                }
                else {
                    SWRL_list.add(swrl);
                    swrl_map.put(swrl, new Vector<>());
                    swrl_map.get(swrl).add(path);
                }

            }
            for(String key : swrl_map.keySet()) {
                writer.write(String.valueOf(++id));
                writer.newLine();
                writer.write(key);
                writer.newLine();
                writer.flush();
                for(Path path : swrl_map.get(key)) {
                    writer.write(showPath(path));
                    writer.newLine();
                    writer.flush();
                }
                writer.newLine();
                writer.newLine();
                writer.newLine();
                writer.flush();
                System.out.print(key);
                System.out.println(" " + swrl_map.get(key).size());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
