package io.github.hadymic.log.function;

import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.node.DiffNode;
import io.github.hadymic.log.annotation.OptLogFunc;

import java.util.Objects;

/**
 * Diff函数
 *
 * @author Hadymic
 */
@OptLogFunc
public interface IDiffFunction {

    @OptLogFunc("Diff")
    default String diff(final Object source, final Object target) {
        if (source == null && target == null) {
            return "";
        }
        if (source != null && target != null &&
                !Objects.equals(source.getClass(), target.getClass())) {
            return "";
        }
        DiffNode diffNode = ObjectDifferBuilder.buildDefault().compare(target, source);
        return diffByDiffNode(source, target, diffNode);
    }

    String diffByDiffNode(final Object source, final Object target, DiffNode diffNode);
}
