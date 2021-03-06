package com.mndk.bte_tr.map;

import java.util.List;

public class ExternalTileMapSet {
	
	private final String name;
	private final List<ExternalTileMap> mapSet;
	
	public ExternalTileMapSet(String name, List<ExternalTileMap> mapSet) {
		this.name = name;
		this.mapSet = mapSet;
	}
	
	public String getName() { 
		return name;
	}
	
	public List<ExternalTileMap> getMaps() {
		return mapSet;
	}
	
}
