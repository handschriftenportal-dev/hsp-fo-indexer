package de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.xml;

import de.staatsbibliothek.berlin.hsp.fo.indexer.common.util.TypeHelper;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.model.ClassField;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.model.ClassModel;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.model.FieldSelector;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.model.FieldSource;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.postprocessing.converter.ClassToClassModelConverter;
import de.staatsbibliothek.berlin.hsp.fo.indexer.mapping.util.InstanceFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.dom4j.DocumentException;
import org.dom4j.Node;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class XMLMapper {

  private final XPathEvaluator xPathEvaluator;
  private final HashMap<String, ClassModel> mappingModelCache = new HashMap<>();;

  public XMLMapper(final PostProcessingHandler handler) {
    this.xPathEvaluator = new XPathEvaluator(handler);
  }

  public <T> Optional<T> map(final Class<T> clazz, final String xml) {
    return map(clazz, xml.getBytes(StandardCharsets.UTF_8));
  }

  public <T> Optional<T> map(final Class<T> clazz, final byte[] xml) {
    final ClassModel model = mappingModelCache.computeIfAbsent(clazz.getName(), (k) -> ClassToClassModelConverter.convert(clazz));
    try {
      return map(model, xml);
    } catch (DocumentException e) {
      log.error("Error while parsing XML", e);
      return Optional.empty();
    }
  }

  private <T> Optional<T> map(final ClassModel model, final byte[] xml) throws DocumentException {
    final Optional<T> ret = (Optional<T>) InstanceFactory.createInstance(model.getType());
    if(ret.isEmpty()) {
      return ret;
    }

    for (ClassField field : model.getFields()) {
      final List<String> values = getFieldValue(field, xml);
      if (CollectionUtils.isNotEmpty(values)) {
        final String methodName = String.format("set%s", StringUtils.capitalize(field.getName()));
        final Method method = MethodUtils.getMatchingMethod(model.getType(), methodName, field.getType());
        try {
          method.invoke(ret.get(), TypeHelper.castToJavaType(values, field.getType()).orElse(null));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
          log.warn("Error while invoking method {} {}", methodName, ex.getMessage(), ex);
        }
      }
    }
    return ret;
  }

  public List<String> getFieldValue(final ClassField field, final byte[] xml) {
    final List<String> ret = new ArrayList<>();

    for(FieldSource fieldSource : field.getSources()) {
      final List<String> values = getAttributeFieldValue(fieldSource, xml);
      ret.addAll(values);
      if(!ret.isEmpty()) {
        break;
      }
    }
    return ret;
  }

  public List<String> getAttributeFieldValue(final FieldSource source, final byte[] xml) {
    List<String> ret = new ArrayList<>();

    for (FieldSelector selector : source.getFieldSelectors()) {
      final List<Node> nodes = xPathEvaluator.getNodes(selector.isMultiValue(), selector.getXPath(), xml);
      for (Node el : nodes) {
        /* get value */
        List<String> values = xPathEvaluator.getValue(el, selector);

        List<String> filtered = values.stream()
            .filter(s -> !s.isBlank())
            .toList();

        /* add value if there is any, add default value otherwise */
        if (filtered.isEmpty()) {
          XPathEvaluator.getValue(el, selector.getDefaultValue()).ifPresent(ret::add);
        } else {
          ret.addAll(filtered);
        }

        /* add additional value */
        XPathEvaluator.getValue(el, selector.getAdditionalValue()).ifPresent(ret::add);
      }
    }
    if (source.isDistinct()) {
      ret = ret.stream().distinct().collect(Collectors.toList());
    }
    return ret;
  }
}
