package de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.annotation;

import de.staatsbibliothek.berlin.hsp.fo.indexer.testutil.extension.EmbeddedSolr;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Indicates that a fields value should be injected by
 * {@link EmbeddedSolr} with an {@link EmbeddedSolrInstance}.
 * This will work only in test classes extended with
 * {@link EmbeddedSolr} for fields with type {@link EmbeddedSolrInstance}.
 */


@Retention(RUNTIME)
@Target(FIELD)
public @interface EmbeddedSolrInstance {
}
