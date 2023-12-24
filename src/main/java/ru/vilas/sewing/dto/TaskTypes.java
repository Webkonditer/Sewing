package ru.vilas.sewing.dto;

import lombok.Getter;

@Getter
public enum TaskTypes {
    QUANTITATIVE("Количественная"),
    HOURLY("Часовая"),
    PACKAGING("Упаковка");

    private final String russianName;

    TaskTypes(String russianName) {
        this.russianName = russianName;
    }

}
