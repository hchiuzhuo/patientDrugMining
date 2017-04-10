libpath="./lib/commons-csv-1.0.jar:"
libpath=$libpath"./lib/Jama-1.0.3.jar:"
libpath=$libpath"./lib/datanucleus-api-jdo-3.2.1.jar:"
libpath=$libpath"./lib/la4j-0.4.9.jar:"
libpath=$libpath"./lib/datanucleus-core-3.2.2.jar:"
libpath=$libpath"./lib/postgresql-9.3-1102.jdbc41.jar:"
libpath=$libpath"./lib/datanucleus-rdbms-3.2.1.jar:"
libpath=$libpath"./lib/spmf.jar:"
libpath=$libpath"./lib/dom4j-2.0.0-ALPHA-2.jar:"
libpath=$libpath"./lib/log4j-1.2.17.jar:"
libpath=$libpath"./lib/spark-assembly-1.0.1-hadoop1.0.4.jar:"
echo $libpath
java -Xms4056M  -cp $libpath./target/demo-0.0.2-SNAPSHOT.jar org.imeds.daemon.imedsDaemon
