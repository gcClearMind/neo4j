package work;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import tool.CoreOWLUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
                ad.add("0");
                ad.add("0");
                ad.add("0");
                // 处理子类关系
                List<OntClass> first_add_classes = addList(ontModel, first);
                List<OntClass> second_add_classes = addList(ontModel, second);
                // 处理直接关系
                for(OntClass o1: first_add_classes) {
                    String first_label = o1.getURI().substring(o1.getURI().indexOf('#') + 1);
                    if(first_label.contains("uml") && first.size() != 1) { // 判断为不为uml类的label
                        continue;
                    }
                    for(OntClass o2: second_add_classes) {
                        String second_label = o2.getURI().substring(o2.getURI().indexOf('#') + 1);
                        if(second_label.contains("uml") && second.size() != 1) {
                            continue;
                        }

                        ad.set(0,first_label);
                        ad.set(1,rel);
                        ad.set(2,second_label);
                        relation.put(ad, 1);
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();

        }
        // 关闭连接
        driver.close();
        for(List<String> o : relation.keySet()){
            OntClass first = CoreOWLUtil.createClass(ontModel, o.get(0));
            String relationName = o.get(1);
            OntClass second = CoreOWLUtil.createClass(ontModel, o.get(2));
            addRelation(ontModel, first, second, relationName);
        }
        printClasses(ontModel);
        printProperties(ontModel);
//        model.write(System.out, "N-TRIPLES");
        OutputStream out = Files.newOutputStream(Paths.get("output.rdf"));
        model.write(out,"RDF/XML-ABBREV");
    }


}
