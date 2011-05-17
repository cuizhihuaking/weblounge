/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
 *  http://weblounge.o2it.ch
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software Foundation
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.o2it.weblounge.contentrepository.impl.index.solr;

import static ch.o2it.weblounge.contentrepository.impl.index.solr.SolrFields.LOCALIZED_FULLTEXT;

import ch.o2it.weblounge.common.content.page.Pagelet;
import ch.o2it.weblounge.common.language.Language;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Extension to a <code>SolrUpdateableInputDocument</code> that facilitates in
 * posting weblounge pages to solr. This implementation provides utility methods
 * that will ease handling of objects such as dates or users and help prevent
 * posting of <code>null</code> values.
 */
public abstract class AbstractInputDocument extends SolrInputDocument implements Map<String, Collection<Object>> {

  /** Serial version uid */
  private static final long serialVersionUID = 1812364663819822015L;

  /**
   * Adds the field and its value to the search index. This method is here for
   * convenience so we don't need to do <code>null</code> check on each and
   * every field value.
   * 
   * @param fieldName
   *          the field name
   * @param fieldValue
   *          the value
   * @param addToFulltext
   *          <code>true</code> to add the contents to the fulltext field as
   *          well
   */
  public void addField(String fieldName, Object fieldValue,
      boolean addToFulltext) {
    if (fieldName == null)
      throw new IllegalArgumentException("Field name cannot be null");
    if (fieldValue == null)
      return;
    if (fieldValue.getClass().isArray()) {
      Object[] fieldValues = (Object[]) fieldValue;
      for (Object v : fieldValues) {
        super.addField(fieldName, v);
      }
    } else {
      super.addField(fieldName, fieldValue);
    }

    // Add to fulltext
    if (addToFulltext) {
      String fulltext = (String) super.getFieldValue(SolrFields.FULLTEXT);
      if (fieldValue.getClass().isArray()) {
        Object[] fieldValues = (Object[]) fieldValue;
        for (Object v : fieldValues) {
          fulltext = StringUtils.join(new Object[] { fulltext, v.toString() }, " ");
        }
      } else {
        fulltext = StringUtils.join(new Object[] {
            fulltext,
            fieldValue.toString() }, " ");
      }
      super.setField(SolrFields.FULLTEXT, fulltext);
    }
  }

  /**
   * Adds the field and its value to the indicated field of the search index as
   * well as to the language-sensitive fulltext field. The implementation
   * performs a <code>null</code> test and silently returns if <code>null</code>
   * was passed in.
   * 
   * @param fieldName
   *          the field name
   * @param fieldValue
   *          the value
   * @param language
   *          the language
   * @param addToFulltext
   *          <code>true</code> to add the contents to the fulltext field as
   *          well
   */
  public void addField(String fieldName, Object fieldValue, Language language,
      boolean addToFulltext) {
    if (fieldName == null)
      throw new IllegalArgumentException("Field name cannot be null");
    if (fieldValue == null)
      return;

    addField(fieldName, fieldValue);

    // Add to fulltext
    if (addToFulltext) {

      // Update the localized fulltext
      String localizedFieldName = getLocalizedFieldName(LOCALIZED_FULLTEXT, language);
      String localizedFulltext = (String) super.getFieldValue(localizedFieldName);
      if (fieldValue.getClass().isArray()) {
        Object[] fieldValues = (Object[]) fieldValue;
        for (Object v : fieldValues) {
          localizedFulltext = StringUtils.join(new Object[] {
              localizedFulltext,
              v.toString() }, " ");
        }
      } else {
        localizedFulltext = StringUtils.join(new Object[] {
            localizedFulltext,
            fieldValue.toString() }, " ");
      }
      super.setField(localizedFieldName, localizedFulltext);

    }
  }

  /**
   * Returns the localized field name, which is the original field name extended
   * by an underscore and the language identifier.
   * 
   * @param fieldName
   *          the field name
   * @param language
   *          the language
   * @return the localized field name
   */
  protected String getLocalizedFieldName(String fieldName, Language language) {
    return MessageFormat.format(fieldName, language.getIdentifier());
  }

  /**
   * Returns a string representation of the pagelet's element content in the
   * specified language. If <code>format</code> is <code>true</code> then the
   * content is formatted as <code>field=&lt;value&gt;;;</code>, otherwise just
   * the values are added.
   * 
   * @param pagelet
   *          the pagelet
   * @param language
   *          the language
   * @return the serialized element content
   */
  protected String[] serializeContent(Pagelet pagelet, Language language) {
    List<String> result = new ArrayList<String>();
    for (String element : pagelet.getContentNames(language)) {
      String[] content = pagelet.getMultiValueContent(element, language, true);
      for (String c : content) {
        StringBuffer buf = new StringBuffer();
        buf.append(element).append(":= ").append(c);
        result.add(buf.toString());
      }
    }
    return result.toArray(new String[result.size()]);
  }

  /**
   * Returns a string representation of the pagelet's element properties. If
   * <code>format</code> is <code>true</code> then the property is formatted as
   * <code>field=&lt;value&gt;;;</code>, otherwise just the values are added.
   * 
   * @param pagelet
   *          the pagelet
   * @return the serialized element properties
   */
  protected String[] serializeProperties(Pagelet pagelet) {
    List<String> result = new ArrayList<String>();
    for (String property : pagelet.getPropertyNames()) {
      for (String v : pagelet.getMultiValueProperty(property)) {
        StringBuffer buf = new StringBuffer();
        buf.append(property).append(":=").append(v);
        result.add(buf.toString());
      }
    }
    return result.toArray(new String[result.size()]);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Map#containsKey(java.lang.Object)
   */
  public boolean containsKey(Object o) {
    return super.getFieldNames().contains(o);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Map#containsValue(java.lang.Object)
   */
  public boolean containsValue(Object o) {
    for (String fieldName : super.getFieldNames()) {
      if (super.getFieldValues(fieldName).contains(o))
        return true;
    }
    return false;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Map#entrySet()
   */
  public Set<java.util.Map.Entry<String, Collection<Object>>> entrySet() {
    Map<String, Collection<Object>> m = new HashMap<String, Collection<Object>>();
    for (String fieldName : super.getFieldNames()) {
      List<Object> values = new ArrayList<Object>();
      for (Object v : super.getFieldValues(fieldName))
        values.add(v.toString());
      m.put(fieldName, values);
    }
    return m.entrySet();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Map#get(java.lang.Object)
   */
  public Collection<Object> get(Object name) {
    return super.getFieldValues(name.toString());
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Map#isEmpty()
   */
  public boolean isEmpty() {
    return super.getFieldNames().size() == 0;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Map#keySet()
   */
  public Set<String> keySet() {
    return new HashSet<String>(super.getFieldNames());
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Map#put(java.lang.Object, java.lang.Object)
   */
  public Collection<Object> put(String name, Collection<Object> values) {
    throw new UnsupportedOperationException("Use addField() instead of this method");
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Map#putAll(java.util.Map)
   */
  public void putAll(Map<? extends String, ? extends Collection<Object>> m) {
    throw new UnsupportedOperationException("Use addField() instead of this method");
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Map#remove(java.lang.Object)
   */
  public Collection<Object> remove(Object name) {
    Collection<Object> values = super.getFieldValues(name.toString());
    return values;
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Map#size()
   */
  public int size() {
    return super.getFieldNames().size();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.util.Map#values()
   */
  public Collection<Collection<Object>> values() {
    List<Collection<Object>> values = new ArrayList<Collection<Object>>();
    for (String field : super.getFieldNames()) {
      values.add(super.getFieldValues(field));
    }
    return values;
  }

}
