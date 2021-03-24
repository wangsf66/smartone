package com.smartone.ex.mapping.busimodel.metadata;

import java.util.ArrayList;
import java.util.List;

import com.douglei.orm.mapping.metadata.AbstractMetadata;

/**
 * 
 * @author DougLei
 */
public class BusiModelMetadata extends AbstractMetadata{
	private List<RelationMetadata> relations;
	
	public BusiModelMetadata(String name, String oldName) {
		super.name=name;
		super.oldName = oldName;
	}
	public void addRelation(RelationMetadata relationMetadata) {
		if(relations == null) {
			relations = new ArrayList<RelationMetadata>();
		}
		relations.add(relationMetadata);
	}
	
	public List<RelationMetadata> getRelations() {
		return relations;
	}
	
}
