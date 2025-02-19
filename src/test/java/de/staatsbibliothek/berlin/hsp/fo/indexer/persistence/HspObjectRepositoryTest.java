package de.staatsbibliothek.berlin.hsp.fo.indexer.persistence;

import de.staatsbibliothek.berlin.hsp.fo.indexer.persistence.impl.RepositoryImpl;
import de.staatsbibliothek.berlin.hsp.fo.indexer.type.HspObjectType;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.*;
import java.util.stream.Stream;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

public class HspObjectRepositoryTest extends BasePersistenceServiceTest {

  private static RepositoryImpl<HspBaseTestClass> repository;
  /* necessary when checking if first repository really only works on the given type */
  private static RepositoryImpl<HspBaseTestClass> secondRepository;
  private final Map<HspObjectType, RepositoryImpl<HspBaseTestClass>> repositories = new HashMap<>();

  @BeforeEach
  void beforeEach() {
    Stream.of(HspObjectType.values())
        .forEach(type -> repositories.put(type, new RepositoryImpl<HspBaseTestClass>(persistenceService, type)));
    repository = repositories.get(HspObjectType.OBJECT);
    secondRepository = repositories.get(HspObjectType.DESCRIPTION);
  }

  @Test
  void whenSaveIsCalled_ThenEntityShouldBeSaved() throws Exception {
    final HspBaseTestClass obj = new HspBaseTestClass("id", HspObjectType.OBJECT.getValue()[0]);
    final boolean saved = repository.save(obj);

    assertThat(saved, is(true));

    final Optional<HspBaseTestClass> loaded = repository.findById("id");
    assertThat(loaded, isPresentAndIs(obj));
  }

  @Test
  void whenFindByIdIsCalled_thenMathingEntityShouldBeReturned() throws Exception {
    final HspBaseTestClass obj = new HspBaseTestClass("id", HspObjectType.OBJECT.getValue()[0]);
    final HspBaseTestClass obj2 = new HspBaseTestClass("id2", HspObjectType.OBJECT.getValue()[0]);

    final boolean saved = repository.saveAll(List.of(obj, obj2));
    assertThat(saved, is(true));

    final Optional<HspBaseTestClass> loaded = repository.findById("id2");
    assertThat(loaded, isPresentAndIs(obj2));
  }

  @Test
  void whenFindByIdIsCalledWithNotExistingId_thenNothingShouldBeReturned() throws Exception {
    final HspBaseTestClass obj = new HspBaseTestClass("id2", HspObjectType.OBJECT.getValue()[0]);
    final boolean saved = repository.save(obj);
    assertThat(saved, is(true));

    final Optional<HspBaseTestClass> loaded = repository.findById("id");
    assertThat(loaded, isEmpty());
  }

  @Test
  void whenFindByIdIsCalledWithWrongType_thenNothingShouldBeReturned() throws Exception {
    final HspBaseTestClass obj = new HspBaseTestClass("id", HspObjectType.OBJECT.getValue()[0]);
    final boolean saved = repository.save(obj);
    assertThat(saved, is(true));

    final Optional<HspBaseTestClass> loaded = secondRepository.findById("id");
    assertThat(loaded, isEmpty());
  }

  @Test
  void whenDeleteIsCalled_thenMatchingEntityShouldBeRemoved() throws Exception {
    final HspBaseTestClass obj = new HspBaseTestClass("id", HspObjectType.OBJECT.getValue()[0]);
    final HspBaseTestClass obj2 = new HspBaseTestClass("id2", HspObjectType.OBJECT.getValue()[0]);
    final boolean saved = repository.saveAll(List.of(obj, obj2));
    assertThat(saved, is(true));

    Optional<HspBaseTestClass> loaded = repository.findById("id");
    assertThat(loaded, isPresentAndIs(obj));

    loaded = repository.findById("id2");
    assertThat(loaded, isPresentAndIs(obj2));

    final Boolean deleted = repository.delete("id-search: id");
    assertThat(deleted, is(true));

    loaded = repository.findById("id");
    assertThat(loaded, isEmpty());

    loaded = repository.findById("id2");
    assertThat(loaded, isPresentAndIs(obj2));
  }

  @EnumSource(HspObjectType.class)
  @ParameterizedTest
  void whenDeleteAllIsCalled_thenAllEntitiesOfAssociatedTypeShouldBeDeleted(final HspObjectType type) throws Exception {
    final HspObjectType anotherType = getAnotherType(type);
    final HspBaseTestClass obj = new HspBaseTestClass("id", type.getValue()[0]);
    final HspBaseTestClass anotherObj = new HspBaseTestClass("another-id", anotherType.getValue()[0]);
    boolean saved = repositories.get(type)
        .save(obj) && repositories.get(anotherType)
        .save(anotherObj);

    assertThat(saved, is(true));

    Optional<HspBaseTestClass> loaded = repositories.get(type)
        .findById(obj.getId());
    assertThat(loaded, isPresentAndIs(obj));

    Optional<HspBaseTestClass> loadedDesc = repositories.get(anotherType)
        .findById(anotherObj.getId());
    assertThat(loadedDesc, isPresentAndIs(anotherObj));

    final Boolean deleted = repositories.get(type)
        .deleteAll();
    assertThat(deleted, is(true));

    loaded = repositories.get(type)
        .findById(obj.getId());
    assertThat(loaded, isEmpty());

    loaded = repositories.get(anotherType)
        .findById(anotherObj.getId());
    assertThat(loaded, isPresentAndIs(anotherObj));
  }

  @Test
  void whenDeleteByGroupIdIsCalled_thenAllEntitiesWithMatchingGroupIdAndAssociatedTypeShouldBeDeleted() throws Exception {
    final HspBaseTestClass obj = new HspBaseTestClass("id", HspObjectType.OBJECT.getValue()[0], "group-id-test");
    final HspBaseTestClass obj2 = new HspBaseTestClass("id2", HspObjectType.OBJECT.getValue()[0], "group-id-test2");
    final HspBaseTestClass desc = new HspBaseTestClass("id-desc", HspObjectType.DESCRIPTION.getValue()[0], "group-id-test");
    boolean saved = repository.saveAll(List.of(obj, obj2)) && secondRepository.save(desc);

    assertThat(saved, is(true));

    Optional<HspBaseTestClass> loaded = repository.findById("id");
    assertThat(loaded, isPresentAndIs(obj));

    loaded = repository.findById("id2");
    assertThat(loaded, isPresentAndIs(obj2));

    Optional<HspBaseTestClass> loadedDesc = secondRepository.findById("id-desc");
    assertThat(loadedDesc, isPresentAndIs(desc));

    final Boolean deleted = repository.delete("group-id-search:group-id-test");
    assertThat(deleted, is(true));

    loaded = repository.findById("id");
    assertThat(loaded, isEmpty());

    loaded = secondRepository.findById("id-desc");
    assertThat(loaded, isPresentAndIs(desc));
  }

  private HspObjectType getAnotherType(final HspObjectType type) {
    final int typeSize = HspObjectType.values().length;
    final int typeIndex = Arrays.asList(HspObjectType.values())
        .indexOf(type);

    return HspObjectType.values()[(typeIndex + 1) % typeSize];
  }
}
