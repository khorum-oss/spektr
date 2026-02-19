package org.khorum.oss.spektr.dsl.soap.dsl

/**
 * DSL marker annotation for SOAP envelope builder functions.
 *
 * This annotation prevents implicit receivers from outer scopes from being
 * accessible within nested DSL blocks, enforcing proper scoping and preventing
 * accidental access to outer builder contexts.
 *
 * @see DslMarker
 */
@DslMarker
annotation class SoapDslMarker
