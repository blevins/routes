package org.baswell.routes;

import org.baswell.routes.meta.MetaAuthenticator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

public class MetaHandler
{
  final RoutingTable routingTable;

  final RoutesConfig routesConfig;

  public MetaHandler(RoutingTable routingTable, RoutesConfig routesConfig)
  {
    this.routingTable = routingTable;
    this.routesConfig = routesConfig;
  }

  public boolean handled(HttpServletRequest request, HttpServletResponse response, RequestPath path, RequestParameters parameters, HttpMethod httpMethod, Format format) throws IOException
  {
    String routesMetaPath = routesConfig.routesMetaPath;
    if (!routesMetaPath.startsWith("/")) routesMetaPath = "/" + routesMetaPath;

    if (path.startsWith(routesMetaPath))
    {
      if ((routesConfig.metaAuthenticator == null) || routesConfig.metaAuthenticator.metaRequestAuthenticated(request, response))
      {
        path = path.substring(routesMetaPath.length());

        if (format.type == Format.Type.HTML)
        {
          if (path.equals(""))
          {
            getRoutesPage(response);
            return true;
          }

        }
        else if (format.type == Format.Type.JSON)
        {
          if (path.equals(""))
          {
            getRoutes(request, response, parameters);
            return true;
          }
        }
        return false;
      }
      else
      {
        return true;
      }
    }

    return false;
  }

  void getRoutesPage(HttpServletResponse response) throws IOException
  {
    response.getOutputStream().write(getIndexHtml("routes.html"));
  }

  void getRoutes(HttpServletRequest request, HttpServletResponse response, RequestParameters parameters) throws IOException
  {
    List<RouteTableRow> rows = new ArrayList<RouteTableRow>();
    if (parameters.contains("path"))
    {
      RequestPath requestPath = new RequestPath(parameters.get("path"));
      HttpMethod httpMethod = HttpMethod.fromServletMethod(parameters.get("httpMethod"));
      String acceptType = parameters.get("acceptType");
      RequestParameters requestParameters = new RequestParameters(parameters.get("parameters"));

      for (RouteNode routeNode : routingTable.getRouteNodes())
      {
        if ((routeNode.criteria.matches(httpMethod, new Format(acceptType), requestPath, requestParameters)))
        {
          rows.add(new RouteTableRow(routeNode, request));
        }
      }
    }
    else
    {
      for (RouteNode routeNode : routingTable.getRouteNodes())
      {
        rows.add(new RouteTableRow(routeNode, request));
      }
    }

    response.setContentType("application/json");
    PrintWriter writer = response.getWriter();
    writer.write("[");
    for (int i = 0; i < rows.size(); i++)
    {
      if (i != 0)
      {
        writer.write(",");
      }
      rows.get(i).toJson(writer);
    }

    writer.write("]");
  }

  byte[] getIndexHtml(String file) throws IOException
  {
    InputStream indexStream = MetaHandler.class.getResourceAsStream("/" + file);
    byte[] buffer = new byte[1024];
    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
    int read;

    while ((read = indexStream.read(buffer)) != -1)
    {
      bytesOut.write(buffer, 0, read);
    }

    indexStream.close();

    return bytesOut.toByteArray();
  }

  class RouteTableRow
  {
    String link;

    String path;

    String httpMethods;

    String acceptFormats;

    String classMethod;

    RouteTableRow(RouteNode routeNode, HttpServletRequest request)
    {
      path = routeNode.routeConfig.route;
      link = request.getContextPath() + path;

      if ((routeNode.routeConfig.httpMethods == null) || routeNode.routeConfig.httpMethods.isEmpty())
      {
        httpMethods = "";
      }
      else
      {
        for (HttpMethod httpMethod : routeNode.routeConfig.httpMethods)
        {
          if (httpMethods == null)
          {
            httpMethods = httpMethod.toString();
          }
          else
          {
            httpMethods += ", " + httpMethod;
          }
        }
      }

      if ((routeNode.routeConfig.acceptedFormats == null) || routeNode.routeConfig.acceptedFormats.isEmpty())
      {
        acceptFormats = "";
      }
      else
      {
        for (Format.Type acceptFormat : routeNode.routeConfig.acceptedFormats)
        {
          if (acceptFormats == null)
          {
            acceptFormats = acceptFormat.name;
          }
          else
          {
            acceptFormats += ", " + acceptFormat.name;
          }
        }
      }

      classMethod = routeNode.method.getDeclaringClass().getSimpleName() + ":" + routeNode.method.getName();
    }

    void toJson(PrintWriter writer) throws IOException
    {
      writer.write("{\"link\": \"" + link + "\", \"path\": \"" + path + "\", \"httpMethods\": \"" + httpMethods + "\", \"acceptFormats\": \"" + acceptFormats + "\", \"classMethod\": \"" + classMethod + "\"}");
    }
  }


}
