package work;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import tool.CoreOWLUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static tool.CoreOWLUtil.*;



public class test2 {
    public static void main(String[] args) throws IOException {


        InputStream inputStream = test.class.getResourceAsStream("/Initialization.properties");
        Properties properties=new Properties();
        properties.load(inputStream);
        // 连接数据库
        String uri = properties.getProperty("neo4j.uri");
        String user = properties.getProperty("neo4j.user");
        String password = properties.getProperty("neo4j.password");
        Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));

        //rdf文件
        Model model = ModelFactory.createDefaultModel();
        SetSourceName("http://www.neo4j.com/ontologies/data.owl");

        String inputFileName = Paths.get("Initialization.rdf").toString();
        OntModel ontModel = getOntModel(model, inputFileName);

        // 执行一些数据库操作
        Map<List<String>, Integer> relation = new TreeMap<>((o1, o2) -> {
            //比较顺序
            return o1.get(0).compareTo(o2.get(0)) == 0 ?
                    (o1.get(2).compareTo(o2.get(2)) == 0 ?
                            o1.get(1).compareTo(o2.get(1)) : o1.get(2).compareTo(o2.get(2)))
                    : o1.get(0).compareTo(o2.get(0));
        });
        try (Session session = driver.session()) {
            Result result = session.run("MATCH (n)-[r]-(m) RETURN labels(n) as first , labels(m) as second ,type(r) as relation");
            while (result.hasNext()) {
                Record record = result.next();
                List<Object> first = record.get("first").asList();
                List<Object> second = record.get("second").asList();
                String rel = record.get("relation").asString();
                List<String> ad = new ArrayList<>();
                // todo sysml
                // 处理子类关系
                addList(ontModel, first);
                addList(ontModel, second);
                for(Object o1: first) {

                }
                relation.put(ad, 1);
            }
        }
        catch (Exception e) {
            e.printStackTrace();

        }
        // 关闭连接
        driver.close();

        printClasses(ontModel);

        model.write(System.out, "N-TRIPLES");
        OutputStream out = Files.newOutputStream(Paths.get("output.rdf"));
        model.write(out,"RDF/XML-ABBREV");
    }


}
