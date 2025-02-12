package com.example.spring_doc.domain.post.post.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SearchKeywordType {
    TITLE("title"),
    CONTENT("content");

    private final String value;
}
