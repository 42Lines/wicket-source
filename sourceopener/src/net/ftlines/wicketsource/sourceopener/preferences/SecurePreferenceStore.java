package net.ftlines.wicketsource.sourceopener.preferences;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;

/**
 * This is an adpater between ISecurePreferences (underlying storage) and 
 * IPreferences (API that the convenience PreferencesPage field editors want to use). 
 *
 * Originally from net.synck.gcontacts.preferences.SecurePreferenceStore and licensed as Lesser GPL
 * 
 * http://code.google.com/p/gclipsecontacts/source/browse/net.synck.gcontacts/src/net/synck/gcontacts/preferences/SecurePreferenceStore.java
 *
 */
public class SecurePreferenceStore extends EventManager implements
        IPersistentPreferenceStore {

        private ISecurePreferences securePreferences;

        private IStorageExceptionHandler exceptionHandler;

        private Set<String> encryptFieldsSet = new HashSet<String>();
        
        private Properties defaultProperties;

        private boolean dirty = false;
        
        public interface IStorageExceptionHandler {
                public void handle(StorageException e);
        }

        
        public SecurePreferenceStore(ISecurePreferences securePreferences) {
                super();
                this.securePreferences = securePreferences;
                defaultProperties = new Properties();
                exceptionHandler = new IStorageExceptionHandler() {
                        @Override
                        public void handle(StorageException e) {
                                // TODO Auto-generated method stub                              
                        }                       
                };
        }

        public void setStorageExceptionHandler(IStorageExceptionHandler handler) {
                if (handler == null)
                        throw new IllegalArgumentException("Storage exception handler can not be null");
                exceptionHandler = handler;
        }
        
        public void setDoEncryptPreference(String preferenceKey) {
                encryptFieldsSet.add(preferenceKey);
        }
        
        @Override
        public void addPropertyChangeListener(IPropertyChangeListener listener) {
                addListenerObject(listener);            
        }

        @Override
        public boolean contains(String name) {
                for (String key : securePreferences.keys()) {
                        if (key.equals(name))
                                return true;
                }
                
                return defaultProperties.contains(name);
        }

        @Override
        public void firePropertyChangeEvent(String name, Object oldValue,
                        Object newValue) {
                final Object[] finalListeners = getListeners();
                // Do we need to fire an event.
                if (finalListeners.length > 0
                                && (oldValue == null || !oldValue.equals(newValue))) {
                        final PropertyChangeEvent pe = new PropertyChangeEvent(this, name,
                                        oldValue, newValue);
                        for (int i = 0; i < finalListeners.length; ++i) {
                                final IPropertyChangeListener l = (IPropertyChangeListener) finalListeners[i];
                                SafeRunnable.run(new SafeRunnable(JFaceResources
                                                .getString("PreferenceStore.changeError")) { //$NON-NLS-1$
                                                        public void run() {
                                                                l.propertyChange(pe);
                                                        }
                                                });
                        }
                }
        }

        @Override
        public boolean getBoolean(String name) {
                try {
                        return securePreferences.getBoolean(name, IPreferenceStore.BOOLEAN_DEFAULT_DEFAULT);
                } catch (StorageException e) {
                        exceptionHandler.handle(e);                     
                }
                return IPreferenceStore.BOOLEAN_DEFAULT_DEFAULT;
        }

        @Override
        public boolean getDefaultBoolean(String name) {
                if (defaultProperties.containsKey(name)) {
                        Boolean value = Boolean.parseBoolean(defaultProperties.getProperty(name));
                        return value;
                } else {
                        return IPreferenceStore.BOOLEAN_DEFAULT_DEFAULT;
                }
        }

        @Override
        public double getDefaultDouble(String name) {
                if (defaultProperties.containsKey(name)) {
                        Double value;
                        try {
                         value = Double.parseDouble(defaultProperties.getProperty(name));
                        } catch (NumberFormatException e) {
                                return  IPreferenceStore.DOUBLE_DEFAULT_DEFAULT;
                        }
                        return value.doubleValue();
                } else {
                        return IPreferenceStore.DOUBLE_DEFAULT_DEFAULT;
                }
        }

        @Override
        public float getDefaultFloat(String name) {
                if (defaultProperties.containsKey(name)) {
                        Float value;
                        try {
                         value = Float.parseFloat(defaultProperties.getProperty(name));
                        } catch (NumberFormatException e) {
                                return  IPreferenceStore.FLOAT_DEFAULT_DEFAULT;
                        }
                        return value.floatValue();
                } else {
                        return IPreferenceStore.FLOAT_DEFAULT_DEFAULT;
                }               
        }

        @Override
        public int getDefaultInt(String name) {
                if (defaultProperties.containsKey(name)) {
                        Integer value;
                        try {
                         value = Integer.parseInt(defaultProperties.getProperty(name));
                        } catch (NumberFormatException e) {
                                return  IPreferenceStore.INT_DEFAULT_DEFAULT;
                        }
                        return value.intValue();
                } else {
                        return IPreferenceStore.INT_DEFAULT_DEFAULT;
                }       
        }

        @Override
        public long getDefaultLong(String name) {
                if (defaultProperties.containsKey(name)) {
                        Long value;
                        try {
                         value = Long.parseLong(defaultProperties.getProperty(name));
                        } catch (NumberFormatException e) {
                                return  IPreferenceStore.LONG_DEFAULT_DEFAULT;
                        }
                        return value.longValue();
                } else {
                        return IPreferenceStore.LONG_DEFAULT_DEFAULT;
                }
        }

        @Override
        public String getDefaultString(String name) {
                if (defaultProperties.containsKey(name)) {                      
                         return defaultProperties.getProperty(name);                    
                } else {
                        return IPreferenceStore.STRING_DEFAULT_DEFAULT;
                }
        }

        @Override
        public double getDouble(String name) {
                try {
                        return securePreferences.getDouble(name, IPreferenceStore.DOUBLE_DEFAULT_DEFAULT);
                } catch (StorageException e) {
                        exceptionHandler.handle(e);                     
                }
                return IPreferenceStore.DOUBLE_DEFAULT_DEFAULT;

        }

        @Override
        public float getFloat(String name) {
                try {
                        return securePreferences.getFloat(name, IPreferenceStore.FLOAT_DEFAULT_DEFAULT);
                } catch (StorageException e) {
                        exceptionHandler.handle(e);                     
                }
                return IPreferenceStore.FLOAT_DEFAULT_DEFAULT;

        }

        @Override
        public int getInt(String name) {
                try {
                        return securePreferences.getInt(name, IPreferenceStore.INT_DEFAULT_DEFAULT);
                } catch (StorageException e) {
                        exceptionHandler.handle(e);                     
                }
                return IPreferenceStore.INT_DEFAULT_DEFAULT;
        }

        @Override
        public long getLong(String name) {
                try {
                        return securePreferences.getLong(name, IPreferenceStore.LONG_DEFAULT_DEFAULT);
                } catch (StorageException e) {
                        exceptionHandler.handle(e);                     
                }
                return IPreferenceStore.LONG_DEFAULT_DEFAULT;
        }

        @Override
        public String getString(String name) {
                try {
                        return securePreferences.get(name, IPreferenceStore.STRING_DEFAULT_DEFAULT);
                } catch (StorageException e) {
                        exceptionHandler.handle(e);                     
                }
                return IPreferenceStore.STRING_DEFAULT_DEFAULT;
        }

        @Override
        public boolean isDefault(String name) {
                return (!contains(name) && defaultProperties.containsKey(name));
        }

        @Override
        public boolean needsSaving() {
                return dirty;
        }

        @Override
        public void putValue(String name, String value) {
                String oldValue = getString(name);
                if (oldValue == null || !oldValue.equals(value)) {
                        setValue(name, value);
                        dirty = true;
                }
        }

        @Override
        public void removePropertyChangeListener(IPropertyChangeListener listener) {
                removeListenerObject(listener);
        }

        @Override
        public void setDefault(String name, double value) {
                defaultProperties.put(name, value);
        }

        @Override
        public void setDefault(String name, float value) {
                defaultProperties.put(name, value);

        }

        @Override
        public void setDefault(String name, int value) {
                defaultProperties.put(name, value);
        }

        @Override
        public void setDefault(String name, long value) {
                defaultProperties.put(name, value);
        }

        @Override
        public void setDefault(String name, String defaultObject) {
                defaultProperties.put(name, defaultObject);
        }

        @Override
        public void setDefault(String name, boolean value) {
                defaultProperties.put(name, value);

        }

        @Override
        public void setToDefault(String name) {
                //securePreferences.
        }

        @Override
        public void setValue(String name, double value) {               
                Double oldValue = getDouble(name);
                if (oldValue != value) {
                        try {
                                securePreferences.putDouble(name, value, encryptFieldsSet.contains(name));
                        } catch (StorageException e) {
                                exceptionHandler.handle(e);
                        }
                        dirty = true;
                        firePropertyChangeEvent(name, new Double(oldValue), new Double(
                                        value));
                }
                
        
        }

        @Override
        public void setValue(String name, float value) {
                Float oldValue = getFloat(name);
                
                if (oldValue != value) {

                        try {
                                securePreferences.putFloat(name, value, encryptFieldsSet.contains(name));
                        } catch (StorageException e) {
                                exceptionHandler.handle(e);
                        }
                        dirty = true;
                        firePropertyChangeEvent(name, new Float(oldValue), new Float(
                                        value));
                }
                
        }

        @Override
        public void setValue(String name, int value) {
                
                Integer oldValue = getInt(name);
                
                if (oldValue != value) {

                        try {
                                securePreferences.putInt(name, value, encryptFieldsSet.contains(name));
                        } catch (StorageException e) {
                                exceptionHandler.handle(e);
                        }
                        dirty = true;
                        firePropertyChangeEvent(name, new Integer(oldValue), new Integer(
                                        value));
                }
                
                

        }

        @Override
        public void setValue(String name, long value) {
                
                Long oldValue = getLong(name);
                
                if (oldValue != value) {

                        try {
                                securePreferences.putLong(name, value, encryptFieldsSet.contains(name));
                        } catch (StorageException e) {
                                exceptionHandler.handle(e);
                        }
                        dirty = true;
                        firePropertyChangeEvent(name, new Long(oldValue), new Long(
                                        value));
                }
                
                
        }

        @Override
        public void setValue(String name, String value) {
                
                String oldValue = getString(name);
                
                if (oldValue != value) {
                        try {
                                securePreferences.put(name, value, encryptFieldsSet.contains(name));
                        } catch (StorageException e) {
                                exceptionHandler.handle(e);
                        }
                        dirty = true;
                        firePropertyChangeEvent(name, new String(oldValue), new String(
                                        value));
                }                               
        }

        @Override
        public void setValue(String name, boolean value) {
                
                Boolean oldValue = getBoolean(name);
                
                if (oldValue != value) {

                        try {
                                securePreferences.putBoolean(name, value, encryptFieldsSet.contains(name));
                        } catch (StorageException e) {
                                exceptionHandler.handle(e);
                        }
                        dirty = true;
                        firePropertyChangeEvent(name, new Boolean(oldValue), new Boolean(
                                        value));
                }
                
        }

        @Override
        public void save() throws IOException {
                securePreferences.flush();
                dirty  = false;
        }

}
