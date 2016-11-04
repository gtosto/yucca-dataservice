package org.csi.yucca.dataservice.binaryapi.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ParserDecorator;
import org.apache.tika.sax.BodyContentHandler;
import org.csi.yucca.dataservice.binaryapi.dao.MongoDBBinaryDAO;
import org.csi.yucca.dataservice.binaryapi.dao.MongoDBMetadataDAO;
import org.csi.yucca.dataservice.binaryapi.dao.MongoDBStreamDAO;
import org.csi.yucca.dataservice.binaryapi.dao.MongoDBTenantDAO;
import org.csi.yucca.dataservice.binaryapi.knoxapi.HdfsFSUtils;
import org.csi.yucca.dataservice.binaryapi.model.api.Dataset;
import org.csi.yucca.dataservice.binaryapi.model.api.MyApi;
import org.csi.yucca.dataservice.binaryapi.model.metadata.BinaryData;
import org.csi.yucca.dataservice.binaryapi.model.metadata.Metadata;
import org.csi.yucca.dataservice.binaryapi.model.metadata.Stream;
import org.csi.yucca.dataservice.binaryapi.mongo.singleton.Config;
import org.csi.yucca.dataservice.binaryapi.mongo.singleton.MongoSingleton;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.xml.sax.SAXException;

import com.mongodb.MongoClient;


@Path("/binary")
public class BinaryService {

	private final String MEDIA = "media";
	private final String PATH_INTERNAL_HDFS = "/rawdata/files/";
	private final Integer MAX_SIZE_FILE_ATTACHMENT = 154857601;
	
	private final String PATH_RAWDATA = "/rawdata";

	static Logger LOG = Logger.getLogger(BinaryService.class);
	static Logger LOGACCOUNT = Logger.getLogger("sdpaccounting");

	
	@GET
	@Path("/hello")
	public String hello() {
		return "hello";
	}
	
	
	@GET //ok
	@Produces({"text/csv"})
	@Path("/{apiCode}/download/{idDataSet}/{datasetVersion}")
	public Response downloadCSVFile(@PathParam("apiCode") String apiCode, @PathParam("idDataSet") Long idDataSet, @PathParam("datasetVersion") String datasetVersion) throws WebApplicationException, NumberFormatException, UnknownHostException {
		
		System.out.println("downloadCSVFile!");
		long startTime = System.currentTimeMillis();
		
		//MessageContext mc = wsContext.getMessageContext();
	    //HttpServletRequest req = (HttpServletRequest)mc.get(MessageContext.SERVLET_REQUEST);
	    
		//System.out.println("accountingLog start!");
		
		//AccountingLog accountingLog = new AccountingLog();  //info.getClientIp()
		//accountingLog.setUniqueid(apiCode + "|" + idDataSet + "|" + datasetVersion);
		//accountingLog.setApicode(apiCode);
		//accountingLog.setQuerString("GET - downloadCSVFile");
		
		//System.out.println("accountingLog go!");
		
		//Enumeration<String> headerNames = req.getHeaderNames();
		//String headerName = "";
		//String headerValue = "";
		//String logAccountingMessage = "";
		
		//while (headerNames.hasMoreElements()) { 
		//	headerName = headerNames.nextElement();
		//	headerValue = req.getHeader(headerName);
			
		//	String uniqueid = "";
		//	String forwardefor = "";
		//	String jwt = "";
		//	if ("UNIQUE_ID".equals(headerName)) uniqueid = headerValue;
		//	else if ("X-Forwarded-For".equals(headerName)) forwardefor = headerValue;
		//	else if ("X-JWT-Assertion".equals(headerName)) jwt = headerValue;
		//} 
		
		//System.out.println("accountingLog done!");
		
		MongoClient mongo = MongoSingleton.getMongoClient();
		String supportDb = Config.getInstance().getDbSupport();
		String supportDatasetCollection = Config.getInstance().getCollectionSupportDataset();
		String supportTenantCollection = Config.getInstance().getCollectionSupportTenant();
		String supportStreamCollection = Config.getInstance().getCollectionSupportStream();
		MongoDBMetadataDAO metadataDAO = new MongoDBMetadataDAO(mongo, supportDb, supportDatasetCollection);
		MongoDBTenantDAO tenantDAO = new MongoDBTenantDAO(mongo, supportDb, supportTenantCollection);
		MongoDBStreamDAO streamDAO = new MongoDBStreamDAO(mongo, supportDb, supportStreamCollection);
		String supportApiCollection = Config.getInstance().getCollectionSupportApi();
		
		System.out.println("set MONGODB!");
		
		//Get tenantCode from ApiCode
		org.csi.yucca.dataservice.binaryapi.dao.MongoDBApiDAO apiDAO = new org.csi.yucca.dataservice.binaryapi.dao.MongoDBApiDAO(mongo, supportDb, supportApiCollection);
		MyApi api = null;
		try {
			api = apiDAO.readApiByCode(apiCode);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		if (api == null){ 
			throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON)
					.entity("{\"error_name\":\"Api not found\", \"error_code\":\"E117a\", \"output\":\"NONE\", \"message\":\"this binary does not exist\"}")
					.build());
		}
		
		System.out.println("Get tenantCode from ApiCode! => " + api.getApiName());
		
		Metadata mdMetadata = null;
		String tenantCode = api.getConfigData().getTenantCode();
		String organizationCode = tenantDAO.getOrganizationByTenantCode(tenantCode).toUpperCase();
		//accountingLog.setJwtData(tenantCode);
		Integer dsVersion = null;
		
		System.out.println("tenantCode! => " + tenantCode);
		
		if ((datasetVersion.equals("current")) || (datasetVersion.equals("all"))){
			System.out.println("Current");

			mdMetadata = metadataDAO.getCurrentMetadaByBinaryID(idDataSet);
			if (mdMetadata == null){ 
				throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON)
						.entity("{\"error_name\":\"Binary not found\", \"error_code\":\"E117a\", \"output\":\"NONE\", \"message\":\"this binary does not exist\"}")
						.build());
			} else {
				if (datasetVersion.equals("all")){
					if (!mdMetadata.getConfigData().getSubtype().equals("bulkDataset")){
						throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON)
								.entity("{\"error_name\":\"Binary not found\", \"error_code\":\"E118\", \"output\":\"NONE\", \"message\":\"All available only for bulk dataset\"}")
								.build());
					}
					LOG.info("Richiesto caricamento ALL");
					dsVersion = 0;
					System.out.println("dsVersion b = " + dsVersion);
				} else {
					dsVersion = mdMetadata.getDatasetVersion();
				}
				System.out.println("dsVersion a = " + dsVersion);
			}
		} else {
			System.out.println("Versione specifica");
			dsVersion = Integer.parseInt(datasetVersion);
			System.out.println("dsVersion b = " + dsVersion);
			mdMetadata = metadataDAO.readCurrentMetadataByTntAndIDDS(idDataSet, dsVersion, tenantCode);
		}

		
		if (mdMetadata != null){
			String datasetCode = mdMetadata.getDatasetCode();
			//accountingLog.setDatasetcode(datasetCode);
			
			String typeDirectory = "";
			String subTypeDirectory = "";
			Boolean checkDataSet = false;
			List<Dataset> myListDS = api.getDataset();
			
			//Verify if have a permission to download selected file (...)
			for (Iterator<Dataset> itDS = myListDS.iterator(); itDS.hasNext();) {
				
				Dataset itemDS = itDS.next();
				if(itemDS.getIdDataset().equals(idDataSet)){
					checkDataSet = true;
				}
			}
				
			if (checkDataSet){
				if (mdMetadata.getConfigData().getTenantCode().equals(api.getConfigData().getTenantCode())) {
					
					String dataDomain = mdMetadata.getInfo().getDataDomain().toUpperCase();
					
					if (mdMetadata.getConfigData().getSubtype().equals("bulkDataset")){
						//typeDirectory = "db_" + mdMetadata.getConfigData().getTenantCode();
						//subTypeDirectory = datasetCode;
						
						
						if (mdMetadata.getInfo().getCodSubDomain() == null){
							System.out.println("CodSubDomain is null => " + mdMetadata.getInfo().getCodSubDomain());
							typeDirectory = "db_" + mdMetadata.getConfigData().getTenantCode();
						} else {
							System.out.println("CodSubDomain => " + mdMetadata.getInfo().getCodSubDomain());
							typeDirectory = "db_" + mdMetadata.getInfo().getCodSubDomain();
						}
						subTypeDirectory = mdMetadata.getDatasetCode();

						System.out.println("typeDirectory => " + typeDirectory);
						System.out.println("subTypeDirectory => " + subTypeDirectory);
					} else if (mdMetadata.getConfigData().getSubtype().equals("streamDataset")){
						Stream tmp = streamDAO.getStreamByDataset(idDataSet, dsVersion);
						typeDirectory = "so_" + tmp.getStreams().getStream().getVirtualEntitySlug();
						subTypeDirectory = tmp.getStreamCode();
					} else if (mdMetadata.getConfigData().getSubtype().equals("socialDataset")){
						Stream tmp = streamDAO.getStreamByDataset(idDataSet, dsVersion);
						typeDirectory = "so_" + tmp.getStreams().getStream().getVirtualEntitySlug();
						subTypeDirectory = tmp.getStreamCode();
					}
					
					System.out.println("mdFromMongo.subtype = " + mdMetadata.getConfigData().getSubtype());
					System.out.println("typeDirectory = " + typeDirectory);
					
					if (typeDirectory.equals("")) {
						throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON)
								.entity("{\"error_name\":\"Metadata Wrong\", \"error_code\":\"E126\", \"output\":\"NONE\", \"message\":\"ther's an error in metadata configuration, or dataset is not bulk or stream\"}")
								.build());
					}
					
					String hdfsDirectory = "/" + Config.getHdfsRootDir() + "/" + organizationCode + PATH_RAWDATA + "/" + dataDomain + "/" + typeDirectory + "/" + subTypeDirectory + "/";
					System.out.println("hdfsDirectory = " + hdfsDirectory);
					Reader is = null;
					try {
						is = org.csi.yucca.dataservice.binaryapi.knoxapi.HdfsFSUtils.readDir(hdfsDirectory, dsVersion);
					} catch (Exception e) {
						LOG.error("Internal error during READDIR", e);
						throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON)
								.entity("{\"error_name\":\"Internal error\", \"error_code\":\"\", \"output\":\"NONE\", \"message\":\""+e.getMessage()+"\"}").build());
					}
					System.out.println("InputStream letto");
					
					if (is != null){ 
						//accountingLog.setElapsed(System.currentTimeMillis() - startTime);
						//accountingLog.set
						//LOGACCOUNT.info(accountingLog.toString());
						return Response.ok(is).header("Content-Disposition", "attachment; filename=" + tenantCode + "-" + datasetCode + "-" + ((dsVersion == 0) ? "all" : dsVersion.toString()) + ".csv").build();
						//return is;
					} else { 
						//accountingLog.setElapsed(System.currentTimeMillis() - startTime);
						//accountingLog.setErrore("Binary not found - this dataset does not exist");
						//LOGACCOUNT.info(accountingLog.toString());
						throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON)
								.entity("{\"error_name\":\"Binary not found\", \"error_code\":\"E117b\", \"output\":\"NONE\", \"message\":\"this csv does not exist\"}").build());
					}
				} else {
					//accountingLog.setElapsed(System.currentTimeMillis() - startTime);
					//accountingLog.setErrore("Dataset not found - this dataset does not exist");
					//LOGACCOUNT.info(accountingLog.toString());
					throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON)
							.entity("{\"error_name\":\"Dataset not found\", \"error_code\":\"E116a\", \"output\":\"NONE\", \"message\":\"this dataset does not exist\"}").build());
				}
			} else {
				//accountingLog.setElapsed(System.currentTimeMillis() - startTime);
				//accountingLog.setErrore("Dataset not found - this dataset does not exist");
				//LOGACCOUNT.info(accountingLog.toString());
				throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON)
						.entity("{\"error_name\":\"Dataset not found\", \"error_code\":\"E116b\", \"output\":\"NONE\", \"message\":\"this dataset does not exist\"}").build());
			}	
		}
		//accountingLog.setElapsed(System.currentTimeMillis() - startTime);
		//accountingLog.setErrore("Dataset not found - null is inconsistent");
		//LOGACCOUNT.info(accountingLog.toString());

		throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON)
				.entity("{\"error_name\":\"Dataset not found\", \"error_code\":\"E119\", \"output\":\"NONE\", \"message\":\"null is inconsistent\"}").build());
	}

	@GET //ok
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/{apiCode}/attachment/{idDataSet}/{datasetVersion}/{idBinary}")
	public InputStream downloadFile(@PathParam("apiCode") String apiCode, @PathParam("idDataSet") Long idDataSet, @PathParam("datasetVersion") Integer datasetVersion,
			@PathParam("idBinary") String idBinary) throws WebApplicationException, NumberFormatException, UnknownHostException {

		long startTime = System.currentTimeMillis();
		MongoClient mongo = MongoSingleton.getMongoClient();
		String supportDb = Config.getInstance().getDbSupport();
		String supportDatasetCollection = Config.getInstance().getCollectionSupportDataset();
		MongoDBMetadataDAO metadataDAO = new MongoDBMetadataDAO(mongo, supportDb, supportDatasetCollection);
		String supportApiCollection = Config.getInstance().getCollectionSupportApi();
		MongoDBBinaryDAO binaryDAO = null;
		BinaryData binaryData = null;

		System.out.println("apiCode = " + apiCode);
		System.out.println("idBinary = " + idBinary);
		
		//Get tenantCode from ApiCode
		org.csi.yucca.dataservice.binaryapi.dao.MongoDBApiDAO apiDAO = new org.csi.yucca.dataservice.binaryapi.dao.MongoDBApiDAO(mongo, supportDb, supportApiCollection);
		MyApi api = null;
		try {
			api = apiDAO.readApiByCode(apiCode);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (api != null){
			String tenantCode = api.getConfigData().getTenantCode();
			List<Dataset> myListDS = api.getDataset();
			
			binaryDAO = new MongoDBBinaryDAO(mongo, "DB_" + tenantCode, MEDIA);
			binaryData = binaryDAO.readCurrentBinaryDataByIdBinary(idBinary, idDataSet);

			Metadata mdBinaryDataSet = null;
			try { 
				System.out.println("idDataSet = " + idDataSet);
				System.out.println("datasetVersion = " + datasetVersion);
				System.out.println("tenantCode = " + tenantCode);
				mdBinaryDataSet = metadataDAO.readCurrentMetadataByTntAndIDDS(idDataSet, datasetVersion, tenantCode);
				System.out.println("mdBinaryDataSet = " + mdBinaryDataSet.toJson());
			} catch (Exception ex) {
				System.out.println("DATASET NON TROVATO! return null");
			}
			
			if (mdBinaryDataSet != null){
				Metadata mdFromMongo = null;
				
				//Verify if have a permission to download selected file (idBinary)
				for (Iterator<Dataset> itDS = myListDS.iterator(); itDS.hasNext();) {
					Dataset itemDS = itDS.next();
					mdFromMongo = metadataDAO.readCurrentMetadataByTntAndIDDS(itemDS.getIdDataset(), itemDS.getDatasetVersion(), itemDS.getTenantCode());
					
					if (mdFromMongo.getConfigData().getSubtype().equals("bulkDataset")
							&& (mdFromMongo.getConfigData().getTenantCode().equals(api.getConfigData().getTenantCode()) && 
							   (mdFromMongo.getInfo().getBinaryIdDataset().equals(idDataSet)) && 
							   (mdFromMongo.getInfo().getBinaryDatasetVersion().equals(datasetVersion)))) {
						if (binaryData == null){ 
							throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON)
									.entity("{\"error_name\":\"Binary not found\", \"error_code\":\"E117a\", \"output\":\"NONE\", \"message\":\"this binary does not exist\"}").build());
						} else {
							String pathForUri = binaryData.getPathHdfsBinary();

							InputStream is;
							try {
								is = HdfsFSUtils.readFile(pathForUri);
							} catch (Exception e) {
								LOG.error("Internal error during READFile", e);
								throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON)
										.entity("{\"error_name\":\"Internal error\", \"error_code\":\"\", \"output\":\"NONE\", \"message\":\""+e.getMessage()+"\"}").build());
							}
							if (is != null)
								return is;
							else 
								throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON)
										.entity("{\"error_name\":\"Binary not found\", \"error_code\":\"E117b\", \"output\":\"NONE\", \"message\":\"this binary does not exist\"}").build());
						}
					} else {
						throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON)
								.entity("{\"error_name\":\"Dataset not attachment\", \"error_code\":\"E112\", \"output\":\"NONE\", \"message\":\"this dataset does not accept attachments\"}").build());
					}
				}
			} else {
				throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON)
						.entity("{\"error_name\":\"Dataset not found\", \"error_code\":\"E116\", \"output\":\"NONE\", \"message\":\"this dataset does not exist\"}").build());
			}
		} else {
			throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON)
					.entity("{\"error_name\":\"API Code Wrong\", \"error_code\":\"E115\", \"output\":\"NONE\", \"message\":\"this api does not exist\"}").build());
		}
		return null;
	}

	@POST
	@Path("/prova/")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
    public void put(MultipartFormDataInput input) throws IOException
    {
	int i = 0;
       for (String key : input.getFormDataMap().keySet())
       {
    	   System.out.println(i+"-"+key);
    	   
    	   List<InputPart> list = input.getFormDataMap().get(key);
    	   System.out.println(i+"-"+list.size());
    	   System.out.println(i+"-"+list.get(0).getHeaders());
       }
		

    }	
	
	
	private String parseFileName(MultivaluedMap<String, String> headers) {
		String[] contentDispositionHeader = headers.getFirst("Content-Disposition").split(";");
		for (String name : contentDispositionHeader) {
			if ((name.trim().startsWith("filename"))) {
				String[] tmp = name.split("=");
				String fileName = tmp[1].trim().replaceAll("\"","");
				return fileName;
			}
		}
		return "randomName";
	}
	
	@POST  //ok
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/input/{tenant}/")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadFile(MultipartFormDataInput body)  throws NumberFormatException, IOException {
		
		String aliasFile = body.getFormDataMap().get("aliasFile").get(0).getBodyAsString();
		String idBinary = body.getFormDataMap().get("idBinary").get(0).getBodyAsString();
		String tenantCode = body.getFormDataMap().get("tenantCode").get(0).getBodyAsString();
		String datasetCode = body.getFormDataMap().get("datasetCode").get(0).getBodyAsString();
		Integer datasetVersion = Integer.parseInt(body.getFormDataMap().get("datasetVersion").get(0).getBodyAsString());
		
		InputPart fileInputPart = body.getFormDataMap().get("upfile").get(0);
		String filename = parseFileName(fileInputPart.getHeaders());
		
		long startTime = System.currentTimeMillis();
		//Get size for verify max size file upload (dirty) 
		Integer sizeFileAttachment = null;
		try {
			sizeFileAttachment = fileInputPart.getBody(InputStream.class, null).available();
		} catch (IOException ex2) {
			ex2.printStackTrace();
		}
		
		if (sizeFileAttachment > MAX_SIZE_FILE_ATTACHMENT){
			return Response.status(413)
					.entity("{\"error_name\":\"File too Big\", \"error_code\":\"E114\", \"output\":\"NONE\", \"message\":\"THE SIZE IS TOO BIG\"}").build();
		}
		
		SimpleDateFormat sdfStartMongo = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
		Date dateS = new Date();
		System.out.println("start Mongo => " + sdfStartMongo.format(dateS));
		
		String pathFile = filename;
		BinaryData binaryData = new BinaryData();
		MongoClient mongo = MongoSingleton.getMongoClient();
		String supportDb = Config.getInstance().getDbSupport();
		String supportDatasetCollection = Config.getInstance().getCollectionSupportDataset();
		String supportTenantCollection = Config.getInstance().getCollectionSupportTenant();
		String supportStreamCollection = Config.getInstance().getCollectionSupportStream();
		MongoDBTenantDAO tenantDAO = new MongoDBTenantDAO(mongo, supportDb, supportTenantCollection);
		MongoDBStreamDAO streamDAO = new MongoDBStreamDAO(mongo, supportDb, supportStreamCollection);
		MongoDBMetadataDAO metadataDAO = new MongoDBMetadataDAO(mongo, supportDb, supportDatasetCollection);
		MongoDBBinaryDAO binaryDAO = new MongoDBBinaryDAO(mongo, "DB_" + tenantCode, MEDIA);

		//Get idDataset from datasetCode, datasetVersion and tenantCode
		Metadata mdFromMongo = null;
		try {
			//mdFromMongo � il DATASET di riferimento, quello BULK, STREAM O SOCIAL
			mdFromMongo = metadataDAO.readCurrentMetadataByTntAndDSCode(datasetCode, datasetVersion, tenantCode);
		} catch (Exception ex1) {
			ex1.printStackTrace();
		}
		if (mdFromMongo == null) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("{\"error_name\":\"Dataset unknown\", \"error_code\":\"E111\", \"output\":\"NONE\", \"message\":\"You could not find the specified dataset\"}").build();
		}
		
		binaryData.setIdDataset(mdFromMongo.getInfo().getBinaryIdDataset());
		binaryData.setIdBinary(idBinary);
		binaryData.setAliasNameBinary(aliasFile);
		binaryData.setTenantBinary(tenantCode);
		binaryData.setDatasetCode(datasetCode);
		binaryData.setFilenameBinary(pathFile);
		binaryData.setContentTypeBinary(fileInputPart.getMediaType().getType());
		
		System.out.println("BinaryIdDataset => " + mdFromMongo.getInfo().getBinaryIdDataset());
		
		//mdBinaryDataSet � il DATASET BINARY!!!!
		Metadata mdBinaryDataSet = metadataDAO.getCurrentMetadaByBinaryID(mdFromMongo.getInfo().getBinaryIdDataset());
		System.out.println("mdBinaryDataSet => " + mdBinaryDataSet.getIdDataset());
		
		SimpleDateFormat sdfEndMongo = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
		Date dateE = new Date();
		System.out.println("end Mongo => " + sdfEndMongo.format(dateE));
		
		System.out.println("Subtype = " + mdFromMongo.getConfigData().getSubtype());
		if (mdFromMongo.getConfigData().getSubtype().equals("bulkDataset") && (mdBinaryDataSet != null)) {

			String typeDirectory = "";
			String subTypeDirectory = "";

			SimpleDateFormat sdfStartWebHDFS = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
			Date dateSWebHDFS = new Date();
			System.out.println("start WebHDFS => " + sdfStartWebHDFS.format(dateSWebHDFS));

			System.out.println("mdBinaryDataSet.getInfo() => " + mdFromMongo.getInfo().toJson().toString());
			System.out.println("mdBinaryDataSet.getInfo().getDataDomain() => " + mdFromMongo.getInfo().getDataDomain());
			
			String dataDomain = mdFromMongo.getInfo().getDataDomain();
			System.out.println("dataDomain => " + dataDomain);
			dataDomain = dataDomain.toUpperCase();
			
			if (mdFromMongo.getConfigData().getSubtype().equals("bulkDataset")){
				if (mdBinaryDataSet.getInfo().getCodSubDomain() == null){
					System.out.println("CodSubDomain is null => " + mdBinaryDataSet.getInfo().getCodSubDomain());
					typeDirectory = "db_" + mdBinaryDataSet.getConfigData().getTenantCode();
				} else {
					System.out.println("CodSubDomain => " + mdBinaryDataSet.getInfo().getCodSubDomain());
					typeDirectory = "db_" + mdBinaryDataSet.getInfo().getCodSubDomain();
				}
				subTypeDirectory = mdBinaryDataSet.getDatasetCode();

				System.out.println("typeDirectory => " + typeDirectory);
				System.out.println("subTypeDirectory => " + subTypeDirectory);
			} else if (mdFromMongo.getConfigData().getSubtype().equals("streamDataset")){
				Stream tmp = streamDAO.getStreamByDataset(mdBinaryDataSet.getIdDataset(), datasetVersion);
				typeDirectory = "so_" + tmp.getStreams().getStream().getVirtualEntitySlug();
				subTypeDirectory = tmp.getStreamCode();
			} else {
				typeDirectory = "";
			}

			System.out.println("mdFromMongo.subtype = " + mdBinaryDataSet.getConfigData().getSubtype());
			System.out.println("typeDirectory = " + typeDirectory);

			String organizationCode = tenantDAO.getOrganizationByTenantCode(tenantCode).toUpperCase();
			
			if (typeDirectory.equals("")) {
				throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON)
						.entity("{\"error_name\":\"Metadata Wrong\", \"error_code\":\"E126\", \"output\":\"NONE\", \"message\":\"ther's an error in metadata configuration, or dataset is not bulk or stream\"}")
						.build());
			}
			
			String hdfsDirectory = "/" + Config.getHdfsRootDir() + "/" + organizationCode + PATH_RAWDATA + "/" + dataDomain + "/" + typeDirectory + "/" + subTypeDirectory + "/" + idBinary;
			System.out.println("hdfsDirectory = " + hdfsDirectory);
			
			String pathForUri = "/" + Config.getHdfsRootDir() + "/tnt-" + tenantCode + PATH_INTERNAL_HDFS + mdBinaryDataSet.getDatasetCode() + "/" + idBinary;
			binaryData.setPathHdfsBinary(hdfsDirectory);
			String uri = null;
			try {
				uri = HdfsFSUtils.writeFile(hdfsDirectory, fileInputPart.getBody(InputStream.class, null), idBinary);
				
			} catch (Exception ex) {
				ex.printStackTrace();
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity("{\"error_name\":\"Dataset attachment wrong\", \"error_code\":\"E113\", \"output\":\"NONE\", \"message\":\"" + ex.getMessage() + "\"}").build();
			}
			

			SimpleDateFormat sdfEndWebHDFS = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
			Date dateEWebHDFS = new Date();
			System.out.println("end WebHDFS => " + sdfEndWebHDFS.format(dateEWebHDFS));
			
			System.out.println("HDFS URI = " + uri);
			
			binaryData.setDatasetVersion(mdBinaryDataSet.getDatasetVersion());
			binaryData.setMetadataBinary("");
			binaryData.setSizeBinary(0L);
			binaryDAO.createBinary(binaryData);

			updateMongo(binaryData.getTenantBinary(), binaryData.getDatasetCode(), binaryData.getDatasetVersion(), binaryData.getIdBinary());

			return Response.ok().build();
		} else {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("{\"error_name\":\"Dataset not attachment\", \"error_code\":\"E112\", \"output\":\"NONE\", \"message\":\"this dataset does not accept attachments\"}").build();
		}
	}

	public void updateMongo(@PathParam("tenantCode") String tenantCode, @PathParam("datasetCode") String datasetCode, @PathParam("datasetVersion") Integer datasetVersion,
			@PathParam("idBinary") String idBinary) throws WebApplicationException, NumberFormatException, UnknownHostException {

		long startTime = System.currentTimeMillis();
		MongoClient mongo = MongoSingleton.getMongoClient();
		MongoDBBinaryDAO binaryDAO = null;
		BinaryData binaryData = null;
		
		binaryDAO = new MongoDBBinaryDAO(mongo, "DB_" + tenantCode, MEDIA);
		
		System.out.println("updateMongo - tenantCode = " + tenantCode + ", datasetCode = " + datasetCode + ", datasetVersion = " + datasetVersion + ", idBinary=" + idBinary);

		//Get idDataset from datasetCode 
		String supportDb = Config.getInstance().getDbSupport();
		String supportDatasetCollection = Config.getInstance().getCollectionSupportDataset();
		MongoDBMetadataDAO metadataDAO = new MongoDBMetadataDAO(mongo, supportDb, supportDatasetCollection);
		Metadata mdFromMongo = null;
		try {
			mdFromMongo = metadataDAO.readCurrentMetadataByTntAndDSCode(datasetCode, datasetVersion, tenantCode);
		} catch (Exception ex1) {
			ex1.printStackTrace();
		}
		if (mdFromMongo == null) {
			throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("{\"error_name\":\"Dataset unknown\", \"error_code\":\"E111\", \"output\":\"NONE\", \"message\":\"You could not find the specified dataset\"}").build());
		}
		Long idDataset = mdFromMongo.getInfo().getBinaryIdDataset();
		
		//Get binaryObject
		binaryData = binaryDAO.readCurrentBinaryDataByIdBinary(idBinary, idDataset);
		String pathForUri = binaryData.getPathHdfsBinary();
		System.out.println("updateMongo - idBinary = " + idBinary);
		System.out.println("updateMongo - idDataset = " + idDataset);
		System.out.println("updateMongo - pathForUri = " + pathForUri);
		
		//Get METADATA of selected file
		//InputStream fileToParser = HdfsFSUtils.readFile(Config.getHdfsUsername() + tenantCode, Config.getKnoxPwd() , pathForUri, Config.getKnoxUrl());
		InputStream fileToParser = null;

		try {
			fileToParser = HdfsFSUtils.readFile(pathForUri);
		} catch (Exception e1) {
			LOG.error("Internal error during READDIR", e1);
			throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON)
					.entity("{\"error_name\":\"Internal error\", \"error_code\":\"\", \"output\":\"NONE\", \"message\":\""+e1.getMessage()+"\"}").build());
		} 
		
		System.out.println("fileToParser = " + fileToParser.toString());
		
		if (fileToParser != null){

			Map<String,String> mapHS = null;
			try {
				mapHS = extractMetadata(fileToParser);
				binaryData.setMetadataBinary(mapHS.toString());
				
				Long sizeFileLenght = 0L;
				
				sizeFileLenght = new Long(HdfsFSUtils.statusFile(pathForUri).getLength());
				
				System.out.println("sizeFileLenght = " + sizeFileLenght.toString());
				
				binaryData.setSizeBinary(sizeFileLenght);
				binaryDAO.updateBinaryData(binaryData);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					fileToParser.close();
					throw new WebApplicationException(Response.ok().build());
				} catch (IOException ex) {
					
					ex.printStackTrace();
				}
			}
		}
		throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
				.entity("{\"error_name\":\"Dataset not attachment\", \"error_code\":\"E112\", \"output\":\"NONE\", \"message\":\"this dataset does not accept attachments\"}").build());
	}

/*
	@DELETE //to do
	@Path("/{tenantCode}/clearMetadata/{datasetCode}")
	public void clearMetadata(@PathParam("tenantCode") String tenantCode, @PathParam("datasetCode") String datasetCode) throws WebApplicationException, NumberFormatException, UnknownHostException {

		long startTime = System.currentTimeMillis();
		MongoClient mongo = MongoSingleton.getMongoClient();

		String typeDirectory = "";
		String subTypeDirectory = "";
		
		//Get idDataset from datasetCode 
		String supportDb = Config.getInstance().getDbSupport();
		String supportDatasetCollection = Config.getInstance().getCollectionSupportDataset();

		String supportTenantCollection = Config.getInstance().getCollectionSupportTenant();
		String supportStreamCollection = Config.getInstance().getCollectionSupportStream();
		MongoDBTenantDAO tenantDAO = new MongoDBTenantDAO(mongo, supportDb, supportTenantCollection);
		MongoDBStreamDAO streamDAO = new MongoDBStreamDAO(mongo, supportDb, supportStreamCollection);
		MongoDBMetadataDAO metadataDAO = new MongoDBMetadataDAO(mongo, supportDb, supportDatasetCollection);
		MongoDBBinaryDAO binaryDAO = new MongoDBBinaryDAO(mongo, "DB_" + tenantCode, MEDIA);
		
		Metadata mdFromMongo = null;
		try {
			mdFromMongo = metadataDAO.readCurrentMetadataByTntAndDSCode(datasetCode, tenantCode);
		} catch (Exception ex1) {
			ex1.printStackTrace();
		}
		if (mdFromMongo == null) {
			throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("{\"error_name\":\"Dataset unknown\", \"error_code\":\"E111\", \"output\":\"NONE\", \"message\":\"You could not find the specified dataset\"}").build());
		}

		Metadata mdBinaryDataSet = metadataDAO.getCurrentMetadaByBinaryID(mdFromMongo.getInfo().getBinaryIdDataset());
		
		String organizationCode = tenantDAO.getOrganizationByTenantCode(tenantCode).toUpperCase();
		String dataDomain = mdBinaryDataSet.getInfo().getDataDomain().toUpperCase();
		
		if (mdBinaryDataSet.getConfigData().getSubtype().equals("bulkDataset")){
			typeDirectory = "db_" + mdBinaryDataSet.getConfigData().getTenantCode();
		} else if (mdBinaryDataSet.getConfigData().getSubtype().equals("streamDataset")){
			Stream tmp = streamDAO.getStreamByDataset(mdBinaryDataSet.getIdDataset(), mdBinaryDataSet.getDatasetVersion());
			typeDirectory = "so_" + tmp.getStreams().getStream().getVirtualEntitySlug();
			subTypeDirectory = datasetCode;
		} else if (mdBinaryDataSet.getConfigData().getSubtype().equals("socialDataset")){
			Stream tmp = streamDAO.getStreamByDataset(mdBinaryDataSet.getIdDataset(), mdBinaryDataSet.getDatasetVersion());
			typeDirectory = "so_" + tmp.getStreams().getStream().getVirtualEntitySlug();
			subTypeDirectory = tmp.getStreamCode();
		}
		
		//Get binaryObject
		String hdfsDirectory = "/" + Config.getHdfsRootDir() + "/" + organizationCode + PATH_RAWDATA + "/" + dataDomain + "/" + typeDirectory + "/" + subTypeDirectory + "/";
		String pathForUri = "/pre-tenant/tnt-"+tenantCode+"/rawdata/files/"+datasetCode+"/";
		pathForUri = "/tenant/tnt-sandbox/rawdata/files/"+datasetCode+"/";

		//Get METADATA of selected file
		Boolean resultDel = false;
		try {
			//resultDel = HdfsFSUtils.deleteDir(Config.getHdfsUsername() + tenantCode, Config.getKnoxPwd(), pathForUri, Config.getKnoxUrl());
			if (Config.getHdfsLibrary().equals("webhdfs")){
				resultDel = org.csi.yucca.dataservice.binaryapi.webhdfs.HdfsFSUtils.deleteDir(Config.getKnoxUser(), Config.getKnoxPwd(), pathForUri, Config.getKnoxUrl());
			} else if (Config.getHdfsLibrary().equals("hdfs")){
				resultDel = org.csi.yucca.dataservice.binaryapi.hdfs.HdfsFSUtils.deleteDir(Config.getHdfsUsername() + tenantCode, pathForUri);
			} else {
				resultDel = org.csi.yucca.dataservice.binaryapi.localfs.LocalFSUtils.deleteDir(Config.getHdfsUsername() + tenantCode, pathForUri);
			}
			
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
	
			throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("{\"error_name\":\"Dataset not attachment (METADATA not found) " + ex.getMessage() + "\", \"error_code\":\"E112\", \"output\":\"NONE\", \"message\":\"this dataset does not accept attachments " + ex.getCause() + "\"}").build());
		}
		if (resultDel){
			Long idDataset = mdFromMongo.getIdDataset();
			
			//Get binaryObject
			List<BinaryData> binaryData = binaryDAO.readCurrentBinaryDataByIdDataset(idDataset);
			
			for (Iterator<BinaryData> binDS = binaryData.iterator(); binDS.hasNext();) {
				BinaryData binary = binDS.next();
				binaryDAO.deleteBinaryData(binary);
			}
			
			
			throw new WebApplicationException(Response.ok().build());
		} else 
			throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
					.entity("{\"error_name\":\"Dataset not found\", \"error_code\":\"E112\", \"output\":\"NONE\", \"message\":\"this dataset does not accept attachments\"}").build());
	}
	*/
	public static Map<String, String> extractMetadata(InputStream is) {

		//System.out.println("sizeFileLenght = " + sizeFileLenght.toString());
		
	    Map<String,String> map = new HashMap<String,String>();
	    BodyContentHandler contentHandler = new BodyContentHandler(0);
	    org.apache.tika.metadata.Metadata metadata = new org.apache.tika.metadata.Metadata();

        Parser parser = new AutoDetectParser();
        ParseContext parseContext = new ParseContext();
        parseContext.set(Parser.class, new ParserDecorator(parser));
        
        TikaInputStream tikaInputStream = null;
        Integer sizeFileLenght = 0;
	    try {
		    sizeFileLenght = is.available();
	        parser.parse(is, contentHandler, metadata, parseContext);
		    for (String name : metadata.names()) {
		    	if (!name.equals("X-Parsed-By"))
		    		map.put(name, metadata.get(name));
	        }
	    } catch (IOException e) {
	        for (String name : metadata.names()) {
	        	if (!name.equals("X-Parsed-By"))
		    		map.put(name, metadata.get(name));
	        }
	    } catch (SAXException e) {
	        for (String name : metadata.names()) {
	        	if (!name.equals("X-Parsed-By"))
		    		map.put(name, metadata.get(name));
	        }
	    } catch (TikaException e) {
	        for (String name : metadata.names()) {
	        	if (!name.equals("X-Parsed-By"))
		    		map.put(name, metadata.get(name));
	        }
	    } catch (Exception e) {
	        for (String name : metadata.names()) {
	        	if (!name.equals("X-Parsed-By"))
		    		map.put(name, metadata.get(name));
	        }
	    } finally {
		    map.put("sizeFileLenght", sizeFileLenght.toString());
	    	IOUtils.closeQuietly(tikaInputStream);
	    }

	    return map;
	}


}