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
package org.baswell.routes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

import static org.baswell.routes.ContentConversionType.*;
import static org.baswell.routes.RoutesMethods.*;

class ResponseProcessor
{
  private final RoutesConfiguration routesConfiguration;

  private final AvailableLibraries availableLibraries;

  ResponseProcessor(RoutesConfiguration routesConfiguration)
  {
    this.routesConfiguration = routesConfiguration;
    availableLibraries = new AvailableLibraries(routesConfiguration);
  }
  
  void processResponse(ResponseType responseType, ContentConversionType contentConversionType, Object response, String contentType, RouteConfiguration routeConfiguration, HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException, ServletException
  {
    if (response != null)
    {
      switch (responseType)
      {
        case BYTES_CONTENT:
          servletResponse.getOutputStream().write((byte[])response);
          break;

        case STREAM_CONTENT:
          fastChannelCopy((InputStream)response, servletResponse.getOutputStream(), routesConfiguration.streamBufferSize);
          break;
          
        case STRING_CONTENT:

          if (contentConversionType == null)
          {
            MediaType mediaType = null;
            if (hasContent(routeConfiguration.contentType))
            {
              mediaType = MediaType.findFromMimeType(routeConfiguration.contentType);
            }

            if ((mediaType == null) && (routeConfiguration.respondsToMedia.size() == 1))
            {
              mediaType = routeConfiguration.respondsToMedia.get(0);
            }

            contentConversionType = mapContentConversionType(response.getClass(), mediaType, availableLibraries);
            if (contentConversionType == null)
            {
              contentConversionType = ContentConversionType.TO_STRING;
            }

            if (nullEmpty(servletResponse.getContentType()) && contentConversionType.mimeType != null)
            {
              servletResponse.setContentType(contentConversionType.mimeType);
            }
          }

          switch(contentConversionType)
          {
            case JACKSON:
              JacksonBridge.sendJackson(response, servletResponse);
              break;

            case GSON:
              GSONBridge.sendGson(response, servletResponse);
              break;

            case W3C_NODE:
              sendNode(response, servletResponse);
              break;

            case JAXB:
              sendJaxb(response, servletResponse);
              break;

            case JDOM2_DOCUMENT:
              JDOMBridge.sendJdom2Document(response, servletResponse);
              break;

            case JDOM2_ELEMENT:
              JDOMBridge.sendJdom2Element(response, servletResponse);
              break;

            case TO_STRING:
            case JSON_SIMPLE:
            default:
              sendToString(response, servletResponse);
              break;
          }
          break;
          
        case FORWARD_DISPATCH:
          forwardDispatch(response.toString(), servletRequest, servletResponse, routeConfiguration);
          break;
          
        case VOID:
          break;
      }
    }
  }


  void forwardDispatch(String path, HttpServletRequest servletRequest, HttpServletResponse servletResponse, RouteConfiguration routeConfiguration) throws IOException, ServletException
  {
    if (path.startsWith("redirect:"))
    {
      servletResponse.sendRedirect(routesConfiguration.redirectHandler.getRedirectUrl(path.substring(9, path.length()), servletRequest));
    }
    else
    {
      if (!path.startsWith("/"))
      {
        path = routeConfiguration.forwardPath + path;
      }

      servletRequest.getRequestDispatcher(path).forward(servletRequest, servletResponse);
    }
  }

  void sendNode(Object response, HttpServletResponse servletResponse) throws IOException
  {
    try
    {
      TransformerFactory.newInstance().newTransformer().transform(new DOMSource((Node) response), new StreamResult(servletResponse.getOutputStream()));
    }
    catch (TransformerException e)
    {
      throw new RuntimeException(e);
    }
  }

  void sendJaxb(Object response, HttpServletResponse servletResponse) throws IOException
  {
    try
    {
      JAXBContext.newInstance(response.getClass()).createMarshaller().marshal(response, servletResponse.getOutputStream());
    }
    catch (JAXBException e)
    {
      throw new RuntimeException(e);
    }
  }

  void sendToString(Object response, HttpServletResponse servletResponse) throws IOException
  {
    servletResponse.getWriter().write(response.toString());
  }

  static void fastChannelCopy(InputStream inStream, OutputStream outStream, int bufferSize) throws IOException
  {
    final ReadableByteChannel src = Channels.newChannel(inStream);
    final WritableByteChannel dest = Channels.newChannel(outStream);
    final ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
    while (src.read(buffer) != -1)
    {
      // prepare the buffer to be drained
      buffer.flip();
      // write to the channel, may block
      dest.write(buffer);
      // If partial transfer, shift remainder down
      // If buffer is empty, same as doing clear()
      buffer.compact();
    }
    // EOF will leave buffer in fill state
    buffer.flip();
    // make sure the buffer is fully drained.
    while (buffer.hasRemaining())
    {
      dest.write(buffer);
    }
  }

}
