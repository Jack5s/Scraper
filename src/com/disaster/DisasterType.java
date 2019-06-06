package com.disaster;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public enum DisasterType {
	// Invalid means the data is wrong
	BioBloom, Cold, Conflict, Desertification, Drought, Earthquake, Epidemic, Flood, ForestFire, Iceberg, IDP, Landslide, Mudflow, Smog, Snow, Storm, Tsunami, Volcano, SpecialEvent;

	private static HashMap<String, DisasterType> map = new HashMap<String, DisasterType>() {
		private static final long serialVersionUID = 1L;
		{
			put("bioBloom", BioBloom);
			put("algae bloom", BioBloom);
			put("cold", Cold);
			put("cold-wave", Cold);
			put("cold spell", Cold);
			put("deep_freeze", Cold);
			put("extreme_cold", Cold);
			put("severe_cold", Cold);
			put("chill", Cold);
			put("hypothermia", Cold);
			put("ice", Cold);
			put("icy", Cold);
			put("freezing_temperatures", Cold);
			put("frigid", Cold);
			put("conflict", Conflict);
			put("eruption", Conflict);
			put("desertification", Desertification);
			put("drought", Drought);
			put("earthquake", Earthquake);
			put("epidemic", Epidemic);
			put("ebola", Epidemic);
			put("flood", Flood);
			put("drown", Flood);
			put("drowned", Flood);
			put("drowning", Flood);
			put("heavy_rain", Flood);
			put("heavy_rains", Flood);
			put("monsoon", Flood);
			put("forestFire", ForestFire);
			put("grass_fire", ForestFire);
			put("wildfire", ForestFire);
			put("wild fire", ForestFire);
			put("forest fire", ForestFire);
			put("iceberg", Iceberg);
			put("idp", IDP);
			put("population displacement", IDP);
			put("refugee camp", IDP);
			put("landslide", Landslide);
			put("mudflow", Mudflow);
			put("mass movement", Mudflow);			
			put("smog", Smog);
			put("snow", Snow);
			put("avalanche", Snow);
			put("blizzard", Snow);
			put("heavy_snow", Snow);
			put("snowfall", Snow);
			put("storm", Storm);
			put("cyclone", Storm);			
			put("tornado", Storm);
			put("tropical cyclone", Storm);
			put("tropical-cyclone", Storm);
			put("hurricane", Storm);
			put("typhoon", Storm);
			put("wind", Storm);
			put("tsunami", Tsunami);
			put("volcanic", Volcano);
			put("volcano", Volcano);
		}
	};

	public static DisasterType getDisasterTypeFromString(String disasterStr) {
		disasterStr = disasterStr.toLowerCase();
		Iterator<Entry<String, DisasterType>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, DisasterType> pair = it.next();
			String key = pair.getKey();
			int index = disasterStr.indexOf(key);
			if (index >= 0) {
				return pair.getValue();
			}
		}
		return DisasterType.SpecialEvent;
	}

	public String toString() {
		switch (this) {
		case BioBloom:
			return "BioBloom";
		case Cold:
			return "Cold";
		case Conflict:
			return "Conflict";
		case Desertification:
			return "desertification";
		case Drought:
			return "Drought";
		case Earthquake:
			return "Earthquake";
		case Epidemic:
			return "Epidemic";
		case Flood:
			return "Flood";
		case ForestFire:
			return "ForestFire";
		case Iceberg:
			return "Iceberg";
		case IDP:
			return "IDP";
		case Landslide:
			return "Landslide";
		case Mudflow:
			return "Mudflow";
		case Smog:
			return "Smog";
		case Snow:
			return "Snow";
		case Storm:
			return "Storm";
		case Tsunami:
			return "Tsunami";
		case Volcano:
			return "Volcano";
		default:
			return "SpecialEvent";
		}
	}
}
