package it.csi.smartdata.odata.datadiscovery;

import it.csi.smartdata.odata.dbconfig.ConfigParamsSingleton;
import it.csi.smartdata.odata.dbconfig.MongoSingleton;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class MongoDbStore {
	MongoClient mongoClient;
	Map<String,String> mongoParams = null;

	Integer MAX_RECORDS = 50;
	//	MongoCredential credential=null;
	private Logger logger = Logger.getLogger(MongoDbStore.class);
	public MongoDbStore(){
		mongoParams = ConfigParamsSingleton.getInstance().getParams();
		try {
			mongoClient = MongoSingleton.getMongoClient();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Map<String, Object> getDataset(Long idDataset) {

		Map<String,Object> ret = null;

		DB db = mongoClient.getDB(mongoParams.get("MONGO_DB_META"));
		DBCollection colldataset = db.getCollection(mongoParams.get("MONGO_COLLECTION_DATASET"));

		DBCollection collapi = db.getCollection(mongoParams.get("MONGO_COLLECTION_API"));
		DBCollection collstream = db.getCollection(mongoParams.get("MONGO_COLLECTION_STREAM"));

		DBObject searchById = new BasicDBObject("idDataset", idDataset);
		searchById.put("configData.current", 1);
		DBObject found = colldataset.findOne(searchById);

		if (found != null) {
			Map<String, Object> cur = extractOdataPropertyFromMongo(collapi,
					collstream, found);

			ret=cur;
		}

		return ret;
	}

	public List<Map<String, Object>> getAllFilteredDatasets(Object userQuery) {
		List<Map<String,Object>> ret = new ArrayList<Map<String,Object>>();
		DB db = mongoClient.getDB(mongoParams.get("MONGO_DB_META"));
		DBCollection coll = db.getCollection(mongoParams.get("MONGO_COLLECTION_DATASET"));
		DBCollection collapi = db.getCollection(mongoParams.get("MONGO_COLLECTION_API"));
		DBCollection collstream = db.getCollection(mongoParams.get("MONGO_COLLECTION_STREAM"));

		BasicDBObject query = (BasicDBObject) userQuery;
		query.put("configData.current", 1);
		DBCursor cursor = coll.find(query);

		while (cursor.hasNext()) {

			DBObject found=cursor.next();

			Map<String, Object> cur = extractOdataPropertyFromMongo(collapi,
					collstream, found);
			if(ret.size()<MAX_RECORDS){
				ret.add(cur);
			}else 
				break;
		}

		return ret;
	}



	public List<Map<String, Object>> getDatasetFields(Long datasetKey) {

		List<Map<String,Object>> ret = new ArrayList<Map<String,Object>>();
		DB db = mongoClient.getDB(mongoParams.get("MONGO_DB_META"));
		DBCollection coll = db.getCollection(mongoParams.get("MONGO_COLLECTION_DATASET"));

		BasicDBObject query = new BasicDBObject();
		query.append("idDataset", datasetKey);
		query.put("configData.current", 1);
		DBCursor cursor = coll.find(query);

		while (cursor.hasNext()) {

			DBObject obj=cursor.next();

			DBObject dataset = (DBObject) obj.get("info");

			BasicDBList fieldsList = (BasicDBList) dataset.get("fields");

			for (int i =0;i<fieldsList.size();i++){
				DBObject measure = (DBObject) fieldsList.get(i);

				String fieldName = (String) measure.get("fieldName");
				String fieldAlias = (String)measure.get("fieldAlias");
				String dataType = (String)measure.get("dataType");
				Integer sourceColumn = measure.get("sourceColumn") == null ? null : ((Number) measure.get("sourceColumn")).intValue();
				Integer isKey = measure.get("isKey") == null ? null :  ((Number)measure.get("isKey")).intValue();

				String measureUnit = (String)measure.get("measureUnit");

				Map<String,Object> cur = new HashMap<String, Object>();
				cur.put("fieldName", fieldName);
				cur.put("fieldAlias", fieldAlias);
				cur.put("dataType", dataType);
				cur.put("sourceColumn", sourceColumn);
				cur.put("isKey", isKey);
				cur.put("measureUnit", measureUnit);
				ret.add(cur);
			}
		}

		return ret;
	}
	public Map<String, Object> getDatasetSensor(Long datasetId) {
		DB db = mongoClient.getDB(mongoParams.get("MONGO_DB_META"));
		DBCollection collstream = db.getCollection(mongoParams.get("MONGO_COLLECTION_STREAM"));
		DBCollection colldataset = db.getCollection(mongoParams.get("MONGO_COLLECTION_DATASET"));



		DBObject searchActive = new BasicDBObject("idDataset",datasetId);
		searchActive.put("configData.current", 1);
		DBObject existingDatasetOfStream = colldataset.findOne(searchActive);
		if(existingDatasetOfStream==null)
			return null;

		DBObject searchById = new BasicDBObject("configData.idDataset", datasetId);

		searchById.put("configData.datasetVersion", existingDatasetOfStream.get("datasetVersion"));

		DBObject found = collstream.findOne(searchById);

		return extractDataFromStream(found);
	}

	public Map<String, Object> getStream(Long idStream) {
		DB db = mongoClient.getDB(mongoParams.get("MONGO_DB_META"));
		DBCollection collstream = db.getCollection(mongoParams.get("MONGO_COLLECTION_STREAM"));
		DBCollection colldataset = db.getCollection(mongoParams.get("MONGO_COLLECTION_DATASET"));

		DBObject searchById = new BasicDBObject("idStream", idStream);
		DBObject found = collstream.findOne(searchById);

		//we have to find the stream with the version that has an dataset current : 1 



		DBObject configData = (DBObject) found.get("configData");


		DBObject searchActive = new BasicDBObject("idDataset",configData.get("idDataset"));
		searchActive.put("configData.current", 1);
		DBObject existingDatasetOfStream = colldataset.findOne(searchActive);
		if(existingDatasetOfStream==null)
			return null;

		searchById = new BasicDBObject("idStream", idStream);
		searchById.put("configData.datasetVersion", existingDatasetOfStream.get("datasetVersion"));
		found = collstream.findOne(searchById);

		return extractDataFromStream(found);
	}

	public List<Map<String, Object>> getAllFilteredStreams(Object userQuery) {
		List<Map<String,Object>> ret = new ArrayList<Map<String,Object>>();
		DB db = mongoClient.getDB(mongoParams.get("MONGO_DB_META"));
		DBCollection collstream = db.getCollection(mongoParams.get("MONGO_COLLECTION_STREAM"));
		DBCollection colldataset = db.getCollection(mongoParams.get("MONGO_COLLECTION_DATASET"));

		BasicDBObject query = (BasicDBObject) userQuery;
		DBCursor cursor = collstream.find(query);

		while (cursor.hasNext()) {
			DBObject found=cursor.next();

			//if stream with that id and version has an active (current:1) dataset document
			DBObject configData = (DBObject) found.get("configData");
			DBObject searchActive = new BasicDBObject("idDataset",configData.get("idDataset"));
			searchActive.put("datasetVersion", configData.get("datasetVersion"));
			searchActive.put("configData.current", 1);
			DBObject existingDatasetOfStream = colldataset.findOne(searchActive);
			if(existingDatasetOfStream!=null){
				Map<String, Object> cur = extractDataFromStream(found);
				if(cur != null){
					if(ret.size()<MAX_RECORDS){
						ret.add(cur);
					}else {
						break;
					}
				}
			}
		}
		return ret;
	}

	private Map<String, Object> extractDataFromStream(DBObject found){
		Map<String, Object> cur = null;
		if (found != null) {
			cur  = new HashMap<String, Object>();	
			DBObject streams = (DBObject) found.get("streams");
			DBObject stream = (DBObject) streams.get("stream");
			DBObject configData = (DBObject) found.get("configData");

			String streamCode = (String) found.get("streamCode");
			String streamName = (String) found.get("streamName");
			String streamDescription = (String) stream.get("virtualEntityDescription");

			Integer IdStream = (Integer)found.get("idStream");
			Integer IdSensor = (Integer)stream.get("idVirtualEntity");
			Integer idDataset =(Integer)configData.get("idDataset");
			Integer datasetVersion =(Integer)configData.get("datasetVersion");
			String tenantCode =(String)configData.get("tenantCode");


			String code =(String)stream.get("virtualEntityCode");
			String name =(String)stream.get("virtualEntityName");
			String type =(String)stream.get("virtualEntityType");
			String category =(String)stream.get("virtualEntityCategory");
			String visibility =(String)stream.get("visibility");

			cur.put("idStream", IdStream);
			cur.put("idSensor", IdSensor);
			cur.put("idDataset", idDataset);
			cur.put("datasetVersion", datasetVersion);
			cur.put("tenantCode", tenantCode);
			cur.put("visibility", visibility);

			cur.put("streamCode", streamCode);
			cur.put("streamName", streamName);
			cur.put("streamDescription", streamDescription);

			cur.put("smartOName", name);
			cur.put("smartOCode", code);
			cur.put("smartOType", type);
			cur.put("smartOCategory",category );

			DBObject vePos = (DBObject) stream.get("virtualEntityPositions");



			//FIXME return all the positions if required
			if(vePos!=null){
				BasicDBList pos = (BasicDBList) vePos.get("position");
				if(pos!=null && pos.size()>0){
					DBObject veSingPos = (DBObject) pos.get(0);
					cur.put("latitude", veSingPos.get("lat"));
					cur.put("longitude", veSingPos.get("lon"));
					cur.put("elevation", veSingPos.get("elevation"));
				}
			}
		}
		return cur;
	}

	private Map<String, Object> extractOdataPropertyFromMongo(
			DBCollection collapi, DBCollection collstream, DBObject datasetFound) {
		Long id = datasetFound.get("idDataset") == null ? null :((Number)datasetFound.get("idDataset")).longValue();
		Long datasetVersion = datasetFound.get("datasetVersion") == null ? null :((Number)datasetFound.get("datasetVersion")).longValue();
		String datasetCode =(String)datasetFound.get("datasetCode") ;
		DBObject configData = (DBObject) datasetFound.get("configData");
		String tenant=configData.get("tenantCode").toString();
		String datasetStatus=(String)configData.get("datasetStatus");


		DBObject info = (DBObject) datasetFound.get("info");

		String license=(String)info.get("license");
		String dataDomain=(String)info.get("dataDomain");
		String description = (String)info.get("description");
		Double fps = info.get("fps") ==null ? null : ((Number)info.get("fps")).doubleValue();

		String datasetName = (String)info.get("datasetName");
		String visibility=(String)info.get("visibility");
		String registrationDate=(String)info.get("registrationDate");
		
		String startIngestionDate=(String)info.get("startIngestionDate");
		String endIngestionDate=(String)info.get("endIngestionDate");
		String importFileType=(String)info.get("importFileType");

		String disclaimer = (String)info.get("disclaimer");
		String copyright = (String)info.get("copyright");


		StringBuilder fieldsBuilder = new StringBuilder();
		BasicDBList fieldsList = (BasicDBList) info.get("fields");

		String prefix = "";
		for (int i =0;i<fieldsList.size();i++){
			DBObject measure = (DBObject) fieldsList.get(i);
			String mis = measure.get("measureUnit") == null ? null : measure.get("measureUnit").toString();
			if(mis!=null){
				fieldsBuilder.append(prefix);
				prefix = ",";
				fieldsBuilder.append(mis);
			}
		}
		String unitaMisura=fieldsBuilder.toString();
		StringBuilder tagsBuilder = new StringBuilder();
		BasicDBList  tagsList = (BasicDBList) info.get("tags");

		String tags=null;
		prefix = "";
		if(tagsList!=null){
			for (int i =0;i<tagsList.size();i++){
				DBObject tagObj = (DBObject) tagsList.get(i);
				tagsBuilder.append(prefix);
				prefix = ",";
				tagsBuilder.append(tagObj.get("tagCode").toString());
			}
			tags = tagsBuilder.toString();
		}

		DBObject tenantssharingDB = (DBObject) info.get("tenantssharing");
		
		
		StringBuilder tenantsBuilder = new StringBuilder();
		BasicDBList  tenantsList = (BasicDBList) tenantssharingDB.get("tenantsharing");
		String tenantsharing=null;
		prefix = "";
		if(tenantsList!=null){
			for (int i =0;i<tenantsList.size();i++){
				DBObject tenantsObj = (DBObject) tenantsList.get(i);
				tenantsBuilder.append(prefix);
				prefix = ",";
				tenantsBuilder.append(tenantsObj.get("tenantCode").toString());
			}
			tenantsharing = tenantsBuilder.toString();
		}

		Map<String,Object> cur = new HashMap<String, Object>();
		cur.put("idDataset", id);
		cur.put("tenantCode", tenant);
		cur.put("tenantsharing", tenantsharing);
		cur.put("dataDomain", dataDomain);
		cur.put("license", license);
		cur.put("description", description);

		cur.put("fps", fps);




		cur.put("measureUnit", unitaMisura);
		cur.put("tags",tags );

//		BasicDBObject findapi = new BasicDBObject();
//		findapi.append("dataset.idDataset", id);
//		findapi.append("dataset.datasetVersion", datasetVersion);



		StringBuilder apibuilder = new StringBuilder(); 
			//				DBObject config = (DBObject) parent.get("configData");
			apibuilder.append(mongoParams.get("MONGO_API_ADDRESS"));
			apibuilder.append("name="+datasetCode);
			apibuilder.append("_odata");	
			apibuilder.append("&version=1.0&provider=admin");					
	
			cur.put("API",apibuilder.toString());

		BasicDBObject findstream = new BasicDBObject();
		findstream.append("configData.idDataset", id);
		findstream.append("configData.datasetVersion", datasetVersion);
		DBObject streams = collstream.findOne(findstream);


		StringBuilder streambuilder = new StringBuilder(); 




		if(streams != null ){
			DBObject nx = streams;

			DBObject config = (DBObject) nx.get("configData");
			DBObject streamsObj = (DBObject) nx.get("streams");
			DBObject stream = (DBObject) streamsObj.get("stream");



			streambuilder.append(mongoParams.get("MONGO_API_ADDRESS"));
			streambuilder.append("name="+config.get("tenantCode"));
			streambuilder.append(".");
			streambuilder.append(stream.get("virtualEntityCode"));
			streambuilder.append("_");
			streambuilder.append(nx.get("streamCode"));
			streambuilder.append("_stream");
			streambuilder.append("&version=1.0&provider=admin");	
		}

		String download = mongoParams.get("MONGO_DOWNLOAD_ADDRESS")+"/"+tenant+"/"+datasetCode+"/csv";


		cur.put("STREAM",streambuilder.toString() );

		cur.put("download", download);

		cur.put("datasetName", datasetName);
		cur.put("visibility", visibility);
		cur.put("registrationDate", registrationDate);
		cur.put("startIngestionDate", startIngestionDate);
		cur.put("endIngestionDate", endIngestionDate);
		cur.put("importFileType", importFileType);
		cur.put("datasetStatus", datasetStatus);

		cur.put("datasetVersion", (datasetVersion==null)?null:datasetVersion.toString());
		cur.put("datasetCode", datasetCode);
		cur.put("disclaimer", disclaimer);
		cur.put("copyright", copyright);

		return cur;
	}

	public Map<String, Object> getDatasetFromStream(Integer idStream) {
		Map<String,Object> ret = new HashMap<String,Object>();

		DB db = mongoClient.getDB(mongoParams.get("MONGO_DB_META"));
		DBCollection colldataset = db.getCollection(mongoParams.get("MONGO_COLLECTION_DATASET"));

		DBCollection collapi = db.getCollection(mongoParams.get("MONGO_COLLECTION_API"));
		DBCollection collstream = db.getCollection(mongoParams.get("MONGO_COLLECTION_STREAM"));



		DBObject searchById = new BasicDBObject("idStream", idStream);
		DBObject found = collstream.findOne(searchById);

		if(found!=null){

			DBObject configData= (DBObject) found.get("configData");
			DBObject searchDatasetById = new BasicDBObject("idDataset", configData.get("idDataset"));
			searchDatasetById.put("configData.current", 1);
			DBObject foundDataset = colldataset.findOne(searchDatasetById);

			if (foundDataset != null) {
				Map<String, Object> cur = extractOdataPropertyFromMongo(collapi,collstream, foundDataset);
				ret=cur;
			}
		}
		return ret;
	}



}
