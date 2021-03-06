package com.mndk.bte_tr.map_new;

import java.util.List;

public class ExternalMapSet {
	
	private final String name;
	private final List<NewExternalMapManager> mapSet;
	
	public ExternalMapSet(String name, List<NewExternalMapManager> mapSet) {
		this.name = name;
		this.mapSet = mapSet;
	}
	
	public String getName() { 
		return name;
	}
	
	public List<NewExternalMapManager> getMaps() {
		return mapSet;
	}
	
}
