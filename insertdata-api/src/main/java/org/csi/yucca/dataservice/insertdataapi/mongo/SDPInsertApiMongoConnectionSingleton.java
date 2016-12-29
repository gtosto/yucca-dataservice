package org.csi.yucca.dataservice.insertdataapi.mongo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.csi.yucca.dataservice.insertdataapi.model.output.CollectionConfDto;
import org.csi.yucca.dataservice.insertdataapi.util.SDPInsertApiConfig;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class SDPInsertApiMongoConnectionSingleton {
	public static final String MONGO_DB_CFG_TENANT="MONGO_DB_CFG_TENANT";
	public static final String MONGO_DB_CFG_METADATA="MONGO_DB_CFG_METADATA";
	public static final String MONGO_DB_CFG_STREAM="MONGO_DB_CFG_STREAM";
	public static final String MONGO_DB_CFG_APPOGGIO="MONGO_DB_CFG_APPOGGIO";
	public static final String MONGO_DB_CFG_STATUS="MONGO_DB_CFG_STATUS";


	public static final String DB_MESURES="DBMEASURES";
	public static final String DB_DATA="DBDATA";
	public static final String DB_MESURES_TRASH="DBMEASURES_TRASH";
	public static final String DB_DATA_TRASH="DBDATA_TRASH";
	
	
	public static SDPInsertApiMongoConnectionSingleton instance=null;

	private static final Log log=LogFactory.getLog("org.csi.yucca.datainsert");
	
	private static Map<String, CollectionConfDto> params =   Collections.synchronizedMap(new PassiveExpiringMap<String, CollectionConfDto>(10,TimeUnit.MINUTES));  // new HashMap<String, CollectionConfDto>();
	private static HashMap<String, MongoClient> mongoConnection = new HashMap<String, MongoClient>();
	//private static HashMap<String, MongoClient> mongoTenantConnection = new HashMap<String, MongoClient>();

	public synchronized static SDPInsertApiMongoConnectionSingleton getInstance() throws Exception{
		//if(instance == null || singletonToRefresh()) {
		if(instance == null) {
			//if (instance!=null) instance.cleanMongoConnection(); 
			instance = new SDPInsertApiMongoConnectionSingleton();
		}
		return instance;
	}
	private String takeNvlValues(Object obj) {
		if (null==obj) return null;
		else return obj.toString();
	}
	
	public void cleanMongoConnection() {
		if (mongoConnection!=null && mongoConnection.size()>0) {
			Iterator<String> chiavi=mongoConnection.keySet().iterator();
			while (chiavi.hasNext()) {
				String chiave=chiavi.next();
				mongoConnection.get(chiave).close();
			}
			
		}
	}
	
	private SDPInsertApiMongoConnectionSingleton() throws Exception{
		DBCursor cursor=null;
		try {
			mongoConnection = new HashMap<String, MongoClient>();
			params = Collections.synchronizedMap(new PassiveExpiringMap<String, CollectionConfDto>(10,TimeUnit.MINUTES)); 
			//STREAM
			String host=SDPInsertApiConfig.getInstance().getMongoCfgHost(SDPInsertApiConfig.MONGO_DB_CFG_STREAM);
			int port=SDPInsertApiConfig.getInstance().getMongoCfgPort(SDPInsertApiConfig.MONGO_DB_CFG_STREAM);
			
			
			mongoConnection.put(MONGO_DB_CFG_STREAM, getMongoClient(host, port));

			host=SDPInsertApiConfig.getInstance().getMongoCfgHost(SDPInsertApiConfig.MONGO_DB_CFG_METADATA);
			port=SDPInsertApiConfig.getInstance().getMongoCfgPort(SDPInsertApiConfig.MONGO_DB_CFG_METADATA);
			mongoConnection.put(MONGO_DB_CFG_METADATA, getMongoClient(host, port));

			host=SDPInsertApiConfig.getInstance().getMongoCfgHost(SDPInsertApiConfig.MONGO_DB_CFG_APPOGGIO);
			port=SDPInsertApiConfig.getInstance().getMongoCfgPort(SDPInsertApiConfig.MONGO_DB_CFG_APPOGGIO);
			mongoConnection.put(MONGO_DB_CFG_APPOGGIO, getMongoClient(host, port));


			host=SDPInsertApiConfig.getInstance().getMongoCfgHost(SDPInsertApiConfig.MONGO_DB_CFG_STATUS);
			port=SDPInsertApiConfig.getInstance().getMongoCfgPort(SDPInsertApiConfig.MONGO_DB_CFG_STATUS);
			mongoConnection.put(MONGO_DB_CFG_STATUS, getMongoClient(host, port));

			host=SDPInsertApiConfig.getInstance().getMongoCfgHost(SDPInsertApiConfig.MONGO_DB_CFG_TENANT);
			port=SDPInsertApiConfig.getInstance().getMongoCfgPort(SDPInsertApiConfig.MONGO_DB_CFG_TENANT);
			mongoConnection.put(MONGO_DB_CFG_TENANT, getMongoClient(host, port));


		} catch (Exception e) {
			//TODO log
			e.printStackTrace();
		}finally {
			try { cursor.close(); } catch (Exception ec) {}
		}
	}

	public CollectionConfDto getDataDbConfiguration(String tenantCode) throws Exception {
		if (null==params.get(tenantCode)) reloadDataDbConfig();
		return params.get(tenantCode);
	}


	private void reloadDataDbConfig() throws Exception {
		log.info("Reloading tenant configuration....");
		DBCursor cursor =null;
		try {
			MongoClient mongoClient =getMongoClient(SDPInsertApiMongoConnectionSingleton.MONGO_DB_CFG_TENANT);	
			DB db = mongoClient.getDB(SDPInsertApiConfig.getInstance().getMongoCfgDB(SDPInsertApiConfig.MONGO_DB_CFG_TENANT));
			String collection=SDPInsertApiConfig.getInstance().getMongoCfgCollection(SDPInsertApiConfig.MONGO_DB_CFG_TENANT);
			DBCollection coll = db.getCollection(collection);

			cursor = coll.find();
			while (cursor.hasNext()) {

				DBObject obj=cursor.next();

				String tenant=obj.get("tenantCode").toString();
				
				CollectionConfDto collectionConf=new CollectionConfDto();
				
				collectionConf.setSocialSolrCollectionName( takeNvlValues(obj.get("socialSolrCollectionName")));
				collectionConf.setMeasuresSolrCollectionName( takeNvlValues(obj.get("measuresSolrCollectionName")));
				collectionConf.setDataSolrCollectionName( takeNvlValues(obj.get("dataSolrCollectionName")));
				collectionConf.setMediaSolrCollectionName( takeNvlValues(obj.get("mediaSolrCollectionName")));
				
				collectionConf.setSocialPhoenixSchemaName( takeNvlValues(obj.get("socialPhoenixSchemaName")));
				collectionConf.setDataPhoenixSchemaName( takeNvlValues(obj.get("dataPhoenixSchemaName")));
				collectionConf.setMediaPhoenixSchemaName( takeNvlValues(obj.get("mediaPhoenixSchemaName")));
				collectionConf.setMeasuresPhoenixSchemaName(takeNvlValues(obj.get("measuresPhoenixSchemaName")));
				
				collectionConf.setSocialPhoenixTableName(takeNvlValues(obj.get("socialPhoenixTableName")));
				collectionConf.setDataPhoenixTableName( takeNvlValues(obj.get("dataPhoenixTableName")));
				collectionConf.setMediaPhoenixTableName( takeNvlValues(obj.get("mediaPhoenixTableName")));
				collectionConf.setMeasuresPhoenixTableName( takeNvlValues(obj.get("measuresPhoenixTableName")));
				
				params.put(tenant, collectionConf);
				
			}
		} catch (Exception e) {
			log.error("Error",e);
			throw e;
		} finally {
			try { cursor.close(); } catch (Exception ec) {}

		}
		
	}

	public MongoClient getMongoClient(String databaseType) throws Exception{
		if (MONGO_DB_CFG_TENANT.equals(databaseType)) return mongoConnection.get(MONGO_DB_CFG_TENANT);
		if (MONGO_DB_CFG_METADATA.equals(databaseType)) return mongoConnection.get(MONGO_DB_CFG_METADATA);
		if (MONGO_DB_CFG_STATUS.equals(databaseType)) return mongoConnection.get(MONGO_DB_CFG_STATUS);
		if (MONGO_DB_CFG_APPOGGIO.equals(databaseType)) return mongoConnection.get(MONGO_DB_CFG_APPOGGIO);
		if (MONGO_DB_CFG_STREAM.equals(databaseType)) return mongoConnection.get(MONGO_DB_CFG_STREAM);
		throw new  Exception ("invalid mongo db or configuration error");
	}


	private MongoClient getMongoClientLocal(String host, int port) throws Exception{
		StringTokenizer st= new StringTokenizer(host,";",false);
		ArrayList<ServerAddress> arrServerAddr=new ArrayList<ServerAddress>();
		while (st.hasMoreTokens()) {
			String newHost=st.nextToken();
			ServerAddress serverAddr=new ServerAddress(newHost,port);
			arrServerAddr.add(serverAddr);
		}
		MongoCredential credential = MongoCredential.createMongoCRCredential(SDPInsertApiConfig.getInstance().getMongoDefaultUser(), 
				"admin", 
				SDPInsertApiConfig.getInstance().getMongoDefaultPassword().toCharArray());
		MongoClient mongoClient = null;
		mongoClient = new MongoClient(arrServerAddr,Arrays.asList(credential));
		return mongoClient;


	}


	public MongoClient getMongoClient(String host, int port) throws Exception{
		MongoClient ret=mongoConnection.get(host+"___"+port);
		if (ret!=null) return ret;
		ret=  getMongoClientLocal(host, port);
		
		mongoConnection.put(host+"___"+port, ret);
		return ret;
		
		
	}	
}