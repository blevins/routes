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
package org.baswell.routes.testroutes;

import java.util.HashSet;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.baswell.routes.BeforeRoute;

public class BaseRoutes
{
  public Set<String> methodsCalled;

  public BaseRoutes()
  {}
  
  @BeforeRoute(order=0)
  public void setupMethodsCalled(HttpServletRequest request)
  {
    methodsCalled = new HashSet<String>();
    request.setAttribute("methodsCalled", methodsCalled);
  }

}
