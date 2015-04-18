package org.baswell.routes;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Set;

import static org.baswell.routes.RoutesMethods.*;
import static org.baswell.routes.Pair.*;

class ResponseTypeMapper
{
  static ResponseType mapResponseType(Method method, RouteConfiguration routeConfiguration)
  {
    Class returnType = method.getReturnType();
    if ((returnType == void.class) || (returnType == Void.class))
    {
      return ResponseType.VOID;
    }
    else if (returnType.isArray() && (returnType.getComponentType() == byte.class))
    {
      return ResponseType.BYTES_CONTENT;
    }
    else if (returnType == InputStream.class)
    {
      return ResponseType.STREAM_CONTENT;
    }
    else if ((returnType == String.class) && !routeConfiguration.returnedStringIsContent)
    {
      return ResponseType.FORWARD_DISPATCH;
    }
    else
    {
      return ResponseType.STRING_CONTENT;
    }
  }

  static Pair<ResponseStringWriteStrategy, String> mapResponseStringWriteStrategy(Method method, Set<MediaType> respondToMedia, String contentType, AvailableLibraries availableLibraries)
  {
    return mapResponseStringWriteStrategy(method.getReturnType(), respondToMedia, contentType, availableLibraries);
  }

  static Pair<ResponseStringWriteStrategy, String> mapResponseStringWriteStrategy(Object returnedObject, Set<MediaType> respondToMedia, String contentType, AvailableLibraries availableLibraries)
  {
    return mapResponseStringWriteStrategy(returnedObject.getClass(), respondToMedia, contentType, availableLibraries);
  }

  static Pair<ResponseStringWriteStrategy, String> mapResponseStringWriteStrategy(Class returnType, Set<MediaType> respondToMedia, String contentType, AvailableLibraries availableLibraries)
  {
    String returnTypePackage = returnType.getPackage().getName();

    MediaType mediaType = contentType == null ? null : MediaType.findFromMimeType(contentType);
    if ((mediaType == null) && (respondToMedia != null) && (respondToMedia.size() == 1))
    {
      mediaType = respondToMedia.iterator().next();
    }

    if (returnTypePackage.startsWith("org.json"))
    {
      return pair(ResponseStringWriteStrategy.TO_STRING, MIMETypes.JSON);
    }
    else if (returnTypePackage.startsWith("org.w3c.dom"))
    {
      return pair(ResponseStringWriteStrategy.W3C_NODE, MIMETypes.XML);
    }
    else if (returnTypePackage.startsWith("org.jdom2"))
    {
      return pair(ResponseStringWriteStrategy.JDOM, MIMETypes.XML);
    }
    else if (returnTypePackage.startsWith("org.dom4j"))
    {
      return pair(ResponseStringWriteStrategy.DOM4J, MIMETypes.XML);
    }
    else if (returnType.getAnnotation(XmlRootElement.class) != null)
    {
      return pair(ResponseStringWriteStrategy.JAXB, MIMETypes.XML);
    }
    else if (classImplementsInterface(returnType, CharSequence.class))
    {
      return pair(ResponseStringWriteStrategy.TO_STRING, contentType);
    }
    else if ((((mediaType != null) && (mediaType == MediaType.JSON)))
            && availableLibraries.gsonAvailable())
    {
      return pair(ResponseStringWriteStrategy.GSON, MIMETypes.JSON);
    }
    else
    {
      return null;
    }
  }
}


