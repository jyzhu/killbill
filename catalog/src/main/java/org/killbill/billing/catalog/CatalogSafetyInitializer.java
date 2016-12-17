/*
 * Copyright 2014-2016 Groupon, Inc
 * Copyright 2014-2016 The Billing Project, LLC
 *
 * The Billing Project licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.killbill.billing.catalog;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

public class CatalogSafetyInitializer {


    //
    // Ensure that all uninitialized arrays for which there is neither a 'required' XmlElementWrapper or XmlElement annotation
    // end up initialized with a default zero length array (allowing to safely get the length and iterate over (0) element.
    //
    public static void initializeNonRequiredArrayFields(final Object obj) {
        try {
            final Field[] fields = obj.getClass().getDeclaredFields();
            for (final Field f : fields) {
                if (f.getType().isArray()) {
                    final XmlElementWrapper xmlElementWrapper = f.getAnnotation(XmlElementWrapper.class);
                    if (xmlElementWrapper != null) {
                        if (!xmlElementWrapper.required()) {
                            initializeArrayIfNull(obj, f);
                        }
                    } else {
                        final XmlElement xmlElement = f.getAnnotation(XmlElement.class);
                        if (xmlElement != null && !xmlElement.required()) {
                            initializeArrayIfNull(obj, f);
                        }
                    }
                }
            }
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Failed during catalog initialization : ", e);
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException("Failed during catalog initialization : ", e);
        }
    }

    private static void initializeArrayIfNull(final Object obj, final Field f) throws IllegalAccessException, ClassNotFoundException {
        f.setAccessible(true);
        if (f.get(obj) == null) {
            f.set(obj, getZeroLengthArrayInitializer(f));
        }
        f.setAccessible(false);
    }

    private static Object[] getZeroLengthArrayInitializer(final Field f) throws ClassNotFoundException {
        // Yack... type erasure, why?
        final String arrayClassName = f.getType().getCanonicalName();
        final Class type = Class.forName(arrayClassName.substring(0, arrayClassName.length() - 2));
        return (Object[]) Array.newInstance(type, 0);
    }
}