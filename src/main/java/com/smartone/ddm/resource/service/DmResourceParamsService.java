package com.smartone.ddm.resource.service;

import java.util.List;

import com.douglei.orm.context.SessionContext;
import com.douglei.orm.context.Transaction;
import com.douglei.orm.mapping.validator.ValidateFailResult;
import com.douglei.orm.sessionfactory.sessions.Session;
import com.ibs.code.result.DataValidationResult;
import com.ibs.code.service.BasicService;
import com.ibs.code.service.ServiceValidator;
import com.ibs.components.response.ResponseContext;
import com.smartone.ddm.resource.entity.DmResource;
import com.smartone.ddm.resource.entity.DmResourceParam;
import com.smartone.ddm.resource.validation.ColumnLengthCannotBeShortedValidationResult;
import com.smartone.ddm.resource.validation.ColumnPrecisionCannotBeShortedValidationResult;
import com.smartone.ddm.resource.validation.DataTypeNotModifiedValidationResult;
import com.smartone.ddm.resource.validation.DefaultValueNotNullValidationResult;
import com.smartone.ddm.resource.validation.DefaultValueValidationResult;
import com.smartone.ddm.resource.validation.IsUniqueValidationResult;
import com.smartone.ddm.resource.validation.NullableValidationResult;
import com.smartone.ddm.resource.validation.ParamUniqueValidationResult;
import com.smartone.ddm.resource.validation.TableNothingValidationResult;
/**
 * 
 * @author wangShuFang
 */
public class DmResourceParamsService extends BasicService {
	
	private static final ResourceUniqueValidator resourceUniqueValidator = new ResourceUniqueValidator();
	
	private static final ResourceParamValidator resourceParamValidator = new ResourceParamValidator();
	
	@Transaction
	public void insertMany(List<DmResourceParam> list) {
		if (validateByValidator(list, resourceUniqueValidator) == DataValidationResult.SUCCESS) {
			for(DmResourceParam dmResourceParam:list) {
				SessionContext.getTableSession().save(setBasicPropertyValues(dmResourceParam,true));
			}
			DmResource dmResource = SessionContext.getTableSession().uniqueQuery(DmResource.class,
					"SELECT * FROM DM_RESOURCE WHERE ID='"+list.get(0).getResourceId()+"'");
			dmResource.setIsBuildModel(0);
			SessionContext.getTableSession().update(setBasicPropertyValues(dmResource,false));
			cancelmodel(dmResource);
			ResponseContext.addData(list);
		}
	}
	 
	@Transaction		
	public void updateMany(List<DmResourceParam> list) {
		for(DmResourceParam dmResourceParams:list) {
			updateResourceParams(dmResourceParams);
		}
		ResponseContext.addData(list);
	}
	
	public void updateResourceParams(DmResourceParam dmResourceParams) {
		DmResource dmResource  = SessionContext.getTableSession().uniqueQuery(DmResource.class,
				"select * from DM_RESOURCE WHERE ID='" + dmResourceParams.getResourceId() + "'");
		if(dmResource.getIsBuildModel()==0) {
			if (validateByValidator(dmResourceParams, resourceUniqueValidator) == DataValidationResult.SUCCESS) {
				SessionContext.getTableSession().update(setBasicPropertyValues(dmResourceParams,false));
			}
		}else {
			//查找nameSpase，重复，tableId不同
			DmResourceParam oldResourceParams = SessionContext.getTableSession().uniqueQuery(DmResourceParam.class,
					"select * from DM_RESOURCE_PARAMS WHERE ID='" + dmResourceParams.getId() + "'");
			if (oldResourceParams == null) {
				ResponseContext.addValidationFull(dmResourceParams, "dmResourceParams", "不存在此参数","smartone.dynamic.param.notExists");
			    return;
			}
			List<DmResourceParam> obj =(List<DmResourceParam>)SessionContext.getSqlSession().query(DmResourceParam.class,"select * from DM_RESOURCE_PARAMS WHERE PARAM_NAME='"+dmResourceParams.getParamName()+"' AND ID !='"+dmResourceParams.getId()+"' AND RESOURCE_ID ='"+dmResourceParams.getResourceId()+"'");
			if(obj!=null&&obj.size()>0) {
				ResponseContext.addValidationFull(dmResourceParams, "paramName", "不可插入重复的列名","smartone.resource.insert.uniqueParamName");
			    return;
			}
			if (dmResourceParams.getParamName().equals(oldResourceParams.getParamName())) {
				if (validateByValidator(dmResourceParams,resourceParamValidator) == DataValidationResult.SUCCESS) {
					updateTableBuildModel(dmResourceParams,oldResourceParams,dmResource);
				}
			} else {
				if(dmResource.getIsBuildModel()==1&&oldResourceParams.getOldParamName()==null) {
					dmResourceParams.setOldParamName(oldResourceParams.getParamName()); 
				}
				if (validateByValidator(dmResourceParams,resourceUniqueValidator,resourceParamValidator) == DataValidationResult.SUCCESS) {
					updateTableBuildModel(dmResourceParams,oldResourceParams,dmResource);
				}
			}
		}
    }
	
	//修改列信息，将对应表状态改为重新建
	public void updateTableBuildModel(DmResourceParam dmResourceParams,DmResourceParam oldResourceParams,DmResource dmResource) {
		if (!oldResourceParams.getParamName().equals(dmResourceParams.getParamName())
				|| !oldResourceParams.getIsUnique().equals(dmResourceParams.getIsUnique())
				|| !oldResourceParams.getIsNullable().equals(dmResourceParams.getIsNullable())
				|| !oldResourceParams.getIsValidate().equals(dmResourceParams.getIsValidate())
				|| !oldResourceParams.getLength().equals(dmResourceParams.getLength())
				|| !oldResourceParams.getDefaultValue().equals(dmResourceParams.getDefaultValue())) {
			//当参数列名等信息发生变化时，修改所属表的建模状态
			dmResource.setIsBuildModel(0);
		}
		SessionContext.getTableSession().update(setBasicPropertyValues(dmResource,false));
		SessionContext.getTableSession().update(setBasicPropertyValues(dmResourceParams,false));
		cancelmodel(dmResource);
	}
	

	@Transaction
	public void delete(String ids) {
		DmResource dmResource = null;
		String[] idsArray = ids.split(",");
		dmResource = SessionContext.getTableSession().uniqueQuery(DmResource.class,
				"SELECT * FROM DM_RESOURCE WHERE ID=(select RESOURCE_ID FROM DM_RESOURCE_PARAMS WHERE ID='"+idsArray[0]+"')");
		deleteByIds("DM_RESOURCE_PARAMS","ID",ids);
		dmResource.setIsBuildModel(0);
		SessionContext.getTableSession().update(setBasicPropertyValues(dmResource,false));
		cancelmodel(dmResource);
	}
	
	public void cancelmodel(DmResource dmResource){
		//修改mapping为失效，table不可以从容器中移除
		SessionContext.getSqlSession().executeUpdate("update DM_RESOURCE_MAPPING set IS_FAILURE=1 where RESOURCE_ID = '"+dmResource.getId()+"'");
		DmResource dmBusiResource = SessionContext.getTableSession().uniqueQuery(DmResource.class,"select * from DM_RESOURCE where id  in(select REF_BUSI_MODEL_ID from DM_CFG_BUSI_MODEL_RES_RELATIONS where REF_RESOURCE_ID='"+dmResource.getId()+"')");
	    if(dmBusiResource!=null&&dmBusiResource.getIsBuildModel()==1) {
	    	dmBusiResource.setIsBuildModel(0);
	    	SessionContext.getTableSession().update(setBasicPropertyValues(dmBusiResource,false));
	    }
	}
}

//在添加时验证同一张表中Cname是否唯一
class ResourceUniqueValidator implements ServiceValidator<DmResourceParam> {
	@Override
	public ValidateFailResult validate(int arg0,DmResourceParam dmResourceParams, Session session, String arg3, String arg4, String arg5) {
		DmResourceParam drp = session.getSqlSession().uniqueQuery(DmResourceParam.class,"select * from DM_RESOURCE_PARAMS WHERE RESOURCE_ID='"+dmResourceParams.getResourceId()+"' and PARAM_NAME='"+dmResourceParams.getParamName().toUpperCase()+"'");
		//表中的列名唯一验证
		if(drp!=null&&drp.getParamName().equals(dmResourceParams.getParamName())) {
			return new ParamUniqueValidationResult("paramName", "参数值不唯一, 已存在相同值的数据", "smartone.value.violation.unique.paramsValue");
		}
		
		if(dmResourceParams.getLength()==null) {
			return new ColumnLengthCannotBeShortedValidationResult("length","属性长度不可为null","smartone.value.violation.lengthNotNull");
		}
		
		if(drp!=null&&dmResourceParams.getDefaultValue()=="") {
			return new DefaultValueNotNullValidationResult("defaultValue","属性默认值不可以是空字符串","smartone.value.violation.DefaultValueNotNull");
		}
			
		if(drp!=null&&dmResourceParams.getResourceId()!=null) {
			DmResource dmResource  = (DmResource)session.getSqlSession().uniqueQuery(DmResource.class,"select * from DM_RESOURCE WHERE ID='"+dmResourceParams.getResourceId()+"'");
			//关联的外键对象不可以不存在
		    if(dmResource==null) {
		    	return new TableNothingValidationResult("resourceId", "关联的表信息不存在","smartone.value.violation.TableNothing");
		    }
		}
		
	    //当列存在唯一约束时，值不能为空
	    if(dmResourceParams.getIsUnique()!=null&&dmResourceParams.getIsUnique()==1) {
	    	if(dmResourceParams.getIsNullable()!=null&&dmResourceParams.getIsNullable()==1) {
	    		return new NullableValidationResult("isNullable","当列有唯一约束时，值不可以为空。", "smartone.value.violation.Nullable");
	    	}
	    }
	    //数据类型是integer和double时才可以设置精度值
//	    if(dmResourceParams.getDatatype()!=null&&(!"integer".equals(dmResourceParams.getDatatype())&&!"double".equals(dmResourceParams.getDatatype()))) {
//	    	if(dmResourceParams.getPrecision()!=null&&dmResourceParams.getPrecision()!=0) {
//	    		return new PrecisionValidationResult("precision","当属性类型不是integer和double时，不可以为属性添加精度","smartone.value.violation.Precision");
//	    	}
//	    }
	    //数据类型是clob和blob大字段时，不能添加唯一约束
	    if(dmResourceParams.getDatatype()!=null&&("clob".equals(dmResourceParams.getDatatype())||"blob".equals(dmResourceParams.getDatatype()))) {
	    	if(dmResourceParams.getIsUnique()!=null&&dmResourceParams.getIsUnique()==1) {
	    		return new IsUniqueValidationResult("isUnique","属性类型是大字段时，不可添加唯一约束。", "smartone.value.violation.IsUnique");
	    	}
	    }
	    //数据类型date时，不能赋默认值
	    if(dmResourceParams.getDatatype()!=null&&"date".equals(dmResourceParams.getDatatype())) {
	    	if(dmResourceParams.getDefaultValue()!=null) {
	    		return new DefaultValueValidationResult("defaultValue","属性类型为date类型时，不可以添加默认值。",  "smartone.value.violation.DefaultValue");
	    	}
	    }
		return null;
	}
	
	
}

//修改列信息验证
class ResourceParamValidator implements ServiceValidator<DmResourceParam> {

	@Override
	public ValidateFailResult validate(int arg0,DmResourceParam dmResourceParams, Session session, String arg3, String arg4, String arg5) {
			DmResourceParam obj =session.getSqlSession().uniqueQuery(DmResourceParam.class,"select * from DM_RESOURCE_PARAMS WHERE ID='"+dmResourceParams.getId()+"'");
			if(obj!=null&&dmResourceParams.getDatatype()!=null&&!obj.getDatatype().equals(dmResourceParams.getDatatype())){
				return new DataTypeNotModifiedValidationResult("datatype", "属性datatype的值不可以被改变", "smartone.value.violation.DataTypeNotModified");
			}
			if(obj!=null&&dmResourceParams.getLength()!=null&&obj.getLength()>dmResourceParams.getLength()){
				return new ColumnLengthCannotBeShortedValidationResult("length","属性length的值不可以缩小","smartone.value.violation.ColumnLengthCannotBeShorted");
			}
			if(obj!=null&&dmResourceParams.getPrecision()!=null&&obj.getPrecision()>dmResourceParams.getPrecision()) {
				return new ColumnPrecisionCannotBeShortedValidationResult("precision","属性精度的值不可以缩小","smartone.value.violation.ColumnPrecisionCannotBeShorted");
			}

			if(dmResourceParams.getResourceId()!=null) {
				DmResource dmResource  = (DmResource)session.getSqlSession().uniqueQuery(DmResource.class,"select * from DM_RESOURCE WHERE ID='"+dmResourceParams.getResourceId()+"'");
				//关联的外键对象不可以不存在
			    if(dmResource==null) {
			    	return new TableNothingValidationResult("resourceId", "关联的表信息不存在","smartone.value.violation.TableNothing");
			    }
			}
			
			//当列存在唯一约束时，值不能为空
		    if(dmResourceParams.getIsUnique()!=null&&dmResourceParams.getIsUnique()==1) {
		    	if(dmResourceParams.getIsNullable()!=null&&dmResourceParams.getIsNullable()==1) {
		    		return new NullableValidationResult("isNullable","当列有唯一约束时，值不可以为空。", "smartone.value.violation.Nullable");
		    	}
		    }
		    //数据类型是integer和double时才可以设置精度值
//		    if(!"integer".equals(dmResourceParams.getDatatype())&&!"double".equals(dmResourceParams.getDatatype())) {
//		    	if(dmResourceParams.getPrecision()!=null&&dmResourceParams.getPrecision()!=0) {
//		    		return new PrecisionValidationResult("precision","当属性类型不是integer和double时，不可以为属性添加精度","smartone.value.violation.Precision");
//		    	}
//		    }
		    //数据类型是clob和blob大字段时，不能添加唯一约束
		    if("clob".equals(dmResourceParams.getDatatype())||"blob".equals(dmResourceParams.getDatatype())) {
		    	if(dmResourceParams.getIsUnique()!=null&&dmResourceParams.getIsUnique()==1) {
		    		return new IsUniqueValidationResult("isUnique","属性类型是大字段时，不可添加唯一约束。", "smartone.value.violation.IsUnique");
		    	}
		    }
		    //数据类型date时，不能赋默认值
		    if("date".equals(dmResourceParams.getDatatype())) {
		    	if(dmResourceParams.getDefaultValue()!=null) {
		    		return new DefaultValueValidationResult("defaultValue","属性类型为date类型时，不可以添加默认值。",  "smartone.value.violation.DefaultValue");
		    	}
		    }
			return null;
	}
}
