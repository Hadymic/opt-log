package io.github.hadymic.log.function.impl;

import de.danielbechler.diff.node.DiffNode;
import io.github.hadymic.log.annotation.OptLogField;
import io.github.hadymic.log.configuration.OptLogProperties;
import io.github.hadymic.log.function.IDiffFunction;
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

    @Override
    public String diffByDiffNode(Object source, Object target, DiffNode diffNode) {
        if (!diffNode.hasChanges()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        diffNode.visit((node, visit) -> visitDiffNode(source, target, node, sb));
        return sb.toString().replaceAll(properties.getField().getFieldSeparator() + "$", "");
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
        String content = getDiffContent(source, target, node, fieldName);
        if (StringUtils.hasText(content)) {
            sb.append(content).append(properties.getField().getFieldSeparator());
        }
    }

    private String getDiffContent(Object source, Object target, DiffNode node, String fieldName) {
        if (valueIsCollection(node, source, target)) {
            return getCollectionDiffContent(source, target, node, fieldName);
        }

        DiffNode.State state = node.getState();
        switch (state) {
            case ADDED:
                return properties.getDiffTemplate().getAdd()
                        .replace(properties.getField().getFieldName(), fieldName)
                        .replace(properties.getField().getTargetValue(), String.valueOf(node.canonicalGet(target)));
            case CHANGED:
                return properties.getDiffTemplate().getUpdate()
                        .replace(properties.getField().getFieldName(), fieldName)
                        .replace(properties.getField().getSourceValue(), String.valueOf(node.canonicalGet(source)))
                        .replace(properties.getField().getTargetValue(), String.valueOf(node.canonicalGet(target)));
            case REMOVED:
                return properties.getDiffTemplate().getDelete()
                        .replace(properties.getField().getFieldName(), fieldName)
                        .replace(properties.getField().getSourceValue(), String.valueOf(node.canonicalGet(source)));
            default:
                return "";
        }
    }

    private String getCollectionDiffContent(Object source, Object target, DiffNode node, String fieldName) {
        Collection<Object> sourceList = getListValue(node, source);
        Collection<Object> targetList = getListValue(node, target);
        Collection<Object> addList = listSubtract(targetList, sourceList);
        Collection<Object> delList = listSubtract(sourceList, targetList);
        String addContent = getListContent(addList);
        String delContent = getListContent(delList);
        if (StringUtils.hasText(addContent) && StringUtils.hasText(delContent)) {
            return properties.getDiffTemplate().getUpdateForList()
                    .replace(properties.getField().getFieldName(), fieldName)
                    .replace(properties.getField().getAddValues(), addContent)
                    .replace(properties.getField().getDelValues(), delContent);
        } else if (StringUtils.hasText(addContent)) {
            return properties.getDiffTemplate().getAddForList()
                    .replace(properties.getField().getFieldName(), fieldName)
                    .replace(properties.getField().getAddValues(), addContent);
        } else if (StringUtils.hasText(delContent)) {
            return properties.getDiffTemplate().getDeleteForList()
                    .replace(properties.getField().getFieldName(), fieldName)
                    .replace(properties.getField().getDelValues(), delContent);
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

    private String getListContent(Collection<Object> list) {
        String separator = properties.getField().getListItemSeparator();
        StringBuilder sb = new StringBuilder();
        for (Object obj : list) {
            sb.append(obj).append(separator);
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
            parentFieldName.insert(0, fieldAnnotation.value().concat(properties.getField().getOfWord()));
            parent = parent.getParentNode();
        }
        return parentFieldName.toString();
    }

    public void setProperties(OptLogProperties properties) {
        this.properties = properties;
    }
}
