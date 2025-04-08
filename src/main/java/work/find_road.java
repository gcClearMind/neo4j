package work;


import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.neo4j.driver.types.Relationship;
import tool.CoreOWLUtil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;

import static tool.CoreOWLUtil.*;

public class find_road {
    public static String getSWRL(org.neo4j.driver.types.Path path){

        List<Node> nodes = (List<Node>) path.nodes();
        List<Relationship> relationships = (List<Relationship>) path.relationships();
        StringBuilder res = new StringBuilder();
        String cur = null;
        String now = null;
        Boolean flag = false;
        String start = null;
        String end = null;
        for(int i = 0; i  < nodes.size(); i++) {
            Node node = nodes.get(i);
            Map nodeMap = node.asMap();
            now = "individual" + String.valueOf((char)('A' + i));
            String xmiType = (String)nodeMap.get("xmi:type");
            List<String> labels = (List<String>) node.labels();
            String label = null;

            if(labels.size() == 1) {
                label = labels.get(0);
            }
            else {
                for (String s : labels) {
                    if (s.equals(xmiType)) {
                        continue;
                    } else {
                        label = s;
                        break;
                    }
                }
            }
            if(i == 0) {
                start = now;
                res.append(label).append("(?").append(now).append(") ^ ");
            }
            else{
                Relationship relationship = relationships.get(i - 1);
                String rel = relationship.type();
                Long endId = relationship.endNodeId();
                if(endId.equals(node.id())) {
                    res.append(rel).append("(?").append(cur).append(", ?").append(now).append(") ^ ");
                }
                else { // 反向
                    res.append(rel).append("(?").append(now).append(", ?").append(cur).append(") ^ ");
                }
                if(i == nodes.size() - 1) {
                    end = now;
                    res.append(label).append("(?").append(now).append(")");
                }
                else{
                    res.append(label).append("(?").append(now).append(") ^ ");
                }
            }

            cur = now;

        }
        res.append(" -> ").append("relation").append("(?").append(start).append(", ?").append(end).append(")");
        return res.toString();
    }

    public static void main(String[] args) throws IOException {

        InputStream inputStream = test.class.getResourceAsStream("/Initialization.properties");
        Properties properties=new Properties();
        properties.load(inputStream);
        // 连接数据库
        String uri = properties.getProperty("neo4j.uri");
        String user = properties.getProperty("neo4j.user");
        String password = properties.getProperty("neo4j.password");
        Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));

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
                String swrl = getSWRL(path);

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
                    double articleRankScore = 0.0;
                    double pageRankScore = 0.0;
                    int length = 0;

                    for(org.neo4j.driver.types.Node node : path.nodes()) {
                        Map nodeMap = node.asMap();
                        articleRankScore = articleRankScore + (double) nodeMap.get("articleRankScore");
                        pageRankScore = pageRankScore + (double) nodeMap.get("pageRankScore");

                        length++;
                    }

                    double avg_articleRankScore = articleRankScore / length ;
                    double avg_pageRankScore = pageRankScore / length ;


                    writer.write(showPath(path));
                    writer.newLine();
                    writer.write("articleRankScore: " + articleRankScore + "; avg_articleRankScore: " + avg_articleRankScore +
                            "; pageRankScore: " + pageRankScore + "; avg_pageRankScore: " + avg_pageRankScore);
                    writer.newLine();

                    writer.flush();
                }
                writer.newLine();
                writer.newLine();
                writer.newLine();
                writer.flush();

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}