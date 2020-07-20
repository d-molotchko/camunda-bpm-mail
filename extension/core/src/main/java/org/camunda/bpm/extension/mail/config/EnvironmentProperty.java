package org.camunda.bpm.extension.mail.config;

import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class EnvironmentProperty extends Properties {
    public EnvironmentProperty() {
        super();
    }

    @Override
    public String getProperty(String key) {

        Object oval = this.get(key);
        String sval = (oval instanceof String) ? (String) oval : null;
        Properties defaults;

        return (sval != null && sval.startsWith("${") && sval.endsWith("}"))
                ? System.getenv(key)
                : ((sval == null) && ((defaults = super.defaults) != null))
                ? defaults.getProperty(key)
                : sval;
    }

    @Override
    public synchronized Object put(Object key, Object value) {
        final Map<String, String> ordered = new LinkedHashMap<String, String>();
        ordered.put((String) key, (String) value);
        StringSubstitutor sub = new StringSubstitutor(new StringLookup() {
            @Override
            public String lookup(String key) {
                String value = ordered.get(key);
                if (value == null)
                    return System.getenv(key);
                return value;
            }
        });
        return super.put(key, sub.replace(ordered.get(key)));
    }
}
