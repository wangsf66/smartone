package com.smartone.ex.mapping.busimodel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import com.douglei.orm.configuration.Dom4jUtil;
import com.douglei.orm.mapping.MappingParseToolContext;
import com.douglei.orm.mapping.MappingParser;
import com.douglei.orm.mapping.MappingSubject;
import com.douglei.orm.mapping.handler.entity.AddOrCoverMappingEntity;
import com.smartone.ex.mapping.ExMappingTypeConstants;
import com.smartone.ex.mapping.busimodel.metadata.BusiModelMetadata;
import com.smartone.ex.mapping.busimodel.metadata.RelationMetadata;
import com.smartone.ex.mapping.busimodel.metadata.SqlMetadata;
import com.smartone.ex.mapping.busimodel.metadata.parser.BusiModelMetadataParser;
import com.smartone.ex.mapping.busimodel.metadata.parser.RelationMetadataParser;

/**
 * 
 * @author DougLei
 */
class BusiModelMappingParser extends MappingParser{
	private static BusiModelMetadataParser busiModelMetadataParser = new BusiModelMetadataParser();
	
	private static RelationMetadataParser relationMetadataParser = new RelationMetadataParser();
	
	private BusiModelMetadata busiModelMetadata;
	
	/**
	 * 解析
	 * @param input
	 * @return
	 * @throws Exception
	 */
	public MappingSubject parse(AddOrCoverMappingEntity entity,InputStream input) throws Exception {
		Document document = MappingParseToolContext.getMappingParseTool().getSAXReader().read(input);
		Element  parent  = document.getRootElement();
		Element busimodeElement = Dom4jUtil.getElement(ExMappingTypeConstants.BUSIMODEL,parent);
		busiModelMetadata = busiModelMetadataParser.parse(busimodeElement);
		getBusiModeElements(busimodeElement);
		return buildMappingSubjectByDom4j(entity.isEnableProperty(),new BusiModelMapping(busiModelMetadata),parent);
		//return new BusiModelMapping(busiModelMetadata);
	}

	/**
	 * 解析业务子资源
	 * @param busimodeElement
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<Element> getBusiModeElements(Element busimodeElement) {
		// 解析<relations>
		Element relationsElement = Dom4jUtil.getElement("relations",busimodeElement); 
		List<Element> relationElements = relationsElement.elements("relation");
	
		if(relationElements.isEmpty()) 
			throw new NullPointerException("<relations>元素下至少配置一个<relation>元素");
		RelationMetadata relationMetadata = null;
		Element childrenElement = null;
		for(Element relationElement:relationElements) {
			//解析业务子资源
			relationMetadata = relationMetadataParser.parse(relationElement);
			//赋值sqllist
			relationMetadata.setSqlList(addSqlList(relationElement,relationMetadata.getRefResourceType()));
			childrenElement = relationElement.element("childlist");
			List<Element> childrenRelationList = null;
			if(childrenElement!=null) {
				childrenRelationList= childrenElement.elements("relation");
				if(childrenRelationList.size()>0) {
					buildChilTree(relationMetadata,childrenRelationList);
				}
			}
			//将子资源赋值给主资源
			busiModelMetadata.addRelation(relationMetadata);
		}
		return relationElements;
	}
	
	// 递归，建立子树形结构
	@SuppressWarnings("unchecked")
	private void buildChilTree(RelationMetadata relationMetadataRoot,List<Element> childrenElement) {
		Element chilMenus = null;
		List<RelationMetadata> relationMetadataList = new ArrayList<RelationMetadata>();
		RelationMetadata relationMetadata = null;
		for (Element element : childrenElement) {
			//解析业务子资源
			relationMetadata = relationMetadataParser.parse(element);
			//赋值sqllist
			relationMetadata.setSqlList(addSqlList(element,relationMetadata.getRefResourceType()));
			chilMenus = element.element("childlist");
			List<Element> childrenRelationList = null;
			if(chilMenus!=null) {
				childrenRelationList= chilMenus.elements("relation");
				if(childrenRelationList.size()>0) {
					buildChilTree(relationMetadata,childrenRelationList);
				}
			}
			relationMetadataList.add(relationMetadata);
		}
		relationMetadataRoot.setChildrenList(relationMetadataList);
	}
	
	@SuppressWarnings("unchecked")
	private List<SqlMetadata> addSqlList(Element relationElement,String refResourceType) {
		if("20".equals(refResourceType)) {
			Element sqlListElement = Dom4jUtil.getElement("sqllist",relationElement); 
			List<Element> sqlElements = sqlListElement==null?Collections.emptyList():sqlListElement.elements("sql");
			List<SqlMetadata> sqllist = new ArrayList<SqlMetadata>();
			SqlMetadata sqlMetadata = null;
			for(Element sqlElement:sqlElements) {
				sqlMetadata = new SqlMetadata(sqlElement.attributeValue("name"),sqlElement.attributeValue("type"),sqlElement.attributeValue("oldName"),sqlElement.attributeValue("sqlType"));
				sqllist.add(sqlMetadata);
			}
			return sqllist;
		}
		return null;
	}

}
