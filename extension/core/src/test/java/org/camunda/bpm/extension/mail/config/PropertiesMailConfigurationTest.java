package org.camunda.bpm.extension.mail.config;

import junit.framework.TestCase;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class PropertiesMailConfigurationTest extends TestCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesMailConfiguration.class);

    public static final String ENV_PROPERTIES_PATH = "MAIL_CONFIG";
    public static final String PROPERTIES_CLASSPATH_PREFIX = "classpath:";
    public static final String DEFAULT_PROPERTIES_PATH = PROPERTIES_CLASSPATH_PREFIX + "/mail-config.properties";

    protected String path = null;

    public void testLoadProperties() throws IOException {
        final Map<String, String> ordered = new LinkedHashMap<String, String>();
        String path = getPropertiesPath();

        Properties properties = new Properties() {
            @Override
            public synchronized Object put(Object key, Object value) {
                ordered.put((String)key, (String)value);
                StringSubstitutor sub = new StringSubstitutor(new StringLookup() {
                    @Override
                    public String lookup(String key) {
                        String value = ordered.get(key);
                        if (value == null)
                            return System.getProperty(key);
                        return value;
                    }
                });
                return super.put(key, sub.replace(ordered.get(key)));
            }
        };
        try {
            InputStream inputStream = getProperiesAsStream(path);
            if (inputStream != null) {
                properties.load(inputStream);
//                return properties;
//
            } else {
                throw new IllegalStateException("Cannot find mail configuration at: " + path);
            }

        } catch (IOException e) {
            throw new IllegalStateException("Unable to load mail configuration from: " + path, e);
        }
        }

    protected String getPropertiesPath() {
        return Optional.ofNullable(path).orElseGet(() ->
                Optional.ofNullable(System.getenv(ENV_PROPERTIES_PATH))
                        .orElse(DEFAULT_PROPERTIES_PATH));
    }

    protected InputStream getProperiesAsStream(String path) throws FileNotFoundException {

        if (path.startsWith(PROPERTIES_CLASSPATH_PREFIX)) {
            String pathWithoutPrefix = path.substring(PROPERTIES_CLASSPATH_PREFIX.length());

            LOGGER.debug("load mail properties from classpath '{}'", pathWithoutPrefix);

            return getClass().getResourceAsStream(pathWithoutPrefix);

        } else {
            Path config = Paths.get(path);

            LOGGER.debug("load mail properties from path '{}'", config.toAbsolutePath());

            File file = config.toFile();
            if (file.exists()) {
                return new FileInputStream(file);
            } else {
                return null;
            }
        }
    }
}
