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



public class test {

    public static void main(String[] args) throws IOException {


        InputStream inputStream = test.class.getResourceAsStream("/Initialization.properties");
        Properties properties=new Properties();
        properties.load(inputStream);
        // 连接数据库
        String uri = properties.getProperty("neo4j.uri");
        String user = properties.getProperty("neo4j.user");
        String password = properties.getProperty("neo4j.password");
        Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));

        // 执行一些数据库操作
        Map<List<String>, Integer> relation = new TreeMap<>(new Comparator<List<String>>() {
            @Override
            public int compare(List<String> o1, List<String> o2) {
                //比较顺序
                return o1.get(0).compareTo(o2.get(0)) == 0 ?
                        (o1.get(2).compareTo(o2.get(2)) == 0 ?
                                o1.get(1).compareTo(o2.get(1)) : o1.get(2).compareTo(o2.get(2)))
                        : o1.get(0).compareTo(o2.get(0));
            }
        });
        try (Session session = driver.session()) {
            Result result = session.run("MATCH (n)-[r]-(m) RETURN labels(n) as first , labels(m) as second ,type(r) as relation");
            while (result.hasNext()) {
                Record record = result.next();
                List<Object> first = record.get("first").asList();
                List<Object> second = record.get("second").asList();
                String rel = record.get("relation").asString();
                String fs = null, se = null;
                List<String> ad = new ArrayList<>();

                // todo sysml
                for(Object x : first) {
                    if(! x.toString().contains("uml")){
                        fs = x.toString();
                    }
                }
                if (fs == null) {
                    fs = first.get(0).toString();
                }
                ad.add(fs);
                ad.add(rel);
                for(Object x : second) {
                    if(! x.toString().contains("uml")){
                        se = x.toString();
                    }
                }
                if (se == null) {
                    se = first.get(0).toString();
                }
                ad.add(se);
                relation.put(ad, 1);
            }
        }
        catch (Exception e) {
            e.printStackTrace();

        }

//        OutputStream out2 = Files.newOutputStream(Paths.get("data.txt"));
//        for(List<String> x : relation.keySet()) {
////            System.out.println(x);
//
//            out2.write((x.toString()+ '\n').getBytes());
//        }
        // 关闭连接
        driver.close();

        Model model = ModelFactory.createDefaultModel();
        SetSourceName("http://www.neo4j.com/ontologies/data.owl");

        String inputFileName = Paths.get("data/1.rdf").toString();
        OntModel ontModel = getOntModel(model, inputFileName);




        for(List<String> o : relation.keySet()){
            OntClass first = CoreOWLUtil.createClass(ontModel, o.get(0));
            String relationName = o.get(1);
            OntClass second = CoreOWLUtil.createClass(ontModel, o.get(2));
            addRelation(ontModel, first, second, relationName);
        }
        printClasses(ontModel);

        model.write(System.out, "N-TRIPLES");
        OutputStream out = Files.newOutputStream(Paths.get("data/output.rdf"));
        model.write(out,"RDF/XML-ABBREV");
    }


}
