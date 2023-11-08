package tool;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;

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
    public static OntClass createClass(OntModel ontModel, String className){
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
    public static OntClass getClass(OntModel ontModel, String className) {
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
    public static void addSubClass(OntModel ontModel, OntClass fatherClass, OntClass sonClass) {
        fatherClass.addSubClass(sonClass);
//        CoreOWLUtil.ontModel2Owl(ontModel);
    }

    private static Path genPath(LinkedList<Pair<OntProperty, OntClass>> stack) {
        Path path = new Path();

        // 遍历给定的堆栈
        for (Pair<OntProperty, OntClass> pair : stack) {
            path.add(pair);
        }

        // 返回生成的路径
        return path;
    }


    public static LinkedList<Path> getAllPath(OntModel ontModel, OntClass start, OntClass end) throws IOException {
        LinkedList<Path> paths = new LinkedList<>();
        Pair<OntProperty, OntClass> top_node; //即栈顶节点
        Pair<OntProperty, OntClass> cur_node;//当前的临接节点
        Pair<OntProperty, OntClass> next_node;//下一个入栈节点
        //遍历过程中使用的栈
        LinkedList<Pair<OntProperty, OntClass>> stack = new LinkedList<>();
        //标记节点是否在栈内，避免回路
        Map<OntClass, Integer> states = new HashMap<>();
        ExtendedIterator<OntClass> classes = ontModel.listClasses();
        while (classes.hasNext()) {
            OntClass ontClass = classes.next();
            states.put(ontClass, 0);
        }
        states.put(start, 1);
        cur_node = null;
        stack.add(new Pair<>(null, start));
        while(!stack.isEmpty()) {
            top_node = stack.getLast();
            //找到一条路径
            if(top_node.getValue().equals(end) && stack.size() > 1) {
                if(! (stack.size() <= 2 && start.equals(end))) {
                    paths.add(genPath(stack));
                }
                cur_node = stack.removeLast();
                states.put(cur_node.getValue(), states.get(cur_node.getValue()) - 1);
            }
            else
            {
                List<Pair<OntProperty, OntClass>> next_nodes = getRelations(ontModel, top_node.getValue());
                int flag = -1;
                next_node = null;
                for(Pair<OntProperty, OntClass> m : next_nodes) {
                    if(cur_node == null) {
                        next_node = m;
                        break;
                    }
                    if(m.getValue().equals(cur_node)) {
                        flag = 1;
                        continue;
                    }
                    if(flag == -1 || (states.get(m.getValue()) == 1 && !end.equals(m.getValue()))) {
                        continue;
                    }
                    else {
                        next_node = m;
                        break;
                    }
                }
                if (next_node != null) {
                    if(next_node.equals(end)) {
                        System.out.println("ok");
                    }
                    stack.add(next_node);
                    states.put(next_node.getValue(), states.get(next_node.getValue()) + 1);
                    cur_node = null; //新节点入栈，要从头遍历它的所有临接节点
                }
                else {
                    cur_node = stack.removeLast();
                    states.put(cur_node.getValue(), 0);
                }
            }

        }

        return paths;

    }



    /*
     * @Description: 获取该节点深度
     * @Author: shk001
     * @Date: 2023/10/26
     * @param: [ontModel 读取OWL文件生成的OntModel类对象, ontClass 类]
     * @return: Integer
     **/
    public static Integer getDeep(OntModel ontModel, OntClass ontClass) {
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
    public static void addSiblingClass(OntModel ontModel, OntClass BroClass, OntClass sonClass) {
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
//        sourceClass.addProperty(newRelation, nameSpace+relationName);
//        CoreOWLUtil.ontModel2Owl(ontModel);
        return newRelation;
    }

    /*
     * @Description: 获得与sourceClass相连的所有Property
     * @Author: shk001
     * @param: [ontModel 读取OWL文件生成的OntModel类对象, sourceClass 头类]
     * @return: org.apache.jena.ontology.ObjectProperty
     **/
    public static List<Pair<OntProperty, OntClass>> getRelations(OntModel ontModel, OntClass sourceClass) throws IOException {
        String nameSpace = CoreOWLUtil.getNameSpace();

        List<Pair<OntProperty, OntClass>> result = new ArrayList<>();
        /*
            一个property中有多个domain和range时无法返回该property
         */
//        ExtendedIterator<OntProperty> prop = sourceClass.listDeclaredProperties(true);
//        while (prop.hasNext()) {
//            OntProperty ppp = prop.next();
//            OntClass range = ppp.getRange() == null ? null: ppp.getRange().asClass();
//
//            Pair<OntProperty, OntClass> ad = new Pair(ppp, range);
////            if(range.equals(sourceClass)) {
////                System.out.println("-------------------------");
////                System.out.println("ok");
////                System.out.println("-------------------------");
////            }
//            result.add(ad);
//        }
        Iterator properties = ontModel.listOntProperties();
        while (properties.hasNext()) {
            OntProperty property = (OntProperty) properties.next();
            Iterator<? extends OntResource> domains = property.listDomain();
            while (domains.hasNext()) {
                OntClass domain = domains.next().asClass();
                if(!(domain.getURI().compareTo(sourceClass.getURI()) == 0)) {
                    continue;
                }
                /*
                todo
                找不到对应的range
                 */
                OntClass range = property.getRange().asClass();
                Pair<OntProperty, OntClass> ad = new Pair<>(property, range);
                result.add(ad);
            }


        }
//        ExtendedIterator<OntProperty> properties = ontModel.listAllOntProperties()
//                .filterKeep(p -> p.getDomain().equals(sourceClass));
//        while (properties.hasNext()) {
//            OntProperty property = properties.next();
//            OntClass range = property.getRange().asClass();
//            if(range.equals(sourceClass)) {
//                System.out.println("-------------------------");
//                System.out.println("ok");
//                System.out.println("-------------------------");
//            }
//            Pair<OntProperty, OntClass> ad = new Pair(property, range);
//            result.add(ad);
//        }
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