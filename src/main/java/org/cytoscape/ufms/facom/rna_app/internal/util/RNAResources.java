package org.cytoscape.ufms.facom.rna_app.internal.util;

import java.net.URL;

public class RNAResources {

	public static enum ImageName {
		LOGO("/img/LOGO.png"),
		LOGO_SIMPLE("/img/LOGO_SIMPLE.png"),
		LOGO_SMALL("/img/LOGO_SMALL.png");

		private final String name;

		private ImageName(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public static URL getUrl(ImageName img) {
		return RNAResources.class.getResource(img.toString());
	}	
	
}
