package org.csi.yucca.adminapi.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DatasetSubtypeResponse implements Response{

	private Integer idDatasetSubtype;
	private String datasetSubtype;
	private String description;
	private Integer idDatasetType;
	public Integer getIdDatasetSubtype() {
		return idDatasetSubtype;
	}
	public void setIdDatasetSubtype(Integer idDatasetSubtype) {
		this.idDatasetSubtype = idDatasetSubtype;
	}
	public String getDatasetSubtype() {
		return datasetSubtype;
	}
	public void setDatasetSubtype(String datasetSubtype) {
		this.datasetSubtype = datasetSubtype;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Integer getIdDatasetType() {
		return idDatasetType;
	}
	public void setIdDatasetType(Integer idDatasetType) {
		this.idDatasetType = idDatasetType;
	}

	
}
