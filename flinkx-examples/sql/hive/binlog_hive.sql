CREATE TABLE source
(
    id             bigint,
    col_bit        boolean,
    col_tinyint    tinyint,
    col_smallint   smallint,
    col_int        int,
    col_bigint     bigint,
    col_float      float,
    col_double     double,
    col_decimal    decimal(10, 0),
    col_string     char(10),
    col_varchar    varchar(10),
    col_char       char(10),
    col_binary     binary,
    col_timestamp  timestamp,
    col_date       date
) WITH (
      'connector' = 'binlog-x'
      ,'username' = 'root'
      ,'password' = 'root'
      ,'cat' = 'insert'
      ,'jdbcUrl' = 'jdbc:mysql://ip:3308/tudou?useSSL=false'
      ,'host' = 'ip'
      ,'port' = '3308'
      ,'table' = 'tudou.kudu'
      ,'timestamp-format.standard' = 'SQL'
      );

CREATE TABLE sink
(
    id              bigint,
    col_boolean     boolean,
    col_tinyint     tinyint,
    col_smallint    smallint,
    col_int         int,
    col_bigint      bigint,
    col_float       float,
    col_double      double,
    col_decimal     decimal(10, 0),
    col_string      char(10),
    col_varchar     varchar(10),
    col_char        char(10),
    col_binary      binary,
    col_timestamp   timestamp,
    col_date        date
) WITH (
      'connector' = 'hive-x'
      ,'properties.hadoop.user.name' = 'root'
      ,'properties.dfs.ha.namenodes.ns' = 'nn1,nn2'
      ,'properties.fs.defaultFS' = 'hdfs://ns'
      ,'properties.dfs.namenode.rpc-address.ns.nn2' = 'ip:9000'
      ,'properties.dfs.client.failover.proxy.provider.ns' = 'org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider'
      ,'properties.dfs.namenode.rpc-address.ns.nn1' = 'ip:9000'
      ,'properties.dfs.nameservices' = 'ns'
      ,'properties.fs.hdfs.impl.disable.cache' = 'true'
      ,'properties.fs.hdfs.impl' = 'org.apache.hadoop.hdfs.DistributedFileSystem'
      ,'defaultFS' = 'hdfs://ns'
      ,'fieldDelimiter' = ','
      ,'encoding' = 'utf-8'
      ,'maxFileSize' = '10485760'
      ,'nextCheckRows' = '20000'
      ,'writeMode' = 'overwrite'
      ,'fileType' = 'parquet'

      ,'jdbcUrl' = 'jdbc:hive2://ip:10000/tudou'
      ,'username' = ''
      ,'password' = ''
      ,'partition' = 'pt'
      ,'partitionType' = 'DAY'
      ,'tableName' = 'kudu'
      );

insert into sink
select *
from source u;