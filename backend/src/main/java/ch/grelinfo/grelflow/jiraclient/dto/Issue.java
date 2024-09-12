package ch.grelinfo.grelflow.jiraclient.dto;

import java.util.HashMap;

public record Issue(
    String key,
    HashMap<String, Object> fields
) {}