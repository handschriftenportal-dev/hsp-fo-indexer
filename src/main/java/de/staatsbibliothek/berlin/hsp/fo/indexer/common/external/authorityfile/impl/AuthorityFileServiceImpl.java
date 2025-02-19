package de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.impl;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.external.authorityfile.*;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.ContentInformation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.client.GraphQlTransportException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * Service for fetching authority files from a given graphQL endpoint
 */
@Service
@Slf4j
public class AuthorityFileServiceImpl implements AuthorityFileService {
  //@formatter:off
  private static final String QUERY = ("query gndentity($id: String, $label: String) {"
            + "  findGNDEntityFacts(idOrName: $id, nodeLabel: $label) {"
            + "    gndId: gndIdentifier"
            + "    id"
            + "    identifier {"
            + "      text"
            + "      type"
            + "      url"
            + "    }"
            + "    preferredName"
            + "    variantName {"
            + "      name"
            + "      languageCode"
            + "    }"
            + "  }"
            + "}").replaceAll("\\p{javaSpaceChar}{2,}", " ");
  //@formatter:on

  private GraphQlService graphQlService;

  private static final String OPERATION_NAME = "gndentity";
  private static final String RESULT_PATH = "findGNDEntityFacts";

  private final AuthorityFileRepository authorityFileRepository;

  @Autowired
  public AuthorityFileServiceImpl(final GraphQlService graphQlService,
                                  final AuthorityFileRepository authorityFileRepository) {
    this.graphQlService = graphQlService;
    this.authorityFileRepository = authorityFileRepository;
  }

  @Override
  public GNDEntity[] findByIdOrName(final String idOrName, final String nodeLabel) throws AuthorityFileServiceException {
    if (authorityFileRepository.contains(idOrName)) {
      return authorityFileRepository.get(idOrName);
    }
    try {
      final GNDEntity[] result = graphQlService.find(QUERY, Map.of("id", idOrName, "label", nodeLabel), OPERATION_NAME, RESULT_PATH, GNDEntity[].class);
      authorityFileRepository.add(idOrName, result);
      return result;
    } catch (GraphQlTransportException e) {
      handleErrors(e, idOrName, nodeLabel);
    }
    return new GNDEntity[]{};
  }


  @Override
  public void resetCache() {
    authorityFileRepository.deleteAll();
  }

  @Override
  public Object resolve(String uri, ContentInformation contentInformation) throws AuthorityFileServiceException {
    return findByIdOrName(uri, contentInformation.getType()
        .getType());
  }

  private void handleErrors(final GraphQlTransportException response, final String idOrName, final String nodeLabel) throws AuthorityFileServiceException {
    if (Objects.requireNonNull(response.getMessage()).contains("UnknownHostException")) {
      throw new AuthorityFileServiceException(response.getMessage(), true, true);
    } else {
      log.warn("An error occurred while querying AuthorityFileServiceImpl for {} with node {}: {}", idOrName, nodeLabel, response.getMessage());
    }
  }
}
