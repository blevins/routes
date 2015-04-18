package org.baswell.routes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Default route information for all route methods contained within the annotated class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Routes
{
  /**
   * The base matching URI paths. If provided these values will be prepended to all {@link Route#value()} paths. Multiple
   * values can be specified here here so that a routes class supports multiple root urls.
   */
  String[] value() default {};

  /**
   * If provided this value will be prepended to all forward paths returned by routes of the annotated class.
   */
  String forwardPath() default "";

  /**
   * The default type of media all routes in this class know how to serve. Overridden by {@link Route#respondsToMedia()}.
   */
  MediaType[] defaultRespondsToMedia() default {};

  /**
   * Should public methods be candidates for HTTP routes if they aren't annotated with {@link org.baswell.routes.Route}?
   * If true only unannotated, public methods of the immediate class will be used. Public, unannotated methods of extended
   * classes will not be candidates (ex. {@link java.lang.Object#equals(Object)}. If this attribute is not explicitly set
   * then the {@link org.baswell.routes.RoutesConfiguration#routeUnannotatedPublicMethods}  will be used.
   *
   * This should be used as a single value. Only the first value in the array will be used.
   */
  boolean[] routeUnannotatedPublicMethods() default {};

  /**
   * Default content type returned for routes in this class. This can be overridden in {@link Route#contentType()}, or by
   * explicitly calling {@link javax.servlet.http.HttpServletResponse#setContentType(String)} in the route method.
   */
  String defaultContentType() default "";
  
  /**
   *
   * If true by default strings returned from routes of this annotated class are sent back as the content. If false by default
   * returned strings are interpreted as file paths that the request is forwarded to. This can be overridden in
   * {@link Route#returnedStringIsContent()}.
   *
   * This should be used as a single value. Only the first value in the array will be used.
   *
   * Default value: <code>false</code>
   */
  boolean[] defaultReturnedStringIsContent() default {};

  /**
   * Tags to apply to all routes within the annotated class. If a route method defines its own tags, they will be added to
   * the tags defined here. Tags defined here will be added to any tags defined explicitly in the Route ({@link org.baswell.routes.Route#tags()}).
   *
   * @see BeforeRoute#exceptTags()
   * @see BeforeRoute#onlyTags()
   * @see AfterRoute#exceptTags()
   * @see AfterRoute#onlyTags()
   */
  String[] tags() default {};

}
