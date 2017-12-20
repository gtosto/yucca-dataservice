package org.csi.yucca.adminapi.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.csi.yucca.adminapi.model.Api;
import org.csi.yucca.adminapi.util.Constants;

/**
 * 
 * @author gianfranco.stolfa
 *
 */
public interface ApiMapper {
	
	public static final String API_TABLE = Constants.SCHEMA_DB + "yucca_api";

	/*************************************************************************
	 * 					DELETE API
	 * ***********************************************************************/
	public static final String DELETE_API =
		"DELETE FROM " + API_TABLE + " WHERE id_data_source = #{idDataSource} AND datasourceversion = #{dataSourceVersion} AND apisubtype = #{apisubtype}";
	@Delete(DELETE_API)
	int deleteApi(@Param("idDataSource") Integer idDataSource, @Param("dataSourceVersion") Integer dataSourceVersion, @Param("apisubtype") String apisubtype);
	
	/*************************************************************************
	 * 					INSERT API
	 * ***********************************************************************/
	public static final String INSERT_API = 
	" INSERT INTO " + API_TABLE + "( apicode, apiname, apitype, apisubtype, id_data_source, datasourceversion, entitynamespace ) "
	+ "VALUES (#{apicode}, #{apiname}, #{apitype}, #{apisubtype}, #{idDataSource}, #{datasourceversion}, #{entitynamespace})";
	@Insert(INSERT_API)
	@Options(useGeneratedKeys=true, keyProperty="idapi")
	int insertApi(Api api);
	
}
