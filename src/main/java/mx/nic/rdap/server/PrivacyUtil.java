package mx.nic.rdap.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PrivacyUtil {

	private final static Logger logger = Logger.getLogger(PrivacyUtil.class.getName());

	private static final Map<String, Map<String, PrivacyStatus>> OBJECTS_PRIVACY_SETTING = new HashMap<>();

	/** Path where the default properties are read */
	private static final String DEFAULT_PATH = "META-INF/privacy_default/";

	/** Path where the user properties are read */
	private static final String USER_PATH = "META-INF/privacy/";

	// ***** Names of the properties files *****
	private static final String ENTITY = "entity";
	private static final String ENTITY_PUBLIC_ID = "entity_public_id";
	private static final String ENTITY_LINKS = "entity_links";
	private static final String ENTITY_REMARKS = "entity_remarks";
	private static final String ENTITY_EVENTS = "entity_events";

	private static final String DOMAIN = "domain";
	private static final String DOMAIN_PUBLIC_ID = "domain_public_id";
	private static final String DOMAIN_VARIANTS = "domain_variants";
	private static final String DOMAIN_LINKS = "domain_links";
	private static final String DOMAIN_REMARKS = "domain_remarks";
	private static final String DOMAIN_EVENTS = "domain_events";
	private static final String SECURE_DNS = "secure_dns";
	private static final String DS_DATA = "ds_data";

	private static final String NAMESERVER = "nameserver";
	private static final String NAMESERVER_LINKS = "nameserver_links";
	private static final String NAMESERVER_REMARKS = "nameserver_remarks";
	private static final String NAMESERVER_EVENTS = "nameserver_events";

	private static final String AUTNUM = "autnum";
	private static final String AUTNUM_LINKS = "autnum_links";
	private static final String AUTNUM_REMARKS = "autnum_remarks";
	private static final String AUTNUM_EVENTS = "autnum_events";

	// ***** End of names of the properties files *****

	public static void loadAllPrivacySettings() throws IOException {
		loadObjectPrivacySettings(ENTITY);
		loadObjectPrivacySettings(ENTITY_PUBLIC_ID);
		loadObjectPrivacySettings(ENTITY_LINKS);
		loadObjectPrivacySettings(ENTITY_EVENTS);
		loadObjectPrivacySettings(ENTITY_REMARKS);

		loadObjectPrivacySettings(DOMAIN);
		loadObjectPrivacySettings(DOMAIN_PUBLIC_ID);
		loadObjectPrivacySettings(DOMAIN_VARIANTS);
		loadObjectPrivacySettings(DOMAIN_LINKS);
		loadObjectPrivacySettings(DOMAIN_EVENTS);
		loadObjectPrivacySettings(DOMAIN_REMARKS);
		loadObjectPrivacySettings(SECURE_DNS);
		loadObjectPrivacySettings(DS_DATA);

		loadObjectPrivacySettings(NAMESERVER);
		loadObjectPrivacySettings(NAMESERVER_LINKS);
		loadObjectPrivacySettings(NAMESERVER_EVENTS);
		loadObjectPrivacySettings(NAMESERVER_REMARKS);

		loadObjectPrivacySettings(AUTNUM);
		loadObjectPrivacySettings(AUTNUM_LINKS);
		loadObjectPrivacySettings(AUTNUM_EVENTS);
		loadObjectPrivacySettings(AUTNUM_REMARKS);

	}

	private static void loadObjectPrivacySettings(String objectName) throws IOException {
		Properties properties = new Properties();
		ClassLoader classLoader = PrivacyUtil.class.getClassLoader();
		HashMap<String, PrivacyStatus> objectProperties = new HashMap<>();
		try (InputStream in = classLoader.getResourceAsStream(DEFAULT_PATH + objectName + ".properties");) {
			properties.load(in);
		} catch (NullPointerException e) {
			logger.log(Level.WARNING, "Cannot load file: " + DEFAULT_PATH + objectName + ".properties");
			throw e;
		}

		InputStream in = classLoader.getResourceAsStream(USER_PATH + objectName + ".properties");
		if (in != null) {
			properties.load(in);
			in.close();
		}

		StringBuilder builder = new StringBuilder();
		boolean isInvalidProperties = false;
		Set<Object> keySet = properties.keySet();
		for (Object key : keySet) {

			// There is no empty value.
			if (((String) key).isEmpty()) {
				continue;
			}

			String property = properties.getProperty((String) key).trim();
			PrivacyStatus privacyProperty = PrivacyStatus.valueOf(property.toUpperCase());

			switch (privacyProperty) {
			case OWNER:
				break;
			case AUTHENTICATE:
				break;
			case ANY:
				break;
			case NONE:
				break;
			default:
				isInvalidProperties = true;
				builder.append(key + "=" + property + ", ");
				continue;
			}
			objectProperties.put((String) key, privacyProperty);
		}

		if (isInvalidProperties) {
			throw new RuntimeException(
					"Invalid privacy file '" + objectName + ".properties'.\n Invalid values: " + builder.toString());
		}

		OBJECTS_PRIVACY_SETTING.put(objectName, Collections.unmodifiableMap(objectProperties));
	}

	/**
	 * Indicate if the object or attribute should be show to the user according
	 * to the privacy status of the object or attribute.
	 * 
	 * @param objectValue
	 *            Actual value of the object to be evaluate.
	 * @param objectName
	 *            Name of the object or attribute.
	 * @param objectPrivacyStatus
	 *            Privacy status of the object.
	 * @param isAuthenticated
	 *            Indicate if the user is authenticated.
	 * @param isOwner
	 *            Indicate if the user is the owner of the object or attribute
	 *            to evaluate.
	 * @return <code>false</code> if the object or attribute shouldn't be shown
	 *         according of its privacy's level, or if the <b>objectValue</b> is
	 *         <code>null</code> or empty, otherwise <code>true</code>.
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isObjectVisible(Object objectValue, String objectName, PrivacyStatus objectPrivacyStatus,
			boolean isAuthenticated, boolean isOwner) {
		if (objectValue == null)
			return false;

		if (objectValue instanceof String) {
			if (((String) objectValue).isEmpty()) {
				return false;
			}
		} else if (objectValue instanceof List && ((List) objectValue).isEmpty()) {
			return false;
		}

		if (objectPrivacyStatus == null) {
			throw new NullPointerException("Attribute '" + objectName + "' does not have privacy status configured.");
		}

		boolean result = false;
		switch (objectPrivacyStatus) {
		case OWNER:
			if (isOwner) {
				result = true;
			}
			break;
		case AUTHENTICATE:
			if (isAuthenticated) {
				result = true;
			}
			break;
		case ANY:
			result = true;
			break;
		case NONE:
			break;
		}
		return result;
	}

	public static Map<String, PrivacyStatus> getEntityPrivacySettings() {
		return OBJECTS_PRIVACY_SETTING.get(ENTITY);
	}

	public static Map<String, PrivacyStatus> getEntityPublicIdsPrivacySettings() {
		return OBJECTS_PRIVACY_SETTING.get(ENTITY_PUBLIC_ID);
	}

	public static Map<String, PrivacyStatus> getNameserverPrivacySettings() {
		return OBJECTS_PRIVACY_SETTING.get(NAMESERVER);
	}

	public static Map<String, PrivacyStatus> getDomainPrivacySettings() {
		return OBJECTS_PRIVACY_SETTING.get(DOMAIN);
	}

	public static Map<String, PrivacyStatus> getDomainPublicIdsPrivacySettings() {
		return OBJECTS_PRIVACY_SETTING.get(DOMAIN_PUBLIC_ID);
	}

	public static Map<String, PrivacyStatus> getDomainVariantsPrivacySettings() {
		return OBJECTS_PRIVACY_SETTING.get(DOMAIN_VARIANTS);
	}

	public static Map<String, PrivacyStatus> getSecureDnsPrivacySettings() {
		return OBJECTS_PRIVACY_SETTING.get(SECURE_DNS);
	}

	public static Map<String, PrivacyStatus> getDsDataPrivacySettings() {
		return OBJECTS_PRIVACY_SETTING.get(DS_DATA);
	}

	public static Map<String, PrivacyStatus> getEntityLinkPrivacySettings() {
		return OBJECTS_PRIVACY_SETTING.get(ENTITY_LINKS);
	}

	public static Map<String, PrivacyStatus> getEntityRemarkPrivacySettings() {
		return OBJECTS_PRIVACY_SETTING.get(ENTITY_REMARKS);
	}

	public static Map<String, PrivacyStatus> getEntityEventPrivacySettings() {
		return OBJECTS_PRIVACY_SETTING.get(ENTITY_EVENTS);
	}

	public static Map<String, PrivacyStatus> getDomainLinkPrivacySettings() {
		return OBJECTS_PRIVACY_SETTING.get(DOMAIN_LINKS);
	}

	public static Map<String, PrivacyStatus> getDomainRemarkPrivacySettings() {
		return OBJECTS_PRIVACY_SETTING.get(DOMAIN_REMARKS);
	}

	public static Map<String, PrivacyStatus> getDomainEventPrivacySettings() {
		return OBJECTS_PRIVACY_SETTING.get(DOMAIN_EVENTS);
	}

	public static Map<String, PrivacyStatus> getNameserverLinkPrivacySettings() {
		return OBJECTS_PRIVACY_SETTING.get(NAMESERVER_LINKS);
	}

	public static Map<String, PrivacyStatus> getNameserverRemarkPrivacySettings() {
		return OBJECTS_PRIVACY_SETTING.get(NAMESERVER_REMARKS);
	}

	public static Map<String, PrivacyStatus> getNameserverEventPrivacySettings() {
		return OBJECTS_PRIVACY_SETTING.get(NAMESERVER_EVENTS);
	}

	public static Map<String, PrivacyStatus> getAutnumPrivacySettings() {
		return OBJECTS_PRIVACY_SETTING.get(AUTNUM);
	}

	public static Map<String, PrivacyStatus> getAutnumLinkPrivacySettings() {
		return OBJECTS_PRIVACY_SETTING.get(AUTNUM_LINKS);
	}

	public static Map<String, PrivacyStatus> getAutnumRemarkPrivacySettings() {
		return OBJECTS_PRIVACY_SETTING.get(AUTNUM_REMARKS);
	}

	public static Map<String, PrivacyStatus> getAutnumEventPrivacySettings() {
		return OBJECTS_PRIVACY_SETTING.get(AUTNUM_EVENTS);
	}
}