/*
 * Copyright 2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.internal;

import groovy.lang.MissingPropertyException;
import org.gradle.api.plugins.Convention;

import java.util.HashMap;
import java.util.Map;

public class DynamicObjectHelper extends AbstractDynamicObject {
    public enum Location {
        BeforeConvention, AfterConvention
    }

    private final BeanDynamicObject delegateObject;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private DynamicObject parent;
    private Convention convention;
    private DynamicObject beforeConvention;
    private DynamicObject afterConvention;

    public DynamicObjectHelper(Object delegateObject) {
        this(new BeanDynamicObject(delegateObject));
    }

    DynamicObjectHelper(BeanDynamicObject delegateObject) {
        this.delegateObject = delegateObject;
    }

    protected String getDisplayName() {
        return delegateObject.getDisplayName();
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public DynamicObject getParent() {
        return parent;
    }

    public void setParent(DynamicObject parent) {
        this.parent = parent;
    }

    public Convention getConvention() {
        return convention;
    }

    public void setConvention(Convention convention) {
        this.convention = convention;
    }

    public void addObject(DynamicObject object, Location location) {
        switch (location) {
            case BeforeConvention:
                beforeConvention = object;
                break;
            case AfterConvention:
                afterConvention = object;
        }
    }

    public BeanDynamicObject getDelegateObject() {
        return delegateObject;
    }

    public boolean hasProperty(String name) {
        if (delegateObject.hasProperty(name)) {
            return true;
        }
        if (beforeConvention != null && beforeConvention.hasProperty(name)) {
            return true;
        }
        if (convention != null && convention.hasProperty(name)) {
            return true;
        }
        if (afterConvention != null && afterConvention.hasProperty(name)) {
            return true;
        }
        if (parent != null && parent.hasProperty(name)) {
            return true;
        }
        return additionalProperties.containsKey(name);
    }

    public Object getProperty(String name) {
        if (delegateObject.hasProperty(name)) {
            return delegateObject.getProperty(name);
        }
        if (additionalProperties.containsKey(name)) {
            return additionalProperties.get(name);
        }
        if (beforeConvention != null && beforeConvention.hasProperty(name)) {
            return beforeConvention.getProperty(name);
        }
        if (convention != null && convention.hasProperty(name)) {
            return convention.getProperty(name);
        }
        if (afterConvention != null && afterConvention.hasProperty(name)) {
            return afterConvention.getProperty(name);
        }
        if (parent != null && parent.hasProperty(name)) {
            return parent.getProperty(name);
        }
        throw propertyMissingException(name);
    }

    public void setProperty(String name, Object value) {
        if (delegateObject.hasProperty(name)) {
            delegateObject.setProperty(name, value);
        } else if (additionalProperties.containsKey(name)) {
            additionalProperties.put(name, value);
        } else if (beforeConvention != null && beforeConvention.hasProperty(name)) {
            beforeConvention.setProperty(name, value);
        } else if (convention != null && convention.hasProperty(name)) {
            convention.setProperty(name, value);
        } else if (afterConvention != null && afterConvention.hasProperty(name)) {
            afterConvention.setProperty(name, value);
        } else {
            additionalProperties.put(name, value);
        }
    }

    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.putAll(additionalProperties);
        if (parent != null) {
            properties.putAll(parent.getProperties());
        }
        if (afterConvention != null) {
            properties.putAll(afterConvention.getProperties());
        }
        if (convention != null) {
            properties.putAll(convention.getAllProperties());
        }
        if (beforeConvention != null) {
            properties.putAll(beforeConvention.getProperties());
        }

        properties.putAll(delegateObject.getProperties());
        properties.put("properties", properties);
        return properties;
    }

    public boolean hasMethod(String name, Object... params) {
        if (delegateObject.hasMethod(name, params)) {
            return true;
        }
        if (beforeConvention != null && beforeConvention.hasMethod(name, params)) {
            return true;
        }
        if (convention != null && convention.hasMethod(name, params)) {
            return true;
        }
        if (afterConvention != null && afterConvention.hasMethod(name, params)) {
            return true;
        }
        if (parent != null && parent.hasMethod(name, params)) {
            return true;
        }
        return false;
    }

    public Object invokeMethod(String name, Object... params) {
        if (delegateObject.hasMethod(name, params)) {
            return delegateObject.invokeMethod(name, params);
        }
        if (beforeConvention != null && beforeConvention.hasMethod(name, params)) {
            return beforeConvention.invokeMethod(name, params);
        }
        if (convention != null && convention.hasMethod(name, params)) {
            return convention.invokeMethod(name, params);
        }
        if (afterConvention != null && afterConvention.hasMethod(name, params)) {
            return afterConvention.invokeMethod(name, params);
        }
        if (parent != null && parent.hasMethod(name, params)) {
            return parent.invokeMethod(name, params);
        }
        throw methodMissingException(name, params);
    }

    /**
     * Returns the inheritable properties and methods of this object.
     *
     * @return an object containing the inheritable properties and methods of this object.
     */
    public DynamicObject getInheritable() {
        return new InheritedDynamicObject();
    }

    private DynamicObjectHelper snapshotInheritable() {
        DynamicObjectHelper helper = new DynamicObjectHelper(delegateObject.withNoProperties());
        helper.parent = parent;
        helper.convention = convention;
        helper.additionalProperties = additionalProperties;
        if (beforeConvention != null) {
            helper.beforeConvention = beforeConvention;
        }
        return helper;
    }

    private class InheritedDynamicObject implements DynamicObject {
        public void setProperty(String name, Object value) {
            throw new MissingPropertyException(String.format("Could not find property '%s' inherited from %s.", name,
                    delegateObject.getDisplayName()));
        }

        public boolean hasProperty(String name) {
            return snapshotInheritable().hasProperty(name);
        }

        public Object getProperty(String name) {
            return snapshotInheritable().getProperty(name);
        }

        public Map<String, Object> getProperties() {
            return snapshotInheritable().getProperties();
        }

        public boolean hasMethod(String name, Object... params) {
            return snapshotInheritable().hasMethod(name, params);
        }

        public Object invokeMethod(String name, Object... params) {
            return snapshotInheritable().invokeMethod(name, params);
        }
    }
}

