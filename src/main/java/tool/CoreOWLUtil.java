package tool;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;

import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;

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
        int size = 0;
        while (classes.hasNext()) {
            size++;
            OntClass ontClass = classes.next();
            System.out.println("Class: " + ontClass.getURI());
        }
        System.out.println("Class size: " + size);
    }

    public static void printProperties(OntModel ontModel) {
        // 获取所有类
        int size = 0;
       Iterator<OntProperty> properties = ontModel.listOntProperties();
       while (properties.hasNext()) {
           size++;
           OntProperty property = properties.next();
           System.out.println("Property: " + property.getURI());
       }
        System.out.println("Property size: " + size);
    }


    public static Integer getPropertyNumber(OntProperty property, OntClass sourceClass, OntClass targetClass) {
        int num = 0;
        Iterator<? extends OntProperty> subProperties =  property.listSubProperties();
        while (subProperties.hasNext()) {
            OntProperty subProperty = subProperties.next();
            if(subProperty.getDomain().asClass().equals(sourceClass) &&
                subProperty.getRange().asClass().equals(targetClass)) {
                num = -1;
                break;
            }
            num++;
        }
        return num;
    }


    public static String getRealName(String ontName) {
        return ontName.substring(ontName.indexOf('#') + 1);
    }


    public static String showPath(Path path) {
        List<Pair<OntProperty, OntClass>> pathList = path.getPathList();
        List<Pair<String, String>> realList = new ArrayList<>();
        for(Pair<OntProperty, OntClass> o : pathList) {
            String KeyName = null, ValueName = null;
            if (o.getKey() != null) {
//                KeyName = getRealName(o.getKey().getSuperProperty().getURI());
                KeyName = getRealName(o.getKey().getURI());
            }
            ValueName = getRealName(o.getValue().getURI());
            realList.add(new Pair<>(KeyName, ValueName));
        }
        return realList.toString();
    }

    public static String showPath(org.neo4j.driver.types.Path path) {
        List<Node> nodes = (List<Node>) path.nodes();
        List<Relationship> relationships = (List<Relationship>) path.relationships();
        StringBuilder res = new StringBuilder();
        for(int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            Map<String, Object> nodeMap = node.asMap();
            String nodeName = null;
            if(node.containsKey("name")) {
                nodeName = (String) nodeMap.get("name");
            }
            else {
                nodeName = "";
            }
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
                res.append("[(").append(label).append(":").append(nodeName).append(")");
            }
            else {
                Relationship relationship = relationships.get(i - 1);
                Long endId = relationship.endNodeId();
                if(endId.equals(node.id())) {
                    res.append("-[:").append(relationship.type()).append("]->");
                }
                else { // 反向
                    res.append("<-[:").append(relationship.type()).append("]-");

                }
                res.append("(").append(label).append(":").append(nodeName).append(")");
            }
        }
        res.append("]");
        return  res.toString();
    }


    public static String getSWRL(OntModel ontModel, org.neo4j.driver.types.Path path){

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


    public static String getSWRL(Path path, String result) {
        int index = 0;
        StringBuilder res = new StringBuilder();
        StringBuilder res_SWRL = new StringBuilder();
        String cur = null;
        String now = null;
        res_SWRL.append("[Rules: ");
        result = getNameSpace() + result;
        List<Pair<OntProperty, OntClass>> pathList = path.getPathList();
        String start = null;
        String end = null;
        for(Pair<OntProperty, OntClass> o : pathList) {
            String KeyName = null, ValueName = null;
            now = String.valueOf((char)('a' + index));
            if(o.getKey() != null) {
//                KeyName = getRealName(o.getKey().getURI());
                KeyName = o.getKey().getURI();
            }
            else {
                start = now;
            }
//            ValueName = getRealName(o.getValue().getURI());
            ValueName = o.getValue().getURI();
            if(KeyName == null) {
                res.append(ValueName).append("(?").append(now).append(") ^ ");
                res_SWRL.append("(").append("?").append(now).append(" rdf:type ").append(ValueName).append("), ");
            }
            else {
                res.append(KeyName).append("(?").append(cur).append(", ?").append(now).append(") ^ ");
                res_SWRL.append("(?").append(cur).append(" ").append(KeyName).append(" ?").append(now).append("), ");
                if(index != pathList.size() - 1) {
                    res.append(ValueName).append("(?").append(now).append(") ^ ");
                    res_SWRL.append("(").append("?").append(now).append(" rdf:type ").append(ValueName).append("), ");
                }
                else {
                    end = now;
                    res.append(ValueName).append("(?").append(now).append(")");
                    res_SWRL.append("(").append("?").append(now).append(" rdf:type ").append(ValueName).append(")");
                }
            }
            cur = now;
            index++;
        }
        res.append(" -> ").append(result).append("(?").append(start).append(", ?").append(end).append(")");
        res_SWRL.append(" -> ").append("(?").append(start).append(" ").append(result).append(" ?").append(end).append(")");
        res_SWRL.append("]");
        System.out.println(res);
        return res_SWRL.toString();
    }


    public static OntModel getOntModel(Model model, String inputFileName) throws IllegalArgumentException {
        InputStream in = FileManager.get().open(inputFileName);
        if(in == null){
            throw new IllegalArgumentException(
                    "File: " + inputFileName + " not found.");
        }

        model.read(in, null);
        return ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, model);
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
        return newClass;
    }

    /*
     * @Description: 得到原模型中的类，没有则返回null
     * @param: [ontModel 读取OWL文件生成的OntModel类对象, className 类的名称]
     * @return: org.apache.jena.ontology.OntClass
     **/
    public static OntClass getClass(OntModel ontModel, String className) {
        String nameSpace = CoreOWLUtil.getNameSpace();
        return ontModel.getOntClass(nameSpace + className);
    }


    /*
     * @Description: 向传入的父类和子类添加父子关系
     * @Author: zt
     * @Date: 2023/2/28 16:53
     * @param: [ontModel 读取OWL文件生成的OntModel类对象, fatherClass 父类, sonClass 子类]
     * @return: void
     **/
    public static void addSubClass(OntClass fatherClass, OntClass sonClass) {
        fatherClass.addSubClass(sonClass);
    }

    private static Path genPath(LinkedList<Pair<OntProperty, OntClass>> stack) {
        Path path = new Path();

        // 遍历给定的堆栈
        for (Pair<OntProperty, OntClass> pair : stack) {
            OntProperty property = pair.getKey();
//            if (property != null) {
//                property = property.getSuperProperty();
//            }
            path.add(new Pair<>(property, pair.getValue()));
        }

        // 返回生成的路径
        return path;
    }


    public static LinkedList<Path> getAllPath(OntModel ontModel, OntClass start, OntClass end, int Max_Path_Number) {
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
        if(!start.equals(end)) {
            states.put(start, 1);
        }
        cur_node = null;
        int i = 0;
        stack.add(new Pair<OntProperty, OntClass>(null, start));
        while(!stack.isEmpty()) {
//            System.out.println(stack.size());
            top_node = stack.getLast();
            if(stack.size() > Max_Path_Number) {
                cur_node = stack.removeLast();
                states.put(cur_node.getValue(), 0);
                continue;
            }
            //找到一条路径
            if(top_node.getValue().equals(end) && stack.size() > 1) {
                if(stack.size() == 2 && start.equals(end)) { // 自环
                    cur_node = stack.removeLast();
                states.put(cur_node.getValue(), 0);
                continue;
            }
            //形成路径
            Path path = genPath(stack);
            if(paths.contains(path)) {
                System.out.println("--------------------------");

            }
                paths.add(path);
                System.out.println("res        " + (++i));
                cur_node = stack.removeLast();
                states.put(cur_node.getValue(), 0);
                continue;
            }
            else {
                List<Pair<OntProperty, OntClass>> next_nodes = getRelations(ontModel, top_node.getValue());
                int flag = -1;
                next_node = null;
                for(Pair<OntProperty, OntClass> m : next_nodes) {
                    if(cur_node == null) {  //新节点 从头遍历
                        if(states.get(m.getValue()) > 0) { // 已经遍历过
                            continue;
                        }
                        next_node = m;
                        break;
                    }
                    if(flag == -1 && m.getValue().equals(cur_node.getValue())) { // 找到了上一次遍历的节点
                        if(m.getKey() == null  || m.getKey().equals(cur_node.getKey())) { //且路径相同
                            flag = 1;
                        }
                        continue;
                    }

                    if(flag == -1) { // 未找到上次遍历到的节点
                        continue;
                    }
                    if(states.get(m.getValue()) > 0) { // 如果已经遍历过
                        continue;
                    }
                    else {
                        next_node = m;
                        break;
                    }
                }
                if (next_node != null) {
//                    if(next_node.getValue().equals(end)) {
//                        System.out.println("ok");
//                    }
                    stack.add(next_node);
                    //states.get(next_node.getValue()) + 1
                    states.put(next_node.getValue(), 1);
                    cur_node = null; //新节点入栈，要从头遍历它的所有临接节点
                }
                else {//遍历完了
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
    public static Integer getDeep(OntClass ontClass) {
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
    public static List<OntClass> addList(OntModel ontModel, List<Object> list) {
        List<OntClass> res_lists = new ArrayList<>();
        int deep = -1;
        String deepClassName = null;
        //创建所有节点，存在则获取,并找到最深的节点
        String umlName = null;
        for(Object o: list) {
            String ClassName = o.toString();
            OntClass newClass = createClass(ontModel, ClassName);
            int tmpDeep = getDeep(newClass);
            if(tmpDeep > deep || (deep == tmpDeep && ClassName.contains("uml"))) { // 找到最深的本体
                deepClassName = ClassName;
                deep = tmpDeep;
            }
            if (ClassName.contains("uml")) {
                umlName = ClassName;
            }
        }
        assert deepClassName != null;
        if(deepClassName.contains("uml")) { // 只有一层
            OntClass fatherClass = getClass(ontModel, deepClassName);
            if(list.size() == 1) {
                res_lists.add(fatherClass);
                return res_lists;
            }
            for(Object o: list) {
                String ClassName = o.toString();
                if(ClassName.contains("uml")) continue;
                OntClass sonClass = getClass(ontModel, ClassName);
                res_lists.add(sonClass);
                addSubClass(fatherClass, sonClass);
            }
        }
        else {
            Map<OntClass, Integer> dirt = new HashMap<>();
            OntClass tmpClass = getClass(ontModel, deepClassName);
            while(tmpClass.hasSuperClass()) {
                tmpClass = tmpClass.getSuperClass();
                dirt.put(tmpClass, 1);
            }
            OntClass umlClass = getClass(ontModel, umlName);
            for(Object o : list) {
                String ClassName = o.toString();
                OntClass sonClass = getClass(ontModel, ClassName);
                if(dirt.containsKey(sonClass)) {
                    continue;
                }
                if(sonClass.getURI().equals(deepClassName)) { //最深节点
                    res_lists.add(sonClass);
                    continue;
                }
                tmpClass = getClass(ontModel, ClassName);
                while(tmpClass.hasSuperClass()) {//存在上级则得到最上级
                    dirt.put(tmpClass, 1);
                    tmpClass = tmpClass.getSuperClass();
                }
                if(tmpClass.equals(umlClass)) { //如果上级一致 但和最长链不属于同一分支 不处理
                    res_lists.add(sonClass);
                    continue;
                }
                else if(tmpClass.equals(sonClass)){ //上级不一致，但无上级
                    res_lists.add(sonClass);
                    addSubClass(umlClass, sonClass);
                }
                else { //上级不一致且存在上级
//                    res_lists.add(sonClass);
                    continue;
                }
            }
        }
        return res_lists;
    }



    /*
     * @Description: 向传入的兄弟类和子类添加父子关系
     * @Author: shk001
     * @param: [ontModel 读取OWL文件生成的OntModel类对象, BroClass 兄弟类, sonClass 子类]
     * @return: void
     **/
    public static void addSiblingClass(OntClass BroClass, OntClass sonClass) {
        if(BroClass.equals(sonClass)) return;
        OntClass fatherClass = BroClass.getSuperClass();
        fatherClass.addSubClass(sonClass);
    }

    /*
     * @Description: 向传入的头类和尾类之间添加关系，关系名称由参数传递
     * @param: [ontModel 读取OWL文件生成的OntModel类对象, sourceClass 头类, targetClass 尾类, relationName 关系名称]
     * @return: org.apache.jena.ontology.ObjectProperty
     **/
    public static void addRelation(OntModel ontModel, OntClass sourceClass, OntClass targetClass, String relationName)  {
        String nameSpace = CoreOWLUtil.getNameSpace();
        ObjectProperty fatherRelation = ontModel.createObjectProperty(nameSpace + relationName);
        // todo 加边？ 图怎么存？

        // todo 扩展到子类?
//        int number = getPropertyNumber(fatherRelation, sourceClass, targetClass);
//        if(number == -1) {// 已存在
//            return ;
//        }
        //todo 2024/7/16
//        OntProperty newRelation = ontModel.createOntProperty(nameSpace + relationName + "-" + number);
//        fatherRelation.addDomain(sourceClass);
//        newRelation.addDomain(sourceClass);
//        newRelation.addRange(targetClass);
//        fatherRelation.addSubProperty(newRelation);
//        OntProperty top = ontModel.getObjectProperty("http://www.w3.org/2002/07/owl#topObjectProperty");
//        top.addSubProperty(fatherRelation);
        return ;
    }


    /*
     * @Description: 获得与sourceClass相连的所有Property
     * @Author: shk001
     * @param: [ontModel 读取OWL文件生成的OntModel类对象, sourceClass 头类]
     * @return: org.apache.jena.ontology.ObjectProperty
     **/
    public static List<Pair<OntProperty, OntClass>> getRelations(OntModel ontModel, OntClass sourceClass)  {

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

        /*

         */

        Iterator<OntProperty> properties = ontModel.listOntProperties();
        while (properties.hasNext()) {
            OntProperty property =  properties.next();
            Iterator<? extends OntResource> domains = property.listDomain();
            while (domains.hasNext()) {
                OntClass domain = domains.next().asClass();
                if(!(domain.getURI().compareTo(sourceClass.getURI()) == 0)) {
                    continue;
                }

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
//        System.out.println(result.size());
//        System.out.println("-----------------");
        return  result;
    }

    /*
     * @Description: 向传入的类添加属性信息，属性名称由参数传递
     * @Author: zt
     * @Date: 2023/2/28 17:23
     * @param: [ontModel 读取OWL文件生成的OntModel类对象, ontClass 需要被添加的类别, propertyName 属性名称]
     * @return: org.apache.jena.ontology.DatatypeProperty
     **/
    public static void addProperty(OntModel ontModel, String propertyName) {
        String nameSpace = CoreOWLUtil.getNameSpace();
        OntProperty newProperty = ontModel.createOntProperty(nameSpace + propertyName);
        return;
    }

    /*
     * @Description: 删除指定的类中的属性信息
     * @Author: zt
     * @Date: 2023/3/27 15:39
     * @param: [ontModel, ontClass, propertyName]
     * @return: void
     **/
    public static void removeProperty(OntModel ontModel, OntClass ontClass, String propertyName) {
        String nameSpace = CoreOWLUtil.getNameSpace();
        DatatypeProperty newProperty = ontModel.createDatatypeProperty(nameSpace + propertyName);
        newProperty.removeDomain(ontClass);
    }

    /*
     * @Description: 删除指定名称的类，如果这个类有子类的话，就不能删除，直接抛出异常
     * @Author: zt
     * @Date: 2023/2/28 18:18
     * @param: [ontModel 读取OWL文件生成的OntModel类对象, classname 需要删除的类的名称]
     * @return: void
     **/
    public static void removeClass(OntModel ontModel, String classname) {
        OntClass ontClass = CoreOWLUtil.createClass(ontModel, classname);
        ontClass.remove();
    }

    /*
     * @Description: 删除指定名称的关系中的指定的domain和range
     * @Author: zt
     * @Date: 2023/3/7 10:42
     * @param: [ontModel, propertyName 关系的名称, ontClass 要从这个关系中移除的类]
     * @return: void
     **/
    public static void removeRelationDomainAndRange(OntModel ontModel, String propertyName, OntClass ontClass) {
        String nameSpace = CoreOWLUtil.getNameSpace();
        ObjectProperty relation = ontModel.createObjectProperty(nameSpace + propertyName);
        //只要这个类是这个关系的range或者domain，就进行删除
        if(relation.hasDomain(ontClass)){
            relation.removeDomain(ontClass);
        }
        if(relation.hasRange(ontClass)){
            relation.removeRange(ontClass);
        }
    }

    public static void removeRelation(OntModel ontModel, String propertyName, OntClass domain, OntClass range) {
        String nameSpace = CoreOWLUtil.getNameSpace();
        ObjectProperty relation = ontModel.createObjectProperty(nameSpace + propertyName);
        if(relation.hasDomain(domain)){
            relation.removeDomain(domain);
        }
        if(relation.hasRange(range)){
            relation.removeRange(range);
        }
    }

}