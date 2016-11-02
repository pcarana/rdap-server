package mx.nic.rdap.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import mx.nic.rdap.server.db.model.ZoneModel;
import mx.nic.rdap.server.exception.ObjectNotFoundException;

/**
 * Class containing the configuration of the rdap server
 * 
 * @author dalpuche
 *
 */
public class RdapConfiguration {
	private final static Logger logger = Logger.getLogger(RdapConfiguration.class.getName());
	private static Properties systemProperties;
	private static final String LANGUAGE_KEY = "language";
	private static final String ZONE_KEY = "zones";
	private static final String MINIMUN_SEARCH_PATTERN_LENGTH_KEY = "minimum.search.pattern.length";
	private static final String MAX_NUMBER_OF_RESULTS_FOR_AUTHENTICATED_USER = "max.number.result.authenticated.user";
	private static final String MAX_NUMBER_OF_RESULTS_FOR_UNAUTHENTICATED_USER = "max.number.result.unauthenticated.user";

	public RdapConfiguration() {
	}

	/**
	 * @return the systemProperties
	 */
	public static Properties getSystemProperties() {
		return systemProperties;
	}

	/**
	 * Load the parameters defined in the configuration file
	 * 
	 * @param systemProperties
	 *            the systemProperties to set
	 * @throws ObjectNotFoundException
	 */
	public static void loadSystemProperties(Properties systemProperties) throws ObjectNotFoundException {
		RdapConfiguration.systemProperties = systemProperties;
		validateConfiguratedZones();
	}

	/**
	 * Return the server language defined in the configuration file
	 * 
	 * @return
	 */
	public static String getServerLanguage() {
		String lang = "en";// default:English
		if (systemProperties.containsKey(LANGUAGE_KEY))
			lang = systemProperties.getProperty(LANGUAGE_KEY);
		else {
			logger.log(Level.WARNING, "Language not found in configuration file. Using default: English");
		}
		return lang.trim();
	}

	/**
	 * Return the list of zones defined in the configuration file
	 * 
	 * @return
	 */
	public static List<String> getServerZones() {
		if (systemProperties.containsKey(ZONE_KEY)) {
			String zones[] = systemProperties.getProperty(ZONE_KEY).trim().split(",");
			List<String> trimmedZones = new ArrayList<String>();
			for (String zone : zones) {
				trimmedZones.add(zone.trim());
			}
			return trimmedZones;
		} else {
			logger.log(Level.SEVERE, "Zones not found in configuration file");
			throw new RuntimeException("MUST defined zones in configuration file");
		}
	}

	/**
	 * Return the minimum search pattern length defined in configuration file
	 * 
	 * @return
	 */
	public static int getMinimumSearchPatternLength() {
		int length = 5;// default 5
		if (systemProperties.containsKey(MINIMUN_SEARCH_PATTERN_LENGTH_KEY))
			try {
				length = Integer.parseInt(systemProperties.getProperty(MINIMUN_SEARCH_PATTERN_LENGTH_KEY).trim());
			} catch (NumberFormatException nfe) {
				logger.log(Level.WARNING,
						"Minimum search pattern length not found in configuration file. Using default: 5");
			}
		return length;
	}

	/**
	 * Return the max number of results for the authenticated user
	 * 
	 * @return
	 */
	public static int getMaxNumberOfResultsForAuthenticatedUser() {
		int maxResults = 20;// default 20
		if (systemProperties.containsKey(MAX_NUMBER_OF_RESULTS_FOR_AUTHENTICATED_USER))
			try {
				maxResults = Integer
						.parseInt(systemProperties.getProperty(MAX_NUMBER_OF_RESULTS_FOR_AUTHENTICATED_USER).trim());
			} catch (NumberFormatException nfe) {
				logger.log(Level.WARNING,
						"Max number of results for the authenticated user not found in configuration file. Using default: 20");
			}
		return maxResults;
	}

	/**
	 * Return the max number of results for the authenticated user
	 * 
	 * @return
	 */
	public static int getMaxNumberOfResultsForUnauthenticatedUser() {
		int maxResults = 10;// default 10
		if (systemProperties.containsKey(MAX_NUMBER_OF_RESULTS_FOR_UNAUTHENTICATED_USER))
			try {
				maxResults = Integer
						.parseInt(systemProperties.getProperty(MAX_NUMBER_OF_RESULTS_FOR_UNAUTHENTICATED_USER).trim());
			} catch (NumberFormatException nfe) {
				logger.log(Level.WARNING,
						"Max number of results for the unauthenticated user not found in configuration file. Using default: 10");
			}
		return maxResults;
	}

	/**
	 * Validate if the configurated zones are in the database
	 * 
	 * @throws ObjectNotFoundException
	 */
	public static void validateConfiguratedZones() throws ObjectNotFoundException {
		ZoneModel.validateConfiguratedZones();
	}

}