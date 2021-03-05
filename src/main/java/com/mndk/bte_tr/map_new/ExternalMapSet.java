package com.mndk.bte_tr.map_new;

import java.util.Set;

public class ExternalMapSet {
	
	private final String name;
	private final Set<NewExternalMapManager> mapSet;
	
	public ExternalMapSet(String name, Set<NewExternalMapManager> mapSet) {
		this.name = name;
		this.mapSet = mapSet;
	}
	
	public String getName() { 
		return name;
	}
	
	public Set<NewExternalMapManager> getMaps() {
		return mapSet;
	}
	
}
