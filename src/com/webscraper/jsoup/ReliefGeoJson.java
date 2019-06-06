package com.webscraper.jsoup;

//the class is for parse the geojson in Relief website
public class ReliefGeoJson {
	public Data data;
	public Options options;

	public class Data {
		public String type;
		public Feature1[] features;
		public Properties1 properties;

		public class Feature1 {
			public String type;
			public Feature2[] features;
			public Properties2 properties;

			public class Feature2 {
				public String type;
				public Geometry geometry;

				public class Geometry {
					public String type;
					public double[] coordinates;
				}
			}

			public class Properties2 {
				public String url;
				public String name;
				public String icon;
				public String color;
				public String description;
			}
		}

		public class Properties1 {
			public String locationType;
		}
	}

	public class Options {
		public class icon {
			public int[] iconSize;
			public int[] iconAnchor;
			public int[] popupAnchor;
			public int[] shadowAnchor;
			public int[] shadowSize;
			String icon;
			String color;
		}

	}
}
