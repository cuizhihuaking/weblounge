/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
 *  http://entwinemedia.com/weblounge
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

package ch.entwine.weblounge.cache.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * In the <code>CachedResponseMetaInfo</code>, the cache stores
 * <code>HTTP</code> headers that have been set in the associated
 * <code>HttpServletResponse</code>.
 */
final class CacheableHttpServletResponseHeaders implements Serializable {

  /** The serial version uid */
  private static final long serialVersionUID = 8273352269230366714L;

  /** List of custom headers */
  private Map<String, String> headers = new HashMap<String, String>(5);

  /** List with headers to ignore */
  private static final List<String> ignoredHeaders = new ArrayList<String>();

  static {
    ignoredHeaders.add("content-length");
    ignoredHeaders.add("etag");
    ignoredHeaders.add("expires");
    ignoredHeaders.add("content-type");
  }

  /**
   * Writes the cached response headers to a new response.
   * 
   * @param response
   *          the response
   */
  void apply(HttpServletResponse response) {
    if (response == null)
      return;
    for (Map.Entry<String, String> e : headers.entrySet()) {
      String key = e.getKey();
      int hashIndex = key.indexOf('#');
      if (hashIndex > 0)
        key = key.substring(hashIndex);
      if (!ignoredHeaders.contains(key.toLowerCase()))
        response.addHeader(key, e.getValue());
    }
  }

  /**
   * Returns <code>true</code> if the response headers contain the header with
   * name <code>header</code>.
   * 
   * @param header
   *          the header name
   * @return <code>true</code> if the header was set
   */
  boolean containsHeader(String header) {
    return headers.containsKey(header);
  }

  /**
   * Adds an <code>HTTP</code> header to the response, possibly replacing any
   * existing headers with that name.
   * 
   * @param key
   *          the header name
   * @param value
   *          the header value
   */
  void setHeader(String key, String value) {
    headers.put(key, value);
  }

  /**
   * Adds an <code>HTTP</code> header to the response. If a header with that
   * name has already been set, then the value is added to the existing values
   * for that header.
   * <p>
   * Note that this method will flatten those headers that have multiple values.
   * Use {@link #getHeaders()} to receive them in their original form.
   * 
   * @param key
   *          the header name
   * @param value
   *          the header value
   */
  void addHeader(String key, String value) {
    String o = headers.get(key);
    if (o == null) {
      headers.put(key, value);
    } else {
      int i = 1;
      while (true) {
        String overflowKey = new StringBuffer(key).append("#").append(i).toString();
        if (headers.containsKey(overflowKey)) {
          i++;
          continue;
        }
        headers.put(overflowKey, value);
        break;
      }
    }
  }

  /**
   * Returns the headers as they were originally added to the response.
   * 
   * @return the headers
   */
  @SuppressWarnings("unchecked")
  Map<String, Object> getHeaders() {
    Map<String, Object> original = new HashMap<String, Object>();
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      String key = entry.getKey();
      int hashIndex = key.indexOf('#');
      if (hashIndex > 0)
        key = key.substring(0, hashIndex);
      if (original.containsKey(key)) {
        Object value = original.get(key);
        if (value instanceof List) {
          ((List<String>) value).add(entry.getValue());
        } else {
          List<String> values = new ArrayList<String>();
          values.add((String)value);
          values.add(entry.getValue());
          value = values;
        }
        original.put(key, value);
      } else {
        original.put(key, entry.getValue());
      }
    }
    return original;
  }

  /**
   * Returns the number of headers.
   * 
   * @return the number of headers
   */
  public int size() {
    return headers.size();
  }

}
