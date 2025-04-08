package work;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import tool.CoreOWLUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static tool.CoreOWLUtil.*;



public class test {

    public static void main(String[] args) throws IOException {
        InputStream inputStream = test.class.getResourceAsStream("/Initialization.properties");
        Properties properties = new Properties();
        properties.load(inputStream);
        String uri = properties.getProperty("neo4j.uri");
        String user = properties.getProperty("neo4j.user");
        String password = properties.getProperty("neo4j.password");
        Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        try (Session session = driver.session()) {
            String query = "MATCH (n)<-[:client]-(r)-[:supplier]->(m) " +
                    "RETURN id(n) AS start, id(r) AS mid, r, id(m) AS end";

            Result result = session.run(query);

            while (result.hasNext()) {
                Record record = result.next();

                long startId = record.get("start").asLong();
                long endId = record.get("end").asLong();

                Node rNode = record.get("r").asNode();

                String relationType = null;
                for (String label : rNode.labels()) {
                    if (!label.startsWith("uml:")) {
                        relationType = label;
                        break; // 找到就退出
                    }
                }

                // ✅ 2. 如果没有非uml:标签，检查 name 属性
                if (relationType == null || relationType.isEmpty()) {
                    if (rNode.containsKey("name") && !rNode.get("name").isNull()) {
                        relationType = rNode.get("name").asString();
                    }
                }

                // ✅ 3. 最终 fallback
                if (relationType == null || relationType.isEmpty()) {
                    // fallback：取第一个 label，或者默认值
                    relationType = rNode.labels().iterator().hasNext() ?
                            rNode.labels().iterator().next() :
                            "Relation";
                }

                // ✅ 4. 清理非法字符（Neo4j 不允许 : / 空格 等）
                relationType = relationType.replaceAll("[^a-zA-Z0-9_]", "_").toLowerCase();
                // ✅ 打印日志
                System.out.println("Create relation from node " + startId + " to " + endId + " type: " + relationType);

                // ✅ 执行创建关系
                String createRelationQuery =
                        "MATCH (a), (b) " +
                                "WHERE id(a) = $startId AND id(b) = $endId " +
                                "MERGE (a)-[rel:" + relationType + "]->(b)";

                session.run(createRelationQuery,
                        Values.parameters("startId", startId, "endId", endId));

            }

        } finally {
            driver.close();
        }
    }
}

