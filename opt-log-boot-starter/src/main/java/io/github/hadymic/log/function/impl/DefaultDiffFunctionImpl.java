package io.github.hadymic.log.function.impl;

import de.danielbechler.diff.node.DiffNode;
import io.github.hadymic.log.annotation.OptLogField;
import io.github.hadymic.log.configuration.OptLogProperties;
import io.github.hadymic.log.context.OptLogContext;
import io.github.hadymic.log.function.IDiffFunction;
import io.github.hadymic.log.parse.OptLogSpELSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Diff函数的默认实现
 *
 * @author Hadymic
 */
@Slf4j
public class DefaultDiffFunctionImpl implements IDiffFunction {

    private OptLogProperties properties;
    private OptLogSpELSupport optLogSpELSupport;

    @Override
    public String diffByDiffNode(Object source, Object target, DiffNode diffNode) {
        if (!diffNode.hasChanges()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        diffNode.visit((node, visit) -> visitDiffNode(source, target, node, sb));
        return sb.toString().replaceAll(properties.getDiff().getField().getFieldSeparator() + "$", "");
    }

    private void visitDiffNode(Object source, Object target, DiffNode node, StringBuilder sb) {
        // only leaf-nodes with changes
        if (node.isRootNode() || !node.hasChanges() || node.getValueTypeInfo() != null) {
            return;
        }
        OptLogField fieldAnnotation = node.getFieldAnnotation(OptLogField.class);
        if (fieldAnnotation == null) {
            return;
        }
        String fieldName = getFullFieldName(node, fieldAnnotation.value());
        if (!StringUtils.hasText(fieldName)) {
            return;
        }
        String content = getDiffContent(source, target, node, fieldName, fieldAnnotation.function());
        if (StringUtils.hasText(content)) {
            sb.append(content).append(properties.getDiff().getField().getFieldSeparator());
        }
    }

    private String getDiffContent(Object source, Object target, DiffNode node, String fieldName, String function) {
        if (valueIsCollection(node, source, target)) {
            return getCollectionDiffContent(source, target, node, fieldName, function);
        }

        DiffNode.State state = node.getState();
        switch (state) {
            case ADDED:
                return properties.getDiff().getTemplate().getAdd()
                        .replace(properties.getDiff().getField().getFieldName(), fieldName)
                        .replace(properties.getDiff().getField().getTargetValue(), getFunctionResult(node.canonicalGet(target), function));
            case CHANGED:
                return properties.getDiff().getTemplate().getUpdate()
                        .replace(properties.getDiff().getField().getFieldName(), fieldName)
                        .replace(properties.getDiff().getField().getSourceValue(), getFunctionResult(node.canonicalGet(source), function))
                        .replace(properties.getDiff().getField().getTargetValue(), getFunctionResult(node.canonicalGet(target), function));
            case REMOVED:
                return properties.getDiff().getTemplate().getDelete()
                        .replace(properties.getDiff().getField().getFieldName(), fieldName)
                        .replace(properties.getDiff().getField().getSourceValue(), getFunctionResult(node.canonicalGet(source), function));
            default:
                return "";
        }
    }

    private String getCollectionDiffContent(Object source, Object target, DiffNode node, String fieldName, String function) {
        Collection<Object> sourceList = getListValue(node, source);
        Collection<Object> targetList = getListValue(node, target);
        Collection<Object> addList = listSubtract(targetList, sourceList);
        Collection<Object> delList = listSubtract(sourceList, targetList);
        String addContent = getListContent(addList, function);
        String delContent = getListContent(delList, function);
        if (StringUtils.hasText(addContent) && StringUtils.hasText(delContent)) {
            return properties.getDiff().getTemplate().getUpdateForList()
                    .replace(properties.getDiff().getField().getFieldName(), fieldName)
                    .replace(properties.getDiff().getField().getAddValues(), addContent)
                    .replace(properties.getDiff().getField().getDelValues(), delContent);
        } else if (StringUtils.hasText(addContent)) {
            return properties.getDiff().getTemplate().getAddForList()
                    .replace(properties.getDiff().getField().getFieldName(), fieldName)
                    .replace(properties.getDiff().getField().getAddValues(), addContent);
        } else if (StringUtils.hasText(delContent)) {
            return properties.getDiff().getTemplate().getDeleteForList()
                    .replace(properties.getDiff().getField().getFieldName(), fieldName)
                    .replace(properties.getDiff().getField().getDelValues(), delContent);
        } else {
            return "";
        }
    }

    private Collection<Object> getListValue(DiffNode node, Object object) {
        Object value = node.canonicalGet(object);
        // noinspection unchecked
        return value == null ? new ArrayList<>() : (Collection<Object>) value;
    }

    private Collection<Object> listSubtract(Collection<Object> minuend,
                                            Collection<Object> subTractor) {
        Collection<Object> addItemList = new ArrayList<>(minuend);
        addItemList.removeAll(subTractor);
        return addItemList;
    }

    private String getListContent(Collection<Object> list, String function) {
        String separator = properties.getDiff().getField().getListItemSeparator();
        StringBuilder sb = new StringBuilder();
        for (Object obj : list) {
            sb.append(getFunctionResult(obj, function)).append(separator);
        }
        return sb.toString().replaceAll(separator + "$", "");
    }

    private boolean valueIsCollection(DiffNode node, Object source, Object target) {
        Object sourceValue = node.canonicalGet(source);
        Object targetValue = node.canonicalGet(target);
        boolean sourceResult = false;
        boolean targetResult = false;
        if (sourceValue != null) {
            sourceResult = sourceValue instanceof Collection;
        }
        if (targetValue != null) {
            targetResult = targetValue instanceof Collection;
        }
        return sourceResult || targetResult;
    }

    private String getFullFieldName(DiffNode node, String fieldName) {
        if (node.getParentNode() == null) {
            return fieldName;
        }
        return getParentFieldName(node) + fieldName;
    }

    private String getParentFieldName(DiffNode node) {
        DiffNode parent = node.getParentNode();
        StringBuilder parentFieldName = new StringBuilder();
        while (parent != null) {
            OptLogField fieldAnnotation = parent.getFieldAnnotation(OptLogField.class);
            if (fieldAnnotation == null) {
                parent = parent.getParentNode();
                continue;
            }
            parentFieldName.insert(0, fieldAnnotation.value().concat(properties.getDiff().getField().getOfWord()));
            parent = parent.getParentNode();
        }
        return parentFieldName.toString();
    }

    private String getFunctionResult(Object value, String function) {
        if (!StringUtils.hasText(function)) {
            return String.valueOf(value);
        }
        OptLogContext.putVariable(properties.getDiff().getField().getFunctionField(), value);
        Object result = optLogSpELSupport.resolveTemplate(function);
        return String.valueOf(result);
    }

    public void setProperties(OptLogProperties properties) {
        this.properties = properties;
    }

    public void setOptLogSpELSupport(OptLogSpELSupport optLogSpELSupport) {
        this.optLogSpELSupport = optLogSpELSupport;
    }
}
