package me.mrbast.dadamathparser;

import java.util.Map;

public interface Expression {
    double evaluate(Map<String, Double> variables);
}