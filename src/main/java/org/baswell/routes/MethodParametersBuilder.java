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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.baswell.routes.RoutesMethods.*;

class MethodParametersBuilder
{
  List<MethodParameter> buildParameters(Method method)
  {
    return buildParameters(method, null);
  }
  
  List<MethodParameter> buildParameters(Method method, Criteria routeCriteria) throws RoutesException
  {
    List<MethodParameter> routeParameters = new ArrayList<MethodParameter>();
    Type[] parameters = method.getGenericParameterTypes();

    int routeIndex = 0;
    int groupIndex = 0;
    CriterionForPathSegment currentPathSegmentCriteron = null;
    CriterionForParameter currentParameterCriteron = null;

    boolean pathSegmentsProcessed = false;
    Type allowedRequestContentType = null;
    
    PARAMETERS_LOOP: for (int i = 0; i < parameters.length; i++)
    {
      Type parameter = parameters[i];
      Class parameterClass = typeToClass(parameter);
      if (parameterClass == HttpServletRequest.class)
      {
        routeParameters.add(new MethodParameter(MethodRouteParameterType.SERVLET_REQUEST));
      }
      else if (parameterClass == HttpServletResponse.class)
      {
        routeParameters.add(new MethodParameter(MethodRouteParameterType.SERVLET_RESPONSE));
      }
      else if (parameterClass == HttpSession.class)
      {
        routeParameters.add(new MethodParameter(MethodRouteParameterType.SESSION));
      }
      else if (parameterClass == RequestPath.class)
      {
        routeParameters.add(new MethodParameter(MethodRouteParameterType.REQUEST_PATH));
      }
      else if (parameterClass == RequestParameters.class)
      {
        routeParameters.add(new MethodParameter(MethodRouteParameterType.REQUEST_PARAMETERS));
      }
      else if (parameterClass == RequestContent.class)
      {
        Type requestContentType = ((ParameterizedType)method.getGenericParameterTypes()[i]).getActualTypeArguments()[0];

        if ((allowedRequestContentType != null) && (allowedRequestContentType != requestContentType))
        {
          throw new RoutesException("RequestContent with different types are not allowed.");
        }

        allowedRequestContentType = requestContentType;

        routeParameters.add(new MethodParameter(MethodRouteParameterType.REQUEST_CONTENT, requestContentType));
      }
      else if (parameterClass == RequestedMediaType.class)
      {
        routeParameters.add(new MethodParameter(MethodRouteParameterType.REQUESTED_MEDIA_TYPE));
      }
      else if (parameterClass == URL.class)
      {
        routeParameters.add(new MethodParameter(MethodRouteParameterType.URL));
      }
      else if (parameterClass == Map.class)
      {
        MethodRouteParameterType mapType = getRouteMapParameterType(parameter);
        if (mapType != null)
        {
          routeParameters.add(new MethodParameter(mapType));
        }
        else
        {
          throw new RoutesException("Unsupported Map parameter: " + parameter+ " at index: " + i + " in method: " + method);
        }
      }
      else if (routeCriteria != null)
      {
        MethodPathParameterType methodPathParameterType = getMethodRouteType(parameter);

        if (methodPathParameterType != null)
        {
          if (!pathSegmentsProcessed)
          {
            if ((currentPathSegmentCriteron == null) || (groupIndex >= currentPathSegmentCriteron.numberPatternGroups))
            {
              currentPathSegmentCriteron = null;
              groupIndex = 0;

              while (routeIndex < routeCriteria.pathCriteria.size())
              {
                CriterionForPathSegment pathSegmentCriterion = routeCriteria.pathCriteria.get(routeIndex++);
                if (pathSegmentCriterion.type == CriterionForPathSegment.RequestPathSegmentCrierionType.PATTERN)
                {
                  currentPathSegmentCriteron = pathSegmentCriterion;
                  break;
                }
              }
            }

            if (currentPathSegmentCriteron != null)
            {
              if (parameterClass != List.class)
              {
                routeParameters.add(new MethodParameter(MethodRouteParameterType.ROUTE_PATH, currentPathSegmentCriteron.index, currentPathSegmentCriteron.numberPatternGroups > 0 ? groupIndex++ : null, methodPathParameterType));
                continue PARAMETERS_LOOP;
              }
              else
              {
                throw new RoutesException("List method parameter: " + parameter+ " at index: " + i + " in method: " + method + " is mapped to path segment (" + currentPathSegmentCriteron.index + "). Path segments cannot be mapped to lists only parameters.");
              }
            }

            pathSegmentsProcessed = true;
            routeIndex = groupIndex =0;
          }

          if ((currentParameterCriteron == null) || groupIndex >= currentParameterCriteron.numberPatternGroups)
          {
            currentParameterCriteron = null;
            groupIndex = 0;

            while (routeIndex < routeCriteria.parameterCriteria.size())
            {
              CriterionForParameter parameterCriterion = routeCriteria.parameterCriteria.get(routeIndex++);
              if (parameterCriterion.type == CriterionForParameter.RequestParameterType.PATTERN)
              {
                currentParameterCriteron = parameterCriterion;
                break;
              }
            }
          }

          if (currentParameterCriteron != null)
          {
            if (currentParameterCriteron.presenceRequired || !parameterClass.isPrimitive())
            {
              MethodRouteParameterType methodRouteParameterType = parameterClass == List.class ? MethodRouteParameterType.ROUTE_PARAMETERS : MethodRouteParameterType.ROUTE_PARAMETER;
              routeParameters.add(new MethodParameter(methodRouteParameterType, currentParameterCriteron.name, groupIndex++, methodPathParameterType));
              continue PARAMETERS_LOOP;
            }
            else
            {
              throw new RoutesException("Primitive method parameter: " + parameter + " at index " + i + " cannot be mapped to optional parameters in method: " + method);
            }
          }

          throw new RoutesException("Unmapped method parameter: " + parameter + " at index: " + i + " in method: " + method);
        }
        else
        {
          throw new RoutesException("Unsupported method parameter: " + parameter+ " at index: " + i + " in method: " + method);
        }
      }
      else
      {
        throw new RoutesException("Unsupported method parameter: " + parameter+ " at index: " + i + " in method: " + method);
      }
    }
    
    return routeParameters;
  }

  static MethodRouteParameterType getRouteMapParameterType(Type mapParameter)
  {
    if (mapParameter instanceof ParameterizedType)
    {
      Type[] types = ((ParameterizedType)mapParameter).getActualTypeArguments();
      if ((types.length == 2) && (types[0] == String.class))
      {
        if ((types[1] instanceof ParameterizedType))
        {
          ParameterizedType pt = (ParameterizedType)types[1];
          if (pt.getRawType() == List.class)
          {
            Type[] listTypes = pt.getActualTypeArguments();
            return ((listTypes.length == 1) && (listTypes[0] == String.class)) ? MethodRouteParameterType.PARAMETER_LIST_MAP : null;
          }
          else
          {
            return null;
          }
        }
        else if (types[1] == String.class)
        {
          return MethodRouteParameterType.PARAMETER_MAP;
        }
      }
      else
      {
        return null;
      }
    }
    if (mapParameter instanceof Class)
    {
      return MethodRouteParameterType.PARAMETER_LIST_MAP;
    }
    else
    {
      return null;
    }
  }
  
  static MethodPathParameterType getMethodRouteType(Type parameter)
  {
    Class parameterClass = null;;
    if (parameter instanceof Class)
    {
      parameterClass = (Class)parameter;
    }
    else if (parameter instanceof ParameterizedType)
    {
      ParameterizedType pt = (ParameterizedType)parameter;
      if (pt.getRawType() == List.class)
      {
        Type[] types = ((ParameterizedType)parameter).getActualTypeArguments();
        if ((types.length == 1) && (types[0] instanceof Class))
        {
          parameterClass = (Class)types[0];
        }
      }
    }
    
    if (parameterClass != null)
    {
      if (parameterClass == String.class)
      {
        return MethodPathParameterType.STRING;
      }
      else if ((parameterClass == Character.class) || (parameterClass == char.class))
      {
        return MethodPathParameterType.CHARCTER;
      }
      else if ((parameterClass == Boolean.class) || (parameterClass == boolean.class))
      {
        return MethodPathParameterType.BOOLEAN;
      }
      else if ((parameterClass == Byte.class) || (parameterClass == byte.class))
      {
        return MethodPathParameterType.BYTE;
      }
      else if ((parameterClass == Short.class) || (parameterClass == short.class))
      {
        return MethodPathParameterType.SHORT;
      }
      else if ((parameterClass == Integer.class) || (parameterClass == int.class))
      {
        return MethodPathParameterType.INTEGER;
      }
      else if ((parameterClass == Long.class) || (parameterClass == long.class))
      {
        return MethodPathParameterType.LONG;
      }
      else if ((parameterClass == Float.class) || (parameterClass == float.class))
      {
        return MethodPathParameterType.FLOAT;
      }
      else if ((parameterClass == Double.class) || (parameterClass == double.class))
      {
        return MethodPathParameterType.DOUBLE;
      }
    }

    return null;
  }
}
