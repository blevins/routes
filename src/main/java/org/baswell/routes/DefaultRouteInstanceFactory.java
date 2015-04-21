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

/**
 * Create a new route object each time and discards the object after use.
 */
public class DefaultRouteInstanceFactory implements RouteInstanceFactory
{
  @Override
  public Object getInstanceOf(Class routeClass)
  {
    try
    {
      return routeClass.getConstructor().newInstance();
    }
    catch (Exception e)
    {
      throw new RouteInstantiationException("Unable to instantiate route instance: " + routeClass, e);
    }
  }

  @Override
  public void doneUsing(Object object)
  {}
}
