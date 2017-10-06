package org.csi.yucca.adminapi.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.csi.yucca.adminapi.model.Tenant;
import org.csi.yucca.adminapi.util.Constants;

/**
 * 
 * @author gianfranco.stolfa
 *
 */
public interface TenantMapper {
	
	
	String TENANT_TABLE = Constants.SCHEMA_DB + ".yucca_tenant";
	String R_TENANT_BUNDLES_TABLE = Constants.SCHEMA_DB + ".yucca_r_tenant_bundles";
	
	public static final String SELECT_TENANT_COLUMNS = 	
			" SELECT TENANT.id_tenant, tenantcode, name, description, clientkey, " + 
			" clientsecret, activationdate, deactivationdate, usagedaysnumber, " + 
			" userfirstname, userlastname, useremail, usertypeauth, creationdate, " + 
			" expirationdate, id_ecosystem, TENANT.id_organization, id_tenant_type, " + 
			" id_tenant_status, datasolrcollectionname, measuresolrcollectionname, " + 
			" mediasolrcollectionname, socialsolrcollectionname, dataphoenixtablename, " + 
			" dataphoenixschemaname, measuresphoenixtablename, measuresphoenixschemaname, " + 
			" mediaphoenixtablename, mediaphoenixschemaname, socialphoenixtablename, " + 
			" socialphoenixschemaname, id_share_type ";
	
	
	/*************************************************************************
	 * 
	 * 					SELECT TENANT BY TENANT CODE
	 * 
	 * ***********************************************************************/
	public static final String SELECT_TENANT_BY_TENANT_CODE =
	SELECT_TENANT_COLUMNS + " FROM " + TENANT_TABLE + " TENANT " + 
	" WHERE  tenantcode = #{tenantCode}";
	@Results({
        @Result(property = "idTenant",       column = "id_tenant"),
        @Result(property = "idEcosystem",    column = "id_ecosystem"),
        @Result(property = "idOrganization", column = "id_organization"),
        @Result(property = "idTenantType",   column = "id_tenant_type"),
        @Result(property = "idTenantStatus", column = "id_tenant_status"),
        @Result(property = "idShareType",    column = "id_share_type")
      })	
	@Select(SELECT_TENANT_BY_TENANT_CODE) 
	Tenant selectTenantByTenantCode(@Param("tenantCode") String tenantCode);
	
	/*************************************************************************
	 * 
	 * 					SELECT TENANT BY USERNAME
	 * 
	 * ***********************************************************************/
	public static final String SELECT_ACTIVE_TENANT_BY_USERNAME_AND_ID_TENANT_TYPE =
	SELECT_TENANT_COLUMNS +
	" FROM " + TENANT_TABLE + " TENANT, " + UserMapper.USER_TABLE + " USERS, " + UserMapper.R_TENANT_USERS_TABLE + " TENANT_USERS " + 
	" WHERE TENANT.id_tenant_type = #{idTenantType} AND " +
	" TENANT.activationdate <= current_timestamp AND " +
	" (TENANT.deactivationdate > current_timestamp or TENANT.deactivationdate is null) AND " +
	" USERS.username = #{username} AND " +
	" USERS.id_user = TENANT_USERS.id_user AND " +
	" TENANT.id_tenant = TENANT_USERS.id_tenant ";
	@Results({
        @Result(property = "idTenant",       column = "id_tenant"),
        @Result(property = "idEcosystem",    column = "id_ecosystem"),
        @Result(property = "idOrganization", column = "id_organization"),
        @Result(property = "idTenantType",   column = "id_tenant_type"),
        @Result(property = "idTenantStatus", column = "id_tenant_status"),
        @Result(property = "idShareType",    column = "id_share_type")
      })	
	@Select(SELECT_ACTIVE_TENANT_BY_USERNAME_AND_ID_TENANT_TYPE) 
	List<Tenant> selectActiveTenantByUserNameAndIdTenantType(@Param("username") String username, @Param("idTenantType") Integer idTenantType);

	
	/*************************************************************************
	 * 
	 * 					SELECT ORGANIZATION BY ID TENANT
	 * 
	 * ***********************************************************************/
	public static final String SELECT_ORGANIZATION_BY_ID_TENANT = 
			"SELECT id_organization FROM " + TENANT_TABLE + " where id_tenant = #{idTenant}";
	@Select(SELECT_ORGANIZATION_BY_ID_TENANT) 
	Integer selectIdOrganizationByIdTenant(@Param("idTenant") int idTenant);

	
	/*************************************************************************
	 * 
	 * 					SELECT ID TENANT BY ID ORGANIZATION
	 * 
	 * ***********************************************************************/
	public static final String SELECT_ID_TENANT_BY_ID_ORGANIZATION = "SELECT id_tenant FROM " + TENANT_TABLE
	+ " where id_organization = #{idOrganization}";
	@Select(SELECT_ID_TENANT_BY_ID_ORGANIZATION) 
	List<Integer> selectIdTenantByIdOrganization(@Param("idOrganization") int idOrganization);
	
	
	/*************************************************************************
	 * 
	 * 					UPDATE TENANT STATUS
	 * 
	 * ***********************************************************************/	
	public static final String UPDATE_TENANT_STATUS = 
			"UPDATE " + TENANT_TABLE + " set id_tenant_status = #{idTenantStatus} where tenantcode = #{tenantCode}";
		@Update(UPDATE_TENANT_STATUS)
		int updateTenantStatus(@Param("idTenantStatus") Integer idTenantStatus, @Param("tenantCode") String tenantCode);
	
	
	/*************************************************************************
	 * 
	 * 					INSERT TENANT
	 * 
	 * ***********************************************************************/
	
	public static final String INSERT_TENANT = 
		"INSERT INTO " + TENANT_TABLE +
			" ( creationdate, expirationdate, activationdate, deactivationdate, id_share_type, " +
			" tenantcode, name, description, clientkey, clientsecret, usagedaysnumber, " +
			" userfirstname, userlastname, useremail, usertypeauth, id_ecosystem, " +
			" id_organization, id_tenant_type, id_tenant_status, datasolrcollectionname, " +
			" measuresolrcollectionname, mediasolrcollectionname, socialsolrcollectionname, " +
			" dataphoenixtablename, dataphoenixschemaname, measuresphoenixtablename, " +
			" measuresphoenixschemaname, mediaphoenixtablename, mediaphoenixschemaname, " +
			" socialphoenixtablename, socialphoenixschemaname ) " +
			" VALUES (	#{creationdate}, #{expirationdate}, #{activationdate}, #{deactivationdate}, #{idShareType}, " +
			" #{tenantcode}, #{name}, #{description}, #{clientkey}, #{clientsecret}, #{usagedaysnumber}, " +
			" #{userfirstname}, #{userlastname}, #{useremail}, #{usertypeauth}, #{idEcosystem}, " +
			" #{idOrganization}, #{idTenantType}, #{idTenantStatus}, #{datasolrcollectionname}, " +
			" #{measuresolrcollectionname}, #{mediasolrcollectionname}, #{socialsolrcollectionname}, " +
			" #{dataphoenixtablename}, #{dataphoenixschemaname}, #{measuresphoenixtablename}, " +
			" #{measuresphoenixschemaname}, #{mediaphoenixtablename}, #{mediaphoenixschemaname}, " +
			" #{socialphoenixtablename}, #{socialphoenixschemaname})";
	@Insert(INSERT_TENANT)
	@Options(useGeneratedKeys=true, keyProperty="idTenant")
	int insertTenant(Tenant tenant);
	
	
	/*************************************************************************
	 * 
	 * 					INSERT TENANT BUNDLES
	 * 
	 * ***********************************************************************/	
	public static final String INSERT_TENANT_BUNDLES = 
			"INSERT INTO " + R_TENANT_BUNDLES_TABLE + "(id_tenant, id_bundles)VALUES (#{idTenant}, #{idBundles})";
	@Insert(INSERT_TENANT_BUNDLES)
	int insertTenantBundles(@Param("idTenant") int idTenant, @Param("idBundles") int idBundles);
	
	
	/*************************************************************************
	 * 
	 * 					DELETE TENANT
	 * 
	 * ***********************************************************************/
	public static final String DELETE_TENANT = "DELETE FROM " + TENANT_TABLE + " WHERE tenantcode=#{tenantcode}";
	@Delete(DELETE_TENANT)
	int deleteTenant(String tenantcode);	
	
}