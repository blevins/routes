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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

class BeforeRouteNode implements Comparable<BeforeRouteNode>
{
  final Method method;

  final List<MethodParameter> parameters;
  
  final boolean returnsBoolean;
  
  final Set<String> onlyTags;
  
  final Set<String> exceptTags;
  
  final Integer order;

  BeforeRouteNode(Method method, List<MethodParameter> parameters, boolean returnsBoolean, Set<String> onlyTags, Set<String> exceptTags, Integer order)
  {
    this.method = method;
    this.parameters = parameters;
    this.returnsBoolean = returnsBoolean;
    this.onlyTags = onlyTags;
    this.exceptTags = exceptTags;
    this.order = order;
  }

  @Override
  public int compareTo(BeforeRouteNode o)
  {
    int o1 = order == null ? Integer.MAX_VALUE : order;
    int o2 = o.order == null ? Integer.MAX_VALUE : o.order;
    
    if (o1 == o2)
    {
      return method.getName().compareTo(o.method.getName());
    }
    else
    {
      return o1 - o2;
    }
  }
}
