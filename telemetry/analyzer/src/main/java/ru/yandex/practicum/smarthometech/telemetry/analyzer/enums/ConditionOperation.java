package ru.yandex.practicum.smarthometech.telemetry.analyzer.enums;

import java.util.function.BiPredicate;

public enum ConditionOperation {
    EQUALS(Integer::equals),
    GREATER_THAN((a, b) -> a > b),
    LOWER_THAN((a, b) -> a < b);

    private final BiPredicate<Integer, Integer> evaluator;

    ConditionOperation(BiPredicate<Integer, Integer> evaluator) {
        this.evaluator = evaluator;
    }

    public boolean evaluate(int actual, int expected) {
        return evaluator.test(actual, expected);
    }
}