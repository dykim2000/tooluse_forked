package example;

import java.io.File;

import com.anji.util.Properties;

/**
 * Centralizes runtime configuration derived from the selected properties file.
 */
public final class RunConfig {
	public static final String DEFAULT_PROPERTIES_FILE = "example.properties";

	private RunConfig() {
	}

	public static String getPropertiesFilename(String[] args, int index, String fallback) {
		if (args != null && args.length > index && args[index] != null && args[index].trim().length() > 0) {
			return args[index].trim();
		}
		return fallback;
	}

	public static String getPrefixFromPropertiesFilename(String propertiesFilename) {
		String fileName = new File(propertiesFilename).getName();
		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex > 0) {
			return fileName.substring(0, dotIndex);
		}
		return fileName;
	}

	public static File ensureResultsRoot(String prefix) {
		File root = new File("./db/" + prefix);
		if (!root.exists()) {
			root.mkdirs();
		}
		return root;
	}

	public static void applyOutputOverrides(Properties props, String prefix) {
		String baseDir = "./db/" + prefix;
		props.setProperty("persistence.base.dir", baseDir);
		props.setProperty("id.file", baseDir + "/id.xml");
		props.setProperty("neat.id.file", baseDir + "/neatid.xml");
		props.setProperty("presentation.dir", baseDir + "/nevt");
	}
}
