package cz.cuni.mff.d3s.demo.components;

import java.util.Map;

import cz.cuni.mff.d3s.deeco.annotations.Role;

@Role
public class SoldierRole {
	
	public Integer ensembleId;
		
	public Map<String, SoldierData> everyone;
	
	public Boolean isOnline;
	
}