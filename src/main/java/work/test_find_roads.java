package work;

import org.apache.jena.base.Sys;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Path;
import org.neo4j.driver.types.Relationship;
import org.swrlapi.drools.owl.axioms.A;
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
        int pathLen = 4;
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
        Set<String> SWRL_list = new HashSet<>();
        Map<String, Vector<Path>> swrl_map = new HashMap<>();
        try (Session session = driver.session()) {
            Result result = session.run("match p = (n:`" + start + "`)-[*2.."+ pathLen +"]-(m:`" + end + "`) " +
                    " WHERE NOT ANY(r IN relationships(p) WHERE type(r) in ['packagedElement'])" +
                    " return p as list");
            int id = 0;
            int sum = 0;
            while (result.hasNext()) {
                Record record = result.next();
                org.neo4j.driver.types.Path path = record.get("list").asPath();
                String swrl = CoreOWLUtil.getSWRL(ontModel, path);

                sum++;

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
                writer.write(swrl_map.get(key).size() + " " + 1.0 * swrl_map.get(key).size() / sum);
                writer.newLine();
                writer.flush();

                for(Path path : swrl_map.get(key)) {
                    int length = 0;

                    for(org.neo4j.driver.types.Node node : path.nodes()) {
                        Map nodeMap = node.asMap();


                        length++;
                    }
//                    org.neo4j.driver.types.Node startNode = path.start();
//                    org.neo4j.driver.types.Node endNode = path.end();
//                    result = session.run("match(n)-[r]-(m) where id(n) = '" + startNode.id() + "' and id(m)  = '" + endNode.id() + "' return r" );
//                    ArrayList<Relationship> relList = new ArrayList<>();
//                    while (result.hasNext()) {
//                        Record record = result.next();
//                        Relationship rel = record.get("r").asRelationship();
//                        relList.add(rel);
//                    }

                    writer.write(showPath(path));
                    writer.newLine();
                    writer.newLine();
//                    if(!relList.isEmpty()) {
//                        System.out.println(relList);
//                    }
//
//                    writer.write(relList.toString());
//                    writer.newLine();
                    writer.flush();
                }
                writer.newLine();
                writer.newLine();
                writer.newLine();
                writer.flush();
//                System.out.print(key);
//                System.out.println(" " + swrl_map.get(key).size());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
