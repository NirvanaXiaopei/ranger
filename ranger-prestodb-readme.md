# Ranger 适配 prestodb 经验总结
## 1 思路
首先参考网上博客 https://www.jianshu.com/p/888186c38827 得到的结论是ranger可以适配prestosql，但是不能适配prestodb，于是转向两者的差异。从github上的时间线来看，  

	presto 在2019.1.21 从facebook分家  
	使用 prestosql 的名称从2019.1.22 开始 到 2021.1.2结束   
	从 2021.1.3开始 变更名称为trino
	ranger2.0 依赖的presto-sql是310版本，即发布日期2019.5.4
	由于prestodb没有stable的说法，以最新的prestodb0.270为例，与prestosql310对比版本中的差异。
到目前为止，就有了ranger中presto插件， 依赖的presto310，与prestodb0.270这三个模块。  

短期来看，prestodb0.270与prestosql310差异较小，插件的改动量较小，理论上修改代码就能通过；prestosql310的命名后续不在存在，并且与trino的差异较大，因此在ranger2.0.0版本中，使用prestodb代价较小，并且带来的好处是presto不用修改，只需修改ranger中的模块就可以。  

长远来看，ranger2.0.0依赖prestosql333版本，插件模块的演进向trino方向靠拢，并且需要较为复杂的改动，后续如果要切换为trino分支，倒是可以依赖ranger社区的发展。

## 2 代码修改
2.1 修改pom.xml依赖  
总的pom，ranger-presto-plugin-shim 和 ranger-presto-plugin两个模块的module。  

2.2 java文件引入的包  
统一修改为facebook的包名，例如  

	import com.facebook.presto.common.CatalogSchemaName;
	import com.facebook.presto.spi.CatalogSchemaTableName;
	import com.facebook.presto.spi.SchemaTableName;
	import com.facebook.presto.spi.security.SystemAccessControl;
	import com.facebook.presto.spi.security.SystemAccessControlFactory;
2.3 修改java代码  
ranger-presto-plugin-shim 和 ranger-presto-plugin两个模块的下的RangerSystemAccessControl.java  
修改方式也很简单，将 AccessControlContext 对象占的位置全部替换为null。  

2.4 其他带有io.prestosql的文件  
修改rats.txt，web中的一些配置
agents-common/src/main/resources/service-defs/ranger-servicedef-presto.json
src/main/assembly/admin-web.xml  
mv ranger-presto-plugin-shim/src/main/resources/META-INF/services/io.prestosql.spi.Plugin ranger-presto-plugin-shim/src/main/resources/META-INF/services/com.facebook.presto.spi.Plugin

2.5 如果后续还要变化的
这两个jar包应该可以直接替换了  
(admin web) /opt/ranger/ranger-2.0.0-admin/ews/webapp/WEB-INF/classes/ranger-plugins/presto/ranger-presto-plugin-2.0.0.jar  
(client 生成插件使用) /opt/ranger/ranger-2.0.0-presto-plugin/lib/ranger-presto-plugin-shim-2.0.0.jar

## 3 本地编译
编译ranger：  
到github下载ranger-2.0.0分支，修改代码，执行maven编译  
mvn -DskipTests=true clean compile package install assembly:assembly  


注：ant -f release-build.xml -Dranger-release-version=2.0.0  // 生成ranger-2.0.0的压缩包，可用于发布
## 4 安装测试
需要依赖java1.8+，mysql，mysql-connector-java.jar（提前放置到/usr/share/java/）

	tar -zxvf ranger-2.1.0-SNAPSHOT-admin.tar.gz 
	cd /opt/app/ranger-2.1.0-SNAPSHOT-admin/
	vi install.properties
	    db_root_user=root
	    db_root_password=mydbml_Guoxiaopei_123456
	    db_host=10.13.171.158
	    
	    db_name=db_prestodb
	    db_user=user_prestodb
	    db_password=passwd_prestodb_123
	    
	    #禁用审计功能
	    #audit_store=solr
	
	mysql命令行中执行：
		CREATE DATABASE `db_prestodb` CHARACTER SET utf8 COLLATE utf8_general_ci;
   		CREATE USER 'user_prestodb'@'%' IDENTIFIED BY 'passwd_prestodb_123';
		GRANT ALL ON db_prestodb.* TO 'user_prestodb'@'%';
启动Ranger Admin服务		
http://10.30.1.25:6080  默认密码admin/admin  

	解压插件，cd /opt/ranger/ranger-2.0.0-presto-plugin && vi install.properties
		POLICY_MGR_URL=http://10.30.1.25:6080
		REPOSITORY_NAME=prestodbdev
		COMPONENT_INSTALL_DIR_NAME=/data2/presto/presto-server-0.270
		XAAUDIT.SUMMARY.ENABLE=false
		sh ./enable-presto-plugin.sh
	为了保险起见，检查下配置：
	presto配置文件目录etc是否生成access-control.properties
	$PRESTO_HOME/plugin/ranger/ 是否有软连接生成
	$PRESTO_HOME/plugin/ranger/ranger-presto-plugin-impl/conf/中配置文件是否与/opt/app/presto-server-317/etc/中一致

Ranger访问策略本地缓存目录 /etc/ranger/, 目录权限修改为presto启动用户  

到 $PRESTO_HOME 重启presto服务  

	bin/launcher stop
	bin/launcher start
如果有问题，可查看presto server启动日志进行排查。

页面配置presto连接：

	Service Name : prestodbdev （与前面插件中的 REPOSITORY_NAME 一致）
	username: xiaopei
	password: ***empty***
	com.facebook.presto.jdbc.PrestoDriver
	jdbc:presto://10.13.171.158:8899/catalog
测试链接，查看能否成功。

测试效果  
没有指定用户：  

	./presto --server 10.30.1.25:8899 --catalog hive --schema default
	presto:default> show tables;  
	Query 20220218_051905_00002_4s82h failed: Access Denied: Cannot access catalog hive  
指定用户：  

	./presto --server 10.30.1.25:8899 --catalog hive --schema default --user xiaopei  
	presto:default> show tables;  
	      Table
	------------------
	 abc
	 fsimage_analysis
	 gxp
	 table_a
	 table_b
	 wc
	(6 rows)
	
	Query 20220218_051928_00005_4s82h, FINISHED, 1 node
	Splits: 19 total, 19 done (100.00%)
	249ms [6 rows, 140B] [24 rows/s, 561B/s]


## 5 中间踩的坑
5.1 mysql设置问题  
踩坑mysql报错MySQLSyntaxErrorException: Specified key was too long; max key length is 767 bytes  

	SET GLOBAL innodb_large_prefix = ON;
	SET GLOBAL innodb_file_format=Barracuda;
	SET GLOBAL innodb_file_per_table = ON;

5.2 本地连接mysql报权限问题，需要开启root的访问权限：  

	GRANT ALL PRIVILEGES ON *.* TO 'root'@'10.30.1.25' IDENTIFIED BY 'mydbml_Guoxiaopei_123456' WITH GRANT OPTION;
	FLUSH PRIVILEGES;

5.3 执行ranger admin安装时报错：  
python版本问题，由于之前安装过anaconda3，因此默认的python指向python3的版本，解决方法  

	到anaconda的安装目录，删除 python 软链接即可。 
5.4 同一台机器测试两个不同版本的ranger admin，发现登陆有问题，发现时软链接的问题  

	whereis ranger-admin
	ranger-admin: /usr/bin/ranger-admin
	ll /usr/bin/ranger-admin
	/usr/bin/ranger-admin -> /opt/ranger-prestosql/ranger-2.0.0-admin/ews/ranger-admin-services.sh
	删掉软连接重新 执行 setup.sh
5.5 梳理清楚目录关系，否则很容易绕迷糊  

	ranger /data0/software/ 编译目录
	ranger /opt/ranger2.0.0-xxx 服务安装目录
	presto /data0/presto  presto两个版本的目录（0.270从网上下载，310从trino编译）
	组合测试
	ranger2.0.0（sql版） + presto-sql-310 可以跑得通
	ranger2.0.0（修改的db版）+ prestodb-0.270 

