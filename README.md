在数据库存在的情况下依次运行src\main\java\work下的
Initialization （初始化本体、添加SysML的基础本体之间的关系，需要存在空白的rdf（1.rdf）和infor.txt(存储着SysML的基础本体之间的关系），输出为data/Initialization.rdf）

test_addOntologyAndRelation（添加点和边的本体到protege中，输入为data/Initialization.rdf以及知识图谱数据库，输出为data/output.rdf）

test_add_individuals（添加知识图谱中的实例到protege中，输入为data/output.rdf和知识图谱数据库，输出为data/output_individual.rdf）

test_find_roads（通过固定头尾实例的标签，查询两个本体之间可能存在的路径及其规则，现已转移到sysml/learn中）

test_infer (通过protege来进行推理，需要手动更改其中的ruleString字段，程序会将推理后的结果返回到知识图谱数据库中)