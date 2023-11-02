package tool;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


public class CoreOWLUtil {

    /*
     * @Description: 将OntModel类的对象写成一个OWL文件
     * @param: [ontModel]
     * @return: void
     **/

    public static String SourceName;


    public static void SetSourceName(String name){
        SourceName = name;
    }


    public static String getSourceName(){
        return SourceName;
    }

    /*
     * @Description: 返回OWL文件的命名空间(在开头名的基础上加上#号)
     * @Author: zt
     * @Date: 2023/2/28 16:26
     * @param: []
     * @return: java.lang.String
     **/
    public static String getNameSpace(){
        return  CoreOWLUtil.getSourceName() + "#";
    }

    public static void printClasses(OntModel ontModel) {
        // 获取所有类
        ExtendedIterator<OntClass> classes = ontModel.listClasses();

        while (classes.hasNext()) {
            OntClass ontClass = classes.next();
            System.out.println("Class: " + ontClass.getURI());
        }
    }


    public static OntModel getOntModel(Model model, String inputFileName) throws IllegalArgumentException {
        InputStream in = FileManager.get().open(inputFileName);
        if(in == null){
            throw new IllegalArgumentException(
                    "File: " + inputFileName + " not found.");
        }

        model.read(in, null);
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, model);
        return ontModel;
    }


    public static void ontModel2Owl(OntModel ontModel) throws IOException {
        //输出owl文件到文件系统
        String filepath = "src/main/resources/owl/core/CoreOntology.owl";
        FileOutputStream fileOS = new FileOutputStream(filepath);
//        RDFWriter rdfWriter = baseOnt.getWriter("RDF/XML");
        RDFWriter rdfWriter = ontModel.getWriter("RDF/XML");
        rdfWriter.setProperty("showXMLDeclaration","true");
        rdfWriter.setProperty("showDoctypeDeclaration", "true");
        rdfWriter.write(ontModel, fileOS, null);
        //用writer就不需要用下面的方法了
        //baseOnt.write(fileOS, "RDF/XML");
        fileOS.close();
    }

    /*
     * @Description: 将指定位置的OWL文件读取为OntModel类的一个对象
     * @param: []
     * @return: org.apache.jena.ontology.OntModel
     **/
    public static OntModel owl2OntModel(){
        //设置本体的命名空间
        String SOURCE = CoreOWLUtil.getSourceName();
        OntDocumentManager ontDocMgr = new OntDocumentManager();
        //设置本体owl文件的位置
        ontDocMgr.addAltEntry(SOURCE, "file:src/main/resources/owl/core/CoreOntology.owl");
        OntModelSpec ontModelSpec = new OntModelSpec(OntModelSpec.OWL_MEM);
        ontModelSpec.setDocumentManager(ontDocMgr);
        // asserted ontology
        OntModel baseOnt = ModelFactory.createOntologyModel(ontModelSpec);
        baseOnt.read(SOURCE, "RDF/XML");
        return baseOnt;
    }

    /*
     * @Description: 新增加一个类
     * @param: [ontModel 读取OWL文件生成的OntModel类对象, className 新增加类的名称]
     * @return: org.apache.jena.ontology.OntClass
     **/
    public static OntClass createClass(OntModel ontModel, String className) throws IOException {
        OntClass newClass = getClass(ontModel, className);
        if(newClass != null) return newClass;
        String nameSpace = CoreOWLUtil.getNameSpace();
        newClass = ontModel.createClass(nameSpace + className);
//        CoreOWLUtil.ontModel2Owl(ontModel);
        return newClass;
    }

    /*
     * @Description: 得到原模型中的类，没有则返回null
     * @param: [ontModel 读取OWL文件生成的OntModel类对象, className 类的名称]
     * @return: org.apache.jena.ontology.OntClass
     **/
    public static OntClass getClass(OntModel ontModel, String className) throws IOException {
        String nameSpace = CoreOWLUtil.getNameSpace();
        OntClass newClass = ontModel.getOntClass(nameSpace + className);
//        CoreOWLUtil.ontModel2Owl(ontModel);

        return newClass;
    }


    /*
     * @Description: 向传入的父类和子类添加父子关系
     * @Author: zt
     * @Date: 2023/2/28 16:53
     * @param: [ontModel 读取OWL文件生成的OntModel类对象, fatherClass 父类, sonClass 子类]
     * @return: void
     **/
    public static void addSubClass(OntModel ontModel, OntClass fatherClass, OntClass sonClass) throws IOException {
        fatherClass.addSubClass(sonClass);
//        CoreOWLUtil.ontModel2Owl(ontModel);
    }


    public static LinkedList<Path> getAllPath(OntModel ontModel, OntClass start, OntClass end) throws IOException {
        LinkedList<Path> paths = new LinkedList<Path>();
        OntClass top_node; //即栈顶节点
        OntClass cur_node;//当前的临接节点
        OntClass next_node;//下一个入栈节点
        //遍历过程中使用的栈
        LinkedList<Integer> stack = new LinkedList<Integer>();
        //标记节点是否在栈内，避免回路
        Map<OntClass, Integer> states = new HashMap<OntClass, Integer>();
        Queue<OntClass> queue = new LinkedList<>();
        List<OntClass> add = new ArrayList<>();
        add.add(start);
        int num = 0;

        queue.add(start);


        return paths;
//        Reasoner reasoner = ReasonerRegistry.getRDFSReasoner();
//
//        // Find the paths between the two classes
//        ExtendedIterator<org.apache.jena.graph.Triple> paths = ontModel.getGraph().find(start.asNode(),null,end.asNode());
//        while (paths.hasNext()) {
//            org.apache.jena.graph.Triple path = paths.next();
//            System.out.println("-------");
//            System.out.println(path);
//            System.out.println("-------");
//        }
//        return null;
    }


    /*
     * @Description: 获取该节点深度
     * @Author: shk001
     * @Date: 2023/10/26
     * @param: [ontModel 读取OWL文件生成的OntModel类对象, ontClass 类]
     * @return: Integer
     **/
    public static Integer getDeep(OntModel ontModel, OntClass ontClass) throws IOException {
        int deep = 0;
        while(ontClass.hasSuperClass()) {
            deep++;
            ontClass = ontClass.getSuperClass();
        }
        return deep;
    }

        /*
     * @Description: 将同一节点下的标签集导入本体
     * @Author: shk001
     * @Date: 2023/10/26
     * @param: [ontModel 读取OWL文件生成的OntModel类对象, list 同一个节点下的标签]
     * @return: void
     **/
    public static void addList(OntModel ontModel, List<Object> list) throws IOException {
        int deep = -1;
        String deepClassName = null;
        //创建所有节点，存在则获取,并找到最深的节点
        for(Object o: list) {
            OntClass newClass = createClass(ontModel, o.toString());
            int tmpDeep = getDeep(ontModel, newClass);
            if(tmpDeep > deep || (deep == tmpDeep && o.toString().contains("uml"))) { // 找到最深的本体
                deepClassName = o.toString();
                deep = tmpDeep;
            }
        }
        if(deepClassName.contains("uml")) { // 只有一层
            OntClass fatherClass = getClass(ontModel, deepClassName);
            for(Object o: list) {
                if(o.toString().contains("uml")) continue;
                OntClass sonClass = getClass(ontModel, o.toString());
                addSubClass(ontModel, fatherClass, sonClass);
            }
        }
        else {
            Map<OntClass, Integer> dirt = new HashMap<>();
            OntClass BroClass = getClass(ontModel, deepClassName);
            OntClass tmpClass = getClass(ontModel, deepClassName);
            dirt.put(tmpClass, 1);
            while(tmpClass.hasSuperClass()) {
                tmpClass = tmpClass.getSuperClass();
                dirt.put(tmpClass, 1);
            }
            for(Object o: list) {
                OntClass sonClass = getClass(ontModel, o.toString());
                if(dirt.containsKey(sonClass)) continue;

                addSiblingClass(ontModel, BroClass, sonClass);
                /*
                debug
                 */
                if(getClass(ontModel, "uml:DataType")!=null && getClass(ontModel, "uml:DataType").hasSuperClass()) {
                    System.out.println("--------------");
                    System.out.println(list);
                    System.out.println("--------------");
                }
            }
        }
    }



    /*
     * @Description: 向传入的兄弟类和子类添加父子关系
     * @Author: shk001
     * @param: [ontModel 读取OWL文件生成的OntModel类对象, BroClass 兄弟类, sonClass 子类]
     * @return: void
     **/
    public static void addSiblingClass(OntModel ontModel, OntClass BroClass, OntClass sonClass) throws IOException {
        if(BroClass.equals(sonClass)) return;
        OntClass fatherClass = BroClass.getSuperClass();
        fatherClass.addSubClass(sonClass);
//        CoreOWLUtil.ontModel2Owl(ontModel);
    }

    /*
     * @Description: 向传入的头类和尾类之间添加关系，关系名称由参数传递
     * @Author: zt
     * @Date: 2023/2/28 17:11
     * @param: [ontModel 读取OWL文件生成的OntModel类对象, sourceClass 头类, targetClass 尾类, relationName 关系名称]
     * @return: org.apache.jena.ontology.ObjectProperty
     **/
    public static OntProperty addRelation(OntModel ontModel, OntClass sourceClass, OntClass targetClass, String relationName) throws IOException {
        String nameSpace = CoreOWLUtil.getNameSpace();
        OntProperty newRelation = ontModel.createOntProperty(nameSpace + relationName);
        newRelation.addDomain(sourceClass);
        newRelation.addRange(targetClass);
//        CoreOWLUtil.ontModel2Owl(ontModel);
        return newRelation;
    }

    /*
     * @Description: 获得与sourceClass相连的所有Property
     * @Author: shk001
     * @param: [ontModel 读取OWL文件生成的OntModel类对象, sourceClass 头类]
     * @return: org.apache.jena.ontology.ObjectProperty
     **/
    public static List<HashMap<OntProperty, OntClass>> getRelations(OntModel ontModel, OntClass sourceClass) throws IOException {
        String nameSpace = CoreOWLUtil.getNameSpace();
        ExtendedIterator<OntProperty> prop = sourceClass.listDeclaredProperties();
        System.out.println(prop.toString());
        List<HashMap<OntProperty, OntClass>> result = new ArrayList<HashMap<OntProperty, OntClass>>();
        System.out.println("-----------------");
        while (prop.hasNext()) {
            OntProperty ppp = prop.next();
            System.out.println(ppp.getURI());
            OntClass next = ppp.getRange().asClass();
            HashMap<OntProperty, OntClass> ad = new HashMap<OntProperty, OntClass>();
            ad.put(ppp, next);
            result.add(ad);
        }

        ExtendedIterator<OntProperty> properties = ontModel.listAllOntProperties()
                .filterKeep(p -> p.getDomain().equals(sourceClass));
        while (properties.hasNext()) {
            OntProperty property = properties.next();
            HashMap<OntProperty, OntClass> ad = new HashMap<OntProperty, OntClass>();
            ExtendedIterator<? extends OntResource> rangeIter = property.listRange()
                    .filterKeep(r -> r.asClass().hasSuperClass(sourceClass));
            while (rangeIter.hasNext()) {
                OntResource range = rangeIter.next();
                OntClass next = range.asClass();
                ad.put(property, next);
            }
            result.add(ad);
        }
        System.out.println(result.size());
        System.out.println("-----------------");
        return  result;
 //        CoreOWLUtil.ontModel2Owl(ontModel);

    }

    /*
     * @Description: 向传入的类添加属性信息，属性名称由参数传递
     * @Author: zt
     * @Date: 2023/2/28 17:23
     * @param: [ontModel 读取OWL文件生成的OntModel类对象, ontClass 需要被添加的类别, propertyName 属性名称]
     * @return: org.apache.jena.ontology.DatatypeProperty
     **/
    public static DatatypeProperty addProperty(OntModel ontModel, OntClass ontClass, String propertyName) throws IOException {
        String nameSpace = CoreOWLUtil.getNameSpace();
        DatatypeProperty newProperty = ontModel.createDatatypeProperty(nameSpace + propertyName);
        newProperty.addDomain(ontClass);
//        CoreOWLUtil.ontModel2Owl(ontModel);
        return newProperty;
    }

    /*
     * @Description: 删除指定的类中的属性信息
     * @Author: zt
     * @Date: 2023/3/27 15:39
     * @param: [ontModel, ontClass, propertyName]
     * @return: void
     **/
    public static void removeProperty(OntModel ontModel, OntClass ontClass, String propertyName) throws IOException {
        String nameSpace = CoreOWLUtil.getNameSpace();
        DatatypeProperty newProperty = ontModel.createDatatypeProperty(nameSpace + propertyName);
        newProperty.removeDomain(ontClass);
//        CoreOWLUtil.ontModel2Owl(ontModel);
    }

    /*
     * @Description: 删除指定名称的类，如果这个类有子类的话，就不能删除，直接抛出异常
     * @Author: zt
     * @Date: 2023/2/28 18:18
     * @param: [ontModel 读取OWL文件生成的OntModel类对象, classname 需要删除的类的名称]
     * @return: void
     **/
    public static void removeClass(OntModel ontModel, String classname) throws Exception {
        OntClass ontClass = CoreOWLUtil.createClass(ontModel, classname);
        ontClass.remove();
//        CoreOWLUtil.ontModel2Owl(ontModel);
    }

    /*
     * @Description: 删除指定名称的关系中的指定的domain和range
     * @Author: zt
     * @Date: 2023/3/7 10:42
     * @param: [ontModel, propertyName 关系的名称, ontClass 要从这个关系中移除的类]
     * @return: void
     **/
    public static void removeRelationDomainAndRange(OntModel ontModel, String propertyName, OntClass ontClass) throws IOException {
        String nameSpace = CoreOWLUtil.getNameSpace();
        ObjectProperty relation = ontModel.createObjectProperty(nameSpace + propertyName);
        //只要这个类是这个关系的range或者domain，就进行删除
        if(relation.hasDomain(ontClass)){
            relation.removeDomain(ontClass);
        }
        if(relation.hasRange(ontClass)){
            relation.removeRange(ontClass);
        }
//        CoreOWLUtil.ontModel2Owl(ontModel);
    }

    public static void removeRelation(OntModel ontModel, String propertyName, OntClass domain, OntClass range) throws IOException {
        String nameSpace = CoreOWLUtil.getNameSpace();
        ObjectProperty relation = ontModel.createObjectProperty(nameSpace + propertyName);
        if(relation.hasDomain(domain)){
            relation.removeDomain(domain);
        }
        if(relation.hasRange(range)){
            relation.removeRange(range);
        }
//        CoreOWLUtil.ontModel2Owl(ontModel);
    }

}