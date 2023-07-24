package org.baswell.routes;

import jakarta.servlet.http.HttpServletRequest;

public interface RoutesRedirectHandler
{
  String getRedirectUrl(String url, HttpServletRequest request);
}
