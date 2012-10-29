package com.clojurewerkz.cascading.mongodb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cascading.tuple.FieldsResolverException;
import cascading.tuple.TupleEntry;
import com.mongodb.MongoURI;
import com.mongodb.hadoop.mapred.MongoOutputFormat;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.fs.Path;

import org.apache.hadoop.util.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cascading.flow.FlowProcess;
import cascading.scheme.Scheme;
import cascading.scheme.SinkCall;
import cascading.scheme.SourceCall;
import cascading.tap.Tap;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import com.mongodb.BasicDBObject;
import com.mongodb.hadoop.io.BSONWritable;
import com.mongodb.hadoop.mapred.MongoInputFormat;
import com.mongodb.hadoop.util.MongoConfigUtil;

@SuppressWarnings("rawtypes")
public class MongoDBScheme extends Scheme<JobConf, RecordReader, OutputCollector, BSONWritable[], BSONWritable[]> {

  /**
   * Field logger
   */
  private static final Logger logger = LoggerFactory.getLogger(MongoDbCollector.class);

  private String pathUUID;
  public String mongoUri;
  public List<String> columnFieldNames;
  public Map<String, String> fieldMappings;
  public String keyColumnName;

  private String host;
  private Integer port;
  private String database;
  private String collection;

  public MongoDBScheme(String host, Integer port, String database, String collection, List<String> columnFieldNames, Map<String, String> fieldMappings) {
    this(host, port, database, collection, "_id", columnFieldNames, fieldMappings);
  }

  public MongoDBScheme(String host, Integer port, String database, String collection, String keyColumnName, List<String> columnFieldNames, Map<String, String> fieldMappings) {
    this.mongoUri = String.format("mongodb://%s:%d/%s.%s", host, port, database, collection);
    this.pathUUID = UUID.randomUUID().toString();
    this.columnFieldNames = columnFieldNames;
    this.fieldMappings = fieldMappings;
    this.keyColumnName = keyColumnName;

    this.host = host;
    this.port = port;
    this.database = database;
    this.collection = collection;
  }

  public MongoDBScheme(String host, Integer port, String username, String password, String database, String collection, String keyColumnName, List<String> columnFieldNames, Map<String, String> fieldMappings) {
    this.mongoUri = String.format("mongodb://%s:%s@%s:%d/%s.%s", username, password, host, port, database, collection);
    this.pathUUID = UUID.randomUUID().toString();
    this.columnFieldNames = columnFieldNames;
    this.fieldMappings = fieldMappings;
    this.keyColumnName = keyColumnName;

    this.host = host;
    this.port = port;
    this.database = database;
    this.collection = collection;
  }

  /**
   *
   * @return
   */
  public Path getPath() {
    return new Path(pathUUID);
  }

  /**
   *
   * @return
   */
  public String getIdentifier() {
    return String.format("%s_%d_%s_%s", this.host, this.port, this.database, this.collection);
  }

  /**
   *
   * @param process
   * @param tap
   * @param conf
   */
  @Override
  public void sourceConfInit(FlowProcess<JobConf> process, Tap<JobConf, RecordReader, OutputCollector> tap, JobConf conf) {
    MongoConfigUtil.setReadSplitsFromShards(conf, true);
    MongoConfigUtil.setInputURI( conf, new MongoURI(this.mongoUri) );
    FileInputFormat.setInputPaths(conf, this.getIdentifier());
    conf.setInputFormat(MongoInputFormat.class);

    // TODO: MongoConfigUtil.setFields(conf, fieldsBson);
    // TODO: MongoConfigUtil.setQuery(conf, q);
    // TODO: MongoConfigUtil.setFields(conf, fields);
  }

  /**
   *
   * @param process
   * @param tap
   * @param conf
   */
  @Override
  public void sinkConfInit(FlowProcess<JobConf> process, Tap<JobConf, RecordReader, OutputCollector> tap,
                           JobConf conf) {
    conf.setOutputFormat(MongoOutputFormat.class);
    MongoConfigUtil.setOutputURI(conf, this.mongoUri);

    FileOutputFormat.setOutputPath(conf, getPath());
  }

  /**
   *
   * @param flowProcess
   * @param sourceCall
   */
  @Override
  public void sourcePrepare(FlowProcess<JobConf> flowProcess, SourceCall<BSONWritable[], RecordReader> sourceCall) {
    sourceCall.setContext(new BSONWritable[2]);

    sourceCall.getContext()[0] = (BSONWritable) sourceCall.getInput().createKey();
    sourceCall.getContext()[1] = (BSONWritable) sourceCall.getInput().createValue();
  }

  /**
   *
   * @param flowProcess
   * @param sourceCall
   * @return
   * @throws IOException
   */
  @Override
  public boolean source(FlowProcess<JobConf> flowProcess, SourceCall<BSONWritable[], RecordReader> sourceCall) throws IOException {
    Tuple result = new Tuple();

    BSONWritable key = sourceCall.getContext()[0];
    BSONWritable value = sourceCall.getContext()[1];

    if (!sourceCall.getInput().next(key, value)) {
      logger.info("Nothing left to read, exiting");
      return false;
    }

    for (String columnFieldName : columnFieldNames) {
      Object tupleEntry= value.get(columnFieldName);
      if (tupleEntry != null) {
        result.add(tupleEntry);
      } else if (columnFieldName != this.keyColumnName) {
        result.add("");
      }
    }

    sourceCall.getIncomingEntry().setTuple(result);
    return true;
  }

  /**
   *
   * @param flowProcess
   * @param sinkCall
   * @throws IOException
   */
  @Override
  public void sink(FlowProcess<JobConf> flowProcess, SinkCall<BSONWritable[], OutputCollector> sinkCall) throws IOException {
    TupleEntry tupleEntry = sinkCall.getOutgoingEntry();
    OutputCollector outputCollector = sinkCall.getOutput();

    Object key = tupleEntry.selectTuple(new Fields(this.fieldMappings.get(this.keyColumnName))).get(0);

    BasicDBObject dbObject = new BasicDBObject();

    for (String columnFieldName : columnFieldNames) {
      String columnFieldMapping = fieldMappings.get(columnFieldName);
      Object tupleEntryValue = null;

      try {
        tupleEntryValue = tupleEntry.get(columnFieldMapping);
      } catch(FieldsResolverException e) {
        logger.error("Couldn't resolve field: {}", columnFieldName);
      }

      if(tupleEntryValue != null && columnFieldName != keyColumnName) {
        logger.info("Putting for output: {} {}", columnFieldName, tupleEntryValue);
        dbObject.put(columnFieldName, tupleEntryValue);
      }
    }
    logger.info("Putting key for output: {} {}", key, dbObject);
    outputCollector.collect(new ObjectId(), dbObject);
  }

}
