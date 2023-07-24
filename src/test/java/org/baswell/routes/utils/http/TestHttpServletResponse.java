/*
 * Copyright 2015 Corey Baswell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.baswell.routes.utils.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public class TestHttpServletResponse implements HttpServletResponse
{
  public ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

  public PrintWriter writer = new PrintWriter(outputStream);

  public String contentType;
  
  public Integer contentLength;
  
  public int bufferSize;
  
  public List<Cookie> cookies = new ArrayList<Cookie>();
  
  public Integer responseCode;

  public String redirect;

  public String getContentAsString()
  {
    return new String(outputStream.toByteArray());
  }

   @Override
  public String getCharacterEncoding()
  {
    return "UTF-8";
  }

  @Override
  public String getContentType()
  {
    return contentType;
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException
  {
    return new TestServletOutputStream(outputStream);
  }

  @Override
  public PrintWriter getWriter() throws IOException
  {
    return writer;
  }

  @Override
  public void setCharacterEncoding(String charset)
  {
    throw new RuntimeException("Not implemented.");
  }

  @Override
  public void setContentLength(int len)
  {
    contentLength = len;
  }

  @Override
  public void setContentLengthLong(long l) {

  }

  @Override
  public void setContentType(String type)
  {
    contentType = type;
  }

  @Override
  public void setBufferSize(int size)
  {
    bufferSize = size;
  }

  @Override
  public int getBufferSize()
  {
    return bufferSize;
  }

  @Override
  public void flushBuffer() throws IOException
  {
    outputStream.flush();
  }

  @Override
  public void resetBuffer()
  {
    outputStream.reset();
  }

  @Override
  public boolean isCommitted()
  {
    return false;
  }

  @Override
  public void reset()
  {
    outputStream.reset();
  }

  @Override
  public void setLocale(Locale loc)
  {
    throw new RuntimeException("Not implemented.");

  }

  @Override
  public Locale getLocale()
  {
    throw new RuntimeException("Not implemented.");
  }

  @Override
  public void addCookie(Cookie cookie)
  {
    cookies.add(cookie);
  }

  @Override
  public boolean containsHeader(String name)
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public String encodeURL(String url)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String encodeRedirectURL(String url)
  {
    throw new RuntimeException("Not implemented.");
  }

  @Override
  public void sendError(int sc, String msg) throws IOException
  {
    responseCode = sc;
  }

  @Override
  public void sendError(int sc) throws IOException
  {
    responseCode = sc;
  }

  @Override
  public void sendRedirect(String location) throws IOException
  {
    redirect = location;
  }

  @Override
  public void setDateHeader(String name, long date)
  {}

  @Override
  public void addDateHeader(String name, long date)
  {}

  @Override
  public void setHeader(String name, String value)
  {}

  @Override
  public void addHeader(String name, String value)
  {}

  @Override
  public void setIntHeader(String name, int value)
  {}

  @Override
  public void addIntHeader(String name, int value)
  {}

  @Override
  public void setStatus(int sc)
  {
    responseCode = sc;
  }

  public int getStatus()
  {
    return responseCode == null ? 200 : responseCode;
  }

  public String getHeader(String name)
  {
    throw new RuntimeException("Not implemented.");
  }

  public Collection<String> getHeaders(String name)
  {
    throw new RuntimeException("Not implemented.");
  }

  public Collection<String> getHeaderNames()
  {
    throw new RuntimeException("Not implemented.");
  }
}
